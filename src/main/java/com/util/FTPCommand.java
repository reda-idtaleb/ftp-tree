package com.util;

/**
 * This is an enumeration of the FTP commands. The FTP commands are 
 * defined in the RFC959 {@link http://abcdrfc.free.fr/rfc-vf/pdf/rfc959.pdf}.
 * @author idtaleb
 *
 */
public enum FTPCommand {
	/** USER NAME */
	USER,
	/** PASSWORD */
	PASS,
	/** LIST CURRENT DIRECTORY CATALOG */
	LIST,
	/** PASSIVE MODE */
	PASV,
	/** CHANGE WORKING DIRECTORY */
	CWD,
	/** PRINT WORKING DIRECTORY */
	PWD,
	/** CHANGE TO PARENT DIRECTORY */
	CDUP,
	/** DATA PORT */
	PORT, 
	
	REIN,
	/** LOGOUT FROM THE SERVER */
	QUIT;
}
