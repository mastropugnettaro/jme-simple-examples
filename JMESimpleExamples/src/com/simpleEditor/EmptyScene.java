package com.simpleEditor;


import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;



public class EmptyScene extends SimpleApplication{

    public static void main(String[] args) {
        EmptyScene app = new EmptyScene();
        AppSettings aps = new AppSettings(true);
        aps.setVSync(true);
        aps.setResolution(800, 600);
        app.setSettings(aps);
        app.start();
    }

              
    

    @Override
    public void simpleInitApp() {
        
        EditorBaseParts baseParts = new EditorBaseParts((Application)this);
    
    }

    

    
    @Override
    public void simpleUpdate(float tpf) {
       
        
    }    
    
    
    


}
