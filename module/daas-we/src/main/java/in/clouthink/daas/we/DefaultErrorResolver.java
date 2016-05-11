package in.clouthink.daas.we;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DefaultErrorResolver implements ErrorResolver {

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

	private static final Log logger = LogFactory.getLog(DefaultErrorResolver.class);

	public DefaultErrorResolver() {
	}

	@Override
	public ResponseEntity resolve(ErrorContext errorContext) {
		Exception ex = errorContext.getException();
		Object handler = null;
		boolean developerMode = errorContext.isDeveloperMode();
		try {
			if (ex instanceof NoSuchRequestHandlingMethodException) {
				return handleNoSuchRequestHandlingMethod((NoSuchRequestHandlingMethodException) ex,
														 handler,
														 developerMode);
			}
			else if (ex instanceof HttpRequestMethodNotSupportedException) {
				return handleHttpRequestMethodNotSupported((HttpRequestMethodNotSupportedException) ex,
														   handler,
														   developerMode);
			}
			else if (ex instanceof HttpMediaTypeNotSupportedException) {
				return handleHttpMediaTypeNotSupported((HttpMediaTypeNotSupportedException) ex, handler, developerMode);
			}
			else if (ex instanceof HttpMediaTypeNotAcceptableException) {
				return handleHttpMediaTypeNotAcceptable((HttpMediaTypeNotAcceptableException) ex,
														handler,
														developerMode);
			}
			else if (ex instanceof MissingServletRequestParameterException) {
				return handleMissingServletRequestParameter((MissingServletRequestParameterException) ex,
															handler,
															developerMode);
			}
			else if (ex instanceof ServletRequestBindingException) {
				return handleServletRequestBindingException((ServletRequestBindingException) ex,
															handler,
															developerMode);
			}
			else if (ex instanceof ConversionNotSupportedException) {
				return handleConversionNotSupported((ConversionNotSupportedException) ex, handler, developerMode);
			}
			else if (ex instanceof TypeMismatchException) {
				return handleTypeMismatch((TypeMismatchException) ex, handler, developerMode);
			}
			else if (ex instanceof HttpMessageNotReadableException) {
				return handleHttpMessageNotReadable((HttpMessageNotReadableException) ex, handler, developerMode);
			}
			else if (ex instanceof HttpMessageNotWritableException) {
				return handleHttpMessageNotWritable((HttpMessageNotWritableException) ex, handler, developerMode);
			}
			else if (ex instanceof MethodArgumentNotValidException) {
				return handleMethodArgumentNotValidException((MethodArgumentNotValidException) ex,
															 handler,
															 developerMode);
			}
			else if (ex instanceof MissingServletRequestPartException) {
				return handleMissingServletRequestPartException((MissingServletRequestPartException) ex,
																handler,
																developerMode);
			}
			else if (ex instanceof BindException) {
				return handleBindException((BindException) ex, handler, developerMode);
			}
			else if (ex instanceof NoHandlerFoundException) {
				return handleNoHandlerFoundException((NoHandlerFoundException) ex, handler, developerMode);
			}
			else if (ex instanceof AccessDeniedException) {
				return handleAccessDeniedException((AccessDeniedException) ex, handler, developerMode);
			}
			else {
				HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				if (ex instanceof HttpStatusProvider) {
					HttpStatus httpStatusEx = ((HttpStatusProvider) ex).getHttpStatus();
					if (httpStatusEx != null) {
						httpStatus = httpStatusEx;
					}
				}

				return ResponseEntityBuilder.getBuilder(developerMode).buildResponseEntity(ex, httpStatus);
			}
		}
		catch (Exception handlerException) {
			logger.warn("Handling of [" + ex.getClass().getName() + "] resulted in Exception", handlerException);
		}

		return null;
	}

	private ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex,
																	  Object handler,
																	  boolean developerMode) {
		return ResponseEntityBuilder.getBuilder(developerMode).buildResponseEntity(ex, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Handle the case where no request handler method was found.
	 * <p>
	 * The default implementation logs a warning, sends an HTTP 404 error, and
	 * returns an empty {@code ModelAndView}. Alternatively, a fallback view
	 * could be chosen, or the NoSuchRequestHandlingMethodException could be
	 * rethrown as-is.
	 *
	 * @param ex      the NoSuchRequestHandlingMethodException to be handled
	 * @param handler the executed handler, or {@code null} if none chosen at the
	 *                time of the exception (for example, if multipart resolution
	 *                failed)
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException ex,
																			  Object handler,
																			  boolean developerMode)
			throws IOException {
		pageNotFoundLogger.warn(ex.getMessage());

		return ResponseEntityBuilder.getBuilder(developerMode).buildResponseEntity(ex, HttpStatus.NOT_FOUND);
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
	 * @param ex      the HttpRequestMethodNotSupportedException to be handled
	 * @param handler the executed handler, or {@code null} if none chosen at the
	 *                time of the exception (for example, if multipart resolution
	 *                failed)
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
																				Object handler,
																				boolean developerMode)
			throws IOException {
		pageNotFoundLogger.warn(ex.getMessage());

		HttpHeaders headers = new HttpHeaders();
		String[] supportedMethods = ex.getSupportedMethods();
		if (supportedMethods != null) {
			headers.set("Allow", StringUtils.arrayToDelimitedString(supportedMethods, ", "));
		}

		return ResponseEntityBuilder.getBuilder(developerMode)
									.buildResponseEntity(ex, headers, HttpStatus.METHOD_NOT_ALLOWED);
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
	 * @param ex      the HttpMediaTypeNotSupportedException to be handled
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
																			Object handler,
																			boolean developerMode) throws IOException {
		HttpHeaders headers = new HttpHeaders();

		List<MediaType> mediaTypes = ex.getSupportedMediaTypes();
		if (!CollectionUtils.isEmpty(mediaTypes)) {
			headers.set("Accept", MediaType.toString(mediaTypes));
		}

		return ResponseEntityBuilder.getBuilder(developerMode)
									.buildResponseEntity(ex, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
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
	 * @param ex      the HttpMediaTypeNotAcceptableException to be handled
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
																			 Object handler,
																			 boolean developerMode) throws IOException {
		return ResponseEntityBuilder.getBuilder(developerMode).buildResponseEntity(ex, HttpStatus.NOT_ACCEPTABLE);
	}

	/**
	 * Handle the case when a required parameter is missing.
	 * <p>
	 * The default implementation sends an HTTP 400 error, and returns an empty
	 * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
	 * the MissingServletRequestParameterException could be rethrown as-is.
	 *
	 * @param ex      the MissingServletRequestParameterException to be handled
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
																				 Object handler,
																				 boolean developerMode)
			throws IOException {
		return ResponseEntityBuilder.getBuilder(developerMode).buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle the case when an unrecoverable binding exception occurs - e.g.
	 * required header, required cookie.
	 * <p>
	 * The default implementation sends an HTTP 400 error, and returns an empty
	 * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
	 * the exception could be rethrown as-is.
	 *
	 * @param ex      the exception to be handled
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleServletRequestBindingException(ServletRequestBindingException ex,
																				 Object handler,
																				 boolean developerMode)
			throws IOException {
		return ResponseEntityBuilder.getBuilder(developerMode).buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle the case when a {@link org.springframework.web.bind.WebDataBinder}
	 * conversion cannot occur.
	 * <p>
	 * The default implementation sends an HTTP 500 error, and returns an empty
	 * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
	 * the TypeMismatchException could be rethrown as-is.
	 *
	 * @param ex      the ConversionNotSupportedException to be handled
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleConversionNotSupported(ConversionNotSupportedException ex,
																		 Object handler,
																		 boolean developerMode) throws IOException {

		return ResponseEntityBuilder.getBuilder(developerMode)
									.buildResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Handle the case when the un-supported exceptions occur
	 * <p>
	 * The default implementation sends an HTTP 500 error, and returns an empty
	 * {@code ModelAndView}.
	 *
	 * @param ex      the un-supported exception to be handled
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleInternalServerError(ConversionNotSupportedException ex,
																	  Object handler,
																	  boolean developerMode) throws IOException {

		return ResponseEntityBuilder.getBuilder(developerMode)
									.buildResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Handle the case when a {@link org.springframework.web.bind.WebDataBinder}
	 * conversion error occurs.
	 * <p>
	 * The default implementation sends an HTTP 400 error, and returns an empty
	 * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
	 * the TypeMismatchException could be rethrown as-is.
	 *
	 * @param ex      the TypeMismatchException to be handled
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleTypeMismatch(TypeMismatchException ex,
															   Object handler,
															   boolean developerMode) throws IOException {
		return ResponseEntityBuilder.getBuilder(developerMode).buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
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
	 * @param ex      the HttpMessageNotReadableException to be handled
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
																		 Object handler,
																		 boolean developerMode) throws IOException {
		return ResponseEntityBuilder.getBuilder(developerMode).buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
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
	 * @param ex      the HttpMessageNotWritableException to be handled
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleHttpMessageNotWritable(HttpMessageNotWritableException ex,
																		 Object handler,
																		 boolean developerMode) throws IOException {

		return ResponseEntityBuilder.getBuilder(developerMode)
									.buildResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Handle the case where an argument annotated with {@code @Valid} such as
	 * an {@link RequestBody} or {@link RequestPart} argument fails validation.
	 * An HTTP 400 error is sent back to the client.
	 *
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
																				  Object handler,
																				  boolean developerMode)
			throws IOException {

		if (ex.getBindingResult() != null) {
			List<FormError> formErrorList = new ArrayList<FormError>();
			for (ObjectError objectError : ex.getBindingResult().getAllErrors()) {
				if (objectError instanceof FieldError) {
					FormError formError = new FormError();
					formError.setField(((FieldError) objectError).getField());
					formError.setMessage(((FieldError) objectError).getDefaultMessage());
					formErrorList.add(formError);
				}
			}

			return ResponseEntityBuilder.getBuilder(developerMode)
										.buildResponseEntity(ex,
															 HttpStatus.BAD_REQUEST.getReasonPhrase(),
															 HttpStatus.BAD_REQUEST,
															 formErrorList);
		}

		return ResponseEntityBuilder.getBuilder(developerMode)
									.buildResponseEntity(ex,
														 HttpStatus.BAD_REQUEST.getReasonPhrase(),
														 HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle the case where an {@linkplain ModelAttribute @ModelAttribute}
	 * method argument has binding or validation errors and is not followed by
	 * another method argument of type {@link BindingResult}. By default an HTTP
	 * 400 error is sent back to the client.
	 *
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleBindException(BindException ex, Object handler, boolean developerMode)
			throws IOException {

		if (ex.getBindingResult() != null) {
			List<FormError> formErrorList = new ArrayList<FormError>();
			for (ObjectError objectError : ex.getBindingResult().getAllErrors()) {
				if (objectError instanceof FieldError) {
					FormError formError = new FormError();
					formError.setField(((FieldError) objectError).getField());
					formError.setMessage(((FieldError) objectError).getDefaultMessage());
					formErrorList.add(formError);
				}
			}

			return ResponseEntityBuilder.getBuilder(developerMode)
										.buildResponseEntity(ex,
															 HttpStatus.BAD_REQUEST.getReasonPhrase(),
															 HttpStatus.BAD_REQUEST,
															 formErrorList);
		}

		return ResponseEntityBuilder.getBuilder(developerMode)
									.buildResponseEntity(ex,
														 HttpStatus.BAD_REQUEST.getReasonPhrase(),
														 HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle the case where an {@linkplain RequestPart @RequestPart}, a
	 * {@link MultipartFile}, or a {@code javax.servlet.http.Part} argument is
	 * required but is missing. An HTTP 400 error is sent back to the client.
	 *
	 * @param handler the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException ex,
																					 Object handler,
																					 boolean developerMode)
			throws IOException {
		return ResponseEntityBuilder.getBuilder(developerMode).buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle the case where no handler was found during the dispatch.
	 * <p>
	 * The default sends an HTTP 404 error, and returns an empty
	 * {@code ModelAndView}. Alternatively, a fallback view could be chosen, or
	 * the NoHandlerFoundException could be rethrown as-is.
	 *
	 * @param ex      the NoHandlerFoundException to be handled
	 * @param handler the executed handler, or {@code null} if none chosen at the
	 *                time of the exception (for example, if multipart resolution
	 *                failed)
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException potentially thrown from response.sendError()
	 */
	protected ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex,
																		  Object handler,
																		  boolean developerMode) throws IOException {
		return ResponseEntityBuilder.getBuilder(developerMode).buildResponseEntity(ex, HttpStatus.NOT_FOUND);
	}

}
