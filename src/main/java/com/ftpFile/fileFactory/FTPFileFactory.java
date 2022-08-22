/**
 * 
 */
package com.ftpFile.fileFactory;

import com.ftpFile.fileComposite.*;
import com.util.FTPFileType;

/**
 * This class represents the manufacturer of FTP files according to their types.
 * @author idtaleb
 *
 */
public class FTPFileFactory {

	/**
	 * Builds the right type of FTP file.
	 * @param filename The filename of the FTP file.
	 * @param root The root directory of the FTP file. 
	 * @param fileType The type of the FTP file.
	 * @return a a FTP file component {@link FTPFileComponent}
	 */
	public static FTPFileComponent buildFTPFile(String filename, FTPDirectoryFile root, FTPFileType fileType) {
		switch (fileType) {
			case REGULAR_FILE:
				return new FTPRegularFile(filename, root, 0);
			case SYMBOLIC_FILE:
				return new FTPSymbolicFile(filename, root, 0);
			case DIRECTORY_FILE:	
				return new FTPDirectoryFile(filename, root, 0);
			default:
				return null;
		}
	}

}
