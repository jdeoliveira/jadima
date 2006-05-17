/*
 * NoStubsCacheManager.java
 *
 * Created on 19 de mayo de 2005, 12:51 PM
 */

package ve.usb.jgm.client.cache;

import ve.usb.jgm.client.*;
import java.util.*;
import java.io.*;
import ve.usb.jgm.repo.*;
import com.softcorporation.xmllight.*;
import org.apache.log4j.*;
/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class NoStubsCacheManager extends StubsCacheManager {
    
    private static final Logger logger = Logger.getLogger(StubsCacheManager.class);
    
    /** Creates a new instance of NoStubsCacheManager */
    public NoStubsCacheManager() {
    }

    public void configure(Element el) throws StubsCacheManagerConfigurationException {
    }

    public Collection<File> getStubs(Collection<Version> versions) throws VersionNotFoundException {
        
        //este cache manager me crea los jars solicitados en archivos temporales
        //que se van a borrar eventualmente

        Collection<Version> requestedVersions = new HashSet<Version>(versions);
        Collection<Version> result = new HashSet<Version>();
        
        //pedirle cada repo todas las versiones
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
                        
                        //lo agregamos al bag
                        result.add(stubVersion);
                    }
                } else {
                    logger.debug("None found, trying next repo");
                }
            } catch (Exception e) {
                logger.warn("Exception communicating with repository, trying next", e);
            }
        }
        
        //ya tenemos en result todo lo que se encontro
        
        //verificamos que no quede nada pendiente en requestedVersions
        if (requestedVersions.size() > 0) {
            throw new VersionNotFoundException("Impossible to obtain stubs for the following libraries: " + Arrays.deepToString(requestedVersions.toArray(new Version[0])));
        }
        
        HashSet<File> files = new HashSet<File>();
        
        //lo tenemos todo, entonces bajamos los stubs a disco y devolvemos las referecias a los File's correspondientes
        for (Version v: result) {
            logger.debug("Storing " + v);
            
            FileOutputStream fos = null;
            try {
                logger.debug("version stub bytecode size is: " + v.getStubsJar().length);
                //guardar los bytes en un temp file
                File f = File.createTempFile("jdm-stub", ".jar");
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
        
        return files;
    }

    public void init() throws StubsCacheManagerInitializationException {
    }
    
}
