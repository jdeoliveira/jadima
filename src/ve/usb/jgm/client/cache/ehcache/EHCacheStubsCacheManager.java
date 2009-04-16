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
public class EHCacheStubsCacheManager extends StubsCacheManager {
    
    private static final Logger logger = Logger.getLogger(EHCacheStubsCacheManager.class);
    private static final Logger cacheLogger = Logger.getLogger("ve.usb.jgm.client.cache.stubs");
    
    private Cache cache;
    
    /** Creates a new instance of EHCacheStubsCacheManager */
    public EHCacheStubsCacheManager() {
        //no-argument constructor
    }
    
    public void configure(com.softcorporation.xmllight.Element el) throws StubsCacheManagerConfigurationException {
        try {
            logger.debug("Starting EHCache stubs cache manager");
            
            //TODO: tomar esto del element
            long timeToLive = 1800;
            long expirerInterval = 300;
            
            logger.debug("Time to live set to " + timeToLive + " seconds");
            logger.debug("Expirer thread interval set to " + expirerInterval + " seconds");
            
            String cacheDir = System.getProperty("user.home") + File.separator + ".jgm" + File.separator + "ehcache" + File.separator + "stubs";
            logger.debug("Cache directory set to " + cacheDir);
            String oldValue = System.getProperty("java.io.tmpdir");
            System.setProperty("java.io.tmpdir", cacheDir);
            
            URL url = getClass().getResource("stubs-cache.xml");
            logger.debug("Reading ehcache configuration file from " + url);
            CacheManager manager = CacheManager.create(url);
            
            logger.debug("Creating stubs cache instance");
            cache = new Cache("stubs", 0, true, false, timeToLive, 0, true, expirerInterval);
            
            logger.debug("Adding cache stubs instance to cache manager");
            manager.addCache(cache);
            
            logger.debug("EHCache stubs cache manager successfuly configured");
            
            System.setProperty("java.io.tmpdir", oldValue);
        
        } catch (CacheException e) {
            logger.error("Cache initialization exception", e);
            throw new StubsCacheManagerConfigurationException("Cache initialization exception", e);
        }
        /*} catch (XMLLightException e) {
            logger.error("XML parsing error reading cache manager configuration section", e);
            throw new StubsCacheManagerConfigurationException("XML parsing error reading cache manager configuration section", e);
        }*/
    }
    
    public java.util.Collection<java.io.File> getStubs(java.util.Collection<ve.usb.jgm.repo.Version> versions) throws ve.usb.jgm.repo.VersionNotFoundException {
        try {
            long before = System.currentTimeMillis();
            //este cache manager me crea los jars solicitados en archivos temporales
            
            Collection<Version> requestedVersions = new LinkedList<Version>(versions);
            List<Version> result = new LinkedList<Version>();
            
            Collection<Version> firstRequestedVersions = new LinkedList<Version>(versions);
            
            //Primero buscamos en el cache si tengo lo que necesito
            for (Version v: firstRequestedVersions) {
                String verid = v.getLibraryName() + "-" + v.getNumberMajor() + "." + v.getNumberMinor();
                cacheLogger.info("Searching " + verid);
                net.sf.ehcache.Element el = cache.get(verid);
                if (el != null) {
                    cacheLogger.info("Cache HIT for " + verid);
                    //como ya esta en el cache, la sacamos del requested versions
                    Version ver = (Version)el.getValue();
                    result.add(ver);
                    requestedVersions.remove(v);
                } else {
                    cacheLogger.info("Cache MISS for " + verid);
                }
            }
            
            //pedirle cada repo todas las versiones que faltan (que no estan en el cache)
            if (requestedVersions.size() > 0) {
                long before = System.currentTimeMillis();
                for (RepositoryClient c: repos) {
                    logger.debug("trying " + c.getName());
                    try {
                        Collection<Version> resp = c.requestStubs(requestedVersions);
                        if (resp.size() > 0) {
                            //la encontre, la devuelvo de una
                            Iterator<Version> it = resp.iterator();
                            while (it.hasNext()) {
                                Version stubVersion = it.next();
                                logger.debug("Stubs " + stubVersion.getLibraryName() + "-" + stubVersion.getNumberMajor() + "." + stubVersion.getNumberMinor() + " found");

                                logger.debug("requested versions is: " + Arrays.deepToString(requestedVersions.toArray(new Version[0])));
                                //lo sacamos de las que necesitamos
                                requestedVersions.remove(stubVersion);

                                logger.debug("requested versions is: " + Arrays.deepToString(requestedVersions.toArray(new Version[0])));

                                //Lo mandamos al cache
                                cacheLogger.info("Storing " + stubVersion.getLibraryName() + "-" + stubVersion.getNumberMajor() + "." + stubVersion.getNumberMinor());
                                net.sf.ehcache.Element newEl = new net.sf.ehcache.Element(stubVersion.getLibraryName() + "-" + stubVersion.getNumberMajor() + "." + stubVersion.getNumberMinor(), stubVersion);
                                cache.put(newEl);

                                //lo agregamos al bag
                                result.add(stubVersion);
                            }
                        } else {
                            logger.debug("None found, trying next repo");
                        }
                    } catch (Exception e) {
                        logger.warn("Exception communicating with repository " + c.getName() + ", trying next");
                        logger.debug("Exception communicating with repository " + c.getName() + ", trying next", e);
                    }
                }
            }
            
            //ya tenemos en result todo lo que se encontro
            
            //verificamos que no quede nada pendiente en requestedVersions
            if (requestedVersions.size() > 0) {
                throw new VersionNotFoundException("Impossible to obtain stubs for the following libraries: " + Arrays.deepToString(requestedVersions.toArray(new Version[0])));
            }
            
            LinkedList<File> files = new LinkedList<File>();
            
            //ordenamos las versiones de acuerdo a la prioridad
            logger.debug("Sorting found stub on priority");
            
            Collections.sort(result, new Comparator<Version>() {
               
                public int compare(Version v1, Version v2) {
                    if (v1.getPriority() < v2.getPriority()) {
                        return -1;
                    } else if (v1.getPriority() == v2.getPriority()) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
                
            });
            
            //lo tenemos todo, entonces bajamos los stubs a disco y devolvemos las referecias a los File's correspondientes
            for (Version v: result) {
                logger.debug("Storing " + v + " (priority: " + v.getPriority() + ")");
                
                FileOutputStream fos = null;
                try {
                    logger.debug("version stub bytecode size is: " + v.getStubsJar().length);
                    //guardar los bytes en un temp file
                    File f = File.createTempFile("jdm-stub", ".jar");
                    logger.debug("Writing " + f.getName() + " for " + v.getLibraryName());
                    f.deleteOnExit();
                    fos = new FileOutputStream(f);
                    fos.write(v.getStubsJar());
                    fos.flush();
                    logger.debug(v + " stored sucessfully");
                    files.add(f);
                } catch (IOException e) {
                    logger.error("Unable to store temporary stub jar file", e);
                } finally  {
                    try { fos.close(); } catch (Exception e) {}
                }
            }
            long after = System.currentTimeMillis();
            double timeTook = after - before;
            cacheLogger.info("Ellapsed time searching for stubs: " + timeTook);   
            
            return files;
        } catch (Throwable e) {
            logger.fatal("Unexpected exception caught", e);
            throw new RuntimeException("Unexpected exception caught", e);
        }
    }
    
    public void init() throws StubsCacheManagerInitializationException {
        //nothing to do here
    }
    
}
