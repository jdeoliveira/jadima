/*
 * RepositoryClientFactory.java
 *
 * Created on 16 de mayo de 2005, 12:01 AM
 */

package ve.usb.jgm.client;

import ve.usb.jgm.client.config.*;
import java.util.*;
import org.apache.log4j.*;
import com.softcorporation.xmllight.*;
import java.io.*;
import java.lang.reflect.*;

/**
 *
 * Este se encarga de cargar la configuracion de los cache managers desde
 * el archivo de configuracion, una unica vez (en el inicializador estatico)
 *
 * Llamadas subsecuentes me dev
 uelven las instancias de los cache managers ya cargadas en memoria
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class PrefetchAgentFactory {
    
    private static final Logger logger = Logger.getLogger(PrefetchAgentFactory.class);
    
    private static PrefetchAgent prefetchAgent = null;
        
    static {
        try {
            Element elemRoot = XmlConfigurator.getConfigurationElement();

            //Obtener configuracion del agente de prefetching
            Element prefetchEl = elemRoot.getElem("prefetch");
            if (prefetchEl.isNull()) {
                logger.error("Missing prefetch tag on the configuration file");
                throw new RuntimeException("Missing prefetch tag on the configuration file");
            }

            String type = "<unknow>"; 
            try {
                type = prefetchEl.getAttr("type");
                

                //buscamos la clase dada, obtenemos el constructor sin parametros 
                //y le pedimos una nueva instancia
                prefetchAgent = (PrefetchAgent)Class.forName(type).getConstructor().newInstance();

                //Le pasamos su element para que se configure
                prefetchAgent.configure(prefetchEl);
                prefetchAgent.init();

            } catch (NoSuchMethodException e) {
                logger.error("Prefetch agent is of an invalid type. (The class " + type + " doesn't have a no-argument constructor)", e);
                throw new RuntimeException("Prefetch agent  is of an invalid type. (The class " + type + " doesn't have a no-argument constructor)", e);
            } catch (SecurityException e) {
                logger.error("Prefetch agent  is of an invalid type. (Couln't access " + type + " no-argument constructor)", e);
                throw new RuntimeException("Prefetch agent  is of an invalid type. (Couln't access " + type + " no-argument constructor)", e);
            } catch (ClassCastException e) {
                logger.error("Prefetch agent  is of an invalid type. (The class " + type + " is not a subclass of ve.usb.jgm.client)", e);
                throw new RuntimeException("Prefetch agent  is of an invalid type. (The class " + type + " is not a subclass of ve.usb.jgm.client)", e);
            } catch (InstantiationException e) {
                logger.error("Prefetch agent  is of an invalid type. (The class " + type + " couldn't be instantiated)", e);
                throw new RuntimeException("Prefetch agent  is of an invalid type. (The class " + type + " couldn't be instantiated)", e);
            } catch (IllegalAccessException e) {
                logger.error("Prefetch agent  is of an invalid type. (" + type + "'s no-argument constructor is not public)", e);
                throw new RuntimeException("Prefetch agent  is of an invalid type. (The class " + type + " couldn't be instantiated)", e);
            } catch (InvocationTargetException e) {
                logger.error("Prefetch agent is of an invalid type. (No-argument constructor of " + type + " threw an exception)", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (No-argument constructor of " + type + " threw an exception)", e);
            } catch (ClassNotFoundException e) {
                logger.error("Prefetch agent  is of an invalid type. (The class " + type + " couldn't be found)", e);
                throw new RuntimeException("Prefetch agent  is of an invalid type. (The class " + type + " couldn't be found)", e);
            } catch (PrefetchAgentConfigurationException e) {
                logger.error("Prefetch agent  is of an invalid type. (Errors configuring " + type + ")", e);
                throw new RuntimeException("Prefetch agent  is of an invalid type. (Errors configuring " + type + ")", e);
            } catch (PrefetchAgentInitializationException e) {
                logger.error("Prefetch agent  is of an invalid type. (Unable to initialize " + type + ")", e);
                throw new RuntimeException("Prefetch agent  is of an invalid type. (Unable to initialize " + type + ")", e);
            }

            logger.debug("Configured prefetch agent sucessfully loaded from configuration file");
        } catch (XMLLightException e) {
            logger.error("Errors parsing configuration file", e);
            throw new RuntimeException("Errors parsing configuration file", e);
        }
        
    }
    
    public static PrefetchAgent makePrefetchAgent() {
        return prefetchAgent;
    }
       
}
