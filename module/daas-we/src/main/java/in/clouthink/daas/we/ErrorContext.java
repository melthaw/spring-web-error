package in.clouthink.daas.we;

import org.springframework.web.method.HandlerMethod;

/**
 *
 */
public interface ErrorContext {
    
    HandlerMethod getHandlerMethod();
    
    Exception getException();
    
    boolean isDeveloperMode();
    
}
