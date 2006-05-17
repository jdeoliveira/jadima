/*
 * EHCacheStubsCacheManager.java
 *
 * Created on 9 de junio de 2005, 03:11 AM
 */

package ve.usb.jgm.client.cache.ehcache;

import ve.usb.jgm.client.*;
import ve.usb.jgm.repo.*;
import java.net.*;
import net.sf.ehcache.*;
import com.softcorporation.xmllight.*;
import org.apache.log4j.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class EHCacheBytecodeCacheManager extends BytecodeCacheManager {
    
    private HashMap<String, Bytecode> backgroundRequests;
    
    private static final Logger logger = Logger.getLogger(EHCacheStubsCacheManager.class);
    private static final Logger cacheLogger = Logger.getLogger("ve.usb.jgm.client.cache.stubs");
    
    private BackgroundRequestorThread thread;
    
    private Cache cache;
    
    /** Creates a new instance of EHCacheStubsCacheManager */
    public EHCacheBytecodeCacheManager() {
        //no-argument constructor
    }
    
    public void configure(com.softcorporation.xmllight.Element el) throws BytecodeCacheManagerConfigurationException {
        try {
            logger.debug("Starting EHCache bytecode cache manager");
            
            //TODO: tomar esto del element
            long timeToLive = 1800;
            long expirerInterval = 300;
            
            logger.debug("Time to live set to " + timeToLive + " seconds");
            logger.debug("Expirer thread interval set to " + expirerInterval + " seconds");
            
            String cacheDir = System.getProperty("user.home") + File.separator + ".jgm" + File.separator + "ehcache" + File.separator + "bytecode";
            logger.debug("Cache directory set to " + cacheDir);
            String oldValue = System.getProperty("java.io.tmpdir");
            System.setProperty("java.io.tmpdir", cacheDir);
            
            URL url = getClass().getResource("bytecode-cache.xml");
            logger.debug("Reading ehcache configuration file from " + url);
            CacheManager manager = CacheManager.create(url);
            
            logger.debug("Creating stubs cache instance");
            cache = new Cache("bytecode", 0, true, false, timeToLive, 0, true, expirerInterval);
            
            logger.debug("Adding cache bytecode instance to cache manager");
            manager.addCache(cache);
            
            logger.debug("EHCache bytecode cache manager successfuly configured");
            
            System.setProperty("java.io.tmpdir", oldValue);
        
        } catch (CacheException e) {
            logger.error("Cache initialization exception", e);
            throw new BytecodeCacheManagerConfigurationException("Cache initialization exception", e);
        }
        /*} catch (XMLLightException e) {
            logger.error("XML parsing error reading cache manager configuration section", e);
            throw new StubsCacheManagerConfigurationException("XML parsing error reading cache manager configuration section", e);
        }*/
    }
    
    public byte[] getClasses(Bytecode mainClass, Collection<Bytecode> relatedClasses) throws ClassNotFoundException {
        logger.debug("Requesting main class: " + mainClass);
        logger.debug("Requesting related classes: " + Arrays.deepToString(relatedClasses.toArray(new Bytecode[0])));
        
        try {
            //este cache manager me crea los jars solicitados en archivos temporales
            
            byte[] result = null;
            
            //Primero chequeamos que la clase principal este en el cache
            logger.debug("Searching " + mainClass + " on the local cache");
            String cid = mainClass.toString();
            net.sf.ehcache.Element el = cache.get(cid);
            if (el != null) {
                //esta, la sacamos
                logger.debug("Cache HIT for " + mainClass);
                
                //guardamos en el profile
                PrefetchCacheProfile.instance().addCacheHit(mainClass.toString());
                
                result = ((Bytecode)el.getValue()).getClassData();
                
                logger.debug("Data obtained from cache is " + result);
                
            } else {
                
                //no esta, 
                logger.debug("Cache MISS for " + mainClass);
                
                PrefetchCacheProfile.instance().addCacheMiss(mainClass.toString());
                
                //determinamos si la clase principal esta siendo cargada
                //por el thread background
                logger.debug("Checking if main class is already being requested");

                if (backgroundRequests.containsKey(mainClass.toString())) {
                        //si, entonces debemos esperar por el codigo real
                        logger.debug("Yes, it is, checking if is already downloaded");
                        //Verificamos si no habra llegado ya
                        Bytecode cdata = backgroundRequests.get(mainClass.toString());
                        logger.debug("cdata=" + cdata);
                        synchronized(cdata) {
                            while (cdata.getClassData() == null) {
                                logger.debug("Not yet here, let's wait");
                                try {

                                    //No ha llegado, esperamos hasta que el otro thread (el 
                                    //background notifique que llego algo)
                                    cdata.wait();

                                } catch (InterruptedException e) {
                                    //El thread background notifico que llego algo,
                                    //volvemos a chequear si llego lo que estamos esperando
                                    logger.debug("Someone waked me, rechecking if " + mainClass + " has arrived");
                                }
                            }
                        }
                        logger.debug("Yes, it's here. Taking it.");
                        result = backgroundRequests.get(mainClass.toString()).getClassData();
                        synchronized(backgroundRequests) {
                            backgroundRequests.remove(mainClass.toString());
                            backgroundRequests.notifyAll();
                        }
                        
                        PrefetchCacheProfile.instance().addPrefetchedClass(mainClass.toString());
                } else {
                    //no, la solicitamos
                    logger.debug("No, it's not being requested. Let's request it");

                    PrefetchCacheProfile.instance().addIndividuallyLoadedClass(mainClass.toString());
                    
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
                                result = theMainClass.getClassData();
                            } else {
                                logger.debug("Not found, trying next repo");
                            }
                        } catch (Exception e) {
                            logger.warn("Exception communicating with repository, trying next", e);
                        }
                    }

                    //si caemos aqui es porque no se encontro en ningun repositorio
                    if (result == null) {
                        logger.debug("Class " + mainClass.getClassName() + " " + mainClass.getMajorVersion() + "." + mainClass.getMinorVersion() + " not found on cache or configured repositories");
                        throw new ClassNotFoundException("Class " + mainClass.getClassName() + " " + mainClass.getMajorVersion() + "." + mainClass.getMinorVersion() + " not found on cache or configured repositories");
                    }
                }
            } 
            
            //agregamos las related classes al mapa para que el thread las busque
            logger.debug("Adding related classes to background requests map");
            synchronized(backgroundRequests) {
                logger.debug("Monitor aquiered on backgroundRequests");
                for (Bytecode b: relatedClasses) {
                    if (!(backgroundRequests.containsKey(b.toString()))) {
                        logger.debug("Adding " + b + " to the background requests map");
                        backgroundRequests.put(b.toString(), b);
                    }
                }
                logger.debug("Releasing monitor on backgroundRequests");
                backgroundRequests.notifyAll();
            }
            logger.debug("Done adding related classes to background requests map");

            mainClass.setClassData(result);
            logger.info("Storing " + mainClass + " on cache (" + mainClass.getClass().toString() + ")");
            net.sf.ehcache.Element newEl = new net.sf.ehcache.Element((Serializable)(mainClass.toString()), (Serializable)mainClass);
            cache.put(newEl);
            
            Thread.currentThread().yield();
            
            return result;

        } catch (Throwable e) {
            logger.fatal("Unexpected exception caught", e);
            throw new RuntimeException("Unexpected exception caught", e);
        }
    }
    
    public void init() throws BytecodeCacheManagerInitializationException {
        backgroundRequests = new HashMap<String, Bytecode>();
        
        //levantamos el thread background
        thread = new BackgroundRequestorThread(backgroundRequests, repos);
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }
}
