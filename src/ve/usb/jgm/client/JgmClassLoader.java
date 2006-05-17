/*
 * @(#)ClassLoader.java	1.162 02/03/19
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package ve.usb.jgm.client;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.apache.log4j.Logger;
import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.JgmStubVersionAttribute;
import ve.usb.jgm.client.RepositoryClient;
import ve.usb.jgm.util.*;

public class JgmClassLoader extends ClassLoader {
    
    private static Logger logger = Logger.getLogger(JgmClassLoader.class);
    
    static {
        //Agregamos el custom bytecode attribute reader al subsistema BCEL.
        //esto esta en un bloque estatico para garantizar que la operacion
        //es realizada ANTES de cualquier llamada al classloader, y una UNICA vez
        Attribute.addAttributeReader("ve.usb.jgm.stub.version", new StubVersionAttributeReader());
    }
    
    String[] classPath;
    private Map loadedClasses;
    
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        logger.debug("Asking to load " + name + " class and " + ((resolve)?(""):("not ")) + " resolve it");
        
        int majorVersion = 0;
        int minorVersion = 0;
        
        
        //Primero buscamos a ver si ya habiamos cargado la clase solicitada previamente
        logger.debug("Searching previously loaded clases..."+ name);
        if (loadedClasses.containsKey(name)) {
            logger.debug("Yes, we already loaded this class, returning it "+ name);
            return (Class)loadedClasses.get(name);
        } else {
            logger.debug("No, look's like it's first time " + name);
            
            //Buscamos el path del repositorio local
            if (classPath == null) classPath = System.getProperty("java.class.path").split(System.getProperty("path.separator"), -2);
            
            
            String path = name.replace('.', File.separatorChar);
            Class c = null;
            boolean found = false;
            boolean isStub = false;
            byte[] realClassData = null;
            for (int i = 0; i < classPath.length; i++) {
                try {
                    String unPath = classPath[i];
                    
                    boolean defFound = false;
                    JavaClass cl = null;
                    
                    //TODO: Method extract from here...
                    //Esto tambien se va a usar en la implementacion del getResourceAsStream de este
                    //classloader
                    
                    InputStream in = null;
                    
                    if (unPath.endsWith(".jar")) {
                        //es un jar, lo abrimos y buscamos en su interior la clase que estamos
                        //cargando
                        logger.debug("This classpath entry is a jar, searching within it... (" + unPath + ")");
                        ZipFile jar = new ZipFile(unPath);
                        int entries = jar.size();
                        ZipInputStream jar_in = new ZipInputStream(new FileInputStream(unPath));
                        in = jar_in;
                        for (int j = 0; j < entries; j++) {
                            ZipEntry entry = jar_in.getNextEntry();
                            String realName = path + ".class";
                            //logger.debug("comparing " + entry.getName() + " against " + realName);
                            if (entry.getName().equals(realName)) {
                                //lo encontramos, leemos los bytes
                                logger.debug("class found in jar, processing it");
                                cl = (new ClassParser(unPath, path + ".class")).parse();    
                                defFound = true;
                            }
                        }
                        jar.close();
                    } else {
                        String unPathS = unPath + File.separator + path + ".class";
                        logger.debug("Trying to open " + unPathS);

                        in = new FileInputStream(unPathS);

                        logger.debug("Parsing class definition");
                        cl = (new ClassParser(in, unPath + File.separator + path + ".class")).parse();    
                        logger.debug("Class definition parsed");
                        defFound = true;
                        
                        in = new FileInputStream(unPathS);
                    }
                    
                    if (!defFound) {
                        //No estaba dentro del jar, continuamos al proximo classpath entry
                        continue;
                    }
                    
                    logger.debug("Reading attributes");
                    Attribute[] at = cl.getAttributes();
                    for (int j = 0; j < at.length; j++) {
                        logger.debug("Attribute " + j + " is: " + at[j].toString());
                        try {
                            JgmStubVersionAttribute ver = (JgmStubVersionAttribute)at[j];
                            logger.debug("Version found: " + ver.getMajorVersion() + "." + ver.getMinorVersion());
                            majorVersion = ver.getMajorVersion();
                            minorVersion = ver.getMinorVersion();
                            isStub = true;

                            //Pedimos al broker que nos busque la clase
                            realClassData = RepositoryBroker.requestClass(name, majorVersion, minorVersion);
                            c = makeTheClass(name, realClassData);
                            logger.debug("saving class data for " + name);
                            loadedClasses.put(name, c);
                        } catch (ClassCastException e) {
                            logger.debug("this attribute is not jgm stub version");
                        }
                    }
                    logger.debug("Attributes readed");
                    
                    if (!(isStub)) {
                        logger.debug("Apparently is not a stub");
                        byte[] classData = InputStreamUtil.readAll(in);
                        //int bytes = in.read(classData);
                        c = defineClass(name, classData, 0, classData.length);
                        loadedClasses.put(name, c);
                        found = true;
                    }
                } catch (FileNotFoundException e) {
                    logger.debug("not found, trying next classpath entry");
                } catch (IOException e) {
                    logger.error("IOException trying to open a classpath file", e);
                }
            }
            if (!(found)) {
                logger.debug("Not found or not stub, delegating to super class loader");
                c = super.loadClass(name, resolve);
                loadedClasses.put(name, c);
            }
            return c;
        }
    }
    
    private final Class makeTheClass(String className, byte[] classData) {
        logger.debug("Defining class " + className);
        try {
            if (loadedClasses.containsKey(className)) {
                logger.debug("Yes, we already loaded this class, returning it "+ className);
                return (Class)loadedClasses.get(className);
            } else {
                logger.debug("Not previously loaded, defining it " + className);
                return defineClass(className, classData, 0, classData.length);
            }
        } catch (LinkageError e) {
            logger.error("Linkage error making the class " + className, e);
            return null;
        }
    }
    
    JgmClassLoader() {
        loadedClasses = new HashMap();
    }
}