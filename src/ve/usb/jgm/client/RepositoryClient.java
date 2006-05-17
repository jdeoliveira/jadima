/*
 * RepositoryClient.java
 *
 * Created on 17 de mayo de 2005, 11:17 PM
 */

package ve.usb.jgm.client;

import ve.usb.jgm.repo.*;
import com.softcorporation.xmllight.*;
import java.util.*;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public abstract class RepositoryClient {

    /**
     * Holds value of property name.
     */
    private String name;

    /**
     * Holds value of property priority.
     */
    private int priority;
    
    
    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public final String getName() {

        return this.name;
    }

    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public final void setName(String name) {

        this.name = name;
    }

    /**
     * Getter for property priority.
     * @return Value of property priority.
     */
    public final int getPriority() {

        return this.priority;
    }

    /**
     * Setter for property priority.
     * @param priority New value of property priority.
     */
    public final void setPriority(int priority) {

        this.priority = priority;
    }
    
    public abstract void configure(Element el) throws RepositoryClientConfigurationException;
    
    public abstract void init() throws RepositoryClientInitializationException;
    

    public abstract Collection<Bytecode> requestClasses(
        Collection<Bytecode> classes
    ) 
    throws 
        RepositoryClientCommunicationException;
    
    public abstract Collection<Version> requestStubs(
        Collection<Version> versions
    ) 
    throws 
        RepositoryClientCommunicationException;
    
    
    
    public abstract void publish(
        String libName,
        String libDesc,
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
        LibraryAlreadyExistsException, 
        AccessDeniedException,
        RepositoryClientCommunicationException;
    
    
    
    public abstract void update(
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
        RepositoryClientCommunicationException;
    
    
    public abstract void upgrade(
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
        VersionAlreadyExistsException,
        LibraryNotFoundException,
        AccessDeniedException,
        RepositoryClientCommunicationException;
     
    
    public abstract void updateVersionMetadata(
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
        RepositoryClientCommunicationException;
    
    
    public abstract void updateLibraryMetadata(
        String libName, 
        String newDescription, 
        String[] newAllowedRoles
    ) 
    throws
        LibraryNotFoundException,
        AccessDeniedException,
        RepositoryClientCommunicationException;

    
    public abstract void deleteVersion(
        String libName, 
        int majorVersion,
        int minorVersion
    ) 
    throws
        LibraryNotFoundException,
        VersionNotFoundException,
        AccessDeniedException,
        RepositoryClientCommunicationException;
    
    
    public abstract void deleteLibrary(
        String libName
    ) 
    throws
        LibraryNotFoundException,
        AccessDeniedException,
        RepositoryClientCommunicationException;
     
    
    public abstract void createBackend() 
    throws RepositoryClientCommunicationException, AccessDeniedException;
    
    public abstract void setRepositoryRoles(String[] newAdminRoles, String[] newPublishRoles) 
    throws RepositoryClientCommunicationException, AccessDeniedException;

    public abstract Collection<Library> getAllLibraries()
    throws RepositoryClientCommunicationException;
    
    public abstract Library getLibrary(String libName)
    throws RepositoryClientCommunicationException,LibraryNotFoundException,AccessDeniedException;
    
    public abstract Version getVersion(String libName, int majorVersion, int minorVersion, boolean requestJavadocs)
    throws RepositoryClientCommunicationException, LibraryNotFoundException, VersionNotFoundException, AccessDeniedException;
    
}
