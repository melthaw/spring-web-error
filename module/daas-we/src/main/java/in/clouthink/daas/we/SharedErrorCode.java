package in.clouthink.daas.we;

import org.springframework.http.HttpStatus;

/**
 *  @author dz
 */
public enum SharedErrorCode implements ErrorCodeProvider, HttpStatusProvider {

	BAD_REQUEST(HttpStatus.BAD_REQUEST),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
	FORBIDDEN(HttpStatus.FORBIDDEN),
	NOT_FOUND(HttpStatus.NOT_FOUND),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
	NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE),
	CONFLICT(HttpStatus.CONFLICT),
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
	UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

	private HttpStatus httpStatus;

	SharedErrorCode(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	@Override
	public String getErrorCode() {
		return Integer.toString(httpStatus.value());
	}

}
