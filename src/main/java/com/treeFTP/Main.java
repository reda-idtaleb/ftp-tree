package com.treeFTP;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;


import com.exceptions.CannotExploreDirectoryException;
import com.exceptions.CannotLoginException;
import com.exceptions.ConnectionException;
import com.exceptions.ControlChannelClosedException;
import com.exceptions.DataChannelException;
import com.exceptions.DeconnectionException;
import com.exceptions.TimeoutExceededException;
import com.ftp.FTPClient;
import com.ftpFile.fileComposite.FTPDirectoryFile;
import com.ftpFile.fileComposite.FTPFileComponent;

public class Main {
	/** Red color to print the error messages */
	private static final String ANSI_RED = "\033[1;91m";
	/** Green color to indicate the successful of an operation */
	private static final String ANSI_GREEN = "\033[1;32m";
	/** Yellow color to indicate the intermediate operation */
	private static final String ANSI_YELLOW = "\033[1;33m"; 
	/** Neutral color */
	private static final String ANSI_RESET = "\033[1;97m";
	/** Regular white color */
	private static final String ANSI_REGULAR = "\033[0;37m";
	
	/** args options */
	private static final String USER_OPTION = "-u";
	private static final String PASSWORD_OPTION = "-p";
	private static final String PORT_OPTION = "-port";
	private static final String JSON_OPTION = "-json";
	private static final String DEPTH_OPTION = "-d";
	private static final String DIR_OPTION = "-dir";
	
	
	/** The prefix of all the messages of trace */
	private static final String prefix = ANSI_RESET+">> "+ANSI_REGULAR;
	
	
	private static FTPClient ftpClient;
	private static FTPFileComponent root;
	private static Integer port = 21;
	private static Integer depth = -1;
	private static String host = "";
	private static String startingDir = "";
	private static String user = "anonymous";
	private static String password = "anonymous";
	private static Map<String, Object> argsValue = new HashMap<String, Object>();
	
	/**
	 * Check if the FTP server host is a valid domain name.
	 * @param domainName A FTP server host.
	 * @return True if the domain name is well formatted, otherwise false.
	 */
	public static boolean isValidDomainName(String domainName){
		String regex = "^((?!-)[A-Za-z0-9-]"
		        	 + "{1,63}(?<!-)\\.)"
		        	 + "+[A-Za-z]{2,6}";
		Pattern p = Pattern.compile(regex);
		if (domainName == null) 
			return false;
		Matcher m = p.matcher(domainName);
		return m.matches();
	}
	
	private static String usage() {
		String help = prefix+ANSI_RESET+"Usage: java -jar target/tree-ftp-0.0.1-SNAPSHOT.jar <serverHost> "
				+ "[-u <username> -p <password>] [-dir <absolute pathname>] [-d <depth value>] [-json </path.json>]";
		return help;
	}
	
	/**
	 * Verify if all the arguments are good.
	 * @param args The arguments
	 * @return True if the arguments ar well formatted, false otherwise. 
	 */
	public static boolean isValidArguments(String[] args) {
		if (args.length < 1) 
			return false;

		host = args[0];
		if (args.length < 2) {
			argsValue.put(USER_OPTION, user);
			argsValue.put(PASSWORD_OPTION, password);
			argsValue.put(PORT_OPTION, port);
			argsValue.put(JSON_OPTION, null);		
			argsValue.put(DEPTH_OPTION, depth);
			argsValue.put(DIR_OPTION, startingDir);
			return true;
		}
		
		String[] options = Arrays.copyOfRange(args, 1, args.length);
		for (String arg : options) {
			try {
				// User
				if (arg.contentEquals(USER_OPTION)) 
					argsValue.put(arg, args[Arrays.asList(args).indexOf(arg)+1]) ;
				// Password
				else if (arg.contentEquals(PASSWORD_OPTION)) 
					argsValue.put(arg, args[Arrays.asList(args).indexOf(arg)+1]) ;
				// Port
				else if (arg.contentEquals(PORT_OPTION)) {
					try {
						argsValue.put(arg, Integer.parseInt(args[Arrays.asList(args).indexOf(arg)+1]));
					} catch (NumberFormatException e) {
						System.out.println(prefix+ANSI_RED+"Error: The value of the option <-port number> must be a number!"+ANSI_RESET);
						return false;
					}
				}
				// Json
				else if (arg.contentEquals(JSON_OPTION)) 
					argsValue.put(arg, args[Arrays.asList(args).indexOf(arg)+1]);
				// Depth
				else if (arg.contentEquals(DEPTH_OPTION)) {
					try {
						argsValue.put(arg, Integer.parseInt(args[Arrays.asList(args).indexOf(arg)+1]));
					} catch (NumberFormatException e) {
						System.out.println(prefix+ANSI_RED+"Error: The value of the option <-d number> must be a number!"+ANSI_RESET);
						return false;
					}
				}		
				// Path
				else if (arg.contentEquals(DIR_OPTION)) 
					argsValue.put(arg, args[Arrays.asList(args).indexOf(arg)+1]) ;
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println(prefix+ANSI_RED+"Error: Value of options missing!"+ANSI_RESET);
				return false;
			}
		}
		if (argsValue.isEmpty()) {
			System.out.println(prefix+ANSI_RED+"Error in arguments: "+ANSI_RESET+ "[" + String.join(", ", options)+"]");
			return false;
		}
		if ((!argsValue.containsKey(USER_OPTION) && argsValue.containsKey(PASSWORD_OPTION))) {
			System.out.println(prefix+ANSI_RED+"Error: The username and password must both be present!"+ANSI_RESET);
			return false;
		} 
		else if (argsValue.containsKey(USER_OPTION) && !argsValue.containsKey(PASSWORD_OPTION)) {
			System.out.println(prefix+ANSI_RED+"Error: The username and password must both be present!"+ANSI_RESET);
			return false;
		}	
		
		if (!argsValue.containsKey(USER_OPTION) || !argsValue.containsKey(PASSWORD_OPTION)) {
			argsValue.put(USER_OPTION, user);
			argsValue.put(PASSWORD_OPTION, password);
		}
		
		if (!argsValue.containsKey(PORT_OPTION))
			argsValue.put(PORT_OPTION, port);
		
		if (!argsValue.containsKey(DEPTH_OPTION))
			argsValue.put(DEPTH_OPTION, depth);
		
		if (!argsValue.containsKey(JSON_OPTION)) 
			argsValue.put(JSON_OPTION, null);
		
		if (!argsValue.containsKey(DIR_OPTION)) 
			argsValue.put(DIR_OPTION, startingDir);
		
		return true;
	}
	
	/**
	 * Launch the Tree FTP program.
	 */
	private static void launchTreeFTP() {
		String user = (String) argsValue.get(USER_OPTION);
		String password = (String) argsValue.get(PASSWORD_OPTION);
		String startingDir = (String) argsValue.get(DIR_OPTION);
		Integer depth = (Integer) argsValue.get(DEPTH_OPTION);
		Integer port = (Integer) argsValue.get(PORT_OPTION);
		ftpClient = new FTPClient();
		try {
			try {
				System.out.println(prefix+ANSI_YELLOW+"Establishing connection to FTP server..."+ANSI_RESET);
				ftpClient.connect(host, port);
			}
			catch (ControlChannelClosedException e) {
				 showErrorWhenFailedToConnect(e);
			}
			
			if(ftpClient.login(user, password)) {		
				System.out.println(prefix+ANSI_GREEN+"Login success!"+ANSI_RESET);
				root = new FTPDirectoryFile(ftpClient.getWorkingDirectoryName(), null, 0);
				System.out.println(prefix+ANSI_YELLOW+"Building the FTP tree in progress..."+ANSI_RESET);
				if (((String)argsValue.get(DIR_OPTION)).isEmpty())
					ftpClient.allFiles(root, depth);
				else {
					try {
						ftpClient.changeWorkingDirectory(startingDir);
						ftpClient.list(ftpClient.getWorkingDirectoryName(), (FTPDirectoryFile) root);	
					} catch (CannotExploreDirectoryException e){
						System.out.println(prefix+ANSI_RED+"[Error: "+e.getMessage()+"]"+ANSI_RESET);
					}
				}
			}
			else 
				throw new CannotLoginException("Error: cannot login !");			
		} catch (ControlChannelClosedException e) {
			try {
				System.out.println(prefix+ANSI_RED+"Error: Problem when accessing to the directory " + root.getCurrentFile().getPathname()+ANSI_RESET);
				
				showErrorWhenFailedToConnect(e);
				ftpClient.login(user, password);
				System.out.println(prefix+ANSI_RED+"Error: Cannot resume tree building after a server disconnect error."+ANSI_RESET);
				System.out.println(prefix+ANSI_RED+"Some of subdirectories are not explored!"+ANSI_RESET);
			}
			catch (IOException e1) {
				System.out.println(prefix+ANSI_RED+"Error: Cannot resume tree building after a server disconnect error."+ANSI_RESET);
				System.out.println(prefix+ANSI_RED+"Some of subdirectories are not explored!"+ANSI_RESET);
			}
		}
		catch (CannotLoginException e) {
			System.out.println(prefix+ANSI_RED
					+"Error: unable to establish a connection to the server: "
					+ "The username and/or password is incorrect."+ANSI_RESET);
			try {
				ftpClient.disconnect();
			} catch (DeconnectionException e1) {
				System.out.println(prefix+ANSI_RED+"Failed to disconnect!"+ANSI_RESET);
			}
			System.out.println(prefix+ANSI_RESET+"Disconnected from the server.");
			System.out.println(prefix+ANSI_RESET+"Connection closed.");
			System.exit(0);
		}
		catch (ConnectionException e) {
			System.out.println(prefix+ANSI_RED+"Error: FTP server refused connection!"+ANSI_RESET);
		}
		catch (DataChannelException e) {
			System.out.println(prefix+ANSI_RED+"Error: FTP server closed data channel"+ e.getMessage()+ANSI_RESET);
		}
		catch (DeconnectionException e) {
			System.out.println(prefix+ANSI_RED+"Error: Failed to disconnect!"+ANSI_RESET);
		}
		catch (CannotExploreDirectoryException e) {
			System.out.println(prefix+ANSI_RED+"[Error: "+e.getMessage()+"]"+ANSI_RESET);
		}
		catch (IOException e) {	
			System.out.println(prefix+ANSI_RED+"Unknown error occured!"+ANSI_RESET);
		}
		
	}

	/**
	 * Show a message when a connection failed and try to reconnect until timeout exceeded.
	 * @throws IOException When a connection error occurs while connecting to the FTP server.
	 * @throws CannotLoginException When cannot login to the server.
	 */
	private static void showErrorWhenFailedToConnect(ControlChannelClosedException e) throws IOException {
		try {
			System.out.println(prefix+ANSI_RED+"Error: Lost connection to the server, caused by " + e.getMessage()+ANSI_RESET);
			System.out.println(prefix+ANSI_YELLOW+"Try to reconnect ..."+ANSI_RESET);	
			ftpClient.reconnect(host);
			System.out.println(prefix+ANSI_GREEN+"Connection to the server reestablished."+ANSI_RESET);
		} catch (TimeoutExceededException e1) {
		    System.out.println(prefix+ANSI_RED+"Timeout exceeded: " + FTPClient.getTimeoutConnection() +"s"+ANSI_RESET);
		} catch (Exception e1) {
			throw new ConnectionException("Error: Cannot reconnecting to the server. The timeout is exceeded");
		}
	}
	
	public static void main(String[] args) {	
		if (!isValidArguments(args)) {
			System.out.println(prefix+ANSI_RED+"Lack of arguments!\n"+ANSI_RESET + usage());
			System.out.println(prefix+ANSI_RED+"Program failed! Please try again!"+ANSI_RESET);
			System.exit(0);
		}	
		launchTreeFTP(); 
		if (root != null) {
			System.out.println(root.showTree());
			if (argsValue.get(JSON_OPTION) != null) {
				try {
					File jsonFile = root.toJson((String)argsValue.get(JSON_OPTION));
					System.out.println("\nThe .json file is exported to: " + jsonFile.getAbsolutePath());
				} catch (IOException e) {
					System.out.println(prefix+ANSI_RED+"Error: cannot create the file!"+e.getMessage()+ANSI_RESET);
				}
			}
		}
		
	}

}
