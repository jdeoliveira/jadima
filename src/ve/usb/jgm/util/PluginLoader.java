/*
 * PluginLoader.java
 *
 * Created on 2 de mayo de 2005, 09:07 PM
 */

package ve.usb.jgm.util;

import java.io.*;
import org.apache.log4j.*;
/**
 *
 * @author  Administrator
 */
public class PluginLoader {
    
    private static Logger logger = Logger.getLogger(PluginLoader.class);
    
    //add all the jars in the plugin directory to the classpath 
    static {
        logger.debug("Adding all the jars in the plugin directory to the classpath");
        logger.debug("The classpath before is: " + System.getProperty("java.class.path"));
        
        File pluginDir = new File(System.getenv("JGM_HOME") + System.getProperty("file.separator") + "plugins");
        
        String[] jars = pluginDir.list();
        
        for (int i = 0; i < jars.length; i++) {
            String aJar = jars[i];
            
            logger.debug("adding " + aJar);
            
            System.setProperty("java.class.path", System.getProperty("java.class.path") + System.getProperty("path.separator") + System.getenv("JGM_HOME") + System.getProperty("file.separator") + "plugins" + System.getProperty("file.separator") + aJar);
        }
        
        logger.debug("The classpath after is: " + System.getProperty("java.class.path"));
    }
}
