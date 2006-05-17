/*
 * Configuration.java
 *
 * Created on 1 de mayo de 2005, 05:45 PM
 */

package ve.usb.jgm.client.config;

import java.util.*;
import ve.usb.jgm.client.XmlConfigurator;
/**
 *
 * @author  Administrator
 */
public class Configuration {
    
    //We are singleton!
    private static Configuration myself;
    
    private Collection<Repository> repos;
    private Cache stubsCache;
    private Cache bytecodeCache;
    private Prefetch prefetch;

    public static Configuration getInstance() throws InvalidConfigurationFileException {
        if (myself == null) {
            //myself = XmlConfigurator.load(System.getProperty("user.home") + System.getProperty("file.separator") + ".jgm" + System.getProperty("file.separator") + "jgm-config.xml");
        }
        return myself;
    }
    
    /** Creates a new instance of Configuration */
    public Configuration() {
        repos = new LinkedList<Repository>();
        stubsCache = new Cache(5000, "ve.usb.jgm.client.cache.LRU");
        bytecodeCache = new Cache(10000, "ve.usb.jgm.client.cache.LRU");
        prefetch = new Prefetch(5);
    }

    public void addRepository(Repository r) {
        repos.add(r);
    }
    
    public void setStubsCache(Cache _cache) {
        stubsCache = _cache;
    }
    
    public void setBytecodeCache(Cache _cache) {
        bytecodeCache = _cache;
    }
    
    public Collection<Repository> getRepositories() {
        return repos;
    }
    
    public Cache getStubsCache() {
        return stubsCache;
    }
    
    public Cache getBytecodeCache() {
        return bytecodeCache;
    }
    
    public void setPrefetch(Prefetch _prefetch) {
        prefetch = _prefetch;
    }
    
    public Prefetch getPrefetch() {
        return prefetch;
    }
    
    public void removeRepository(Repository r) {
        repos.remove(r);
    }
}
