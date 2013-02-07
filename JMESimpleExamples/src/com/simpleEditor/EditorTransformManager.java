/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import java.util.List;

/**
 *
 * @author mifth
 */
public class EditorTransformManager extends AbstractControl {
    
    private Node transformTool;
    private Node moveTool, rotateTool, scaleTool, collisionPlane;
    private Transform selectedCenter, actionCenter;
    private Node root, guiNode;
    private TransformToolType transformType;
    private PickedAxis pickedAxis;
    private AssetManager assetMan;
    private Application app;
    private boolean isActive = false;
//    private Geometry testGeo;
    private Vector3f deltaMoveVector;
    private EditorBaseManager base;
    private Node tranformParentNode;
    // tools
    private EditorTransformMoveTool moveToolObj;
    private EditorTransformRotateTool rotateToolObj;
    private EditorTransformScaleTool scaleToolObj;
    
    protected enum TransformToolType {
        
        MoveTool, RotateTool, ScaleTool, None
    };
    
    protected enum TransformCoordinates {
        
        WorldCoords, LocalCoords, ViewCoords
    };
    
    protected enum PickedAxis {
        
        X, Y, Z, XY, XZ, YZ, View, None
    };
    
    public EditorTransformManager(Application app, EditorBaseManager base) {
        
        this.app = app;
        this.base = base;
        assetMan = app.getAssetManager();
        root = (Node) app.getViewPort().getScenes().get(0);
        guiNode = (Node) app.getGuiViewPort().getScenes().get(0);
        
        transformTool = new Node("transformTool");
        root.attachChild(transformTool);
        
        createManipulators();
        tranformParentNode = new Node("tranformParentNode");
        Node selectableNode = (Node) root.getChild("selectableNode");
        selectableNode.attachChild(tranformParentNode);
        
        pickedAxis = PickedAxis.None;
        transformType = TransformToolType.ScaleTool;  //default type

        createCollisionPlane();
        
        moveToolObj = new EditorTransformMoveTool(this, this.app, this.base);
        rotateToolObj = new EditorTransformRotateTool(this, this.app, this.base);
        scaleToolObj = new EditorTransformScaleTool(this, this.app, this.base);
        
    }
    
    private void createCollisionPlane() {
        float size = 2000;
        Geometry g = new Geometry("plane", new Quad(size, size));
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        mat.getAdditionalRenderState().setWireframe(true);
        g.setMaterial(mat);
        g.setLocalTranslation(-size / 2, -size / 2, 0);
        collisionPlane = new Node();
        collisionPlane.attachChild(g);
        root.attachChild(collisionPlane);
    }
    
    protected TransformToolType getTransformToolType() {
        return transformType;
    }
    
    protected PickedAxis getpickedAxis() {
        return pickedAxis;
    }
    
    protected void setPickedAxis(PickedAxis axis) {
        pickedAxis = axis;
    }
    
    protected Node tranformParentNode() {
        return tranformParentNode;
    }
    
    protected Node getCollisionPlane() {
        return collisionPlane;
    }
    
    protected Vector3f getDeltaMoveVector() {
        return deltaMoveVector;
    }
    
    protected void setDeltaMoveVector(Vector3f vec) {
        deltaMoveVector = vec;
    }
    
    protected Node getTranformParentNode() {
        return tranformParentNode;
    }
    
    protected void updateTransform(Transform center) {
        if (center != null) {
            Vector3f vec = center.getTranslation().subtract(app.getCamera().getLocation()).normalize().multLocal(1.3f);
            transformTool.setLocalTranslation(app.getCamera().getLocation().add(vec));
            transformTool.setLocalRotation(center.getRotation());
        }
    }
    
    protected boolean activate() {
        boolean result = false;
        
        if (transformType != TransformToolType.None) {
            
            CollisionResult colResult = null;
            CollisionResults results = new CollisionResults();
            Ray ray = new Ray();
            Vector3f pos = app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0f).clone();
            Vector3f dir = app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0.1f).clone();
            dir.subtractLocal(pos).normalizeLocal();
            ray.setOrigin(pos);
            ray.setDirection(dir);
            transformTool.collideWith(ray, results);
            
            if (results.size() > 0) {
                colResult = results.getClosestCollision();
                
                if (transformType == TransformToolType.MoveTool) {
                    moveToolObj.setCollisionPlane(colResult);
                } else if (transformType == TransformToolType.RotateTool) {
                    rotateToolObj.setCollisionPlane(colResult);
                } else if (transformType == TransformToolType.ScaleTool) {
                    scaleToolObj.setCollisionPlane(colResult);
                }                
                
                attachSelectedToTransformParent();
                isActive = true;
                result = true;
            }
        }
        
        return result;
    }
    
    private void createManipulators() {
        
        Material mat_red = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_red.setColor("Color", ColorRGBA.Red);
        
        Material mat_blue = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_blue.setColor("Color", ColorRGBA.Blue);
        
        Material mat_green = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_green.setColor("Color", ColorRGBA.Green);
        
        Material mat_white = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_white.setColor("Color", ColorRGBA.White);
        
        moveTool = (Node) assetMan.loadModel("Models/simpleEditor/manipulators/manipulators_move.j3o");
        
        moveTool.getChild("move_x").setMaterial(mat_red);
        moveTool.getChild("collision_move_x").setMaterial(mat_red);
        moveTool.getChild("collision_move_x").setCullHint(Spatial.CullHint.Always);
        moveTool.getChild("move_y").setMaterial(mat_blue);
        moveTool.getChild("collision_move_y").setMaterial(mat_blue);
        moveTool.getChild("collision_move_y").setCullHint(Spatial.CullHint.Always);
        moveTool.getChild("move_z").setMaterial(mat_green);
        moveTool.getChild("collision_move_z").setMaterial(mat_green);
        moveTool.getChild("collision_move_z").setCullHint(Spatial.CullHint.Always);
        moveTool.getChild("move_view").setMaterial(mat_white);
        moveTool.getChild("collision_move_view").setMaterial(mat_white);
        moveTool.getChild("collision_move_view").setCullHint(Spatial.CullHint.Always);
        moveTool.scale(0.1f);
        
        rotateTool = (Node) assetMan.loadModel("Models/simpleEditor/manipulators/manipulators_rotate.j3o");
        rotateTool.getChild("rot_x").setMaterial(mat_red);
        rotateTool.getChild("collision_rot_x").setMaterial(mat_red);
        rotateTool.getChild("collision_rot_x").setCullHint(Spatial.CullHint.Always);
        rotateTool.getChild("rot_y").setMaterial(mat_blue);
        rotateTool.getChild("collision_rot_y").setMaterial(mat_blue);
        rotateTool.getChild("collision_rot_y").setCullHint(Spatial.CullHint.Always);
        rotateTool.getChild("rot_z").setMaterial(mat_green);
        rotateTool.getChild("collision_rot_z").setMaterial(mat_green);
        rotateTool.getChild("collision_rot_z").setCullHint(Spatial.CullHint.Always);
        rotateTool.getChild("rot_view").setMaterial(mat_white);
        rotateTool.getChild("collision_rot_view").setMaterial(mat_white);
        rotateTool.getChild("collision_rot_view").setCullHint(Spatial.CullHint.Always);
        rotateTool.scale(0.1f);
        
        scaleTool = (Node) assetMan.loadModel("Models/simpleEditor/manipulators/manipulators_scale.j3o");
        scaleTool.getChild("scale_x").setMaterial(mat_red);
        scaleTool.getChild("collision_scale_x").setMaterial(mat_red);
        scaleTool.getChild("collision_scale_x").setCullHint(Spatial.CullHint.Always);
        scaleTool.getChild("scale_y").setMaterial(mat_blue);
        scaleTool.getChild("collision_scale_y").setMaterial(mat_blue);
        scaleTool.getChild("collision_scale_y").setCullHint(Spatial.CullHint.Always);
        scaleTool.getChild("scale_z").setMaterial(mat_green);
        scaleTool.getChild("collision_scale_z").setMaterial(mat_green);
        scaleTool.getChild("collision_scale_z").setCullHint(Spatial.CullHint.Always);
        scaleTool.getChild("scale_view").setMaterial(mat_white);
        scaleTool.getChild("collision_scale_view").setMaterial(mat_white);
        scaleTool.getChild("collision_scale_view").setCullHint(Spatial.CullHint.Always);
        scaleTool.scale(0.1f);
        
        
    }
    
    private void attachSelectedToTransformParent() {
        
        tranformParentNode.setLocalTranslation(selectedCenter.getTranslation().clone());
        Vector3f moveDeltaVec = new Vector3f().subtract(tranformParentNode.getLocalTranslation());
        List selectedList = base.getSelectionManager().getSelectionList();
        for (Object ID : selectedList) {
            long id = (Long) ID;
            Spatial sp = base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            tranformParentNode.attachChild(sp);
            sp.getLocalTranslation().addLocal(moveDeltaVec);
            sp.setUserData("LayerSelected", sp.getParent());
        }
    }
    
    private void detachSelectedFromTransformParent() {
        List selectedList = base.getSelectionManager().getSelectionList();
        Vector3f moveDeltaVecInvert = tranformParentNode.getLocalTranslation().subtract(new Vector3f());
        for (Object ID : selectedList) {
            long id = (Long) ID;
            Spatial sp = base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            Node layer = (Node) sp.getUserData("LayerSelected");
            layer.attachChild(sp);
            sp.getLocalTranslation().addLocal(moveDeltaVecInvert);
            sp.setUserData("LayerSelected", null);
        }
    }
    
    protected void deactivate() {
        if (transformType != TransformToolType.None && pickedAxis != PickedAxis.None) {
            
            pickedAxis = PickedAxis.None;
            detachSelectedFromTransformParent();
            
            if (selectedCenter != null) {
                // set new selection center
                base.getSelectionManager().getSelectionCenter().setTranslation(tranformParentNode.getLocalTranslation().clone());
                selectedCenter.set(base.getSelectionManager().getSelectionCenter());
                transformTool.setLocalTranslation(selectedCenter.getTranslation().clone());
                deltaMoveVector = null;  // clear deltaVector
                isActive = false;
            }
        }
    }
    
    @Override
    protected void controlUpdate(float tpf) {

        // Move Selected Objects!
        if (pickedAxis != PickedAxis.None && selectedCenter != null && transformType == transformType.MoveTool && isActive) {
            transformTool.detachAllChildren();
            moveToolObj.moveObjects();
        }


        // update transform tool
        if (!isActive && base.getSelectionManager().getSelectionList().size() > 0) {
            selectedCenter = base.getSelectionManager().getSelectionCenter();
            transformTool.detachAllChildren();
            if (transformType == transformType.MoveTool) {
                transformTool.attachChild(moveTool);
            } else if (transformType == transformType.RotateTool) {
                transformTool.attachChild(rotateTool);
            } else if (transformType == transformType.ScaleTool) {
                transformTool.attachChild(scaleTool);
            }
            updateTransform(selectedCenter);
        } else if (base.getSelectionManager().getSelectionList().size() == 0) {
            transformTool.detachAllChildren();
        }
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
