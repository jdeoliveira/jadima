/*
 * Browser.java
 *
 * Created on August 2, 2006, 12:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ve.usb.jgm.gui;

import java.io.*;
import java.net.*;
import javax.mail.Message;
import javax.mail.search.StringTerm;
import javax.swing.event.*;
import java.util.*;
import ve.usb.jgm.repo.*;
import ve.usb.jgm.client.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import javax.swing.JOptionPane;

/**
 *
 * @author rafa
 */
public class Browser {
    
    public static Vector visitedLinks = new Vector();
    public static Vector visitedForwardLinks = new Vector();
    private static Vector downloadedLibraries = new Vector(); 
    
    /**
     * Break the version string to get the major and minor number of 
the version
     */
    public static String[] breakString(String version){
        String[] versionNumbers = new String[2];
        StringTokenizer st = new StringTokenizer(version, ".");
        int i=0;
        while(st.hasMoreTokens()){
            versionNumbers[i] = st.nextElement().toString();
            i++;
        }
        return versionNumbers;
    }
    
    /**
     * Get and decompress the javadoc.zip in a temporal directory
     */
    private static String getJavadoc(String name, String[] versionNumbers, Collection libs){

        //DEBO HACER ESTO PORQUE LOS JAVADOCS EN LA COLECCION INICIAL SON NULL
        Integer integer = 0;
        String tempPath = System.getenv("JGM_HOME").concat("/temp/");
        String fullName = null;
        String url = "file:///".concat(System.getenv("JGM_HOME")).concat("/index.html");
        
        try{
            
            
            fullName = name.concat(" ").concat(versionNumbers[0]).concat(".").concat(versionNumbers[1]);
            
            if (!downloadedLibraries.contains(fullName)){
            
                Version v = RepositoryBroker.getVersion(name, integer.valueOf(versionNumbers[0]).intValue()                                          
                , integer.valueOf(versionNumbers[1]).intValue(), true);

                

                File javadocOut = File.createTempFile("jdm-javadoc", "zip");
                javadocOut.deleteOnExit();
                FileOutputStream fos = null;
               // JOptionPane.showMessageDialog(null, v.getJavadocZip().length, "Jdm Project", JOptionPane.INFORMATION_MESSAGE);
              
                if(v.getJavadocZip().length != 0){
                   // JOptionPane.showMessageDialog(null, "hello hello", "Jdm Project", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        fos = new FileOutputStream(javadocOut);
                        fos.write(v.getJavadocZip());
                        fos.flush();
                    } finally {
                        try { fos.close(); } catch (Exception e) {}
                    }

                    //ANT PARA DESCOMPRIMIR
                    Project p = new Project();
                    p.init();
                    Target t = new Target();
                    t.setName("unzip-javadocs");
                    t.setProject(p);
                    Expand zip = new Expand();
                    zip.setDest(new File(tempPath.concat(fullName)));
                    zip.setSrc(javadocOut);
                    zip.setProject(p);
                    t.addTask(zip);
                    p.addTarget("unzip-javadocs", t);
                    p.executeTarget("unzip-javadocs");
                    
                    File file = new File(tempPath.concat(fullName).concat("/index.html"));
                    if(file.exists()) {
                         downloadedLibraries.addElement(fullName);
                         url = "file:///";
                         url = url.concat(tempPath.concat(fullName).concat("/index.html"));
                        // JOptionPane.showMessageDialog(null, url, "s", JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                }
                /*else{ //javadoc not found
                    String message = "Javadoc not found";
                    JOptionPane.showMessageDialog(null, message, "Jdm 
Project", JOptionPane.INFORMATION_MESSAGE);
                }*/
            } else {
                
                url = "file:///";
                url = url.concat(tempPath.concat(fullName).concat("/index.html"));
            }
            
        }catch(LibraryNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }catch (VersionNotFoundException e) {
            e.printStackTrace();
        }
        
       
        
        
        /*JOptionPane.showMessageDialog(null, 
System.getProperty("user.dir"), "Jdm Project", JOptionPane.INFORMATION_MESSAGE);
        
      
         JOptionPane.showMessageDialog(null, 
tempPath.concat(fullName).concat("/index.html"), "Jdm Project", JOptionPane.INFORMATION_MESSAGE);
        // JOptionPane.showMessageDialog(null, f.exists(), "Jdm 
Project", JOptionPane.INFORMATION_MESSAGE);
        
        JOptionPane.showMessageDialog(null, file.exists(), "Jdm 
Project", JOptionPane.INFORMATION_MESSAGE);*/
        

        return url; 
    }
    
    /**
     * Load the initial page on the browser
     */
    private static void initMethod(ViewJdmProject view, String name, String version, Collection<Library> libs) {  
        
        try {
            String[] versionNumbers = breakString(version); 
            String urlString = getJavadoc(name, versionNumbers, libs);
            URL url = new URL(urlString);
            visitedLinks.addElement(url);
            view.jEditorPane1.addHyperlinkListener(new LinkFollower(view.jEditorPane1, visitedLinks));
            view.jEditorPane1.setPage(url);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
   private static void getDescription(ViewJdmProject view, String name, String version, Collection<Library> libs) {  
       
        for (Library lib: libs) {
            for(Version v: lib.getVersions()){
                if(lib.getName().compareTo(name) == 0){
                       view.jTextAreaDesc.setText(v.getLibraryDescription());
                }
            }
        }

    }

    /**
     * Manage the browser forward funcionality
    
     */
    private static void goForward(ViewJdmProject view){
        try{
            if(visitedForwardLinks.size()>0){
                view.jEditorPane1.setPage((URL) (visitedForwardLinks.lastElement()));
                visitedLinks.addElement(visitedForwardLinks.lastElement());
                visitedForwardLinks.removeElement(visitedForwardLinks.lastElement());
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    /**
     * Manage the browser backward funcionality
     */
    private static void goBack(ViewJdmProject view){
        try{
            if(visitedLinks.size()>1){
                view.jEditorPane1.setPage((URL) (visitedLinks.elementAt(visitedLinks.size()-2)));
                visitedForwardLinks.addElement(visitedLinks.lastElement());
                visitedLinks.removeElement(visitedLinks.lastElement());
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    
    /**
     * Main Browser method. Call the initMethod and listen for the 
forward and backward actions
     */
    public static void loadBrowser(final ViewJdmProject view, String name, String version, Collection<Library> libs){
        getDescription(view, name, version, libs);
        initMethod(view, name, version, libs);
        view.setVisible(true);
        
        view.jButtonBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) 
{
                goBack(view);
            }
        });
        
        view.jButtonForward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) 
{
                goForward(view);
            }
        });
    }
    
}
