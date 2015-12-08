package in.clouthink.daas.we;

import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public interface ErrorContextBuilder {
    
    ErrorContext build(HttpServletRequest request,
                       HttpServletResponse response,
                       HandlerMethod handlerMethod,
                       Exception exception);
                       
}
