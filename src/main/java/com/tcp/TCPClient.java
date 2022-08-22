package com.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This is a class that represents the protocol TCP client side. 
 * Use this class to make connection to a server, send data to 
 * and read data from that server. 
 * @author idtaleb
 *
 */
public class TCPClient {
	/**
	 * The default FTP server port. 
	 */
	private static final int FTP_SERVER_PORT = 21;
	/*
	 * The socket used to make the connection with the server. 
	 */
	private Socket socket;
	/*
	 * The input buffer used to receive the data from the server.
	 */
	private BufferedReader reader;
	/*
	 * The output channel used to send commands to the server.
	 */
	private PrintWriter writer; 
	
	/**
	 * Create a connection Socket from the server host and server port.
	 * The socket uses the default FTP server(21).
	 * @param host A FTP server host
	 * @throws IOException When an I/O error occurred.
	 * @throws UnknownHostException When the specified host is not correct or may be not found.
	 */
	public TCPClient(String host) throws IOException, UnknownHostException {
		this(host, FTP_SERVER_PORT);
	}
	
	/**
	 * Create a connection Socket from the server host and server port.
	 * @param host A FTP server host
	 * @param port A FTP port server
	 * @throws IOException When an I/O error occurred.
	 * @throws UnknownHostException When the specified host is not correct or may be not found.
	 */
	public TCPClient(String host, int port) throws IOException, UnknownHostException {
		this.connect(host, port);
	}

	/**
	 * Create a connection Socket from the server host and server port.
	 * @param host A FTP server host.
	 * @param port A FTP port server.
	 * @throws IOException When an I/O error occurred.
	 * @throws UnknownHostException When the specified host is not correct or may be not found.
	 */
	public void connect(String host, int port) throws UnknownHostException, IOException {
		socket = new Socket(host, port);
		
		InputStream inS = socket.getInputStream();
		InputStreamReader isr = new InputStreamReader(inS);
		reader = new BufferedReader(isr);
		
		OutputStream out = socket.getOutputStream();
		writer = new PrintWriter(out, true);
	}
	
	/**
	 * Send a request to the server using the output stream of the socket.
	 * @param request The request to send to the server.
	 */
	public void sendRequest(String request) {
		writer.println(request);
	}
	
	/**
	 * Read the data from the input stream of the socket received from the server.
	 * @return Return the data received from the end point of the socket.
	 * @throws IOException If an I/O error occurs.
	 */
	public String getResponse() throws IOException  {
		String reply = null;
		try {
			reply = reader.readLine();
		} catch (IOException e) {
			socket.close();
		}
		return reply;
	}
	
	/**
	 * Disconnect The TCP client, closing the input and output streams 
	 * and also the socket. 
	 * @throws When an I/O error occurs due to the socket or input/output 
	 *         streams problems. 
	 */
	public void disconnect() throws IOException {
		try {
			if (reader != null) 
				reader.close();
			if (writer != null) 
				writer.close();	
			if (socket != null)
				socket.close();
		} finally {
			reader = null;
			writer = null;
			socket = null;
		}
	}
	
	/**
	 * Get the host name of the end point socket.
	 * @return The host name of the end point.
	 */
	public String getHostName() {
		return this.getSocket().getInetAddress().getHostName();
	}
	
	/**
	 * Get the port of the end point socket.
	 * @return The port of the end point.
	 */
	public int getPort() {
		return this.getSocket().getPort();
	}
	
	/**
	 * Use this method to get the data received from the server.
	 * @return The input buffer of the socket.
	 */
	public BufferedReader getReader() {
		return reader;
	}

	/**
	 * Use this method to get the output stream of the socket.
	 * @return The output stream of the socket. 
	 */
	public PrintWriter getWriter() {
		return writer;
	}

	/**
	 * @return The connection socket
	 */
	public Socket getSocket() {
		return socket;
	}
	
	
	
}
