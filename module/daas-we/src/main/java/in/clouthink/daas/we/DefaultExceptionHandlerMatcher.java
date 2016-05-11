package in.clouthink.daas.we;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

/**
 * The method which is annotated with <code>@ResponseBody</code> or
 * the controller which is annotated with <code>@RestController</code>.
 */
public class DefaultExceptionHandlerMatcher implements ExceptionHandlerMatcher {

	@Override
	public boolean isMatched(HandlerMethod handlerMethod, Exception exception) {
		return (handlerMethod.getMethod().isAnnotationPresent(ResponseBody.class)) ||
			   handlerMethod.getBeanType().isAnnotationPresent(RestController.class);
	}

}
