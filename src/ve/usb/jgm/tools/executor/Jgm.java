package ve.usb.jgm.tools.executor;

import java.lang.reflect.*;
import org.apache.commons.cli.*;
import org.apache.log4j.*;
import ve.usb.jgm.client.project.*;
import java.io.*;
import ve.usb.jgm.client.*;

public class Jgm {

    private static Logger logger = Logger.getLogger(Jgm.class);
    private static Options options;
    
    public static void main(String[] args) {
        
        Logger.getRootLogger().setLevel(Level.WARN);
        String logPattern = "%-5p - %m\n";
        Logger.getLogger("org.apache.axis").setLevel(Level.FATAL);
        Logger.getLogger("net.sourceforge.ehcache").setLevel(Level.FATAL);
        PatternLayout layout = new PatternLayout(logPattern);
        BasicConfigurator.configure(new ConsoleAppender(layout,"System.out"));
        
        
        //options.addOption("f", "prefetch-profile", true, "Location of the prefetching profile file (optional, defaults to project location)");
        //options.addOption("d", "project-descriptor", true, "Location of the application's project descriptor file (optional, defaults to project location)");
        
        //Inicializamos CLI
        
        options = new Options();
        
        options.addOption(
            OptionBuilder   .withArgName("main-class")
                            .hasArg()
                            .isRequired()
                            .withDescription("Fully qualified name of the application's main class")
                            .withLongOpt("mainclass")
                            .create("m"));
        
        options.addOption(
            OptionBuilder   .withArgName("project-location")
                            .hasArg()
                            .withDescription("Project's package root directory or jar file")
                            .withLongOpt("projectlocation")
                            .isRequired()
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
        

            //ya tenemos TODO para comenzar a ejecutar

            //Primero agregamos el project location al classpath del sistema
            //System.setProperty("java.class.path", line.getOptionValue("p") + System.getProperty("path.separator") + System.getProperty("java.class.path"));
            //a ver si se tarda menos, para que no busque en donde no debe las clases de la app
            System.setProperty("java.class.path", line.getOptionValue("p"));
            
            logger.debug("The system classpath is now " + System.getProperty("java.class.path"));

            JgmClassLoader cl = JgmClassLoaderFactory.createJgmClassLoader();

            String mainClassName =  line.getOptionValue("m");

            //Tomamos el resto de los parametros para pasarselos al main del host app
            String[] otherArgs = line.getArgs();
            String[] subArgs = new String[otherArgs.length];
            for (int i = 0; i < subArgs.length; i++) {
                subArgs[i] = otherArgs[i];
            }

            //aqui deberia utilizar Class.forName(...) cual sera la diferencia?
            Class mainClass = cl.loadClass(mainClassName);

            Method mainMethod = mainClass.getMethod("main", new Class[] { String[].class });

            mainMethod.invoke(null, new Object[] { subArgs });

        } catch (MissingOptionException e) {
            logger.error("The following options are required: " + e.getMessage());
            showHelpAndExit();
        } catch (ParseException e) {
            //Error de parsing, show help and exit...
            logger.error(e.getMessage());
            showHelpAndExit();            
        } catch (ClassNotFoundException e) {
            System.out.println("The supplied main class couldn't be found");
            e.printStackTrace();
            logger.error("The supplied main class couldn't be found", e);
            System.exit(1);
        } catch (NoSuchMethodException e) {
            System.out.println("The supplied main class doesn't have a main method");
            e.printStackTrace();
            logger.error("The supplied main class doesn't have a main method", e);
            System.exit(1);
        } catch (IllegalAccessException e) {
            System.out.println("IllegalAccessException:");
            e.printStackTrace();
            logger.error("IllegalAccessException:", e);
            System.exit(1);
        } catch (InvocationTargetException e) {
            System.out.println("InvocationTargetException:");
            e.printStackTrace();
            logger.error("InvocationTargetException:", e);
            System.exit(1);
        }
    }
    
    public static void showHelpAndExit() {
        
        HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                "jdm", 
                "\n" + 
                "JaDiMa Command Line Tools :: Application Runner v1.0\n" +
                "====================================================\n", 
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