package in.clouthink.daas.we;

import in.clouthink.daas.we.annotation.ErrorMapping;
import in.clouthink.daas.we.annotation.ErrorMappings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class ErrorMappingResolver implements ErrorResolver {
    
    private ConcurrentMap<Method, ErrorMappingCache> methodErrorMappingsCache = new ConcurrentHashMap<>();
    
    @Override
    public ResponseEntity resolve(ErrorContext errorContext) {
        HandlerMethod handlerMethod = errorContext.getHandlerMethod();
        Exception exception = errorContext.getException();
        boolean developerMode = errorContext.isDeveloperMode();
        if (matched(handlerMethod.getMethod())
            && (exception instanceof ErrorCodeProvider)) {
            String errorCode = ((ErrorCodeProvider) exception).getErrorCode();
            
            ErrorMappingCache errorMappingCache = resolveErrorMappingCache(handlerMethod);
            HttpStatus httpStatus = errorMappingCache.get(errorCode);
            
            if (httpStatus == null) {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            
            return ResponseEntityBuilder.getBuilder(developerMode)
                                        .buildResponseEntity(exception,
                                                             httpStatus);
        }
        
        return null;
    }
    
    private ErrorMappingCache resolveErrorMappingCache(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        ErrorMappingCache result = methodErrorMappingsCache.get(method);
        if (result != null) {
            return result;
        }
        result = new ErrorMappingCache();
        
        ErrorMappings errorMappings = method.getAnnotation(ErrorMappings.class);
        
        HttpStatus defaultHttpStatus = errorMappings.httpStatus();
        
        for (Class<? extends Enum> enumClazz : errorMappings.errorType()) {
            List<Enum<?>> enums = resolveEnums(enumClazz);
            for (Enum<?> enumRef : enums) {
                if (enumRef instanceof ErrorCodeProvider) {
                    result.put(((ErrorCodeProvider) enumRef).getErrorCode(),
                               resolveHttpStatus(enumRef, defaultHttpStatus));
                }
                else if (enumRef instanceof ValueProvider) {
                    result.put(((ValueProvider) enumRef).getValue(),
                               resolveHttpStatus(enumRef, defaultHttpStatus));
                }
                else {
                    result.put(enumRef.name(),
                               resolveHttpStatus(enumRef, defaultHttpStatus));
                }
            }
        }
        
        ErrorMapping[] errorMappingArray = errorMappings.value();
        for (ErrorMapping errorMapping : errorMappingArray) {
            result.put(errorMapping.errorCode(), errorMapping.httpStatus());
        }
        
        methodErrorMappingsCache.put(method, result);
        return result;
    }
    
    private HttpStatus resolveHttpStatus(Enum<?> enumRef,
                                         HttpStatus defaultHttpStatus) {
        HttpStatus result = defaultHttpStatus;
        if (enumRef instanceof HttpStatusProvider) {
            HttpStatus httpStatus = ((HttpStatusProvider) enumRef).getHttpStatus();
            if (httpStatus != null) {
                result = httpStatus;
            }
        }
        return result;
    }
    
    private List<Enum<?>> resolveEnums(Class<? extends Enum> enumClazz) {
        List<Enum<?>> result = new ArrayList<>();
        for (Field field : enumClazz.getDeclaredFields()) {
            if (field.isEnumConstant()) {
                result.add(Enum.valueOf(enumClazz, field.getName()));
            }
        }
        return result;
    }
    
    private boolean matched(Method method) {
        return method.isAnnotationPresent(ErrorMappings.class);
    }
    
    private static class ErrorMappingCache extends
                                           ConcurrentHashMap<String, HttpStatus> {
                                           
        public ErrorMappingCache(int initialCapacity,
                                 float loadFactor,
                                 int concurrencyLevel) {
            super(initialCapacity, loadFactor, concurrencyLevel);
        }
        
        public ErrorMappingCache(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
        }
        
        public ErrorMappingCache(int initialCapacity) {
            super(initialCapacity);
        }
        
        public ErrorMappingCache() {
        }
        
        public ErrorMappingCache(Map<? extends String, ? extends HttpStatus> m) {
            super(m);
        }
    }
    
}
