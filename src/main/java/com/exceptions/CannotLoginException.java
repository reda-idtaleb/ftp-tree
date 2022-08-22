/**
 * 
 */
package com.exceptions;

/**
 * When the user name or/and password are incorrect. So the server close the session.
 * @author idtaleb
 *
 */
public class CannotLoginException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public CannotLoginException(String message) {
		super(message);
	}

}
