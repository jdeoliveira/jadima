package ve.usb.jgm.tools.query;

import org.apache.commons.cli.*;
import org.apache.log4j.*;
import java.io.*;
import ve.usb.jgm.client.*;
import ve.usb.jgm.repo.*;
import java.util.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;

public class Query {

    private static Logger logger = Logger.getLogger(Query.class);
    private static Options options;
    
    public static void main(String[] args) {
        
        Logger.getRootLogger().setLevel(Level.WARN);
        String logPattern = "%-5p - %m\n";
        Logger.getLogger("org.apache.axis").setLevel(Level.FATAL);
        Logger.getLogger("net.sourceforge.ehcache").setLevel(Level.FATAL);
        PatternLayout layout = new PatternLayout(logPattern);
        BasicConfigurator.configure(new ConsoleAppender(layout,"System.out"));
        
        
        //Inicializamos CLI
        
        options = new Options();
        
        OptionGroup group = new OptionGroup();
        
        group.addOption(
            OptionBuilder   .withDescription("Show all available libraries")
                            .withLongOpt("list")
                            .create("l"));
        
        group.addOption(
            OptionBuilder   .withDescription("Show all available versions of supplied library")
                            .withLongOpt("versions")
                            .hasArg()
                            .withArgName("library-name")
                            .create("v"));
        
        group.addOption(
            OptionBuilder   .withDescription("Download specified library javadoc documentation")
                            .withLongOpt("javadoc")
                            .hasArgs(3)
                            .withValueSeparator('.')
                            .withArgName("library.major.minor")
                            .create("j"));
        
        group.addOption(
            OptionBuilder   .withDescription("Show metadata info of the supplied library")
                            .withLongOpt("info")
                            .hasArgs(3)
                            .withValueSeparator('.')
                            .withArgName("library.major.minor")
                            .create("i"));
        
        group.setRequired(true);
        options.addOptionGroup(group);
       
        options.addOption(
            OptionBuilder   .hasArg()
                            .withArgName("repository-name")
                            .withDescription("Only query on the supplied repository")
                            .withLongOpt("repository")
                            .create("r"));
        
        options.addOption(
            OptionBuilder   .hasArg()
                            .withArgName("destination-directory")
                            .withDescription("Destination directory of the downloaded javadoc documentation (optional, defaults to <lib-name>-<major>.<minor> on the current directory)")
                            .withLongOpt("destination-dir")
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
        

            //ya tenemos TODO para comenzar
            
            if (line.hasOption("l")) {
                
                
                //quiere la lista de todas las librerias
                Collection<Library> libs = null;
                if (line.hasOption("r")) {
                    libs = RepositoryBroker.getAllLibraries(line.getOptionValue("r"));
                } else {
                    //se mete aqui RA
                    System.out.println("-------------------> Query REAA");
                    libs = RepositoryBroker.getAllLibraries();
                }
                
                for (Library lib: libs) {
                    System.out.println(lib.getName());
                }
                
                
            } else if (line.hasOption("v")) {
                
                
                //quiere la lista de todas las versiones
                Version[] vers = null;
                if (line.hasOption("r")) {
                    vers = RepositoryBroker.getLibrary(line.getOptionValue("r"), line.getOptionValue("v")).getVersions();
                } else {
                    vers = RepositoryBroker.getLibrary(line.getOptionValue("v")).getVersions();
                }
                
                for (Version v: vers) {
                    System.out.println(v.getNumberMajor() + "." + v.getNumberMinor());
                }
                
                
            } else if (line.hasOption("j")) {
                
                
                //quiere los javadocs                
                String[] verData = line.getOptionValues("j");
                Version v = RepositoryBroker.getVersion(
                    verData[0], 
                    (new Integer(verData[1])).intValue(),
                    (new Integer(verData[2])).intValue(),
                    true
                );
                
                if (v.getJavadocZip() == null) {
                    logger.warn("No javadoc returned from repository");
                }
                
                File javadocOut = File.createTempFile("jdm-javadoc", "zip");
                javadocOut.deleteOnExit();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(javadocOut);
                    fos.write(v.getJavadocZip());
                    fos.flush();
                } finally {
                    try { fos.close(); } catch (Exception e) {}
                }
                
                //ahora mando a ANT a descomprimir en -d
                Project p = new Project();
                p.init();
                Target t = new Target();
                t.setName("unzip-javadocs");
                t.setProject(p);
                Expand zip = new Expand();
                zip.setDest(new File(line.getOptionValue("d", "javadoc-" + verData[0] + "-" + verData[1] + "." + verData[2])));
                zip.setSrc(javadocOut);
                zip.setProject(p);
                t.addTask(zip);
                p.addTarget("unzip-javadocs", t);
                p.executeTarget("unzip-javadocs");
                
                
            } else if (line.hasOption("i")) {
                
                
                //quiere metadata de una version particular
                String[] verData = line.getOptionValues("i");
                Version v = RepositoryBroker.getVersion(
                    verData[0], 
                    (new Integer(verData[1])).intValue(),
                    (new Integer(verData[2])).intValue(),
                    false
                );
                
                //la mostramos en pantalla
                System.out.println("Name: " + v.getLibraryName());
                System.out.println("Description:\n" + format(v.getLibraryDescription()));
                System.out.println("--");
                System.out.println("Version " + v.getNumberMajor() + "." + v.getNumberMinor());
                System.out.println("Description:\n" + format(v.getDescription()));
                
                
            }
            
            
        } catch (MissingOptionException e) {
            logger.error("The following options are required: " + e.getMessage());
            showHelpAndExit();
        } catch (ParseException e) {
            //Error de parsing, show help and exit...
            logger.error(e.getMessage());
            showHelpAndExit();            
        } catch (RepositoryClientCommunicationException e) {
            logger.error("Couln't communicate with supplied repository");
            logger.debug("Couln't communicate with supplied repository", e);
        } catch (LibraryNotFoundException e) {
            logger.error("Requested library not found");
        }   catch (VersionNotFoundException e) {
            logger.error("Requested version not found");
        } catch (AccessDeniedException e) {
            logger.error("The supplied repository denies access to the requested resource");
        } catch (NumberFormatException e) {
            logger.error("The version numbers must be integers!");
        } catch (BuildException e) {
            logger.error("Couln't expand downloaded javadoc zip file");
            logger.debug("Couln't expand downloaded javadoc zip file", e);
        } catch (IOException e) {
            logger.error("Couln't create downloaded javadoc zip file");
            logger.debug("Couln't create downloaded javadoc zip file", e);
        }
        
    }
    
    private static String format(String input) {
        StringBuffer output = new StringBuffer("");
        String[] words = input.split(" ");
        int cols = 0;
        for (String word: words) {
            if ((cols = cols + word.length()) > 80) {
                //si le agregamos la palabra actual, se sobrepasaria de 80 cols
                //entonces metemos un newline y reiniciamos el contador cols
                output.append("\n");
                output.append(word);
                output.append(" ");
                cols = 0;
            } else {
                output.append(word);
                output.append(" ");
            }
        }
        return output.toString();
    }
    
    public static void showHelpAndExit() {
        
        HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                "jdm", 
                "\n" + 
                "JaDiMa Command Line Tools :: Repository Query v1.0\n" +
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
    
}