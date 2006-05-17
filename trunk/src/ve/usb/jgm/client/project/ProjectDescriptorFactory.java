/*
 * XmlConfigurator.java
 *
 * Created on 4 de mayo de 2005, 11:48 PM
 */

package ve.usb.jgm.client.project;

import com.softcorporation.xmllight.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import ve.usb.jgm.util.*;

/**
 *
 * @author  Administrator
 */
public class ProjectDescriptorFactory {
    
    private static Logger logger = Logger.getLogger(ProjectDescriptorFactory.class);
    
    /*
     * Loads the configuration from the xml file pointed by the fileName
     * parameter
     */
    public static ProjectDescriptor load(InputStream in) throws InvalidProjectDescriptorFileException {
        try {
            ProjectDescriptor project = new ProjectDescriptor();
            
            byte[] buffer = InputStreamUtil.readAll(in);
            in.close();
            
            String doc = new String(buffer);
            
            // need to clear comments in input document!
            String clearDoc = XMLLight.clearComments(doc);
            
            // reading root element.
            Element elemRoot = XMLLight.getElem(clearDoc, "jgm-project");
            
            // check that Element exists in XML document
            if (elemRoot.isNull()) {
                logger.error("Root element is null");
                throw new InvalidProjectDescriptorFileException("Root element is null");
            }
            
            project.setName(elemRoot.getAttr("name"));
            project.setDescription(elemRoot.getAttr("description"));
            
            //Obtener configuracion de dependencias
            
            Element dependencies = elemRoot.getElem("depends");
            String name;
            int major;
            int minor;
            int priority;
            Element dep = dependencies.getElem("library");
            while(!dep.isNull()) {
                name = dep.getAttr("name");
                major = (new Integer(dep.getAttr("majorVersion"))).intValue();
                minor = (new Integer(dep.getAttr("minorVersion"))).intValue();
                
                try {
                    //cargamos la prioridad (opcional)
                    priority = (new Integer(dep.getAttr("priority"))).intValue();
                } catch (NumberFormatException e) {
                    //si no esta en el xml, le asginamos el valor maximo.
                    logger.info("Dependency " + name + " has invalid or no prioririty, assumming Integer.MAX_VALUE");
                    priority = Integer.MAX_VALUE;
                }
                    
                
                logger.debug("Adding dependency '" + name + "' to project descriptor");
                
                Dependency d = new Dependency(name, major, minor, 0);
                
                project.addDependency(d);
                
                dep = dependencies.getElem("library");
            }
            
            return project;
            
        } catch (XMLLightException e) {
            logger.error("Error parsing xml document", e);
            throw new InvalidProjectDescriptorFileException("Error parsing xml document", e);
        } catch (IOException e) {
            logger.error("Error reading project descriptor file", e);
            throw new InvalidProjectDescriptorFileException("Error reading project descriptor file", e);
        } catch (NumberFormatException e) {
            logger.error("Error reading project descriptor file (some number is invalid)", e);
            throw new InvalidProjectDescriptorFileException("Error reading project descriptor file (some number is invalid)", e);
        } catch (NullPointerException e) {
            logger.error("Error reading project descriptor file (something is missing)", e);
            throw new InvalidProjectDescriptorFileException("Error reading project descriptor file (something is missing)", e);
        }
    }
    
    /*
     * Saves the project file
     */
    public static void save(ProjectDescriptor project, String fileName) {
        
        Writer outFile = null;
        
        try {
            Element rootEl = new Element("jgm-project");
            
            Element depends = new Element("depends");
            
            Iterator<Dependency> it = project.getDependencies().iterator();
            while (it.hasNext()) {
                Dependency d = it.next();
                Element dep = new Element("library");
                dep.setAttr("name", d.getLibraryName());
                dep.setAttr("majorVersion", d.getMajorVersion());
                dep.setAttr("minorVersion", d.getMinorVersion());
                depends.addElem(dep);
            }
            
            rootEl.addElem(depends);
            
            //ya lo construimos, ahora lo metemos en el archivo
            String xmlDocument = XMLLight.getXMLDocument(rootEl);
            
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            file.createNewFile();
            outFile = new FileWriter(file);
            PrintWriter out = new PrintWriter(new BufferedWriter(outFile));
            
            out.println(xmlDocument);
            
            out.flush();
            out.close();
            outFile.close();
            
        } catch (IOException e) {
            logger.error("Error writing project descriptor file", e);
        }
    }
    
}
