/*
 * RepositoryClient.java
 *
 * Created on 13 de diciembre de 2004, 07:57 AM
 */

package ve.usb.jgm.ws.client;

import java.util.*;
import ve.usb.jgm.client.config.*;
import ve.usb.jgm.repo.*;
import ve.usb.jgm.client.*;
import com.softcorporation.xmllight.*;
import org.apache.log4j.*;
import org.apache.axis.*;
import org.apache.axis.client.*;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import javax.xml.namespace.*;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import ve.usb.jgm.ws.faults.*;
import ve.usb.jgm.ws.*;

/**
 *
 * @author  Administrator
 */
public class InsecureRepositoryServiceClient extends RepositoryClient {
    
    private static Logger logger = Logger.getLogger(InsecureRepositoryServiceClient.class);
    
    private String url;
    
    
    public InsecureRepositoryServiceClient() {
        //nothing to do here
    }
    
    public void configure(Element el) throws RepositoryClientConfigurationException {
        try {
            Element urlEl = el.getElem("url");
            url = urlEl.getAttr("value");
        } catch (XMLLightException e) {
            logger.warn("Errors parsing repository configuration data", e);
            throw new RepositoryClientConfigurationException("Errors parsing repository configuration data", e);
        }
    }
    
    public void init() {
        //nothing to initialize here
    }
    
    
    public Collection<Bytecode> requestClasses(Collection<Bytecode> classes) throws RepositoryClientCommunicationException {

        if (!(classes.isEmpty())) {
            PrefetchCacheProfile.instance().addRepoAccess(classes.toString() + "\n");
        }
        
        logger.debug("I'm " + getName() + ", requesting " + classes);
        
        try {

            Service service = new Service();
            Call call = (Call)service.createCall();
                
                
            //Registramos los (de)serializadores de nuestros arreglos de requests
            //y responses
            QName qn1 = new QName("ns:RepositoryServiceNS", "urn:Bytecode");
            call.registerTypeMapping(Bytecode.class, qn1,
            new org.apache.axis.encoding.ser.BeanSerializerFactory(Bytecode.class, qn1),
            new org.apache.axis.encoding.ser.BeanDeserializerFactory(Bytecode.class, qn1));

                
            QName qnA = new QName("urn:ArrayOfBytecode", "ArrayOfBytecode");
            call.registerTypeMapping(Bytecode[].class, qnA,
            new org.apache.axis.encoding.ser.ArraySerializerFactory(Bytecode[].class, qnA),
            new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
                
            //Ajustamos los parametros de la llamada
            call.setTargetEndpointAddress( new java.net.URL(url));
            call.setOperationName(new QName("JgmService", "getClasses"));
            call.addParameter( "arg0", qnA , ParameterMode.IN);
            call.setReturnType(qnA, Bytecode[].class);
                
            //Efectuamos la llamada
            Bytecode[] ret = (Bytecode[]) call.invoke("getClasses",
                new Object[] { 
                    (Bytecode[])classes.toArray(new Bytecode[0]) 
            } );

            logger.debug("Remote repository returned " + Arrays.deepToString(ret));
                
            HashSet<Bytecode> result = new HashSet<Bytecode>();
            Collections.addAll(result, ret);
            return result;
                
        } catch (MalformedURLException e) {
            logger.warn("Repository url is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (AxisFault e) {
            logger.warn("Axis fault detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Axis fault detected communicating with remote repository", e);
        } catch (ServiceException e) {
            logger.warn("Service exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Service exception detected communicating with remote repository", e);
        }
    }
    
    public void publish(
        String libName,
        String libDesc,
        String versionDesc,
        int majorVersion,
        int minorVersion,
        int revision,
        byte[] jarData,
        byte[] jarStubData,
        byte[] javadocZip,
        String[] allowedRoles
    ) throws 
        LibraryAlreadyExistsException, 
        AccessDeniedException,
        RepositoryClientCommunicationException
    {
        try {
            if (jarData == null) logger.debug("jar data is null");
            if (jarStubData == null) logger.debug("jar stub data is null");
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            s.publish(
                    libName,
                    libDesc,
                    versionDesc,
                    majorVersion,
                    minorVersion,
                    revision,
                    jarData,
                    jarStubData,
                    javadocZip,
                    allowedRoles
            );
            
            
            logger.debug("Call returned");
            
        } catch (LibraryAlreadyExistsFault e) {
            logger.debug("The library already exists on this repository", e);
            throw new LibraryAlreadyExistsException(e.getMessage());
        } catch (AccessDeniedFault e) {
            logger.debug("Access denied to publish on this repository", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (MalformedURLException e) {
            logger.debug("Supplied repository url " + url + " is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (RemoteException e) {
            logger.debug("Remote exception caught", e);
            throw new RepositoryClientCommunicationException("Remote exception caught", e);
        } catch (ServiceException e) {
            logger.debug("Service exception caught", e);
            throw new RepositoryClientCommunicationException("Service exception caught", e);
        }
    }
    
    public void update(
        String libName, 
        int majorVersion, 
        int minorVersion, 
        int revision, 
        byte[] jarData
    ) 
    throws 
        RevisionAlreadyExistsException,
        LibraryNotFoundException,
        VersionNotFoundException,
        AccessDeniedException,
        RepositoryClientCommunicationException
    {
        try {
            logger.debug("Updating: " + libName + " " + majorVersion + " " + minorVersion + " " + revision);
            if (jarData == null) logger.debug("jar data is null");
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            s.update(
                    libName, 
                    majorVersion,
                    minorVersion,
                    revision,
                    jarData
            );
            
            
            logger.debug("Call returned");
            
        } catch (LibraryNotFoundFault e) {
            logger.debug("The library " + libName + " doesn't exists on this repository", e);
            throw new LibraryNotFoundException(e.getMessage());
        } catch (VersionNotFoundFault e) {
            logger.debug("The version " + majorVersion + "." + minorVersion + " of " + libName + " doesn't exists on this repository", e);
            throw new VersionNotFoundException(e.getMessage());
        } catch (RevisionAlreadyExistsFault e) {
            logger.debug("The revision " + revision + " of " + libName + " " + majorVersion + "." + minorVersion + " doesn't exists on this repository", e);
            throw new RevisionAlreadyExistsException(e.getMessage());
        } catch (AccessDeniedFault e) {
            logger.debug("Access denied to publish on this repository", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (MalformedURLException e) {
            logger.debug("Supplied repository url " + url + " is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (RemoteException e) {
            logger.debug("Remote exception caught", e);
            throw new RepositoryClientCommunicationException("Remote exception caught", e);
        } catch (ServiceException e) {
            logger.debug("Service exception caught", e);
            throw new RepositoryClientCommunicationException("Service exception caught", e);
        }
    }
    
    public void upgrade(
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
        try {
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            s.upgrade(
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
            
            logger.debug("Call returned");
            
        } catch (LibraryNotFoundFault e) {
            logger.debug("The library " + libName + " doesn't exists on this repository", e);
            throw new LibraryNotFoundException(e.getMessage());
        } catch (VersionAlreadyExistsFault e) {
            logger.debug("The version " + majorVersion + "." + minorVersion + " of " + libName + " already exists on this repository", e);
            throw new VersionAlreadyExistsException(e.getMessage());
        } catch (AccessDeniedFault e) {
            logger.debug("Access denied to publish on this repository", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (MalformedURLException e) {
            logger.debug("Supplied repository url " + url + " is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (RemoteException e) {
            logger.debug("Remote exception caught", e);
            throw new RepositoryClientCommunicationException("Remote exception caught", e);
        } catch (ServiceException e) {
            logger.debug("Service exception caught", e);
            throw new RepositoryClientCommunicationException("Service exception caught", e);
        }
    }
    

    public Collection<ve.usb.jgm.repo.Version> requestStubs(Collection<ve.usb.jgm.repo.Version> versions) throws RepositoryClientCommunicationException {
        try {
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            HashSet<ve.usb.jgm.repo.Version> result = new HashSet<ve.usb.jgm.repo.Version>();
            
            result.addAll(Arrays.asList(s.getStubs(versions.toArray(new ve.usb.jgm.repo.Version[0]))));

            logger.debug("Call returned");
            
            return result;
            
        } catch (MalformedURLException e) {
            logger.warn("Repository url is invalid");
            logger.debug("Repository url is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (AxisFault e) {
            logger.warn("Axis fault detected communicating with remote repository");
            logger.debug("Axis fault detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Axis fault detected communicating with remote repository", e);
        } catch (ServiceException e) {
            logger.warn("Service exception detected communicating with remote repository");
            logger.debug("Service exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Service exception detected communicating with remote repository", e);
        } catch (RemoteException e) {
            logger.warn("Remote exception detected communicating with remote repository");
            logger.debug("Remote exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Remote exception detected communicating with remote repository", e);
        }
    }    

    public void updateLibraryMetadata(String libName, String newDescription, String[] newAllowedRoles) throws LibraryNotFoundException, AccessDeniedException, RepositoryClientCommunicationException {
        try {
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            s.updateLibraryMetadata(libName, newDescription, newAllowedRoles);

            logger.debug("Call returned");
              
        } catch (MalformedURLException e) {
            logger.warn("Repository url is invalid");
            logger.debug("Repository url is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (LibraryNotFoundFault e) {
            logger.debug("The library " + libName + " doesn't exists on this repository", e);
            throw new LibraryNotFoundException(e.getMessage());
        } catch (AccessDeniedFault e) {
            logger.debug("Access denied to administer this repository", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (AxisFault e) {
            logger.warn("Axis fault detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Axis fault detected communicating with remote repository", e);
        } catch (ServiceException e) {
            logger.warn("Service exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Service exception detected communicating with remote repository", e);
        } catch (RemoteException e) {
            logger.warn("Remote exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Remote exception detected communicating with remote repository", e);
        }
    }

    public void updateVersionMetadata(String libName, int majorVersion, int minorVersion, String newDescription, String[] newAllowedRoles) throws LibraryNotFoundException, VersionNotFoundException, AccessDeniedException, RepositoryClientCommunicationException {
        try {
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            s.updateVersionMetadata(libName, majorVersion, minorVersion, newDescription, newAllowedRoles);

            logger.debug("Call returned");
            
        } catch (MalformedURLException e) {
            logger.warn("Repository url is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (LibraryNotFoundFault e) {
            logger.debug("The library " + libName + " doesn't exists on this repository", e);
            throw new LibraryNotFoundException(e.getMessage());
        } catch (VersionNotFoundFault e) {
            logger.debug("The version " + majorVersion + "." + minorVersion + " of " + libName + " doesn't exists on this repository", e);
            throw new VersionNotFoundException(e.getMessage());
        } catch (AccessDeniedFault e) {
            logger.debug("Access denied to publish on this repository", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (AxisFault e) {
            logger.warn("Axis fault detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Axis fault detected communicating with remote repository", e);
        } catch (ServiceException e) {
            logger.warn("Service exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Service exception detected communicating with remote repository", e);
        } catch (RemoteException e) {
            logger.warn("Remote exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Remote exception detected communicating with remote repository", e);
        }
    }
    
    public void deleteVersion(String libName, int majorVersion, int minorVersion) throws LibraryNotFoundException, VersionNotFoundException, AccessDeniedException, RepositoryClientCommunicationException {
        try {
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            s.deleteVersion(libName, majorVersion, minorVersion);

            logger.debug("Call returned");
            
        } catch (MalformedURLException e) {
            logger.warn("Repository url is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (LibraryNotFoundFault e) {
            logger.debug("The library " + libName + " doesn't exists on this repository", e);
            throw new LibraryNotFoundException(e.getMessage());
        } catch (VersionNotFoundFault e) {
            logger.debug("The version " + majorVersion + "." + minorVersion + " of " + libName + " doesn't exists on this repository", e);
            throw new VersionNotFoundException(e.getMessage());
        } catch (AccessDeniedFault e) {
            logger.debug("Access denied to publish on this repository", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (AxisFault e) {
            logger.warn("Axis fault detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Axis fault detected communicating with remote repository", e);
        } catch (ServiceException e) {
            logger.warn("Service exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Service exception detected communicating with remote repository", e);
        } catch (RemoteException e) {
            logger.warn("Remote exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Remote exception detected communicating with remote repository", e);
        }
    }
    
    public void deleteLibrary(String libName) throws LibraryNotFoundException, AccessDeniedException, RepositoryClientCommunicationException {
        try {
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            s.deleteLibrary(libName);

            logger.debug("Call returned");
              
        } catch (MalformedURLException e) {
            logger.warn("Repository url is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (LibraryNotFoundFault e) {
            logger.debug("The library " + libName + " doesn't exists on this repository", e);
            throw new LibraryNotFoundException(e.getMessage());
        } catch (AccessDeniedFault e) {
            logger.debug("Access denied to administer this repository", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (AxisFault e) {
            logger.warn("Axis fault detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Axis fault detected communicating with remote repository", e);
        } catch (ServiceException e) {
            logger.warn("Service exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Service exception detected communicating with remote repository", e);
        } catch (RemoteException e) {
            logger.warn("Remote exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Remote exception detected communicating with remote repository", e);
        }
    }
    
    public void createBackend() throws RepositoryClientCommunicationException, AccessDeniedException {
        try {
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            s.createBackend();

            logger.debug("Call returned");
            
        } catch (MalformedURLException e) {
            logger.warn("Repository url is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (AccessDeniedFault e) {
            logger.debug("Access denied to administer this repository", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (AxisFault e) {
            logger.warn("Axis fault detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Axis fault detected communicating with remote repository", e);
        } catch (ServiceException e) {
            logger.warn("Service exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Service exception detected communicating with remote repository", e);
        } catch (RemoteException e) {
            logger.warn("Remote exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Remote exception detected communicating with remote repository", e);
        }
    }
    
    public void setRepositoryRoles(String[] newAdminRoles, String[] newPublishRoles) throws RepositoryClientCommunicationException, AccessDeniedException {
        try {
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            s.setRepositoryRoles(newAdminRoles, newPublishRoles);

            logger.debug("Call returned");
            
        } catch (MalformedURLException e) {
            logger.warn("Repository url is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (AccessDeniedFault e) {
            logger.debug("Access denied to administer this repository", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (AxisFault e) {
            logger.warn("Axis fault detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Axis fault detected communicating with remote repository", e);
        } catch (ServiceException e) {
            logger.warn("Service exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Service exception detected communicating with remote repository", e);
        } catch (RemoteException e) {
            logger.warn("Remote exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Remote exception detected communicating with remote repository", e);
        }
    }

    public Collection<Library> getAllLibraries() throws RepositoryClientCommunicationException {
        try {
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            Library[] libs = s.getAllLibraries();

            logger.debug("Call returned");
            
            return Arrays.asList(libs);
            
        } catch (MalformedURLException e) {
            logger.warn("Repository url is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (AxisFault e) {
            logger.debug("Axis fault detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Axis fault detected communicating with remote repository", e);
        } catch (ServiceException e) {
            logger.warn("Service exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Service exception detected communicating with remote repository", e);
        } catch (RemoteException e) {
            logger.warn("Remote exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Remote exception detected communicating with remote repository", e);
        }
    }

    public Library getLibrary(String libName) throws RepositoryClientCommunicationException, LibraryNotFoundException, AccessDeniedException {
        try {
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            Library lib = s.getLibrary(libName);

            logger.debug("Call returned");
            
            return lib;
            
        } catch (MalformedURLException e) {
            logger.warn("Repository url is invalid");
            logger.debug("Repository url is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (LibraryNotFoundFault e) {
            logger.debug("The library " + libName + " doesn't exists on this repository", e);
            throw new LibraryNotFoundException(e.getMessage());
        } catch (AccessDeniedFault e) {
            logger.debug("Access denied to library " + libName + " on this repository", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (AxisFault e) {
            logger.warn("Axis fault detected communicating with remote repository");
            logger.debug("Axis fault detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Axis fault detected communicating with remote repository", e);
        } catch (ServiceException e) {
            logger.warn("Service exception detected communicating with remote repository");
            logger.debug("Service exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Service exception detected communicating with remote repository", e);
        } catch (RemoteException e) {
            logger.warn("Remote exception detected communicating with remote repository");
            logger.debug("Remote exception detected communicating with remote repositorycp ", e);
            throw new RepositoryClientCommunicationException("Remote exception detected communicating with remote repository", e);
        }
    }

    public ve.usb.jgm.repo.Version getVersion(String libName, int majorVersion, int minorVersion, boolean requestJavadocs) throws RepositoryClientCommunicationException, LibraryNotFoundException, VersionNotFoundException, AccessDeniedException {
        try {
            
            RepositoryServiceServiceLocator l = new RepositoryServiceServiceLocator();
            RepositoryService s = l.getJgmService(new java.net.URL(url));
            
            ve.usb.jgm.repo.Version v = s.getVersion(libName, majorVersion, minorVersion, requestJavadocs);

            logger.debug("Call returned");
            
            return v;
            
        } catch (MalformedURLException e) {
            logger.warn("Repository url is invalid");
            logger.debug("Repository url is invalid", e);
            throw new RepositoryClientCommunicationException("Supplied repository url " + url + " is invalid", e);
        } catch (RepositoryInternalErrorFault e) {
            logger.debug("Repository internal error detected", e);
            throw new RepositoryClientCommunicationException(e.getMessage());
        } catch (LibraryNotFoundFault e) {
            logger.debug("The library " + libName + " doesn't exists on this repository", e);
            throw new LibraryNotFoundException(e.getMessage());
        } catch (AccessDeniedFault e) {
            logger.debug("Access denied to library " + libName + " on this repository", e);
            throw new AccessDeniedException(e.getMessage());
        } catch (AxisFault e) {
            logger.warn("Axis fault detected communicating with remote repository");
            logger.debug("Axis fault detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Axis fault detected communicating with remote repository", e);
        } catch (ServiceException e) {
            logger.warn("Service exception detected communicating with remote repository");
            logger.debug("Service exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Service exception detected communicating with remote repository", e);
        } catch (RemoteException e) {
            logger.warn("Remote exception detected communicating with remote repository");
            logger.debug("Remote exception detected communicating with remote repository", e);
            throw new RepositoryClientCommunicationException("Remote exception detected communicating with remote repository", e);
        }
    }
    
}
