package in.clouthink.daas.we;

import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

/**
 *
 */
public interface ErrorResponseHandler<T> {
    
    void handle(ServletWebRequest webRequest, ResponseEntity<T> responseEntity);
    
}
