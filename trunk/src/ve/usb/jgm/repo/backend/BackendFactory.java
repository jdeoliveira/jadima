/*
 * RepositoryFactory.java
 *
 * Created on 18 de junio de 2004, 09:23 AM
 */

package ve.usb.jgm.repo.backend;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.softcorporation.xmllight.*;

// Import log4j classes.
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;


/**
 *
 * @author  jdeoliveira
 */
public class BackendFactory {
    
    static Logger logger = Logger.getLogger(BackendFactory.class);
    
    /**
     * 
     * @param config 
     * @throws ve.usb.jgm.repo.exceptions.MisconfiguredLocalRepositoryException 
     * @return 
     */
    public static Backend makeBackend(Element config) throws BackendInitializationException, BackendConfigurationException {
        logger.info("Initializating new backend");
        Backend b = null;
        
        String className = config.getAttr("class");
        
        if ((className == null) || (className.length() == 0)) {
            logger.warn("Empty backend class name");
            throw new BackendInitializationException("Empty backend class name");
        } else {
            logger.debug("Backend class name is " + className);
            try {
                b = (Backend)Class.forName(className).newInstance();
            } catch (ClassNotFoundException e) {
                logger.error("Specified backend class not found (" + className + ")", e);
                throw new BackendInitializationException("Specified backend class not found (" + className + ")", e);
            } catch (InstantiationException e) {
                logger.error("Unable to instantiate specified backend class (" + className + ")", e);
                throw new BackendInitializationException("Unable to instantiate specified backend class (" + className + ")", e);
            } catch (IllegalAccessException e) {
                logger.error("Unable to instantiate specified backend class (" + className + ")", e);
                throw new BackendInitializationException("Unable to instantiate specified backend class (" + className + ")", e);
            } 
            
            logger.debug("Backend instantiated");
            
            //Ahora configuramos el repositorio, pasandole su propio tag.
            b.configure(config);
            
            logger.debug("Backend configured");
            logger.info("Backend initialized correctly");
            
            return b;
            
        }
    }
}
