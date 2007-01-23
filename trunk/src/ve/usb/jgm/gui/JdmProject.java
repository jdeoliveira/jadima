/*
 * JdmProject.java
 *
 * Created on 25 de julio de 2006, 11:46 AM
 *u
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ve.usb.jgm.gui;

import com.sun.java_cup.internal.action_part;
import java.awt.event.MouseEvent;
import org.apache.commons.cli.*;
import org.apache.log4j.*;
import java.io.*;
import ve.usb.jgm.client.*;
import ve.usb.jgm.repo.*;
import java.util.*;
import java.lang.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import com.softcorporation.xmllight.*;
import javax.swing.JOptionPane;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
/**
 *
 * @author Karyn Cuenca
 * @author Rafael Angarita
 */
public class JdmProject {
    
    static ViewJdmProject view = new ViewJdmProject();
    static Browser browser = new Browser();
    static Vector selectedDependences = new Vector();
    static Vector selectedLibrary = new Vector();
    static Collection<Library> libs = null;    
    static String XMLPath = null;
   
    
    /**
     * Receive a String a return a node for the tree
     */
    private static DefaultMutableTreeNode makeTreeNode(String name){
        return new DefaultMutableTreeNode(name);
    }
    
    /**
     * Load the libraries and build the tree
     */ 
    private static boolean LoadDependences(){
        
        Vector libraryList = new Vector();
        DefaultMutableTreeNode theRoot, node, nodeVersion;
        
        DefaultTreeModel model = (DefaultTreeModel)view.jTree1.getModel();
        String convert = "";

        libs = RepositoryBroker.getAllLibraries();
        
        theRoot =  makeTreeNode("Libraries");
        for (Library lib: libs) {
            libraryList.addElement(lib.getName());
            node = makeTreeNode(lib.getName());
            theRoot.add(node);
            for(Version v: lib.getVersions()){
                convert = convert.valueOf(v.getNumberMajor());
                convert = convert.concat(".");
                nodeVersion = makeTreeNode(convert.concat(convert.valueOf(v.getNumberMinor())));
                node.add(nodeVersion);
            }            
        } 
        model.setRoot(theRoot);
        
        return true;
    }

    private static void ClearTree(){
        
        Vector libraryList = new Vector();
        DefaultMutableTreeNode theRoot, node, nodeVersion;
        
        DefaultTreeModel model = (DefaultTreeModel)view.jTree1.getModel();
        String convert = "";

        //libs = RepositoryBroker.getAllLibraries();
        
        theRoot =  makeTreeNode("Libraries");
       /* for (Library lib: libs) {
            libraryList.addElement(lib.getName());
            node = makeTreeNode(lib.getName());
            theRoot.add(node);
            for(Version v: lib.getVersions()){
                convert = convert.valueOf(v.getNumberMajor());
                convert = convert.concat(".");
                nodeVersion = makeTreeNode(convert.concat(convert.valueOf(v.getNumberMinor())));
                node.add(nodeVersion);
            }            
        } */
        model.setRoot(theRoot);
    }
    

    /**
     * Add the selected library to the user project panel
     */
    private static void AddDependences(){
        
        Object[] libraryPath = view.jTree1.getLeadSelectionPath().getPath();
        System.out.println(libraryPath.length);
        
        String libraryFullName = libraryPath[1].toString();
       
        libraryFullName = libraryFullName.concat(" ");
       
        libraryFullName = libraryFullName.concat(libraryPath[2].toString());

        if(!selectedDependences.contains(libraryFullName)){
            selectedDependences.addElement(libraryFullName);
        }
        view.jList2.setListData(selectedDependences);
    }
    
    
    /**
     * Remove the selected library from the user project panel
     */
    private static void RemoveDependences(){
        
        Vector dependencesToRemove = new Vector();
        
        for(int i=0; i<view.jList2.getSelectedValues().length; i++){
            dependencesToRemove.addElement(view.jList2.getSelectedValues()[i]);             
        }
        
        for(int j=0; j<dependencesToRemove.size();j++){
            selectedDependences.remove(dependencesToRemove.elementAt(j));
        }
        
       view.jList2.setListData(selectedDependences);
    }
    
    
    private static void validateData(int flag) throws JdmGuiException{
     
        try{
            if (view.jTextField1.getText().compareTo("") == 0)
                throw new JdmGuiException("Completar campo para nombre del proyecto");
            else if (view.jTextField2.getText().compareTo("") == 0)
                throw new JdmGuiException("Completar campo para descripcion del proyecto");
            else if (selectedDependences.isEmpty())
                throw new JdmGuiException("Add dependences");
            else{
                handlingXML(flag);
            }
            
        }catch (JdmGuiException e) {e.showError();}   
    }
    
    private static void GenerateXML(String path){
        
        try{
           
            XMLConfiguration.JdmProjectXML(path, view.jTextField1.getText(), view.jTextField2.getText(), selectedDependences);
        }catch (Exception e){}
    }
    
    
    /**
     * Get the selected element of the tree and send it to the browser
     */
    private static void ShowJavaDoc(){
        
        Object node = view.jTree1.getLastSelectedPathComponent();
        TreeNode treeNode = (TreeNode) node;
        
        if(treeNode.isLeaf()){
            Object[] libraryPath = view.jTree1.getLeadSelectionPath().getPath();
            view.jTextAreaName.setText(libraryPath[1].toString());
            Browser.loadBrowser(view, libraryPath[1].toString(), libraryPath[2].toString(), libs);
        }

    }
    
    private static void cleanSystem(){
        
        File dir = new File(System.getenv("JGM_HOME").concat("/temp/"));
        deleteDir(dir);
    }
    
    private static void deleteDir(File dir){
    
        if (dir.exists()){
            if (dir.isDirectory()){
                String[] children = dir.list();
                for (int i=0; i<children.length; i++){
                    deleteDir(new File(dir, children[i]));
                }
            }
        }
        
        dir.delete();
    }
    
    private static String pathFile(JFileChooser viewFile){
        
        String path = null;
        
        int returnVal = viewFile.showOpenDialog(view);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            
            File selectedFile = viewFile.getSelectedFile();
           
            path = selectedFile.getAbsolutePath();
             
            System.out.println(path);
        }
        
        return path;
    }
    
    private static void handlingXML(int flag){
        
        String path;
             
        JFileChooser viewFile = new JFileChooser();
        try{
        if (flag == 0){

            viewFile.setDialogTitle("Open");

            path = pathFile(viewFile);
            XMLPath = path;

            Object[] object = XMLConfiguration.readJdmProjectXML(path);
            view.jTextField1.setText((String)object[0]);
            view.jTextField2.setText((String)object[1]);

            selectedDependences = (Vector)object[2];
            view.jList2.setListData(selectedDependences);

        }
        else if (flag == 1){

            viewFile.setDialogTitle("Save As");
            viewFile.setApproveButtonText("Save");

            path = pathFile(viewFile);
            GenerateXML(path);

        }
        else{

            if (XMLPath == null){
                handlingXML(1);
            }
            else{
                path = XMLPath;
                GenerateXML(path);


            }
        } 
        }catch (JdmGuiException e) {e.showError();}
    }
    
    /**
     * Main JdmProject method. Call loadDependences and listen to the buttons actions.
     */
    public static void main(String[] args){
        ProgressBar progressBar = new ProgressBar();
        progressBar.startProgress();
        ClearTree();
        while(!LoadDependences()){
            
            
        }
        progressBar.stopProgress();
        
        
        Toolkit tk = Toolkit.getDefaultToolkit ();
        Dimension screen = tk.getScreenSize ();
        
        int fw =  (int) (screen.getWidth ());
        int fh =  (int) (screen.getWidth ());
       // view.setSize (fw,fh);

        
        
        int lx =  (int) (screen.getWidth ()  * 3/8);
        int ly =  (int) (screen.getHeight () * 3/8);
      //  view.setLocation(lx, ly);
        view.setVisible(true);
                
        view.addWindowListener(new java.awt.event.WindowListener() {
            public void processWindowEvent(java.awt.event.WindowEvent evt) {
            }
            
            public void windowDeactivated(java.awt.event.WindowEvent evt){
            }
            
            public void windowActivated(java.awt.event.WindowEvent evt){
            }
            
            public void windowDeiconified(java.awt.event.WindowEvent evt){
            }
            
            public void windowIconified(java.awt.event.WindowEvent evt){
            }
            
            public void windowClosed(java.awt.event.WindowEvent evt){
              
            }
            
            public void windowClosing(java.awt.event.WindowEvent evt){
                  cleanSystem();
            }
            
            public void windowOpened(java.awt.event.WindowEvent evt){
            }
        });
        
        view.jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddDependences();
            }
        });
        
        view.jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveDependences();
            }
        });
        
        view.jTree1.addMouseListener(new java.awt.event.MouseListener() {
           public void mouseClicked(java.awt.event.MouseEvent evt) {
                ShowJavaDoc();
           }
           public void mouseEntered(MouseEvent e) {
           }
           public  void mouseExited(MouseEvent e) {
           }
           public  void mousePressed(MouseEvent e) {
           }
           public  void mouseReleased(MouseEvent e) {
           }
        });

        
         view.jMenuItemConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               JdmConfig jdmConfig = new JdmConfig();
               jdmConfig.show();
            }
        });
        
         view.jMenuItemOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               handlingXML(0);
            }
        });
        
         view.jMenuItemSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try{
                    validateData(1);
                }catch (JdmGuiException e){}
            }
        });
        
        view.jMenuItemSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try{
                    validateData(2);
                }catch (JdmGuiException e){} 
            }
        });
        
        view.jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               cleanSystem();
               view.dispose();
            }
        });
 
    }
}