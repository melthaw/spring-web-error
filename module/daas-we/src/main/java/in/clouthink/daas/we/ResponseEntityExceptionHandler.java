package in.clouthink.daas.we;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

/**
 * It borrows methods for handling standard Spring MVC exceptions from
 * {@link org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler}
 * The difference is that all handlers use {@link HttpServletRequest} instead of
 * {@link org.springframework.web.context.request.WebRequest} because we want to
 * be able to use a {@link org.springframework.web.servlet.LocaleResolver} for
 * internationalization https://jira.spring.io/browse/SPR-8580
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
 */
@ControllerAdvice(annotations = RestController.class)
public class ResponseEntityExceptionHandler implements InitializingBean {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    /**
     * Log category to use when no mapped handler is found for a request.
     * 
     * @see #PAGE_NOT_FOUND_LOGGER
     */
    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";
    
    /**
     * Additional logger to use when no mapped handler is found for a request.
     * 
     * @see #PAGE_NOT_FOUND_LOG_CATEGORY
     */
    protected static final Log PAGE_NOT_FOUND_LOGGER = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);
    
    private boolean i18nEnabled = false;
    
    private boolean developerEnabled = false;
    
    @Autowired(required = false)
    private MessageSource resourceBundle;
    
    @Autowired(required = false)
    private LocaleResolver localeResolver;
    
    protected MessageSource getResourceBundle() {
        return resourceBundle;
    }
    
    public void setResourceBundle(MessageSource resourceBundle) {
        this.resourceBundle = resourceBundle;
    }
    
    protected LocaleResolver getLocaleResolver() {
        return this.localeResolver;
    }
    
    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }
    
    public boolean isI18nEnabled() {
        return i18nEnabled;
    }
    
    public void setI18nEnabled(boolean i18nEnabled) {
        this.i18nEnabled = i18nEnabled;
    }
    
    public boolean isDeveloperEnabled() {
        return developerEnabled;
    }
    
    public void setDeveloperEnabled(boolean developerEnabled) {
        this.developerEnabled = developerEnabled;
    }
    
    /**
     * Provides handling for standard Spring MVC exceptions.
     * 
     * @param exception
     *            the target exception
     * @param request
     *            the current request
     */
    @ExceptionHandler(value = { NoSuchRequestHandlingMethodException.class,
                                HttpRequestMethodNotSupportedException.class,
                                HttpMediaTypeNotSupportedException.class,
                                HttpMediaTypeNotAcceptableException.class,
                                MissingServletRequestParameterException.class,
                                ServletRequestBindingException.class,
                                ConversionNotSupportedException.class,
                                TypeMismatchException.class,
                                HttpMessageNotReadableException.class,
                                HttpMessageNotWritableException.class,
                                MissingServletRequestPartException.class,
                                MethodArgumentNotValidException.class,
                                BindException.class,
                                NoHandlerFoundException.class,
                                AccessDeniedException.class,
                                ApplicationException.class })
    public final ResponseEntity<Object> handleException(Exception exception,
                                                        HttpServletRequest request) {
        logger.error(exception, exception);
        final HttpHeaders headers = new HttpHeaders();
        if (exception instanceof NoSuchRequestHandlingMethodException) {
            return handleNoSuchRequestHandlingMethod((NoSuchRequestHandlingMethodException) exception,
                                                     headers,
                                                     SharedErrorCode.NOT_FOUND,
                                                     request);
        }
        else if (exception instanceof HttpRequestMethodNotSupportedException) {
            return handleHttpRequestMethodNotSupported((HttpRequestMethodNotSupportedException) exception,
                                                       headers,
                                                       SharedErrorCode.METHOD_NOT_ALLOWED,
                                                       request);
        }
        else if (exception instanceof HttpMediaTypeNotSupportedException) {
            return handleHttpMediaTypeNotSupported((HttpMediaTypeNotSupportedException) exception,
                                                   headers,
                                                   SharedErrorCode.UNSUPPORTED_MEDIA_TYPE,
                                                   request);
        }
        else if (exception instanceof HttpMediaTypeNotAcceptableException) {
            return handleHttpMediaTypeNotAcceptable((HttpMediaTypeNotAcceptableException) exception,
                                                    headers,
                                                    SharedErrorCode.NOT_ACCEPTABLE,
                                                    request);
        }
        else if (exception instanceof MissingServletRequestParameterException) {
            return handleMissingServletRequestParameter((MissingServletRequestParameterException) exception,
                                                        headers,
                                                        SharedErrorCode.BAD_REQUEST,
                                                        request);
        }
        else if (exception instanceof ServletRequestBindingException) {
            return handleServletRequestBindingException((ServletRequestBindingException) exception,
                                                        headers,
                                                        SharedErrorCode.BAD_REQUEST,
                                                        request);
        }
        else if (exception instanceof ConversionNotSupportedException) {
            return handleConversionNotSupported((ConversionNotSupportedException) exception,
                                                headers,
                                                SharedErrorCode.INTERNAL_SERVER_ERROR,
                                                request);
        }
        else if (exception instanceof TypeMismatchException) {
            return handleTypeMismatch((TypeMismatchException) exception,
                                      headers,
                                      SharedErrorCode.BAD_REQUEST,
                                      request);
        }
        else if (exception instanceof HttpMessageNotReadableException) {
            return handleHttpMessageNotReadable((HttpMessageNotReadableException) exception,
                                                headers,
                                                SharedErrorCode.BAD_REQUEST,
                                                request);
        }
        else if (exception instanceof HttpMessageNotWritableException) {
            return handleHttpMessageNotWritable((HttpMessageNotWritableException) exception,
                                                headers,
                                                SharedErrorCode.INTERNAL_SERVER_ERROR,
                                                request);
        }
        else if (exception instanceof MethodArgumentNotValidException) {
            return handleMethodArgumentNotValid((MethodArgumentNotValidException) exception,
                                                headers,
                                                SharedErrorCode.BAD_REQUEST,
                                                request);
        }
        else if (exception instanceof MissingServletRequestPartException) {
            return handleMissingServletRequestPart((MissingServletRequestPartException) exception,
                                                   headers,
                                                   SharedErrorCode.BAD_REQUEST,
                                                   request);
        }
        else if (exception instanceof BindException) {
            return handleBindException((BindException) exception,
                                       headers,
                                       SharedErrorCode.BAD_REQUEST,
                                       request);
        }
        else if (exception instanceof NoHandlerFoundException) {
            return handleNoHandlerFoundException((NoHandlerFoundException) exception,
                                                 headers,
                                                 SharedErrorCode.NOT_FOUND,
                                                 request);
        }
        else if (exception instanceof AccessDeniedException) {
            return handleAccessDeniedException((AccessDeniedException) exception,
                                               headers,
                                               SharedErrorCode.UNAUTHORIZED,
                                               request);
        }
        else if (exception instanceof ApplicationException) {
            return handleApplicationException((ApplicationException) exception,
                                              headers,
                                              request);
        }
        else {
            logger.warn("Unknown exception type: "
                        + exception.getClass().getName());
            ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                                   SharedErrorCode.UNEXPECTED_ERROR,
                                                                   request);
            return handleExceptionInternal(exception,
                                           errorContainer,
                                           headers,
                                           SharedErrorCode.UNEXPECTED_ERROR.getHttpStatus(),
                                           request);
        }
        
    }
    
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorContainer> handleException(Throwable e,
                                                          HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        
        if (!(e instanceof Exception)) {
            final ErrorContainer errorContainer = convert2ErrorContainer(e,
                                                                         SharedErrorCode.UNEXPECTED_ERROR,
                                                                         null,
                                                                         request);
                                                                         
            return new ResponseEntity<>(errorContainer,
                                        httpHeaders,
                                        SharedErrorCode.UNEXPECTED_ERROR.getHttpStatus());
        }
        
        Class errorClazz = e.getClass();
        
        ErrorCode mappedErrorCode = ErrorCodes.getInstance()
                                              .mappedErrorCode(errorClazz);
        while (mappedErrorCode == null
               && errorClazz.getSuperclass() != Object.class) {
            errorClazz = errorClazz.getSuperclass();
            mappedErrorCode = ErrorCodes.getInstance()
                                        .mappedErrorCode(errorClazz);
        }
        
        if (mappedErrorCode != null) {
            return getResponseEntity(request,
                                     httpHeaders,
                                     (Exception) e,
                                     mappedErrorCode,
                                     new Object[] {});
        }
        
        return getResponseEntity(request,
                                 httpHeaders,
                                 (Exception) e,
                                 SharedErrorCode.UNEXPECTED_ERROR,
                                 new Object[] {});
    }
    
    /**
     * Customize the response for NoSuchRequestHandlingMethodException. This
     * method logs a warning and delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException exception,
                                                                       HttpHeaders headers,
                                                                       ErrorCode errorCode,
                                                                       HttpServletRequest request) {
                                                                       
        PAGE_NOT_FOUND_LOGGER.warn(exception.getMessage());
        
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
                                                               
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * Customize the response for HttpRequestMethodNotSupportedException. This
     * method logs a warning, sets the "Allow" header, and delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException exception,
                                                                         HttpHeaders headers,
                                                                         ErrorCode errorCode,
                                                                         HttpServletRequest request) {
                                                                         
        PAGE_NOT_FOUND_LOGGER.warn(exception.getMessage());
        
        Set<HttpMethod> supportedMethods = exception.getSupportedHttpMethods();
        if (!supportedMethods.isEmpty()) {
            headers.setAllow(supportedMethods);
        }
        
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
                                                               
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * Customize the response for HttpMediaTypeNotSupportedException. This
     * method sets the "Accept" header and delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException exception,
                                                                     HttpHeaders headers,
                                                                     ErrorCode errorCode,
                                                                     HttpServletRequest request) {
                                                                     
        List<MediaType> mediaTypes = exception.getSupportedMediaTypes();
        if (!CollectionUtils.isEmpty(mediaTypes)) {
            headers.setAccept(mediaTypes);
        }
        
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
                                                               
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * Customize the response for HttpMediaTypeNotAcceptableException. This
     * method delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException exception,
                                                                      HttpHeaders headers,
                                                                      ErrorCode errorCode,
                                                                      HttpServletRequest request) {
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
                                                               
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * Customize the response for MissingServletRequestParameterException. This
     * method delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException exception,
                                                                          HttpHeaders headers,
                                                                          ErrorCode errorCode,
                                                                          HttpServletRequest request) {
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
                                                               
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * Customize the response for ServletRequestBindingException. This method
     * delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException exception,
                                                                          HttpHeaders headers,
                                                                          ErrorCode errorCode,
                                                                          HttpServletRequest request) {
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
                                                               
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * Customize the response for ConversionNotSupportedException. This method
     * delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException exception,
                                                                  HttpHeaders headers,
                                                                  ErrorCode errorCode,
                                                                  HttpServletRequest request) {
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
                                                               
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * Customize the response for TypeMismatchException. This method delegates
     * to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException exception,
                                                        HttpHeaders headers,
                                                        ErrorCode errorCode,
                                                        HttpServletRequest request) {
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
                                                               
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * Customize the response for HttpMessageNotReadableException. This method
     * delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
                                                                  HttpHeaders headers,
                                                                  ErrorCode errorCode,
                                                                  HttpServletRequest request) {
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
                                                               
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * Customize the response for HttpMessageNotWritableException. This method
     * delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException exception,
                                                                  HttpHeaders headers,
                                                                  ErrorCode errorCode,
                                                                  HttpServletRequest request) {
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
                                                               
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * Customize the response for MethodArgumentNotValidException. This method
     * delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  ErrorCode errorCode,
                                                                  HttpServletRequest request) {
        final ErrorContainer error = new ErrorContainer();
        error.setErrorCode(SharedErrorCode.BAD_REQUEST);
        error.setErrorMessage(SharedErrorCode.BAD_REQUEST.getHttpStatus()
                                                         .getReasonPhrase());
        if (exception.getBindingResult() != null) {
            List<FormError> formErrorList = new ArrayList<FormError>();
            for (ObjectError objectError : exception.getBindingResult()
                                                    .getAllErrors()) {
                if (objectError instanceof FieldError) {
                    FormError formError = new FormError();
                    formError.setField(((FieldError) objectError).getField());
                    formError.setMessage(((FieldError) objectError).getDefaultMessage());
                    formErrorList.add(formError);
                }
            }
            error.setFormError(formErrorList);
        }
        if (isDeveloperEnabled() && (exception.getStackTrace() != null)) {
            error.setDeveloperMessage(Arrays.toString(exception.getStackTrace()));
        }
        error.setMoreInfo(exception.getMessage());
        
        return new ResponseEntity<>(error,
                                    headers,
                                    SharedErrorCode.BAD_REQUEST.getHttpStatus());
    }
    
    /**
     * Customize the response for MissingServletRequestPartException. This
     * method delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException exception,
                                                                     HttpHeaders headers,
                                                                     ErrorCode errorCode,
                                                                     HttpServletRequest request) {
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * Customize the response for BindException. This method delegates to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<Object> handleBindException(BindException exception,
                                                         HttpHeaders headers,
                                                         ErrorCode errorCode,
                                                         HttpServletRequest request) {
        final ErrorContainer error = new ErrorContainer();
        error.setErrorCode(SharedErrorCode.BAD_REQUEST);
        error.setErrorMessage(SharedErrorCode.BAD_REQUEST.getHttpStatus()
                                                         .getReasonPhrase());
        if (exception.getBindingResult() != null) {
            List<FormError> formErrorList = new ArrayList<FormError>();
            for (ObjectError objectError : exception.getBindingResult()
                                                    .getAllErrors()) {
                if (objectError instanceof FieldError) {
                    FormError formError = new FormError();
                    formError.setField(((FieldError) objectError).getField());
                    formError.setMessage(((FieldError) objectError).getDefaultMessage());
                    formErrorList.add(formError);
                }
            }
            error.setFormError(formErrorList);
        }
        if (isDeveloperEnabled() && (exception.getStackTrace() != null)) {
            error.setDeveloperMessage(Arrays.toString(exception.getStackTrace()));
        }
        error.setMoreInfo(exception.getMessage());
        
        return new ResponseEntity<>(error,
                                    headers,
                                    SharedErrorCode.BAD_REQUEST.getHttpStatus());
                                    
    }
    
    /**
     * Customize the response for NoHandlerFoundException. This method delegates
     * to
     * {@link #handleExceptionInternal(Exception, ErrorContainer, HttpHeaders, HttpStatus, HttpServletRequest)}
     * .
     * 
     * @param exception
     *            the exception
     * @param headers
     *            the headers to be written to the response
     * @param errorCode
     *            the selected response status
     * @param request
     *            the current request
     * @return a {@code ResponseEntity} instance
     * @since 4.0
     */
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException exception,
                                                                   HttpHeaders headers,
                                                                   ErrorCode errorCode,
                                                                   HttpServletRequest request) {
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * @param exception
     * @param headers
     * @param errorCode
     * @param request
     * @return
     */
    protected ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException exception,
                                                                 HttpHeaders headers,
                                                                 ErrorCode errorCode,
                                                                 HttpServletRequest request) {
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * @param exception
     * @param headers
     * @param request
     * @return
     */
    protected ResponseEntity<Object> handleApplicationException(ApplicationException exception,
                                                                HttpHeaders headers,
                                                                HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        ErrorContainer errorContainer = convert2ErrorContainer(exception,
                                                               errorCode,
                                                               request);
        return handleExceptionInternal(exception,
                                       errorContainer,
                                       headers,
                                       errorCode.getHttpStatus(),
                                       request);
    }
    
    /**
     * A single place to customize the response body of all Exception types.
     * This method returns {@code null} by default.
     *
     * @param exception
     *            the exception
     * @param body
     *            the body to use for the response
     * @param headers
     *            the headers to be written to the response
     * @param status
     *            the selected response status
     * @param request
     *            the current request
     */
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception,
                                                             ErrorContainer body,
                                                             HttpHeaders headers,
                                                             HttpStatus status,
                                                             HttpServletRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute("javax.servlet.error.exception", exception);
        }
        return new ResponseEntity<Object>(body, headers, status);
    }
    
    /**
     * @param request
     *            request
     * @param httpHeaders
     *            httpHeaders
     * @param throwable
     *            throwable
     * @param errorCode
     *            errorCode
     * @return ResponseEntity
     */
    protected ResponseEntity<ErrorContainer> getResponseEntity(HttpServletRequest request,
                                                               HttpHeaders httpHeaders,
                                                               Throwable throwable,
                                                               ErrorCode errorCode) {
        return getResponseEntity(request,
                                 httpHeaders,
                                 throwable,
                                 errorCode,
                                 null);
    }
    
    /**
     * @param request
     *            request
     * @param httpHeaders
     *            httpHeaders
     * @param throwable
     *            throwable
     * @param errorCode
     *            errorCode
     * @param messageArguments
     *            messageArguments
     * @return ResponseEntity
     */
    protected ResponseEntity<ErrorContainer> getResponseEntity(HttpServletRequest request,
                                                               HttpHeaders httpHeaders,
                                                               Throwable throwable,
                                                               ErrorCode errorCode,
                                                               Object[] messageArguments) {
        final ErrorContainer errorContainer = convert2ErrorContainer(throwable,
                                                                     errorCode,
                                                                     messageArguments,
                                                                     request);
        return new ResponseEntity<>(errorContainer,
                                    httpHeaders,
                                    errorCode.getHttpStatus());
    }
    
    /**
     * @param throwable
     *            throwable
     * @param errorCode
     *            errorCode
     * @param request
     *            request
     * @return String
     */
    protected String getErrorMessage(Throwable throwable,
                                     ErrorCode errorCode,
                                     HttpServletRequest request) {
        return getErrorMessage(throwable, errorCode, null, request);
    }
    
    /**
     * @param throwable
     *            throwable
     * @param errorCode
     *            errorCode
     * @param messageArguments
     *            messageArguments
     * @param request
     *            request
     * @return String
     */
    protected String getErrorMessage(Throwable throwable,
                                     ErrorCode errorCode,
                                     Object[] messageArguments,
                                     HttpServletRequest request) {
        if (!isI18nEnabled()) {
            String result = throwable.getMessage();
            if (StringUtils.isEmpty(result)) {
                result = errorCode.getHttpStatus().getReasonPhrase();
            }
            return result;
        }
        
        Locale locale = Locale.getDefault();
        if (this.localeResolver != null) {
            locale = this.localeResolver.resolveLocale(request);
        }
        
        if (this.resourceBundle != null) {
            try {
                return resourceBundle.getMessage(errorCode.getCode(),
                                                 messageArguments,
                                                 errorCode.getHttpStatus()
                                                          .getReasonPhrase(),
                                                 locale);
            }
            catch (Exception e) {
                logger.error(e, e);
            }
        }
        
        String result = throwable.getMessage();
        if (StringUtils.isEmpty(result)) {
            result = errorCode.getHttpStatus().getReasonPhrase();
        }
        return result;
    }
    
    /**
     * @param throwable
     *            the target exception
     * @param errorCode
     *            errorCode
     * @param request
     *            request
     * @return ErrorContainer
     */
    protected ErrorContainer convert2ErrorContainer(Throwable throwable,
                                                    ErrorCode errorCode,
                                                    HttpServletRequest request) {
        return convert2ErrorContainer(throwable, errorCode, null, request);
    }
    
    /**
     * @param throwable
     *            throwable
     * @param errorCode
     *            errorCode
     * @param messageArguments
     *            messageArguments
     * @param request
     *            request
     * @return ErrorContainer
     */
    protected ErrorContainer convert2ErrorContainer(Throwable throwable,
                                                    ErrorCode errorCode,
                                                    Object[] messageArguments,
                                                    HttpServletRequest request) {
        ErrorContainer result = new ErrorContainer();
        result.setErrorCode(errorCode);
        result.setErrorMessage(getErrorMessage(throwable,
                                               errorCode,
                                               messageArguments,
                                               request));
        if (isDeveloperEnabled()) {
            if (throwable.getStackTrace() != null) {
                result.setDeveloperMessage(Arrays.toString(throwable.getStackTrace()));
            }
            result.setMoreInfo(throwable.getMessage());
        }
        return result;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (isI18nEnabled() && (getResourceBundle() == null)) {
            throw new IllegalStateException("The i18n feature is enabled but the resource bundle is not injected.");
        }
    }
    
}
