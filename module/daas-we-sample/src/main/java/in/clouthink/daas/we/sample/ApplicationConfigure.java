package in.clouthink.daas.we.sample;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import in.clouthink.daas.we.CustomExceptionHandlerExceptionResolver;

@Configuration
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ComponentScan(value = "in.clouthink.daas.we.sample")
public class ApplicationConfigure extends WebMvcConfigurerAdapter {
    
    private List<HttpMessageConverter<?>> converters;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        this.converters = converters;
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        CustomExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new CustomExceptionHandlerExceptionResolver(true);
        exceptionHandlerExceptionResolver.setMessageConverters(converters);
        exceptionHandlerExceptionResolver.setContentNegotiationManager(new ContentNegotiationManager());
	    exceptionHandlerExceptionResolver.afterPropertiesSet();
        exceptionResolvers.add(exceptionHandlerExceptionResolver);
    }
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ApplicationConfigure.class, args);
    }
    
}
