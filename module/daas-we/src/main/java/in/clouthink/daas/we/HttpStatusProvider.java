package in.clouthink.daas.we;

import org.springframework.http.HttpStatus;

/**
 *
 */
public interface HttpStatusProvider {

	/**
	 * @return
	 */
	HttpStatus getHttpStatus();

}
