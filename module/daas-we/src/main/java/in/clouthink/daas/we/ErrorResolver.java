package in.clouthink.daas.we;

import org.springframework.http.ResponseEntity;

/**
 *
 */
public interface ErrorResolver<T> {

	/**
	 * @param errorContext
	 * @return
	 */
	ResponseEntity<T> resolve(ErrorContext errorContext);

}
