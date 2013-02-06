/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.collision.CollisionResult;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public class EditorMappings implements AnalogListener, ActionListener {

    private Node root, camHelper;
    private Application app;
    private Camera camera;
    private EditorBaseManager baseParts;
    private EditorCameraManager camMan;

    public EditorMappings(Application app, EditorBaseManager baseParts) {

        this.app = app;
        this.baseParts = baseParts;
        root = (Node) this.app.getViewPort().getScenes().get(0);
        camHelper = (Node) root.getChild("camTrackHelper");
        camera = app.getCamera();
        camMan = baseParts.getCamManager();
        
        setupKeys();


    }

    private void setupKeys() {
        //Set up keys and listener to read it

        String[] mappings = new String[]{
            "MoveCameraHelper",
            "MoveOrSelect"
        };


        app.getInputManager().addListener(this, mappings);

        app.getInputManager().addMapping("MoveCameraHelper", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        app.getInputManager().addMapping("MoveOrSelect", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    }

    public void onAnalog(String name, float value, float tpf) {

        // Move Camera
        if (name.equals("MoveCameraHelper")) {
            camMan.moveCamera();
        }
    }

    public void onAction(String name, boolean isPressed, float tpf) {

        // Select a transformTool or an entity
        if (name.equals("MoveOrSelect") && isPressed) {
            CollisionResult result = baseParts.getTransformTool().activate();

        } else if (name.equals("MoveOrSelect") && !isPressed) {
            baseParts.getTransformTool().deactivate();
            System.out.println("transform done");
        }

    }
}
