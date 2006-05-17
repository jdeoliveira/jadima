package ve.usb.jgm.tools.admin;

import java.lang.reflect.*;
import org.apache.commons.cli.*;
import org.apache.log4j.*;
import ve.usb.jgm.client.project.*;
import java.io.*;
import ve.usb.jgm.client.*;
import org.apache.tools.ant.*;
import ve.usb.jgm.repo.*;

public class UpdateMetadata extends Task {

    private static Logger logger = Logger.getLogger(UpdateMetadata.class);
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
     * Holds value of property majorVersion.
     */
    private int majorVersion;

    /**
     * Holds value of property minorVersion.
     */
    private int minorVersion;

    /**
     * Holds value of property newDescription.
     */
    private String newDescription;

    /**
     * Holds value of property newAllowedRoles.
     */
    private java.lang.String[] newAllowedRoles;

    public UpdateMetadata() {
        //nothing to do here
        majorVersion = -1;
        minorVersion = -1;
    }
    
    public static void main(String[] args) {
        
        Logger.getRootLogger().setLevel(Level.WARN);
        String logPattern = "%-5p - %m\n";
        Logger.getLogger("org.apache.axis").setLevel(Level.FATAL);
        PatternLayout layout = new PatternLayout(logPattern);
        BasicConfigurator.configure(new ConsoleAppender(layout,"System.out"));
        
        
        //options.addOption("f", "prefetch-profile", true, "Location of the prefetching profile file (optional, defaults to project location)");
        //options.addOption("d", "project-descriptor", true, "Location of the application's project descriptor file (optional, defaults to project location)");
        
        //Inicializamos CLI
        
        options = new Options();
        
        options.addOption(
            OptionBuilder   .withArgName("repo-name")
                            .hasArg()
                            .isRequired()
                            .withDescription("Repository name")
                            .withLongOpt("repsitory-oname")
                            .create("r"));
        
        options.addOption(
            OptionBuilder   .withArgName("library-name")
                            .hasArg()
                            .withDescription("Library name")
                            .withLongOpt("library-name")
                            .isRequired()
                            .create("n"));
        
        
        //si pasa version, es que quiere modificar la descripcion o los roles de la version
        options.addOption(
            OptionBuilder   .withArgName("version-number")
                            .hasArgs(2) 
                            .withDescription("Version number of the library in the following form: <major>.<minor>")
                            .withLongOpt("version")
                            .withType(new Integer(0))
                            .withValueSeparator('.') //el separador de argumentos es el punto
                            .create("v"));
        
        
        options.addOption(
            OptionBuilder   .withArgName("new-description")
                            .hasArg()
                            .withDescription("Library's o version's new description")
                            .withLongOpt("newdescription")
                            .create("d"));
        
        options.addOption(
            OptionBuilder   .withArgName("new-allowed-roles")
                            .hasArgs()
                            .withDescription("Library's o version's new security roles allowed to access it, separated by commas (,)")
                            .withLongOpt("newallowedroles")
                            .withValueSeparator(',')
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
        
            UpdateMetadata task = new UpdateMetadata();
            task.setRepositoryName(line.getOptionValue("r"));
            task.setLibraryName(line.getOptionValue("n"));
            if (line.hasOption("v")) {
                String[] ver = line.getOptionValues("v");
                task.setMajorVersion((new Integer(ver[0])).intValue());
                task.setMinorVersion((new Integer(ver[1])).intValue());
            }
            
            if (line.hasOption("d")) {
                task.setNewDescription(line.getOptionValue("d"));
            }
            
            if (line.hasOption("a")) {
                task.setNewAllowedRoles(line.getOptionValues("a"));
            }

            Project p = new Project();
            p.init();
            task.setProject(p);
            Target t = new Target();
            t.setProject(p);
            t.setName("admin-update");
            t.addTask(task);
            p.addTarget("admin-update", t);
            
            
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
            
            
            p.executeTarget("admin-update");
            

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
    
    public void execute() {

        //Validamos los parametros requeridos
        
        if (repositoryName == null) {
            log("Repository name is required", Project.MSG_ERR);
            throw new BuildException("Repository name is required");
        }
        
        if ((newAllowedRoles == null) && (newDescription == null)) {
            log("Nothing to do! must specify new description and/or new allowed roles!", Project.MSG_ERR);
            throw new BuildException("Nothing to do! must specify new description and/or new allowed roles!");
        }
        
        try {
            
            //Vemos si lo que quiere es actualizar una version o una libreria
            if ((majorVersion == -1) && (minorVersion == -1)) {
                
                log("Updating metadata of library " + libraryName + " on repository " + repositoryName, Project.MSG_INFO);
                //quiere actualizar la libreria, porque no indico numero de version
                RepositoryBroker.updateLibraryMetadata(
                    repositoryName,
                    libraryName,
                    newDescription,
                    newAllowedRoles);
                
                log("Metadata of library " + libraryName + " updated successfuly on repository " + repositoryName, Project.MSG_INFO);
                
            } else {
                //indico numero de version, quiere actualizar la version
                
                log("Updating metadata of version " + libraryName + "-" + majorVersion + "." + minorVersion  + " on repository " + repositoryName, Project.MSG_INFO);
                
                RepositoryBroker.updateVersionMetadata(
                    repositoryName,
                    libraryName,
                    majorVersion,
                    minorVersion,
                    newDescription,
                    newAllowedRoles);
                
                log("Metadata of version " + libraryName + "-" + majorVersion + "." + minorVersion + " updated successfuly on repository " + repositoryName, Project.MSG_INFO);
            }
            
            
            
        } catch (LibraryNotFoundException e) {
            log("Library " + libraryName + " doesn't exists on the " + repositoryName + " repository", Project.MSG_ERR);
            throw new BuildException("Library " + libraryName + " doesn't exists on the " + repositoryName + " repository", e);
        } catch (VersionNotFoundException e) {
            log("Version " + majorVersion + "." + minorVersion + " of " + libraryName + " doesn't exists on the " + repositoryName + " repository", Project.MSG_ERR);
            throw new BuildException("Version " + majorVersion + "." + minorVersion + " of " + libraryName + " doesn't exists on the " + repositoryName + " repository", e);
        } catch (AccessDeniedException e) {
            log("Permission denied to administer " + repositoryName + " repository", Project.MSG_ERR);
            throw new BuildException("Permission denied to administer on " + repositoryName + " repository", e);
        } catch (RepositoryClientCommunicationException e) {
            log("Couldn't publish on " + repositoryName + " repository, nested exception is: " + e.getMessage(), Project.MSG_ERR);
            throw new BuildException("Couldn't publish on " + repositoryName + " repository", e);
        }
    }
    
    
    public static void showHelpAndExit() {
        
        HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                "jdm", 
                "\n" + 
                "JaDiMa Command Line Tools :: Library/Version Metadata Updater v1.0\n" +
                "==================================================================\n", 
                options, 
                "\n" + 
                "Copyright (c) 2004 - 2005,  Universidad Simon Bolivar :: Jesus " +
                "De Oliveira <jesus@bsc.co.ve> :: Caracas, Venezuela - " +
                "July 2005", 
                true
            );
            System.exit(1);
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
     * Getter for property newDescription.
     * @return Value of property newDescription.
     */
    public String getNewDescription() {

        return this.newDescription;
    }

    /**
     * Setter for property newDescription.
     * @param newDescription New value of property newDescription.
     */
    public void setNewDescription(String newDescription) {

        this.newDescription = newDescription;
    }

    /**
     * Getter for property newAllowedRoles.
     * @return Value of property newAllowedRoles.
     */
    public java.lang.String[] getNewAllowedRoles() {

        return this.newAllowedRoles;
    }

    /**
     * Setter for property newAllowedRoles.
     * @param newAllowedRoles New value of property newAllowedRoles.
     */
    public void setNewAllowedRoles(java.lang.String[] newAllowedRoles) {

        this.newAllowedRoles = newAllowedRoles;
    }
    
}