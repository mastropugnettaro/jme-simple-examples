/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.entitysystem.EntityNameComponent;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Line;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.RadioButtonGroupStateChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mifth
 */
public class EditorGuiManager extends AbstractAppState implements ScreenController {

    private Screen screen;
    private static Nifty nifty;
    private SimpleApplication application;
    private Node gridNode, rootNode, guiNode;
    private AssetManager assetManager;
    private ViewPort guiViewPort;
    private EditorBaseManager base;
    private Element popupMoveToLayer, popupEditComponent;
    private ListBox entitiesListBox, sceneObjectsListBox, componentsListBox;
    private long lastIdOfComponentList, idComponentToChange;

    public EditorGuiManager(EditorBaseManager base) {
        this.base = base;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {

        super.initialize(stateManager, app);
        application = (SimpleApplication) app;
        rootNode = application.getRootNode();
        assetManager = app.getAssetManager();
        guiNode = application.getGuiNode();
        guiViewPort = application.getGuiViewPort();

        createGrid();
//        createSimpleGui();
        setTempLighting();

        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(application.getAssetManager(),
                application.getInputManager(),
                application.getAudioRenderer(),
                guiViewPort);

        nifty = niftyDisplay.getNifty();
//     nifty.loadStyleFile("nifty-default-styles.xml");
//     nifty.loadControlFile("nifty-default-controls.xml");        
        nifty.fromXml("Interface/basicGui.xml", "start", this);


        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);
        application.getInputManager().setCursorVisible(true);

//    Element niftyElement = nifty.getCurrentScreen().findElementByName("button1");
//    niftyElement.getElementInteraction().getPrimary().setOnClickMethod(new NiftyMethodInvoker(nifty, "printGo()", this));


        // Set Logger for only warnings     
        Logger root = Logger.getLogger("");
        Handler[] handlers = root.getHandlers();
        for (int i = 0; i < handlers.length; i++) {
            if (handlers[i] instanceof ConsoleHandler) {
                ((ConsoleHandler) handlers[i]).setLevel(Level.WARNING);
            }
        }


        // set checkboxes for layers
        CheckBox lastEnabled = null;
        for (int i = 0; i < 20; i++) {
            CheckBox cb = nifty.getScreen("start").findNiftyControl("layer" + (i + 1), CheckBox.class);
            Node layer = base.getLayerManager().getLayer(i + 1);
            Object isEnabledObj = layer.getUserData("isEnabled");
            boolean isEnabled = (Boolean) isEnabledObj;
            if (isEnabled) {

                cb.check();
                lastEnabled = cb;
            } else {
                cb.uncheck();
            }
        }


        // SET THE LAYER ACTIVE (Red color)
        Node activeLayer = base.getLayerManager().getActiveLayer();
        if (activeLayer != null) {
            nifty.getScreen("start").getFocusHandler().resetFocusElements();
            Element selectImage = nifty.getScreen("start").findElementByName(base.getLayerManager().getActiveLayer().getName());
            selectImage.startEffect(EffectEventId.onFocus);
        } // SET LAST SELECTED LAYER (IF IT PARSES NOT SO GOOD)
        else if (activeLayer == null && lastEnabled != null) {

            nifty.getScreen("start").getFocusHandler().resetFocusElements();
            Element selectImage = lastEnabled.getElement();
            selectImage.startEffect(EffectEventId.onFocus);
        }

        // set popup test
        popupMoveToLayer = nifty.createPopup("popupMoveToLayer");
        popupMoveToLayer.disable();
        screen.getFocusHandler().resetFocusElements();

        // set popup test
        popupEditComponent = nifty.createPopup("popupEditComponent");
        popupEditComponent.disable();
        screen.getFocusHandler().resetFocusElements();

        // ListBoxes
        entitiesListBox = nifty.getScreen("start").findNiftyControl("entitiesListBox", ListBox.class);
        sceneObjectsListBox = nifty.getScreen("start").findNiftyControl("sceneObjectsListBox", ListBox.class);
        componentsListBox = nifty.getScreen("start").findNiftyControl("componentsListBox", ListBox.class);
        sceneObjectsListBox.changeSelectionMode(ListBox.SelectionMode.Multiple, false);

        //Temp
//        nifty.getScreen("start").findNiftyControl("entityList1", TextField.class).setText("/home/mifth/jMonkeyProjects/AD/ad/trunk/ADAssets/assets/Scripts/Entities/entities.json");
        nifty.getScreen("start").findNiftyControl("scenePath1", TextField.class).setText("/home/mifth/jMonkeyProjects/AD/ad/trunk/ADAssets/assets");
//        base.getSceneManager().findFiles("/home/mifth/jMonkeyProjects/AD/ad/trunk/ADAssets/assets", "j3o");

        nifty.gotoScreen("start"); // start the screen 
        screen.getFocusHandler().resetFocusElements();
    }

    protected void clearGui() {
        // clear gui lists
        entitiesListBox.clear();
        sceneObjectsListBox.clear();
        componentsListBox.clear();

        // clear layers
        for (int i = 0; i < 20; i++) {
            CheckBox cb = screen.findNiftyControl("layer" + (i + 1), CheckBox.class);
            cb.uncheck();
            Element selectActiveLayerImage = screen.findElementByName("layer" + (i + 1));
            selectActiveLayerImage.stopEffect(EffectEventId.onFocus);
            selectActiveLayerImage.startEffect(EffectEventId.onEnabled);
        }

        // clear assets
        for (int i = 0; i < 7; i++) {
            String strID = "scenePath" + (i + 1);
            nifty.getScreen("start").findNiftyControl(strID, TextField.class).setText("");
        }

        screen.getFocusHandler().resetFocusElements();

    }

    public static Nifty getNifty() {
        return nifty;
    }

    /**
     * This is called when the RadioButton selection has changed.
     */
    @NiftyEventSubscriber(id = "RadioGroupConstraints")
    public void RadioGroupConstraintsChanged(final String id, final RadioButtonGroupStateChangedEvent event) {

        if (event.getSelectedId().equals("constraint_none")) {
            base.getTransformManager().getConstraintTool().setConstraint(0);
        } else if (event.getSelectedId().equals("constraint_1")) {
            base.getTransformManager().getConstraintTool().setConstraint(1.0f);
        } else if (event.getSelectedId().equals("constraint_5")) {
            base.getTransformManager().getConstraintTool().setConstraint(5.0f);
        } else if (event.getSelectedId().equals("constraint_10")) {
            base.getTransformManager().getConstraintTool().setConstraint(10.0f);
        }
    }    
    
    /**
     * This is called when the RadioButton selection has changed.
     */
    @NiftyEventSubscriber(id = "RadioGroupSelection")
    public void RadioGroupSelectionChanged(final String id, final RadioButtonGroupStateChangedEvent event) {

        if (event.getSelectedId().equals("mouse_sel")) {
            setMouseSelection();
        } else if (event.getSelectedId().equals("rectangle_sel")) {
            setRectangleSelection();
        }
    }

    /**
     * This is called when the RadioButton selection has changed.
     */
    @NiftyEventSubscriber(id = "RadioGroupSelectionAdditive")
    public void RadioGroupSelectionAdditiveChanged(final String id, final RadioButtonGroupStateChangedEvent event) {

        if (event.getSelectedId().equals("normal_sel")) {
            setNormalSelection();
        } else if (event.getSelectedId().equals("additive_sel")) {
            setAdditiveSelection();
        }
    }

//    // for sceneObjectsListBox manipulation
//    @NiftyEventSubscriber(id = "sceneObjectsListBox")
//    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent changed) {
//    }
    public void setMoveManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformManager().setTransformType(EditorTransformManager.TransformToolType.MoveTool);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setRotateManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformManager().setTransformType(EditorTransformManager.TransformToolType.RotateTool);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setScaleManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformManager().setTransformType(EditorTransformManager.TransformToolType.ScaleTool);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void clearTransform(String transformType) {
        for (Long id : base.getSelectionManager().getSelectionList()) {
            Node entity = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            if (transformType.equals("Translation")) {
                entity.setLocalTranslation(new Vector3f());
            } else if (transformType.equals("Rotation")) {
                entity.setLocalRotation(new Quaternion());
            } else if (transformType.equals("Scale")) {
                entity.setLocalScale(new Vector3f(1, 1, 1));
            }

        }
        base.getSelectionManager().calculateSelectionCenter();

        // set history
        base.getHistoryManager().prepareNewHistory();
        base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
        base.getHistoryManager().getHistoryList().get(base.getHistoryManager().getHistoryCurrentNumber()).setDoTransform(true);
    }

    public void setGrid() {
        int indexGrid = rootNode.getChildIndex(gridNode);
        if (indexGrid == -1) {
            rootNode.attachChild(gridNode);
        } else {
            rootNode.detachChild(gridNode);
        }
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setMouseSelection() {
        base.getSelectionManager().setSelectionTool(EditorSelectionManager.SelectionToolType.MouseClick);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setRectangleSelection() {
        base.getSelectionManager().setSelectionTool(EditorSelectionManager.SelectionToolType.Rectangle);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setLocalCoords() {
        base.getTransformManager().setTrCoordinates(EditorTransformManager.TransformCoordinates.LocalCoords);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setWorldCoords() {
        base.getTransformManager().setTrCoordinates(EditorTransformManager.TransformCoordinates.WorldCoords);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setViewCoords() {
        base.getTransformManager().setTrCoordinates(EditorTransformManager.TransformCoordinates.ViewCoords);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setAdditiveSelection() {
        base.getSelectionManager().setSelectionMode(EditorSelectionManager.SelectionMode.Additive);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setNormalSelection() {
        base.getSelectionManager().setSelectionMode(EditorSelectionManager.SelectionMode.Normal);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void newSceneButton() {
        clearGui();
        base.getSceneManager().newScene();
    }

    public void LoadSceneButton() {
        clearGui();
        base.getSceneManager().newScene();
        base.getSceneManager().loadScene();

        // reload assets lists
        int guiAssetLine = 1;
        for (String obj : base.getSceneManager().getAssetsList()) {
            // show assets at the gui
            if (guiAssetLine <= 7) {
                String strAssetLine = "scenePath" + guiAssetLine;
                nifty.getScreen("start").findNiftyControl(strAssetLine, TextField.class).setText((String) obj);
                guiAssetLine += 1;
            }

        }

        // update list of all entities
        ConcurrentHashMap<String, String> entList = base.getSceneManager().getEntitiesListsList();
        entitiesListBox.clear();
        for (String str : entList.keySet()) {
            entitiesListBox.addItem(str);
        }


        // update list of objects
        for (Node ndLayer : base.getLayerManager().getLayers()) {
            for (Spatial spEntity : ndLayer.getChildren()) {
                Object obj = spEntity.getUserData("EntityID");
                long idObj = (Long) obj;
                EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(idObj, EntityNameComponent.class);
                sceneObjectsListBox.addItem(nameComp.getName());
            }

        }

//        sceneObjectsListBox.
//        System.out.println(base.getEntityManager().getAllControls().size());
//        screen.getFocusHandler().resetFocusElements();
    }

    public void saveSceneButton() {
        base.getSceneManager().saveScene();
//        screen.getFocusHandler().resetFocusElements();
    }

    public void saveAsNewSceneButton() {
        base.getSceneManager().saveAsNewScene();
//        screen.getFocusHandler().resetFocusElements();
    }

    public ListBox getEntitiesListBox() {
        return entitiesListBox;
    }

    public ListBox getSceneObjectsListBox() {
        return sceneObjectsListBox;
    }

    public void updateAssetsButton() {
        // update assets
        base.getSceneManager().clearAssets();

        for (int i = 0; i < 7; i++) {
            String strID = "scenePath" + (i + 1);
            String str = nifty.getScreen("start").findNiftyControl(strID, TextField.class).getDisplayedText();

//            System.out.println(str + strID);
            if (str != null && str.length() > 0) {
                base.getSceneManager().addAsset(str);
            }
        }

        // update list of all entities
        ConcurrentHashMap<String, String> entList = base.getSceneManager().getEntitiesListsList();
        entitiesListBox.clear();
        for (String str : entList.keySet()) {
            entitiesListBox.addItem(str);
        }
    }

    public void addEntityToSceneButton() {
        // create entity
        if (entitiesListBox.getSelection().size() > 0) {
            long id = base.getSceneManager().addEntityToScene(entitiesListBox.getSelection().get(0).toString());

            // clear selection
            base.getSelectionManager().clearSelectionList();
            base.getSelectionManager().selectEntity(id, base.getSelectionManager().getSelectionMode());
            base.getSelectionManager().calculateSelectionCenter();

            // add entty to sceneList
            EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
            sceneObjectsListBox.addItem(nameComp.getName());
            sceneObjectsListBox.sortAllItems();
            setSelectedObjectsList();

            // set history
            base.getHistoryManager().prepareNewHistory();
            base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
        }
    }

    // This is just visual representation of selected objects
    protected void setSelectedObjectsList() {

        List<Long> selList = base.getSelectionManager().getSelectionList();

        for (Object indexDeselect : sceneObjectsListBox.getSelection()) {
            sceneObjectsListBox.deselectItem(indexDeselect);
        }

        for (Long id : selList) {
            EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
            String objectString = nameComp.getName();
            sceneObjectsListBox.selectItem(objectString);
        }
    }

    public void removeClonesButton() {
        if (entitiesListBox.getSelection().size() > 0) {
            base.getSceneManager().removeClones(entitiesListBox.getSelection().get(0).toString());
        }
        base.getGuiManager().getSceneObjectsListBox().sortAllItems();
        base.getGuiManager().setSelectedObjectsList();
        screen.getFocusHandler().resetFocusElements();

//        // set history
//        base.getHistoryManager().prepareNewHistory();
//        base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
    }

    // select entities from the list of seceneObjectsList
    public void selectEntitiesButton() {
//        List<Long> lectlList = base.getSelectionManager().getSelectionList();

        if (sceneObjectsListBox.getSelection().size() > 0) {

            base.getSelectionManager().clearSelectionList();
            for (Object obj : sceneObjectsListBox.getSelection()) {
                String objStr = (String) obj;
                long id = Long.valueOf(objStr.substring(objStr.indexOf("_IDX") + 4, objStr.length()));
                Node entNode = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
                System.out.println(objStr.substring(objStr.indexOf("_IDX") + 4, objStr.length()));

                // check if entity is in selected layer
                Node possibleLayer = (Node) entNode.getParent();
                if (possibleLayer != null) {
                    Object isEnabledObj = possibleLayer.getUserData("isEnabled");
                    if (isEnabledObj != null) {
                        boolean isEnabled = (Boolean) isEnabledObj;
                        if (isEnabled == true) {
                            base.getSelectionManager().selectEntity(id, EditorSelectionManager.SelectionMode.Additive);
                        }
                    }
                }
            }
            base.getSelectionManager().calculateSelectionCenter();
            setSelectedObjectsList();

            // set history
            base.getHistoryManager().prepareNewHistory();
            base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
        }
    }

    public void removeSelectedButton() {
        base.getSelectionManager().clearSelectionList();
        base.getSelectionManager().calculateSelectionCenter();

        for (Object obj : sceneObjectsListBox.getSelection()) {
            String objStr = (String) obj;
            long id = Long.valueOf(objStr.substring(objStr.indexOf("_IDX") + 4, objStr.length()));
            base.getSceneManager().removeEntityObject(id);
            sceneObjectsListBox.removeItem(obj);
        }
        setSelectedObjectsList();
    }

    public void cloneSelectedButton() {
        if (base.getSelectionManager().getSelectionList().size() > 0) {
            List<Long> list = base.getSceneManager().cloneSelectedEntities();
            for (Long id : list) {
                EntityNameComponent newRealName = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
                base.getGuiManager().getSceneObjectsListBox().addItem(newRealName.getName());
            }
//            setSelectedObjectsList();

//            // set history
//            base.getHistoryManager().prepareNewHistory();
//            base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
        }
    }

    public void addComponentButton() {
        // if entity is selected
        if (base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            idComponentToChange = lastIdOfComponentList; // set emp id to change

            popupEditComponent.enable();
            nifty.showPopup(nifty.getCurrentScreen(), popupEditComponent.getId(), null);

            popupEditComponent.findNiftyControl("entityDataName", TextField.class).setText("");
            popupEditComponent.findNiftyControl("entityData", TextField.class).setText("");

            popupEditComponent.getFocusHandler().resetFocusElements();

        }

    }

    public void removeSelectedComponentButton() {
        if (componentsListBox.getSelection().size() > 0
                && base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            String strName = (String) componentsListBox.getSelection().get(0);
            base.getDataManager().getEntityData(lastIdOfComponentList).remove(strName);

            componentsListBox.removeItem(strName);
        }

    }

    public void editComponent() {

        if (componentsListBox.getSelection().size() > 0) {
            // textFields
            String dataComponentName = (String) componentsListBox.getSelection().get(0);

            // if entity is selected
            if (base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
                idComponentToChange = lastIdOfComponentList; // set emp id to change

                popupEditComponent.enable();
                nifty.showPopup(nifty.getCurrentScreen(), popupEditComponent.getId(), null);

                ConcurrentHashMap<String, String> data = base.getDataManager().getEntityData(idComponentToChange);
                popupEditComponent.findNiftyControl("entityDataName", TextField.class).setText(dataComponentName);
                popupEditComponent.findNiftyControl("entityData", TextField.class).setText(data.get(dataComponentName));

                popupEditComponent.getFocusHandler().resetFocusElements();
            }
        }
    }

    public void copyComponentToSelectedEntityButton() {
        if (componentsListBox.getSelection().size() > 0
                && base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            String strDataName = (String) componentsListBox.getSelection().get(0);
            String strData = base.getDataManager().getEntityData(lastIdOfComponentList).get(strDataName);
            List<Long> list = base.getSelectionManager().getSelectionList();
            for (long id : list) {
                if (id != lastIdOfComponentList) {
                    base.getDataManager().getEntityData(id).put(strDataName, strData);
                }
            }
        }
    }

    public void removeComponentFromSelectedEntityButton() {
        if (componentsListBox.getSelection().size() > 0
                && base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            String strDataName = (String) componentsListBox.getSelection().get(0);
            List<Long> list = base.getSelectionManager().getSelectionList();
            for (long id : list) {
                if (id != lastIdOfComponentList
                        && base.getDataManager().getEntityData(id).containsKey(strDataName) == true) {
                    base.getDataManager().getEntityData(id).remove(strDataName);
                }
            }
        }
    }

    public void finishEditComponent(String bool) {
        boolean boo = Boolean.valueOf(bool);
        // if entity is selected

        if (boo) {
            ConcurrentHashMap<String, String> data = base.getDataManager().getEntityData(idComponentToChange);

            String newDataName = popupEditComponent.findNiftyControl("entityDataName", TextField.class).getDisplayedText();
            String newData = popupEditComponent.findNiftyControl("entityData", TextField.class).getDisplayedText();
            data.put(newDataName, newData);

            if (base.getSelectionManager().getSelectionList().size() > 0
                    && base.getSelectionManager().getSelectionList().get(base.getSelectionManager().getSelectionList().size() - 1) == idComponentToChange) {
                if (componentsListBox.getItems().contains(newDataName) == false) {
                    componentsListBox.addItem(newDataName);
                }
            }
        }

        nifty.closePopup(popupEditComponent.getId());
        popupEditComponent.disable();
        popupEditComponent.getFocusHandler().resetFocusElements();
    }

    public void switchLayer(String srtinG) {
        CheckBox cb = screen.findNiftyControl("layer" + srtinG, CheckBox.class);

        int iInt = Integer.valueOf(srtinG);
        Node activeLayer = base.getLayerManager().getActiveLayer(); // active layer
        Node layerToSwitch = base.getLayerManager().getLayer(iInt); // layer to switch on/off
        Node selectableNode = (Node) rootNode.getChild("selectableNode");

        Object isEnabledObj = layerToSwitch.getUserData("isEnabled");
        boolean isEnabled = (Boolean) isEnabledObj;

        // Switching off
        if (isEnabled == true) {
            //set checkbox effect off
            cb.uncheck();

            // detach layer
            selectableNode.detachChild(layerToSwitch);
            layerToSwitch.setUserData("isEnabled", false);

            // remove layer from selection
            List<Long> selectionList = base.getSelectionManager().getSelectionList();
            for (Spatial sp : layerToSwitch.getChildren()) {
                Object idObj = sp.getUserData("EntityID");
                long id = (Long) idObj;
                if (selectionList.indexOf(id) > -1) {
                    Node removeSelBox = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
                    base.getSelectionManager().removeSelectionBox(removeSelBox);
                    selectionList.remove(id);
                }
            }
            base.getSelectionManager().calculateSelectionCenter();

            // if selected layer is active
            if (activeLayer.equals(layerToSwitch)) {
                // deactivate active and slected layer
                layerToSwitch.setUserData("isActive", false);
                screen.findElementByName(layerToSwitch.getName()).stopEffect(EffectEventId.onFocus);
                screen.getFocusHandler().resetFocusElements();

                // set new active layer
                if (selectableNode.getChildren().size() > 0) {
                    Node nd = (Node) selectableNode.getChild(selectableNode.getChildren().size() - 1);
                    nd.setUserData("isActive", true);
                    base.getLayerManager().setActiveLayer(nd);
                    Element newActive = screen.findElementByName(nd.getName());
                    newActive.startEffect(EffectEventId.onFocus);
                    screen.getFocusHandler().resetFocusElements();
                } else {
                    base.getLayerManager().setActiveLayer(null);
                }
            }
        } // switching on
        else {
            //set checkbox effect on
            cb.check();

            if (activeLayer != null) {

                Element selectActiveLayerImage = screen.findElementByName(activeLayer.getName());
                selectActiveLayerImage.stopEffect(EffectEventId.onFocus);
                selectActiveLayerImage.startEffect(EffectEventId.onEnabled);
                screen.getFocusHandler().resetFocusElements();
                activeLayer.setUserData("isActive", false);
            }


            // SET THE LAYER ACTIVE (Red color)
//            CheckBox cb = screen.findNiftyControl("layer" + (iInt), CheckBox.class);
            Element selectImage = screen.findElementByName(layerToSwitch.getName());
            selectImage.startEffect(EffectEventId.onFocus);
            base.getLayerManager().setActiveLayer(layerToSwitch);

            selectableNode.attachChild(layerToSwitch);
            layerToSwitch.setUserData("isActive", true);
            layerToSwitch.setUserData("isEnabled", true);
        }

        setSelectedObjectsList();
        screen.getFocusHandler().resetFocusElements();
//        System.out.println("sel" + selectableNode.getChildren().size());
    }

    public void moveToLayerEnable(String bool) {
        boolean boolValue = Boolean.valueOf(bool);
        if (boolValue) {
            screen.getFocusHandler().resetFocusElements();
            popupMoveToLayer.enable();
            nifty.showPopup(nifty.getCurrentScreen(), popupMoveToLayer.getId(), null);
            popupMoveToLayer.getFocusHandler().resetFocusElements();
        } else {
            nifty.closePopup(popupMoveToLayer.getId());
            popupMoveToLayer.disable();
            popupMoveToLayer.getFocusHandler().resetFocusElements();
        }

    }

    public void moveToLayer(String srtinG) {
        // move to layer
        int iInt = Integer.valueOf(srtinG);
        List<Long> lst = base.getSelectionManager().getSelectionList();
        for (Long lng : lst) {
            Node moveNode = (Node) base.getSpatialSystem().getSpatialControl(lng).getGeneralNode();
            base.getLayerManager().addToLayer(moveNode, iInt);
        }

        // clear selection if layer is inactive
        Object boolObj = base.getLayerManager().getLayer(iInt).getUserData("isEnabled");
        boolean bool = (Boolean) boolObj;
        if (bool == false) {
            // remove selection boxes
//            for (Long idToRemove : lst) {
//                base.getSelectionManager().removeSelectionBox((Node) base.getSpatialSystem().getSpatialControl(idToRemove).getGeneralNode());
//            }
            base.getSelectionManager().clearSelectionList();
            base.getSelectionManager().calculateSelectionCenter();
        }

        nifty.closePopup(popupMoveToLayer.getId());
        popupMoveToLayer.disable();
        screen.getFocusHandler().resetFocusElements();

    }

    private void createGrid() {
        gridNode = new Node("gridNode");

        //Create a grid plane
        Geometry g = new Geometry("GRID", new Grid(201, 201, 10f));
        Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floor_mat.getAdditionalRenderState().setWireframe(true);
        floor_mat.setColor("Color", new ColorRGBA(0.4f, 0.4f, 0.4f, 0.15f));
        floor_mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        g.setCullHint(Spatial.CullHint.Never);
        g.setShadowMode(RenderQueue.ShadowMode.Off);
        g.setQueueBucket(RenderQueue.Bucket.Transparent);
        g.setMaterial(floor_mat);
        g.center().move(new Vector3f(0f, 0f, 0f));
        gridNode.attachChild(g);

        // Red line for X axis
        final Line xAxis = new Line(new Vector3f(-1000f, 0f, 0f), new Vector3f(1000f, 0f, 0f));
        xAxis.setLineWidth(2f);
        Geometry gxAxis = new Geometry("XAxis", xAxis);
        gxAxis.setModelBound(new BoundingBox());
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1.0f, 0.2f, 0.5f, 0.2f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gxAxis.setCullHint(Spatial.CullHint.Never);
        gxAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gxAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gxAxis.setMaterial(mat);
        gxAxis.setCullHint(Spatial.CullHint.Never);

        gridNode.attachChild(gxAxis);

        // Blue line for Z axis
        final Line zAxis = new Line(new Vector3f(0f, 0f, -1000f), new Vector3f(0f, 0f, 1000f));
        zAxis.setLineWidth(2f);
        Geometry gzAxis = new Geometry("ZAxis", zAxis);
        gzAxis.setModelBound(new BoundingBox());
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.2f, 1.0f, 0.2f, 0.2f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gzAxis.setCullHint(Spatial.CullHint.Never);
        gzAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gzAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gzAxis.setMaterial(mat);
        gzAxis.setCullHint(Spatial.CullHint.Never);
        gridNode.attachChild(gzAxis);

        rootNode.attachChild(gridNode);

    }

    public Node getGridNode() {
        return gridNode;
    }

    private void setTempLighting() {

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.8f, -0.6f, -0.08f).normalizeLocal());
        dl.setColor(new ColorRGBA(1.1f, 1, 0.95f, 1));
        rootNode.addLight(dl);

        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(1, 1, 2, 1));
        rootNode.addLight(al);

        application.getViewPort().setBackgroundColor(ColorRGBA.DarkGray);
    }

    private void createSimpleGui() {

        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize());
        ch.setText("W,A,S,D,Q,Z, MiddleMouseButton, RightMouseButton, Scroll"); // crosshairs
        ch.setColor(new ColorRGBA(1f, 0.8f, 0.1f, 0.3f));
        ch.setLocalTranslation(application.getCamera().getWidth() * 0.1f, application.getCamera().getHeight() * 0.1f, 0);
        guiNode.attachChild(ch);

    }

    @Override
    public void update(float tpf) {
        // This is for componentsList!
        List<Long> selList = base.getSelectionManager().getSelectionList();
        if (selList.size() == 0) {
            componentsListBox.clear();
            lastIdOfComponentList = -1; // just for the case if user will select the same entity
        } else if (selList.get(selList.size() - 1) != lastIdOfComponentList) {
            componentsListBox.clear();
            lastIdOfComponentList = selList.get(selList.size() - 1);
            ConcurrentHashMap<String, String> data = base.getDataManager().getEntityData(lastIdOfComponentList);
            for (String key : data.keySet()) {
                componentsListBox.addItem(key);
            }
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
        //this is called on the OpenGL thread after the AppState has been detached
    }

    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
}
