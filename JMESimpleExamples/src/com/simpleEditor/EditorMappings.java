/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.collision.CollisionResult;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
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
    private boolean transformResult = false;
    private boolean selectResult = false;

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
            "MoveOrSelect",
            "ScaleAll"
        };


        app.getInputManager().addListener(this, mappings);

        app.getInputManager().addMapping("MoveCameraHelper", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        app.getInputManager().addMapping("MoveOrSelect", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addMapping("ScaleAll", new KeyTrigger(KeyInput.KEY_S));

    }

    public void onAnalog(String name, float value, float tpf) {

        // Move Camera
        if (name.equals("MoveCameraHelper")) {
            camMan.moveCamera();
        }
    }

    public void onAction(String name, boolean isPressed, float tpf) {

        // Select or transformTool an entity
        if (name.equals("MoveOrSelect") && isPressed && !name.equals("ScaleAll")) {

            if (baseParts.getTransformTool().isIsActive() == false) {
                transformResult = baseParts.getTransformTool().activate();
            }
            if (!transformResult) {
                selectResult = baseParts.getSelectionManager().activate();
            }

        } else if (name.equals("MoveOrSelect") && !isPressed && !name.equals("ScaleAll")) {
            if (transformResult) {
                baseParts.getTransformTool().deactivate();
                transformResult = false;
            }
            if (selectResult) {
                baseParts.getSelectionManager().deactivate();
                selectResult = false;
            }

            System.out.println("transform done");
        }


        // scaleTool
        if (name.equals("ScaleAll") && isPressed && !name.equals("MoveOrSelect")) {
            if (baseParts.getTransformTool().isIsActive() == false && baseParts.getSelectionManager().getSelectionList().size() > 0) {
                baseParts.getTransformTool().scaleAll();
                transformResult = true;
            }
        } 
//        else if (name.equals("ScaleAll") && !isPressed && !name.equals("MoveOrSelect")) {
//            if (baseParts.getTransformTool().isIsActive() == false) {
//                baseParts.getTransformTool().deactivate();
//                transformResult = false;
//            }
//        }

    }
}
