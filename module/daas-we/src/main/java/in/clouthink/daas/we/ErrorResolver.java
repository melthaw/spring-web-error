package in.clouthink.daas.we;

import org.springframework.http.ResponseEntity;

/**
 *
 */
public interface ErrorResolver<T> {
    
    ResponseEntity<T> resolve(ErrorContext errorContext);
    
}
