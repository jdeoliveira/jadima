/*
 * RepositoryService.java
 *
 * Created on 13 de diciembre de 2004, 07:25 AM
 */

package ve.usb.jgm.ws.repo;

import org.apache.log4j.*;
import org.apache.log4j.xml.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import com.softcorporation.xmllight.*;
import org.apache.axis.*;
import org.apache.axis.transport.http.*;
import javax.servlet.http.*;
import ve.usb.jgm.repo.backend.*;
import ve.usb.jgm.repo.*;
import ve.usb.jgm.ws.faults.*;
import ve.usb.jgm.util.*;
import ve.usb.jgm.gui.*;
import ve.usb.jgm.client.*;
import java.lang.reflect.*;

//import java.awt.*;
//import java.awt.event.*;
//import javax.swing.*; 

/**
 * Variables de entorno requeridas para el funcionamiento
 * 
 */
public class RepositoryService extends Thread{

    /**
     * Logger (log4j)
     */
    private static final Logger logger = Logger.getLogger(RepositoryService.class);
    
    /**
     * Almacen de datos de este repositorio
     */
    private ve.usb.jgm.repo.backend.Backend myBackend;
       
    /**
     * Repositorios conocidos
     */    
    private static Collection<RepositoryClient> knownRepos;
    
    /**
     * Time for repositories
     */
    private static int time;
    
       
    /**
     * Creates a new instance of the Repository Web Service. This method is called 
     * once, especifically on the first service call made by a client. 
     * 
     * Takes the configuration from the axis' servlet init parameter "jgm-config-dir", 
     * if not present assumes <code>/etc</code> on Linux or <code>/WINNT</code> on 
     * Windows as default value.
     * 
     * Upon sucessfully read and parse of the configuration files, creates and
     * initializes the corresponding backend for this repository
     */
    public RepositoryService() {
        try {
            
            HttpServlet serv =  (HttpServlet)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLET);
            String configDir = serv.getServletContext().getInitParameter("jgm-config-dir");
            
            //Si no tenemos configDir, ponemos /etc para linux, c:/WINNT para >=win2k o c:/windows para win9x
            logger.debug("Os name is " + System.getProperty("os.name"));
            if ((configDir == null) || (configDir.equals(""))) {
                //TODO: Ver valores posibles para os.name
                
            }
            
            //Configurar log4j
            DOMConfigurator.configure(configDir + System.getProperty("file.separator") + "jgm-repository-log4j-config.xml");

            
            logger.info("Initializing Jgm Repository WebService");
            logger.debug("Os name is " + System.getProperty("os.name"));
            logger.debug("Configuration directory is " + configDir);
            String file = configDir.replace('/', System.getProperty("file.separator").toCharArray()[0]) + System.getProperty("file.separator") + "jgm-repository-config.xml";
            
            FileInputStream fs = null;
            byte[] buffer = null;
            try {
                //Cargar los datos sobre el backend
                fs = new FileInputStream(file);
                buffer = InputStreamUtil.readAll(fs);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("JGM Configuration file not found (" + file + " not exists). Please specify the location of the JGM configuration directory on the ");
            } catch (IOException e) {
                throw new RuntimeException("IOException caught while reading JGM Configuration file.", e);
            } finally {
                if (fs != null) try { fs.close(); } catch (IOException e) {}
            }
            
            String document = new String(buffer);
            
            Element config = XMLLight.getElem(document);
            
            //Buscamos la configuracion de los repositorios
            Element backend = config.getElem("backend");
            
            //Le pedimos al factory que instancie un repositorio
            myBackend = ve.usb.jgm.repo.backend.BackendFactory.makeBackend(backend);

            //Arrancamos este backend
            myBackend.init();
            
            //// AGREGUE ESTO ///////
            Element repositories = config.getElem("repositories");
            
            try {
                
                time = (new Integer(repositories.getAttr("time"))).intValue();

            } catch (NumberFormatException e) {
                logger.warn("invalid time attribute value. Check te configuration file");
                
            }
            
            knownRepos = ve.usb.jgm.gui.JdmRepository.knownRepositories(repositories); 
                    
            ////////////////////////
            
        } catch (XMLLightException e) {
            logger.error("XML parsing error", e);
            //throw new RuntimeException("Errors parsing the repository  " +
            //"configuration file.  Check the logs for the details. THE REPOSITORY SERVICE IS " +
            //"UNAVAILABLE.", e);
        }  catch (BackendInitializationException e) {
            //No se pudo configurar, loggeamos y pasamos al siguiente
            logger.error("Errors initializing backend", e);
            //throw new RuntimeException("Unable to initialize  " +
            //"repository backend.  Check the log for the details. THE REPOSITORY SERVICET IS " +
            //"UNAVAILABLE.", e);
        }  catch (BackendConfigurationException e) {
            //No se pudo configurar el backend 
            logger.error("Errors configuring backend", e);
            //throw new RuntimeException("Unable to initialize  " +
            //"repository backend.  Check the log for the details. THE REPOSITORY SERVICET IS " +
            //"UNAVAILABLE.", e);
        }
        logger.info("Jgm Repository WebService initialized sucessfully kkkkkkkkkkkkkkkkkk");
        
        for (RepositoryClient r: knownRepos){
                
                logger.info(r.getName());
            }
    }
    
    /**
     * Returns the classes definitions and meta-data requested as Bytecode objects, 
     * found on the underlying backend of this repository service. If no classes
     * are found, or if the currently authenticated user doesn't have the right role
     * associated, returns an empty array of Bytecode objects.
     * 
     * Returned Bytecode objects are guaranteed to contain the bytecode of the 
     * corresponding class, in it's last revision number.
     * @param requests Array of Bytecode objects containing the requested classes' metadata
     * @throws ve.usb.jgm.ws.faults.RepositoryInternalErrorFault If the request couln't be fullfilled because a repository internal error
     * @return Array of Bytecode objects with the requested classes found on this
     * repository, which the currently authenticated user is allowed to access
     */
    
    ClassFinder classFinder;
    //javax.swing.Timer timer;
    
    public Bytecode[] getClasses( Bytecode[] requests) 
        throws RepositoryInternalErrorFault  {
        
        //Vector nofoundClasses = new Vector();
        Collection<Bytecode> nofoundClasses = new HashSet<Bytecode>();
        
        if (logger.isInfoEnabled())
            logger.info("Begin request for classes " + Arrays.deepToString(requests));
        
        if (requests.length > 1) {
            logger.info("****************************************************************************** LOADING SEVERAL CLASESS ******************************************************************");
        }

        LinkedList<Bytecode> response = new LinkedList<Bytecode>();
        
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        for (Bytecode r: requests) {
            logger.debug("Searching " + r.getClassName());
            try {
                Bytecode bytecode = myBackend.getBytecode(r.getClassName(), r.getMajorVersion(), r.getMinorVersion());

                if (isUserInRole(request, bytecode.getAllowedRoles())) {
                    logger.info("Antes de agregar bytecode a response");
                    response.add(bytecode);
                    logger.info("Despues de agregar bytecode a response");
                } else {
                    logger.info("User not allowed to use this class");
                }
                
            } catch (LibraryNotFoundException e) {
                logger.info("Library not found in backend");
                nofoundClasses.add(r);
            } catch (VersionNotFoundException e) {
                logger.info("Version not found in backend");
                nofoundClasses.add(r);
            } catch (BytecodeNotFoundException e) {
                logger.info("Class not found in backend");
                nofoundClasses.add(r);
            } catch (BackendInternalErrorException e) {
                logger.warn("Backend is broken", e);
                throw new RepositoryInternalErrorFault("Backend is broken");
            } 
        }
        /*
         * LLAMO AL METODO DE CONEXION
         */
        
        //sustituir el metodo find clases por una clase hilo, terminarla despues de cierto tiempo y obtener la respuesta
        
        logger.info("AAAAAAAAQUIIIIIIIIIIIIIIIII"+ nofoundClasses.size());
        
        if (nofoundClasses.size() > 0){
            logger.info("-------------> to find the class!!!!!");
            //thread implementation
            
            classFinder = new ClassFinder();
            
            classFinder.startSearch(nofoundClasses, knownRepos);
            
            synchronized(classFinder){
                 try{
                    logger.info("-------------> im sleeping and waiting!!!!!");
                    classFinder.wait(time);
                    
                 } catch(InterruptedException e){

                }
            }    
            
            logger.info("-------------> im awake!!!!!");
            classFinder.stop = true;

            Collection<Bytecode> resp = classFinder.resp;

            if(resp != null){
                logger.info("-------------> i got the answer!!!!!" + resp.size());

                if (resp.size() > 0) {
                    for (Bytecode r: resp){
                        response.add(r);
                    }
                }
            }
            else{
                logger.info("-------------> the answer is null :(...!!!!");
            }         
           
            logger.info("-------------> finish search!!!!!");         
        }
        //Convertimos la respuesta para enviarla al cliente en el formato adecuado
        return response.toArray(new Bytecode[0]);
    }
    
   
    /*
     * AQUI HAGO LA CONEXION CON LOS REPOSITORIOS CONOCIDOS
     */       
   /* public Collection<Bytecode> findClasses(Collection<Bytecode> classes){
        
        Collection<Bytecode> resp = null;
        
        for (RepositoryClient c: knownRepos){
            
            try {
                resp = c.requestClasses(classes);
                if (resp.size() == 0) {
                    logger.debug("Not found, trying next repo");
                }
            } catch (Exception e) {
                logger.warn("Exception communicating with repository, trying next", e);
            }
        }
        
        return resp;
    }*/
    
    
    /**
     * Returns the reques
     * @param requests 
     * @throws ve.usb.jgm.ws.faults.RepositoryInternalErrorFault 
     * @return 
     */
    public ve.usb.jgm.repo.Version[] getStubs(
        ve.usb.jgm.repo.Version[] requests
    ) 
    throws RepositoryInternalErrorFault  {
        
        if (logger.isInfoEnabled())
            logger.info("Begin request for stubs " + Arrays.deepToString(requests));

        Set<ve.usb.jgm.repo.Version> response = new HashSet<ve.usb.jgm.repo.Version>();
        
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        for (ve.usb.jgm.repo.Version r: requests) {
            logger.debug("Searching " + r.getLibraryName() + "-" + r.getNumberMajor() + "." + r.getNumberMinor());
            try {                                                                                                                        //con stubs, sin javadoc 
                ve.usb.jgm.repo.Version version = myBackend.getVersion(r.getLibraryName(), r.getNumberMajor(), r.getNumberMinor(), true, false);

                //propagamos la prioridad que viene en el request
                version.setPriority(r.getPriority());
                logger.debug("Propagating library requested priority: " + version.getLibraryName() + " / " + version.getPriority());
                
                if (isUserInRole(request, version.getAllowedRoles())) {
                    version.setRevisions(null);
                    response.add(version);
                } else {
                    logger.info("User not allowed to use this version");
                }
                
            } catch (LibraryNotFoundException e) {
                logger.info("Library not found in backend");
            } catch (VersionNotFoundException e) {
                logger.info("Version not found in backend");
            } catch (BackendInternalErrorException e) {
                logger.warn("Backend is broken", e);
                throw new RepositoryInternalErrorFault("Backend is broken");
            }
        }

        //Convertimos la respuesta para enviarla al cliente en el formato adecuado
        return response.toArray(new ve.usb.jgm.repo.Version[0]);
    }
    
    /*
     * Publishes the library jar on this repository
     */
    /**
     * 
     * @param libName 
     * @param libDesc 
     * @param versionDesc 
     * @param majorVersion 
     * @param minorVersion 
     * @param revision 
     * @param jarBytecodeData 
     * @param jarStubData 
     * @param allowedRoles 
     * @throws ve.usb.jgm.ws.faults.LibraryAlreadyExistsFault 
     * @throws ve.usb.jgm.ws.faults.AccessDeniedFault 
     * @throws ve.usb.jgm.ws.faults.RepositoryInternalErrorFault 
     */
    public void publish(
        String libName, 
        String libDesc, 
        String versionDesc, 
        int majorVersion, 
        int minorVersion, 
        int revision, 
        byte[] jarBytecodeData, 
        byte[] jarStubData, 
        byte[] javadocZipData, 
        String[] allowedRoles
    ) 
    throws 
        LibraryAlreadyExistsFault, 
        AccessDeniedFault, 
        RepositoryInternalErrorFault 
    {

        logger.debug("Publishing: " + libName + " " + libDesc + " " + versionDesc + " " + majorVersion + " " + minorVersion + " " + revision);
        if (jarBytecodeData == null) logger.debug("jar data is null");
        if (jarStubData == null) logger.debug("jar stub data is null");
        
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        if (isUserInRole(request, myBackend.getPublishRoles())) {
            //Primero creamos la libreria
            logger.debug("Creating library");
            Library l = new Library();
            l.setName(libName);
            l.setDescription(libDesc);
            l.setAllowedRoles(allowedRoles);
            
            //Ahora creamos la version
            logger.debug("Creating version");
            ve.usb.jgm.repo.Version v = new ve.usb.jgm.repo.Version();
            l.addVersion(v);
            v.setNumberMajor(majorVersion);
            v.setNumberMinor(minorVersion);
            v.setStubsJar(jarStubData);
            v.setJavadocZip(javadocZipData);
            v.setAllowedRoles(allowedRoles);
            v.setDescription(versionDesc);
            
            //Creamos la revision
            logger.debug("Creating revision");
            Revision r = new Revision();
            v.addRevision(r);
            r.setRevisionNumber(revision);

            for (Bytecode b: processBytecodeJar(jarBytecodeData)) {
                r.addClass(b);
            }

            try {
                
                logger.debug("storing library on backend...");
                myBackend.storeLibrary(l);
                logger.debug("library sucessfully stored on backend");
                
            } catch (BackendInternalErrorException e) {
                logger.error("backend internal error storing new library", e);
                throw new RepositoryInternalErrorFault("backend internal error storing new library");
            } catch (LibraryAlreadyExistsException e) {
                logger.debug("Library already exists", e);
                throw new LibraryAlreadyExistsFault("Library already exists");
            }
            
        } else {
            //no esta autorizado para publicar
            throw new AccessDeniedFault("User not allowed to publish on this repository");
        }
    }
    
    /**
     * Stores a new revision of the library
     */
    public void update(
        String libName, 
        int majorVersion, 
        int minorVersion, 
        int revision, 
        byte[] jarData
    ) 
    throws 
        LibraryNotFoundFault,
        VersionNotFoundFault,
        RevisionAlreadyExistsFault,
        AccessDeniedFault,
        RepositoryInternalErrorFault
    {
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        if (isUserInRole(request, myBackend.getPublishRoles())) {

            //Creamos la revision
            Revision r = new Revision();
            r.setRevisionNumber(revision);
            
            for (Bytecode b: processBytecodeJar(jarData)) {
                r.addClass(b);
            }
            
            try {

                //Ya construimos el modelo de la solicitud, ahora la mandamos al 
                //backend para que la guarde en "almacenamiento persistente"
                logger.debug("storing new revision on backend");
                myBackend.storeRevision(libName, majorVersion, minorVersion, r);
                logger.debug("new revision stored sucessfully");
                
            } catch (BackendInternalErrorException e) {
                logger.error("backend internal error storing new revision", e);
                throw new RepositoryInternalErrorFault("backend internal error storing new revision");
            } catch (LibraryNotFoundException e) {
                logger.error("backend internal error storing new revision", e);
                throw new LibraryNotFoundFault("backend internal error storing new revision");
            } catch (VersionNotFoundException e) {
                logger.error("backend internal error storing new revision", e);
                throw new VersionNotFoundFault("backend internal error storing new revision");
            } catch (RevisionAlreadyExistsException e) {
                logger.error("backend internal error storing new revision", e);
                throw new RevisionAlreadyExistsFault("backend internal error storing new revision");
            } 
            
        } else {
            //no esta autorizado para publicar
            throw new AccessDeniedFault("User not allowed to publish on this repository");
        }
    }
    
    /**
     * Stores a new version of the library
     */
    public void upgrade(
        String libName, 
        String versionDesc,
        int majorVersion, 
        int minorVersion, 
        int revision, 
        byte[] bytecodeJarData, 
        byte[] stubJarData,
        byte[] javadocZipData, 
        String[] allowedRoles
    ) 
    throws 
        LibraryNotFoundFault,
        VersionAlreadyExistsFault,
        AccessDeniedFault,
        RepositoryInternalErrorFault
    {
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        if (isUserInRole(request, myBackend.getPublishRoles())) {

            //Ahora creamos la version
            ve.usb.jgm.repo.Version v = new ve.usb.jgm.repo.Version();
            v.setDescription(versionDesc);
            v.setNumberMajor(majorVersion);
            v.setNumberMinor(minorVersion);
            v.setStubsJar(stubJarData);
            v.setJavadocZip(javadocZipData);
            v.setAllowedRoles(allowedRoles);
            
            //Creamos la revision
            Revision r = new Revision();
            v.addRevision(r);
            r.setRevisionNumber(revision);
            
            for (Bytecode b: processBytecodeJar(bytecodeJarData)) {
                r.addClass(b);
            }
            
            try {
                //Ya construimos el modelo de la solicitud, ahora la mandamos al 
                //backend para que la guarde en "almacenamiento persistente"
                logger.debug("storing new version on backend");
                myBackend.storeVersion(libName, v);
                logger.debug("new version sucessfully stored");
            } catch (BackendInternalErrorException e) {
                logger.error("backend internal error storing new version", e);
                throw new RepositoryInternalErrorFault("backend internal error storing new version");
            } catch (LibraryNotFoundException e) {
                logger.error("backend internal error storing new revision", e);
                throw new LibraryNotFoundFault("backend internal error storing new revision");
            } catch (VersionAlreadyExistsException e) {
                logger.error("backend internal error storing new revision", e);
                throw new VersionAlreadyExistsFault("backend internal error storing new revision");
            }
            
        } else {
            //no esta autorizado para publicar
            throw new AccessDeniedFault("User not allowed to publish on this repository");
        }
    }
    
    public void updateLibraryMetadata(String libName, String newDescription, String[] newAllowedRoles) throws LibraryNotFoundFault, AccessDeniedFault, RepositoryInternalErrorFault  {
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        if (isUserInRole(request, myBackend.getAdminRoles())) {
            
            Library l = new Library();
            l.setName(libName);
            l.setDescription(newDescription);
            l.setAllowedRoles(newAllowedRoles);
            
            try {
                logger.debug("Updating library on backend");
                myBackend.updateLibrary(l);
                logger.debug("Library updated sucessfully");
            } catch (BackendInternalErrorException e) {
                logger.error("backend internal error updating existing library's metadata", e);
                throw new RepositoryInternalErrorFault("backend internal error updating existing library's metadata");
            } catch (LibraryNotFoundException e) {
                logger.error("Library not found in backend", e);
                throw new LibraryNotFoundFault("Library not found in backend");
            }
            
        } else {
            //no esta autorizado para publicar
            throw new AccessDeniedFault("User not allowed to administrate this repository");
        }
    }

    public void updateVersionMetadata(String libName, int majorVersion, int minorVersion, String newDescription, String[] newAllowedRoles) throws LibraryNotFoundFault, VersionNotFoundFault, AccessDeniedFault, RepositoryInternalErrorFault {
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        if (isUserInRole(request, myBackend.getAdminRoles())) {
            ve.usb.jgm.repo.Version v = new ve.usb.jgm.repo.Version();
            v.setLibraryName(libName);
            v.setNumberMajor(majorVersion);
            v.setNumberMinor(minorVersion);
            v.setDescription(newDescription);
            v.setAllowedRoles(newAllowedRoles);
            
            try {
                logger.debug("Updating version on backend");
                myBackend.updateVersion(v);
                logger.debug("Version updated sucessfully");
            } catch (BackendInternalErrorException e) {
                logger.error("backend internal error updating existing version's metadata", e);
                throw new RepositoryInternalErrorFault("backend internal error updating existing version's metadata");
            } catch (LibraryNotFoundException e) {
                logger.error("Library not found in backend", e);
                throw new LibraryNotFoundFault("Version not found in backend");
            } catch (VersionNotFoundException e) {
                logger.error("Version not found in backend", e);
                throw new VersionNotFoundFault("Version not found in backend");
            }
            
        } else {
            //no esta autorizado para administrar
            throw new AccessDeniedFault("User not allowed to administrate this repository");
        }
    }
    
    public void deleteVersion(String libName, int majorVersion, int minorVersion) throws LibraryNotFoundFault, VersionNotFoundFault, AccessDeniedFault, RepositoryInternalErrorFault {
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        if (isUserInRole(request, myBackend.getAdminRoles())) {
            try {
                logger.debug("Deleting version on backend");
                myBackend.deleteVersion(libName, majorVersion, minorVersion);
                logger.debug("Version deleted sucessfully");
            } catch (BackendInternalErrorException e) {
                logger.error("backend internal error deleting existing version", e);
                throw new RepositoryInternalErrorFault("backend internal error deleting existing version");
            } catch (LibraryNotFoundException e) {
                logger.error("Library not found in backend", e);
                throw new LibraryNotFoundFault("Version not found in backend");
            } catch (VersionNotFoundException e) {
                logger.error("Version not found in backend", e);
                throw new VersionNotFoundFault("Version not found in backend");
            }
            
        } else {
            //no esta autorizado para publicar
            throw new AccessDeniedFault("User not allowed to administrate this repository");
        }
    }
    
    public void deleteLibrary(String libName) throws LibraryNotFoundFault, AccessDeniedFault, RepositoryInternalErrorFault {
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        if (isUserInRole(request, myBackend.getAdminRoles())) {
            try {
                logger.debug("Deleting library on backend");
                myBackend.deleteLibrary(libName);
                logger.debug("Library deleted sucessfully");
            } catch (BackendInternalErrorException e) {
                logger.error("backend internal error deleting existing library", e);
                throw new RepositoryInternalErrorFault("backend internal error deleting existing library");
            } catch (LibraryNotFoundException e) {
                logger.error("Library not found in backend", e);
                throw new LibraryNotFoundFault("Version not found in backend");
            } 
            
        } else {
            //no esta autorizado para administrar
            throw new AccessDeniedFault("User not allowed to administrate this repository");
        }
    }

    private boolean isUserInRole(HttpServletRequest request, String[] allowedRoles) {
        logger.debug("Checking if authenticated user is on roles " + Arrays.deepToString(allowedRoles));
        
        for (String aRole: allowedRoles) {
            if ((aRole.equals("any")) || (request.isUserInRole(aRole))) {
                logger.debug("Yes, he is");
                return true;
            }
        }
        logger.debug("No, he is not");
        return false;
    }
    
    private Collection<Bytecode> processBytecodeJar(byte[] jarData) throws RepositoryInternalErrorFault {
        FileOutputStream bytecodeOut = null;
        ZipInputStream zipIn = null;
        try {
            HashSet<Bytecode> result = new HashSet<Bytecode>();

            //Almacenamos temporalmente el jar de bytecode
            File bytecode = File.createTempFile("jdm-bytecode", ".jar");
            bytecode.deleteOnExit();
            bytecodeOut = new FileOutputStream(bytecode);
            bytecodeOut.write(jarData);
            bytecodeOut.close();

            ZipFile zip = new ZipFile(bytecode);
            zipIn = new ZipInputStream(new FileInputStream(bytecode));

            byte[] buffer = null;
            byte[] longBuffer = null;
            for (Enumeration entries = zip.entries(); entries.hasMoreElements() ;) {
                ZipEntry entry = zipIn.getNextEntry();

                if (entry != null) {
                    if (entry.getName().endsWith(".class")) {
                        logger.debug("processing " + entry.getName());
                        String className = entry.getName().substring(0, entry.getName().lastIndexOf('.')).replace(File.separatorChar, '.').replace('/', '.');
                        logger.debug("translated name is " + className);
                        
                        int totalReadedBytes = 0;
                        int readedBytes = 0;
                        buffer = new byte[1024]; 
                        longBuffer = new byte[1048576];
                        
                        while ((readedBytes = zipIn.read(buffer)) != -1) {
                            
                            for (int j = 0; j < readedBytes; j++) {
                                longBuffer[j + totalReadedBytes] = buffer[j];
                            }   
                            totalReadedBytes += readedBytes;
                        }
                        
                        logger.debug("readed a total of " + totalReadedBytes);
                        
                        byte[] finalBytecode = new byte[totalReadedBytes];
                        for (int k = 0; k < totalReadedBytes; k++) {
                            finalBytecode[k] = longBuffer[k];
                        }
                        
                        Bytecode b = new Bytecode(
                            className,
                            finalBytecode
                        );
                        result.add(b);
                    }
                } else {
                    logger.debug("skipping null entry");
                    break;
                }

                zipIn.closeEntry();
            }
            zipIn.close();

            return result;
        } catch (IOException e) {
            logger.error("IO Exception while processing bytecode jar", e);
            throw new RepositoryInternalErrorFault("IO Exception while processing bytecode jar");
        } finally {
            try { bytecodeOut.close(); } catch (Exception e) {}
            try { zipIn.close(); } catch (Exception e) {}
        }
    }
    
    /**
     * crea el backend de acuerdo a las opciones especificadas en la configuracion
     */
    public void createBackend() throws RepositoryInternalErrorFault, AccessDeniedFault {
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        logger.info("Creating new backend");
        
        if (isUserInRole(request, myBackend.getAdminRoles())) {
            try {
                myBackend.create();
                myBackend.init();   
            } catch (BackendCreationException e) {
                logger.error("error creating backend", e);
                throw new RepositoryInternalErrorFault(e.getMessage());
            } catch (BackendInitializationException e) {
                logger.error("error initializing backend (after creation)", e);
                throw new RepositoryInternalErrorFault(e.getMessage());
            }
        } else {
            //no esta autorizado para publicar
            throw new AccessDeniedFault("User not allowed to administrate this repository");
        }
    }
    
    public void setRepositoryRoles(String[] newAdminRoles, String[] newPublishRoles) throws AccessDeniedFault, RepositoryInternalErrorFault {
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        if (isUserInRole(request, myBackend.getAdminRoles())) {
            try {
                if (newAdminRoles != null) {
                    myBackend.setAdminRoles(newAdminRoles);   
                }
                if (newPublishRoles != null) {
                    myBackend.setPublishRoles(newPublishRoles);
                }
            } catch (BackendInternalErrorException e) {
                logger.error("error setting new admin roles", e);
                throw new RepositoryInternalErrorFault(e.getMessage());
            }
        } else {
            //no esta autorizado para publicar
            throw new AccessDeniedFault("User not allowed to administrate this repository");
        }
    }
    
    public Library[] getAllLibraries() throws RepositoryInternalErrorFault {
        Set<ve.usb.jgm.repo.Library> response = new HashSet<ve.usb.jgm.repo.Library>();
        
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        try {
            Collection<Library> allLibs = myBackend.getAllLibraries();

            for (Library l: allLibs) {
                if (isUserInRole(request, l.getAllowedRoles())) {
                    response.add(l);
                } else {
                    logger.info("User not allowed to use this library");
                }
            }
            
            //Convertimos la respuesta para enviarla al cliente en el formato adecuado
            return response.toArray(new ve.usb.jgm.repo.Library[0]);
            
        } catch (BackendInternalErrorException e) {
            logger.error("backend error getting all libraries", e);
            throw new RepositoryInternalErrorFault();
        }
    }
    
    public Library getLibrary(String libName) throws RepositoryInternalErrorFault, LibraryNotFoundFault, AccessDeniedFault {

        logger.debug("Retrieving library " + libName + " from backend");
        
        Set<ve.usb.jgm.repo.Version> theFinalVers = new HashSet<ve.usb.jgm.repo.Version>();
        
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        try {
            Library lib = myBackend.getLibrary(libName);
            
            logger.debug("Library retrieved from backend, checking access roles");
            
            if (!(isUserInRole(request, lib.getAllowedRoles()))) {
                throw new AccessDeniedFault("User not allowed to use " + libName + " on this repository");
            }
            logger.debug("Library access role OK");
            
            
            for (ve.usb.jgm.repo.Version v: lib.getVersions()) {
                if (isUserInRole(request, v.getAllowedRoles())) {
                    logger.debug("Version " + v.getNumberMajor() + "." + v.getNumberMinor() + " access role OK");
                    theFinalVers.add(v);
                } else {
                    //deberia ser mas informativo aqui, esto va en el log del servidor
                    //igual con el de getAllLibraries
                    logger.info("User not allowed to use version " + v.getNumberMajor() + "." + v.getNumberMinor());
                }
            }
            
            lib.setVersions(theFinalVers.toArray(new ve.usb.jgm.repo.Version[0]));
            
            logger.debug("Library retrieved successfully");
            
            return lib;
            
        } catch (BackendInternalErrorException e) {
            logger.error("backend error getting library " + libName, e);
            throw new RepositoryInternalErrorFault();
        } catch (LibraryNotFoundException e) {
            logger.info("Requested library " + libName + " not found on backend");
            logger.debug("Requested library " + libName + " not found on backend", e);
            throw new LibraryNotFoundFault(e.getMessage());
        }
    }
    
    public ve.usb.jgm.repo.Version getVersion(String libName, int majorVersion, int minorVersion, boolean requestJavadocs) 
    throws RepositoryInternalErrorFault, LibraryNotFoundFault, VersionNotFoundFault, AccessDeniedFault 
    {
        
        //Seguridad: obtenemos el request para verificar el rol del usuario
        HttpServletRequest request =  (HttpServletRequest)MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        
        try {
            ve.usb.jgm.repo.Version ver = myBackend.getVersion(libName, majorVersion, minorVersion, false, requestJavadocs);
            
            if (!(isUserInRole(request, ver.getAllowedRoles()))) {
                throw new AccessDeniedFault("User not allowed to use version " + majorVersion + "." + minorVersion + " of " + libName + " on this repository");
            }
            
            return ver;
            
        } catch (BackendInternalErrorException e) {
            logger.error("backend error getting library " + libName, e);
            throw new RepositoryInternalErrorFault();
        } catch (LibraryNotFoundException e) {
            logger.info("Requested library " + libName + " not found on backend");
            logger.debug("Requested library " + libName + " not found on backend", e);
            throw new LibraryNotFoundFault(e.getMessage());
        } catch (VersionNotFoundException e) {
            logger.info("Requested version " + majorVersion + "." + minorVersion + " of library " + libName + " not found on backend");
            logger.debug("Requested version " + majorVersion + "." + minorVersion + " of library " + libName + " not found on backend", e);
            throw new VersionNotFoundFault(e.getMessage());
        }
    }
    
    public Revision dummyRevisionMethod() {
        //nothig to do, just to make axis generate Revision's serializer
        return null;
    }
}
