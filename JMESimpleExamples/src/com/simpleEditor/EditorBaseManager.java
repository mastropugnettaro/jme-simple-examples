/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.entitysystem.ComponentsControl;
import com.entitysystem.EntityManager;
import com.entitysystem.EntityNameComponent;
import com.entitysystem.EntitySpatialsControl;
import com.entitysystem.EntitySpatialsSystem;
import com.entitysystem.TransformComponent;
import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.input.FlyByCamera;
import com.jme3.material.Material;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

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
    private EditorCameraManager camManager;
    private EditorTransformManager transformManager;
    private EditorMappings mappings;
    private EditorSelectionManager selectionManager;
    private EditorLayerManager layerManager;
    private EntityManager entityManager;
    private EntitySpatialsSystem spatialSystem = new EntitySpatialsSystem();
    private EditorSceneManager sceneManager;



    public EditorBaseManager(Application app) {

        this.app = app;
        sceneCamera = this.app.getCamera();
        viewPort = this.app.getViewPort();
        assetManager = this.app.getAssetManager();

        flyCam = this.app.getStateManager().getState(FlyCamAppState.class).getCamera();
        flyCam.setEnabled(false);

        setGlobalNodes();
        
        camManager = new EditorCameraManager(this.app, this);        
        camManager.setCamTracker();
        mappings = new EditorMappings(this.app, this);



        // setup global tools
        layerManager = new EditorLayerManager(this.app, this);
        selectionManager = new EditorSelectionManager(this.app, this);
        selectableNode.addControl(selectionManager);
        transformManager = new EditorTransformManager(this.app, this);
        selectableNode.addControl(transformManager);      
        entityManager = new EntityManager();
        sceneManager = new EditorSceneManager(this.app, this);

        setSomeEntities();

        EditorGuiManager gui = new EditorGuiManager(this);
        this.app.getStateManager().attach(gui);        
        
//        // test for entity loading
//        assetManager.registerLocator("/home/mifth/jMonkeyProjects/AD/ad/trunk/ADAssets/dist/lib/ADAssets.jar", ZipLocator.class);
////        assetManager.registerLocator("/home/mifth/jMonkeyProjects/AD/ad/trunk/ADAssets/assets", FileLocator.class);
//        Node model = (Node) assetManager.loadModel("Models/ships/federation/fed_hunter_01/fed_hunter_01.j3o");
//        rootNode.attachChild(model);
        
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

    protected EditorCameraManager getCamManager() {
        return camManager;
    }    
    
    protected EditorTransformManager getTransformTool() {
        return transformManager;
    }

    protected EditorSelectionManager getSelectionManager() {
        return selectionManager;
    }    
    
    protected EditorMappings getEditorMappings() {
        return mappings;
    }    
    
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    protected EntitySpatialsSystem getSpatialSystem() {
        return spatialSystem;
    }

    protected EditorLayerManager getLayerManager() {
        return layerManager;
    }    
    
    public EditorSceneManager getSceneManager() {
        return sceneManager;
    }
    
    private void setSomeEntities() {
    for (int i=0; i<7 ; i++) {
    
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geo = new Geometry("Box"+i, b);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//        mat.getAdditionalRenderState().setWireframe(true);
        geo.setMaterial(mat);
        Node selectedSp = new Node();
        selectedSp.attachChild(geo);        

        Geometry geo2 = geo.clone();
        geo2.scale(0.3f, 0.32f, 1);
        geo2.move(0, 0, 1);
        selectedSp.attachChild(geo2);
        
        // setup Entity
        long ent = entityManager.createEntity();                
        ComponentsControl components = entityManager.getComponentControl(ent);
        
        EntityNameComponent name = new EntityNameComponent("ent" + i);
        components.setComponent(name);
        
        // Check for different transform of entity
        Transform tr = new Transform();
//        Vector3f loc = new Vector3f((float) Math.random() * 20.0f,(float) Math.random() * 10.0f,(float)Math.random() * 20.0f);
        Vector3f loc = new Vector3f(0,0,i*3);
        tr.setTranslation(loc);
//        selectedSp.setLocalTransform(tr);
        
        TransformComponent transform = new TransformComponent(tr);
        components.setComponent(transform);

        // Update components
        components.setUpdateType(ComponentsControl.UpdateType.staticEntity);
        selectedSp.setLocalTransform(tr);
        selectedSp.setName("ent" + i);
//        selectedSp.setLocalRotation(new Quaternion(0.1f,0.2f,0.1f,0.5f));
        
        EntitySpatialsControl spatialControl = spatialSystem.addSpatialControl(selectedSp, ent, entityManager.getComponentControl(ent));
        spatialControl.setType(EntitySpatialsControl.SpatialType.Node);
        spatialControl.recurseNode();

        layerManager.addToLayer(selectedSp, 1);
        if (i < 3) layerManager.addToLayer(selectedSp, 2);
//        selectionManager.selectEntity(ent, EditorSelectionManager.SelectionMode.Additive);
//        selectionManager.calculateSelectionCenter();
//        transformManager.setTransformToolType(EditorTransformManager.TransformToolType.MoveTool);        
        
        
        System.out.println(selectedSp.getUserData("EntityID"));
    }        
    }

}
