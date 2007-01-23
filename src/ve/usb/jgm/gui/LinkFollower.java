/*
 * LinkFollower.java
 *
 * Created on August 2, 2006, 7:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ve.usb.jgm.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

/**
 *
 * @author rafa
 */


public class LinkFollower implements HyperlinkListener {

  private JEditorPane pane;
  private Vector visitedLinks = new Vector();
  
  public LinkFollower(JEditorPane pane, Vector visitedLinks) {
    this.pane = pane;
    this.visitedLinks = visitedLinks;
  }

  public void hyperlinkUpdate(HyperlinkEvent evt) {
    
    if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        visitedLinks.addElement(evt.getURL());
        pane.setPage(evt.getURL());        
      }
      catch (Exception e) {        
      } 
    }
    
  }

}
