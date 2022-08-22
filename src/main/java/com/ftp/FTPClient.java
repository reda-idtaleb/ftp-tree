package com.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.exceptions.ActiveModeException;
import com.exceptions.BadFormattedResponseException;
import com.exceptions.CannotExploreDirectoryException;
import com.exceptions.ConnectionException;
import com.exceptions.ControlChannelClosedException;
import com.exceptions.DataChannelException;
import com.exceptions.DeconnectionException;
import com.exceptions.TimeoutExceededException;
import com.ftpFile.fileComposite.FTPDirectoryFile;
import com.ftpFile.fileComposite.FTPFileComponent;
import com.tcp.TCPClient;
import com.tcp.TCPServer;
import com.util.FTPCommand;
import com.util.FTPFileType;

/**
 * This is the implementation of The FTP client corresponding 
 * to the RFC959 {@link http://abcdrfc.free.fr/rfc-vf/pdf/rfc959.pdf}
 * This class makes it possible to make the link with an FTP server 
 * and find the files contained in this server.
 * The FTPClient allows to provide high level functionalities to communicate with the server.
 * @author idtaleb
 *
 */
public class FTPClient {
	/**
	 * The string representing the end-of-line code.
	 */
	private final static String ASCIIEOL = System.getProperty("line.separator");
	/**
	 * The default FTP control port
	 */
	private static final int DEFAULT_CONTROL_PORT = 21;
	/**
	 * The default FTP data port
	 */
	private static final int DEFAULT_DATA_PORT = 20;
	/**
	 * A constant that represents the data active mode.
	 */
	private static final int DATA_ACTIVE_MODE = 0;
	/**
	 * A constant that represents the data passive mode.
	 */
	private static final int DATA_PASSIVE_MODE = 1;
	/**
	 * A constant that indicates the limit time of the reconnection
	 * when the server closed the control channel.
	 */
	private static final int TIMEOUT_CONNECTION = 5;
	/**
	 * The passive port value returned by FTP server during the data channel connection.
	 */
	private int pasvPort;
	/**
	 * The passive host address returned by FTP server during the data channel connection.
	 */
	private String pasvHost;
	/**
	 * TCP Client used to communicate with the FTP server using the control channel.
	 */
	private TCPClient tcpClient;
	/**
	 * Indicates if the connection is established or not.
	 */
	private boolean isConnected;
	/**
	 * The buffer that will contains the command to send to the FTP server.
	 */
	private StringBuffer command;
	/**
	 * The response received from the FTP server.
	 */
	private StringBuffer response;
	/**
	 * The code of a FTP response. 
	 */
	private int responseCode;
	
	/**
	 * Determines the current data mode(active or passive).
	 * By default the passive mode is used.
	 */
	private int currentDataMode;
	
	/**
	 * Default FTPClient constructor.
	 */
	public FTPClient() {
		this._initialize_();
	}
	
	/**
	 * Initialize all the parameters of the FTP connection.
	 */
	private void _initialize_() {
		command = new StringBuffer();
		response = new StringBuffer();
		isConnected = false;
		tcpClient = null;
		pasvHost = null;
		pasvPort = DEFAULT_DATA_PORT;
		currentDataMode = DATA_PASSIVE_MODE;
	}

	/**
	 * Make a connection with the distant server. 
	 * The connection uses the TCP protocol by using sockets.
	 * @param host The server's address
	 * @throws ConnectionException Error when trying to connect to the FTP server.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws DeconnectionException After a failed connection and when cannot disconnect from 
	 *         the server, may be because of a problem when closing the connection socket or when 
	 *         closing the input/output streams.
	 * @throws IOException When an I/O error occurred due to the connection socket
	 *         or maybe due to the input/output socket streams.
	 */
	public void connect(String host) throws IOException {
		this.connect(host, DEFAULT_CONTROL_PORT);
	}
	
	/**
	 * Make a connection with the distant server. 
	 * The connection uses the TCP protocol by using sockets.
	 * @param host The server's address
	 * @param port The port of the server
	 * @throws ConnectionException Error when trying to connect to the FTP server.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws DeconnectionException After a failed connection and when cannot disconnect from 
	 *         the server, may be because of a problem when closing the connection socket or when 
	 *         closing the input/output streams.
	 * @throws IOException When an I/O error occurred due to the connection socket
	 *         or maybe due to the input/output socket streams.
	 */
	public void connect(String host, int port) throws IOException {
		try {
			tcpClient = new TCPClient(host, port);
			this.getAllResponse();
			// thread.interupt
			if (this.responseCode/100 != 2) {
				this.disconnect();
				throw new ConnectionException("FTP server refused connection.");
			}
			isConnected = true;
		} catch (IOException e) {
			isConnected = false;
			throw new ConnectionException("Error when trying to connect to the FTP server!");
		}
	}
	
	/**
	 * Check if a connection with the FTP server is established.
	 * @return True if the connection is established, False otherwise. 
	 * @throws DeconnectionException When cannot disconnect from the server, may be 
	 * 	  	   because of a problem when closing the connection socket or when closing 
	 *         the input/output streams.
	 */
	public boolean isConnected() throws IOException {
		return isConnected;
	}
	
	/**
	 * Disconnect from the FTP server and initialize all the FTPClient parameters.
	 * @throws DeconnectionException When cannot disconnect from the server, may be 
	 * 	  	   because of a problem when closing the connection socket or when closing 
	 *         the input/output streams.
	 */
	public void disconnect() throws DeconnectionException {
		try {
			tcpClient.disconnect();		
		} catch (IOException e) {
			this._initialize_();
			throw new DeconnectionException("Deconnection failed!");
		}
		this._initialize_();
	}
	
	/**
	 * Login to the FTP server using the user name and password
	 * @param user The user name too login
	 * @param password The password to use
	 * @return return true if the login was successful
	 * @throws ConnectionException When unexpected connection closure from the server.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws IOException When an I/O error occurred
	 */
	public boolean login(String user, String password) throws IOException{
		int replyCode = _user_(user);
		// If it's a Definitive positive response, then the login is successful
		if(replyCode/100 == 2) 
			return true;
		// If we received an intermediate response, then we ask to login with password
		if(replyCode/100 == 3)
			replyCode = _pass_(password);	
		else 
			return false; // if we are here so the response is not positive
		return replyCode/100 == 2;
	}
	
	/**
	 * List of all the files containing in the specified directory. If the directory is null 
	 * so the files of the working directory are returned. 
	 * @param dirName A directory name.
	 * @param rootDir A {@link FTPDirectoryFile} that represents the root.
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws CannotExploreDirectoryException When the directory is may be inaccessible or not found.
	 * @throws DataChannelException When cannot switch to the passive mode. 
	 *         So the data channel is closed.
	 * @throws IOException when an I/O error occurred.
	 */
	public List<FTPFileComponent> list(String dirName, FTPDirectoryFile rootDir) throws IOException {
		String line;
		List<FTPFileComponent> listFiles = new ArrayList<FTPFileComponent>();
		
		// make the data connection using passive mode
		Socket socket = askDataConnection(FTPCommand.LIST.name(), dirName);
		
		if (socket == null || !(this.responseCode/100 <= 2))
			throw new DataChannelException("Cannot establish a data connection! The data channel is closed.");
		
		// read the response
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		while((line = in.readLine()) != null) { 
			FTPFileComponent file = FTPFileComponent.createFile(line, rootDir);
			file.setDepth(rootDir.getDepth()+1);
			listFiles.add(file);
		}
		
		in.close();
		// Exit the passive mode
		socket.close();
		
		this.getPendingReply();	
		return listFiles;	
	}
	
	/**
	 * Get all the files that contains in the FTP server. 
	 * This a recursive method by using the Depth-First Search algorithm.
	 * @param file A FTP file.
	 * @param depth The level of the component.
	 * @return The root directory passed as argument.
	 *         The {@link FTPDirectoryFile} is a recursive structure, so it
	 *         will contains recursively all the components of a directory.
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws CannotExploreDirectoryException There are several errors can raise that exception:
	 * 		   </br>1- when asking the name of the working directory. 
	 *            The current directory is may be inaccessible or not found.
	 *         </br>2- when cannot change to a next directory in the tree. 
	 *            The next directory is may be inaccessible or not found.
	 *         </br>3- when trying to go back to the parent directory. 
	 *            The parent directory is may be inaccessible or not found.
	 * @throws IOException When an I/O error occurred.
	 */
	public FTPFileComponent allFiles(FTPFileComponent file, int depth) throws IOException {
		if (depth < 0)  
			return exploreAllDirectories(file);
		else {
			if (file.getDepth() < depth) 			
				return exploreDirectoriesOfDepth(file, depth);
			else 
				return file; 
		}		
	} 
	
	/**
	 * Explore all the directories of the current directory.
	 * @param file The file to explore.
	 * @return return the recursive construction of the file.
	 * @throws IOException When an I/O occurs.
	 */
	private FTPFileComponent exploreAllDirectories(FTPFileComponent file) throws IOException {
		return this.exploreDirectoriesOfDepth(file, FTPFileComponent.DEFAULT_DEPTH);
	}

	/**
	 * Explore the sub directories of the current directory passed as the first argument.
	 * This method defines a depth exploration.
	 * @param file The current File to explore.
	 * @param depth The depth of the explorations.
	 * @return A {@link FTPFileComponent} File.
	 * @throws IOException When an I/O error occurred.
	 */
	private FTPFileComponent exploreDirectoriesOfDepth(FTPFileComponent file, int depth) throws IOException {
		List<FTPFileComponent> files = new ArrayList<FTPFileComponent>();
		if (file.getFileType() != FTPFileType.DIRECTORY_FILE)
			return file;
		else {				
			String nextDir = file.getPathname();
			if (((FTPDirectoryFile) file).isAccessibleDirectory()) {
				try {
					this.changeWorkingDirectory(nextDir);
				} catch (IOException e) {
					return file;
				}
				files = list(this.getWorkingDirectoryName(), (FTPDirectoryFile) file);			 
				if (files.isEmpty()) {
					this.changeToParentDirectory();
					return file;
				}
				for (FTPFileComponent f : files) 	
					allFiles(f, depth);	
			}
			else 
				return file;
		}	 
		this.changeToParentDirectory();
		return file;
	}
	 
	/**
	 * Change the working directory. It sends a 'CWD' command to the FTP server 
	 * to ask it to move from the current directory to the chosen directory 
	 * passed as an argument.
	 * @param pathname The path name of the directory.
	 * @return A list of FTPFile containing all the files in the current directory
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws CannotExploreDirectoryException Error when cannot change to directory passed
	 * 		   the argument. The directory is may be inaccessible or not found.
	 * @throws IOException When an I/O error occurred.
	 */
	public void changeWorkingDirectory(String pathname) throws IOException{
		int code = this._cwd_(pathname);
		if (code/100 != 2)
			throw new CannotExploreDirectoryException("Cannot change to directory: " + pathname);
	}
	
	/**
	 * Return the current directory. It sends a 'PWD' command to the FTP server
	 * to know in which directory we are.
	 * @return Return a String that represents the pathname of the working directory.
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws DataChannelException When a negative response received from the FTP server.
	 *         The error is not related to the directory problems but to the command itself.
	 * @throws CannotExploreDirectoryException Error when asking the name of the working 
	 * 		   directory. The directory is may be inaccessible or not found.
	 * @throws IOException When an I/O error is occurred.
	 */
	public String getWorkingDirectoryName() throws IOException {
		int code = this._pwd_();
		String reply = this.getResponse();
		if (code/100 == 550)
			throw new CannotExploreDirectoryException("Error when asking the name of the working directory, caused by: "+reply);
		else if (code/100 != 2)
			throw new DataChannelException("Error when PWD command sent to the server: " + reply);
		int firstIndex = reply.indexOf('"') + 1;
		return reply.substring(firstIndex, reply.indexOf('"', firstIndex));
	}
	
	/**
	 * Change to parent directory. It send a 'CDUP' command to the FTP server to go back to the 
	 * parent directory.
	 * @throws ConnectionException When unexpected connection closure from the server.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws CannotExploreDirectoryException Error when trying to go back to the parent directory. 
	 *         The directory is may be inaccessible or not found.
	 * @throws IOException When an I/O error is occurred.
	 */
	public void changeToParentDirectory() throws IOException {
		int code = this._cdup_();
		String reply = this.getResponse();
		if (code/100 != 2)
			throw new CannotExploreDirectoryException("Error when trying to go back to the parent directory, caused by: " + reply);
	}
	
	/**
	 * Send a command to the FTP server, and return the reply code received from the server.
	 * @param request A string representing an FTP command to send.
	 * @param args The argument of the command. This argument can be null if the command don't
	 * 			   Take any arguments.
	 * @return The integer value that represents the FTP reply code received from the server,
	 *         as a response to the command sent.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws DataChannelException When cannot switch to the passive mode.        
	 * @throws IOException When an I/O error occurred
	 */
	public int sendCommand(String request, String args) throws IOException {
		// initialize the buffer before building the command
		command.setLength(0);
		command.append(request);
		if(args != null) {
			command.append(' ');
			command.append(args);
		}
		command.append(ASCIIEOL);
		tcpClient.sendRequest(command.toString());
		this.getAllResponse();
		return responseCode;
	}
	
	/**
	 * Get the response received from the FTP server 
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws ControlChannelClosedException When the control channel is closed by the FTP server. Severals causes are possible.
	 *                                       421 Timeout is the most common problem.
	 * @throws IOException When an I/O error occurred.
	 */
	private void getAllResponse() throws IOException {		
		String reply = tcpClient.getResponse();
		//System.out.println("reply = "+reply);
		this.response.setLength(0);
		// If the FTP reply is null so the control channel is closed.
		// maybe tcpclient.getsocket().isDiconnected()??
		if(reply == null) {
			throw new ControlChannelClosedException("Error: Control channel has been closed by the FTP server!");
		}
		else if(reply.length() < 3)
			throw new BadFormattedResponseException("The response is badly formatted or incomplete.");
		
		this.responseCode = Integer.parseInt(reply.substring(0, 3));
		this.response.append(reply);
		
		if(reply.length() >= 4 && reply.contains("-") && reply.indexOf("-") == 3) {
			while(!(reply.startsWith(String.valueOf(this.responseCode) + ' '))) {
				reply = tcpClient.getResponse();
				this.response.append(reply);
			}
		}	
	}
	
	/**
	 * Wait for the pending response from the FTP server. 
	 * @return The code of the FTP reply.
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws IOException when an I/O error occurred.
	 */
	private int getPendingReply() throws IOException {
		this.getAllResponse();
		return this.responseCode;
	}
	
	/**
	 * Ask the FTP server to establish the data connection.
	 * When calling this method, the passive mode is activated and
	 * the command passed as argument is sent to the FTP server on 
	 * the control channel.  
	 * @param command The FTP command to send to the FTP server. 
	 * @param arg The argument of the command. If no argument so it can take 
	 *        a null value.
	 * @return The socket representing the data connection channel. If an error 
	 *         occurred when receiving the reply from the FTP server so a null 
	 *         is returned.  
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws DataChannelException When cannot switch to the passive mode.
	 * @throws IOException When an I/O error occurred.
	 */
	public Socket askDataConnection(String command, String arg) throws IOException {
		Socket socket;
		if (currentDataMode == DATA_ACTIVE_MODE) {
			try {
				socket = this.activateActiveMode();
			} catch (ActiveModeException e) {
				this.currentDataMode = DATA_PASSIVE_MODE;
				socket = activatePassiveMode();
			}
		}
		else
			socket = activatePassiveMode();
		int code = sendCommand(command, arg);
		// if Not a positive preliminary response, so we close the data channel		
		if(code/100 != 1) {
			socket.close();
			return null;
		}
		return socket;
	}
	
	/**
	 * Logout to the FTP server by sending a 'QUIT' command.
	 * @return True if a successfully logout, otherwise false. 
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws IOException When an I/O error occurred.
	 */
	public boolean logout() throws IOException {
		if (this._quit_()/100 != 2) 
			return false;
		isConnected = false;
		return true;	
	}
	
	/**
	 * Try to reconnect to the FTP server. Use this method only if the FTP server closed
	 * the control channel. 
	 * @param host The FTP server host.
	 * @throws UnknownHostException When the host or/and the port are not valid.
	 * @throws ExecutionException When the thread doesn't finished it's task because
	 * 		                      of a an aborted exception.
	 * @throws InterruptedException When the thread is interrupted while he's doesn't finished its task.  
	 * @throws TimeoutExceededException When the reconnecting to the FTP server exceeds the timeout.
	 */
	public void reconnect(String host) throws UnknownHostException, IOException, InterruptedException, ExecutionException, TimeoutExceededException {
		this.reconnect(host, DEFAULT_CONTROL_PORT);
	}
	
	/**
	 * Try to reconnect to the FTP server. Use this method only if the FTP server closed
	 * the control channel. This method specifies also a port. This method fail if the reconnect timeout
	 * is exceeded.
	 * @param host The FTP server host.
	 * @param port The FTP server port.
	 * @throws UnknownHostException When the host or/and the port are not valid.
	 * @throws IOException When an I/O error occurs. 
	 * @throws ExecutionException When the thread doesn't finished it's task because
	 * 		                      of a an aborted exception.
	 * @throws InterruptedException When the thread is interrupted while he's doesn't finished its task. 
	 * @throws TimeoutExceededException When the reconnecting to the FTP server exceeds the timeout.
	 */
	public void reconnect(String host, int port) throws UnknownHostException, IOException, InterruptedException, ExecutionException, TimeoutExceededException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<TCPClient> future = executor.submit(new FTPClient().new Task(host, port));
        try {
			this.tcpClient = future.get(TIMEOUT_CONNECTION, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new TimeoutExceededException("Cannot reconnect to the FTP server cause of timeout exceeded!");
        }
        future.cancel(true);
        executor.shutdownNow();	
	}
	
	/**
	 * Establish a data connection with the FTP server using the passive mode.
	 * The FTP server will be asked to listen to a port so that the we can
	 * connect to that port to transfer data on it.
	 * @return The socket representing the data connection channel. The socket is specified
	 *         by the listened port and the address provided by the FTP server.
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws DataChannelException When cannot switch to the passive mode.
	 * @throws IOException If cannot switch to passive mode. The response provided by
	 *         the server is negative because of an I/O error.
	 */
	private Socket activatePassiveMode() throws IOException {
		String reply;
		Socket socket;
		int code = this._pasv_();
		reply = this.response.toString();
		
		// If an error occurred during the data connection
		if (code/100 != 2)
			throw new DataChannelException("Cannot switch to passive mode: " + reply);
		
		// if we get here so we get a positive response, 
		// and we have the address and the "listened" port.
		String[] truncReply = reply.substring(reply.indexOf('(')+1, reply.indexOf(')')).split(",");
		this.pasvHost = truncReply[0] + "." + truncReply[1] + "." + truncReply[2] + "." + truncReply[3];
		// The port is an address with 16 bits of the two lasts values.
		this.pasvPort = (Integer.parseInt(truncReply[4]) << 8) + Integer.parseInt(truncReply[5]);
		socket = new Socket(this.pasvHost, this.pasvPort);
		return socket;
	}
	
	/**
	 * Activate the active data mode. So the client will act like a server. 
	 * The server will connect to a listened port. The active mode can fail if the 
	 * server cannot reach the IP address of the client.
	 * @return The connection socket.
	 * @throws IOException When an error occurs while listening to a port.
	 * @throws ActiveModeException When the FTP server cannot connect to the client.
	 */
	private Socket activateActiveMode() throws IOException, ActiveModeException {
		// listen on any free port
		TCPServer server = new TCPServer(0);
		ServerSocket socket = server.getServerSocket();
		if (socket == null)
			throw new ActiveModeException("Cannot handle the active mode");
		int port = socket.getLocalPort();
		// the first 8 bits
		Integer block1 = (port >> 8);
		// the last 8 bits
		Integer block2 = (port - (block1 << 8));
		String address = InetAddress.getLocalHost().getHostAddress();
		URL publicIP = new URL("http://checkip.amazonaws.com");
		BufferedReader sc = new BufferedReader(new InputStreamReader(publicIP.openStream()));
		address = sc.readLine().trim(); 
		List<String> tokens = new ArrayList<String>(Arrays.asList(address.split("\\.")));
		tokens.add(block1.toString()); 
		tokens.add(block2.toString()); 
		String portAddess = String.join(",", tokens);	
		this._port_(portAddess);
		if (this.responseCode/100 == 2)
			server.accept();
		return null;
	}
	
	/** ***** LOW LEVEL METHODS ***** */
	
	/**
	 * Send the 'USER' command to the FTP server to login using the user account.
	 * @param username The user name to login.
	 * @return The code received from the FTP server as a reply to the 'USER' command
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws IOException When an I/O error occurred
	 */
	private int _user_(String username) throws IOException {
		return this.sendCommand(FTPCommand.USER.name(), username);
	}
	
	/**
	 * Send the 'PASS' command to the FTP server to login using the password.
	 * @param password The password to login.
	 * @return The code received from the FTP server as a reply to the 'PASS' command.
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws IOException When an I/O error occurred
	 */
	private int _pass_(String password) throws IOException {
		return this.sendCommand(FTPCommand.PASS.name(), password);
	}
	
	/**
	 * Send the 'PASV' command to the FTP server to asks the server to "listen" to a data port.
	 * @return The code received from the FTP server as a reply to the 'PASV' command.
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws IOException When an I/O error occurred
	 */
	private int _pasv_() throws IOException {
		return this.sendCommand(FTPCommand.PASV.name(), null);
	}
	
	/**
	 * Send the 'CWD' command to the FTP server to change the working directory to the chosen
	 * directory passed as the argument .
	 * @param pathname The pathname of the destination directory. 
	 * @return The code received from the FTP server as a reply to the 'CWD' command .
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws IOException When an I/O error occurred.
	 */
	private int _cwd_(String pathname) throws IOException {
		return this.sendCommand(FTPCommand.CWD.name(), pathname);
	}
	
	/**
	 * Send the 'PWD' command to the FTP server to print the name of the working directory.
	 * @return The code received from the FTP server as a reply to the 'PWD' command. 
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws IOException IOException When an I/O error occurred.
	 */
	private int _pwd_() throws IOException {
		return this.sendCommand(FTPCommand.PWD.name(), null);
	}
	
	/**
	 * Send the 'CDUP' command to the FTP server to go back to the parent directory.
	 * @return The code received from the FTP server as a reply to the 'CDUP' command.
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws IOException IOException When an I/O error occurred.
	 */
	private int _cdup_() throws IOException {
		return this.sendCommand(FTPCommand.CDUP.name(), null);
	}
	
	/**
	 * Send the 'PORT' command to the FTP server to communicate the listened data port.
	 * @param address the address of the listened port.
	 * @return The code received from the FTP server as a reply to the 'PORT' command.
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws IOException IOException When an I/O error occurred.
	 */
	private int _port_(String address) throws IOException {
		return this.sendCommand(FTPCommand.PORT.name(), address);
	}
	
	/**
	 * Send the 'QUIT' command to the FTP server to logout from the FTP server.
	 * @return The code received from the FTP server as a reply to the 'QUIT' command.
	 * @throws ConnectionException When unexpected connection closure without specifications.
	 * @throws BadFormattedResponseException When The response is badly formatted or incomplete.
	 * @throws IOException IOException When an I/O error occurred.
	 */
	private int _quit_() throws IOException {
		return this.sendCommand(FTPCommand.QUIT.name(), null);
	}
	
	/** ***** Getters/Setters ***** */ 
	
	/**
	 * Get the code of the response provided by the FTP server
	 * @return Return the 3 digits representing the code of the server response.
	 */
	public int getResponsCode() {
		return this.responseCode;	
	}

	/**
	 * @return The response received from the FTP server
	 */
	public String getResponse() {
		return response.toString();
	}

	/**
	 * @return The timeout connection.
	 */
	public static int getTimeoutConnection() {
		return TIMEOUT_CONNECTION;
	}

	/**
	 * This class is to run a task on thread during a certain time.
	 * @author idtaleb
	 *
	 */
	private class Task implements Callable<TCPClient> {
		
		private String host;
		private int port;
		
	    public Task(String host, int port) {
			this.host = host;
			this.port = port;
		}

		@Override
	    public TCPClient call() throws Exception {		
			while (!isConnected)			
				connect(host, port);
			return tcpClient;	
	    }
	}
	
}
