/*
 * StubVersionAttribute.java
 *
 * Created on 24 de abril de 2005, 11:38 PM
 */

package org.apache.bcel.classfile;

import  org.apache.bcel.Constants;

/**
 *
 * @author  Administrator
 */
public class JgmStubVersionAttribute extends Attribute {
    
    private int stubversion_index;
    private int major;
    private int minor;
    
    
    /** Creates a new instance of StubVersionAttribute */
    public JgmStubVersionAttribute(int name_index, int length, int major, int minor, ConstantPool constant_pool) {
        super(Constants.ATTR_UNKNOWN, name_index, length, constant_pool);
        this.major = (int)major;
        this.minor = (int)minor;
        this.stubversion_index = stubversion_index;
    }
    
    public void accept(Visitor v) {
        //No hacemos nada... que deberiamos hacer??
    }
    
    public Attribute copy(ConstantPool constant_pool) {
        return (JgmStubVersionAttribute)clone();
    }

    public final int getMajorVersion() {
        return major;
    }
    
    public final int getMinorVersion() {
        return minor;
    }
    
    public String toString() {
        return "JgmStubVersionAttribute(" + getMajorVersion() + "." + getMinorVersion() + ")";
    }
}
