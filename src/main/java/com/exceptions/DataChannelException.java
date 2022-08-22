package com.exceptions;

import java.io.IOException;

/**
 * When the data channel is closed by the FTP server.
 * @author idtaleb
 *
 */
public class DataChannelException extends IOException {

	public DataChannelException(String message) {
		super(message);	}
}
