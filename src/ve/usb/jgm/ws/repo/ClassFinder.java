/*
 * ClassFinder.java
 *
 * Created on November 11, 2006, 12:50 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ve.usb.jgm.ws.repo;


import org.apache.log4j.*;
//import org.apache.log4j.xml.*;
//import java.io.*;
import java.util.*;
//import java.util.zip.*;
//import com.softcorporation.xmllight.*;
//import org.apache.axis.*;
//import org.apache.axis.transport.http.*;
//import javax.servlet.http.*;
//import ve.usb.jgm.repo.backend.*;
import ve.usb.jgm.repo.*;
//import ve.usb.jgm.ws.faults.*;
//import ve.usb.jgm.util.*;
//import ve.usb.jgm.gui.*;
import ve.usb.jgm.client.*;
//import java.lang.reflect.*;
/**
 *
 * @author rafa
 */
public class ClassFinder extends Thread{
    
    private static final Logger logger = Logger.getLogger(RepositoryService.class);
    
    public static Collection<Bytecode> resp = null;
    public static Collection<Bytecode> clasess = null;
    public static Collection<RepositoryClient> knownRepos = null;
    public  int a = 2;
    public boolean stop = false;
    
    public void run(){
        
        logger.warn(">>>>>>>> ENTER run");

        for (RepositoryClient c: knownRepos){
            
            try {
                
                resp = c.requestClasses(clasess);
                logger.info("TENGO LA CLASE");
                if (resp.size() == 0) {
                    
                    logger.debug(">>>>>>>> Not found, trying next repo");
                }
                
            } catch (Exception e) {
                
               logger.warn(">>>>>>>> Exception communicating with repository, trying next", e);
            }
            
            if(stop){
                
                break;
            }
        
        }
        
        //bucle for test the waiting time
        
       /* while(a<9999){
            if(stop){
                break;
            }
            logger.warn(a);
            a++;
        }*/
        synchronized(this){
            this.notifyAll();
        }

       logger.warn(">>>>>>>> EXIT run");
    }
    
  
    public void startSearch(Collection<Bytecode> clas, Collection<RepositoryClient> repo){
        
        logger.warn(">>>>>>>>ENTER startSearch()");
        
        clasess = clas;
        knownRepos = repo;
        start();
        
        logger.warn(">>>>>>>>EXIT startSearch()");
    }
    
}
