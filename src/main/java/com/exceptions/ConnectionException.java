package com.exceptions;

import java.io.IOException;

public class ConnectionException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public ConnectionException(String message) {
		super(message);
	}
}
