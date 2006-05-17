package ve.usb.jgm.tools.admin;

import java.lang.reflect.*;
import org.apache.commons.cli.*;
import org.apache.log4j.*;
import ve.usb.jgm.client.project.*;
import java.io.*;
import ve.usb.jgm.client.*;
import org.apache.tools.ant.*;
import ve.usb.jgm.repo.*;

public class UpdateRepository extends Task {

    private static Logger logger = Logger.getLogger(UpdateMetadata.class);
    private static Options options;

    /**
     * Holds value of property repositoryName.
     */
    private String repositoryName;

    /**
     * Holds value of property newPublishRoles.
     */
    private java.lang.String[] newPublishRoles;

    /**
     * Holds value of property newAdminRoles.
     */
    private java.lang.String[] newAdminRoles;

    public UpdateRepository() {
        //nothing to do here
    }
    
    public static void main(String[] args) {
        
        Logger.getRootLogger().setLevel(Level.WARN);
        String logPattern = "%-5p - %m\n";
        Logger.getLogger("org.apache.axis").setLevel(Level.FATAL);
        PatternLayout layout = new PatternLayout(logPattern);
        BasicConfigurator.configure(new ConsoleAppender(layout,"System.out"));
        
        
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
            OptionBuilder   .withArgName("new-admin-roles")
                            .hasArgs()
                            .withDescription("Repository's new security roles allowed to administer it, separated by commas (,)")
                            .withLongOpt("newadminroles")
                            .withValueSeparator(',')
                            .create("a"));
        
        options.addOption(
            OptionBuilder   .withArgName("new-publish-roles")
                            .hasArgs()
                            .withDescription("Repository's new security roles allowed to publish on it, separated by commas (,)")
                            .withLongOpt("newpublishroles")
                            .withValueSeparator(',')
                            .create("p"));
                                
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
        
            UpdateRepository task = new UpdateRepository();
            task.setRepositoryName(line.getOptionValue("r"));
            
            if (line.hasOption("p")) {
                task.setNewPublishRoles(line.getOptionValues("p"));
            }
            
            if (line.hasOption("a")) {
                task.setNewAdminRoles(line.getOptionValues("a"));
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
        
        if ((newAdminRoles == null) && (newPublishRoles == null)) {
            log("Nothing to do! must specify new admin roles and/or new publish roles!", Project.MSG_ERR);
            throw new BuildException("Nothing to do! must specify new admin roles and/or new publish roles!");
        }
        
        try {
            
            log("Updating security roles on repository " + repositoryName, Project.MSG_INFO);
            
            RepositoryBroker.setRepositoryRoles(repositoryName, newAdminRoles, newPublishRoles);
            
            log("Security roles updated successfuly on repository " + repositoryName, Project.MSG_INFO);
            
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
                "JaDiMa Command Line Tools :: Repository Security Administrator v1.0\n" +
                "===================================================================\n", 
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
     * Getter for property newPublishRoles.
     * @return Value of property newPublishRoles.
     */
    public java.lang.String[] getNewPublishRoles()  {

        return this.newPublishRoles;
    }

    /**
     * Setter for property newPublishRoles.
     * @param newPublishRoles New value of property newPublishRoles.
     */
    public void setNewPublishRoles(java.lang.String[] newPublishRoles)  {

        this.newPublishRoles = newPublishRoles;
    }

    /**
     * Getter for property newAdminRoles.
     * @return Value of property newAdminRoles.
     */
    public java.lang.String[] getNewAdminRoles()  {

        return this.newAdminRoles;
    }

    /**
     * Setter for property newAdminRoles.
     * @param newAdminRoles New value of property newAdminRoles.
     */
    public void setNewAdminRoles(java.lang.String[] newAdminRoles)  {

        this.newAdminRoles = newAdminRoles;
    }
    
}