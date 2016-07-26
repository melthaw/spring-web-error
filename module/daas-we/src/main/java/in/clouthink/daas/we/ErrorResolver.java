package in.clouthink.daas.we;

import org.springframework.http.ResponseEntity;

/**
 * @author dz
 */
public interface ErrorResolver<T> {

	/**
	 * @param errorContext
	 * @return
	 */
	ResponseEntity<T> resolve(ErrorContext errorContext);

}
