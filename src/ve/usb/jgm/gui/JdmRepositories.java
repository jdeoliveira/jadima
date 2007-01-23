/*
 * JdmRepositories.java
 *
 * Created on 8 de agosto de 2006, 04:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ve.usb.jgm.gui;

import ve.usb.jgm.repo.*;
import java.util.*;
import java.io.*;
import ve.usb.jgm.client.*;
/**
 *
 * @author rafa
 */
public class JdmRepositories {

    private static Collection<RepositoryClient> myRepos = RepositoryClientFactory.makeRepositoryClients();
    static ViewJdmRepositories view = new ViewJdmRepositories();
    
    private static void getRepositories(){
        
        Vector repositories = new Vector();
        
         for (RepositoryClient c: myRepos) {
             System.out.println(c.getName());
             repositories.addElement(c.getName());
         }
         
         view.jList1.setListData(repositories);
        
    }
    
    public static void show(){
        
        getRepositories();
        view.setVisible(true);
        
        view.jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               view.dispose();
            }
        });
        
    }
    
    
}
