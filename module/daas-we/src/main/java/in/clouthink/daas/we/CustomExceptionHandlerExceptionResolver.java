package in.clouthink.daas.we;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import in.clouthink.daas.we.annotation.ErrorMapping;
import in.clouthink.daas.we.annotation.ErrorMappings;

public class CustomExceptionHandlerExceptionResolver extends
                                                     ExceptionHandlerExceptionResolver
                                                     implements
                                                     InitializingBean {
                                                     
    /**
     * Log category to use when no mapped handler is found for a request.
     * 
     * @see #pageNotFoundLogger
     */
    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";
    
    /**
     * Additional logger to use when no mapped handler is found for a request.
     * 
     * @see #PAGE_NOT_FOUND_LOG_CATEGORY
     */
    protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);
    
    private boolean developerMode = true;
    
    private boolean takeOverAllMethods = true;
    
    private HandlerMethodReturnValueHandler httpEntityReturnValueHandler;
    
    private ConcurrentMap<Method, ErrorMappingCache> methodErrorMappingsCache = new ConcurrentHashMap<Method, ErrorMappingCache>();
    
    public CustomExceptionHandlerExceptionResolver(boolean developerMode) {
        super();
        this.developerMode = developerMode;
    }
    
    public boolean isTakeOverAllMethods() {
        return takeOverAllMethods;
    }
    
    public void setTakeOverAllMethods(boolean takeOverAllMethods) {
        this.takeOverAllMethods = takeOverAllMethods;
    }
    
    @Override
    protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request,
                                                           HttpServletResponse response,
                                                           HandlerMethod handlerMethod,
                                                           Exception exception) {
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        ModelAndViewContainer mavContainer = new ModelAndViewContainer();
        
        if (matched(handlerMethod)) {
            if (exception instanceof ErrorCodeProvider) {
                String errorCode = ((ErrorCodeProvider) exception).getErrorCode();
                
                ErrorMappingCache errorMappingCache = resolveErrorMappingCache(handlerMethod);
                HttpStatus httpStatus = errorMappingCache.get(errorCode);
                
                if (httpStatus == null) {
                    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                }
                
                ResponseEntity<ErrorResponse> errorResponseEntity = buildResponseEntity(exception,
                                                                                        httpStatus);
                                                                                        
                handleResponseEntityReturnValue(webRequest,
                                                mavContainer,
                                                errorResponseEntity);
            }
            else {
                // TODO take over the un-mapped exception , all be set to
                // internal server error;
                ResponseEntity<ErrorResponse> errorResponseEntity = takeOverException(request,
                                                                                      response,
                                                                                      handlerMethod,
                                                                                      exception);
                                                                                      
                handleResponseEntityReturnValue(webRequest,
                                                mavContainer,
                                                errorResponseEntity);
            }
        }
        else if (takeOverAllMethods) {
            ResponseEntity<ErrorResponse> errorResponseEntity = takeOverException(request,
                                                                                  response,
                                                                                  handlerMethod,
                                                                                  exception);
            handleResponseEntityReturnValue(webRequest,
                                            mavContainer,
                                            errorResponseEntity);
        }
        else {
            return super.doResolveHandlerMethodException(request,
                                                         response,
                                                         handlerMethod,
                                                         exception);
        }
        
        return new ModelAndView();
    }
    
    private void handleResponseEntityReturnValue(ServletWebRequest webRequest,
                                                 ModelAndViewContainer mavContainer,
                                                 ResponseEntity<ErrorResponse> errorResponseEntity) {
        try {
            webRequest.getResponse()
                      .setStatus(errorResponseEntity.getStatusCode().value());
            httpEntityReturnValueHandler.handleReturnValue(errorResponseEntity,
                                                           null,
                                                           new ModelAndViewContainer(),
                                                           webRequest);
            mavContainer.setRequestHandled(false);
        }
        catch (Exception ex) {
            if (logger.isTraceEnabled()) {
                logger.trace(getReturnValueHandlingErrorMessage("Error handling return value",
                                                                errorResponseEntity),
                             ex);
            }
        }
        
    }
    
    private String getReturnValueHandlingErrorMessage(String message,
                                                      Object returnValue) {
        StringBuilder sb = new StringBuilder(message);
        if (returnValue != null) {
            sb.append(" [type=" + returnValue.getClass().getName() + "] ");
        }
        sb.append("[value=" + returnValue + "]");
        return sb.toString();
    }
    
    private ResponseEntity<ErrorResponse> takeOverException(HttpServletRequest request,
                                                            HttpServletResponse response,
                                                            HandlerMethod handler,
                                                            Exception ex) {
                                                            
        try {
            if (ex instanceof NoSuchRequestHandlingMethodException) {
                return handleNoSuchRequestHandlingMethod((NoSuchRequestHandlingMethodException) ex,
                                                         request,
                                                         response,
                                                         handler);
            }
            else if (ex instanceof HttpRequestMethodNotSupportedException) {
                return handleHttpRequestMethodNotSupported((HttpRequestMethodNotSupportedException) ex,
                                                           request,
                                                           response,
                                                           handler);
            }
            else if (ex instanceof HttpMediaTypeNotSupportedException) {
                return handleHttpMediaTypeNotSupported((HttpMediaTypeNotSupportedException) ex,
                                                       request,
                                                       response,
                                                       handler);
            }
            else if (ex instanceof HttpMediaTypeNotAcceptableException) {
                return handleHttpMediaTypeNotAcceptable((HttpMediaTypeNotAcceptableException) ex,
                                                        request,
                                                        response,
                                                        handler);
            }
            else if (ex instanceof MissingServletRequestParameterException) {
                return handleMissingServletRequestParameter((MissingServletRequestParameterException) ex,
                                                            request,
                                                            response,
                                                            handler);
            }
            else if (ex instanceof ServletRequestBindingException) {
                return handleServletRequestBindingException((ServletRequestBindingException) ex,
                                                            request,
                                                            response,
                                                            handler);
            }
            else if (ex instanceof ConversionNotSupportedException) {
                return handleConversionNotSupported((ConversionNotSupportedException) ex,
                                                    request,
                                                    response,
                                                    handler);
            }
            else if (ex instanceof TypeMismatchException) {
                return handleTypeMismatch((TypeMismatchException) ex,
                                          request,
                                          response,
                                          handler);
            }
            else if (ex instanceof HttpMessageNotReadableException) {
                return handleHttpMessageNotReadable((HttpMessageNotReadableException) ex,
                                                    request,
                                                    response,
                                                    handler);
            }
            else if (ex instanceof HttpMessageNotWritableException) {
                return handleHttpMessageNotWritable((HttpMessageNotWritableException) ex,
                                                    request,
                                                    response,
                                                    handler);
            }
            else if (ex instanceof MethodArgumentNotValidException) {
                return handleMethodArgumentNotValidException((MethodArgumentNotValidException) ex,
                                                             request,
                                                             response,
                                                             handler);
            }
            else if (ex instanceof MissingServletRequestPartException) {
                return handleMissingServletRequestPartException((MissingServletRequestPartException) ex,
                                                                request,
                                                                response,
                                                                handler);
            }
            else if (ex instanceof BindException) {
                return handleBindException((BindException) ex,
                                           request,
                                           response,
                                           handler);
            }
            else if (ex instanceof NoHandlerFoundException) {
                return handleNoHandlerFoundException((NoHandlerFoundException) ex,
                                                     request,
                                                     response,
                                                     handler);
            }
            else if (ex instanceof AccessDeniedException) {
                return handleAccessDeniedException((AccessDeniedException) ex,
                                                   request,
                                                   response,
                                                   handler);
            }
            else {
                HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                if (ex instanceof HttpStatusProvider) {
                    HttpStatus httpStatusEx = ((HttpStatusProvider) ex).getHttpStatus();
                    if (httpStatusEx != null) {
                        httpStatus = httpStatusEx;
                    }
                }
                
                return buildResponseEntity(ex, httpStatus);
            }
        }
        catch (Exception handlerException) {
            logger.warn("Handling of [" + ex.getClass().getName()
                        + "] resulted in Exception",
                        handlerException);
        }
        
        return null;
    }
    
    private ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex,
                                                                      HttpServletRequest request,
                                                                      HttpServletResponse response,
                                                                      HandlerMethod handler) {
        return buildResponseEntity(ex, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Handle the case where no request handler method was found.
     * <p>
     * The default implementation logs a warning, sends an HTTP 404 error, and
     * returns an empty {@code ModelAndView}. Alternatively, a fallback view
     * could be chosen, or the NoSuchRequestHandlingMethodException could be
     * rethrown as-is.
     * 
     * @param ex
     *            the NoSuchRequestHandlingMethodException to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler, or {@code null} if none chosen at the
     *            time of the exception (for example, if multipart resolution
     *            failed)
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException ex,
                                                                              HttpServletRequest request,
                                                                              HttpServletResponse response,
                                                                              Object handler) throws IOException {
        pageNotFoundLogger.warn(ex.getMessage());
        
        return buildResponseEntity(ex, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle the case where no request handler method was found for the
     * particular HTTP request method.
     * <p>
     * The default implementation logs a warning, sends an HTTP 405 error, sets
     * the "Allow" header, and returns an empty {@code ModelAndView}.
     * Alternatively, a fallback view could be chosen, or the
     * HttpRequestMethodNotSupportedException could be rethrown as-is.
     * 
     * @param ex
     *            the HttpRequestMethodNotSupportedException to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler, or {@code null} if none chosen at the
     *            time of the exception (for example, if multipart resolution
     *            failed)
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                                HttpServletRequest request,
                                                                                HttpServletResponse response,
                                                                                Object handler) throws IOException {
        pageNotFoundLogger.warn(ex.getMessage());
        
        HttpHeaders headers = new HttpHeaders();
        String[] supportedMethods = ex.getSupportedMethods();
        if (supportedMethods != null) {
            headers.set("Allow",
                        StringUtils.arrayToDelimitedString(supportedMethods,
                                                           ", "));
        }
        
        return buildResponseEntity(ex, headers, HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    /**
     * Handle the case where no
     * {@linkplain org.springframework.http.converter.HttpMessageConverter
     * message converters} were found for the PUT or POSTed content.
     * <p>
     * The default implementation sends an HTTP 415 error, sets the "Accept"
     * header, and returns an empty {@code ModelAndView}. Alternatively, a
     * fallback view could be chosen, or the HttpMediaTypeNotSupportedException
     * could be rethrown as-is.
     * 
     * @param ex
     *            the HttpMediaTypeNotSupportedException to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                            HttpServletRequest request,
                                                                            HttpServletResponse response,
                                                                            Object handler) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        
        List<MediaType> mediaTypes = ex.getSupportedMediaTypes();
        if (!CollectionUtils.isEmpty(mediaTypes)) {
            headers.set("Accept", MediaType.toString(mediaTypes));
        }
        
        return buildResponseEntity(ex,
                                   headers,
                                   HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
    
    /**
     * Handle the case where no
     * {@linkplain org.springframework.http.converter.HttpMessageConverter
     * message converters} were found that were acceptable for the client
     * (expressed via the {@code Accept} header.
     * <p>
     * The default implementation sends an HTTP 406 error and returns an empty
     * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
     * the HttpMediaTypeNotAcceptableException could be rethrown as-is.
     * 
     * @param ex
     *            the HttpMediaTypeNotAcceptableException to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
                                                                             HttpServletRequest request,
                                                                             HttpServletResponse response,
                                                                             Object handler) throws IOException {
        return buildResponseEntity(ex, HttpStatus.NOT_ACCEPTABLE);
    }
    
    /**
     * Handle the case when a required parameter is missing.
     * <p>
     * The default implementation sends an HTTP 400 error, and returns an empty
     * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
     * the MissingServletRequestParameterException could be rethrown as-is.
     * 
     * @param ex
     *            the MissingServletRequestParameterException to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                                 HttpServletRequest request,
                                                                                 HttpServletResponse response,
                                                                                 Object handler) throws IOException {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle the case when an unrecoverable binding exception occurs - e.g.
     * required header, required cookie.
     * <p>
     * The default implementation sends an HTTP 400 error, and returns an empty
     * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
     * the exception could be rethrown as-is.
     * 
     * @param ex
     *            the exception to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleServletRequestBindingException(ServletRequestBindingException ex,
                                                                                 HttpServletRequest request,
                                                                                 HttpServletResponse response,
                                                                                 Object handler) throws IOException {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle the case when a {@link org.springframework.web.bind.WebDataBinder}
     * conversion cannot occur.
     * <p>
     * The default implementation sends an HTTP 500 error, and returns an empty
     * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
     * the TypeMismatchException could be rethrown as-is.
     * 
     * @param ex
     *            the ConversionNotSupportedException to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleConversionNotSupported(ConversionNotSupportedException ex,
                                                                         HttpServletRequest request,
                                                                         HttpServletResponse response,
                                                                         Object handler) throws IOException {
                                                                         
        return buildResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle the case when the un-supported exceptions occur
     * <p>
     * The default implementation sends an HTTP 500 error, and returns an empty
     * {@code ModelAndView}.
     *
     * @param ex
     *            the un-supported exception to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleInternalServerError(ConversionNotSupportedException ex,
                                                                      HttpServletRequest request,
                                                                      HttpServletResponse response,
                                                                      Object handler) throws IOException {
                                                                      
        return buildResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle the case when a {@link org.springframework.web.bind.WebDataBinder}
     * conversion error occurs.
     * <p>
     * The default implementation sends an HTTP 400 error, and returns an empty
     * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
     * the TypeMismatchException could be rethrown as-is.
     * 
     * @param ex
     *            the TypeMismatchException to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleTypeMismatch(TypeMismatchException ex,
                                                               HttpServletRequest request,
                                                               HttpServletResponse response,
                                                               Object handler) throws IOException {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle the case where a
     * {@linkplain org.springframework.http.converter.HttpMessageConverter
     * message converter} cannot read from a HTTP request.
     * <p>
     * The default implementation sends an HTTP 400 error, and returns an empty
     * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
     * the HttpMediaTypeNotSupportedException could be rethrown as-is.
     * 
     * @param ex
     *            the HttpMessageNotReadableException to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                         HttpServletRequest request,
                                                                         HttpServletResponse response,
                                                                         Object handler) throws IOException {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle the case where a
     * {@linkplain org.springframework.http.converter.HttpMessageConverter
     * message converter} cannot write to a HTTP request.
     * <p>
     * The default implementation sends an HTTP 500 error, and returns an empty
     * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
     * the HttpMediaTypeNotSupportedException could be rethrown as-is.
     * 
     * @param ex
     *            the HttpMessageNotWritableException to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotWritable(HttpMessageNotWritableException ex,
                                                                         HttpServletRequest request,
                                                                         HttpServletResponse response,
                                                                         Object handler) throws IOException {
                                                                         
        return buildResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle the case where an argument annotated with {@code @Valid} such as
     * an {@link RequestBody} or {@link RequestPart} argument fails validation.
     * An HTTP 400 error is sent back to the client.
     * 
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                                  HttpServletRequest request,
                                                                                  HttpServletResponse response,
                                                                                  Object handler) throws IOException {
                                                                                  
        if (ex.getBindingResult() != null) {
            List<FormError> formErrorList = new ArrayList<FormError>();
            for (ObjectError objectError : ex.getBindingResult()
                                             .getAllErrors()) {
                if (objectError instanceof FieldError) {
                    FormError formError = new FormError();
                    formError.setField(((FieldError) objectError).getField());
                    formError.setMessage(((FieldError) objectError).getDefaultMessage());
                    formErrorList.add(formError);
                }
            }
            
            return buildResponseEntity(ex,
                                       HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                       HttpStatus.BAD_REQUEST,
                                       formErrorList);
        }
        
        return buildResponseEntity(ex,
                                   HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                   HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle the case where an {@linkplain ModelAttribute @ModelAttribute}
     * method argument has binding or validation errors and is not followed by
     * another method argument of type {@link BindingResult}. By default an HTTP
     * 400 error is sent back to the client.
     *
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleBindException(BindException ex,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response,
                                                                Object handler) throws IOException {
                                                                
        if (ex.getBindingResult() != null) {
            List<FormError> formErrorList = new ArrayList<FormError>();
            for (ObjectError objectError : ex.getBindingResult()
                                             .getAllErrors()) {
                if (objectError instanceof FieldError) {
                    FormError formError = new FormError();
                    formError.setField(((FieldError) objectError).getField());
                    formError.setMessage(((FieldError) objectError).getDefaultMessage());
                    formErrorList.add(formError);
                }
            }
            
            return buildResponseEntity(ex,
                                       HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                       HttpStatus.BAD_REQUEST,
                                       formErrorList);
        }
        
        return buildResponseEntity(ex,
                                   HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                   HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle the case where an {@linkplain RequestPart @RequestPart}, a
     * {@link MultipartFile}, or a {@code javax.servlet.http.Part} argument is
     * required but is missing. An HTTP 400 error is sent back to the client.
     * 
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     */
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException ex,
                                                                                     HttpServletRequest request,
                                                                                     HttpServletResponse response,
                                                                                     Object handler) throws IOException {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle the case where no handler was found during the dispatch.
     * <p>
     * The default sends an HTTP 404 error, and returns an empty
     * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
     * the NoHandlerFoundException could be rethrown as-is.
     * 
     * @param ex
     *            the NoHandlerFoundException to be handled
     * @param request
     *            current HTTP request
     * @param response
     *            current HTTP response
     * @param handler
     *            the executed handler, or {@code null} if none chosen at the
     *            time of the exception (for example, if multipart resolution
     *            failed)
     * @return an empty ModelAndView indicating the exception was handled
     * @throws IOException
     *             potentially thrown from response.sendError()
     * @since 4.0
     */
    protected ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex,
                                                                          HttpServletRequest request,
                                                                          HttpServletResponse response,
                                                                          Object handler) throws IOException {
        return buildResponseEntity(ex, HttpStatus.NOT_FOUND);
    }
    
    private ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                              HttpStatus status) {
        return buildResponseEntity(ex, null, new HttpHeaders(), status, null);
    }
    
    private ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                              String message,
                                                              HttpStatus status) {
        return buildResponseEntity(ex,
                                   message,
                                   new HttpHeaders(),
                                   status,
                                   null);
    }
    
    private ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                              HttpStatus status,
                                                              List<FormError> formErrors) {
        return buildResponseEntity(ex,
                                   null,
                                   new HttpHeaders(),
                                   status,
                                   formErrors);
    }
    
    private ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                              String message,
                                                              HttpStatus status,
                                                              List<FormError> formErrors) {
        return buildResponseEntity(ex,
                                   message,
                                   new HttpHeaders(),
                                   status,
                                   formErrors);
    }
    
    private ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                              HttpHeaders headers,
                                                              HttpStatus status) {
        return buildResponseEntity(ex, null, headers, status, null);
    }
    
    private ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                              String message,
                                                              HttpHeaders headers,
                                                              HttpStatus status) {
        return buildResponseEntity(ex, message, headers, status, null);
    }
    
    private ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                              String message,
                                                              HttpHeaders headers,
                                                              HttpStatus status,
                                                              List<FormError> formErrors) {
                                                              
        ErrorResponse errorResponse = new ErrorResponse();
        if (formErrors != null || ex instanceof FormErrorProvider) {
            errorResponse = new FormErrorResponse();
        }
        
        if (ex instanceof ErrorCodeProvider) {
            String errorCode = ((ErrorCodeProvider) ex).getErrorCode();
            errorResponse.setErrorCode(errorCode);
        }
        else {
            errorResponse.setErrorCode(Integer.toString(status.value()));
        }
        
        if (!StringUtils.isEmpty(message)) {
            errorResponse.setErrorMessage(message);
        }
        else {
            errorResponse.setErrorMessage(ex.getMessage());
        }
        
        if (developerMode) {
            errorResponse.setDeveloperMessage(getStackTrace(ex));
        }
        
        if (formErrors != null) {
            ((FormErrorResponse) errorResponse).setFormErrors(formErrors);
        }
        else if (ex instanceof FormErrorProvider) {
            ((FormErrorResponse) errorResponse).setFormErrors(((FormErrorProvider) ex).getFormErrors());
        }
        
        headers.set("Content-Type", "application/json");
        return new ResponseEntity<>(errorResponse, headers, status);
    }
    
    private String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
    
    private List<Enum<?>> resolveEnums(Class<? extends Enum> enumClazz) {
        List<Enum<?>> result = new ArrayList<>();
        for (Field field : enumClazz.getDeclaredFields()) {
            if (field.isEnumConstant()) {
                result.add(Enum.valueOf(enumClazz, field.getName()));
            }
        }
        return result;
    }
    
    private ErrorMappingCache resolveErrorMappingCache(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        ErrorMappingCache result = methodErrorMappingsCache.get(method);
        if (result != null) {
            return result;
        }
        result = new ErrorMappingCache();
        
        ErrorMappings errorMappings = method.getAnnotation(ErrorMappings.class);
        
        HttpStatus defaultHttpStatus = errorMappings.httpStatus();
        
        for (Class<? extends Enum> enumClazz : errorMappings.errorType()) {
            List<Enum<?>> enums = resolveEnums(enumClazz);
            for (Enum<?> enumRef : enums) {
                if (enumRef instanceof ErrorCodeProvider) {
                    result.put(((ErrorCodeProvider) enumRef).getErrorCode(),
                               resolveHttpStatus(enumRef, defaultHttpStatus));
                }
                else if (enumRef instanceof ValueProvider) {
                    result.put(((ValueProvider) enumRef).getValue(),
                               resolveHttpStatus(enumRef, defaultHttpStatus));
                }
                else {
                    result.put(enumRef.name(),
                               resolveHttpStatus(enumRef, defaultHttpStatus));
                }
            }
        }
        
        ErrorMapping[] errorMappingArray = errorMappings.value();
        for (ErrorMapping errorMapping : errorMappingArray) {
            result.put(errorMapping.errorCode(), errorMapping.httpStatus());
        }
        
        methodErrorMappingsCache.put(method, result);
        return result;
    }
    
    private HttpStatus resolveHttpStatus(Enum<?> enumRef,
                                         HttpStatus defaultHttpStatus) {
        HttpStatus result = defaultHttpStatus;
        if (enumRef instanceof HttpStatusProvider) {
            HttpStatus httpStatus = ((HttpStatusProvider) enumRef).getHttpStatus();
            if (httpStatus != null) {
                result = httpStatus;
            }
        }
        return result;
    }
    
    private boolean matched(HandlerMethod handlerMethod) {
        return handlerMethod.getMethod()
                            .isAnnotationPresent(ErrorMappings.class);
    }
    
    @Override
    public void afterPropertiesSet() {
        if (this.httpEntityReturnValueHandler == null) {
            httpEntityReturnValueHandler = new HttpEntityMethodProcessor(getMessageConverters(),
                                                                         getContentNegotiationManager());
        }
    }
    
    private static class ErrorMappingCache extends
                                           ConcurrentHashMap<String, HttpStatus> {
                                           
        public ErrorMappingCache(int initialCapacity,
                                 float loadFactor,
                                 int concurrencyLevel) {
            super(initialCapacity, loadFactor, concurrencyLevel);
        }
        
        public ErrorMappingCache(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
        }
        
        public ErrorMappingCache(int initialCapacity) {
            super(initialCapacity);
        }
        
        public ErrorMappingCache() {
        }
        
        public ErrorMappingCache(Map<? extends String, ? extends HttpStatus> m) {
            super(m);
        }
    }
}
