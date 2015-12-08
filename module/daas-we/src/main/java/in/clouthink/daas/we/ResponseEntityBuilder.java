package in.clouthink.daas.we;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 *
 */
public class ResponseEntityBuilder {
    
    private static ResponseEntityBuilder developerBuilder = new ResponseEntityBuilder(true);
    
    private static ResponseEntityBuilder productionBuilder = new ResponseEntityBuilder(false);
    
    public static ResponseEntityBuilder getBuilder(boolean developerMode) {
        return developerMode ? developerBuilder : productionBuilder;
    }
    
    public static ResponseEntityBuilder getDeveloperBuilder() {
        return developerBuilder;
    }
    
    public static ResponseEntityBuilder getProductionBuilder() {
        return productionBuilder;
    }
    
    private boolean developerMode;
    
    public ResponseEntityBuilder(boolean developerMode) {
        this.developerMode = developerMode;
    }
    
    public ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                             HttpStatus status) {
        return buildResponseEntity(ex, null, new HttpHeaders(), status, null);
    }
    
    public ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                             String message,
                                                             HttpStatus status) {
        return buildResponseEntity(ex,
                                   message,
                                   new HttpHeaders(),
                                   status,
                                   null);
    }
    
    public ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                             HttpStatus status,
                                                             List<FormError> formErrors) {
        return buildResponseEntity(ex,
                                   null,
                                   new HttpHeaders(),
                                   status,
                                   formErrors);
    }
    
    public ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                             String message,
                                                             HttpStatus status,
                                                             List<FormError> formErrors) {
        return buildResponseEntity(ex,
                                   message,
                                   new HttpHeaders(),
                                   status,
                                   formErrors);
    }
    
    public ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                             HttpHeaders headers,
                                                             HttpStatus status) {
        return buildResponseEntity(ex, null, headers, status, null);
    }
    
    public ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                             String message,
                                                             HttpHeaders headers,
                                                             HttpStatus status) {
        return buildResponseEntity(ex, message, headers, status, null);
    }
    
    public ResponseEntity<ErrorResponse> buildResponseEntity(Exception ex,
                                                             String message,
                                                             HttpHeaders headers,
                                                             HttpStatus status,
                                                             List<FormError> formErrors) {
                                                             
        ErrorResponse errorResponse = new ErrorResponse();
        if (formErrors != null || ex instanceof FormErrorProvider) {
            errorResponse = new FormErrorResponse();
        }
        
        if (ex instanceof ErrorCodeProvider) {
            String errorCode = ((ErrorCodeProvider) ex).getErrorCode();
            errorResponse.setErrorCode(errorCode);
        }
        else {
            errorResponse.setErrorCode(Integer.toString(status.value()));
        }
        
        if (!StringUtils.isEmpty(message)) {
            errorResponse.setErrorMessage(message);
        }
        else {
            errorResponse.setErrorMessage(ex.getMessage());
        }
        
        if (developerMode) {
            errorResponse.setDeveloperMessage(getStackTrace(ex));
        }
        
        if (formErrors != null) {
            ((FormErrorResponse) errorResponse).setFormErrors(formErrors);
        }
        else if (ex instanceof FormErrorProvider) {
            ((FormErrorResponse) errorResponse).setFormErrors(((FormErrorProvider) ex).getFormErrors());
        }
        
        headers.set("Content-Type", "application/json");
        return new ResponseEntity<>(errorResponse, headers, status);
    }
    
    public String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
    
}
