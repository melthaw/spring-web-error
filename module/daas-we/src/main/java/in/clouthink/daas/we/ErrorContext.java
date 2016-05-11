package in.clouthink.daas.we;

import org.springframework.web.method.HandlerMethod;

/**
 *
 */
public interface ErrorContext {

	/**
	 * @return
	 */
	HandlerMethod getHandlerMethod();

	/**
	 * @return
	 */
	Exception getException();

	/**
	 * @return
	 */
	boolean isDeveloperMode();

}
