/*
 * PrefetchCacheProfile.java
 *
 * Created on 1 de abril de 2006, 16:40
 */

package ve.usb.jgm.client;

import java.util.*;
import org.apache.log4j.*;

/**
 *
 * @author  jdeoliveira
 */
public class PrefetchCacheProfile {
    
    private static final Logger logger = Logger.getLogger(PrefetchCacheProfile.class);
    
    //Accesos al repositorio
    private Collection<String> repoAccesses;
    
    // clases encontradas en el cache
    private Collection<String> cacheHits;
    
    // clases no encontradas en el cache
    private Collection<String> cacheMisses;
    
    // clases que al momento de su solicitud ya estaban precargadas
    private Collection<String> prefetchedClasses;
    
    // clases cargadas individualmente
    private Collection<String> individuallyLoadedClasses;
    
    private static PrefetchCacheProfile singleton;
    
    /** Creates a new instance of PrefetchCacheProfile */
    public PrefetchCacheProfile() {
        repoAccesses = new LinkedList();
        cacheHits = new LinkedList();
        cacheMisses = new LinkedList();
        prefetchedClasses = new LinkedList();
        individuallyLoadedClasses = new LinkedList();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                //dump del profile a STDOUT
                logger.info("Prefetch and Cache modules execution profile");
                logger.info("--------------------------------------------");
                
                logger.info("Cache hits: " + cacheHits.size());
                logger.debug("\t" + cacheHits);
                logger.info("Cache misses: " + cacheMisses.size());
                logger.debug("\t" + cacheMisses);
                logger.info(" ");
                
                logger.info("Prefetched classes: " + prefetchedClasses.size());
                logger.debug("\t" + prefetchedClasses);
                logger.info("Non-prefetched classes: " + individuallyLoadedClasses.size());
                logger.debug("\t" + individuallyLoadedClasses);
                logger.info(" ");
                
                logger.info("Remote repositories accesses: " + repoAccesses.size());
                logger.debug("\t" + repoAccesses);



		System.out.println("Prefetch and Cache modules execution profile");
                System.out.println("--------------------------------------------");
                
                System.out.println("Cache hits: " + cacheHits.size());
                logger.debug("\t" + cacheHits);
                System.out.println("Cache misses: " + cacheMisses.size());
                logger.debug("\t" + cacheMisses);
                System.out.println(" ");
                
                System.out.println("Prefetched classes: " + prefetchedClasses.size());
                logger.debug("\t" + prefetchedClasses);
                System.out.println("Non-prefetched classes: " + individuallyLoadedClasses.size());
                logger.debug("\t" + individuallyLoadedClasses);
                System.out.println(" ");
                
                System.out.println("Remote repositories accesses: " + repoAccesses.size());
                logger.debug("\t" + repoAccesses);
            }
        });
    }
    
    public static PrefetchCacheProfile instance() {
        if (singleton == null) {
            singleton = new PrefetchCacheProfile();
        }
        return singleton;
    }
    
    public void addCacheHit(String classname) {
        cacheHits.add(classname);
    }
    
    public void addCacheMiss(String classname) {
        cacheMisses.add(classname);
    }
    
    public void addPrefetchedClass(String classname) {
        prefetchedClasses.add(classname);
    }
    
    public void addIndividuallyLoadedClass(String classname) {
        individuallyLoadedClasses.add(classname);
    }
    
    public void addRepoAccess(String classnames) {
        repoAccesses.add(classnames);
    }
    
}
