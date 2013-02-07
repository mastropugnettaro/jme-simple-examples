/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author mifth
 */
public class EditorTransformRotateTool {

    private AssetManager assetMan;
    private Node root, guiNode;
    private Application app;
    private EditorBaseManager base;
    private EditorTransformManager trManager;
    private Node collisionPlane;

    public EditorTransformRotateTool(EditorTransformManager trManager, Application app, EditorBaseManager base) {

        this.app = app;
        this.base = base;
        assetMan = this.app.getAssetManager();
        root = (Node) this.app.getViewPort().getScenes().get(0);
        this.trManager = trManager;
        collisionPlane = trManager.getCollisionPlane();
    }

    protected void setCollisionPlane(CollisionResult colResult) {


        Transform selectedCenter = base.getSelectionManager().getSelectionCenter();

        // Set PickedAxis
        String type = colResult.getGeometry().getName();
        if (type.indexOf("rot_x") > 0) {
            trManager.setPickedAxis(EditorTransformManager.PickedAxis.X);
        } else if (type.indexOf("rot_y") > 0) {
            trManager.setPickedAxis(EditorTransformManager.PickedAxis.Y);
        } else if (type.indexOf("rot_z") > 0) {
            trManager.setPickedAxis(EditorTransformManager.PickedAxis.Z);
        } else if (type.indexOf("rot_view") > 0) {
            trManager.setPickedAxis(EditorTransformManager.PickedAxis.View);
        }

        EditorTransformManager.PickedAxis pickedAxis = trManager.getpickedAxis();
        
        // set the collision Plane location and rotation
        collisionPlane.setLocalTranslation(selectedCenter.getTranslation().clone());
        collisionPlane.getLocalRotation().lookAt(app.getCamera().getDirection(), Vector3f.UNIT_Y); //equals to angleZ
        
    }
}
