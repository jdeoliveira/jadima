/*
 * Test.java
 *
 * Created on 7 de julio de 2004, 12:04 AM
 */

package ve.usb.jgm.tools.stubgenerator;

import java.util.zip.*;
import java.util.jar.*;
import java.util.*;
import java.io.*;
import org.apache.velocity.app.*;
import org.apache.commons.cli.*;
import org.apache.log4j.*;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.*;
import org.apache.velocity.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import org.apache.bcel.generic.*;
import de.hunsicker.jalopy.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import ve.usb.jgm.tools.compiler.*;
import ve.usb.jgm.repo.*;
import ve.usb.jgm.client.project.*;
import ve.usb.jgm.client.*;

/**
 *
 * @author Jesus De Oliveira
 */
public class StubGenerator extends Task {
    
    //El logger de Log4J
    private static Logger logger = Logger.getLogger(StubGenerator.class);

    private static Options options;

    private File outFile;
    private File bytecodeJarFile;
    private File workDir;
    private boolean keepStubsSources;

    //en -1 para detectar cuando no son setteados
    private int majorVersion = -1; 
    private int minorVersion = -1;

    /**
     * Holds value of property projectDescriptor.
     */
    private File projectDescriptor;

    /**
     * Holds value of property sourceLevel.
     */
    private String sourceLevel;

    public StubGenerator() {
        //nothing to do here
    }
    
    public static void main(String args[]) {

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
            OptionBuilder   .withArgName("bytecode-jar-file")
                            .hasArg()
                            .withDescription("Filename of the library's bytecode jar file")
                            .withLongOpt("bytecodejar")
                            .isRequired()
                            .create("j"));
        
        options.addOption(
            OptionBuilder   .withArgName("output-stubs-jar-file")
                            .hasArg()
                            .withDescription("Filename of the destination stub jar file")
                            .withLongOpt("outstubsjar")
                            .isRequired()
                            .create("d"));
        
        options.addOption(
            OptionBuilder   .withArgName("project-descriptor")
                            .hasArg()
                            .withDescription("File name of the project descriptor, if this library requires other libraries")
                            .withLongOpt("project-descriptor")
                            .create("p"));
        
        options.addOption(
            OptionBuilder   .withArgName("work-dir")
                            .hasArg()
                            .withDescription("Temporary work directory (defaults to system temp dir)")
                            .withLongOpt("workdir")
                            .create("w"));
        
        options.addOption(
            OptionBuilder   .withArgName("version-number")
                            .hasArgs(2) //esto es que tiene 2 argumentos dentro de esta opcion
                            .withDescription("Version number of the library in the following form: <major>.<minor>")
                            .withLongOpt("version")
                            .withType(new Integer(0))
                            .withValueSeparator('.') //el separador de argumentos es el punto
                            .isRequired()
                            .create("v"));
        
        options.addOption(
            OptionBuilder   .withDescription("Keep generated stubs' source files on the work dir")
                            .withLongOpt("keepsources")
                            .create("k"));
        
        options.addOption(
            OptionBuilder   .withArgName("source-level")
                            .hasArg()
                            .withDescription("Source files JDK compatibility level (may be one of 1.3, 1.4, 1.5)")
                            .withLongOpt("sourcelevel")
                            .create("s"));
        
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

            //Tenemos todos los parametros...

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

            StubGenerator app = new StubGenerator();
            
            File bytecodeFile = new File(line.getOptionValue("j"));
            app.setBytecodeJarFile(bytecodeFile);
            
            File workDirFile = new File(line.getOptionValue("w", System.getProperty("java.io.tmpdir")));
            app.setWorkDir(workDirFile);
            
            File outJarFile = new File(line.getOptionValue("d"));
            app.setOutFile(outJarFile);
            
            if (line.hasOption("k")) {
                app.setKeepStubsSources(true);
            } else {
                app.setKeepStubsSources(false);
            }
            
            String[] ver = line.getOptionValues("v");
            
            app.setMajorVersion((new Integer(ver[0])).intValue());
            app.setMinorVersion((new Integer(ver[1])).intValue());
            
            if (line.hasOption("p")) {
                app.setProjectDescriptor(new File(line.getOptionValue("p")));
            }
            
            
            if (line.hasOption("s")) {
                app.setSourceLevel(line.getOptionValue("s"));
            } 
            
            Project p = new Project();
            p.init();
            Target t = new Target();
            t.setName("gen-stubs");
            t.addTask(app);
            app.setProject(p);
            t.setProject(p);
            p.addTarget("gen-stubs", t);

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
                    } else {
                        logger.info(e.getMessage());
                    }
                }
                
            });
            
            p.executeTarget("gen-stubs");

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
    
    public void execute() throws BuildException {
        
        //validar propiedades requeridas antes de comenzar a ejecutar
        if (bytecodeJarFile == null) {
            log("Bytecode jar file is required", Project.MSG_ERR);
            throw new BuildException("Bytecode jar file is required");
        }
        if (outFile == null) {
            log("Output stub jar file is required", Project.MSG_ERR);
            throw new BuildException("Output stub jar file is required");
        }
        if ((majorVersion == -1) || (minorVersion == -1)) {
            log("Major and minor version number are required", Project.MSG_ERR);
            throw new BuildException("Major and minor version number are required");
        }
        
        if (!bytecodeJarFile.exists() || !bytecodeJarFile.canRead()) {
            log("Bytecode jar file doesn't exists or is not readable", Project.MSG_ERR);
            throw new BuildException("Bytecode jar file doesn't exists or is not readable");
        }
        if (outFile.exists()) {
            log("Output stub jar file already exists, will be overwrited!", Project.MSG_WARN);
        }
        
        
        //todo esta ok, comenzamos
        log("Starting stub generation", Project.MSG_INFO);

        if (workDir == null) {
            workDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "jdm-stubgen-" + System.currentTimeMillis());
            workDir.deleteOnExit();
        }
        
        //Inicializamos el motor de velocity, en modo SINGLETON.
        log("Initializing velocity template engine", Project.MSG_DEBUG);
        
        Velocity.setProperty("resource.loader", "class");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.setProperty("velocimacro.library", "jgm-macros.vm");
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        Velocity.setProperty("runtime.log.logsystem.log4j.category", "org.apache.velocity");
        
        try {
            Velocity.init();
        } catch (Exception e) {
            logger.error("Can't initialize Velocity template engine", e);
            throw new BuildException("Can't initialize Velocity template engine", e);
        }
        log("Velocity sucessfully initialized", Project.MSG_DEBUG);
        

        //Aqui guardamos los directorios donde hay fuentes, para luego pasarselos a javac con *.java
        Collection<String> sourceFiles = new HashSet<String>();
        Collection<String> classFiles = new HashSet<String>();

        log("Creating working directories", Project.MSG_INFO);
        
        //Creamos el directorio de almacenamiento temporal
        File srcDir = new File(workDir.getAbsolutePath() + File.separator + "src");
        File binDir = new File(workDir.getAbsolutePath() + File.separator + "bin");
        
        if (!(srcDir.mkdirs())) {
            log("Source's temporary directory already exists", Project.MSG_WARN);
        } else {
            log("Source's temporary directory created", Project.MSG_DEBUG);
        }
        
        if (!(binDir.mkdirs())) {
            log("Binaries' temporary directory already exists", Project.MSG_WARN);
        } else {
            log("Binaries' temporary directory created", Project.MSG_DEBUG);
        }
        
        log("Working directories sucessfully created", Project.MSG_DEBUG);

        //Agregamos el jar al classpath
        log("Setting javac's classpath", Project.MSG_DEBUG);
        
        System.setProperty("java.class.path", bytecodeJarFile.getAbsolutePath());
        
        //Solicitamos los stubs de las dependencias para poder generar correctamente los .javas
        try {
            if (projectDescriptor != null) {
                Collection<File> stubsFiles = null;

                //parseamos el project.xml
                ProjectDescriptor project = ProjectDescriptorFactory.load(new FileInputStream(projectDescriptor));

                //armamos los Version's con las dependencias requeridas por el proyecto
                HashSet<Version> vers = new HashSet<Version>();
                for (Dependency d: project.getDependencies()) {
                    Version v = new Version();
                    v.setLibraryName(d.getLibraryName());
                    v.setNumberMajor(d.getMajorVersion());
                    v.setNumberMinor(d.getMinorVersion());
                    vers.add(v);
                }

                //pedimos stubs jar requeridos esten al cache
                log("Requesting required libraries to remote repositories...", Project.MSG_INFO);

                stubsFiles = RepositoryBroker.requestStubs(vers);

                log("All dependencies found", Project.MSG_INFO);

                //Agregamos los stubs de dependencias al classpath, uno por uno
                for (File f: stubsFiles) {
                    System.setProperty("java.class.path", System.getProperty("java.class.path") + File.pathSeparator + f.getAbsolutePath());
                }
            }        
        } catch (FileNotFoundException e) {
            log("Specified project descriptor file doesn't exists", Project.MSG_ERR);
            throw new BuildException("Specified project descriptor file doesn't exists", e);
        } catch (InvalidProjectDescriptorFileException e) {
            log("Specified project descriptor file is invalid", Project.MSG_ERR);
            throw new BuildException("Specified project descriptor file is invalid", e);
        } catch (VersionNotFoundException e) {
            log("Some of the requested versions are missing: " + e.getMessage(), Project.MSG_ERR);
            throw new BuildException("Some of the requested versions are missing: " + e.getMessage());
        }
        
        log("The new classpath is: " + System.getProperty("java.class.path"), Project.MSG_DEBUG);

        log("Starting bytecode jar's processing", Project.MSG_INFO);

        //Abrimos el JAR
        JarFile zip = null;
        try {
            log("Opening zip file: " + bytecodeJarFile.getAbsolutePath(), Project.MSG_DEBUG);
            
            if (bytecodeJarFile.canRead()) {
                zip = new JarFile(bytecodeJarFile, false, JarFile.OPEN_READ);
                log("Jar sucessfully opened", Project.MSG_DEBUG);
            } else {
                if (bytecodeJarFile.exists()) {
                    log("Can't read bytecode jar file", Project.MSG_ERR);
                    throw new BuildException("Can't read bytecode jar file");
                } else {
                    log("Bytecode jar file doesn't exists", Project.MSG_ERR);
                    throw new BuildException("Bytecode jar file doesn't exists");
                }
            }
        } catch (IOException e) {
            log("Unable to open library jar file: ", Project.MSG_ERR);
            throw new BuildException("Invalid library jar file", e);
        }

        //Generamos los stubs (.java's)
        Hashtable<String, String> primitives = new Hashtable<String, String>();
        primitives.put("byte", "java.lang.Byte");
        primitives.put("short", "java.lang.Short");
        primitives.put("int", "java.lang.Integer");
        primitives.put("long", "java.lang.Long");
        primitives.put("float", "java.lang.Float");
        primitives.put("double", "java.lang.Double");
        primitives.put("char", "java.lang.Character");
        primitives.put("boolean", "java.lang.Boolean");
        primitives.put("java.lang.String", "java.lang.String");

        Hashtable<String, String> primitivesNull = new Hashtable<String, String>();
        primitivesNull.put("byte", "java.lang.Byte.MIN_VALUE");
        primitivesNull.put("short", "java.lang.Short.MIN_VALUE");
        primitivesNull.put("int", "0");
        primitivesNull.put("long", "0L");
        primitivesNull.put("float", "0.000F");
        primitivesNull.put("double", "0.000D");
        primitivesNull.put("char", "' '");
        primitivesNull.put("boolean", "true");
        primitivesNull.put("java.lang.String", "\"\"");

        //hacemos una primera pasada para determinar las clases internas y sus relaciones (las metemos en un MAP)
        Map<String, Collection<String>> classesMap = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> innerClassesMap = new HashMap<String, Collection<String>>();
        
        for (Enumeration entries = zip.entries(); entries.hasMoreElements() ;) {
            JarEntry entry = (JarEntry)entries.nextElement();
            
            log("Processing zip entry " + entry, Project.MSG_DEBUG);
            
            if (!(entry.isDirectory())) {
                
                log("It's a file entry", Project.MSG_DEBUG);
                
                if (entry.getName().endsWith(".class")) {
                    
                    log("It's a class file, determining inner class relationships", Project.MSG_DEBUG);

                    //Guardamos el nombre del archivo para luego post-procesarlo
                    String sourceClassName = entry.getName().substring(0, entry.getName().lastIndexOf('.')).replace('/', File.separatorChar) + ".class";
                    String className = (entry.getName().substring(0, entry.getName().lastIndexOf('.'))).replace('/', '.');
                    
                    classFiles.add(sourceClassName);
                    
                    /*if (className.indexOf('less $') != -1) {
                        String[] temp = className.split("\\$");
                        
                        if (temp.length == 3) {
                            log("Nested inner classes not supported (" + className + ")", Project.MSG_WARN);
                                                        
                        } else {
                            log("It's a inner class of " + temp[0] + " (" + temp[1] + "), registering it", Project.MSG_DEBUG);
                            
                            //Metemos los anonymous tambien
                            if (!(classesMap.containsKey(temp[0]))) {
                                //no esta en el mapa, agregamos de una vez 
                                classesMap.put(temp[0], new HashSet<String>());
                            }
                            //la agregamos al mapa
                            classesMap.get(temp[0]).add(temp[1]);
                            
                        }
                    } else {*/
                        log("It's a no-inner class, registering it", Project.MSG_DEBUG);
                        if (!(classesMap.containsKey(className))) {
                            //no esta en el mapa, agregamos de una vez 
                            classesMap.put(className, new HashSet<String>());
                        } else {
                            log("It's already on the map", Project.MSG_DEBUG);
                        }
                        
                    /*}*/
                    
                } else {
                    log("It's not a class file, doing nothing with it...", Project.MSG_DEBUG);
                }
            } else {
                //Es directorio, lo creamos:
                
                log("It's a directory entry, creating...", Project.MSG_DEBUG);
                
                File unDir = new File(srcDir.getAbsolutePath() + File.separator + entry.getName());
                unDir.mkdirs();
                
                log("Directory sucessfully created", Project.MSG_DEBUG);
                
            }
        }
        
        
        //ya tenemos el mapa con las clases internas y no-internas, y sus relaciones, ahora
        //iteramos sobre el mapa para generar los fuentes de stubs
        for (Map.Entry<String, Collection<String>> entry: classesMap.entrySet()) {
            log("Processing " + entry.getKey() + " with these inner classes: " + entry.getValue().toString(), Project.MSG_DEBUG);
            
            
            
            VelocityContext context = new VelocityContext();
            context.put("util", new Util());
            
            context.put("primitives", primitives);
            context.put("primitivesNull", primitivesNull);
            context.put("majorVersion", majorVersion);
            context.put("minorVersion", minorVersion);

            generateStub(entry.getKey(), entry.getValue(), srcDir, context);

        }
        
        log("Bytecode jar's processing sucessfully finished", Project.MSG_INFO);

        log("Creating ant project", Project.MSG_DEBUG);
        
        //Creamos el dummy ant project
        Project p = new Project();
        p.init();
        
        //compilacion
        Target build = new Target();
        build.setName("build-stubs");
        Jdmc javac = new Jdmc();
        javac.setDestdir(binDir);
        javac.setListfiles(true);
        //agregamos al classpath el directorio bin
        Path binClassPath = javac.createClasspath();
        binClassPath.setLocation(bytecodeJarFile);
        javac.setClasspath(binClassPath);
        javac.setSource(getSourceLevel()); //ponemos el nivel de compatibilidad con los fuentes dados
        Path path = new Path(p, srcDir.getAbsolutePath());
        javac.setSrcdir(path);
        javac.setSourcepath(null);
        javac.setProjectDescriptor(getProjectDescriptor());
        build.addTask(javac);
        
        build.setProject(p);
        javac.setProject(p);
        p.addTarget("build-stubs", build);
        
        //desempaquetado de los resources
        Target unpack = new Target();
        unpack.setName("unpack-res");
        Expand unzip = new Expand();
        unzip.setSrc(bytecodeJarFile);
        unzip.setDest(binDir);
        
        PatternSet fset = new PatternSet();
        fset.setExcludes("**/**.class");
        
        unzip.addPatternset(fset);
        unzip.setProject(p);
        unpack.setProject(p);
        unpack.addTask(unzip);
        p.addTarget("unpack-res", unpack);
              
        
        //empaquetado
        Target pack = new Target();
        pack.setName("pack-stubs");
        Jar jar = new Jar();
        jar.setBasedir(binDir);
        jar.setDestFile(outFile);
        jar.setProject(p);
        pack.setProject(p);
        pack.addTask(jar);
        p.addTarget("pack-stubs", pack);
        
        //clean de los .class
        Target cleanBuild = new Target();
        cleanBuild.setName("clean-build");
        Delete delBuild = new Delete();
        delBuild.setDir(binDir);
        cleanBuild.addTask(delBuild);
        cleanBuild.setProject(p);
        delBuild.setProject(p);
        p.addTarget("clean-build", cleanBuild);
        
        //clean de los .javas
        Target cleanSrc = new Target();
        cleanSrc.setName("clean-src");
        Delete delSrc = new Delete();
        delSrc.setDir(srcDir);
        cleanSrc.addTask(delSrc);
        cleanSrc.setProject(p);
        delSrc.setProject(p);
        p.addTarget("clean-src", cleanSrc);
        
        log("Ant project sucessfully created", Project.MSG_DEBUG);

        //Ahora los compilamos
        log("Starting stubs sources compilation with jdmc", Project.MSG_INFO);
        p.executeTarget("build-stubs");
        log("Stubs sources compilation sucessfully completed", Project.MSG_INFO);

        log("Starting class files post-processing", Project.MSG_INFO);
            
        try {
            
            Iterator itCF = classFiles.iterator();
            
            while (itCF.hasNext()) {
                
                String theItem = (String)itCF.next();
                
                log("Processing " + theItem, Project.MSG_DEBUG);
                
                File classFile = new File(workDir.getAbsolutePath() + File.separator + "bin" + File.separator + theItem);
                JavaClass aClass = (new ClassParser(workDir.getAbsolutePath() + File.separator + "bin" + File.separator + theItem)).parse();
                ClassGen cg = new ClassGen(aClass);
                ConstantPoolGen cp = cg.getConstantPool();

                int idx = cp.addUtf8("ve.usb.jgm.stub.version");

                cg.addAttribute(new Unknown(idx, 2, new byte[] {(byte)majorVersion, (byte)minorVersion}, cp.getFinalConstantPool()));

                JavaClass buildClass = cg.getJavaClass();
                buildClass.dump(workDir.getAbsolutePath() + File.separator + "bin" + File.separator + theItem);
            }
        } catch (Exception e) {
            log("Exception thrown while postprocessing class files", Project.MSG_ERR);
            throw new BuildException("Exception thrown while postprocessing class files", e);
        }
        
        log("Class files post-processing finished", Project.MSG_INFO);
        
        log("Starting resources extraction", Project.MSG_INFO);
        p.executeTarget("unpack-res");
        log("Resources extraction sucessfully completed", Project.MSG_INFO);
            
        p.executeTarget("pack-stubs");
        
        log("Post-processed class files jared", Project.MSG_INFO);
            
        p.executeTarget("clean-build");
            
        log("Class files successfully deleted", Project.MSG_INFO);
        
        if (!(keepStubsSources)) {
            p.executeTarget("clean-src");
            log("Source files successfully deleted", Project.MSG_INFO);
        } else {
            log("Generated stubs source files keept on " + srcDir.getAbsolutePath(), Project.MSG_INFO);
        }
        
        log("Stubs generated successfully", Project.MSG_INFO);
        
    }
    
    private void generateStub(String className, Collection<String> innerClassesNames, File outDir, VelocityContext context) {
        FileWriter w = null;
        try {
            
            log("Searching class on the classpath", Project.MSG_DEBUG);
            
            JavaClass theClass = Repository.lookupClass(className);
            
            log("I got the class definition", Project.MSG_DEBUG);
            
            log("Searching inner classes definitions", Project.MSG_DEBUG);
            
            Collection<JavaClass> namedInnerClasses = new HashSet<JavaClass>();
            List<JavaClass> anonymousInnerClasses = new LinkedList<JavaClass>();
            
            for (String innerClassName: innerClassesNames) {
                JavaClass theInnerClass = Repository.lookupClass(className + "$" + innerClassName);
                log("Looked up JavaClass for " + className + "." + innerClassName + " is: " + theInnerClass, Project.MSG_DEBUG);
                //probamos si es anonima
                try {
                    new Integer(innerClassName);
                    log("It's anonymous", Project.MSG_DEBUG);
                    anonymousInnerClasses.add(theInnerClass);
                } catch (NumberFormatException e) {
                    log("It's not anonymous", Project.MSG_DEBUG);
                    namedInnerClasses.add(theInnerClass);
                }
            }
            /*
            //Ordenamos "alfabeticamente" las clases internas anonimas
             *TODO: VER COMO HACER ESTO
            Collections.sort(anonymousInnerClasses, new Comparator() {
                public int compare(Object o1, Object o2) {
                    
                }
                
            });
            */
            
            for (Attribute at: theClass.getAttributes()) {
                log("An attribute is: " + at.toString(), Project.MSG_DEBUG);
            }
            
             
            //Llenamos la tabla de mappings entre primitivos y wrappers
            String theClassName = theClass.getClassName().substring(theClass.getClassName().lastIndexOf('.') + 1);
            context.put("theClass", theClass);
            context.put("theNamedInnerClasses", namedInnerClasses);
            context.put("theAnonymousInnerClasses", anonymousInnerClasses);
            context.put("className", theClassName);

            log("Creating output file", Project.MSG_DEBUG);

            File f = null;
            if (theClass.getClassName().indexOf(".") != -1) {
                f = new File(outDir.getAbsolutePath() + File.separator + theClass.getClassName().substring(0, theClass.getClassName().lastIndexOf('.')).replace('.', File.separatorChar));
            } else {
                f = new File(outDir.getAbsolutePath() + File.separator + theClass.getClassName());
            }
            f.mkdirs();
            w = new FileWriter(outDir.getAbsolutePath() + File.separator + theClass.getClassName().replace('.', File.separatorChar) + ".java");
            
            if (theClass.isInterface()) {
                log("Merging interface template...", Project.MSG_DEBUG);
                Velocity.mergeTemplate("jgm-interface.vm", "ISO-8859-1", context, w);
            } else {
                log("Merging class template...", Project.MSG_DEBUG);
                log("primitives contains: " + context.get("primitives"), Project.MSG_DEBUG);
                Velocity.mergeTemplate("jgm-class.vm", "ISO-8859-1", context, w);
            }
            
            log("Template merged sucessfully", Project.MSG_DEBUG);
            
            //Ahora aplicamos reglas de formato al source si el USR
            //lo requiere...
            if (keepStubsSources) {
                log("Beautifing source code...", Project.MSG_DEBUG);
                
                Jalopy jalopy = new Jalopy();

                w.flush();
                w.close();

                File file = new File(outDir.getAbsolutePath() + File.separator + theClass.getClassName().replace('.', File.separatorChar) + ".java");

                // specify input and output target
                jalopy.setInput(file);
                jalopy.setOutput(file);

                // format and overwrite the given input file
                jalopy.format();

                if (jalopy.getState() == Jalopy.State.OK)
                    log(file + " successfully formatted", Project.MSG_DEBUG);
                else if (jalopy.getState() == Jalopy.State.WARN)
                    log(file + " formatted with warnings", Project.MSG_DEBUG);
                else if (jalopy.getState() == Jalopy.State.ERROR)
                    log(file + " could not be formatted", Project.MSG_DEBUG);
            } else {
                log("Not beautifing source code.", Project.MSG_DEBUG);
            }
            
        } catch (ResourceNotFoundException e) {
            log("Template file not found", Project.MSG_ERR);
            throw new BuildException("Template file not found", e);
        } catch (FileNotFoundException e) {
            log("Unable to open generated source file for formatting (file not found!)", Project.MSG_ERR);
            throw new BuildException("Unable to open generated source file for formatting (file not found!)", e);
        } catch (IOException e) {
            log("Unable to write generated source file", Project.MSG_ERR);
            throw new BuildException("Unable to write generated source file", e);
        } catch (org.apache.velocity.exception.ParseErrorException e) {
            log("Errors generating stub source file", Project.MSG_ERR);
            throw new BuildException("Errors generating stub source file", e);
        } catch (MethodInvocationException e) {
            log("Errors generating stub source file", Project.MSG_ERR);
            throw new BuildException("Errors generating stub source file", e);
        } catch (Exception e) {
            log("Errors generating stub source file", Project.MSG_ERR);
            throw new BuildException("Errors generating stub source file", e);
        } finally {
            try { w.close(); } catch (Exception e) {}
        }
    }
    
    public static void showHelpAndExit() {
        
        HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                "jdm-stubize", 
                "\n" + 
                "JaDiMa Command Line Tools :: Stub Generator v1.0\n" +
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

    /**
     * Getter for property outFile.
     * @return Value of property outFile.
     */
    public File getOutFile()  {

        return this.outFile;
    }

    /**
     * Setter for property outFile.
     * @param outFile New value of property outFile.
     */
    public void setOutFile(java.io.File outFile)  {

        this.outFile = outFile;
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
     * Getter for property workDir.
     * @return Value of property workDir.
     */
    public File getWorkDir() {

        return this.workDir;
    }

    /**
     * Setter for property workDir.
     * @param workDir New value of property workDir.
     */
    public void setWorkDir(File workDir) {

        this.workDir = workDir;
    }

    /**
     * Getter for property keepStubsSources.
     * @return Value of property keepStubsSources.
     */
    public boolean isKeepStubsSources() {

        return this.keepStubsSources;
    }

    /**
     * Setter for property keepStubsSources.
     * @param keepStubsSources New value of property keepStubsSources.
     */
    public void setKeepStubsSources(boolean keepStubsSources) {

        this.keepStubsSources = keepStubsSources;
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
