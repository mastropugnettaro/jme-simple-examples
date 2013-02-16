package com.simpleEditor;


import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;



public class EmptyScene extends SimpleApplication{

    public static void main(String[] args) {
        EmptyScene app = new EmptyScene();
        AppSettings aps = new AppSettings(true);
        aps.setVSync(true);
        aps.setResolution(1280, 720);
        app.setSettings(aps);
        app.start();
    }

              
    

    @Override
    public void simpleInitApp() {

//        this.setShowSettings(false);
        this.setDisplayStatView(false);
        EditorBaseManager baseParts = new EditorBaseManager(this);        
        
    }

    

    
    @Override
    public void simpleUpdate(float tpf) {
       
        
    }    
    
    
    


}
