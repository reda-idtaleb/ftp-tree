package com.ftpFile.fileComposite;

import java.util.ArrayList;

import com.util.FTPFileType;

/**
 * This class models the regulars FTP files.
 * @author idtaleb
 *
 */
public class FTPRegularFile extends FTPFileComponent{
	
	/**
	 * Construct an FTP regular file specified by it's name, it's parent file. 
	 * @param filename The name of the regular FTP file.
	 * @param parentFile The parent FTP file.
	 * @param depth The level of the component.
	 */
	public FTPRegularFile(String fileName, FTPDirectoryFile parentFile, int depth) {
		super(fileName, parentFile, depth);
		this.childs = new ArrayList<FTPFileComponent>();
	}

	@Override
	protected FTPFileType createFileType() {
		return FTPFileType.REGULAR_FILE;
	}
	
	/**
	 * Check if the file is a regular file
	 * @return true if the file is a regular file, false if not.
	 */
	public boolean isRegularFile() {
		return this.getFileType()==FTPFileType.REGULAR_FILE;
	}
	
	@Override
	protected String _showTree_() {
		return "";
	}
	
}
 