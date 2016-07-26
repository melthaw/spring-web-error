package in.clouthink.daas.we;

import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dz
 */
public interface ErrorContextBuilder {

	/**
	 * @param request
	 * @param response
	 * @param handlerMethod
	 * @param exception
	 * @return
	 */
	ErrorContext build(HttpServletRequest request,
					   HttpServletResponse response,
					   HandlerMethod handlerMethod,
					   Exception exception);

}
