/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
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
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 *
 * @author mifth
 */
public class EditorTool extends AbstractControl implements ActionListener, AnalogListener{

    protected static Vector3f ARROW_X = new Vector3f(1, 0, 0);
    protected static Vector3f ARROW_Y = new Vector3f(0, 1, 0);
    protected static Vector3f ARROW_Z = new Vector3f(0, 0, 1);
    protected static Vector3f QUAD_XY = new Vector3f(1, 1, 0);
    protected static Vector3f QUAD_XZ = new Vector3f(1, 0, 1);
    protected static Vector3f QUAD_YZ = new Vector3f(0, 1, 1);
    protected Node toolNode = new Node();
    protected Node onTopToolNode = new Node();
    protected Node axisMarker;
    protected Material redMat, blueMat, greenMat, yellowMat, cyanMat, magentaMat, orangeMat;
    protected Geometry quadXY, quadXZ, quadYZ;

    
    protected enum AxisMarkerPickType {

        axisOnly, planeOnly, axisAndPlane
    };
    protected AxisMarkerPickType axisPickType;
    
//    private SimpleApplication app;
    private Node selectedSp = new Node();
    private AssetManager assetMan;
    private Node root;
    private Application application;
    
    
    public EditorTool (Application simpleApp, Node select) {

        application = simpleApp;
        assetMan = simpleApp.getAssetManager();
        root = (Node) application.getViewPort().getScenes().get(0);
        

      selectedSp = select;
        
//        // Attach onTopToolNode
       Node nd = (Node) selectedSp;
       root.attachChild(onTopToolNode);
         root.attachChild(toolNode);       
       // Activate Tool
       activate();
       updateToolsTransformation();
       displayPlanes();        
        
        
    }

    @Override
    protected void controlUpdate(float tpf) {
//        System.out.println("eeeeeeeeeeeeeeee");
//        float height  = simple.getCamera().getHeight();
//        if (pickAxisMarker(simple.getCamera(), simple.getInputManager().getCursorPosition(), axisPickType.axisAndPlane) != null
//            ){
            highlightAxisMarker(application.getViewPort().getCamera(), application.getInputManager().getCursorPosition(), axisPickType.planeOnly, true);
//        }         
//        onTopToolNode.
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }    
    
    public void onAction(String name, boolean isPressed, float tpf) {
        // Actions
    }
    
    public void onAnalog(String name, float value, float tpf) {
        // Analogs
//        if (name.equals("MoveCameraHelper") ){
//            highlightAxisMarker(application.getViewPort().getCamera(), application.getInputManager().getCursorPosition(), axisPickType.axisAndPlane, true);
//        }        
    }    
    
    
    /**
     * The tool was selected, start showing the marker.
     * @param manager
     * @param toolNode: parent node that the marker will attach to
     */
    public void activate() {
//        this.manager = manager;
//        this.toolController = toolController;
        //this.selectedSpatial = selectedSpatial;
        addMarker(toolNode, onTopToolNode);
    }

    protected void addMarker(Node toolNode, Node onTopToolNode) {
        this.toolNode = toolNode;
        this.onTopToolNode = onTopToolNode;

        if (axisMarker == null) {
            axisMarker = createAxisMarker();
        }
        axisMarker.removeFromParent();
        this.onTopToolNode.attachChild(axisMarker);
        setDefaultAxisMarkerColors();


        if (selectedSp != null) {
            axisMarker.setLocalTranslation(selectedSp.getWorldTranslation());
        }

    }

    /**
     * Remove the marker from it's parent (the tools node)
     */
    public void hideMarker() {
        if (axisMarker != null) {
            axisMarker.removeFromParent();
        }
    }    
    
    /**
     * Called when the selected spatial has been modified
     * outside of the tool.
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
            axisMarker.setLocalTranslation(selectedSp.getWorldTranslation());
            axisMarker.setLocalRotation(selectedSp.getWorldRotation());
            setAxisMarkerScale(selectedSp);
        } else {
            axisMarker.setLocalTranslation(Vector3f.ZERO);
            axisMarker.setLocalRotation(Quaternion.IDENTITY);
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
                float scale = Math.max(1, smallest/2f);
                axisMarker.setLocalScale(new Vector3f(scale,scale,scale));
            }
        } else {
            axisMarker.setLocalScale(new Vector3f(2,2,2));
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
        if (exclude == null)
            result = results.getClosestCollision();
        else {
            Iterator<CollisionResult> it = results.iterator();
            while (it.hasNext()) {
                CollisionResult cr = it.next();
                if (isExcluded(cr.getGeometry(), exclude))
                    continue;
                else
                    return cr;
            }
            
        }
        return result;
    }
    
    /**
     * Is the selected spatial the one we want to exclude from the picking?
     * Recursively looks up the parents to find out.
     */
    private boolean isExcluded(Spatial s, Spatial exclude) {
        if (s.equals(exclude))
            return true;
        
        if (s.getParent() != null) {
            return isExcluded(s.getParent(), exclude);
        }
        return false;
    }    
    
    
    /**
     * Pick a part of the axis marker. The result is a Vector3f that represents
     * what part of the axis was selected.
     * For example if  (1,0,0) is returned, then the X-axis pole was selected.
     * If (0,1,1) is returned, then the Y-Z plane was selected.
     * 
     * @return null if it did not intersect the marker
     */
    protected Geometry pickAxisMarker(Camera cam, Vector2f mouseLoc, AxisMarkerPickType pickType) {
        if (axisMarker == null) {
            return null;
        }

        CollisionResult cr = pick(cam, mouseLoc, axisMarker);

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
     * Show what axis or plane the mouse is currently over and will affect.
     * @param axisMarkerPickType 
     */
    protected void highlightAxisMarker(Camera camera, Vector2f screenCoord, AxisMarkerPickType axisMarkerPickType) {
        highlightAxisMarker(camera, screenCoord, axisMarkerPickType, false);
    }
    
    /**
     * Show what axis or plane the mouse is currently over and will affect.
     * @param axisMarkerPickType 
     * @param colorAll highlight all parts of the marker when only one is selected
     */
    protected void highlightAxisMarker(Camera camera, Vector2f screenCoord, AxisMarkerPickType axisMarkerPickType, boolean colorAll) {
        setDefaultAxisMarkerColors();
        Geometry picked = pickAxisMarker(camera, screenCoord, axisPickType.axisAndPlane);
        if (picked == null) {
            return;
        }

        if (picked.equals(quadXY) ) {
            quadXY.setMaterial(orangeMat);
        } 
        else if (picked.equals(quadXZ)) {
            quadXZ.setMaterial(orangeMat);
        } 
        else if (picked.equals(quadYZ)) {
            quadYZ.setMaterial(orangeMat);
        } 

    }

    /**
     * Create the axis marker that is selectable
     */
    protected Node createAxisMarker() {
        float size = 2;
        float arrowSize = size;
        float planeSize = size * 0.7f;

        Quaternion YAW090 = new Quaternion().fromAngleAxis(-FastMath.PI / 2, new Vector3f(0, 1, 0));
        Quaternion PITCH090 = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(1, 0, 0));

        redMat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        redMat.getAdditionalRenderState().setWireframe(true);
        redMat.setColor("Color", ColorRGBA.Red);
        //redMat.getAdditionalRenderState().setDepthTest(false);
        greenMat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        greenMat.getAdditionalRenderState().setWireframe(true);
        greenMat.setColor("Color", ColorRGBA.Green);
        //greenMat.getAdditionalRenderState().setDepthTest(false);
        blueMat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        blueMat.getAdditionalRenderState().setWireframe(true);
        blueMat.setColor("Color", ColorRGBA.Blue);
        //blueMat.getAdditionalRenderState().setDepthTest(false);
        yellowMat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        yellowMat.getAdditionalRenderState().setWireframe(false);
        yellowMat.setColor("Color", new ColorRGBA(1f, 1f, 0f, 0.25f));
        yellowMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        yellowMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        //yellowMat.getAdditionalRenderState().setDepthTest(false);
        cyanMat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        cyanMat.getAdditionalRenderState().setWireframe(false);
        cyanMat.setColor("Color", new ColorRGBA(0f, 1f, 1f, 0.25f));
        cyanMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        cyanMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        //cyanMat.getAdditionalRenderState().setDepthTest(false);
        magentaMat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        magentaMat.getAdditionalRenderState().setWireframe(false);
        magentaMat.setColor("Color", new ColorRGBA(1f, 0f, 1f, 0.25f));
        magentaMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        magentaMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        //magentaMat.getAdditionalRenderState().setDepthTest(false);

        orangeMat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        orangeMat.getAdditionalRenderState().setWireframe(false);
        orangeMat.setColor("Color", new ColorRGBA(251f / 255f, 130f / 255f, 0f, 0.4f));
        orangeMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        orangeMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);

        Node axis = new Node();

        // create arrows
        Geometry arrowX = new Geometry("arrowX", new Arrow(new Vector3f(arrowSize, 0, 0)));
        Geometry arrowY = new Geometry("arrowY", new Arrow(new Vector3f(0, arrowSize, 0)));
        Geometry arrowZ = new Geometry("arrowZ", new Arrow(new Vector3f(0, 0, arrowSize)));
        axis.attachChild(arrowX);
        axis.attachChild(arrowY);
        axis.attachChild(arrowZ);

        // create planes
        quadXY = new Geometry("quadXY", new Quad(planeSize, planeSize));
        quadXZ = new Geometry("quadXZ", new Quad(planeSize, planeSize));
        quadXZ.setLocalRotation(PITCH090);
        quadYZ = new Geometry("quadYZ", new Quad(planeSize, planeSize));
        quadYZ.setLocalRotation(YAW090);
//        axis.attachChild(quadXY);
//        axis.attachChild(quadXZ);
//        axis.attachChild(quadYZ);

        axis.setModelBound(new BoundingBox());
        return axis;
    }

    protected void displayPlanes() {
        axisMarker.attachChild(quadXY);
        axisMarker.attachChild(quadXZ);
        axisMarker.attachChild(quadYZ);
    }

    protected void hidePlanes() {
        quadXY.removeFromParent();
        quadXZ.removeFromParent();
        quadYZ.removeFromParent();

    }

    protected void setDefaultAxisMarkerColors() {
        axisMarker.getChild("arrowX").setMaterial(redMat);
        axisMarker.getChild("arrowY").setMaterial(blueMat);
        axisMarker.getChild("arrowZ").setMaterial(greenMat);
        quadXY.setMaterial(yellowMat);
        quadXZ.setMaterial(magentaMat);
        quadYZ.setMaterial(cyanMat);
    }    
    
}
