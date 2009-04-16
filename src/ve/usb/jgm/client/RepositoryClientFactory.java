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
 * Este se encarga de cargar la configuracion de los repositorios desde
 * el archivo de configuracion, una unica vez (en el inicializador estatico)
 *
 * Llamadas subsecuentes me devuelven las instancias de repositorios ya cargadas en memoria
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class RepositoryClientFactory {
    
    private static final Logger logger = Logger.getLogger(RepositoryClientFactory.class);
    
    private static Collection<RepositoryClient> configuredClients = null;
    
    static {
        try {
            long before = System.currentTimeMillis();
            
            configuredClients = new LinkedList<RepositoryClient>();
            
            Element elemRoot = XmlConfigurator.getConfigurationElement();

            //Obtener configuracion de repositorios
            Element repositories = elemRoot.getElem("repositories");
            Element repoEl = repositories.getElem("repo");

            String type;
            String name;
            int priority;

            synchronized (configuredClients) {
            
            
            while(!repoEl.isNull()) {
                type = repoEl.getAttr("type");
                name = repoEl.getAttr("name");
                logger.debug("Instantiating repository " + name + " of type " + type);

                try {
                    priority = (new Integer(repoEl.getAttr("priority"))).intValue();
                } catch (NumberFormatException e) {
                    logger.warn("Repository " + name + " has invalid priority attribute value, assuming priority = 0. Please check te configuration file");
                    priority = 0;
                }

                try {

                    //buscamos la clase dada, obtenemos el constructor sin parametros 
                    //y le pedimos una nueva instancia
                    RepositoryClient aClient = (RepositoryClient)Class.forName(type).getConstructor().newInstance();

                    //Le ponemos el nombre
                    aClient.setName(repoEl.getAttr("name"));
                    
                    //Le pasamos su element para que se configure
                    aClient.configure(repoEl);
                    aClient.init();


                    //Lo agregamos a nuestra coleccion
                    configuredClients.add(aClient);


                } catch (NoSuchMethodException e) {
                    logger.warn("Repository " + name + " is of an invalid type, ignoring it (The class " + type + " doesn't have a no-argument constructor)", e);
                } catch (SecurityException e) {
                    logger.warn("Repository " + name + " is of an invalid type, ignoring it (Couln't access " + type + " no-argument constructor)", e);
                } catch (ClassCastException e) {
                    logger.warn("Repository " + name + " is of an invalid type, ignoring it (The class " + type + " is not a subclass of ve.usb.jgm.client)", e);
                } catch (InstantiationException e) {
                    logger.warn("Repository " + name + " is of an invalid type, ignoring it (The class " + type + " couldn't be instantiated)", e);
                } catch (IllegalAccessException e) {
                    logger.warn("Repository " + name + " is of an invalid type, ignoring it (" + type + "'s no-argument constructor is not public)", e);
                } catch (InvocationTargetException e) {
                    logger.warn("Repository " + name + " is of an invalid type, ignoring it (" + type + "'s no-argument constructor threw an exception)", e);
                } catch (ClassNotFoundException e) {
                    logger.warn("Repository " + name + " is of an invalid type, ignoring it (The class " + type + " couldn't be found)", e);
                } catch (RepositoryClientConfigurationException e) {
                    logger.warn("Repository " + name + " is of an invalid type, ignoring it (Errors configuring " + type + ")", e);
                } catch (RepositoryClientInitializationException e) {
                    logger.warn("Repository " + name + " is of an invalid type, ignoring it (Unable to initialize " + type + ")", e);
                }

                repoEl = repositories.getElem("repo");
            }

            //Chequeamos si tenemos al menos un repositorio configurado
            if (configuredClients.size() == 0) {
                logger.error("No repositories configured");
                throw new RuntimeException("No repositories configured");
            } else {
                logger.debug("Configured repositories sucessfully loaded from configuration file");
            }
            configuredClients.notifyAll();
            }
            
            long after = System.currentTimeMillis();
            double timeTook = after - before;
            logger.info("Ellapsed time discovering repos: " + timeTook);
        } catch (XMLLightException e) {
            logger.error("Errors parsing repositories configuration data", e);
            throw new RuntimeException("Errors parsing repositories configuration data", e);
        }
    }
    
    public static Collection<RepositoryClient> makeRepositoryClients() {
        return configuredClients;
    }
       
}
