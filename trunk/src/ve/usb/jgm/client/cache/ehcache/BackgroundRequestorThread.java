/*
 * BackgroundRequestorThread.java
 *
 * Created on 1 de julio de 2005, 0:31
 */

package ve.usb.jgm.client.cache.ehcache;

import java.util.*;
import ve.usb.jgm.repo.*;
import ve.usb.jgm.client.*;
import org.apache.log4j.*;

/**
 *
 * @author  jdeoliveira
 */
public class BackgroundRequestorThread extends Thread {
    
    private HashSet<String> alreadyLoadedClasses;
    
    private static final Logger logger = Logger.getLogger(BackgroundRequestorThread.class);
    
    public boolean go = true;
    
    private HashMap<String, Bytecode> requests;
    private Collection<RepositoryClient> repos;
    
    /** Creates a new instance of BackgroundRequestorThread */
    public BackgroundRequestorThread(HashMap<String, Bytecode> _requests, Collection<RepositoryClient> _repos) {
        requests = _requests;
        alreadyLoadedClasses = new HashSet<String>();
    }
    
    public void run() {
        logger.debug("Starting background requestor thread");
        
        
        boolean notDone = true;
        while (notDone) {
            Collection<RepositoryClient> losRepos = RepositoryClientFactory.makeRepositoryClients();
            if (losRepos == null) {
                try {
                    logger.debug("Repositories not configured yet. Sleeping 10 sec.");
                    yield();
                    sleep(10000);
                } catch (InterruptedException e) {
                    logger.debug("I'm awakened, rechecking configured repos");
                }
            } else {
                logger.debug("Repositories configured, loaded");
                notDone = false;
                synchronized(losRepos) {
                    repos = new LinkedList<RepositoryClient>(losRepos);
                    losRepos.notifyAll();
                } 
            }
        }
        
        
        
        //todo: poner condicion para terminar el thread
        while (true) {
            logger.debug("Cloning requests map");
            
            HashMap<String,Bytecode> myReqs = null;
            synchronized(requests) {
                logger.debug("I got the monitor on requests");
                myReqs = (HashMap<String,Bytecode>)requests.clone();
                logger.debug("Released monitor on requests");
                requests.notifyAll();
            }
            
            logger.debug("Requests map cloned");
            

            if (myReqs.isEmpty()) {
                logger.debug("No requests yet, sleeping");
                //esta vacio, esperamos
                try {
                    yield();
                    sleep(1000);
                } catch (InterruptedException e) {
                    logger.debug("Someone waked me, rechecking requests map");
                }
            } else {
                //iteramos una por una las solicitudes, y las pedimos al repo broker
                logger.debug("There are pending requests, attending them");

                HashSet<Bytecode> classes = new HashSet<Bytecode>();

                logger.debug("Starting iteration over cloned requests map");
                Set<Map.Entry<String, Bytecode>> set = myReqs.entrySet();
                logger.debug("This is the entry set: " + set.toString()  + " (size:" + set.size() + ")");
                
                Iterator<Map.Entry<String, Bytecode>> it = set.iterator();
                
                while(it.hasNext()) {
                    Map.Entry<String, Bytecode> req = it.next();
                    if (!(alreadyLoadedClasses.contains(req.getValue().toString()))) {
                        logger.debug("Adding " + req.getValue().toString());
                        classes.add(req.getValue());
                        logger.debug("Done Adding " + req.getValue().toString());
                    }
                }

                logger.debug("Requesting all to all repos:");
                //logger.debug(Arrays.deepToString(repos.toArray()));

                Iterator<RepositoryClient> it2 = repos.iterator();
                while(it2.hasNext()) {
                    RepositoryClient c = it2.next();
                    logger.debug("trying " + c.getName());

                    try {

                        Collection<Bytecode> resp = c.requestClasses(classes);
                        if (resp.size() > 0) {
                            //encontre algunas, las saco de la lista original de solicitudes

                            Iterator<Bytecode> it3 = resp.iterator();
                            while (it3.hasNext()) {
                                Bytecode b = it3.next();
                                
                                //la saco de la lista original
                                classes.remove(b);

                                logger.debug("Class " + b + " found");

                                if (requests.containsKey(b.toString())) {
                                    Bytecode r = requests.get(b.toString());
                                    synchronized(r) {
                                        //ahora si alertamos a los otros threads de que tenemos resultados
                                        r.setClassData(b.getClassData());
                                        r.setRevision(b.getRevision());
                                        alreadyLoadedClasses.add(r.toString());
                                        logger.debug("Notifying other threads about request map filled");
                                        r.notifyAll();
                                    }
                                }
                            }

                        } else {
                            logger.debug("Not found, trying next repo");
                        }
                    } catch (Exception e) {
                        logger.warn("Exception communicating with repository, trying next");
                        logger.debug("Exception communicating with repository, trying next", e);
                    }
                }
                
                logger.debug("Done requesting all to all repos");

                if (classes.size() > 0) {
                    logger.debug("The following classes couln't be found on cache or configured repositories: " + Arrays.deepToString(classes.toArray(new Bytecode[0])));
                    throw new RuntimeException("The following classes couln't be found on cache or configured repositories: " + Arrays.deepToString(classes.toArray(new Bytecode[0])));
                }

                
            }
        }
    }
    
}
