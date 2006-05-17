/*
 * JgmVersionAttributeReader.java
 *
 * Created on 24 de abril de 2005, 09:21 PM
 */

package ve.usb.jgm.client;

import org.apache.bcel.classfile.*;
import java.io.*;
import org.apache.log4j.Logger;
import org.apache.bcel.classfile.JgmStubVersionAttribute;

/**
 *
 * @author  Administrator
 */
public class StubVersionAttributeReader implements AttributeReader {
    
    private static final Logger logger = Logger.getLogger(StubVersionAttributeReader.class);
    
    public Attribute createAttribute(int name_index, int length, java.io.DataInputStream file, ConstantPool constant_pool) {
        logger.debug("Asked to create attribute, name_index = " + name_index + ", length = " + length);
        
        try {
            int major = file.read();
            int minor = file.read();
            logger.debug("Found version " + major + "." + minor);
            return new JgmStubVersionAttribute(name_index, 2, major, minor, constant_pool);
        } catch (IOException e) {
           logger.error("Reading major and minor version numbers", e);
            throw new RuntimeException("Reading major and minor version numbers", e);
        }
    }
}
