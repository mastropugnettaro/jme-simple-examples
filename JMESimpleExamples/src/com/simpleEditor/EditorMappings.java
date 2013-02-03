/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;




public class EditorMappings implements AnalogListener, ActionListener{

    
    private Node root, camHelper;
    private Application app;
    private Camera camera;
    
    public EditorMappings (Application app, Node camHelper) {
        
        this.app = app;
        this.camHelper = camHelper;
        camera = app.getCamera();
        setupKeys();
        root = (Node) this.app.getViewPort().getScenes().get(0);            
        
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
        if (name.equals("MoveCameraHelper")){

        // center of the screen
        float width = camera.getWidth() * 0.5f;
        float height = camera.getHeight() * 0.5f;
        Vector2f ceneterScr = new Vector2f(width, height);   
        
        Vector2f endPosMouse = app.getInputManager().getCursorPosition();
        float mouseDist = ceneterScr.distance(endPosMouse);

        
        
        Vector3f camMoveX = camera.getLeft();
        camMoveX.negateLocal();
        camMoveX.normalizeLocal();
                
        Vector3f camMoveY = camera.getUp();
//         camMoveY.negateLocal();        
        camMoveY.normalizeLocal();
        
//        System.out.println(endPosMouse);
        
        camHelper.move(camMoveX.mult((endPosMouse.x - ceneterScr.x) / camera.getWidth()).addLocal(camMoveY.mult((endPosMouse.y - ceneterScr.y) / camera.getHeight())).normalizeLocal().multLocal(mouseDist*0.001f));
        
        } 
    }

    public void onAction(String name, boolean isPressed, float tpf) {

    }
    
    
    
}
