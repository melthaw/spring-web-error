package in.clouthink.daas.we;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 *
 */
public class ApplicationException extends RuntimeException implements ErrorCodeProvider,
																	  HttpStatusProvider,
																	  MessageProvider,
																	  FormErrorProvider {

	private Enum<?> error;

	private List<FormError> formErrors;

	public ApplicationException(Enum<?> error) {
		this.error = error;
	}

	public ApplicationException(Enum<?> error, String message) {
		super(message);
		this.error = error;
	}

	public ApplicationException(Enum<?> error, String message, Throwable cause) {
		super(message, cause);
		this.error = error;
	}

	public ApplicationException(Enum<?> error, Throwable cause) {
		super(cause);
		this.error = error;
	}

	public ApplicationException(Enum<?> error, List<FormError> formErrors) {
		super();
		this.error = error;
		this.formErrors = formErrors;
	}

	public ApplicationException(Enum<?> error, List<FormError> formErrors, String message) {
		super(message);
		this.error = error;
		this.formErrors = formErrors;
	}

	public ApplicationException(Enum<?> error, List<FormError> formErrors, String message, Throwable cause) {
		super(message, cause);
		this.error = error;
		this.formErrors = formErrors;
	}

	public ApplicationException(Enum<?> error, List<FormError> formErrors, Throwable cause) {
		super(cause);
		this.error = error;
		this.formErrors = formErrors;
	}

	@Override
	public String getErrorCode() {
		if (error instanceof ErrorCodeProvider) {
			return ((ErrorCodeProvider) error).getErrorCode();
		}
		if (error instanceof ValueProvider) {
			return ((ValueProvider) error).getValue();
		}

		return error.toString();
	}

	@Override
	public HttpStatus getHttpStatus() {
		if (error instanceof HttpStatusProvider) {
			((HttpStatusProvider) error).getHttpStatus();
		}
		return null;
	}

	@Override
	public List<FormError> getErrorData() {
		return formErrors;
	}

}
