package in.clouthink.daas.we;

import org.springframework.web.method.HandlerMethod;

/**
 *  @author dz
 */
public interface ExceptionHandlerMatcher {

	/**
	 * @param handlerMethod
	 * @param exception
	 * @return
	 */
	boolean isMatched(HandlerMethod handlerMethod, Exception exception);

}
