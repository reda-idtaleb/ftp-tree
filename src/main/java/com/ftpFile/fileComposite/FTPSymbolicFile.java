/**
 * 
 */
package com.ftpFile.fileComposite;

import java.util.ArrayList;

import com.util.FTPFileType;

/**
 * This class models the symbolics FTP files.
 * @author idtaleb
 *
 */
public class FTPSymbolicFile extends FTPFileComponent {

	/**
	 * Construct an FTP symbolic file specified by it's name, it's parent file. 
	 * @param filename The name of the FTP symbolic file.
	 * @param parentFile The parent FTP file.
	 * @param depth The level of the component.
	 */
	public FTPSymbolicFile(String fileName, FTPDirectoryFile parentFile, int depth) {
		super(fileName, parentFile, depth);
		this.childs = new ArrayList<FTPFileComponent>();
	}

	/**
	 * Check if the file is a symbolic file
	 * @return true if the file is a symbolic file, false if not.
	 */
	public boolean isSymbolicFile() {
		return this.getFileType()==FTPFileType.REGULAR_FILE;
	}
	
	@Override
	protected FTPFileType createFileType() {
		return FTPFileType.SYMBOLIC_FILE;
	}

	@Override
	protected String _showTree_() {
		return "";
	}

}
