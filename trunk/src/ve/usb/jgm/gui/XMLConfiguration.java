 /*
 * XMLConfiguration.java
 *
 * Created on 1 de agosto de 2006, 10:29 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ve.usb.jgm.gui;

import com.softcorporation.xmllight.*;
import java.io.*;
import java.util.*;
import ve.usb.jgm.util.XMLLightUtil;
import java.lang.*;

/**
 *
 * @author karyn
 */
public class XMLConfiguration {
    
    /** Creates a new instance of XMLConfiguration */
    public XMLConfiguration() {
    }
    
    public static void JdmProjectXML(String XMLPath, String nameApp, String descApp, Vector JdmDepends) throws IOException, XMLLightException{
        
               
        String filename = XMLPath;
        
        Element jdmproject = new Element("jdm-project");
        jdmproject.setAttr("name", nameApp);
        jdmproject.setAttr("description", descApp);
        
        Element depends = new Element("depends");
        
        for(int i=0; i<JdmDepends.size(); i++){
        
                       
            String fullLibrary = (String)JdmDepends.elementAt(i);
            
            String[] n = fullLibrary.split(" ");
                        
            String[] majorminor =  Browser.breakString(n[1]);
                   
            Element library = new Element("library");
                            
            library.setAttr("name", n[0]);
            library.setAttr("majorVersion", majorminor[0]);
            library.setAttr("minorVersion", majorminor[1]);
            
            depends.addElem(library);
        
        }
        
        jdmproject.addElem(depends);
        
              
        XMLLightUtil.writeXMLFile(filename, jdmproject);
        
     }
    
    public static Object[] readJdmProjectXML(String XMLPath) throws JdmGuiException{
    
         Vector libraries = new Vector(); 
         String nameApp = null;
         String descApp = null;
         String name = null;
         String minorVersion = null;
         String majorVersion = null;
         Object[] object = new Object[3]; 
         
         String filename = XMLPath;
         
         try{
            Element root = XMLLightUtil.readXMLFile(filename);
            
            Properties att = root.getAttributes();
     
            for (Enumeration e = att.keys(); e.hasMoreElements();){
                String key = (String) e.nextElement();
                String val = att.getProperty(key);
                if (key.compareTo("name")==0)
                        nameApp = val;
                if (key.compareTo("description")==0)
                        descApp = val;
            }
            
            Element depends = root.getElem("library");
            while (!depends.isNull()){
                Properties attr = depends.getAttributes();
                for (Enumeration e = attr.keys(); e.hasMoreElements();){
                    String key = (String) e.nextElement();
                    String val = attr.getProperty(key);
                    if (key.compareTo("name")==0)
                        name = val;                   
                    if (key.compareTo("minorVersion")==0)
                        minorVersion = val;
                    if (key.compareTo("majorVersion")==0)
                        majorVersion = val;                    
                }
                
                String library = name.concat(" ").concat(majorVersion).concat(".").concat(minorVersion);
                
                libraries.addElement(library);
                
                depends = root.getElem("library");
            }
            
            System.out.println("Aplication: "+ nameApp + " description: " + descApp);
            
            object[0] = nameApp;
            object[1] = descApp;
            object[2] = libraries;
            
         } catch (XMLLightException e) {
            
            throw new JdmGuiException("XML parsing error");
         } catch (Exception e) {}
         
         return object;
    }
    
    public static void JdmConfigXML(Vector Repositories, String _bytecode, String _stub, String _prefetch) throws IOException, XMLLightException{
    
        String filename = "/home/karyn/Desktop/jgm-config.xml";
        
        Element jgmconfig = new Element("jgm-config");
        
        Element repositories = new Element("repositories");
        
        for (int i=0; i<Repositories.size(); i++){
            
            RepositoryObject repositorio = (RepositoryObject)Repositories.elementAt(i);
            Element repo = new Element("repo");
            repo.setAttr("name",repositorio.getname());
            repo.setAttr("type", repositorio.gettype());
            repo.setAttr("priority", repositorio.getpriority());
            Element url = new Element("url");
            url.setAttr("value", repositorio.getURL());
            repo.addElem(url);
            repositories.addElem(repo);
            
        }
        
        Element cache = new Element("cache");
        
        Element bytecode = new Element("bytecode");
        bytecode.setAttr("type", _bytecode);
        
        cache.addElem(bytecode);
        
        Element stubs = new Element("stubs");
        stubs.setAttr("type", _stub);
                
        cache.addElem(stubs);
        
        Element prefetch = new Element("prefetch");
        prefetch.setAttr("type", _prefetch);
        prefetch.setAttr("thereshold", "50");
        
        jgmconfig.addElem(repositories);
        jgmconfig.addElem(cache);
        jgmconfig.addElem(prefetch);
        
        
        XMLLightUtil.writeXMLFile(filename, jgmconfig);
        
    }
    
    //static ConfigObject config = new ConfigObject();
    
     public static Object[] readJdmConfigXML() throws JdmGuiException{
    
         Vector repositories = new Vector(); 
         String name = null;
         String type = null;
         String priority = null;
         String urlValue = null;
         String typeBytecode = null;
         String typeStub = null;
         String typePrefetch = null;
         Object[] object = new Object[4]; 
         
         String filename = System.getenv("HOME").concat("/.jgm/jgm-config.xml");

         try{
            Element root = XMLLightUtil.readXMLFile(filename);
            
            Element depends = root.getElem("repo");

            while (!depends.isNull()){
                Properties attr = depends.getAttributes();
              
                for (Enumeration e = attr.keys(); e.hasMoreElements();){
                    String key = (String) e.nextElement();
                    String val = attr.getProperty(key);
                    if (key.compareTo("name")==0)
                        name = val;                   
                    if (key.compareTo("type")==0)
                        type = val;
                    if (key.compareTo("priority")==0)
                        priority = val;   
                }
                
              
                
                Element url = depends.getElem("url");
                while(!url.isNull()){
                     attr = url.getAttributes();
                     
                     for (Enumeration e = attr.keys(); e.hasMoreElements();){
                        String key = (String) e.nextElement();
                        String val = attr.getProperty(key);
                        if (key.compareTo("value")==0)
                            urlValue = val;             
                    }
                    url = depends.getElem("url");
                }
              
                Integer prior = new Integer(priority);
                //config.addRepository(name, type, priority, urlValue);
                RepositoryObject repository = new RepositoryObject(name, type, prior, urlValue);
                repositories.addElement(repository); 
                
                depends = root.getElem("repo");
                 
            }
            object[0] = repositories;
            
            depends = root.getElem("bytecode");
            
            while (!depends.isNull()){
                Properties attr = depends.getAttributes();
                
                for (Enumeration e = attr.keys(); e.hasMoreElements();){
                    String key = (String) e.nextElement();
                    String val = attr.getProperty(key);
                    if (key.compareTo("type")==0)
                        typeBytecode = val;
                }
                
                depends = root.getElem("bytecode");
                
            }
            
            object[1] = typeBytecode;
            
            depends = root.getElem("stubs");
            
            while (!depends.isNull()){
                Properties attr = depends.getAttributes();
              
                for (Enumeration e = attr.keys(); e.hasMoreElements();){
                    String key = (String) e.nextElement();
                    String val = attr.getProperty(key);
                    if (key.compareTo("type")==0)
                        typeStub = val;                   
                }
                
                depends = root.getElem("stubs");
                
            }
            
            object[2] = typeStub;
            
            depends = root.getElem("prefetch");
            
            while (!depends.isNull()){
                Properties attr = depends.getAttributes();
        
                for (Enumeration e = attr.keys(); e.hasMoreElements();){
                    String key = (String) e.nextElement();
                    String val = attr.getProperty(key);
                    if (key.compareTo("type")==0)
                        typePrefetch = val;                                     
                }
                
                depends = root.getElem("prefetch");
                
            }
            object[3] = typePrefetch;
           
            
         } catch (XMLLightException e) {
            
            throw new JdmGuiException("XML parsing error");
         } catch (Exception e) {}
         
         return object;
    }
}    
