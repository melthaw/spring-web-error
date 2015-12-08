package in.clouthink.daas.we.sample.web;

import in.clouthink.daas.we.ErrorCodeProvider;
import in.clouthink.daas.we.ErrorDataProvider;

/**
 *
 */
public class FooException extends RuntimeException
                          implements ErrorCodeProvider, ErrorDataProvider<Foo> {
                          
    private String errorCode;
    
    private Foo errorData;
    
    public FooException(String message, String errorCode, Foo errorData) {
        super(message);
        this.errorCode = errorCode;
        this.errorData = errorData;
    }
    
    @Override
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public Foo getErrorData() {
        return errorData;
    }
    
}
