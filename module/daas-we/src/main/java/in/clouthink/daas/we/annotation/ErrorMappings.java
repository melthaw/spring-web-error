package in.clouthink.daas.we.annotation;

import org.springframework.http.HttpStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  @author dz
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorMappings {
    
    HttpStatus httpStatus() default HttpStatus.BAD_REQUEST;
    
    Class<? extends Enum<?>>[] errorType () default {};
    
    ErrorMapping[] value() default {};
    
}
