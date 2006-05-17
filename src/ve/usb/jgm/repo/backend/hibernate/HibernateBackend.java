/*
 * HibernateBackend.java
 *
 * Created on 7 de junio de 2005, 14:38
 */

package ve.usb.jgm.repo.backend.hibernate;

import ve.usb.jgm.repo.backend.*;
import com.softcorporation.xmllight.*;
import ve.usb.jgm.repo.*;
import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.exception.*;
import org.hibernate.tool.hbm2ddl.*;
import org.apache.log4j.*;
import java.util.*;


/**
 *
 * @author  jdeoliveira
 */
public class HibernateBackend implements Backend {
    
    private final static Logger logger = Logger.getLogger(HibernateBackend.class);
    
    private String[] adminRoles;
    private String[] publishRoles;
    
    private Configuration cfg;
    private static SessionFactory sessionFactory;
    private static final ThreadLocal session = new ThreadLocal();
    
    public HibernateBackend() {
        //no-argument constructor
        adminRoles = new String[0];
        publishRoles = new String[0];
    }
    
    public void configure(com.softcorporation.xmllight.Element config) throws BackendConfigurationException {
        try {
            logger.debug("Starting Hibernate Backend configuration process");
            //TODO: pasar los parametors directo desde el XML,
            //algo como <property name="..." value="...."/>
            cfg = new Configuration()
                .addClass(HbLibrary.class)
                .addClass(HbVersion.class)
                .addClass(HbRevision.class)
                .addClass(HbBytecode.class)
                .addClass(HbBackendRole.class);
                
            Element prop = config.getElem("property");
            while (!(prop.isNull())) {
                logger.debug("Setting " + prop.getAttr("name") + "=" + prop.getAttr("value"));
                cfg.setProperty(prop.getAttr("name"), prop.getAttr("value"));
                prop = config.getElem("property");
            }

            logger.debug("Hibernate Backend configured successfully");
            
        } catch (XMLLightException e) {
            logger.error("XML parsing error", e);
            throw new BackendConfigurationException("XML parsing error");
        }
    }

    public void create() throws BackendCreationException {
        try {
            logger.info("Creating Hibernate backend database schema");
            new SchemaExport(cfg).create(false, true);
            logger.info("Hibernate backend database schema successfully created");
        } catch (Throwable t) {
            logger.error("Unable to create database schema", t);
            throw new BackendCreationException("Unable to create database schema", t);
        }
            
        Session session = currentSession();
        Transaction tx = session.beginTransaction();
        try {
            
            //Add the default admin role (any)
            HbBackendRole r = new HbBackendRole();
            r.setOperation(HbBackendRole.OP_ADMIN);
            r.setRoleName("any");
                
            session.save(r);
            tx.commit();
            
            adminRoles = new String[] { "any" };

            logger.warn("BACKEND ADMIN ROLE SET TO any PLEASE SET THE CORRECT SECURITY ROLES FOR THIS BACKEND");
        
        } catch (Exception e) {
            tx.rollback();
            logger.error("Unable to set admin role to any, please modify the backend roles manually", e);
        } finally {
            closeSession();
        }
    }

    public void deleteLibrary(String libraryName) throws LibraryNotFoundException, BackendInternalErrorException {
        Session session = currentSession();
        Transaction tx = session.beginTransaction();
        try {
            
            logger.debug("Deleting library " + libraryName);
            
            Object o = session.createQuery(
                "select l from HbLibrary as l where l.name = ?")
                .setString(0, libraryName)
                .uniqueResult();
            if (o == null) throw new LibraryNotFoundException();
            HbLibrary library = (HbLibrary)o; 
            
            session.delete(library);
            tx.commit();
            
            logger.debug("Library " + libraryName + " successfully deleted");
            
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught deleting library " + libraryName, e);
            tx.rollback();
            throw new BackendInternalErrorException("Hibernate exception caught deleting library " + libraryName, e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public void deleteRevision(String libraryName, int majorVersion, int minorVersion, int revisionNumber) throws LibraryNotFoundException, VersionNotFoundException, RevisionNotFoundException, BackendInternalErrorException {
        Session session = currentSession();
        Transaction tx = session.beginTransaction();
        try {
            
            logger.debug("Deleting revision " + libraryName + "-" + majorVersion + "." + minorVersion + "." + revisionNumber);
            
            Object o = session.createQuery(
                "select r from HbRevision as r inner join r.version as v inner join v.library as l where l.name = ? and v.major = ? and v.minor = ? and r.number = ?")
                .setString(0, libraryName)
                .setInteger(1, majorVersion)
                .setInteger(2, minorVersion)
                .setInteger(3, revisionNumber)
                .uniqueResult();
            
            if (o == null) throw new RevisionNotFoundException();
            HbRevision revision = (HbRevision)o; 
            
            session.delete(revision);
            tx.commit();
            
            logger.debug("Revision " + libraryName + "-" + majorVersion + "." + minorVersion + "." + revisionNumber + " successfuly deleted");
            
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught deleting revision "  + libraryName + "-" + majorVersion + "." + minorVersion + "." + revisionNumber, e);
            tx.rollback();
            throw new BackendInternalErrorException("Hibernate exception caught deleting revision "  + libraryName + "-" + majorVersion + "." + minorVersion + "." + revisionNumber, e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public void deleteVersion(String libraryName, int majorVersion, int minorVersion) throws LibraryNotFoundException, VersionNotFoundException, BackendInternalErrorException {
        Session session = currentSession();
        Transaction tx = session.beginTransaction();
        try {
            
            logger.debug("Deleting version " + libraryName + "-" + majorVersion + "." + minorVersion);
            
            Object o = session.createQuery(
                "select v from HbVersion as v inner join v.library as l where l.name = ? and v.major = ? and v.minor = ?")
                .setString(0, libraryName)
                .setInteger(1, majorVersion)
                .setInteger(2, minorVersion)
                .uniqueResult();
            
            if (o == null) throw new VersionNotFoundException();
            HbVersion version = (HbVersion)o; 
            
            session.delete(version);
            tx.commit();
            
            logger.debug("Version " + libraryName + "-" + majorVersion + "." + minorVersion + " successfully deleted");
            
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught deleting version " + libraryName + "-" + majorVersion + "." + minorVersion, e);
            tx.rollback();
            throw new BackendInternalErrorException("Hibernate exception caught deleting version " + libraryName + "-" + majorVersion + "." + minorVersion, e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public String[] getAdminRoles() {
        return adminRoles;
    }

    public java.util.Collection<Library> getAllLibraries() throws BackendInternalErrorException {
        try {
            
            HashSet<Library> bag = new HashSet<Library>();
            
            logger.debug("Retriving all libraries");
            
            Session session = currentSession();

            List list = session.createQuery(
                "select l from HbLibrary as l")
                .list();
            
            for (Object o: list) {
                HbLibrary l = (HbLibrary)o;
                bag.add(translateLibrary(l));
            }
            
            return bag;
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught retriving all libraries", e);
            throw new BackendInternalErrorException("Hibernate exception caught retriving all libraries", e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public Bytecode getBytecode(String classname, int majorVersion, int minorVersion) throws LibraryNotFoundException, VersionNotFoundException, BytecodeNotFoundException, BackendInternalErrorException {
        try {
            
            logger.debug("Retriving bytecode of " + classname + "-" + majorVersion + "." + minorVersion);
            
            Session session = currentSession();

            List list = session.createQuery(
                "select b from HbBytecode as b inner join b.revision as r inner join r.version as v where v.major = ? and v.minor = ? and b.className = ? order by r.number desc")
                .setInteger(0, majorVersion)
                .setInteger(1, minorVersion)
                .setString(2, classname)
                .list();
            
            logger.debug(list.size() + " bytecode instances found");
            if (list.size() == 0) throw new BytecodeNotFoundException();
            
            HbBytecode b = (HbBytecode)list.get(0);
            
            logger.debug("The first entry revision number is " + b.getRevision().getNumber() + " (max)");
            
            //TODO: utilizar los PARAMETROS, para ahorrar lazy fetching
            Bytecode by = new Bytecode();
            by.setClassName(b.getClassName());
            by.setMajorVersion(b.getRevision().getVersion().getMajor());
            by.setMinorVersion(b.getRevision().getVersion().getMinor());
            by.setRevision(b.getRevision().getNumber());
            by.setClassData(b.getClassData());
            
            //fijamos los allowed roles
            HashSet<String> verRoles = new HashSet<String>(b.getRevision().getVersion().getAllowedRoles());
            HashSet<String> libRoles = new HashSet<String>(b.getRevision().getVersion().getLibrary().getAllowedRoles());
            
            verRoles.retainAll(libRoles);
            
            by.setAllowedRoles(verRoles.toArray(new String[0]));
            
            return by;
           
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught retriving bytecode of " + classname + "-" + majorVersion + "." + minorVersion, e);
            throw new BackendInternalErrorException("Hibernate exception caught retriving bytecode of " + classname + "-" + majorVersion + "." + minorVersion, e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public Library getLibrary(String libraryName) throws LibraryNotFoundException, BackendInternalErrorException {
        try {
            Session session = currentSession();

            Object o = session.createQuery(
                "select l from HbLibrary as l where l.name = ?")
                .setString(0, libraryName)
                .uniqueResult();
            if (o == null) throw new LibraryNotFoundException();
            HbLibrary library = (HbLibrary)o; 
            
            return translateLibrary(library);
            
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught retrieving library " + libraryName, e);
            throw new BackendInternalErrorException("Hibernate exception caught retrieving library " + libraryName, e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public String[] getPublishRoles() {
        return publishRoles;
    }

    public Version getVersion(String libraryName, int majorVersion, int minorVersion, boolean deliverStubJar, boolean deliverJavadocZip) throws LibraryNotFoundException, VersionNotFoundException, BackendInternalErrorException {
        try {
            Session session = currentSession();

            Object o = session.createQuery(
                "select v from HbVersion as v inner join v.library as l where l.name = ? and v.major = ? and v.minor = ?")
                .setString(0, libraryName)
                .setInteger(1, majorVersion)
                .setInteger(2, minorVersion)
                .uniqueResult();
            if (o == null) throw new VersionNotFoundException();
            HbVersion version = (HbVersion)o; 
            
            return translateVersion(version, deliverStubJar, deliverJavadocZip);
            
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught retrieving version " + libraryName + "-" + majorVersion + "." + minorVersion, e);
            throw new BackendInternalErrorException("Hibernate exception caught retrieving version " + libraryName + "-" + majorVersion + "." + minorVersion, e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public void init() throws BackendInitializationException {
        try {
            // Create the SessionFactory
            sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            logger.error("Initial SessionFactory creation failed.", ex);
            throw new BackendInitializationException("Unable to initialize hibernate session factory", ex);
        }
        
        //cargar los roles del backend
        try {
            logger.debug("Loading backend security roles");
            
            Session session = currentSession();

            List list = session.createQuery(
                "select r from HbBackendRole as r")
                .list();
            
            logger.debug("Configured roles are: " + list.toString());
            
            HashSet<String> admin = new HashSet<String>();
            HashSet<String> publish = new HashSet<String>();
            for (Object o: list) {
                HbBackendRole role = (HbBackendRole)o;
                
                logger.debug("iterating: OP=" + role.getOperation() + ", ROLENAME=" + role.getRoleName());
                
                if (role.getOperation() == HbBackendRole.OP_ADMIN) {
                    logger.debug("Adding " + role.getRoleName() + " to admin roles");
                    admin.add(role.getRoleName());
                } else if (role.getOperation() == HbBackendRole.OP_PUBLISH) {
                    logger.debug("Adding " + role.getRoleName() + " to publish roles");
                    publish.add(role.getRoleName());
                }
            }
            
            adminRoles = admin.toArray(new String[0]);
            publishRoles = publish.toArray(new String[0]);
            
            logger.debug("Effective admin roles are: " + Arrays.deepToString(adminRoles));
            logger.debug("Effective publish roles are: " + Arrays.deepToString(publishRoles));
            
        } catch (Exception e) {
            logger.error("Hibernate exception caught retrieving backend allowed security roles. ADMIN ROLE SET TO any", e);
            adminRoles = new String[] { "any" };
            throw new BackendInitializationException("Hibernate exception caught retrieving backend allowed security roles", e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }

    }

    public void setAdminRoles(String[] newRoles) throws BackendInternalErrorException {
        Session session = currentSession();
        Transaction tx = session.beginTransaction();
        try {
            
            logger.debug("Updating backend administrative roles");
            
            session.createQuery(
                "delete from HbBackendRole where operation = ?")
                .setInteger(0, HbBackendRole.OP_ADMIN)
                .executeUpdate();
            
            for (String role: newRoles) {
                
                HbBackendRole r = new HbBackendRole();
                r.setOperation(HbBackendRole.OP_ADMIN);
                r.setRoleName(role);
                
                session.save(r);

            }
            
            tx.commit();
            
            adminRoles = newRoles;
            
            logger.debug("Administrative roles successfully updated");
            
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught updating backend administrative roles", e);
            tx.rollback();
            throw new BackendInternalErrorException("Hibernate exception caught updating backend administrative roles", e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public void setPublishRoles(String[] newRoles) throws BackendInternalErrorException {
            Session session = currentSession();
        Transaction tx = session.beginTransaction();
        try {
            
            logger.debug("Updating backend publish roles");
            
            session.createQuery(
                "delete from HbBackendRole where operation = ?")
                .setInteger(0, HbBackendRole.OP_PUBLISH)
                .executeUpdate();
            
            for (String role: newRoles) {
                
                HbBackendRole r = new HbBackendRole();
                r.setOperation(HbBackendRole.OP_PUBLISH);
                r.setRoleName(role);
                
                session.save(r);

            }
            
            tx.commit();
            
            publishRoles = newRoles;
            
            logger.debug("Publish roles successfully updated");
            
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught updating backend publish roles", e);
            tx.rollback();
            throw new BackendInternalErrorException("Hibernate exception caught updating backend publish roles", e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public void storeLibrary(Library lib) throws LibraryAlreadyExistsException, BackendInternalErrorException {
        logger.debug("Storing library " + lib.getName());
        Session session = currentSession();
        Transaction tx = session.beginTransaction();
        
        try {
            
            dbStoreLibrary(lib);
            tx.commit();
            logger.debug("Transaction commited");
            
        } catch (LibraryAlreadyExistsException e) {
            tx.rollback();
            throw e;
        } catch (VersionAlreadyExistsException e) {
            logger.error("Version already exists storing library " + lib.getName() , e);
            tx.rollback();
            throw new BackendInternalErrorException("Version already exists storing new library " + lib.getName(), e);
        } catch (RevisionAlreadyExistsException e) {
            logger.error("Revision already exists storing library " + lib.getName() , e);
            tx.rollback();
            throw new BackendInternalErrorException("Revision already exists storing new library " + lib.getName(), e);
        } catch (LibraryNotFoundException e) {
            logger.error("Just stored library wasn't found while storing its versions", e);
            tx.rollback();
            throw new BackendInternalErrorException("Just stored library wasn't found while storing its versions", e);
        } catch (VersionNotFoundException e) {
            logger.error("Just stored version wasn't found while storing it's revisions, storing library " + lib.getName(), e);
            tx.rollback();
            throw new BackendInternalErrorException("Just stored library wasn't found while storing its revision, storing library " + lib.getName(), e);            
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught storing library " + lib.getName(), e);
            tx.rollback();
            throw new BackendInternalErrorException("Hibernate exception caught storing library " + lib.getName(), e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public void storeRevision(String libraryName, int majorVersion, int minorVersion, Revision r) throws RevisionAlreadyExistsException, VersionNotFoundException, LibraryNotFoundException, BackendInternalErrorException {
        logger.debug("Storing new revision of " + libraryName + "-" + majorVersion + "." + minorVersion);
        Session session = currentSession();
        Transaction tx = session.beginTransaction();
        try {
            
            dbStoreRevision(libraryName, majorVersion, minorVersion, r);
            tx.commit();
            logger.debug("Transaction commited");

        } catch (VersionNotFoundException e) {
            logger.debug("Version not found storing revision " + libraryName + "-" + majorVersion + "." + minorVersion + "-" + r.getRevisionNumber(), e);
            tx.rollback();
            throw e;
        } catch (RevisionAlreadyExistsException e) {            
            logger.debug("Revision already exists storing revision " + libraryName + "-" + majorVersion + "." + minorVersion + "-" + r.getRevisionNumber(), e);
            tx.rollback();
            throw e;
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught storing revision " + libraryName + "-" + majorVersion + "." + minorVersion + "-" + r.getRevisionNumber(), e);
            tx.rollback();
            throw new BackendInternalErrorException("Hibernate exception caught (" + e.getMessage() + ")", e);
        } finally {
            closeSession(); 
        }
    }

    public void storeVersion(String libraryName, Version version) throws VersionAlreadyExistsException, LibraryNotFoundException, BackendInternalErrorException {
        logger.debug("Storing new version of " + libraryName);
        Session session = currentSession();
        Transaction tx = session.beginTransaction();
        
        try {
            
            dbStoreVersion(libraryName, version);
            tx.commit();
            logger.debug("Transaction commited");
            
        } catch (LibraryNotFoundException e) {
            logger.debug("Library " + libraryName + " not found storing version " + libraryName + "-" + version.getNumberMajor() + "." + version.getNumberMinor(), e);
            tx.rollback();
            throw e;
        } catch (VersionNotFoundException e) {
            logger.error("Version not found storing version " + libraryName + "-" + version.getNumberMajor() + "." + version.getNumberMinor(), e);
            tx.rollback();
            throw new BackendInternalErrorException("Version not found storing version " + libraryName + "-" + version.getNumberMajor() + "." + version.getNumberMinor(), e);
        } catch (RevisionAlreadyExistsException e) {            
            logger.error("Revision already exists storing version " +libraryName + "-" + version.getNumberMajor() + "." + version.getNumberMinor(), e);
            tx.rollback();
            throw new BackendInternalErrorException("Version not found storing version " + libraryName + "-" + version.getNumberMajor() + "." + version.getNumberMinor(), e);
        } catch (VersionAlreadyExistsException e) {            
            logger.debug("Version already exists storing version " + libraryName + "-" + version.getNumberMajor() + "." + version.getNumberMinor(), e);
            tx.rollback();
            throw e;
        } catch (HibernateException e) {
            logger.error("Hibernate exception caugth storing new Version: " + libraryName + "-" + version.getNumberMajor() + "." + version.getNumberMinor(), e);
            tx.rollback();
            throw new BackendInternalErrorException("Hibernate exception caugth storing new Version: " + libraryName + "-" + version.getNumberMajor() + "." + version.getNumberMinor(), e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public void updateLibrary(Library lib) throws LibraryNotFoundException, BackendInternalErrorException {
        Session session = currentSession();
        Transaction tx = session.beginTransaction();
        try {
            
            logger.debug("Updating library " + lib.getName());
            
            Object o = session.createQuery(
                "select l from HbLibrary as l where l.name = ?")
                .setString(0, lib.getName())
                .uniqueResult();
            if (o == null) throw new LibraryNotFoundException();
            HbLibrary library = (HbLibrary)o; 
            
            if (lib.getDescription() != null) {
                library.setDescription(lib.getDescription());
            }
            
            if (lib.getAllowedRoles() != null) {
                library.getAllowedRoles().clear();
                for (String role: lib.getAllowedRoles()) {
                    library.getAllowedRoles().add(role);
                }
            }
            
            tx.commit();
            
            logger.debug("Library " + lib.getName() + " successfully updated");
            
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught updating library " + lib.getName(), e);
            tx.rollback();
            throw new BackendInternalErrorException("Hibernate exception caught updatinglibrary " + lib.getName(), e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }

    public void updateVersion(Version v) throws LibraryNotFoundException, VersionNotFoundException, BackendInternalErrorException {
        Session session = currentSession();
        Transaction tx = session.beginTransaction();
        try {
            
            logger.debug("Updating version " + v.getLibraryName() + "-" + v.getNumberMajor() + "." + v.getNumberMinor());
            
            Object o = session.createQuery(
                "select v from HbVersion as v inner join v.library as l where l.name = ? and v.major = ? and v.minor = ?")
                .setString(0, v.getLibraryName())
                .setInteger(1, v.getNumberMajor())
                .setInteger(2, v.getNumberMinor())
                .uniqueResult();
            
            if (o == null) throw new VersionNotFoundException();
            HbVersion version = (HbVersion)o; 
            
            if (v.getDescription() != null) {
                version.setDescription(v.getDescription());    
            }
            
            if (v.getAllowedRoles() != null) {
                version.getAllowedRoles().clear();
                for (String role: v.getAllowedRoles()) {
                    version.getAllowedRoles().add(role);
                }    
            }
            
            tx.commit();
            
            logger.debug("Version " + v.getLibraryName() + "-" + v.getNumberMajor() + "." + v.getNumberMinor() + " successfully updated");
            
        } catch (HibernateException e) {
            logger.error("Hibernate exception caught deleting version " + v.getLibraryName() + "-" + v.getNumberMajor() + "." + v.getNumberMinor(), e);
            tx.rollback();
            throw new BackendInternalErrorException("Hibernate exception caught updating version " + v.getLibraryName() + "-" + v.getNumberMajor() + "." + v.getNumberMinor(), e);
        } finally {
            try { closeSession(); } catch (Exception e) {}
        }
    }
    
    private static Session currentSession() {
        Session s = (Session) session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
            s = sessionFactory.openSession();
            session.set(s);
        }
        return s;
    }

    private static void closeSession() {
        Session s = (Session) session.get();
        if (s != null)
            s.close();
        session.set(null);
    }
    
    private void dbStoreLibrary(Library lib) 
    throws 
        HibernateException, 
        LibraryAlreadyExistsException, 
        VersionAlreadyExistsException, 
        RevisionAlreadyExistsException, 
        LibraryNotFoundException, 
        VersionNotFoundException 
    {
     
        try {
            Session session = currentSession();

            HbLibrary newLib = new HbLibrary();
            newLib.setName(lib.getName());
            newLib.setDescription(lib.getDescription());
            for (String role: lib.getAllowedRoles()) {
                newLib.getAllowedRoles().add(role);
            }
            session.save(newLib, lib.getName());
            logger.debug("New library successfully saved, starting versions store");

            for (Version v: lib.getVersions()) {
                dbStoreVersion(lib.getName(), v);
            }
            logger.debug("New versions successfully saved");
        } catch (ConstraintViolationException e) {
            logger.debug("Constraint violated storing library " + lib.getName(), e);
            throw new LibraryAlreadyExistsException("Constraint violated storing library " + lib.getName(), e);
        }
    }
    
    private void dbStoreVersion(String libraryName, Version version) 
    throws 
        HibernateException, 
        VersionAlreadyExistsException, 
        LibraryNotFoundException, 
        VersionNotFoundException, 
        RevisionAlreadyExistsException 
    {
        
        try {
            Session session = currentSession();

            Object o = session.createQuery(
                    "select l from HbLibrary as l where l.name = ?")
                    .setString(0, libraryName)
                    .uniqueResult();
            if (o == null) throw new LibraryNotFoundException();
            
            HbLibrary library = (HbLibrary)o;

            HbVersion newVer = new HbVersion();
            newVer.setMajor(version.getNumberMajor());
            newVer.setMinor(version.getNumberMinor());
            newVer.setDescription(version.getDescription());
            newVer.setStubsJar(version.getStubsJar());
            newVer.setJavadocZip(version.getJavadocZip());

            for (String role: version.getAllowedRoles()) {
                newVer.getAllowedRoles().add(role);
            }

            library.addVersion(newVer);
            
            HbVersionId id = new HbVersionId();
            id.setLibrary_name(libraryName);
            id.setMajor(version.getNumberMajor());
            id.setMinor(version.getNumberMinor());
            
            session.save(newVer, id);
            logger.debug("New version successfully saved, starting revisions store");

            for (Revision r: version.getRevisions()) {
                dbStoreRevision(libraryName, version.getNumberMajor(), version.getNumberMinor(), r);
            }

            logger.debug("New revisions successfully saved");
        } catch (ConstraintViolationException e) {
            logger.debug("Constraint violated storing version " + libraryName + "-" + version.getNumberMajor() + "." + version.getNumberMinor(), e);
            throw new VersionAlreadyExistsException("Constraint violated storing version " + libraryName + "-" + version.getNumberMajor() + "." + version.getNumberMinor(), e);
        }
    }
    
    private void dbStoreRevision(String libraryName, int majorVersion, int minorVersion, Revision r) 
    throws 
        HibernateException, 
        VersionNotFoundException, 
        RevisionAlreadyExistsException
    {
        try {
            Session session = currentSession();

            Object o = session.createQuery(
                "select v from HbVersion as v inner join v.library as l where l.name = ? and v.major = ? and v.minor = ?")
                .setString(0, libraryName)
                .setInteger(1, majorVersion)
                .setInteger(2, minorVersion)
                .uniqueResult();
            if (o == null) throw new VersionNotFoundException();
            HbVersion version = (HbVersion)o; 
            
            HbRevision newRev = new HbRevision();
            newRev.setNumber(r.getRevisionNumber());
            version.addRevision(newRev);
            
            HbRevisionId id = new HbRevisionId();
            id.setLibrary_name(libraryName);
            id.setVersion_major(majorVersion);
            id.setVersion_minor(minorVersion);
            id.setNumber(r.getRevisionNumber());
            
            session.save(newRev, id);
            logger.debug("New revision successfully saved, starting bytecode store");

            for (Bytecode b: r.getClasses()) {
                HbBytecode newBytecode = new HbBytecode();
                newBytecode.setClassName(b.getClassName());
                newBytecode.setClassData(b.getClassData());
                newRev.addBytecode(newBytecode);
                
                HbBytecodeId bid = new HbBytecodeId();
                bid.setLibrary_name(libraryName);
                bid.setVersion_major(majorVersion);
                bid.setVersion_minor(minorVersion);
                bid.setRevision_number(r.getRevisionNumber());
                bid.setClassName(b.getClassName());
                
                session.save(newBytecode, bid);
            }
            logger.debug("Bytecode stored");
        } catch (ConstraintViolationException e) {
            logger.debug("Constraint violated storing revision " + libraryName + "-" + majorVersion + "." + minorVersion + "-" + r.getRevisionNumber(), e);
            throw new RevisionAlreadyExistsException("Constraint violated storing revision " + libraryName + "-" + majorVersion + "." + minorVersion + "-" + r.getRevisionNumber(), e);
        }
    }
    
    private Version translateVersion(HbVersion version, boolean deliverStubJar, boolean deliverJavadocZip) {
        Version v = new Version();
        v.setLibraryName(version.getLibrary().getName());
        v.setLibraryDescription(version.getLibrary().getDescription());
        v.setNumberMajor(version.getMajor());
        v.setNumberMinor(version.getMinor());
        v.setDescription(version.getDescription());
        
        HashSet<String> verRoles = new HashSet<String>(version.getAllowedRoles());
        HashSet<String> libRoles = new HashSet<String>(version.getLibrary().getAllowedRoles());
            
        verRoles.retainAll(libRoles);
        
        v.setAllowedRoles(verRoles.toArray(new String[0]));

        if (deliverStubJar) v.setStubsJar(version.getStubsJar());
        if (deliverJavadocZip) v.setJavadocZip(version.getJavadocZip());

        for (Object x: version.getRevisions()) {
            HbRevision r = (HbRevision)x;
            v.addRevision(translateRevision(r));
        }
        
        return v;
    }
    
    private Revision translateRevision(HbRevision revision) {
        Revision r = new Revision();
        r.setRevisionNumber(revision.getNumber());
        r.setDescription("");
        return r;
    }
    
    
    private Library translateLibrary(HbLibrary library) {
        Library l = new Library();
        l.setName(library.getName());
        l.setDescription(library.getDescription());
        l.setAllowedRoles((String[])library.getAllowedRoles().toArray(new String[0]));
        for (Object o: library.getVersions()) {
            HbVersion v = (HbVersion)o;
            l.addVersion(translateVersion(v, false, false));
        }
        return l;
    }
}
