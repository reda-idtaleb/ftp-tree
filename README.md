# FTP-Tree
- Author: Réda ID-TALEB
- Date: 11/01/2022

# Introduction
The tree-ftp tool allows the user to connect to an FTP server in order to view remote files. The display of the files resumes the same display as that of the Unix tree command.
Depth-first traversal algorithms are widely used in this project and in particular during the construction of the remote server's folder tree.

# How to execute
On a shell console, type the following commands:
1. to clean the project
    ```
    mvn clean
    ``` 
2. to compile, and build the .jar
    ```
    mvn package
    ``` 
3. to run the program, you must be on the root(where the pom.xml is)
    ```
    java -jar target/tree-ftp-0.0.1-SNAPSHOT.jar <serverHost> [-port <number>] [-u <username> -p <password>] [-dir <absolute pathname>] [-d <depth value>] [-json </path.json>]
    ``` 

The program takes several arguments, but almost one argument is necessery: the server hostname (such as ftp.ubuntu.com, localhost...).
- You don't need to specify the port if it is 21. The default port is set to 21.
- You don't need to specify the username or password if the server is anonymous.


- You can specify the option -dir to show only the files of that directory.
- You can also specify the depth of the hierarchy.

**Interesting option:**
- You can export the shown tree as a json file, so you need just to specifiy the path and the name of your file.

# For developers
To view the conception of the project there is the [UML diagram ](docs/tree-ftp-uml.png).

# Example of running
Running the following command
```
java -jar target/tree-ftp-0.0.1-SNAPSHOT.jar ftp.ubuntu.com -d 1 -json ubuntu_ftp.json
```

The result is:
```
>> Establishing connection to FTP server...
>> Login success!
>> Building the FTP tree in progress...
/
├── cdimage
├── cloud-images
├── extras
├── lxc-images
├── maas-images
├── old-images
├── releases
├── simple-streams
├── ubuntu
├── ubuntu-cloud-archive
└── ubuntu-ports
11 directories

The .json file is exported to: /home/idtaleb/Desktop/tree-ftp/ubuntu_ftp.json
```
