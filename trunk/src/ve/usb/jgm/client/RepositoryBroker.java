/*
 * RepositoryBroker.java
 *
 * Created on 17 de mayo de 2005, 08:53 PM
 */

package ve.usb.jgm.client;

import ve.usb.jgm.repo.*;
import java.util.*;
import org.apache.log4j.*;
import java.io.*;
/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class RepositoryBroker {
    
    private static final Logger logger = Logger.getLogger(RepositoryBroker.class);
    
    private static Collection<RepositoryClient> myRepos = RepositoryClientFactory.makeRepositoryClients();
    
    //Mis cache managers
    private static StubsCacheManager stubsCacheManager = CacheManagerFactory.makeStubsCacheManager();
    private static BytecodeCacheManager bytecodeCacheManager = CacheManagerFactory.makeBytecodeCacheManager();
    
    //Mi prefetching module
    private static PrefetchAgent prefetchAgent = PrefetchAgentFactory.makePrefetchAgent();
    
    public static byte[] requestClass(String className, int majorVersion, int minorVersion) throws ClassNotFoundException {
        
        //1. iniciar transaccion en el prefetch module
        prefetchAgent.beginTransaction(className, majorVersion, minorVersion);
        
        //2. pedir al prefetch module las clases relacionadas
        Collection<Bytecode> relatedClasses = prefetchAgent.getRelatedClasses(new Bytecode(className, majorVersion, minorVersion));
        
        //3. pedir al cache manager la clase solicitada y las clases relacionadas
        //(posible networked call) Si no se encuentra, se lanza un ClassNotFoundException
        byte[] mainClassBytes = bytecodeCacheManager.getClasses(new Bytecode(className, majorVersion, minorVersion), relatedClasses);
        
        prefetchAgent.endTransaction(className, majorVersion, minorVersion);
        //4. devolver la clase solicitada
        return mainClassBytes;
    }
    
    public static Collection<File> requestStubs(Collection<Version> libraryVersions) throws VersionNotFoundException {
        
        //pedir al cache manager los stubs de las librerias dadas
        //(posible networked call) Si no se encuentra, se lanza un ClassNotFoundException
        return stubsCacheManager.getStubs(libraryVersions);
    }
    
    public static void publish(
        String repoName,
        String libName,
        String libDesc,
        String versionDesc,
        int majorVersion,
        int minorVersion,
        int revision,
        byte[] bytecodeJarData,
        byte[] stubJarData,
        byte[] javadocZip,
        String[] allowedRoles
    ) throws 
        LibraryAlreadyExistsException, 
        AccessDeniedException,
        RepositoryClientCommunicationException
    {
        RepositoryClient theClient = findRepositoryClient(repoName);
        
        theClient.publish(
            libName,
            libDesc,
            versionDesc,
            majorVersion,
            minorVersion,
            revision,
            bytecodeJarData,
            stubJarData, 
            javadocZip, 
            allowedRoles
        );
        
    }
    
    public static void update(
        String repoName,
        String libName, 
        int majorVersion, 
        int minorVersion, 
        int revision, 
        byte[] bytecodeJarData
    ) 
    throws 
        RevisionAlreadyExistsException,
        LibraryNotFoundException,
        VersionNotFoundException,
        AccessDeniedException,
        RepositoryClientCommunicationException
    {
        RepositoryClient theClient = findRepositoryClient(repoName);
        
        theClient.update(
            libName,
            majorVersion,
            minorVersion,
            revision,
            bytecodeJarData
        );
    }
    
    
    public static void upgrade(
        String repoName,
        String libName,
        String versionDesc,
        int majorVersion, 
        int minorVersion, 
        int revision, 
        byte[] bytecodeJarData,
        byte[] stubJarData,
        byte[] javadocZip,
        String[] allowedRoles
    ) 
    throws 
        VersionAlreadyExistsException,
        LibraryNotFoundException,
        AccessDeniedException,
        RepositoryClientCommunicationException
    {
        RepositoryClient theClient = findRepositoryClient(repoName);
        
        theClient.upgrade(
            libName, 
            versionDesc, 
            majorVersion, 
            minorVersion, 
            revision, 
            bytecodeJarData, 
            stubJarData,
            javadocZip, 
            allowedRoles
        );
    }

    /**
     * Updates the metadata of the requested library. If newDescription == null, the description of
     * the library is not updated. If new allowed roles == null, the allowed roles of the
     * library are not updated
     */
    public static void updateLibraryMetadata(
        String repoName, 
        String libName, 
        String newDescription, 
        String[] newAllowedRoles
    ) 
    throws
        LibraryNotFoundException,
        AccessDeniedException,
        RepositoryClientCommunicationException
    {
        
        RepositoryClient theClient = findRepositoryClient(repoName);
        
        theClient.updateLibraryMetadata(
            libName, 
            newDescription, 
            newAllowedRoles
        );
    }
    
    /**
     * Updates the metadata of the requested version. If newDescription == null, the description of
     * the library is not updated. If new allowed roles == null, the allowed roles of the
     * library are not updated
     */
    public static void updateVersionMetadata(
        String repoName, 
        String libName, 
        int majorVersion,
        int minorVersion,
        String newDescription, 
        String[] newAllowedRoles
    ) 
    throws
        LibraryNotFoundException,
        VersionNotFoundException,
        AccessDeniedException,
        RepositoryClientCommunicationException
    {
        
        RepositoryClient theClient = findRepositoryClient(repoName);
        
        theClient.updateVersionMetadata(
            libName, 
            majorVersion,
            minorVersion,
            newDescription, 
            newAllowedRoles
        );
    }
    
    
    
    public static void deleteLibrary(
        String repoName, 
        String libName
    ) 
    throws
        LibraryNotFoundException,
        AccessDeniedException,
        RepositoryClientCommunicationException
    {
        
        RepositoryClient theClient = findRepositoryClient(repoName);
        
        theClient.deleteLibrary(
            libName
        );
    }
    
    /**
     * Updates the metadata of the requested version. If newDescription == null, the description of
     * the library is not updated. If new allowed roles == null, the allowed roles of the
     * library are not updated
     */
    public static void deleteVersion(
        String repoName, 
        String libName, 
        int majorVersion,
        int minorVersion
    ) 
    throws
        LibraryNotFoundException,
        VersionNotFoundException,
        AccessDeniedException,
        RepositoryClientCommunicationException
    {
        
        RepositoryClient theClient = findRepositoryClient(repoName);
        
        theClient.deleteVersion(
            libName, 
            majorVersion,
            minorVersion
        );
    }
    
    /**
     * Creates the specified repository backend subsystem
     */
    public static void createBackend(String repoName)
    throws
        AccessDeniedException,
        RepositoryClientCommunicationException
    {
        
        RepositoryClient theClient = findRepositoryClient(repoName);
       
        theClient.createBackend();
    }
    
    public static void setRepositoryRoles(String repoName, String[] newAdminRoles, String[] newPublishRoles)
    throws
        AccessDeniedException,
        RepositoryClientCommunicationException
    {
        
        RepositoryClient theClient = findRepositoryClient(repoName);
        
        theClient.setRepositoryRoles(newAdminRoles, newPublishRoles);
    }
    
    
    /**
     * returns all the libraries found on every configured repository
     */
    public static Collection<Library> getAllLibraries() {
        logger.info("Collecting all libraries on available repositories");
        
        Map<String, Library> bag = new HashMap<String, Library>();
        
        for (RepositoryClient c: myRepos) {

            logger.debug("Requesting all libraries to " + c.getName());
            
            try {
                Collection<Library> libs = getAllLibraries(c);
                
                //iteramos cada una a ver si ya esta agregada al bag
                for (Library aLib: libs) {
                    logger.debug("Received library " + aLib.getName());
                    if (bag.containsKey(aLib.getName())) {
                        //esta ya en el bag, tenemos que "mergearla"
                        logger.debug("Already in the bag, merging it");
                        
                        Library originalLib = bag.get(aLib.getName());
                        
                        //mezclamos la descripcion
                        originalLib.setDescription(originalLib.getDescription() + "\n\n" + aLib.getDescription() + " (from " + c.getName() + ")");
                        
                        //mezclamos las versiones
                        for (Version v: originalLib.getVersions()) {
                            for (Version vNew: aLib.getVersions()) {
                                if (vNew.equals(v)) {
                                    //tiene una version que ya esta en la anterior
                                    //le agregamos la descripcion de la nueva
                                    v.setDescription(v.getDescription() + "\n\n" + vNew.getDescription() + " (from " + c.getName() + ")");
                                } else {
                                    //tiene una version nueva, la agregamos
                                    vNew.setDescription(vNew.getDescription() + " (from " + c.getName() + ")");
                                    originalLib.addVersion(vNew);
                                }
                            }
                        }
                    } else {
                        //no esta, la agregamos al bag
                        logger.debug("It's new, adding it to the bag");
                        aLib.setDescription(aLib.getDescription() + " (from " + c.getName() + ")");
                        bag.put(aLib.getName(), aLib);
                        
                        for (Version v: aLib.getVersions()) {
                            v.setDescription(v.getDescription() + " (from " + c.getName() + ")");
                        }
                    }
                }
                
            } catch (RepositoryClientCommunicationException e) {
                logger.warn("Unable to communicate with remote repository " + c.getName());
                logger.debug("Unable to communicate with remote repository " + c.getName(), e);
            }
            
        }
        
        Collection<Library> all = new HashSet<Library>();
        for (Map.Entry<String, Library> e: bag.entrySet()) {
            all.add(e.getValue());
        }
        
        return all;
    }
    
    public static Collection<Library> getAllLibraries(String repoName) 
    throws 
        RepositoryClientCommunicationException
    {
        return getAllLibraries(findRepositoryClient(repoName));
    }
    
    
    private static Collection<Library> getAllLibraries(RepositoryClient c) 
    throws 
        RepositoryClientCommunicationException
    {
        return c.getAllLibraries();
    }
    
    
    /**
     * returns the specified library found on every configured repository
     */
    public static Library getLibrary(String libName) 
    throws LibraryNotFoundException
    {
        logger.info("Collecting all versions of " + libName + " on available repositories");
        
        Map<String, Version> bag = new HashMap<String, Version>();
        
        Library theFinalLib = new Library();
        theFinalLib.setName(libName);
        theFinalLib.setDescription("");
        
        boolean atLeastOne = false;
        
        for (RepositoryClient c: myRepos) {

            logger.debug("Requesting library to " + c.getName());
            
            try {
                Library theLib = getLibrary(c, libName);
                
                //si caigo aqui es porque esta llamada me retorno
                atLeastOne = true; 
                
                theFinalLib.setDescription(theFinalLib.getDescription() + "\n\n" + theLib.getDescription() + " (from " + c.getName() + ")");

                //iteramos cada una de las versiones a ver si ya esta agregada al bag
                for (Version v: theLib.getVersions()) {
                    
                    String versionNumber = v.getNumberMajor() + "." + v.getNumberMinor();
                    
                    logger.debug("Received version " + versionNumber);
                    
                    if (bag.containsKey(versionNumber)) {
                        
                        //esta ya en el bag, tenemos que "mergearla"
                        logger.debug("Already in the bag, merging it");
                        
                        Version originalVer = bag.get(versionNumber);
                        
                        //mezclamos la descripcion
                        originalVer.setDescription(originalVer.getDescription() + "\n\n" + v.getDescription() + " (from" + c.getName() + ")");
                        
                    } else {
                        //no esta, la agregamos al bag
                        logger.debug("It's new, adding it to the bag");
                        v.setDescription(v.getDescription() + " (from " + c.getName() + ")");
                        bag.put(versionNumber, v);
                    }
                }
                
            } catch (RepositoryClientCommunicationException e) {
                logger.warn("Unable to communicate with remote repository " + c.getName());
                logger.debug("Unable to communicate with remote repository " + c.getName(), e);
            } catch (AccessDeniedException e) {
                logger.warn("Access denied to library " + libName + " on repository " + c.getName());
            } catch (LibraryNotFoundException e) {
                logger.warn("Library " + libName + " not found on repository " + c.getName());
            }
        }
        
        if (!(atLeastOne)) {
            throw new LibraryNotFoundException();
        }
        
        Collection<Version> all = new HashSet<Version>();
        for (Map.Entry<String, Version> e: bag.entrySet()) {
            all.add(e.getValue());
        }
        theFinalLib.setVersions(all.toArray(new Version[0]));
        return theFinalLib;
    }
    
    public static Library getLibrary(String repoName, String libName) 
    throws 
        RepositoryClientCommunicationException,
        LibraryNotFoundException,
        AccessDeniedException
    {
        return getLibrary(findRepositoryClient(repoName), libName);
    }
    
    
    private static Library getLibrary(RepositoryClient c, String libName) 
    throws 
        RepositoryClientCommunicationException,
        LibraryNotFoundException,
        AccessDeniedException
    {
        return c.getLibrary(libName);
    }
    
    /**
     * returns the specified library found on every configured repository
     */
    public static Version getVersion(String libName, int majorVersion, int minorVersion, boolean requestJavadocs) 
    throws LibraryNotFoundException, VersionNotFoundException
    {
        logger.info("Collecting version " + majorVersion + "." + minorVersion + " of " + libName + " on available repositories");
        
        Version theFinalVer = new Version();
        theFinalVer.setLibraryName(libName);
        theFinalVer.setDescription("");
        theFinalVer.setLibraryDescription("");
        
        
        boolean atLeastOne = false;
        
        for (RepositoryClient c: myRepos) {

            logger.debug("Requesting version to " + c.getName());
            
            try {
                Version theVer = getVersion(c, libName, majorVersion, minorVersion, requestJavadocs);
                
                //si caigo aqui es porque esta llamada me retorno
                atLeastOne = true; 
                
                //mergeamos las descripciones
                theFinalVer.setDescription(theFinalVer.getDescription() + "\n\n" + theVer.getDescription() + " (from " + c.getName() + ")");
                theFinalVer.setLibraryDescription(theFinalVer.getLibraryDescription() + "\n\n" + theVer.getLibraryDescription() + " (from " + c.getName() + ")");
                theFinalVer.setJavadocZip(theVer.getJavadocZip());

            } catch (RepositoryClientCommunicationException e) {
                logger.warn("Unable to communicate with remote repository " + c.getName());
                logger.debug("Unable to communicate with remote repository " + c.getName(), e);
            } catch (AccessDeniedException e) {
                logger.warn("Access denied to library " + libName + " on repository " + c.getName());
            } catch (LibraryNotFoundException e) {
                logger.warn("Library " + libName + " not found on repository " + c.getName());
            } catch (VersionNotFoundException e) {
                logger.warn("Version " + majorVersion + "." + minorVersion + " of " + libName + " not found on repository " + c.getName());
            }
        }
        
        if (!(atLeastOne)) {
            throw new VersionNotFoundException();
        }
        
        return theFinalVer;
    }
    
    public static Version getVersion(String repoName, String libName, int majorVersion, int minorVersion, boolean requestJavadocs) 
    throws 
        RepositoryClientCommunicationException,
        LibraryNotFoundException,
        VersionNotFoundException,
        AccessDeniedException
    {
        return getVersion(findRepositoryClient(repoName), libName, majorVersion, minorVersion, requestJavadocs);
    }
    
    
    private static Version getVersion(RepositoryClient c, String libName, int majorVersion, int minorVersion, boolean requestJavadocs) 
    throws 
        RepositoryClientCommunicationException,
        LibraryNotFoundException,
        VersionNotFoundException,
        AccessDeniedException
    {
        return c.getVersion(libName, majorVersion, minorVersion, requestJavadocs);
    }
    
    
    
    
    
    private static RepositoryClient findRepositoryClient(String repoName) throws RepositoryClientCommunicationException {
        //Primero localizamos el repositorio que esta indicado
        RepositoryClient theClient = null;
        for (RepositoryClient c: myRepos) {
            if (c.getName().equals(repoName)) {
                theClient = c;
                break;
            }
        }
        if (theClient == null) {
            throw new RepositoryClientCommunicationException("Unknow repository '" + repoName + "'");
        } else {
            return theClient;
        }
    }
    
    
    
}
