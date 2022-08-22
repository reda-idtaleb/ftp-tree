/**
 * 
 */
package com.exceptions;

import java.io.IOException;

/**
 * When the control channel is closed by the FTP server. Severals causes are possible.
 * 421 Timeout is the most common problem.
 * @author idtaleb
 *
 */
public class ControlChannelClosedException extends IOException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public ControlChannelClosedException(String message) {
		super(message);
	}

}
