package ve.usb.jgm.tools.compiler;

import java.lang.reflect.*;
import org.apache.commons.cli.*;
import org.apache.log4j.*;
import ve.usb.jgm.client.project.*;
import java.io.*;
import ve.usb.jgm.client.*;
import java.util.*;
import ve.usb.jgm.tools.stubgenerator.*;
import org.apache.tools.ant.*;
import ve.usb.jgm.repo.*;
import ve.usb.jgm.client.project.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.Expand;

public class Jdmc extends org.apache.tools.ant.taskdefs.Javac {

    private static Logger logger = Logger.getLogger(Jdmc.class);

    private static Options options;

    /**
     * Holds value of property projectDescriptor.
     */
    private File projectDescriptor;

   
    
    public Jdmc() {
        //nothing to do here
    }
    
    /**
     * Command line version of this task
     */
    public static void main(String[] args) {
        
        //Inicializamos log4j, con el verbosity por defecto

        Logger.getRootLogger().setLevel(Level.WARN);
        String logPattern = "%-5p - %m\n";
        Logger.getLogger("org.apache.axis").setLevel(Level.FATAL);
        Logger.getLogger("net.sourceforge.ehcache").setLevel(Level.FATAL);
        PatternLayout layout = new PatternLayout(logPattern);
        BasicConfigurator.configure(new ConsoleAppender(layout,"System.out"));
        
        
        //Inicializamos CLI
        
        options = new Options();
        
        options.addOption(
            OptionBuilder   .withArgName("project-descriptor")
                            .hasArg()
                            .withDescription("File name of the project descriptor. Defaults to <srcdir>/jdm-project.xml")
                            .withLongOpt("project-descriptor")
                            .create("p"));
        
        options.addOption(
            OptionBuilder   .withArgName("src-directories")
                            .hasArg()
                            .withDescription("Comma separated list of paths where the java source files to compile are")
                            .withLongOpt("srcdir")
                            .isRequired()
                            .withValueSeparator(',')
                            .create("s"));
        
        options.addOption(
            OptionBuilder   .withArgName("include-patterns")
                            .hasArg()
                            .withDescription("Comma separated list of filename patterns of files that should be compiled")
                            .withLongOpt("includes")
                            .withValueSeparator(',')
                            .create("i"));
        
        options.addOption(
            OptionBuilder   .withArgName("exclude-patterns")
                            .hasArg()
                            .withDescription("Comma separated list of filename patterns of files that should be ignored")
                            .withLongOpt("excludes")
                            .withValueSeparator(',')
                            .create("e"));
        
        options.addOption(
            OptionBuilder   .withArgName("source-level")
                            .hasArg()
                            .withDescription("Source files JDK compatibility level (may be one of 1.3, 1.4, 1.5)")
                            .withLongOpt("excludes")
                            .withValueSeparator(',')
                            .create("l"));
        
        options.addOption(
            OptionBuilder   .withArgName("destination-directory")
                            .hasArg()
                            .withDescription("Destination directory of the compiled classes")
                            .withLongOpt("destdir")
                            .isRequired()
                            .create("d"));
        
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
            
            Jdmc task = new Jdmc();
            
            Project p = new Project();
            p.init();
            task.setProject(p);
            
            String[] srcPaths = line.getOptionValues("s");
            Path theSrcPath = new Path(p);
            for (String s: srcPaths) {
                theSrcPath.append(new Path(p, s));
            }
            task.setSrcdir(theSrcPath);
            
            if (line.hasOption("p")) {
                task.setProjectDescriptor(new File(line.getOptionValue("p")));
            }
            
            task.setDestdir(new File(line.getOptionValue("d")));
            
            //metemos los datos de includes y excludes
            
            if (line.hasOption("i")) {
                String[] includePatterns = line.getOptionValues("i");
                for (String includePattern: includePatterns) {
                    PatternSet.NameEntry nameEntry = task.createInclude();
                    nameEntry.setName(includePattern);
                }
            }
            
            if (line.hasOption("e")) {
                String[] excludePatterns = line.getOptionValues("e");
                for (String excludePattern: excludePatterns) {
                    PatternSet.NameEntry nameEntry = task.createExclude();
                    nameEntry.setName(excludePattern);
                }
            }
            
            if (line.hasOption("l")) {
                task.setSource(line.getOptionValue("l"));
            } 
            
            Target t = new Target();
            t.setName("build");
            t.setProject(p);
            t.addTask(task);
            p.addTarget("build", t);
            
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
            
            
            p.executeTarget("build");
            
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
                "jdmc", 
                "\n" + 
                "JaDiMa Command Line Tools :: Compiler v1.0\n" +
                "================================================\n", 
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
        try {
            //verificar que los parametros requeridos se encuentren,
            //y los forbidden no esten

            if (projectDescriptor == null) {
                findProjectDescriptor();
            }

            if (projectDescriptor == null) {
                log("Project's descriptor file not found, assumming no dependencies", Project.MSG_WARN);
            } else {
                if (!(projectDescriptor.exists())) {
                    throw new BuildException("Project's descriptor file not found (" + projectDescriptor.getAbsolutePath() + " doesn't exists)");
                }

                if (!(projectDescriptor.canRead())) {
                    throw new BuildException("Can't read project's descriptor file");
                }
            }

            if (getDestdir() == null) {
                throw new BuildException("Attribute destdir is required");
            }

            //para poder meter el outdir en el classpath
            /*if (getClasspath() != null) {
                throw new BuildException("Attribute classpath is not allowed");
            }*/ 

            log("Starting compilation", Project.MSG_INFO);
            
            getDestdir().mkdirs();
            
            Collection<File> stubsFiles = null;
            
            if (projectDescriptor != null) {
                //parseamos el project.xml
                ProjectDescriptor project = ProjectDescriptorFactory.load(new FileInputStream(projectDescriptor));

                //armamos los Version's con las dependencias requeridas por el proyecto
                LinkedList<Version> vers = new LinkedList<Version>();
                for (Dependency d: project.getDependencies()) {
                    Version v = new Version();
                    v.setLibraryName(d.getLibraryName());
                    v.setNumberMajor(d.getMajorVersion());
                    v.setNumberMinor(d.getMinorVersion());
                    v.setPriority(d.getPriority());
                    vers.add(v);
                }

                //pedimos stubs jar requeridos esten al cache
                log("Requesting required libraries to remote repositories...", Project.MSG_INFO);
                
                stubsFiles = RepositoryBroker.requestStubs(vers);
                
                log("All dependencies found", Project.MSG_INFO);

                //Los agregamos al classpath y al boot classpath
                Collection<String> boot = new LinkedList<String>();
                for (File f: stubsFiles) {
                    Path stubsClassPath = super.createClasspath();
                    stubsClassPath.setLocation(f);
                    
                    //Verificar que esto no afecte la compilacion de otras libs
                    boot.add(f.getAbsolutePath());
                    
                    log("Added " + f + " to the classpath", Project.MSG_DEBUG);
                }
                
                String theLine = "-Xbootclasspath/p:";
                
                for (String s: boot) {
                    theLine = theLine + s + File.pathSeparator;
                }
                
                ImplementationSpecificArgument arg = super.createCompilerArg();
                arg.setLine(theLine);
            } 
            
            log("Current classpath is: " + super.getClasspath().toString(), Project.MSG_DEBUG);
                        
            //3. Llamamos al execute de la superclase (javac)
            log("Starting normal compilation", Project.MSG_INFO);
            super.execute();
            log("Compilation finished, unpacking stubs...", Project.MSG_INFO);
            
            
            Project p = new Project();
            p.init();
            
            Target t = new Target();
            t.setName("unjar-stubs");
            t.setProject(p);
            
            if (projectDescriptor != null) {
                for (File f: stubsFiles) {
                    Expand task = new Expand();
                    task.setProject(p);
                    task.setSrc(f);
                    task.setDest(getDestdir());
                    t.addTask(task);
                }

                p.addTarget("unjar-stubs", t);

                p.executeTarget("unjar-stubs");

                log("Stubs unpacked", Project.MSG_INFO);
            }
            
            log("Compilation finished successfuly", Project.MSG_INFO);
            
        } catch (VersionNotFoundException e) {
            log("Some of the requested versions are missing: " + e.getMessage(), Project.MSG_ERR);
            throw new BuildException("Some of the requested versions are missing: " + e.getMessage());
        } catch (IOException e) {
            log("IOException: " + e.getMessage(), Project.MSG_ERR);
            throw new BuildException("IOException", e);
        } catch (InvalidProjectDescriptorFileException e) {
            log("Project file is malformed", Project.MSG_ERR);
            throw new BuildException("Project file is malformed", e);
        }

    }
    
    private void findProjectDescriptor() {
        log("Project file descriptor not specified, searching on src dir", Project.MSG_DEBUG);
        //no lo especificaron, hay que buscarlo en la jerarquia de directorios
        //de fuentes, partiendo de cada srcdir
        String[] pathElements = getSrcdir().list();
        for (String pathElement: pathElements) {
            
            log("Searching on " + pathElement, Project.MSG_DEBUG);
            
            File project = searchForProjectDescriptor(pathElement);
            if (project != null) {
                //lo encontramos
                log("Using project file descriptor found at " + project.getAbsolutePath(), Project.MSG_INFO);
                projectDescriptor = project;
                break;
            }
        }
    }
    
    private File searchForProjectDescriptor(String startDir) {
        log("Trying on " + startDir, Project.MSG_DEBUG);
        File project = new File(startDir + File.separator + "jdm-project.xml");
        if (project.exists()) {
            return project;
        } else {
            //no existe, buscamos en el directorio padre
            File actualDir = new File(startDir);
            if (actualDir.exists() && (actualDir.getParentFile() != null)) {
                
                log("Not found, trying parent", Project.MSG_DEBUG);
                return searchForProjectDescriptor(actualDir.getParentFile().getAbsolutePath());

            } else {
                //no tiene mas padres, devolvemos null
                log("Not found, and no more parents, returning null", Project.MSG_DEBUG);
                return null;
            }
        }
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


    


    
}
