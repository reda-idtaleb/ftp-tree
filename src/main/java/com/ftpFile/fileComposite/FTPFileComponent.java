package com.ftpFile.fileComposite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ftpFile.fileFactory.FTPFileFactory;
import com.util.FTPFileType;

/**
 * This class implements composite design, which is used to model 
 * an FTP server directory. This class allows to recursively build 
 * each directory with its components.
 * @author idtaleb
 *
 */
public abstract class FTPFileComponent {
	/**
	 * This constant indicates the exploration depth of a directory.
	 * by default it means that the depth is not set. 
	 */
	public final static int DEFAULT_DEPTH = -1;
	/**
	 * The OS path separator.
	 */
	private final static String PATH_SEPARATOR = System.getProperty("file.separator");
	/**
	 * A constant that indicates the character of the read right.
	 */
	protected static final String READ_RIGHT = "r";
	/**
	 * A constant that indicates the character of the write right.
	 */
	protected static final String WRTIE_RIGHT = "w";
	/**
	 * A constant that indicates the character of the execution right.
	 */
	protected static final String EXECUTE_RIGHT = "x";
	/**
	 * The name of the file
	 */
	private String fileName; 
	/*
	 * The subfiles of a directory.
	 */
	protected List<FTPFileComponent> childs;
	/**
	 * The type of the file.
	 */
	private FTPFileType fileType;
	/**
	 * The user right access
	 */
	private String userRights;
	/**
	 * The group right access
	 */
	private String groupRights;
	/**
	 * The other right access
	 */
	private String otherRights;
	/**
	 * The parent directory path name.
	 */
	private FTPDirectoryFile parentFile;
	/**
	 * The depth of the tree.
	 */
	private int depth;
	/*
	 * The pathname of the file.
	 */
	private String pathname;
	/**
	 * The current file
	 */
	private FTPFileComponent currentFile;

	/**
	 * Default Constructor
	 */
	public FTPFileComponent() {
		this(null, null, 0);
	}
	
	/**
	 * Creates a FTPFileComponent.
	 * @param fileName The name of the FTP file.
	 * @param parentFile The parent directory.
	 * @param depth The level of the component.
	 */
	public FTPFileComponent(String fileName, FTPDirectoryFile parentFile, int depth) {
		this.fileName = fileName;
		this.parentFile = parentFile;
		this.depth = depth;
		this.currentFile = this;
		String pathName = this.buildPathName(fileName, parentFile)  ;
		this.pathname = (pathName.length() > 1) ? pathName.substring(0, pathName.length()-1) : pathName;
		this.userRights = READ_RIGHT+WRTIE_RIGHT+EXECUTE_RIGHT;
		this.groupRights = READ_RIGHT+WRTIE_RIGHT+EXECUTE_RIGHT;
		this.otherRights = READ_RIGHT+WRTIE_RIGHT+EXECUTE_RIGHT;
		this.fileType = this.createFileType();
		this.childs = new ArrayList<FTPFileComponent>();
		if (this.parentFile != null) {
			this.parentFile.addChild(this);
		}
	}

	/**
	 * Build the absolute path of the file
	 * @param fileName The file name of the directory
	 * @param parentFile The parent file of the current directory.
	 */
	protected String buildPathName(String fileName, FTPDirectoryFile parentFile) {	
		if (this.getFileName().contentEquals(PATH_SEPARATOR))
			return this.getFileName();
		else 
			return parentFile.buildPathName(parentFile.getFileName(), parentFile.getParentFile())
					+fileName+PATH_SEPARATOR ;
	}
	
	/**
	 * Create a generic FTP FIle.
	 * @param fileInfo The information of the file.
	 * @param parent The parent directory.
	 * @return A FTPFileComponent that represents a generic FTP File.
	 */
	public static FTPFileComponent createFile(String fileInfo, FTPDirectoryFile parent) {
		return _parseFileInformations_(fileInfo, parent);
	}
	
	/**
	 * Parse a response containing the informations about a file. 
	 * @param fileInfo The response containing the files informations provided by the FTP server.
	 * @param parent The parent directory.
	 * @return A file specified by it's name, parentFile and pathname.
	 */
	private static FTPFileComponent _parseFileInformations_(String fileInfo, FTPDirectoryFile parent) {
		String fileName;
		String symbolicChar = "->"; 
		List<String> tokenizedFile = Arrays.asList(fileInfo.split(" ")); 
		List<String> withouBlanks = tokenizedFile.stream()
												 .filter(s -> !s.contentEquals(""))
												 .collect(Collectors.toList());
		if (withouBlanks.contains(symbolicChar))
			fileName = withouBlanks.get(withouBlanks.size()-3);
		else 
			fileName = withouBlanks.get(withouBlanks.size()-1);
		FTPFileType type = FTPFileType.fromValue(tokenizedFile.get(0).charAt(0));
		String userRights = tokenizedFile.get(0).substring(1, 4);
		String groupRights = tokenizedFile.get(0).substring(4, 7);
		String otherRights = tokenizedFile.get(0).substring(7, 10);
		FTPFileComponent file = FTPFileFactory.buildFTPFile(fileName, parent, type);
		file.fileType = type;
		file.userRights = userRights;
		file.groupRights = groupRights;
		file.otherRights = otherRights;
		return file;
	}
	
	/**
	 * Get the next file of the current file
	 * @return The next FTP file following the current file.
	 */
	public FTPFileComponent getNextFile() {
		if (this.getParentFile() != null) {
			int indexOf = this.getParentFile().getChilds().indexOf(this);
			if ((indexOf+1) >= this.getParentFile().getChilds().size())
				return this.getParentFile().getNextFile();
			return this.getParentFile().getChilds().get(indexOf+1);
		}
		else 
			return this;
	}
	
	/**
	 * @return The presentation of a directory as the UNIX command 'tree'.
	 */
	public String showTree() {
		return this._showTree_();
	}
	
	/**
	 * Get the right FTP type file from the subclasses.
	 * @return The FTP file type.
	 */
	protected abstract FTPFileType createFileType();
	
	/**
	 * Show the directory tree by using the depth-first algorithm. 
	 * @return The hierarchical representation of the directory. 
	 */
	protected abstract String _showTree_();
	
	/**
	 * 
	 * @return True if the file can be read by its user.
	 */
	public boolean hasUserReadRight() {
		return this.userRights.startsWith(READ_RIGHT);
	}
	
	/**
	 * 
	 * @return True if the file can be written by its user.
	 */
	public boolean hasUserWriteRight() {
		return this.userRights.substring(1).startsWith(WRTIE_RIGHT);
	}
	
	/**
	 * 
	 * @return True if the file can be executed by its user.
	 */
	public boolean hasUserExecuteRight() {
		return this.userRights.substring(2).startsWith(EXECUTE_RIGHT);
	}
	
	/**
	 * 
	 * @return True if the file can be read by its group.
	 */
	public boolean hasGroupReadRight() {
		return this.groupRights.startsWith(READ_RIGHT);
	}
	
	/**
	 * 
	 * @return True if the file can be written by its group.
	 */
	public boolean hasGroupWriteRight() {
		return this.groupRights.substring(1).startsWith(WRTIE_RIGHT);
	}
	
	/**
	 * 
	 * @return True if the file can be executed by its group.
	 */
	public boolean hasGroupExecuteRight() {
		return this.groupRights.substring(2).startsWith(EXECUTE_RIGHT);
	}
	
	/**
	 * 
	 * @return True if the file can be read by the other users.
	 */
	public boolean hasOtherReadRight() {
		return this.otherRights.startsWith(READ_RIGHT);
	}
	
	/**
	 * 
	 * @return True if the file can be written by the other users.
	 */
	public boolean hasOtherWriteRight() {
		return this.otherRights.substring(1).startsWith(WRTIE_RIGHT);
	}
	
	/**
	 * 
	 * @return True if the file can be executed by the other users.
	 */
	public boolean hasOtherExecuteRight() {
		return this.otherRights.substring(2).startsWith(EXECUTE_RIGHT);
	}
	
	/** *** GETTERS & SETTERS *** */
	/**
	 * @return The file name of the FTP file.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return The parent FTP file.
	 */
	public FTPDirectoryFile getParentFile() {
		return parentFile;
	}

	/**
	 * @return The type of the FTP file.
	 */
	public FTPFileType getFileType() {
		return fileType;
	}

	/**
	 * Add a FTP file to the list of files of a directory. The added file
	 * may be a directory or a regular file.
	 * @param child The FTP file to add.
	 */
	public void addChild(FTPFileComponent child) {
		child.parentFile = child.getParentFile();
		this.childs.add(child);
	}
		
	/**
	 * 
	 * @return The depth of the component
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @return The files containing in a directory. 
	 */
	public List<FTPFileComponent> getChilds() {
		return childs;
	}
	
	/**
	 * Create a JSON file containing all the directory tree.
	 * @param pathname The path name of the file to save
	 * @return File the generated file.
	 * @throws IOException  When an errors occurs.
	 * @throws FileNotFoundException When the file is nor found.
	 * 
	 */
	public File toJson (String pathname) throws IOException {
		String suffix = ".json";
		String cheminDuFichier = !pathname.endsWith(suffix) ? pathname+suffix : pathname;
		String jsonContent = this.toString();
		File file = new File(cheminDuFichier);
		if (!file.exists())
			file.createNewFile();
		FileWriter writer = new FileWriter(file);
		writer.write(jsonContent);
		writer.flush();
		writer.close();
		return file;
	}

	/**
	 * @return the user rights
	 */
	public String getUserRights() {
		return userRights;
	}

	/**
	 * @return the group rights
	 */
	public String getGroupRights() {
		return groupRights;
	}

	/**
	 * @return the other rights
	 */
	public String getOtherRights() {
		return otherRights;
	}
	
	/**
	 * @param depth the depth to set
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * @return the pathname of the file
	 */
	public String getPathname() {
		return pathname;
	}

	/**
	 * @return the current File
	 */
	public FTPFileComponent getCurrentFile() {
		return currentFile;
	}
	
	@Override
	public String toString() {
		return "{"
				+ "\n\"fileType\": " + "\"" +fileType.name() + "\""
				+ ",\n\"name\": " + "\""+fileName + "\""
				+ ",\n\"userRights\": " + "\""+userRights +"\""
				+ ",\n\"groupRights\": " + "\""+groupRights + "\""
				+ ",\n\"otherRights\": " + "\""+otherRights + "\""
				+ ",\n\"pathname\": " + "\""+ pathname + "\""
				+ ((!(this instanceof FTPDirectoryFile)) ? "}" : (",\n\"files\": "  + childs +  "}\n"))
				 ;
	}

}
