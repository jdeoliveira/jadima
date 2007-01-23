/*
 * JdmGuiException.java
 *
 * Created on 11 de agosto de 2006, 12:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ve.usb.jgm.gui;

import javax.swing.JOptionPane;

/**
 *
 * @author karyn
 */
public class JdmGuiException extends java.lang.Exception {
    
    private String message;
    /**
     * Creates a new instance of JdmGuiException
     */
    public JdmGuiException() {
    }
    
    public JdmGuiException(String message) {
        this.message = message;
    }
    
    public void showError(){
        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
    }   
}
