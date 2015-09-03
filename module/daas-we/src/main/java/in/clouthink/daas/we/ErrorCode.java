package in.clouthink.daas.we;

import org.springframework.http.HttpStatus;

/**
 */
public interface ErrorCode {
    
    public String getCode();
    
    public HttpStatus getHttpStatus();
    
}
