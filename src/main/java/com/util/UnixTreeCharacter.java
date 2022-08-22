package com.util;

/**
 * This class presents the characters of the Unix command 'tree'.
 * This class cannot be instantiated, it defines some constants
 * about the used characters to display the tree of a directory as 
 * the Unix tree command. 
 * @author idtaleb
 *
 */
public class UnixTreeCharacter {
	/** 
	 * Character that represents the root directory.
	 */
	public static final String ROOT_CHAR = ".";
	/**
	 * Character that represents a line space.
	 */
	public static final String LINE_SPACE = "    ";
	/**
	 * Character that represents a line separator.
	 */
	public static final String LINE_SEPERATOR = System.getProperty("line.separator");
	/**
	 * Character that represents an intermediate subfile UNIX character.
	 */
	public static final String SUBFILE_CHAR = "├── ";
	/**
	 * Character that represents the last subfile UNIX character.
	 */
	public static final String LAST_SUBFILE_CHAR = "└── ";
	/**
	 * Character that represents a UNIX subfile level character.
	 */
	public static final String SUBFILE_LEVEL = "│   ";
	
	private String value;
	
	/**
	 * Cannot instantiate this class
	 * @param value A Unix character
	 */
	private UnixTreeCharacter(String value) {
		this.value = value;
	}

	/**
	 * @return The Unix tree character value
	 */
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return this.getValue();
	}
}
