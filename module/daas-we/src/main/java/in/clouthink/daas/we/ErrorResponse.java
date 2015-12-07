package in.clouthink.daas.we;

public class ErrorResponse {
    
    private String errorCode;
    
    private String errorMessage;
    
    private String developerMessage;
    
    private String moreInfo;
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
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
    
}
