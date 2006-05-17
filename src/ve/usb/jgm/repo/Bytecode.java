/*
 * Bytecode.java
 *
 * Created on 12 de mayo de 2005, 12:29 AM
 */

package ve.usb.jgm.repo;

import java.io.*;
import org.apache.log4j.*;
/**
 *
 * @author  Jesus De Oliveira <A HREF="mailto:jesus@bsc.co.ve"><jesus@bsc.co.ve></A>
 */
public class Bytecode implements Serializable, Cloneable {
    
    private static final Logger logger = Logger.getLogger(Bytecode.class);

    /**
     * Holds value of property className.
     */
    private String className;

    /**
     * Holds value of property classData.
     */
    private byte[] classData;

    /**
     * Holds value of property revision.
     */
    private int revision;
    
    /**
     * Holds value of property allowedRoles.
     * --Propagado de Library && Version && Revision--
     */
    private transient String[] allowedRoles;
    private int majorVersion;
    private int minorVersion;
    //IMPORTANTE: Verificar que transient no afecte la serializacion de estos
    //parametros durante el transporte
    
    
    public Bytecode() {
        //no argument constructor to be considered as a bean
    }
    
    /** Creates a new instance of Bytecode 
     * For use only in the ws package
     */
    public Bytecode(String _className, byte[] _classData) {
        className = _className;
        classData = _classData;
    }
    
    /** Creates a new instance of Bytecode 
     * For use only in the backend package
     */
    public Bytecode(String _className, byte[] _classData, int _majorVersion, int _minorVersion, int _revisionVersion, String[] _allowedRoles) {
        className = _className;
        classData = _classData;
        majorVersion = _majorVersion;
        minorVersion = _minorVersion;
        revision = _revisionVersion;
        allowedRoles = _allowedRoles;
    }
    
    /**
     * For use in the client package, making requests
     */
    public Bytecode(String _className, int _majorVersion, int _minorVersion) {
        className = _className;
        majorVersion = _majorVersion;
        minorVersion = _minorVersion;
    }

    /**
     * Getter for property className.
     * @return Value of property className.
     */
    public String getClassName() {

        return this.className;
    }

    /**
     * Setter for property className.
     * @param className New value of property className.
     */
    public void setClassName(String className) {

        this.className = className;
    }

    /**
     * Getter for property classData.
     * @return Value of property classData.
     */
    public byte[] getClassData() {

        return this.classData;
    }

    /**
     * Setter for property classData.
     * @param classData New value of property classData.
     */
    public void setClassData(byte[] classData) {

        this.classData = classData;
    }

    /**
     * Getter for property allowedRoles.
     * @return Value of property allowedRoles.
     */
    public String[] getAllowedRoles() {

        return this.allowedRoles;
    }

    /**
     * Setter for property allowedRoles.
     * @param allowedRoles New value of property allowedRoles.
     */
    public void setAllowedRoles(String[] allowedRoles) {

        this.allowedRoles = allowedRoles;
    }

    /**
     * Getter for property revision.
     * @return Value of property revision.
     */
    public int getRevision() {

        return this.revision;
    }

    /**
     * Setter for property revision.
     * @param revision New value of property revision.
     */
    public void setRevision(int revision) {

        this.revision = revision;
    }

    /**
     * Getter for property majorVersion.
     * @return Value of property majorVersion.
     */
    public int getMajorVersion() {

        return this.majorVersion;
    }

    /**
     * Setter for property majorVersion.
     * @param majorVersion New value of property majorVersion.
     */
    public void setMajorVersion(int majorVersion) {

        this.majorVersion = majorVersion;
    }

    /**
     * Getter for property minorVersion.
     * @return Value of property minorVersion.
     */
    public int getMinorVersion() {

        return this.minorVersion;
    }

    /**
     * Setter for property minorVersion.
     * @param minorVersion New value of property minorVersion.
     */
    public void setMinorVersion(int minorVersion) {

        this.minorVersion = minorVersion;
    }
    
    public String toString() {
        //return "[" + className + "(" + majorVersion + "." + minorVersion + "." + revision + ")(" + ((classData != null)?("LOADED"):("EMPTY")) + ")]";
        return "[" + className + "(" + majorVersion + "." + minorVersion + "." + revision + ")]";
    }

    public boolean equals(Object o) {
        try {
            logger.debug("Comparing " + this.toString() + " with " + o.toString());
            Bytecode b = (Bytecode)o;
            //return (b.getClassName().equals(this.className) && (b.getMajorVersion() == this.getMajorVersion()) && (b.getMinorVersion() == this.getMinorVersion()));
            return b.getClassName().equals(this.className);
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    public int hashCode() {
        return this.className.hashCode();
    }
}
