package in.clouthink.daas.we;

import org.springframework.http.HttpStatus;

/**
 * @author dz
 */
public interface HttpStatusProvider {

	/**
	 * @return
	 */
	HttpStatus getHttpStatus();

}
