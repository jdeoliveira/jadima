package ve.usb.jgm.tools.publisher;

import java.lang.reflect.*;
import org.apache.commons.cli.*;
import org.apache.log4j.*;
import ve.usb.jgm.client.project.*;
import java.io.*;
import ve.usb.jgm.client.*;
import java.util.*;
import ve.usb.jgm.tools.stubgenerator.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import ve.usb.jgm.repo.*;
import ve.usb.jgm.util.*;


public class Upgrader extends org.apache.tools.ant.Task {

    private static Logger logger = Logger.getLogger(Publisher.class);

    private static Options options;
    
    /**
     * Holds value of property repositoryName.
     */
    private String repositoryName;

    /**
     * Holds value of property libraryName.
     */
    private String libraryName;

    /**
     * Holds value of property versionDescription.
     */
    private String versionDescription;

    /**
     * Holds value of property majorVersion.
     */
    private int majorVersion;

    /**
     * Holds value of property minorVersion.
     */
    private int minorVersion;

    /**
     * Holds value of property revision.
     */
    private int revision;

    /**
     * Holds value of property bytecodeJarFile.
     */
    private File bytecodeJarFile;

    /**
     * Holds value of property javadocZipFile.
     */
    private File javadocZipFile;

    /**
     * Holds value of property allowedRoles.
     */
    private String[] allowedRoles;

    /**
     * Holds value of property projectDescriptor.
     */
    private File projectDescriptor;
    /**
     * Holds value of property javadocDir.
     */
    private File javadocDir;

    /**
     * Holds value of property sourceLevel.
     */
    private String sourceLevel;


   
    
    public Upgrader() {
        //nothing to do here
    }
    
    /**
     * Command line version of this task
     */
    public static void main(String[] args) {
        
        //Inicializamos log4j, con el verbosity por defecto

        Logger.getRootLogger().setLevel(Level.WARN);
        String logPattern = "%-5p - %m\n";
        Logger.getLogger("org.apache.velocity").setLevel(Level.FATAL);
        Logger.getLogger("net.sourceforge.ehcache").setLevel(Level.FATAL);
        Logger.getLogger("de.hunsicker.jalopy").setLevel(Level.FATAL);
        Logger.getLogger("org.apache.axis").setLevel(Level.FATAL);
        PatternLayout layout = new PatternLayout(logPattern);
        BasicConfigurator.configure(new ConsoleAppender(layout,"System.out"));
        
        
        //Inicializamos CLI
        
        options = new Options();
        
        options.addOption(
            OptionBuilder   .withArgName("repository-name")
                            .hasArg()
                            .withDescription("Name of the repository on which this library should be published")
                            .withLongOpt("repository-name")
                            .isRequired()
                            .create("r"));
        
        options.addOption(
            OptionBuilder   .withArgName("bytecode-file")
                            .hasArg()
                            .withDescription("File name of the real bytecode jar")
                            .withLongOpt("bytecodejar")
                            .isRequired()
                            .create("b"));
        
        options.addOption(
            OptionBuilder   .withArgName("source-level")
                            .hasArg()
                            .withDescription("Stubs source files JDK compatibility level (may be one of 1.3, 1.4, 1.5)")
                            .withLongOpt("sourcelevel")
                            .create("s"));
        
        OptionGroup javadocOptions = (new OptionGroup())
            .addOption(
                OptionBuilder   .withArgName("javadoc-zipfile")
                                .hasArg()
                                .withDescription("File name of the javadoc documentation zip")
                                .withLongOpt("javadoczip")
                                .create("J"))
            .addOption(
                OptionBuilder   .withArgName("javadoc-dir")
                                .hasArg()
                                .withDescription("Directory of javadoc documentation")
                                .withLongOpt("javadocdir")
                                .create("j"));
        
        javadocOptions.setRequired(true);
        options.addOptionGroup(javadocOptions);
        
        options.addOption(
            OptionBuilder   .withArgName("project-descriptor")
                            .hasArg()
                            .withDescription("File name of the project descriptor, if this library requires other libraries")
                            .withLongOpt("project-descriptor")
                            .create("p"));
        
        options.addOption(
            OptionBuilder   .withArgName("library-name")
                            .hasArg()
                            .withDescription("Library name")
                            .withLongOpt("name")
                            .isRequired()
                            .create("n"));
        
        options.addOption(
            OptionBuilder   .withArgName("version-number")
                            .hasArgs(3) //esto es que tiene 3 argumentos dentro de esta opcion
                            .withDescription("Version number of the library in the following form: <major>.<minor>.<revision>")
                            .withLongOpt("version")
                            .withType(new Integer(0))
                            .withValueSeparator('.') //el separador de argumentos es el punto
                            .isRequired()
                            .create("v"));
        
        options.addOption(
            OptionBuilder   .withArgName("version-description")
                            .hasArg()
                            .withDescription("Version description")
                            .withLongOpt("verdescription")
                            .create("e"));
        
        options.addOption(
            OptionBuilder   .withArgName("allowed-roles")
                            .hasArgs()
                            .withDescription("Security roles allowed to access this library, separated by commas (,)")
                            .withLongOpt("allowedroles")
                            .withValueSeparator(',')
                            .isRequired()
                            .create("a"));
        
        OptionGroup outOptions = (new OptionGroup())
                .addOption(
                    OptionBuilder   .withDescription("Verbose output")
                                    .withLongOpt("verbose")
                                    .create("V"))
        
                .addOption(
                    OptionBuilder   .withDescription("Extremelly verbose output (only for hard debbuging purposes, may be illegible!)")
                                    .withLongOpt("debug")
                                    .create("D"))

                .addOption(
                    OptionBuilder   .withDescription("Quiet output")
                                    .withLongOpt("quiet")
                                    .create("q"));
        
        outOptions.setRequired(false);
                                    
        options.addOptionGroup(outOptions);
        
        options.addOption("h", "help", false, "Show this help message");
        
        //Parseamos la linea de comandos
        CommandLineParser parser = new PosixParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
            
            if (line.hasOption("h")) {
                showHelpAndExit();
            }
            
            //Reajustamos log4j segun las opciones de verbosity
            if (line.hasOption("V")) {
                //Quieren verbose, ponemos el thereshold a INFO
                Logger.getRootLogger().setLevel(Level.INFO);
                layout.setConversionPattern("%-5p - %m\n");
            } else if (line.hasOption("D")) {
                //Quieren DEBUG, ponemos el thereshold a DEBUG
                Logger.getRootLogger().setLevel(Level.DEBUG);
                layout.setConversionPattern("%-5p %-6r %-10t [%.260" +
                "25F:%5L] %m\n");
            } else if (line.hasOption("q")) {
                //Quieren quiet, ponemos el thereshold a ERROR
                Logger.getRootLogger().setLevel(Level.ERROR);
                layout.setConversionPattern("%-5p - %m\n");
            } 
            
            Upgrader task = new Upgrader();
            
            String[] ver = line.getOptionValues("v");
            if (ver.length < 3) { 
                throw new ParseException("Version number must be in the form <major>.<minor>.<revision>");
            }
            
            task.setRepositoryName(line.getOptionValue("r"));
            task.setLibraryName(line.getOptionValue("n"));
            task.setVersionDescription(line.getOptionValue("e", "(no description supplied)"));

            task.setMajorVersion((new Integer(ver[0])).intValue());
            task.setMinorVersion((new Integer(ver[1])).intValue());
            task.setRevision((new Integer(ver[2])).intValue());
            
            File bytecodeFile = new File(line.getOptionValue("b"));
            task.setBytecodeJarFile(bytecodeFile);
            
            if (line.hasOption("J")) {
                File javadocFile = new File(line.getOptionValue("J"));
                task.setJavadocZipFile(javadocFile);
            } else if (line.hasOption("j")){
                File javadocDirFile = new File(line.getOptionValue("j"));
                task.setJavadocDir(javadocDirFile);
            }
            
            task.setAllowedRoles(line.getOptionValues("a"));
            
            if (line.hasOption("p")) {
                task.setProjectDescriptor(new File(line.getOptionValue("p")));
            }
            
            if (line.hasOption("s")) {
                task.setSourceLevel(line.getOptionValue("s"));
            } 
            
            
            Project p = new Project();
            p.init();
            task.setProject(p);
            Target t = new Target();
            t.setName("publish");
            t.setProject(p);
            t.addTask(task);
            p.addTarget("publish", t);
            
            
            //Registramos un build listener para traducir los mensajes del
            //subsistema de logging de ant a log4j
            p.addBuildListener(new BuildListener() {
                
                public void buildStarted(BuildEvent e) {}
                public void buildFinished(BuildEvent e) {}
                public void targetStarted(BuildEvent e) {}
                public void targetFinished(BuildEvent e) {}
                public void taskStarted(BuildEvent e) {}
                public void taskFinished(BuildEvent e) {}
                
                public void messageLogged(BuildEvent e) {
                    if (e.getPriority() == Project.MSG_DEBUG) {
                        logger.debug(e.getMessage());
                    } else if (e.getPriority() == Project.MSG_INFO) {
                        logger.info(e.getMessage());
                    } else if (e.getPriority() == Project.MSG_WARN) {
                        logger.warn(e.getMessage());
                    } else if (e.getPriority() == Project.MSG_ERR) {
                        logger.error(e.getMessage());
                    } 
                }
                
            });
            
            
            p.executeTarget("publish");
            
        } catch (MissingOptionException e) {
            logger.error("The following options are required: " + e.getMessage());
            showHelpAndExit();
        } catch (ParseException e) {
            //Error de parsing, show help and exit...
            logger.error(e.getMessage());
            showHelpAndExit();
        } catch (NumberFormatException e) {
            logger.error("The version numbers must be integers!");
            showHelpAndExit();
        } catch (BuildException e) {
            logger.error("Unable to complete the requested operation: " + e.getMessage());
            logger.debug("Unable to complete the requested operation", e);
        }
    }
    
    
    
    public static void showHelpAndExit() {
        HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                "jdm-publish", 
                "\n" + 
                "JaDiMa Command Line Tools :: Library Upgrader v1.0\n" +
                "==================================================\n", 
                options, 
                "\n" + 
                "Copyright (c) 2004 - 2005,  Universidad Simon Bolivar :: Jesus " +
                "De Oliveira <jesus@bsc.co.ve> :: Caracas, Venezuela - " +
                "July 2005", 
                true
            );
            System.exit(1);
    }
    
    
    
    public void execute() throws BuildException {
        
        //Validamos los parametros requeridos
        if (bytecodeJarFile == null) {
            log("Bytecode jar file is required", Project.MSG_ERR);
            throw new BuildException("Bytecode jar file is required");
        }
        if (((javadocZipFile == null) && (javadocDir == null)) || ((javadocZipFile != null) && (javadocDir != null))) {
            log("Either javadoc zip file or javadoc dir is required (only one)", Project.MSG_ERR);
            throw new BuildException("Either javadoc zip file or javadoc dir is required (only one)");
        }
        if ((majorVersion == -1) || (minorVersion == -1) || (revision == -1)) {
            log("Major, minor and revision version numbers are required", Project.MSG_ERR);
            throw new BuildException("Major, minor and revision version numbers are required");
        }
        if (repositoryName == null) {
            log("Repository name is required", Project.MSG_ERR);
            throw new BuildException("Repository name is required");
        }
        
        if (!bytecodeJarFile.exists() || !bytecodeJarFile.canRead() || !bytecodeJarFile.isFile()) {
            //no existe el archivo de bytecode
            log("Bytecode jar file doesn't exists or is not readable", Project.MSG_ERR);
            throw new BuildException("Bytecode jar file doesn't exists or is not readable");
        }
        if ((javadocZipFile != null) &&
            (!javadocZipFile.exists() || !javadocZipFile.canRead() || !javadocZipFile.isFile())) {
            //no existe el archivo de bytecode
            log("Javadoc zip file doesn't exists or is not readable", Project.MSG_ERR);
            throw new BuildException("Javadoc zip file doesn't exists or is not readable");
        }
        if ((javadocDir != null) &&
            (!javadocDir.exists() || !javadocDir.canRead() || !javadocDir.isDirectory())) {
            //no existe el archivo de bytecode
            log("Javadoc directory doesn't exists or is not readable", Project.MSG_ERR);
            throw new BuildException("Javadoc directory doesn't exists or is not readable");
        }
        
        //todo ok, continuamos
        
        FileInputStream stubsIn = null;
        FileInputStream bytecodeIn = null;
        FileInputStream javadocIn = null;
        try {

            log("Publishing version " + majorVersion + "." + minorVersion + " (revision " + revision + ") of " + libraryName + " on repository " + repositoryName, Project.MSG_INFO);
            
            //Primero generamos los stubs
            Project p = new Project();
            p.init();
            for (Object o: getProject().getBuildListeners()) {
                p.addBuildListener((BuildListener)o);
            }
            Target t = new Target();
            t.setName("gen-stubs");
            t.setProject(p);
            
            StubGenerator stub = new StubGenerator();
            
            stub.setBytecodeJarFile(bytecodeJarFile);
            stub.setKeepStubsSources(false);
            stub.setMajorVersion(majorVersion);
            stub.setMinorVersion(minorVersion);
            
            File tempStubsFile = File.createTempFile("jdm-stub", ".jar");
            tempStubsFile.deleteOnExit();
            tempStubsFile.delete();
            stub.setOutFile(tempStubsFile);
            stub.setProjectDescriptor(getProjectDescriptor());
            stub.setSourceLevel(getSourceLevel());
            
            stub.setProject(p);
            t.addTask(stub);
            p.addTarget("gen-stubs", t);
            p.executeTarget("gen-stubs");
            
            if (javadocDir != null) {
                //pasaron directorio de javadocs, creamos el zip
                log("Packaging javadoc's directory (" + javadocDir.getAbsolutePath() + ")", Project.MSG_INFO);
                
                javadocZipFile = File.createTempFile("jdmpublishjavadoc", ".zip");
                javadocZipFile.delete(); //para que ant no chille
                javadocZipFile.deleteOnExit();
                Target tz = new Target();
                tz.setName("zip-javadocs");
                tz.setProject(p);
                Zip zip = new Zip();
                zip.setBasedir(javadocDir);
                zip.setDestFile(javadocZipFile);
                zip.setIncludes("**/*.*");
                zip.setProject(p);
                tz.addTask(zip);
                p.addTarget("zip-javadocs", tz);
                p.executeTarget("zip-javadocs");
                
                log("javadoc's directory successfuly packed", Project.MSG_INFO);
            }
            
            stubsIn = new FileInputStream(tempStubsFile);
            byte[] stubsBuffer = InputStreamUtil.readAll(stubsIn);
            stubsIn.close();

            bytecodeIn = new FileInputStream(bytecodeJarFile);
            byte[] bytecodeBuffer = InputStreamUtil.readAll(bytecodeIn);
            bytecodeIn.close();
            
            javadocIn = new FileInputStream(javadocZipFile);
            byte[] javadocBuffer = InputStreamUtil.readAll(javadocIn);
            javadocIn.close();

            log("Contacting remote repository", Project.MSG_INFO);
            
            //lo mandamos a publicar
            RepositoryBroker.upgrade(
                repositoryName, 
                libraryName, 
                versionDescription, 
                majorVersion, 
                minorVersion, 
                revision, 
                bytecodeBuffer, 
                stubsBuffer, 
                javadocBuffer, 
                allowedRoles
            );
            
            log("Version sucessfully published", Project.MSG_INFO);
            
        } catch (IOException e) {
            log("IO Exception caught", Project.MSG_ERR);
            throw new BuildException("IO Exception caught", e);
        } catch (LibraryNotFoundException e) {
            log("Library " + libraryName + " doesn't exists on the " + repositoryName + " repository", Project.MSG_ERR);
            throw new BuildException("Library " + libraryName + " doesn't exists on the " + repositoryName + " repository", e);
        } catch (VersionAlreadyExistsException e) {
            log("Version " + majorVersion + "." + minorVersion + " of " + libraryName + " already exists on the " + repositoryName + " repository", Project.MSG_ERR);
            throw new BuildException("Version " + majorVersion + "." + minorVersion + " of " + libraryName + " already exists on the " + repositoryName + " repository", e);
        } catch (AccessDeniedException e) {
            log("Permission denied to publish on " + repositoryName + " repository", Project.MSG_ERR);
            throw new BuildException("Permission denied to publish on " + repositoryName + " repository", e);
        } catch (RepositoryClientCommunicationException e) {
            log("Couldn't publish on " + repositoryName + " repository, nested exception is: " + e.getMessage(), Project.MSG_ERR);
            throw new BuildException("Couldn't publish on " + repositoryName + " repository", e);
        }finally {
            try { stubsIn.close(); } catch (Exception e) {}
            try { bytecodeIn.close(); } catch (Exception e) {}
            try { javadocIn.close(); } catch (Exception e) {}
        }
    }

    /**
     * Getter for property repositoryName.
     * @return Value of property repositoryName.
     */
    public String getRepositoryName() {

        return this.repositoryName;
    }

    /**
     * Setter for property repositoryName.
     * @param repositoryName New value of property repositoryName.
     */
    public void setRepositoryName(String repositoryName) {

        this.repositoryName = repositoryName;
    }

    /**
     * Getter for property libraryName.
     * @return Value of property libraryName.
     */
    public String getLibraryName() {

        return this.libraryName;
    }

    /**
     * Setter for property libraryName.
     * @param libraryName New value of property libraryName.
     */
    public void setLibraryName(String libraryName) {

        this.libraryName = libraryName;
    }

    /**
     * Getter for property versionDescription.
     * @return Value of property versionDescription.
     */
    public String getVersionDescription() {

        return this.versionDescription;
    }

    /**
     * Setter for property versionDescription.
     * @param versionDescription New value of property versionDescription.
     */
    public void setVersionDescription(String versionDescription) {

        this.versionDescription = versionDescription;
    }

    /**
     * Getter for property majorVersion.
     * @return Value of property majorVersion.
     */
    public int getMajorVersion() {

        return this.majorVersion;
    }

    /**
     * Setter for property majorVersion.
     * @param majorVersion New value of property majorVersion.
     */
    public void setMajorVersion(int majorVersion) {

        this.majorVersion = majorVersion;
    }

    /**
     * Getter for property minorVersion.
     * @return Value of property minorVersion.
     */
    public int getMinorVersion() {

        return this.minorVersion;
    }

    /**
     * Setter for property minorVersion.
     * @param minorVersion New value of property minorVersion.
     */
    public void setMinorVersion(int minorVersion) {

        this.minorVersion = minorVersion;
    }

    /**
     * Getter for property revision.
     * @return Value of property revision.
     */
    public int getRevision() {

        return this.revision;
    }

    /**
     * Setter for property revision.
     * @param revision New value of property revision.
     */
    public void setRevision(int revision) {

        this.revision = revision;
    }

    /**
     * Getter for property bytecodeJarFile.
     * @return Value of property bytecodeJarFile.
     */
    public File getBytecodeJarFile() {

        return this.bytecodeJarFile;
    }

    /**
     * Setter for property bytecodeJarFile.
     * @param bytecodeJarFile New value of property bytecodeJarFile.
     */
    public void setBytecodeJarFile(File bytecodeJarFile) {

        this.bytecodeJarFile = bytecodeJarFile;
    }

    /**
     * Getter for property javadocZipFile.
     * @return Value of property javadocZipFile.
     */
    public File getJavadocZipFile() {

        return this.javadocZipFile;
    }

    /**
     * Setter for property javadocZipFile.
     * @param javadocZipFile New value of property javadocZipFile.
     */
    public void setJavadocZipFile(File javadocZipFile) {

        this.javadocZipFile = javadocZipFile;
    }

    /**
     * Getter for property allowedRoles.
     * @return Value of property allowedRoles.
     */
    public String[] getAllowedRoles() {

        return this.allowedRoles;
    }

    /**
     * Setter for property allowedRoles.
     * @param allowedRoles New value of property allowedRoles.
     */
    public void setAllowedRoles(String[] allowedRoles) {

        this.allowedRoles = allowedRoles;
    }

    /**
     * Getter for property projectDescriptor.
     * @return Value of property projectDescriptor.
     */
    public File getProjectDescriptor() {

        return this.projectDescriptor;
    }

    /**
     * Setter for property projectDescriptor.
     * @param projectDescriptor New value of property projectDescriptor.
     */
    public void setProjectDescriptor(File projectDescriptor) {

        this.projectDescriptor = projectDescriptor;
    }

    /**
     * Getter for property javadocDir.
     * @return Value of property javadocDir.
     */
    public File getJavadocDir() {

        return this.javadocDir;
    }

    /**
     * Setter for property javadocDir.
     * @param javadocDir New value of property javadocDir.
     */
    public void setJavadocDir(File javadocDir) {

        this.javadocDir = javadocDir;
    }

    /**
     * Getter for property sourceLevel.
     * @return Value of property sourceLevel.
     */
    public String getSourceLevel() {

        return this.sourceLevel;
    }

    /**
     * Setter for property sourceLevel.
     * @param sourceLevel New value of property sourceLevel.
     */
    public void setSourceLevel(String sourceLevel) {

        this.sourceLevel = sourceLevel;
    }
    
}
