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
 * Llamadas subsecuentes me devuelven las instancias de los cache managers ya cargadas en memoria
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class CacheManagerFactory {
    
    private static final Logger logger = Logger.getLogger(CacheManagerFactory.class);
    
    private static BytecodeCacheManager bytecodeCacheManager = null;
    private static StubsCacheManager stubsCacheManager = null;
    
    static {
        try {   
            Element elemRoot = XmlConfigurator.getConfigurationElement();

            //Obtener configuracion de repositorios
            Element cachesEl = elemRoot.getElem("cache");
            Element bytecodeCacheEl = cachesEl.getElem("bytecode");
            Element stubsCacheEl = cachesEl.getElem("stubs");

            //cargamos la informacion del cache de bytecode primero
            String type = null; 
            try {
                type = bytecodeCacheEl.getAttr("type");

                //buscamos la clase dada, obtenemos el constructor sin parametros 
                //y le pedimos una nueva instancia
                bytecodeCacheManager = (BytecodeCacheManager)Class.forName(type).getConstructor().newInstance();

                //Le pasamos su element para que se configure
                bytecodeCacheManager.configure(bytecodeCacheEl);
                bytecodeCacheManager.init();
                bytecodeCacheManager.loadRepos();

            } catch (NoSuchMethodException e) {
                logger.error("Bytecode cache manager is of an invalid type. (The class " + type + " doesn't have a no-argument constructor)", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (The class " + type + " doesn't have a no-argument constructor)", e);
            } catch (SecurityException e) {
                logger.error("Bytecode cache manager is of an invalid type. (Couln't access " + type + " no-argument constructor)", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (Couln't access " + type + " no-argument constructor)", e);
            } catch (ClassCastException e) {
                logger.error("Bytecode cache manager is of an invalid type. (The class " + type + " is not a subclass of ve.usb.jgm.client)", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (The class " + type + " is not a subclass of ve.usb.jgm.client)", e);
            } catch (InstantiationException e) {
                logger.error("Bytecode cache manager is of an invalid type. (The class " + type + " couldn't be instantiated)", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (The class " + type + " couldn't be instantiated)", e);
            } catch (IllegalAccessException e) {
                logger.error("Bytecode cache manager is of an invalid type. (Couln't access " + type + " no-argument constructor)", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (Couln't access " + type + " no-argument constructor)", e);
            } catch (InvocationTargetException e) {
                logger.error("Bytecode cache manager is of an invalid type. (No-argument constructor of " + type + " threw an exception)", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (No-argument constructor of " + type + " threw an exception)", e);
            } catch (ClassNotFoundException e) {
                logger.error("Bytecode cache manager is of an invalid type. (The class " + type + " couldn't be found)", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (The class " + type + " couldn't be found)", e);
            } catch (BytecodeCacheManagerConfigurationException e) {
                logger.error("Bytecode cache manager is of an invalid type. (Errors configuring " + type + ")", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (Errors configuring " + type + ")", e);
            } catch (BytecodeCacheManagerInitializationException e) {
                logger.error("Bytecode cache manager is of an invalid type. (Unable to initialize " + type + ")", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (Unable to initialize " + type + ")", e);
            }

            type = null;
            try {
                type = stubsCacheEl.getAttr("type");

                //buscamos la clase dada, obtenemos el constructor sin parametros 
                //y le pedimos una nueva instancia
                stubsCacheManager = (StubsCacheManager)Class.forName(type).getConstructor().newInstance();

                //Le pasamos su element para que se configure
                stubsCacheManager.configure(stubsCacheEl);
                stubsCacheManager.init();
                stubsCacheManager.loadRepos();

            } catch (NoSuchMethodException e) {
                logger.error("Stubs cache manager is of an invalid type. (The class " + type + " doesn't have a no-argument constructor)", e);
                throw new RuntimeException("Stubs cache manager is of an invalid type. (The class " + type + " doesn't have a no-argument constructor)", e);
            } catch (SecurityException e) {
                logger.error("Stubs cache manager is of an invalid type. (Couln't access " + type + " no-argument constructor)", e);
                throw new RuntimeException("Stubs cache manager is of an invalid type. (Couln't access " + type + " no-argument constructor)", e);
            } catch (ClassCastException e) {
                logger.error("Stubs cache manager is of an invalid type. (The class " + type + " is not a subclass of ve.usb.jgm.client)", e);
                throw new RuntimeException("Stubs cache manager is of an invalid type. (The class " + type + " is not a subclass of ve.usb.jgm.client)", e);
            } catch (InstantiationException e) {
                logger.error("Stubs cache manager is of an invalid type. (The class " + type + " couldn't be instantiated)", e);
                throw new RuntimeException("Stubs cache manager is of an invalid type. (The class " + type + " couldn't be instantiated)", e);
            } catch (IllegalAccessException e) {
                logger.error("Stubs cache manager is of an invalid type. (Couln't access " + type + " no-argument constructor)", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (Couln't access " + type + " no-argument constructor)", e);
            } catch (InvocationTargetException e) {
                logger.error("Stubs cache manager is of an invalid type. (No-argument constructor of " + type + " threw an exception)", e);
                throw new RuntimeException("Bytecode cache manager is of an invalid type. (No-argument constructor of " + type + " threw an exception)", e);
            } catch (ClassNotFoundException e) {
                logger.error("Stubs cache manager is of an invalid type. (The class " + type + " couldn't be found)", e);
                throw new RuntimeException("Stubs cache manager is of an invalid type. (The class " + type + " couldn't be found)", e);
            } catch (StubsCacheManagerConfigurationException e) {
                logger.error("Stubs cache manager is of an invalid type. (Errors configuring " + type + ")", e);
                throw new RuntimeException("Stubs cache manager is of an invalid type. (Errors configuring " + type + ")", e);
            } catch (StubsCacheManagerInitializationException e) {
                logger.error("Stubs cache manager is of an invalid type. (Unable to initialize " + type + ")", e);
                throw new RuntimeException("Stubs cache manager is of an invalid type. (Unable to initialize " + type + ")", e);
            }


            logger.debug("Configured cache managers sucessfully loaded from configuration file");
        
        } catch (XMLLightException e) {
            logger.error("Errors parsing configuration file", e);
            throw new RuntimeException("Errors parsing configuration file", e);
        }
        
    }
    
    public static BytecodeCacheManager makeBytecodeCacheManager() {
        return bytecodeCacheManager;
    }
    
    public static StubsCacheManager makeStubsCacheManager() {
        return stubsCacheManager;
    }
       
}
