/*
 * FileSystemRepository.java
 *
 * Created on 18 de junio de 2002, 12:27 AM
 */

package ve.usb.jgm.repo.backend.fs;

import ve.usb.jgm.repo.backend.*;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import com.softcorporation.xmllight.*;
import org.apache.log4j.Logger;
import ve.usb.jgm.repo.*;
import java.nio.channels.*;
import ve.usb.jgm.util.*;

/**
 *
 * @author  Jesus
 */
public class FileSystemBackend implements ve.usb.jgm.repo.backend.Backend {
    
    static Logger logger = Logger.getLogger(FileSystemBackend.class);
    
    private String backendDir;
    
    private File backendDirFile;
    
    private String[] adminRoles;
    private String[] publishRoles;
    
    public FileSystemBackend() {
        adminRoles = new String[] { "any" };
    }
    
    
    /**
     * esto esta listo
     */
    public void init() throws BackendInitializationException {
        //Chequeamos que el repositorio exista
        try {
            
            backendDirFile = new File(backendDir);
            if ((!(backendDirFile.exists())) || (!(backendDirFile.isDirectory()))) {
                throw new BackendInitializationException("Supplied backend location (" + backendDir + ") is not a valid directory");
            }

            readInfoFile();
            
        } catch (SecurityException e) {
            logger.error("Couldn't access backend location", e);
            throw new BackendInitializationException("Couldn't access backend location", e);
        } catch (BackendInternalErrorException e) {
            logger.error("Couldn't read backend info file", e);
            throw new BackendInitializationException("Couldn't read backend info file", e);
        }
    }
    
    /**
     * esto esta listo
     */
    public void configure(Element config) throws BackendConfigurationException {
        try {
            Element dir = config.getElem("directory");
            if (dir == null) throw new BackendConfigurationException("Tag <directory .../> not found inside repository configuration tag");
            
            String value = dir.getAttr("location");
            backendDir = value.replace('/', File.separatorChar);
            
            logger.debug("My repoDir is: " + backendDir);
            
        } catch (XMLLightException e) {
            logger.error("XML parsing error", e);
            throw new BackendConfigurationException("XML parsing error");
        }
    }
    
    /**
     * esto esta listo
     */
    public void create() throws BackendCreationException {
        try {
            logger.info("Creating new backend");
            
            //primero creamos el directorio donde se va a almacenar la informacion
            //de este backend
            File f = new File(backendDir);
            if (!(f.mkdirs())) throw new BackendCreationException("Supplied repository location (" + backendDir + ") couldn't be created");
            
            logger.debug("Backend dir created");
            
            File meta = new File(backendDir + File.separator + "meta");
            if (!(meta.mkdirs())) throw new BackendCreationException("Supplied repository location (" + backendDir + ") couldn't be created");
            
            logger.debug("meta dir created");
            
            File bytecode = new File(backendDir + File.separator + "bytecode");
            if (!(bytecode.mkdirs())) throw new BackendCreationException("Supplied repository location (" + backendDir + ") couldn't be created");
            
            logger.debug("bytecode dir created");
            
            adminRoles = new String[] { "any" };
            publishRoles = new String[0];
            
            logger.info("adminRoles set to 'any' and publish roles set to none. PLEASE SET THE CORRECT ADMIN AND PUBLISH ROLES");
            
            logger.debug("saving config file");
            saveInfoFile();
            logger.debug("config file saved");
            logger.info("Backend created sucessfully");
            
        } catch (SecurityException e) {
            logger.error("Supplied repository location (" + backendDir + ") couldn't be created", e);
            throw new BackendCreationException("Supplied repository location (" + backendDir + ") couldn't be created", e);
        } catch (BackendInternalErrorException e) {
            logger.error("Couldn't create backend info file", e);
            throw new BackendCreationException("Couldn't create backend info file", e);
        }
    }

    
    /**
     * esta listo
     */
    public void deleteLibrary(final String libraryName) throws LibraryNotFoundException, BackendInternalErrorException {
        File libFile = new File(backendDir + File.separator + "meta" + File.separator + libraryName);

        if (!(libFile.exists())) {
            throw new LibraryNotFoundException();
        }

        //borramos TODO el directorio
        deleteDir(libFile);

        //Ahora debemos borrar dentro de bytecode todo lo que tenga el nombre de la lib appended al final
        deleteMatchingFiles(
            new File(backendDir + File.separator + "bytecode"), 
            new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    String[] comps = name.split("\\.");
                    if (comps.length == 3) {
                        return (comps[2]).equals(libraryName);
                    } else {
                        return false;
                    }
                }
            }
        );
            
    }

    //es necesario?????
    public void deleteRevision(String libraryName, int majorVersion, int minorVersion, int revisionNumber) {
        
    }

    /**
     * esta listo
     */
    public void deleteVersion(final String libraryName, final int majorVersion, final int minorVersion) throws LibraryNotFoundException, VersionNotFoundException {

        File libDir = new File(backendDir + File.separator + "meta" + File.separator + libraryName);
        if (!(libDir.exists())) {
            throw new LibraryNotFoundException();
        }

        //ahora borramos la version
        File verDir = new File(backendDir + File.separator + "meta" + File.separator + libraryName + File.separator + "version-" + majorVersion + "." + minorVersion);

        if (!(verDir.exists())) {
            throw new VersionNotFoundException();
        }

        //borramos TODO el directorio
        deleteDir(verDir);

        //Ahora debemos borrar dentro de bytecode (dentro del subdirectorio major.minor) todo lo que 
        //tenga el nombre de la lib appended al final
        deleteMatchingFiles(
            new File(backendDir + File.separator + "bytecode" + File.separator + majorVersion + "." + minorVersion), 
            new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    String[] comps = name.split("\\.");
                    if (comps.length == 3) {
                        return (name.split("\\.")[2]).equals(libraryName);
                    } else {
                        return false;
                    }
                }
            }
        );

        //limpiamos los directorios que hayan quedado vacios
        deleteEmptyDirs(new File(backendDir + File.separator + "bytecode" + File.separator + majorVersion + "." + minorVersion));
            
    }
    

    /**
     * esto esta listo
     */
    public Collection<Library> getAllLibraries() throws BackendInternalErrorException {
        try {
                        
            logger.debug("Requesting all libraries");
            
            HashSet<Library> result = new HashSet<Library>();
            
            File libsDir = new File(backendDir + File.separator + "meta");

            String[] libs = libsDir.list();

            for (String aLib: libs) {
                Library lib = getLibrary(aLib);
                result.add(lib);
            }
            
            logger.debug("Libraries found: " + Arrays.deepToString(result.toArray(new Library[0])));
            
            return result;
        } catch (LibraryNotFoundException e) {
            logger.error("Library not found exception searching all libraries, check repository directories", e);
            throw new BackendInternalErrorException("Library not found exception searching all libraries, check repository directories", e);
        } catch (SecurityException e) {
            logger.error("Couldn't access libraries meta directory, check repository directories", e);
            throw new BackendInternalErrorException("Couldn't access libraries meta directory, check repository directories", e);
        }
    }

    /**
     * esto esta listo
     */
    public Bytecode getBytecode(final String classname, int majorVersion, int minorVersion) throws LibraryNotFoundException, VersionNotFoundException, BytecodeNotFoundException, BackendInternalErrorException {
        FileInputStream in = null;
        try {
            logger.info("Searching bytecode of " + classname);
            
            //Ubicamos la ultima revision del archivo class dado
            
            //Primero localizamos todos los archivos cuyo nombre comience por el nombre
            //de la clase (traducido a paths)
            String packageFileName = backendDir + File.separator + "bytecode" + File.separator + majorVersion + "." + minorVersion + File.separator + classname.substring(0, classname.lastIndexOf('.')).replace('.', File.separatorChar);
            File packageFile = new File(packageFileName);
            
            File[] matchingFiles = packageFile.listFiles(
                new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.startsWith(classname.substring(classname.lastIndexOf('.') + 1));
                    }
                }
            );
            
            logger.debug("Searching last revision");
            File lastRevisionFile = null;
            int lastRevision = -1; //para poder permitir revisiones = 0
            String libName = null;
            logger.debug(matchingFiles.length + " revisions found on " + packageFileName);
            for (File f: matchingFiles) {
                String[] tokens = f.getName().split("\\.");
                logger.debug("this revision file name " + f.getName() + " split spits this: " + Arrays.deepToString(tokens));
                int actualRevision = (new Integer(tokens[1])).intValue();
                logger.debug("iterating, actual revision: " + actualRevision + ", last revision: " + lastRevision);
                if (actualRevision > lastRevision) {
                    lastRevision = actualRevision;
                    lastRevisionFile = f;
                    libName = tokens[2];
                }
            }
            
            if (lastRevisionFile == null) {
                //No se encontro ninguna revision, es decir, no existe la version dada
                throw new VersionNotFoundException();
            }
            
            //ahora tenemos en f el archivo de la ultima revision
            logger.debug("Last revision is: " + lastRevisionFile.getName());
            
            //Buscamos permisologia de la libreria + version a la que pertenece esta clase 
            Version v = getVersion(libName, majorVersion, minorVersion, false, false);
            
            in = new FileInputStream(lastRevisionFile);
            byte[] classData = InputStreamUtil.readAll(in);
            
            logger.debug("Readed bytecode is (first 3 bytes): " + new Byte(classData[0]) + " " + new Byte(classData[1]) + " " + new Byte(classData[2]));
            
            return new Bytecode(classname, classData, majorVersion, minorVersion, lastRevision, v.getAllowedRoles());
            
        } catch (FileNotFoundException e) {
            logger.debug("Class not found in this backend", e);
            throw new BytecodeNotFoundException("Class not found in this backend");
        } catch (IOException e) {
            logger.error("Couldn't read class data", e);
            throw new BackendInternalErrorException("Couldn't read class data", e);
        } catch (SecurityException e) {
            logger.error("Couldn't read class data", e);
            throw new BackendInternalErrorException("Couldn't read class data", e);
        }  finally {
            try { in.close(); } catch (Exception e) {}
        }
    }

    /**
     * esto esta listo
     */
    public Library getLibrary(String libraryName) throws LibraryNotFoundException, BackendInternalErrorException {
        
        logger.info("Searching libary " + libraryName);
            
        Library l = new Library();
        l.setName(libraryName);
            
        try {
            
            //Primero localizamos el archivo de metadatos de la libreria
            Element root = XMLLightUtil.readXMLFile(backendDir + File.separator + "meta" + File.separator + libraryName + File.separator + "library-info.xml");
            
            Element description = root.getElem("description");
            l.setDescription(description.getText());
            
            Element allowedRoles = root.getElem("allowedRoles");
            l.setAllowedRoles(allowedRoles.getAttr("roles").split(","));
            
        } catch (FileNotFoundException e) {
            logger.debug("Library info file not found", e);
            throw new LibraryNotFoundException("Library info file not found", e);
        } catch (IOException e) {
            logger.error("Couln't read library info file", e);
            throw new BackendInternalErrorException("Couln't read library info file", e);
        } catch (XMLLightException e) {
            logger.error("Error parsing library info file", e);
            throw new BackendInternalErrorException("Error parsing library info file", e);
        } 
            
        try {
            
            //ahora buscamos cada una de sus versiones
            File libDir = new File(backendDir + File.separator + "meta" + File.separator + libraryName);
            String[] versions = libDir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith("version");
                }
            });
            
            for (String aVersion: versions) {
                String ver = aVersion.substring(aVersion.lastIndexOf('-') + 1);
                String[] numbers = ver.split("\\.");
                int major = (new Integer(numbers[0])).intValue();
                int minor = (new Integer(numbers[1])).intValue();
                
                Version version = getVersion(libraryName, major, minor, false, false);
                
                l.addVersion(version);
            }
         
            return l;
            
        } catch (SecurityException e) {
            logger.error("Couln't access version directory", e);
            throw new BackendInternalErrorException("Couln't access version directory", e);
        } catch (VersionNotFoundException e) {
            logger.error("Version not found while loading library", e);
            throw new BackendInternalErrorException("Version not found while loading library", e);
        }
    }
    

    /**
     * esto esta listo
     */
    public Version getVersion(String libraryName, int majorVersion, int minorVersion, boolean deliverStubJar, boolean deliverJavadocZip) throws LibraryNotFoundException, VersionNotFoundException, BackendInternalErrorException {
    
        logger.info("Searching version " + majorVersion + "." + minorVersion + " of library " + libraryName + " (delivering: " + ((deliverStubJar)?("stubs"):("")) + ((deliverJavadocZip)?("javadocs"):("")) + ")");

        Version v = new Version();
        v.setNumberMajor(majorVersion);
        v.setNumberMinor(minorVersion);
        
        String[] libAllowedRoles = null;
    
        try {   
            //Primero localizamos el archivo de metadatos de la libreria
            Element root = XMLLightUtil.readXMLFile(backendDir + File.separator + "meta" + File.separator + libraryName + File.separator + "library-info.xml");
            
            Element description = root.getElem("description");
            v.setLibraryDescription(description.getText());
            
            Element roles = root.getElem("allowedRoles");
            
            libAllowedRoles = roles.getAttr("roles").split(",");
            
        } catch (FileNotFoundException e) {
            logger.debug("Library metadata file not found", e);
            throw new LibraryNotFoundException("Library metadata file not found", e);
        } catch (IOException e) {
            logger.error("Unable to read Library metadata file", e);
            throw new BackendInternalErrorException("Unable to read Library metadata file", e);
        } catch (XMLLightException e) {
            logger.error("Error parsing library metadata file", e);
            throw new BackendInternalErrorException("Error parsing library metadata file", e);
        }
            
        try {   
            //Localizamos el archivo de metadatos de la version
            
            Element root = XMLLightUtil.readXMLFile(backendDir + File.separator + "meta" + File.separator + libraryName + File.separator + "version-" + majorVersion + "." + minorVersion + File.separator + "version-info.xml");
            
            Element description = root.getElem("description");
            v.setDescription(description.getText());
            v.setLibraryName(libraryName);
            v.setNumberMajor(majorVersion);
            v.setNumberMinor(minorVersion);
            
            Element allowedRolesEl = root.getElem("allowedRoles");
            
            HashSet<String> allowedRoles = new HashSet<String>(Arrays.asList(allowedRolesEl.getAttr("roles").split(",")));
            HashSet<String> allowedRolesLib = new HashSet<String>(Arrays.asList(libAllowedRoles));
            HashSet<String> effectiveAllowedRoles = new HashSet<String>();
            for (String rol: allowedRoles) {
                if (allowedRolesLib.contains(rol)) {
                    effectiveAllowedRoles.add(rol);
                }
            }
            v.setAllowedRoles(effectiveAllowedRoles.toArray(new String[0]));
            
            if (deliverStubJar) {
                
                //aqui deberia usar NIO!!!!
                FileInputStream jarIn = new FileInputStream(backendDir + File.separator + "meta" + File.separator + libraryName + File.separator + "version-" + majorVersion + "." + minorVersion + File.separator + "stubjar.jar");
                v.setStubsJar(InputStreamUtil.readAll(jarIn));
                jarIn.close();
            }
            
            if (deliverJavadocZip) {
                //Nos pidieron cargar el zip de javadocs, lo leemos
                FileInputStream jarIn = new FileInputStream(backendDir + File.separator + "meta" + File.separator + libraryName + File.separator + "version-" + majorVersion + "." + minorVersion + File.separator + "javadoc.zip");
                v.setJavadocZip(InputStreamUtil.readAll(jarIn));
                logger.debug("loaded javadoc zip file: " + v.getJavadocZip());
            }
            
            //ahora buscamos cada una de sus revisiones
            Element revisions = root.getElem("revisions");
            Element rev = revisions.getElem("revision");
            while (!(rev.isNull())) {
                Revision r = new Revision();
                r.setRevisionNumber((new Integer(rev.getAttr("number"))).intValue());
                v.addRevision(r);
                rev = revisions.getElem("revision");
            }
            
            return v;
            
        } catch (FileNotFoundException e) {
            logger.debug("Version metadata file not found", e);
            throw new VersionNotFoundException("Version metadata file not found", e);
        } catch (IOException e) {
            logger.error("Unable to read version metadata file", e);
            throw new BackendInternalErrorException("Unable to read version metadata file", e);
        } catch (XMLLightException e) {
            logger.error("Error parsing version metadata file", e);
            throw new BackendInternalErrorException("Error parsing version metadata file", e);
        }    
    }

    public String[] getAdminRoles() {
        return adminRoles;
    }

    public void setAdminRoles(String[] newRoles) throws BackendInternalErrorException {
        adminRoles = newRoles;
        saveInfoFile();
    }
    
    public String[] getPublishRoles() {
        return publishRoles;
    }

    public void setPublishRoles(String[] newRoles) throws BackendInternalErrorException {
        publishRoles = newRoles;
        saveInfoFile();
    }

    public void storeLibrary(Library lib) throws LibraryAlreadyExistsException, BackendInternalErrorException {
        
        //crear directorio en meta para la libreria
        File libDir = new File(backendDir + File.separator + "meta" + File.separator + lib.getName());
        if (!(libDir.mkdir())) throw new LibraryAlreadyExistsException();

        //construir descriptor de libreria (library-info.xml)
        try {

            Element libInfoEl = new Element("library-info");
            libInfoEl.setAttr("name", lib.getName());

            Element libDescriptionEl = new Element("description");
            libDescriptionEl.setCont(lib.getDescription());

            Element allowedRolesEl = new Element("allowedRoles");
            String roles = "";
            for (String r: lib.getAllowedRoles()) {
                roles = roles + "," + r;
            }
            allowedRolesEl.setAttr("roles", roles.substring(1));

            libInfoEl.addElem(libDescriptionEl);
            libInfoEl.addElem(allowedRolesEl);

            XMLLightUtil.writeXMLFile(backendDir + File.separator + "meta" + File.separator + lib.getName() + File.separator + "library-info.xml", libInfoEl);
            
        } catch (IOException e) {
            logger.error("Unable to write library info file", e);
            throw new BackendInternalErrorException("Unable to write library info file", e);
        } 
        
        //guardamos la(s) version(es)
        try {
            for (Version v: lib.getVersions()) {
                storeVersion(lib.getName(), v);
            }
        } catch (VersionAlreadyExistsException e) {
            logger.error("Version already exists while creating new library", e);
            throw new BackendInternalErrorException("Version already exists while creating new library", e);
        } catch (LibraryNotFoundException e) {
            logger.error("Library not found while creating new library", e);
            throw new BackendInternalErrorException("Library not found while creating new library", e);
        }
    }

    public void storeVersion(String libraryName, Version version) throws VersionAlreadyExistsException, LibraryNotFoundException, BackendInternalErrorException {
        
        //Primero vemos si existe el directorio de la libreria
        File libDir = new File(backendDir + File.separator + "meta" + File.separator + libraryName);
        if ((!(libDir.exists())) || (!(libDir.isDirectory()))) {
            throw new LibraryNotFoundException(libraryName + " not found in this backend");
        }
        
        //crear directorio en meta para la version
        File verDir = new File(backendDir + File.separator + "meta" + File.separator + libraryName + File.separator + "version-" + version.getNumberMajor() + "." + version.getNumberMinor());
        if (!(verDir.mkdir())) throw new VersionAlreadyExistsException();
        
        //crear directorio en bytecode para la version
        File bDir = new File(backendDir + File.separator + "bytecode" + File.separator + version.getNumberMajor() + "." + version.getNumberMinor());
        bDir.mkdir(); //no nos importa si se creo o ya existia, puede haber sido creado por otra libreria
        
        //construir descriptor de la version (version-info.xml)
        try {
            logger.debug("Storing new version of " + libraryName);

            Element infoEl = new Element("version-info");

            Element descriptionEl = new Element("description");
            descriptionEl.setCont(version.getDescription());

            Element allowedRolesEl = new Element("allowedRoles");
            String roles = "";
            for (String r: version.getAllowedRoles()) {
                roles = roles + "," + r;
            }
            allowedRolesEl.setAttr("roles", roles.substring(1));

            infoEl.addElem(descriptionEl);
            infoEl.addElem(allowedRolesEl);
            infoEl.addElem(new Element("revisions"));

            logger.debug("Newly created document is: " + XMLLight.getXMLDocument(infoEl));
             
            logger.debug("Saving metadata file");
            XMLLightUtil.writeXMLFile(backendDir + File.separator + "meta" + File.separator + libraryName + File.separator + "version-" + version.getNumberMajor() + "." + version.getNumberMinor() + File.separator + "version-info.xml", infoEl);
            logger.debug("Metadata file saved");
            
        } catch (IOException e) {
            logger.error("Unable to write version info file", e);
            throw new BackendInternalErrorException("Unable to write version info file", e);
        } 
        
        //Guardamos el jar stub
        FileOutputStream jarOut = null;
        try {
            File jarFile = new File(backendDir + File.separator + "meta" + File.separator +  libraryName + File.separator + "version-" + version.getNumberMajor() + "." + version.getNumberMinor() + File.separator + "stubjar.jar");
            jarFile.createNewFile();
            jarOut = new FileOutputStream(jarFile);
            jarOut.write(version.getStubsJar());
        } catch (IOException e) {
            logger.error("Couldn't create stub jar file", e);
            throw new BackendInternalErrorException("Couldn't create stub jar file", e);
        } finally {
            try { jarOut.close(); } catch (Exception e) { }
        }
        
        //Guardamos el zip de javadocs
        FileOutputStream javadocOut = null;
        try {
            File zipFile = new File(backendDir + File.separator + "meta" + File.separator +  libraryName + File.separator + "version-" + version.getNumberMajor() + "." + version.getNumberMinor() + File.separator + "javadoc.zip");
            zipFile.createNewFile();
            javadocOut = new FileOutputStream(zipFile);
            javadocOut.write(version.getJavadocZip());
        } catch (IOException e) {
            logger.error("Couldn't create javadoc zip file", e);
            throw new BackendInternalErrorException("Couldn't create javadoc zip file", e);
        } finally {
            try { javadocOut.close(); } catch (Exception e) { }
        }
        
        //Guardamos las revisiones
        try {
            for (Revision r: version.getRevisions()) {
                storeRevision(libraryName, version.getNumberMajor(), version.getNumberMinor(), r);
            }
        } catch (RevisionAlreadyExistsException e) {
            logger.error("Revision already exists while creating new version", e);
            throw new BackendInternalErrorException("Revision already exists while creating new version", e);
        } catch (LibraryNotFoundException e) {
            logger.error("Library not found while creating new version (storing revisions)", e);
            throw new BackendInternalErrorException("Library not found while creating new version (storing revisions)", e);
        } catch (VersionNotFoundException e) {
            logger.error("Version not found while creating new version (storing revisions)", e);
            throw new BackendInternalErrorException("Version not found while creating new version (storing revisions)", e);
        }
    }
    
    /**
     * Esto esta listo
     */
    public void storeRevision(String libraryName, int majorVersion, int minorVersion, Revision r) throws RevisionAlreadyExistsException, VersionNotFoundException, LibraryNotFoundException, BackendInternalErrorException {
        
        //Primero vemos si existe el directorio de la libreria
        File libDir = new File(backendDir + File.separator + "meta" + File.separator + libraryName);
        if ((!(libDir.exists())) || (!(libDir.isDirectory()))) {
            throw new LibraryNotFoundException(libraryName + " not found in this backend");
        }
        
        //Ahora vemos si existe el directorio de la version
        File verDir = new File(backendDir + File.separator + "meta" + File.separator + libraryName + File.separator + "version-" + majorVersion + "." + minorVersion);
        if ((!(verDir.exists())) || (!(verDir.isDirectory()))) {
            throw new VersionNotFoundException(libraryName + "-" + majorVersion  + "." + minorVersion + " not found in this backend");
        }
        
        //actualizamos descriptor de la version (version-info.xml)
        try {
            logger.debug("Storing new revision of " + libraryName);

            //Localizamos el archivo de metadatos de la version
            Element root = XMLLightUtil.readXMLFile(backendDir + File.separator + "meta" + File.separator + libraryName + File.separator + "version-" + majorVersion + "." + minorVersion + File.separator + "version-info.xml");
            
            logger.debug("Original document is: " + XMLLight.getXMLDocument(root));
            
            Element revisions = root.getElem("revisions");
            
            //Revisamos que no exista ya la revision
            Element rev = revisions.getElem("revision");
            while (!(rev.isNull())) {
                if (rev.getAttr("number").equals("" + r.getRevisionNumber())) throw new RevisionAlreadyExistsException();
                rev = revisions.getElem("revision");
            }
            
            Element newRev = new Element("revision");
            newRev.setAttr("number", r.getRevisionNumber());

            revisions.addElem(newRev);

            root = XMLLightUtil.replaceElem(root, revisions);

            logger.debug("Modified document is: " + XMLLight.getXMLDocument(root));
            
            logger.debug("saving version metadata file");
            XMLLightUtil.writeXMLFile(backendDir + File.separator + "meta" + File.separator + libraryName + File.separator + "version-" + majorVersion + "." + minorVersion + File.separator + "version-info.xml", root);
            logger.debug("Version metadata file saved");
            
        } catch (IOException e) {
            logger.error("Unable to update version info file", e);
            throw new BackendInternalErrorException("Unable to update version info file", e);
        } catch (SecurityException e) {
            logger.error("Unable to update version info file (access denied)", e);
            throw new BackendInternalErrorException("Unable to update version info file (access denied)", e);
        } catch (XMLLightException e) {
            logger.error("Unable to update version info file (xml parsing error)", e);
            throw new BackendInternalErrorException("Unable to update version info file (xml parsing error)", e);
        } 
        
        //ahora guardamos el bytecode
        FileOutputStream bytecodeOut = null;
        try {
            for (Bytecode b: r.getClasses()) {
                logger.debug("storing bytecode of " + b.getClassName());
                if (b.getClassName().indexOf('.') != -1) {
                    logger.debug("has package component, creating package directory");
                    File packageFile = new File(backendDir + File.separator + "bytecode" + File.separator + majorVersion + "." + minorVersion + File.separator + b.getClassName().substring(0, b.getClassName().lastIndexOf('.')).replace('.', File.separatorChar));
                    packageFile.mkdirs();
                }
                String bytecodeFileName = backendDir + File.separator + "bytecode" + File.separator + majorVersion + "." + minorVersion + File.separator + b.getClassName().replace('.', File.separatorChar) + "." + r.getRevisionNumber() + "." + libraryName;
                logger.debug("storing on " + bytecodeFileName);
                File bytecodeFile = new File(bytecodeFileName);
                if (!(bytecodeFile.createNewFile())) throw new BackendInternalErrorException("Bytecode file already exists");
                
                bytecodeOut = new FileOutputStream(bytecodeFile);
                logger.debug("writing " + b.getClassData().length + " bytes");
                bytecodeOut.write(b.getClassData());
                bytecodeOut.flush();
                bytecodeOut.close();
            }
        } catch (IOException e) {
            logger.error("Couldn't write bytecode file", e);
            throw new BackendInternalErrorException("Couldn't write bytecode file", e);
        } catch (SecurityException e) {
            logger.error("Couldn't write bytecode file (access denied)", e);
            throw new BackendInternalErrorException("Couldn't write bytecode file (access denied)", e);
        } finally {
            try { bytecodeOut.close(); } catch (Exception e) {}
        }
    }    

    
    private void saveInfoFile() throws BackendInternalErrorException {
        try {
            logger.debug("saving backend config file");
            
            Element rootEl = new Element("backend-info");
            
            Element adminRolesEl = new Element("adminRoles");
            String roles = "";
            if (adminRoles.length > 0) {
                for (String r: adminRoles) {
                    roles = roles + "," + r;
                }
                roles = roles.substring(1);
            }
            adminRolesEl.setAttr("roles", roles);   
            rootEl.addElem(adminRolesEl);
            
            Element publishRolesEl = new Element("publishRoles");
            roles = "";
            if (publishRoles.length > 0) {
                for (String r: publishRoles) {
                    roles = roles + "," + r;
                }
                roles = roles.substring(1);
            }    
            publishRolesEl.setAttr("roles", roles);
            rootEl.addElem(publishRolesEl);
            
            XMLLightUtil.writeXMLFile(backendDir + File.separator + "backend-info.xml", rootEl);
            
            logger.debug("backend config file saved");
        } catch (IOException e) {
            logger.error("Error writing backend info file", e);
            throw new BackendInternalErrorException("Error writing backend info file", e);
        } 
    }
    
    private void readInfoFile() throws BackendInternalErrorException {
        try {
            
            Element root = XMLLightUtil.readXMLFile(backendDir + File.separator + "backend-info.xml");
            
            Element allowedRoles = root.getElem("adminRoles");
            adminRoles = (allowedRoles.getAttr("roles").split(","));
            
            Element publishRolesEl = root.getElem("publishRoles");
            publishRoles = (publishRolesEl.getAttr("roles").split(","));
            
        } catch (FileNotFoundException e) {
            throw new BackendInternalErrorException("Backend info file not found", e);
        } catch (IOException e) {
            throw new BackendInternalErrorException("Couln't read backend info file", e);
        } catch (XMLLightException e) {
            throw new BackendInternalErrorException("Error parsing backend info file", e);
        } 
    }
    
    private void deleteDir(File parent) {
        for (File f: parent.listFiles()) {
            if (f.isDirectory()) {
                deleteDir(f);
            } else {
                f.delete();
            }
        }
        parent.delete();
    }
    
    private void deleteEmptyDirs(File parent) {
        if (parent.isDirectory()) {
            for (File f: parent.listFiles()) {
                if (f.isDirectory()) {
                    deleteEmptyDirs(f);
                } 
            }
            parent.delete();
        }
    }
    
    private void deleteMatchingFiles(File parent, FilenameFilter filter) {
        //borramos lo que tenga match en el directorio actual
        for (File f: parent.listFiles(filter)) {
            f.delete();
        }
        
        //ahora recursivamente borramos en los subdirectorios del directorio actual
        for (File f: parent.listFiles()) {
            if (f.isDirectory()) {
                deleteMatchingFiles(f, filter);
            }
        }
    }

    public void updateLibrary(Library lib) throws LibraryNotFoundException, BackendInternalErrorException {

        try {
            
            logger.info("Updating library metadata of " + lib.getName());
            
            //localizamos el archivo de metadatos de la libreria
            
            Element rootEl = XMLLightUtil.readXMLFile(backendDir + File.separator + "meta" + File.separator + lib.getName() + File.separator + "library-info.xml");
            
            if (lib.getDescription() != null) {
                Element libDescriptionEl = new Element("description");
                libDescriptionEl.setCont(lib.getDescription());
                rootEl = XMLLightUtil.replaceElem(rootEl, libDescriptionEl);
            }
            
            if (lib.getAllowedRoles() != null) {

                Element allowedRolesEl = new Element("allowedRoles");
                
                String roles = "";
                for (String r: lib.getAllowedRoles()) {
                    roles = roles + "," + r;
                }
                allowedRolesEl.setAttr("roles", roles.substring(1));
                
                rootEl = XMLLightUtil.replaceElem(rootEl, allowedRolesEl);
            }
            
            XMLLightUtil.writeXMLFile(backendDir + File.separator + "meta" + File.separator + lib.getName() + File.separator + "library-info.xml", rootEl);
            
        } catch (FileNotFoundException e) {
            logger.debug("Library metadata file not found", e);
            throw new LibraryNotFoundException();
        } catch (IOException e) {
            logger.debug("Couln't read library metadata file", e);
            throw new BackendInternalErrorException("Couln't read library metadata file", e);
        } catch (XMLLightException e) {
            logger.debug("Errors parsing library metadata file", e);
            throw new BackendInternalErrorException("Errors parsing library metadata file", e);
        }         
    }

    public void updateVersion(Version v) throws LibraryNotFoundException, VersionNotFoundException, BackendInternalErrorException {

        try {
            logger.info("Updating version metadata of " + v.getLibraryName() + "-" + v.getNumberMajor() + "." + v.getNumberMinor());
            
            //localizamos el archivo de metadatos de la libreria
            File f = new File(backendDir + File.separator + "meta" + File.separator + v.getLibraryName());
            if (!(f.exists())) {
                throw new LibraryNotFoundException();
            }
            
            //ahora si abrimos el archivo de metadatos de la version
            Element rootEl = XMLLightUtil.readXMLFile(f.getAbsolutePath() + File.separator + "version-" + v.getNumberMajor() + "." + v.getNumberMinor() + File.separator + "version-info.xml");
            
            if (v.getDescription() != null) {
                Element verDescriptionEl = new Element("description");
                verDescriptionEl.setCont(v.getDescription());
                rootEl = XMLLightUtil.replaceElem(rootEl, verDescriptionEl);
            } 

            if (v.getAllowedRoles() != null) {
                Element allowedRolesEl = new Element("allowedRoles");
                String roles = "";
                for (String r: v.getAllowedRoles()) {
                    roles = roles + "," + r;
                }
                allowedRolesEl.setAttr("roles", roles.substring(1));
                rootEl = XMLLightUtil.replaceElem(rootEl, allowedRolesEl);
            } 

            XMLLightUtil.writeXMLFile(backendDir + File.separator + "meta" + File.separator + v.getLibraryName() + File.separator + "version-" + v.getNumberMajor() + "." + v.getNumberMinor() + File.separator + "version-info.xml", rootEl);
            
        } catch (FileNotFoundException e) {
            logger.debug("Version metadata file not found", e);
            throw new VersionNotFoundException();
        } catch (IOException e) {
            logger.debug("Couln't read version metadata file", e);
            throw new BackendInternalErrorException("Couln't read library metadata file", e);
        } catch (XMLLightException e) {
            logger.debug("Errors parsing version metadata file", e);
            throw new BackendInternalErrorException("Errors parsing library metadata file", e);
        } 
    }
    
}
