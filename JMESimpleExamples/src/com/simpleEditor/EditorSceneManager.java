/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.entitysystem.ComponentsControl;
import com.entitysystem.EntityManager;
import com.entitysystem.EntityModelPathComponent;
import com.entitysystem.EntityNameComponent;
import com.entitysystem.EntitySpatialsControl;
import com.entitysystem.EntitySpatialsControl_2;
import com.entitysystem.EntityTransformComponent;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import net.sf.launch4j.formimpl.FileChooser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author mifth
 */
public class EditorSceneManager {
    
    private AssetManager assetMan;
    private Node root, guiNode;
    private Application app;
    private EditorBaseManager base;
    private final JFileChooser mFileCm;
    private FileFilter modFilter = new EditorSceneFilter();
    private String scenePath = null;
    private static List<String> assetsList = new ArrayList<String>();
//    private static List<String> entitiesListsList = new ArrayList<String>();
    private static ConcurrentHashMap<String, String> entitiesList = new ConcurrentHashMap<String, String>();
    private static ConcurrentHashMap<String, Spatial> spatialsList = new ConcurrentHashMap<String, Spatial>();
    private EntityManager entityManager;
    
    public EditorSceneManager(Application app, EditorBaseManager base) {
        
        this.app = app;
        this.base = base;
        assetMan = this.app.getAssetManager();
        root = (Node) this.app.getViewPort().getScenes().get(0);
        guiNode = (Node) this.app.getGuiViewPort().getScenes().get(0);
        
        mFileCm = new JFileChooser();
        mFileCm.addChoosableFileFilter(modFilter);
//        mFileCm.addChoosableFileFilter(texFilter);
        mFileCm.setAcceptAllFileFilterUsed(false);
        mFileCm.setPreferredSize(new Dimension(800, 600));
        
        entityManager = base.getEntityManager();
    }
    
    protected void newScene() {
    }
    
    protected void loadScene() {
        mFileCm.setDialogType(JFileChooser.OPEN_DIALOG);
        mFileCm.setDialogTitle("Load Scene");
        mFileCm.setFileFilter(modFilter);
        int returnVal = mFileCm.showOpenDialog(null);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            File file = mFileCm.getSelectedFile();
            System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRRRR");
        }
    }
    
    protected void saveScene() {
    }
    
    protected void saveAsNewScene() {
        mFileCm.setDialogType(JFileChooser.SAVE_DIALOG);
        mFileCm.setDialogTitle("Save Scene");
        mFileCm.setFileFilter(modFilter);
        int returnVal = mFileCm.showOpenDialog(null);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            File file = mFileCm.getSelectedFile();
            System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRRRR");
        }
    }
    
    protected void addAsset(String path) {
        String thePath = correctPath(path);
        File fl = new File(path);

        // registerLoacetor
        if (fl.exists() && assetsList.contains(thePath) == false) {
            if (thePath.endsWith(".jar") || thePath.endsWith(".zip")) {
                assetMan.registerLocator(thePath, ZipLocator.class);
            } else {
                assetMan.registerLocator(thePath, FileLocator.class);
            }
            
            assetsList.add(thePath);
            findFiles(thePath, "j3o");
        }


//        Node model = (Node) assetMan.loadModel("Models/ships/federation/fed_hunter_01/fed_hunter_01.j3o");
//        root.attachChild(model);
    }

//    protected void addEntitiesList(String path) {
//        String thePath = correctPath(path);
//        File fl = new File(path);
//
//        // registerLoacetor
//        if (fl.exists() && entitiesListsList.containsKey(thePath) == false) {
//            JSONObject js = parseJsonFile(thePath);
//            if (js != null) {
//                entitiesListsList.put(thePath, js);
//            }
////            System.out.println(js.size());
//        }
//
//    }
    private Long createEntityModel(String name, String path) {
        Node activeLayer = base.getLayerManager().getActiveLayer();
        
        if (activeLayer != null) {
            // setup Entity
            Node model = null;
            if (spatialsList.get(path) == null) {
                model = (Node) assetMan.loadModel(path);
                spatialsList.put(path, model);
                model = model.clone(false);
            } else {
                model = (Node) spatialsList.get(path).clone(false);
            }
            
            Vector3f camHelperPosition = base.getCamManager().getCamTrackHelper().getWorldTranslation();
            model.setLocalTranslation(camHelperPosition);
            
            long ent = entityManager.createEntity();
            ComponentsControl components = entityManager.getComponentControl(ent);
            
            EntityModelPathComponent modelPath = new EntityModelPathComponent(path);
            components.setComponent(modelPath);
            
            EntityNameComponent nameComponent = new EntityNameComponent(name + "_ID" + ent);
            components.setComponent(nameComponent);
            model.setName(nameComponent.getName());
            
            EntityTransformComponent transform = new EntityTransformComponent(model.getWorldTransform());
            components.setComponent(transform);

//            // Update components
//            components.setUpdateType(ComponentsControl.UpdateType.staticEntity);
//            System.out.println("YYYYY" + model.toString());
            
            EntitySpatialsControl_2 spatialControl = base.getSpatialSystem().addSpatialControl(model, ent, entityManager.getComponentControl(ent));
            spatialControl.setType(EntitySpatialsControl_2.SpatialType.Node);
            spatialControl.recurseNodeID(model);
            
            activeLayer.attachChild(model);
            
            return ent;
        }
        return null;
    }
    
    protected void removeClones(String name) {
        String nameToRemove = name + "_ID";
        List<Long> selList = base.getSelectionManager().getSelectionList();
        List<Long> idsToRemove = new ArrayList<Long>();
        for (Long id : selList) {

            // remove objects from the scene
            EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
            if (nameComp.getName().indexOf(nameToRemove) == 0) {
                idsToRemove.add(id);
            }
        }
        
        for (Long removeID : idsToRemove) {
            EntityNameComponent nameToRemoveReal = (EntityNameComponent) base.getEntityManager().getComponent(removeID, EntityNameComponent.class);
            base.getGuiManager().getSceneObjectsListBox().removeItem(nameToRemoveReal.getName() + "(" + removeID + ")");
            System.out.println("yeeee" + nameToRemoveReal.getName() + "(" + removeID + ")");
            removeEntityObject(removeID);
        }
        idsToRemove.clear();
        idsToRemove = null;
        base.getSelectionManager().calculateSelectionCenter();
    }
    
    protected void cloneSelectedEntities() {
        List<Long> selectionList = base.getSelectionManager().getSelectionList();
        List<Long> tempList = new ArrayList<Long>();
        for (Long id : selectionList) {
            // selected entity's components
            ComponentsControl compControlSelected = base.getEntityManager().getComponentControl(id);
            EntityModelPathComponent modelPathSelected = (EntityModelPathComponent) compControlSelected.getComponent(EntityModelPathComponent.class);
            Node selectedModel = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            Node layerToClone = selectedModel.getParent();
            EntityNameComponent modelNameSelected = (EntityNameComponent) compControlSelected.getComponent(EntityNameComponent.class);
            
            // new entity
            String selectedName = modelNameSelected.getName().substring(0, modelNameSelected.getName().indexOf("_ID"));
            long newID = createEntityModel(selectedName, modelPathSelected.getModelPath());
            Node newModel = (Node) base.getSpatialSystem().getSpatialControl(newID).getGeneralNode();
            newModel.setLocalTransform(selectedModel.getWorldTransform());
            EntityTransformComponent tr = (EntityTransformComponent) base.getEntityManager().getComponent(newID, EntityTransformComponent.class);
            tr.setTransform(selectedModel.getWorldTransform());
            EntityNameComponent newRealName = (EntityNameComponent) base.getEntityManager().getComponent(newID, EntityNameComponent.class);
            
            tempList.add(newID);
            layerToClone.attachChild(newModel);
            base.getGuiManager().getSceneObjectsListBox().addItem(newRealName.getName() + "(" + newID + ")");
        }
        
        // clear selection
        base.getSelectionManager().clearSelectionList();
        
        for (Long id : tempList) {
            base.getSelectionManager().selectEntity(id, EditorSelectionManager.SelectionMode.Additive);
        }
        base.getSelectionManager().calculateSelectionCenter();
    }
    
    protected void removeEntityObject(long id) {
        // remove item from scene list
        EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
//        base.getGuiManager().getSceneObjectsListBox().removeItem(nameComp.getName() + "(" + id + ")");

        //remove item from selection
        List<Long> selList = base.getSelectionManager().getSelectionList();
        if (selList.contains(id)) {
            selList.remove(id);
            Node nd = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            base.getSelectionManager().removeSelectionBox(nd);
            nd = null;
        }

        // destroy entity
        base.getEntityManager().removeEntity(id);
        base.getSpatialSystem().removeSpatialControl(id);
    }

    // Correct path for Windows OS
    protected String correctPath(String path) {
        String pathCorrected = path;
        
        if (File.separatorChar == '\\') {
            pathCorrected = pathCorrected.replace('\\', '/');
        }
        if (!path.endsWith("/") && path.indexOf(".") != (path.length() - 3)) {
            pathCorrected += "/";
        }
        
        return pathCorrected;
    }
    
    protected JSONObject parseJsonFile(String path) {
        // Load JSON script
        JSONParser json = new JSONParser();
        
        FileReader fileRead = null;
        JSONObject jsObj = null;
        
        try {
            fileRead = new FileReader(new File(path));
        } catch (FileNotFoundException ex) {
            System.out.println("bad JSON file");
        }
        
        try {
            jsObj = (JSONObject) json.parse(fileRead);
        } catch (IOException ex) {
            System.out.println("bad JSON file");
        } catch (org.json.simple.parser.ParseException ex) {
            System.out.println("bad JSON file");
        }
        
        
        try {
            fileRead.close();
        } catch (IOException ex) {
            System.out.println("bad JSON file");
        }
        return jsObj;
    }
    
    protected Long addEntityToScene(String name) {
        return createEntityModel(name, entitiesList.get(name));
    }

    // Recursive search of files
    protected void findFiles(String dirEntity, String fileExtension) {
        System.out.println("ooooooooo LOAD entity Dir : " + dirEntity);
        File dir = new File(dirEntity);
        File[] a = dir.listFiles();
        
        for (File f : a) {
            if (f.isDirectory()) {
                // Recursive search
                System.out.println("****** CHECKing Dir : " + f.getName());
                String recursDir = dirEntity + "/" + f.getName();
                findFiles(recursDir, fileExtension);
            } else if (f.getName().endsWith("." + fileExtension)) {
                
                String strF = f.getName();
                String modelName = f.getName().substring(0, f.getName().indexOf(".j3o"));
                String modelRelativePath = f.getAbsolutePath().substring(assetsList.get(assetsList.size()-1).length(), f.getAbsolutePath().length());
                entitiesList.put(modelName, modelRelativePath);
//                strF = strF.substring(globalDirToFind.length(), strF.length());
                System.out.println("========>>FOUND ENTITY :: " + strF);
            }
        }
    }
    
    protected static List<String> getAssetsList() {
        return assetsList;
    }
    
    protected static ConcurrentHashMap<String, String> getEntitiesListsList() {
        return entitiesList;
    }
}
