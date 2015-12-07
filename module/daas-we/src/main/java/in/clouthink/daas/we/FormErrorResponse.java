package in.clouthink.daas.we;

import java.util.List;

public class FormErrorResponse extends ErrorResponse {
    
    private List<FormError> formErrors;
    
    public boolean hasFormError() {
        return (formErrors != null && !formErrors.isEmpty());
    }
    
    public List<FormError> getFormErrors() {
        return formErrors;
    }
    
    public void setFormErrors(List<FormError> formErrors) {
        this.formErrors = formErrors;
    }
    
}
