package in.clouthink.daas.we.sample;

import in.clouthink.daas.we.CustomExceptionHandlerExceptionResolver;
import in.clouthink.daas.we.DefaultErrorResolver;
import in.clouthink.daas.we.DefaultExceptionHandlerMatcher;
import in.clouthink.daas.we.ErrorMappingResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ComponentScan(value = "in.clouthink.daas.we.sample")
public class ApplicationConfigure extends WebMvcConfigurerAdapter {

	@Bean
	public HandlerExceptionResolver customExceptionHandlerExceptionResolver() {
		CustomExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new CustomExceptionHandlerExceptionResolver(
				true);
		exceptionHandlerExceptionResolver.setHandlerMatcher(new DefaultExceptionHandlerMatcher())
										 .getErrorResolver()
										 .add(new ErrorMappingResolver())
										 .setDefaultErrorResolver(new DefaultErrorResolver());
		return exceptionHandlerExceptionResolver;
	}

	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		exceptionResolvers.add(customExceptionHandlerExceptionResolver());
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ApplicationConfigure.class, args);
	}

}
