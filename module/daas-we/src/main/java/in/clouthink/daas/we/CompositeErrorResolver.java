package in.clouthink.daas.we;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author dz
 */
public class CompositeErrorResolver implements ErrorResolver {

	private ErrorResolver defaultErrorResolver = new DefaultErrorResolver();

	private List<ErrorResolver> errorResolvers = new CopyOnWriteArrayList<>();

	public CompositeErrorResolver add(ErrorResolver errorResolver) {
		errorResolvers.add(errorResolver);
		return this;
	}

	public CompositeErrorResolver remove(ErrorResolver errorResolver) {
		errorResolvers.remove(errorResolver);
		return this;
	}

	public CompositeErrorResolver setDefaultErrorResolver(ErrorResolver defaultErrorResolver) {
		this.defaultErrorResolver = defaultErrorResolver;
		return this;
	}

	@Override
	public ResponseEntity resolve(ErrorContext errorContext) {
		for (ErrorResolver errorResolver : errorResolvers) {
			ResponseEntity result = errorResolver.resolve(errorContext);
			if (result != null) {
				return result;
			}
		}
		return defaultErrorResolver.resolve(errorContext);
	}

}
