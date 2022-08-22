package com.exceptions;

import java.io.IOException;

/**
 * When a bad response received from the server when a command is sent by the client.
 * A bad response is a response that not respect the RFC959{http://abcdrfc.free.fr/rfc-vf/pdf/rfc959.pdf} 
 * rules.
 * @author idtaleb
 *
 */
public class BadFormattedResponseException extends IOException {

	private static final long serialVersionUID = 1L;

	public BadFormattedResponseException(String message) {
		super(message);
	}


}
