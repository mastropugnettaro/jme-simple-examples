/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.entitysystem.ComponentsControl;
import com.entitysystem.EntityModelPathComponent;
import com.entitysystem.EntityNameComponent;
import com.entitysystem.EntitySpatialsControl;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.ModelKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.TangentBinormalGenerator;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.json.simple.JSONObject;
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
    private FileFilter modFilter;
    private static List<String> assetsList;
    private static ConcurrentHashMap<String, String> entitiesList;
    private static ConcurrentHashMap<String, Spatial> spatialsList;
//    private EntityManager entityManager;
    private DesktopAssetManager dsk;

    public EditorSceneManager(Application app, EditorBaseManager base) {

        this.app = app;
        this.base = base;
        assetMan = this.app.getAssetManager();
        root = (Node) this.app.getViewPort().getScenes().get(0);
        guiNode = (Node) this.app.getGuiViewPort().getScenes().get(0);

        mFileCm = new JFileChooser();
        mFileCm.addChoosableFileFilter(modFilter);
        mFileCm.setAcceptAllFileFilterUsed(false);
        mFileCm.setPreferredSize(new Dimension(800, 600));
        modFilter = new EditorSceneFilter();

        assetsList = new ArrayList<String>();
        entitiesList = new ConcurrentHashMap<String, String>();
        spatialsList = new ConcurrentHashMap<String, Spatial>();
        dsk = (DesktopAssetManager) assetMan;
//        entityManager = base.getEntityManager();
    }

    protected void newScene() {
        clearScene();
    }

    protected void loadScene() {
        mFileCm.setDialogType(JFileChooser.OPEN_DIALOG);
        mFileCm.setDialogTitle("Load Scene");
        mFileCm.setApproveButtonToolTipText("Open");
        mFileCm.setApproveButtonText("Open");
        mFileCm.setFileFilter(modFilter);
        int returnVal = mFileCm.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = mFileCm.getSelectedFile();

            if (selectedFile.getName().indexOf(".") != 0 && selectedFile.getName().length() > 0) {
                String filePath = selectedFile.getParent();
                filePath = correctPath(filePath);

                String fileName = selectedFile.getName();
                if (fileName.indexOf(".") > 0) {
                    fileName = fileName.substring(0, fileName.indexOf("."));
                }
                String fullPath = filePath + fileName;
                loadSwsFile(fullPath);
                loadSweFile(fullPath);
            }
        }
    }

    protected void loadSwsFile(String filePath) {

        JSONObject jsSettings = loadToJsonFile(filePath + ".sws");

        // version of the editor which the scene was saved
        String SWEVersion = (String) jsSettings.get("EditorVersion");

        // load assets
        JSONObject jsPaths = (JSONObject) jsSettings.get("AssetsPaths");
//        int guiAssetLine = 1;
        for (Object obj : jsPaths.keySet()) {
            addAsset((String)jsPaths.get(obj));

//            // show assets at the gui
//            if (guiAssetLine <= 7) {
//                String strAssetLine = "scenePath" + guiAssetLine;
//                nifty.getScreen("start").findNiftyControl(strAssetLine, TextField.class).setText((String)obj);
//                guiAssetLine += 1;
//            }
        }
        System.out.println("sws is loaded!");
    }

    protected void loadSweFile(String filePath) {
        JSONObject jsScene = loadToJsonFile(filePath + ".swe");

        // set new IDX
        long lastIDX = Long.valueOf((String)jsScene.get("LastIDX")) ;
        base.getEntityManager().setIdx(lastIDX);

        // load layers
        JSONObject jsLayers = (JSONObject) jsScene.get("Layers");
        for (Object layerStrObj : jsLayers.keySet()) {
            JSONObject jslayer = (JSONObject) jsLayers.get(layerStrObj);
            // get layer
            String strLayer = (String) layerStrObj;
            strLayer = strLayer.substring(strLayer.indexOf("layer") + 5, strLayer.length());
            Node layerNode = base.getLayerManager().getLayer(Integer.valueOf(strLayer));
            System.out.println(strLayer + "Layer number");

            // get layer states
            Object isActObj = layerNode.getUserData("isActive");
            boolean isActive = (Boolean) jslayer.get("isActive");
            if (isActive) {
                base.getLayerManager().setActiveLayer(layerNode);
                layerNode.setUserData("isActive", true);
            }

            // don't forget to parse gui layers
            Object isEnObj = layerNode.getUserData("isEnabled");
            boolean isEnabled = (Boolean) jslayer.get("isEnabled");
            if (isEnabled) {
                base.getLayerManager().getSelectableNode().attachChild(layerNode);
                layerNode.setUserData("isEnabled", true);
            }              
            

            // create entities
            JSONObject jsEntities = (JSONObject) jslayer.get("Entities");
            for (Object objID : jsEntities.keySet()) {
                long ID = Long.valueOf((String)objID);
                JSONObject jsEntity = (JSONObject) jsEntities.get(objID);

                String idName = (String) jsEntity.get("IDName");
                idName = idName.substring(0, idName.indexOf("_IDX"));
                
                String idNumber = (String) jsEntity.get("IDName");
                idNumber = idNumber.substring(idNumber.indexOf("_IDX") + 4, idNumber.length());

                System.out.println(idName);
                // create entity
                createEntityModel(idName, entitiesList.get(idName), Long.valueOf(idNumber));
                Node entityNode = (Node) base.getSpatialSystem().getSpatialControl(ID).getGeneralNode();

                //set Transform for the entity
                JSONObject jsTransform = (JSONObject) jsEntity.get("IDTransform");
                Transform entTransform = new Transform();
                entTransform.setTranslation( Float.valueOf((String)jsTransform.get("translationX")), Float.valueOf((String)jsTransform.get("translationY")), 
                        Float.valueOf((String)jsTransform.get("translationZ")));
                entTransform.setRotation(new Quaternion(
                        Float.valueOf((String)jsTransform.get("rotationX")), Float.valueOf((String)jsTransform.get("rotationY")), Float.valueOf((String)jsTransform.get("rotationZ")),
                        Float.valueOf((String)jsTransform.get("rotationW"))));
                entTransform.setScale(Float.valueOf((String)jsTransform.get("scaleX")), Float.valueOf((String)jsTransform.get("scaleY")), Float.valueOf((String)jsTransform.get("scaleZ")));
                entityNode.setLocalTransform(entTransform);
                
                System.out.println(entTransform.toString());
                
                layerNode.attachChild(entityNode);

                //set data components for the entity
                JSONObject jsDataComponents = (JSONObject) jsEntity.get("IDDataComponents");
                for (Object strKey : jsDataComponents.keySet()) {
                    String value = (String)jsDataComponents.get(strKey);
                    base.getDataManager().getEntityData(ID).put((String)strKey, value);
                }
            }
          
        }
    }

    protected void saveScene() {
    }

    protected void saveAsNewScene() {
        mFileCm.setDialogType(JFileChooser.SAVE_DIALOG);
        mFileCm.setDialogTitle("Save Scene");
        mFileCm.setApproveButtonToolTipText("Save");
        String s = "Save";
        mFileCm.setApproveButtonText("Save");
        mFileCm.setFileFilter(modFilter);
        int returnVal = mFileCm.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = mFileCm.getSelectedFile();

            if (selectedFile.getName().indexOf(".") != 0 && selectedFile.getName().length() > 0) {
                String filePath = selectedFile.getParent();
                filePath = correctPath(filePath);

                String fileName = selectedFile.getName();
                if (fileName.indexOf(".") > 0) {
                    fileName = fileName.substring(0, fileName.indexOf("."));
                }
                String fullPath = filePath + fileName;
                saveSwsFile(fullPath);
                saveSweFile(fullPath);
            }
        }
    }

    private void saveSwsFile(String pathToSave) {
        JSONObject saveSceneSettings = new JSONObject();

        // save assets paths
        JSONObject assetsToSave = new JSONObject();
        for (int i = 0; i < assetsList.size(); i++) {

            assetsToSave.put("AssetPath" + i, assetsList.get(i));
        }
        saveSceneSettings.put("AssetsPaths", assetsToSave);

        // save version of the Simple World Editor
        saveSceneSettings.put("EditorVersion", base.getEditorVersoin());
        saveJsonFile(pathToSave + ".sws", saveSceneSettings);

    }

    private void saveSweFile(String pathToSave) {
        JSONObject saveSceneJson = new JSONObject();

        saveSceneJson.put("LastIDX", String.valueOf(base.getEntityManager().getIdx()));

        //save layers
        JSONObject allLayersJs = new JSONObject();
        for (Node layerNode : base.getLayerManager().getLayers()) {
            JSONObject layerToSave = new JSONObject();

            // save layer states
            Object isActObj = layerNode.getUserData("isActive");
            layerToSave.put("isActive", (Boolean) isActObj);
            Object isEnObj = layerNode.getUserData("isEnabled");
            layerToSave.put("isEnabled", (Boolean) isEnObj);

            // save ID entities
            JSONObject entitiesToSave = new JSONObject();
            for (Spatial sp : layerNode.getChildren()) {
                JSONObject entityJSON = new JSONObject();

                Object idObj = sp.getUserData("EntityID");
                long idLong = (Long) idObj;

                // save name
                EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(idLong, EntityNameComponent.class);
                entityJSON.put("IDName", nameComp.getName());

//                        EntityModelPathComponent pathComp = (EntityModelPathComponent) base.getEntityManager().getComponent(idLong, EntityModelPathComponent.class);
                entityJSON.put("IDModel", nameComp.getName().substring(0, nameComp.getName().indexOf("_IDX")));

                // save transforms
                Transform trID = sp.getWorldTransform();
//                trID.getRotation().inverseLocal();
                JSONObject transformToSave = new JSONObject();
                transformToSave.put("translationX", String.valueOf(trID.getTranslation().getX()));
                transformToSave.put("translationY", String.valueOf(trID.getTranslation().getY()));
                transformToSave.put("translationZ", String.valueOf(trID.getTranslation().getZ()));
                transformToSave.put("rotationX", String.valueOf(trID.getRotation().getX()));
                transformToSave.put("rotationY", String.valueOf(trID.getRotation().getY()));
                transformToSave.put("rotationZ", String.valueOf(trID.getRotation().getZ()));
                transformToSave.put("rotationW", String.valueOf(trID.getRotation().getW()));
                transformToSave.put("scaleX", String.valueOf(trID.getScale().getX()));
                transformToSave.put("scaleY", String.valueOf(trID.getScale().getY()));
                transformToSave.put("scaleZ", String.valueOf(trID.getScale().getZ()));
                entityJSON.put("IDTransform", transformToSave);

                // seve data components of entity
                ConcurrentHashMap<String, String> entityData = base.getDataManager().getEntityData(idLong);
                JSONObject dataComponentsToSave = new JSONObject();
                for (String strKey : entityData.keySet()) {
                    dataComponentsToSave.put(strKey, entityData.get(strKey));
                }
                entityJSON.put("IDDataComponents", dataComponentsToSave);

                entitiesToSave.put(String.valueOf(idLong), entityJSON);
            }

            layerToSave.put("Entities", entitiesToSave);
            allLayersJs.put(layerNode.getName(), layerToSave);
        }
        
        saveSceneJson.put("Layers", allLayersJs);
        saveJsonFile(pathToSave + ".swe", saveSceneJson);
        System.out.println("File saved: " + pathToSave + ".swe");
    }

    protected void clearScene() {
        // clear selection
        base.getSelectionManager().clearSelectionList();
        base.getSelectionManager().calculateSelectionCenter();

        // remove all spatials from layers
        base.getLayerManager().clearLayerManager();

        // clear history
        base.getHistoryManager().clearHistory();

        // clear data components
        base.getDataManager().clearEntityData();

        // clear entities
        ConcurrentHashMap<Long, ComponentsControl> allControls = base.getEntityManager().getAllControls();
        for (Long ID : allControls.keySet()) {
            base.getEntityManager().removeEntity(ID);
            base.getSpatialSystem().removeSpatialControl(ID);
        }
        allControls.clear();

        // clear assets list
        clearAssets();
    }

    protected void clearAssets() {
        for (String str : spatialsList.keySet()) {
            dsk.deleteFromCache(new ModelKey(str));
        }
        spatialsList.clear();

        for (String str : assetsList) {
            dsk.unregisterLocator(str, FileLocator.class);
        }
        assetsList.clear();

        dsk.clearCache(); // clear all loaded models        
        dsk.clearAssetEventListeners();
    }

    protected void addAsset(String path) {
        String thePath = correctPath(path);
        File fl = new File(path);

        // registerLoacetor
        if (fl.exists() && assetsList.contains(thePath) == false) {
            dsk.registerLocator(thePath, FileLocator.class);

            assetsList.add(thePath);
            findFiles(thePath, "j3o");
        }
    }

    private Long createEntityModel(String name, String path, Long existedID) {
        Node activeLayer = base.getLayerManager().getActiveLayer();

        if (activeLayer != null) {
            // setup Entity
            Node model = null;
            if (spatialsList.contains(path) == false) {
               Node loadedModel = (Node) dsk.loadModel(path);
                spatialsList.put(path, loadedModel);
                model = loadedModel.clone(false);
            } else {
                model = (Node) spatialsList.get(path).clone(false);
            }

            Vector3f camHelperPosition = base.getCamManager().getCamTrackHelper().getWorldTranslation();
            model.setLocalTranslation(camHelperPosition);


            long ent;
            if (existedID == null) {
                ent = base.getEntityManager().createEntity();
            } else {
                ent = existedID;
            }

            base.getDataManager().setEntityData(ent, new ConcurrentHashMap<String, String>());
            ComponentsControl components = base.getEntityManager().addComponentControl(ent);

            EntityModelPathComponent modelPath = new EntityModelPathComponent(path);
            components.setComponent(modelPath);

            EntityNameComponent nameComponent = new EntityNameComponent(name + "_IDX" + ent);
            components.setComponent(nameComponent);
            model.setName(nameComponent.getName());



            EntitySpatialsControl spatialControl = base.getSpatialSystem().addSpatialControl(model, ent, base.getEntityManager().getComponentControl(ent));
            spatialControl.setType(EntitySpatialsControl.SpatialType.Node);
            spatialControl.recurseNodeID(model);

            activeLayer.attachChild(model);

            return ent;
        }
        return null;
    }

    protected void removeClones(String name) {
        String nameToRemove = name + "_IDX";
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
            base.getGuiManager().getSceneObjectsListBox().removeItem(nameToRemoveReal.getName());
            System.out.println("yeeee" + nameToRemoveReal.getName());
            removeEntityObject(removeID);
        }
        idsToRemove.clear();
        idsToRemove = null;
        base.getSelectionManager().calculateSelectionCenter();
    }

    protected List<Long> cloneSelectedEntities() {
        List<Long> selectionList = base.getSelectionManager().getSelectionList();
        List<Long> tempList = new ArrayList<Long>();
        for (Long idOfSelected : selectionList) {
            // selected entity's components
            ComponentsControl compControlSelected = base.getEntityManager().getComponentControl(idOfSelected);
            EntityModelPathComponent modelPathSelected = (EntityModelPathComponent) compControlSelected.getComponent(EntityModelPathComponent.class);
            Node selectedModel = (Node) base.getSpatialSystem().getSpatialControl(idOfSelected).getGeneralNode();
            Node layerToClone = selectedModel.getParent();
            EntityNameComponent modelNameSelected = (EntityNameComponent) compControlSelected.getComponent(EntityNameComponent.class);

            // new entity
            String selectedName = modelNameSelected.getName().substring(0, modelNameSelected.getName().indexOf("_IDX"));
            long newID = createEntityModel(selectedName, modelPathSelected.getModelPath(), null);
            Node newModel = (Node) base.getSpatialSystem().getSpatialControl(newID).getGeneralNode();
            newModel.setLocalTransform(selectedModel.getWorldTransform());

            // Clone data
            ConcurrentHashMap<String, String> dataOfSelected = base.getDataManager().getEntityData(idOfSelected);
            ConcurrentHashMap<String, String> dataNew = base.getDataManager().getEntityData(newID);
            for (String key : dataOfSelected.keySet()) {
                dataNew.put(key, dataOfSelected.get(key));
            }

            tempList.add(newID);
            layerToClone.attachChild(newModel);
        }

        // clear selection
        base.getSelectionManager().clearSelectionList();

        for (Long id : tempList) {
            base.getSelectionManager().selectEntity(id, EditorSelectionManager.SelectionMode.Additive);
        }
        base.getSelectionManager().calculateSelectionCenter();
        return tempList;
    }

    protected void removeEntityObject(long id) {
        // remove item from scene list
//        EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
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
        base.getDataManager().removeEntityData(id);
    }

    // Correct path for Windows OS
    protected String correctPath(String path) {
        String pathCorrected = path;

        if (File.separatorChar == '\\') {
            pathCorrected = pathCorrected.replace('\\', '/');
        }
        if (!path.endsWith("/") && path.indexOf(".") < 0) {
            pathCorrected += "/";
        }

        return pathCorrected;
    }

    protected void saveJsonFile(String pathToSave, JSONObject saveJson) {
        try {
            File saveFile = new File(pathToSave);
            saveFile.setReadable(true);
            saveFile.setWritable(true);

            FileWriter fileToSave = new FileWriter(saveFile);
            fileToSave.write(saveJson.toJSONString());
            fileToSave.flush();
            fileToSave.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected JSONObject loadToJsonFile(String path) {
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
            fileRead.close();
        } catch (IOException ex) {
            System.out.println("bad JSON file");
        } catch (org.json.simple.parser.ParseException ex) {
            System.out.println("bad JSON file Parser");
        }

        return jsObj;
    }

    protected Long addEntityToScene(String name) {
        return createEntityModel(name, entitiesList.get(name), null);
    }

    // Recursive search of files
    protected void findFiles(String dirEntity, String fileExtension) {
        System.out.println("ooooooooo LOAD entity Dir : " + dirEntity);
        File dir = new File(dirEntity);
        File[] a = dir.listFiles();

        for (File f : a) {
            if (f.isDirectory() && f.getName().indexOf("svn") < 0) {
                // Recursive search
                System.out.println("****** CHECKing Dir : " + f.getName());
                String recursDir = dirEntity + "/" + f.getName();
                findFiles(recursDir, fileExtension);
            } else if (f.getName().endsWith("." + fileExtension)) {

                String strF = f.getName();
                String modelName = f.getName().substring(0, f.getName().indexOf(".j3o"));
                String modelRelativePath = f.getAbsolutePath().substring(assetsList.get(assetsList.size() - 1).length(), f.getAbsolutePath().length());
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
