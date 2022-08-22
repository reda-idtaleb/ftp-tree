package com.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is a class that represents the protocol TCP server side. 
 * Use this class to make connection to a client, send data to 
 * and receive command from the client. 
 * @author idtaleb
 *
 */
public class TCPServer {
	/**
	 * The listening server port.
	 */
	private int port;
	/**
	 * The server socket.
	 */
	private ServerSocket serverSocket;
	/**
	 * The socket used to establish the connection with a client.
	 */
	private Socket connectionSocket;
	/**
	 * The input buffer of the connection socket.  
	 */
	private BufferedReader reader;
	/**
	 * The output stream of the connection socket.
	 */
	private PrintWriter writer;
	
	/**
	 * Construct the TCP server using ServerSocket and initializes the input 
	 * and output streams of the connection socket. 
	 * @param port The port of the server.
	 */
	public TCPServer(int port) {
		this.port = port;
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {   
			System.out.println("Cannot handle the data active mode!");
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return The connection socket.
	 * @throws IOException When an I/O related to the socket or while 
	 *         initializing the input/output socket streams.
	 */
	public Socket accept() throws IOException {
		this.connectionSocket = serverSocket.accept();
		this._initialize_();
		return this.connectionSocket;
	}

	/**
	 * Send a response to the server using the output stream of the socket.
	 * @param request The request to send to the server.
	 */
	public void sendResponse(String request) {
		writer.println(request);
	}
	
	/**
	 * Get the command received from the input stream of the socket provided by the client.
	 * @return Return the data received from the end point(client or server) of the socket.
	 * @throws IOException If an I/O error occurs.
	 */
	public String receivedCommand() throws IOException {		
		return reader.readLine();
	}
	
	/**
	 * Disconnect from the client/server and close the connection socket.
	 * @throws IOException If an I/O error occurs. 
	 */
	public void disconnect() throws IOException{
		this._initialize_();
		this.connectionSocket.close();
	}
	
	/**
	 * @return The input stream of the connection socket.
	 */
	public BufferedReader getReader() {
		return reader;
	}

	/**
	 * @return The output stream of the connection socket.
	 */
	public PrintWriter getWriter() {
		return writer;
	}
	
	/**
	 * @return The connection socket
	 */
	public Socket getConnectionSocket() {
		return connectionSocket;
	}

	/**
	 * @return The server socket
	 */
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	
	/**
	 * Initialize all the parameters of the TCP connection. specifically 
	 * the input/output stream of the connection socket. 
	 * @throws IOException If an I/O error occurs.
	 */
	private void _initialize_() throws IOException {
		// Initialize the data input channel.
		InputStream in =  this.connectionSocket.getInputStream();;
		InputStreamReader isr = new InputStreamReader(in);
		this.reader = new BufferedReader(isr);
		
		// Initialize the data output channel.
		OutputStream out = this.connectionSocket.getOutputStream();
		this.writer = new PrintWriter(out, true);
	}
	
}
