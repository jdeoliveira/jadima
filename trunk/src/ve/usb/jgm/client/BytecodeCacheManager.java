/*
 * CacheManager.java
 *
 * Created on 16 de marzo de 2005, 10:53
 */

package ve.usb.jgm.client;

import java.util.*;
import org.apache.log4j.*;
import com.softcorporation.xmllight.*;
import ve.usb.jgm.repo.*;
import java.util.*;

/**
 * puedo tener varias instancias de esta clase, asi que 
 *
 * @author  jdeoliveira
 */
public abstract class BytecodeCacheManager {
   
    private static final Logger logger = Logger.getLogger(BytecodeCacheManager.class);
    
    protected Collection<RepositoryClient> repos;
    
    public final void loadRepos() {
        //Cargamos los repositorios configurados en el $HOME/.jgm/jgm-config.xml
        logger.debug("Loading configured repositories");
        
        repos = RepositoryClientFactory.makeRepositoryClients();
        
        logger.debug("Repositories loaded");
    }
    
    public abstract void configure(Element el) throws BytecodeCacheManagerConfigurationException;
    
    public abstract void init() throws BytecodeCacheManagerInitializationException;
    
    public abstract byte[] getClasses(Bytecode mainClass, Collection<Bytecode> relatedClasses) throws ClassNotFoundException;
    
}
