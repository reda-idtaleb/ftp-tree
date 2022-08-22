/**
 * 
 */
package com.exceptions;

/**
 * When the active mode cannot be activated cause of unfound port or/and host address.
 * So the server cannot connect to the client.
 * @author idtaleb
 *
 */
public class ActiveModeException extends Exception {
	/**
	 * @param message
	 */
	public ActiveModeException(String message) {
		super(message);
	}

}
