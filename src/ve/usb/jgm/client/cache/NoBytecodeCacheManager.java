/*
 * NoCacheManager.java
 *
 * Cache manager que no hace nada de cache para bytecode, y no es multithreaded
 * Created on 18 de mayo de 2005, 01:06 AM
 */

package ve.usb.jgm.client.cache;

import ve.usb.jgm.client.*;
import ve.usb.jgm.repo.*;
import java.util.*;
import org.apache.log4j.*;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class NoBytecodeCacheManager extends BytecodeCacheManager {
    
    private static final Logger logger = Logger.getLogger(NoBytecodeCacheManager.class);
    
    /** Creates a new instance of NoCacheManager */
    public NoBytecodeCacheManager() {
    }

    public void configure(com.softcorporation.xmllight.Element el) throws BytecodeCacheManagerConfigurationException {
        //Nothing to configure here
    }

    public byte[] getClasses(Bytecode mainClass, Collection<Bytecode> relatedClasses) throws ClassNotFoundException {
        //contactar al repository client 
        //pidiendole solo la main class (las demas no importan, porque como no
        //hay cache, no se va a guardar nada)
        
        //en este caso se consultan los repos secuencialmente y se devuelve
        //la primera ocurrencia de la clase main solicitada
        
        Collection<Bytecode> lasClases = new HashSet<Bytecode>();
        lasClases.add(mainClass);
        
        for (RepositoryClient c: repos) {
            logger.debug("trying " + c.getName());
            try {
                Collection<Bytecode> resp = c.requestClasses(lasClases);
                if (resp.size() > 0) {
                    //la encontre, la devuelvo de una
                    Iterator<Bytecode> it = resp.iterator();
                    Bytecode theMainClass = it.next();
                    logger.debug("Revision " + theMainClass.getRevision() + " found");
                    return theMainClass.getClassData();
                } else {
                    logger.debug("Not found, trying next repo");
                }
            } catch (Exception e) {
                logger.warn("Exception communicating with repository, trying next", e);
            }
        }
        
        //si caemos aqui es porque no se encontro en ningun repositorio
        logger.debug("Class " + mainClass.getClassName() + " " + mainClass.getMajorVersion() + "." + mainClass.getMinorVersion() + " not found on cache or configured repositories");
        throw new ClassNotFoundException("Class " + mainClass.getClassName() + " " + mainClass.getMajorVersion() + "." + mainClass.getMinorVersion() + " not found on cache or configured repositories");
        
    }

    public void init() throws BytecodeCacheManagerInitializationException {
        //nothing to initialize here
    }
    
}
