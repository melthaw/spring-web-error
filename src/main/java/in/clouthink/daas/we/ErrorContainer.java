package in.clouthink.daas.we;

import java.util.List;

public class ErrorContainer {
    
    private ErrorCode errorCode;
    
    private String errorMessage;
    
    private String developerMessage;
    
    private String moreInfo;
    
    private List<FormError> formError;
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getDeveloperMessage() {
        return developerMessage;
    }
    
    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }
    
    public String getMoreInfo() {
        return moreInfo;
    }
    
    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }
    
    public boolean hasFormError() {
        return (formError != null && !formError.isEmpty());
    }
    
    public List<FormError> getFormError() {
        return formError;
    }
    
    public void setFormError(List<FormError> formError) {
        this.formError = formError;
    }
    
}
