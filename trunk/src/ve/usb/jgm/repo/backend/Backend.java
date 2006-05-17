/*
 * Repository.java
 *
 * Created on 18 de junio de 2002, 12:27 AM
 */

package ve.usb.jgm.repo.backend;

import com.softcorporation.xmllight.Element;
import ve.usb.jgm.repo.backend.*;
import ve.usb.jgm.repo.*;
import java.util.*;


/**
 *
 * @author  Jesus
 */
public interface Backend {
    
    /**
     * Inicializa este backend para comenzar a ser utilizado 
     * (p.e. conectar con la BD)
     * @throws ve.usb.jgm.repo.exceptions.BackendInitializationException Si por alguna razon no es posible inicializar este backend
     * (p.e. no se puede conectar con la BD). Esta excepcion
     * envuelve la causa raiz del problema de inicializacion.
     */
    void init() throws BackendInitializationException;
    
    /**
     * Inicializa este backend para comenzar a ser utilizado, es decir, 
     * crea las estructuras necesarias para su funcionamiento 
     * (p.e. crear tablas o estructura de directorios)
     * @throws ve.usb.jgm.repo.exceptions.BackendCreationException Si por alguna razon no es posible crear las estructuras
     * necesarias para el funcionamiento de este backend. Envuelve
     * la causa raiz del problema.
     */
    void create() throws BackendCreationException;
    
    /**
     * Configura este repositorio con los valores contenidos en el elemento XML
     * pasado como parametro
     * @param config XML Element con los parametros de configuracion particulares 
     * de este backend
     * @throws BackendConfigurationException Si el elemento XML con la configuracion es invalido (p.e.
     * necesita algun parametro o tiene errores de sintaxis)
     */
    void configure(Element config) throws BackendConfigurationException;
    
    /**
     * Devuelve el bytecode de la clase solicitada, con la version solicitada
     * @param classname FQN de la clase solicitada
     * @param majorVersion numero de version mayor
     * @param minorVersion numero de version menor
     * @return arreglo de bytes con el bytecode de la clase solicitada
     * @throws BytecodeNotFoundException Si la clase en la version solicitada no existe en este backend
     */
    Bytecode getBytecode(String classname, int majorVersion, int minorVersion) throws LibraryNotFoundException, VersionNotFoundException, BytecodeNotFoundException, BackendInternalErrorException;

    /**
     * used to obtain stub jar when compiling
     */
    Version getVersion(String libraryName, int majorVersion, int minorVersion, boolean deliverStubJar, boolean deliverJavadocZip) throws LibraryNotFoundException, VersionNotFoundException, BackendInternalErrorException;
    
    /**
     * This method is used to list the librarys on some repository
     *
     */
    Library getLibrary(String libraryName) throws LibraryNotFoundException, BackendInternalErrorException;

    Collection<Library> getAllLibraries() throws BackendInternalErrorException;
    
    void storeLibrary(Library lib) throws LibraryAlreadyExistsException, BackendInternalErrorException;
    
    void storeVersion(String libraryName, Version version) throws VersionAlreadyExistsException, LibraryNotFoundException, BackendInternalErrorException;
    
    void storeRevision(String libraryName, int majorVersion, int minorVersion, Revision r) throws RevisionAlreadyExistsException, VersionNotFoundException, LibraryNotFoundException, BackendInternalErrorException;
    
    void deleteLibrary(String libraryName) throws LibraryNotFoundException, BackendInternalErrorException;
    
    void deleteVersion(String libraryName, int majorVersion, int minorVersion) throws LibraryNotFoundException, VersionNotFoundException, BackendInternalErrorException;

    //Es necesario??
    void deleteRevision(String libraryName, int majorVersion, int minorVersion, int revisionNumber) throws LibraryNotFoundException, VersionNotFoundException, RevisionNotFoundException, BackendInternalErrorException;
    
    void updateLibrary(Library lib) throws LibraryNotFoundException, BackendInternalErrorException;
    
    void updateVersion(Version v) throws LibraryNotFoundException, VersionNotFoundException, BackendInternalErrorException;
    
    /**
     * Devuelve los roles de administradores de este backend
     */
    String[] getAdminRoles();
    
    void setAdminRoles(String[] newRoles) throws BackendInternalErrorException;
    
    /**
     * Devuelve los roles de administradores de este backend
     */
    String[] getPublishRoles();
    
    void setPublishRoles(String[] newRoles) throws BackendInternalErrorException;
}
