package in.clouthink.daas.we;

/**
 */
public class ApplicationException extends RuntimeException {
    
    private ErrorCode errorCode;
    
    private Object[] arguments;
    
    public ApplicationException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
    
    public ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ApplicationException(ErrorCode errorCode,
                                String message,
                                Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ApplicationException(ErrorCode errorCode,
                                Object[] arguments,
                                String message) {
        super(message);
        this.errorCode = errorCode;
        this.arguments = arguments;
    }
    
    public ApplicationException(ErrorCode errorCode,
                                Object[] arguments,
                                String message,
                                Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.arguments = arguments;
    }
    
    public ApplicationException(ErrorCode errorCode, Object[] arguments) {
        this.errorCode = errorCode;
        this.arguments = arguments;
    }
    
    public ApplicationException(ErrorCode errorCode,
                                Object[] arguments,
                                Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.arguments = arguments;
    }
    
    public ApplicationException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public Object[] getArguments() {
        return arguments;
    }
    
}
