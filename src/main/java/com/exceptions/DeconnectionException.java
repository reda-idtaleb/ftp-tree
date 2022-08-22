/**
 * 
 */
package com.exceptions;

import java.io.IOException;

/**
 * When cannot disconnect from the server, may be because of a 
 * problem when closing the connection socket or when closing 
 * the input/output control streams.
 * @author idtaleb
 *
 */
public class DeconnectionException extends IOException {

	/**
	 * @param message
	 */
	public DeconnectionException(String message) {
		super(message);
	}

}
