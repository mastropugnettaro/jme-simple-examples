/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.editor;

import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.scene.Node;




public class EditorMappings extends Editor implements AnalogListener, ActionListener{

    
    private Node root, camHelper;
    private EditorCameraMoveControl camMoveControl;
    
    public EditorMappings () {
        
        setupKeys();
        root = (Node) app.getViewPort().getScenes().get(0);            
        camHelper = (Node) root.getChild("camTrackHelper");        
        

        
    }
    
    
    
    private void setupKeys(){
                //Set up keys and listener to read it

        String[] mappings = new String[]{
            "MoveCameraHelper",
            "RotateCameraHelper"

        };
        

        app.getInputManager().addListener(this, mappings);
        
        app.getInputManager().addMapping("MoveCameraHelper", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        app.getInputManager().addMapping("MoveSpatial", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    }
    
    public void onAnalog(String name, float value, float tpf) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("MoveCameraHelper") && isPressed){
//            System.out.println("MoveCamera");
            
            settings.setHeight(app.getViewPort().getCamera().getHeight());
            settings.setWidth(app.getViewPort().getCamera().getWidth());
            camMoveControl = new EditorCameraMoveControl(camHelper, app.getInputManager(), app.getViewPort().getCamera(), settings);
            camHelper.addControl(camMoveControl);
        } else if (name.equals("MoveCameraHelper") && !isPressed) {
            camHelper.removeControl(camMoveControl);
                System.out.println("ReMoveCamera");
        }
    }
    
    
    
}
