/*
 * JdmRepository.java
 *
 * Created on 17 de agosto de 2006, 01:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ve.usb.jgm.ws.repo;

import java.util.*;
import java.lang.*;
import org.apache.log4j.*;
import com.softcorporation.xmllight.*;
import ve.usb.jgm.client.*;
import java.lang.reflect.*;

/**
 *
 * @author karyn
 */
public class JdmRepository {
    
    /** Creates a new instance of JdmRepository */
    private static final Logger logger = Logger.getLogger(JdmRepository.class);
    
    private static Collection<RepositoryClient> knownRepos;
        
    
    public static Collection<RepositoryClient> knownRepositories(Element repositories){
     
        knownRepos = new LinkedList<RepositoryClient>();
        
        try{
            
            Element repo = repositories.getElem("repo");

            String type;
            String name;
            int priority;
            
            
            synchronized (knownRepos) {
                
                while(!repo.isNull()) {
                    
                    type = repo.getAttr("type");
                    name = repo.getAttr("name");
                                      
                    logger.debug("Instantiating repository " + name + " of type " + type);

                    try {
                        priority = (new Integer(repo.getAttr("priority"))).intValue();
                        
                    } catch (NumberFormatException e) {
                        logger.warn("Repository " + name + " has invalid priority attribute value, assuming priority = 0. Please check te configuration file");
                        priority = 0;
                    }
                    
                    try{
                        
                        RepositoryClient aRepo = (RepositoryClient)Class.forName(type).getConstructor().newInstance();

                        
                        aRepo.setName(name);
                        
                        //Le pasamos su element para que se configure
                        aRepo.configure(repo);
                        aRepo.init();
                        
                        //Lo agregamos a la coleccion
                        knownRepos.add(aRepo);
                        
                                
                    }catch (NoSuchMethodException e) {
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

                    repo = repositories.getElem("repo");
                }
            }
            
        } catch (XMLLightException e) {
            logger.error("XML parsing error", e);
        }
        
        return knownRepos;
    }
    
}
