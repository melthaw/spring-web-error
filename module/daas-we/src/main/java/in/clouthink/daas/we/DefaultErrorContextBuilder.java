package in.clouthink.daas.we;

import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class DefaultErrorContextBuilder implements ErrorContextBuilder {
    
    private boolean developerMode;
    
    public DefaultErrorContextBuilder(boolean developerMode) {
        this.developerMode = developerMode;
    }
    
    @Override
    public ErrorContext build(HttpServletRequest request,
                              HttpServletResponse response,
                              HandlerMethod handlerMethod,
                              Exception exception) {
        return new DefaultErrorContext(handlerMethod, exception, developerMode);
    }
    
}
