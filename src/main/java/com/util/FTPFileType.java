/**
 * 
 */
package com.util;

/**
 * This class enumerates different file types.
 * Until now it supports regular files, symbolics files and directories.
 * @author idtaleb
 *
 */
public enum FTPFileType {
	REGULAR_FILE('-'),
	SYMBOLIC_FILE('l'),
	DIRECTORY_FILE('d');
	
	private char value;
	
	private FTPFileType(char pValue) {
		this.value = pValue;
	}

	/**
	 * @return The file type value
	 */
	public char getValue() {
		return value;
	}
	
	/**
	 * create a FileType instance from a value
	 * @param value A value of [-1, 2] 
	 * @return an instance of Polarity with the specified value
	 * @throws IllegalArgumentException When the value is not a -, l, or d
	 */
	public static FTPFileType fromValue(char value) throws IllegalArgumentException {
		switch ( value ) {
			case '-':
				return REGULAR_FILE;
			case 'l':
				return SYMBOLIC_FILE;
			case 'd':
				return DIRECTORY_FILE;			
			default:
				throw new IllegalArgumentException("Value has to be -, l, or d");
		}
	}
}
