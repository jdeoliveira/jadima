/*
 * XmlConfigurator.java
 *
 * Created on 1 de mayo de 2005, 05:48 PM
 */

package ve.usb.jgm.client;

import com.softcorporation.xmllight.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import ve.usb.jgm.client.config.*;
import ve.usb.jgm.util.*;

/**
 *
 * @author  Administrator
 */
public class XmlConfigurator {
    
    private static Logger logger = Logger.getLogger(XmlConfigurator.class);

    private static Element configurationElement = null;
    
    static {
        //cargar el documento xml desde el archivo de configuracion
        String configFile = null;
        
        try {
            configFile = System.getProperty("user.home") + File.separator + ".jgm" + File.separator + "jgm-config.xml";
            
            logger.debug("Loading configuration file " + configFile);
            
            configurationElement = XMLLightUtil.readXMLFile(configFile);
            
            // check that Element exists in XML document
            if (configurationElement.isNull()) {
                logger.error("Root element is null");
                throw new RuntimeException("Errors parsing configuration file (root element is null)");
            }
            
        }  catch (IOException e) {
            logger.error("Unable to read client configuration file", e);
            throw new RuntimeException("Unable to read client configuration file", e);
        } catch (SecurityException e) {
            logger.error("Unable to read client configuration file (access denied)", e);
            throw new RuntimeException("Unable to read client configuration file (access denied)", e);
        } catch (XMLLightException e) {
            logger.error("Errors parsing client configuration file (" + configFile + ")", e);
            throw new RuntimeException("Errors parsing client configuration file (" + configFile + ")", e);
        }
    }
    
    public static Element getConfigurationElement() {
        return configurationElement;
    }    
}
