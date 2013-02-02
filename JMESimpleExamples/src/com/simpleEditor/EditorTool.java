/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 *
 * @author mifth
 */
public class EditorTool extends AbstractControl implements ActionListener, AnalogListener {

    protected Node transformTool = new Node();
    private Node moveTool, rotateTool, scaleTool;

    protected enum AxisMarkerPickType {

        axisOnly, planeOnly, axisAndPlane
    };
    protected AxisMarkerPickType axisPickType;
//    private SimpleApplication app;
    private Node selectedSp = new Node();
    private AssetManager assetMan;
    private Node root;
    private Application application;
    private boolean isActive = false;
    private Node guiNode;

    public EditorTool(Application app, Node select) {

        application = app;
        assetMan = app.getAssetManager();
        root = (Node) application.getViewPort().getScenes().get(0);
        guiNode = (Node) application.getGuiViewPort().getScenes().get(0);
        
        createManipulators();
        selectedSp = select;

        // Attach onTopToolNode
        Node nd = (Node) selectedSp;
//        root.attachChild(transformTool);
//        transformTool.attachChild(moveTool);
        // Activate Tool
//        updateToolsTransformation();

    }

    @Override
    protected void controlUpdate(float tpf) {
        if (selectedSp != null) {
            addMarker();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onAction(String name, boolean isPressed, float tpf) {
    }

    public void onAnalog(String name, float value, float tpf) {
    }

    protected void addMarker() {

//        transformTool.setLocalTranslation(selectedSp.getWorldTranslation());
        root.attachChild(moveTool);

        Vector3f vec = selectedSp.getWorldTranslation().subtract(application.getCamera().getLocation()).normalize().multLocal(1.3f);
        
        moveTool.setLocalTranslation(application.getCamera().getLocation().add(vec));
        
//        moveTool.setCullHint(Spatial.CullHint.Never);

    }

    /**
     * Remove the marker from it's parent (the tools node)
     */
    public void hideMarker() {
        if (transformTool != null) {
            transformTool.setCullHint(Spatial.CullHint.Always);
        }
    }

    /**
     * Called when the selected spatial has been modified outside of the tool.
     * TODO: why? just move the tool where the object is each frame?
     */
    public void updateToolsTransformation() {

        application.enqueue(new Callable<Object>() {
            public Object call() throws Exception {
                doUpdateToolsTransformation();
                return null;
            }
        });
    }

    public void doUpdateToolsTransformation() {
        if (selectedSp != null) {
            transformTool.setLocalTranslation(selectedSp.getWorldTranslation());
            transformTool.setLocalRotation(selectedSp.getWorldRotation());
            setAxisMarkerScale(selectedSp);
        } else {
            transformTool.setLocalTranslation(Vector3f.ZERO);
            transformTool.setLocalRotation(Quaternion.IDENTITY);
        }
    }

    /**
     * Adjust the scale of the marker so it is relative to the size of the
     * selected spatial. It will have a minimum scale of 2.
     */
    private void setAxisMarkerScale(Spatial selected) {
        if (selected != null) {
            if (selected.getWorldBound() instanceof BoundingBox) {
                BoundingBox bbox = (BoundingBox) selected.getWorldBound();
                float smallest = Math.min(Math.min(bbox.getXExtent(), bbox.getYExtent()), bbox.getZExtent());
                float scale = Math.max(1, smallest / 2f);
                transformTool.setLocalScale(new Vector3f(scale, scale, scale));
            }
        } else {
            transformTool.setLocalScale(new Vector3f(2, 2, 2));
        }
    }

    private CollisionResult doPick(Camera cam, Vector2f mouseLoc, Node node, Spatial exclude) {
        CollisionResults results = new CollisionResults();
        Ray ray = new Ray();
        Vector3f pos = cam.getWorldCoordinates(mouseLoc, 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(mouseLoc, 0.1f).clone();
        dir.subtractLocal(pos).normalizeLocal();
        ray.setOrigin(pos);
        ray.setDirection(dir);
        node.collideWith(ray, results);
        CollisionResult result = null;
        if (exclude == null) {
            result = results.getClosestCollision();
        } else {
            Iterator<CollisionResult> it = results.iterator();
            while (it.hasNext()) {
                CollisionResult cr = it.next();
                if (isExcluded(cr.getGeometry(), exclude)) {
                    continue;
                } else {
                    return cr;
                }
            }

        }
        return result;
    }

    /**
     * Is the selected spatial the one we want to exclude from the picking?
     * Recursively looks up the parents to find out.
     */
    private boolean isExcluded(Spatial s, Spatial exclude) {
        if (s.equals(exclude)) {
            return true;
        }

        if (s.getParent() != null) {
            return isExcluded(s.getParent(), exclude);
        }
        return false;
    }

    /**
     * Pick a part of the axis marker. The result is a Vector3f that represents
     * what part of the axis was selected. For example if (1,0,0) is returned,
     * then the X-axis pole was selected. If (0,1,1) is returned, then the Y-Z
     * plane was selected.
     *
     * @return null if it did not intersect the marker
     */
    protected Geometry pickAxisMarker(Camera cam, Vector2f mouseLoc, AxisMarkerPickType pickType) {
        if (transformTool == null) {
            return null;
        }

        CollisionResult cr = pick(cam, mouseLoc, transformTool);

//        if (cr != null) System.out.println(cr.getGeometry());

        if (cr == null || cr.getGeometry() == null) {
            return null;
        }

        return cr.getGeometry();
    }

    private CollisionResult pick(Camera cam, Vector2f mouseLoc, Node node) {
        CollisionResults results = new CollisionResults();
        Ray ray = new Ray();
        Vector3f pos = cam.getWorldCoordinates(mouseLoc, 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(mouseLoc, 0.1f).clone();
        dir.subtractLocal(pos).normalizeLocal();
        ray.setOrigin(pos);
        ray.setDirection(dir);
        node.collideWith(ray, results);
        CollisionResult result = results.getClosestCollision();
        return result;
    }

    /**
     * Create the axis marker that is selectable
     */
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

    }
}
