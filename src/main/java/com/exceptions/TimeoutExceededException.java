/**
 * 
 */
package com.exceptions;

/**
 * When the reconnecting to the FTP server exceeds the timeout.
 * @author idtaleb
 *
 */
public class TimeoutExceededException extends Exception {

	/**
	 * @param message
	 */
	public TimeoutExceededException(String message) {
		super(message);
	}

}
