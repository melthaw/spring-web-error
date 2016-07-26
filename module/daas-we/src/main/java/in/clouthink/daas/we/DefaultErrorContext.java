package in.clouthink.daas.we;

import org.springframework.web.method.HandlerMethod;

/**
 * @author dz
 */
public class DefaultErrorContext implements ErrorContext {

	private HandlerMethod handlerMethod;

	private Exception exception;

	private boolean developerMode;

	public DefaultErrorContext(HandlerMethod handlerMethod, Exception exception, boolean developerMode) {
		this.handlerMethod = handlerMethod;
		this.exception = exception;
		this.developerMode = developerMode;
	}

	@Override
	public HandlerMethod getHandlerMethod() {
		return handlerMethod;
	}

	@Override
	public Exception getException() {
		return exception;
	}

	@Override
	public boolean isDeveloperMode() {
		return developerMode;
	}
}
