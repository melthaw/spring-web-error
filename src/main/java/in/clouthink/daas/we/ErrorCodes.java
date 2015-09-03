package in.clouthink.daas.we;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public final class ErrorCodes {
    
    private static class ErrorCodesHolder {
        static final ErrorCodes instance = new ErrorCodes();
    }
    
    public static ErrorCodes getInstance() {
        return ErrorCodesHolder.instance;
    }
    
    private Map<Class, ErrorCode> mappedErrorCodes = new ConcurrentHashMap<Class, ErrorCode>();
    
    public void mapErrorCode(Class clazz, ErrorCode errorCode) {
        mappedErrorCodes.put(clazz, errorCode);
    }
    
    public ErrorCode mappedErrorCode(Class clazz) {
        return mappedErrorCodes.get(clazz);
    }
    
}
