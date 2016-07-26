package in.clouthink.daas.we;

/**
 *  @author dz
 */
public class ErrorDataResponse<T> extends ErrorResponse {
    
    private T errorData;
    
    public boolean hasErrorData() {
        return errorData != null;
    }

    public T getErrorData() {
        return errorData;
    }

    public void setErrorData(T errorData) {
        this.errorData = errorData;
    }
}
