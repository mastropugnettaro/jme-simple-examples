/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;

/**
 *
 * @author mifth
 */
public class EditorBaseManager {

    private Application app;
    private AssetManager assetManager;
    private Camera sceneCamera;
    private ViewPort viewPort;
    private FlyByCamera flyCam;
    
    // Global Nodes
    private Node rootNode, guiNode;

    private Node selectableNode, hidedNode;
    private Node camTrackHelper;
    
    // Tools
    private EditorCameraSets camSettings;
    private EditorTransformManager transformManager;
    private EditorMappings mappings;
    private EditorSelectionManager selectionManager;
    private EditorLayerManager layerManager;
    public EditorBaseManager(Application app) {

        this.app = app;
        sceneCamera = this.app.getCamera();
        viewPort = this.app.getViewPort();
        assetManager = this.app.getAssetManager();

        flyCam = this.app.getStateManager().getState(FlyCamAppState.class).getCamera();
        flyCam.setEnabled(false);

        setGlobalNodes();
        
        camSettings = new EditorCameraSets(sceneCamera, camTrackHelper, this.app.getInputManager());        
        mappings = new EditorMappings(this.app, this);
        setCamTracker();

        createSimpleGui();
        setLight();

        createGrid();
        EditorGui gui = new EditorGui();
        this.app.getStateManager().attach(gui);

        // setup global tools
        layerManager = new EditorLayerManager(this.app, this);
        selectionManager = new EditorSelectionManager(this.app, this);
        selectableNode.addControl(selectionManager);
        transformManager = new EditorTransformManager(this.app, this);
        selectableNode.addControl(transformManager);        
        
        
        // Testing Entity for a while
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geo = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        geo.setMaterial(mat);
        Node boxNode = new Node();
        boxNode.attachChild(geo);
        boxNode.move(0.5f, 2, 3);
        layerManager.getLayer(1).attachChild(boxNode);
        selectionManager.selectEntity(boxNode, EditorSelectionManager.SelectionMode.Normal);
        transformManager.setTransformToolType(EditorTransformManager.TransformToolType.MoveTool);
        
        Node boxNode2 = (Node) boxNode.clone(false);
        boxNode2.move(-0.5f, -2, 2);
        layerManager.getLayer(1).attachChild(boxNode2);
        selectionManager.selectEntity(boxNode2, EditorSelectionManager.SelectionMode.Additive);
//        transformManager.setTransformToolType(EditorTransformManager.TransformToolType.MoveTool);        

    }

    private void setGlobalNodes() {

        rootNode = (Node) app.getViewPort().getScenes().get(0);
        guiNode = (Node) app.getGuiViewPort().getScenes().get(0);        
        
        camTrackHelper = new Node("camTrackHelper");
        rootNode.attachChild(camTrackHelper);                
        
        selectableNode = new Node("selectableNode");
        rootNode.attachChild(selectableNode);

        hidedNode = new Node("hidedNode");


    }

    
    protected EditorTransformManager getTransformTool() {
        return transformManager;
    }

    public EditorSelectionManager getSelectionManager() {
        return selectionManager;
    }    
    
    protected EditorMappings getEditorMappings() {
        return mappings;
    }    
    
    private void setCamTracker() {

        // Red line for X axis
        final Line xAxis = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0.05f, 0f, 0f));
        xAxis.setLineWidth(2f);
        Geometry gxAxis = new Geometry("XAxis", xAxis);
        gxAxis.setModelBound(new BoundingBox());
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", new ColorRGBA(1.0f, 0.0f, 0.0f, 0.5f));
        mat1.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gxAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gxAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gxAxis.setMaterial(mat1);
//        gxAxis.setCullHint(CullHint.Never);

        camTrackHelper.attachChild(gxAxis);


        // Blue line for Y axis
        final Line yAxis = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0.05f, 0f));
        yAxis.setLineWidth(2f);
        Geometry gyAxis = new Geometry("ZAxis", yAxis);
        gyAxis.setModelBound(new BoundingBox());
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", new ColorRGBA(0.0f, 0.0f, 1.0f, 0.5f));
        mat2.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gyAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gyAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gyAxis.setMaterial(mat2);
//        gzAxis.setCullHint(CullHint.Never);
        camTrackHelper.attachChild(gyAxis);


        // Blue line for Z axis
        final Line zAxis = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 0.05f));
        zAxis.setLineWidth(2f);
        Geometry gzAxis = new Geometry("ZAxis", zAxis);
        gzAxis.setModelBound(new BoundingBox());
        Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", new ColorRGBA(0.0f, 1.0f, 0.0f, 0.5f));
        mat3.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gxAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gzAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gzAxis.setMaterial(mat3);
//        gzAxis.setCullHint(CullHint.Never);
        camTrackHelper.attachChild(gzAxis);

    }

    private void setLight() {

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.8f, -0.6f, -0.08f).normalizeLocal());
        dl.setColor(new ColorRGBA(1, 1, 1, 1));
        rootNode.addLight(dl);

        viewPort.setBackgroundColor(ColorRGBA.Gray);
    }

    private void createSimpleGui() {

        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize());
        ch.setText("W,A,S,D,Q,Z, MiddleMouseButton, RightMouseButton, Scroll"); // crosshairs
        ch.setColor(new ColorRGBA(1f, 0.8f, 0.1f, 0.3f));
        ch.setLocalTranslation(viewPort.getCamera().getWidth() * 0.1f, viewPort.getCamera().getHeight() * 0.1f, 0);
        guiNode.attachChild(ch);

    }

    private void createGrid() {
        Node gridNode = new Node("gridNode");

        //Create a grid plane
        Geometry g = new Geometry("GRID", new Grid(101, 101, 1f));
        Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floor_mat.getAdditionalRenderState().setWireframe(true);
        floor_mat.setColor("Color", new ColorRGBA(0.3f, 0.3f, 0.3f, 0.1f));
        floor_mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        g.setShadowMode(RenderQueue.ShadowMode.Off);
        g.setQueueBucket(RenderQueue.Bucket.Transparent);
        g.setMaterial(floor_mat);
        g.center().move(new Vector3f(0f, 0f, 0f));
        gridNode.attachChild(g);

        // Red line for X axis
        final Line xAxis = new Line(new Vector3f(-50f, 0f, 0f), new Vector3f(50f, 0f, 0f));
        xAxis.setLineWidth(2f);
        Geometry gxAxis = new Geometry("XAxis", xAxis);
        gxAxis.setModelBound(new BoundingBox());
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1.0f, 0.2f, 0.2f, 0.2f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gxAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gxAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gxAxis.setMaterial(mat);
        gxAxis.setCullHint(Spatial.CullHint.Never);

        gridNode.attachChild(gxAxis);

        // Blue line for Z axis
        final Line zAxis = new Line(new Vector3f(0f, 0f, -50f), new Vector3f(0f, 0f, 50f));
        zAxis.setLineWidth(2f);
        Geometry gzAxis = new Geometry("ZAxis", zAxis);
        gzAxis.setModelBound(new BoundingBox());
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.2f, 1.0f, 0.2f, 0.2f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gxAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gzAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gzAxis.setMaterial(mat);
        gzAxis.setCullHint(Spatial.CullHint.Never);
        gridNode.attachChild(gzAxis);

        rootNode.attachChild(gridNode);

    }
}
