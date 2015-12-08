package in.clouthink.daas.we;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor;

public class CustomExceptionHandlerExceptionResolver extends
                                                     ExceptionHandlerExceptionResolver
                                                     implements
                                                     InitializingBean {
                                                     
    private boolean developerMode = true;
    
    private ErrorContextBuilder errorContextBuilder;
    
    private CompositeErrorResolver errorResolver = new CompositeErrorResolver();
    
    private ErrorResponseHandler errorResponseHandler;
    
    private HandlerMethodReturnValueHandler httpEntityReturnValueHandler;
    
    public CustomExceptionHandlerExceptionResolver(boolean developerMode) {
        super();
        getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        this.developerMode = developerMode;
    }
    
    public ErrorContextBuilder getErrorContextBuilder() {
        return errorContextBuilder;
    }
    
    public CustomExceptionHandlerExceptionResolver setErrorContextBuilder(ErrorContextBuilder errorContextBuilder) {
        if (errorContextBuilder == null) {
            throw new NullPointerException();
        }
        this.errorContextBuilder = errorContextBuilder;
        return this;
    }
    
    public CompositeErrorResolver getErrorResolver() {
        return errorResolver;
    }
    
    public CustomExceptionHandlerExceptionResolver setErrorResolver(CompositeErrorResolver errorResolver) {
        if (errorResolver == null) {
            throw new NullPointerException();
        }
        this.errorResolver = errorResolver;
        return this;
    }
    
    public ErrorResponseHandler getErrorResponseHandler() {
        return errorResponseHandler;
    }
    
    public CustomExceptionHandlerExceptionResolver setErrorResponseHandler(ErrorResponseHandler errorResponseHandler) {
        if (errorResponseHandler == null) {
            throw new NullPointerException();
        }
        this.errorResponseHandler = errorResponseHandler;
        return this;
    }
    
    @Override
    protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request,
                                                           HttpServletResponse response,
                                                           HandlerMethod handlerMethod,
                                                           Exception exception) {
        ErrorContext errorContext = errorContextBuilder.build(request,
                                                              response,
                                                              handlerMethod,
                                                              exception);
                                                              
        ResponseEntity errorResponseEntity = errorResolver.resolve(errorContext);
        if (errorResponseEntity == null) {
            return super.doResolveHandlerMethodException(request,
                                                         response,
                                                         handlerMethod,
                                                         exception);
        }
        
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        webRequest.getResponse()
                  .setStatus(errorResponseEntity.getStatusCode().value());
        errorResponseHandler.handle(webRequest, errorResponseEntity);
        
        return new ModelAndView();
    }
    
    @Override
    public void afterPropertiesSet() {
        if (errorContextBuilder == null) {
            this.errorContextBuilder = new DefaultErrorContextBuilder(developerMode);
        }
        if (httpEntityReturnValueHandler == null) {
            this.httpEntityReturnValueHandler = new HttpEntityMethodProcessor(getMessageConverters(),
                                                                              getContentNegotiationManager());
        }
        if (errorResponseHandler == null) {
            this.errorResponseHandler = new DefaultErrorResponseHandler(httpEntityReturnValueHandler);
        }
    }
    
}
