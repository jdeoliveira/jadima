/*
 * JdmConfig.java
 *
 * Created on July 31, 2006, 8:59 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ve.usb.jgm.gui;

import java.lang.*;
import java.util.*;
import java.io.*;
import javax.swing.JOptionPane;

/**
 *
 * @author karyn
 */
public class JdmConfig {
    
    static ViewJdmConfig view = new ViewJdmConfig();
    static ViewEditRepository viewEdit = new ViewEditRepository();
    static Vector Repositories = new Vector(); 
    static Vector RepositoriesList = new Vector();
   
    
    private static void loadModules(){
        view.jComboBox1.removeAllItems();
        view.jTextFieldType.setText("InsecureRepositoryServiceClient");
        view.jTextFieldType.setEditable(false);
        view.jComboBox1.addItem("SOAP");
    }
    
    private static void loadClases(){
        Vector stubsLoaded = new Vector();
        Vector byteCodesLoaded = new Vector();
        Vector prefetchsLoaded = new Vector();

        stubsLoaded.addElement("Stubs Cache Manager");
        stubsLoaded.addElement("No Stubs Cache Manager");
        byteCodesLoaded.addElement("Bytecode Cache Manager");
        byteCodesLoaded.addElement("No Bytecode Cache Manager");
        prefetchsLoaded.addElement("Prefetch");
        prefetchsLoaded.addElement("No Prefetch");
        
        view.jList1.setListData(stubsLoaded);
        view.jList2.setListData(byteCodesLoaded);   
        view.jList3.setListData(prefetchsLoaded);  
    }
    
    private static void AddRepository() throws JdmGuiException{
             
        boolean bandera = true;
        int _priority;
        
        String name = view.jTextFieldName.getText();
        //String type = view.jTextFieldType.getText();
        String type = "ve.usb.jgm.ws.client.InsecureRepositoryServiceClient";
        String priority = view.jTextFieldPriority.getText();
        String url = view.jTextFieldUrl.getText();
        
        for (int i=0; i<Repositories.size(); i++){
            RepositoryObject repo = (RepositoryObject)Repositories.elementAt(i);
            if (repo.getname().compareTo(name)==0){
                bandera = false; 
            }
        }
       
        try{
            if (bandera){

                if (name.compareTo("") ==0 )
                    throw new JdmGuiException("Indique nombre del repositorio");
                else if (url.compareTo("") == 0)
                    throw new JdmGuiException("Indique url del repositorio");
                else if (priority.compareTo("") == 0)
                    throw new JdmGuiException("Indique prioridad del repositorio");
                else{
                    _priority = Integer.parseInt(priority);

                    RepositoryObject repository = new RepositoryObject(name, type, _priority, url);

                    Repositories.addElement(repository);    

                    for (int i=0; i<Repositories.size(); i++){

                        RepositoryObject repo = (RepositoryObject)Repositories.elementAt(i);

                        if (!RepositoriesList.contains(repo.getname()))
                            RepositoriesList.addElement(repo.getname());
                    }
                }
            }
        }catch(JdmGuiException e){ e.showError();}
        
        view.jList4.setListData(RepositoriesList);
        
        view.jTextFieldName.setText("");
        view.jTextFieldPriority.setText("");
        view.jTextFieldUrl.setText("");
        
    }
    
    private static void RemoveRepository(){
    
        Vector repositoriesToRemove = new Vector();
        
        for(int i=0; i<view.jList4.getSelectedValues().length; i++){

            repositoriesToRemove.addElement(view.jList4.getSelectedValues()[i]);             
        }
        
        for(int j=0; j<repositoriesToRemove.size();j++){
            
            for (int k=0; k<Repositories.size(); k++){
                
                RepositoryObject repo = (RepositoryObject)Repositories.elementAt(k);
                
                if ( repo.getname().compareTo((String)repositoriesToRemove.elementAt(j))==0){
                    
                    Repositories.remove(repo);
                }
            }
        }
        
        for (int l=0; l<repositoriesToRemove.size(); l++){
            RepositoriesList.remove(repositoriesToRemove.elementAt(l));
        }
        
        view.jList4.setListData(RepositoriesList);
    }
    
    private static void selectStub(){
        String stub = (String) view.jList1.getSelectedValue();
        view.jTextField1Select.setText(stub);   
    }
    
    private static void selectByteCode(){
        String byteCode = (String) view.jList2.getSelectedValue();
        view.jTextField2Select.setText(byteCode);
    }
    
    private static void selectPrefetch(){
        String prefetch = (String) view.jList3.getSelectedValue();
        view.jTextField3Select.setText(prefetch);
    }
    
    private static void validateData() throws JdmGuiException{
        
        try{
        
            if (Repositories.isEmpty())
                throw new JdmGuiException("Add repository");
            else if (view.jTextField1Select.getText().compareTo("")==0)
                throw new JdmGuiException("Selected Stub Manager");
            else if (view.jTextField2Select.getText().compareTo("")==0)
                throw new JdmGuiException("Selected Bytecode Manager");
            else if (view.jTextField3Select.getText().compareTo("")==0)
                throw new JdmGuiException("Selected Prefetch Agent");
            else{
                generateXML();
                view.dispose();
            }
        }catch (JdmGuiException e) {e.showError();}   
        
    }
    
    private static void generateXML(){
        
        System.out.println("Generate XML");
        String bytecode = null;
        String stub = null;
        String prefetch = null;
        
        try{
            
            if (view.jTextField1Select.getText().compareTo("Stubs Cache Manager")==0){
                bytecode = "ve.usb.jgm.client.cache.ehcache.EHCacheBytecodeCacheManager";
            }
            else{
                bytecode = "ve.usb.jgm.client.cache.NoBytecodeCacheManager";
            }


            if (view.jTextField2Select.getText().compareTo("Bytecode Cache Manager")==0){
                stub = "ve.usb.jgm.client.cache.ehcache.EHCacheStubsCacheManager";
            }
            else{
                stub = "ve.usb.jgm.client.cache.NoStubsCacheManager";
            }


            if (view.jTextField3Select.getText().compareTo("Prefetch")==0){
                prefetch = "ve.usb.jgm.client.prefetch.LocalityPrefetchAgent";
            }
            else{
                prefetch = "ve.usb.jgm.client.prefetch.NoPrefetchAgent";
            }

            XMLConfiguration.JdmConfigXML(Repositories, bytecode, stub, prefetch);

            System.out.println("NAME      ->   " + view.jTextFieldName.getText());
            System.out.println("TYPE      ->   " + view.jTextFieldType.getText());
            System.out.println("PRIORITY  ->   " + view.jTextFieldPriority.getText());
            System.out.println("STUB      ->   " + view.jTextField1Select.getText());
            System.out.println("BYTECODE  ->   " + view.jTextField2Select.getText());
            System.out.println("PREFETCH  ->   " + view.jTextField3Select.getText());
            
        
        }catch (Exception e) {}
        
    }
    
    //static ConfigObject config = new ConfigObject();
    
    private static void loadData() throws JdmGuiException{
        
        try{
            String stub = "ve.usb.jgm.client.cache.ehcache.EHCacheStubsCacheManager";
            String bytecode = "ve.usb.jgm.client.cache.ehcache.EHCacheBytecodeCacheManager";
            String prefetch = "ve.usb.jgm.client.prefetch.LocalityPrefetchAgent";
            
            Object[] object = new Object[3];
            object = XMLConfiguration.readJdmConfigXML();
        
            RepositoriesList.removeAllElements();
            Repositories = (Vector) object[0];
            
             for (int i=0; i<Repositories.size(); i++){
                
                RepositoryObject repo = (RepositoryObject)Repositories.elementAt(i);
                RepositoriesList.addElement(repo.getname());
            }
           
           // System.out.println(config);
            view.jList4.setListData(RepositoriesList);
            
            if (object[1].toString().compareTo(bytecode)==0){
                
                view.jTextField2Select.setText("Bytecode Cache Manager");
            }
            else{
                //System.out.println(config.getBytecode());
                view.jTextField2Select.setText("No Bytecode Cache Manager");
            }


            if (object[2].toString().compareTo(stub)==0){
                view.jTextField1Select.setText("Stubs Cache Manager");
            }
            else{
                view.jTextField1Select.setText("No Stubs Cache Manager");
            }


            if (object[3].toString().compareTo(prefetch)==0){
                view.jTextField3Select.setText("Prefetch");
            }
            else{
                view.jTextField3Select.setText("No Prefetch");
            }
            
            
            
           
            
        } catch (JdmGuiException e) {e.showError();}
        
    }
    
    static String oldName;
    private static void editRepository(String name){
        oldName = name;
        RepositoryObject repo;
        
        
        viewEdit.jTextFieldName.setText(name);
        
        int i= 0;
        repo = (RepositoryObject)Repositories.elementAt(i);
        while(!name.equals(repo.getname())) {
            
            repo = (RepositoryObject)Repositories.elementAt(i);
            i++;
            
        }
        Integer inte = 0;
        
        viewEdit.jTextFieldPriority.setText(inte.toString(repo.getpriority()));
        viewEdit.jTextFieldType.setText(repo.gettype());
        viewEdit.jTextFieldUrl.setText(repo.getURL());
        
        viewEdit.jComboBox1.removeAllItems();
        viewEdit.jComboBox1.addItem("SOAP");
        
        
        
        
        viewEdit.setVisible(true);
        
        
    }
    
    private static void saveChanges() {
        
        Integer j=0;
        String name = viewEdit.jTextFieldName.getText();
        String type = viewEdit.jTextFieldType.getText(); 
        int priority =  j.valueOf(viewEdit.jTextFieldPriority.getText()).intValue();
        String url = viewEdit.jTextFieldUrl.getText();
                
        RepositoryObject repo;
        
        
        RepositoryObject repoEdited = new RepositoryObject(name, type, priority, url);
        
        
        int i= 0;
        repo = (RepositoryObject)Repositories.elementAt(i);
        
        while(!oldName.equals(repo.getname())) {
            
            repo = (RepositoryObject)Repositories.elementAt(i);
            i++;
            
        }
        
        if(i != 0){
            i--;
        }        
        
        Repositories.removeElementAt(i);
        Repositories.add(i, repoEdited);
        viewEdit.dispose();
        
    }
    
    public static void show(){
        view.setVisible(true);
        loadModules();
        loadClases();
        try{
            loadData();
        } catch (JdmGuiException e) {e.showError();}
            
        
        
        view.jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               try{
                    AddRepository(); 
               }catch(JdmGuiException e){}
            }
        });
        
        view.jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               RemoveRepository();
            }
        });
        
        view.jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
                try{
                    validateData();
                }catch (JdmGuiException e){}
                
            }
        });
        
        view.jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view.dispose();
            }
        });
        
        view.jButton1Select.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               selectStub();
            }
        });
        
        view.jButton2Select.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               selectByteCode();
            }
        });
        
        view.jButton3Select.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               selectPrefetch();
            }
        });
        
        view.jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               editRepository((String)view.jList4.getSelectedValue());
            }
        });
        
        viewEdit.jButtonEditCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               viewEdit.dispose();
            }
        });
        
        viewEdit.jButtonSaveChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               saveChanges();
            }
        });
        
        
      }
    
}
