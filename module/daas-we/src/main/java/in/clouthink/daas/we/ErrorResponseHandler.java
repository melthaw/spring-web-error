package in.clouthink.daas.we;

import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * @author dz
 */
public interface ErrorResponseHandler<T> {

	/**
	 * @param webRequest
	 * @param handlerMethod
	 * @param responseEntity
	 */
	void handle(ServletWebRequest webRequest, HandlerMethod handlerMethod, ResponseEntity<T> responseEntity);

}
