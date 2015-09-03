package in.clouthink.daas.we;

import org.springframework.http.HttpStatus;

public enum SharedErrorCode implements ErrorCode {
    
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE),
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private HttpStatus httpStatus;

    SharedErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return this.name();
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
