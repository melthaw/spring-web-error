package in.clouthink.daas.we;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author dz
 */
public class DefaultErrorResponseHandler implements ErrorResponseHandler<ResponseEntity> {

	private static final Log logger = LogFactory.getLog(DefaultErrorResponseHandler.class);

	private HandlerMethodReturnValueHandler httpEntityReturnValueHandler;

	public DefaultErrorResponseHandler(HandlerMethodReturnValueHandler httpEntityReturnValueHandler) {
		this.httpEntityReturnValueHandler = httpEntityReturnValueHandler;
	}

	@Override
	public void handle(ServletWebRequest webRequest,
					   HandlerMethod handlerMethod,
					   ResponseEntity<ResponseEntity> errorResponseEntity) {
		try {
			webRequest.getResponse().setStatus(errorResponseEntity.getStatusCode().value());
			httpEntityReturnValueHandler.handleReturnValue(errorResponseEntity,
														   handlerMethod.getReturnValueType(errorResponseEntity),
														   new ModelAndViewContainer(),
														   webRequest);
		}
		catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				logger.debug(getReturnValueHandlingErrorMessage("Error handling return value", errorResponseEntity),
							 ex);
			}
		}

	}

	private String getReturnValueHandlingErrorMessage(String message, Object returnValue) {
		StringBuilder sb = new StringBuilder(message);
		if (returnValue != null) {
			sb.append(" [type=" + returnValue.getClass().getName() + "] ");
		}
		sb.append("[value=" + returnValue + "]");
		return sb.toString();
	}


}
