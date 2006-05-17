/*
 * XMLLightUtil.java
 *
 * Created on 30 de mayo de 2005, 10:42 AM
 */

package ve.usb.jgm.util;

import com.softcorporation.xmllight.*;
import java.io.*;
import org.apache.log4j.*;

/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class XMLLightUtil {
    
    private static final Logger logger = Logger.getLogger(XMLLightUtil.class);
    
    /**
     * Replaces the first ocurrence of the element named newElem.getName()
     * on the rootEl element
     */
    public static Element replaceElem(Element rootEl, Element newElem) throws XMLLightException {
        Element actualEl = null;
        Element finalEl = new Element(rootEl.getName());
        rootEl.resetPosition();
        actualEl = rootEl.getElem();
        while (!(actualEl.isNull())) {
            if (actualEl.getName().equals(newElem.getName())) {
                //este es el elemento que buscan, lo reemplazamos
                finalEl.addElem(newElem);
            } else {
                //no es el que buscan, metemos el original
                finalEl.addElem(actualEl);
            }
            actualEl = rootEl.getElem();
        } 
        return finalEl;
    }

    public static Element readXMLFile(String fileName) throws IOException, XMLLightException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(fileName));
            StringBuffer document = new StringBuffer("");
            String aLine = null;
            while ((aLine = in.readLine()) != null) {
                document.append(aLine);
            }
            String doc = XMLLight.clearComments(document.toString());
            return XMLLight.getElem(doc);
        } finally {
            try { in.close(); } catch (Exception e) {}
        }
    }
    
    public static void writeXMLFile(String fileName, Element rootEl) throws IOException {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
            out.println(XMLLight.getXMLDocument(rootEl));
            out.flush();
        } finally {
            try { out.close(); } catch (Exception e) {}
        }
    }
    
    
}
