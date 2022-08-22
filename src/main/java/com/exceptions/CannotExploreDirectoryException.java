/**
 * 
 */
package com.exceptions;

import java.io.IOException;

/**
 * When cannot change to a directory because may be the directory don't 
 * contains the execution rights or when the server closed the connection 
 * while accessing to a directory. 
 * @author idtaleb
 *
 */
public class CannotExploreDirectoryException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public CannotExploreDirectoryException(String message) {
		super(message);
	}

}
