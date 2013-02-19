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
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
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
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.RadioButtonGroupStateChangedEvent;
import de.lessvoid.nifty.controls.RadioButtonStateChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.TextFieldChangedEvent;
import de.lessvoid.nifty.controls.textfield.TextFieldControl;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.omg.CosNaming.NameComponent;

/**
 *
 * @author mifth
 */
public class EditorGuiManager extends AbstractAppState implements ScreenController {

    private Screen screen;
    private Nifty nifty;
    private SimpleApplication application;
    private Node gridNode, rootNode, guiNode;
    private AssetManager assetManager;
    private ViewPort guiViewPort;
    private EditorBaseManager base;
    private Element popupElement;
    private ListBox entitiesListBox, sceneObjectsListBox;

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
        setLight();

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
        popupElement = nifty.createPopup("popupMoveToLayer");
        popupElement.disable();
        screen.getFocusHandler().resetFocusElements();

        // ListBoxes
        entitiesListBox = nifty.getScreen("start").findNiftyControl("entitiesListBox", ListBox.class);
        sceneObjectsListBox = nifty.getScreen("start").findNiftyControl("sceneObjectsListBox", ListBox.class);
        sceneObjectsListBox.changeSelectionMode(ListBox.SelectionMode.Multiple, false);

        //Temp
        nifty.getScreen("start").findNiftyControl("entityList1", TextField.class).setText("/home/mifth/jMonkeyProjects/AD/ad/trunk/ADAssets/assets/Scripts/Entities/entities.json");
        nifty.getScreen("start").findNiftyControl("scenePath1", TextField.class).setText("/home/mifth/jMonkeyProjects/AD/ad/trunk/ADAssets/assets");
//        base.getSceneManager().findFiles("/home/mifth/jMonkeyProjects/AD/ad/trunk/ADAssets/assets", "j3o");

        nifty.gotoScreen("start"); // start the screen 
        screen.getFocusHandler().resetFocusElements();
    }

    /**
     * This is called when the RadioButton selection has changed.
     */
    @NiftyEventSubscriber(id = "RadioGroup-1")
    public void onRadioGroup1Changed1(final String id, final RadioButtonGroupStateChangedEvent event) {

        if (event.getSelectedId().equals("mouse_sel")) {
            setMouseSelection();
        } else if (event.getSelectedId().equals("rectangle_sel")) {
            setRectangleSelection();
        }
    }

    /**
     * This is called when the RadioButton selection has changed.
     */
    @NiftyEventSubscriber(id = "RadioGroup-2")
    public void onRadioGroup1Changed2(final String id, final RadioButtonGroupStateChangedEvent event) {

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
        base.getTransformTool().setTransformType(EditorTransformManager.TransformToolType.MoveTool);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setRotateManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformTool().setTransformType(EditorTransformManager.TransformToolType.RotateTool);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setScaleManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformTool().setTransformType(EditorTransformManager.TransformToolType.ScaleTool);
//        screen.getFocusHandler().resetFocusElements();
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
        base.getTransformTool().setTrCoordinates(EditorTransformManager.TransformCoordinates.LocalCoords);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setWorldCoords() {
        base.getTransformTool().setTrCoordinates(EditorTransformManager.TransformCoordinates.WorldCoords);
//        screen.getFocusHandler().resetFocusElements();
    }

    public void setViewCoords() {
        base.getTransformTool().setTrCoordinates(EditorTransformManager.TransformCoordinates.ViewCoords);
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
        base.getSceneManager().newScene();
//        screen.getFocusHandler().resetFocusElements();
    }

    public void LoadSceneButton() {
        base.getSceneManager().loadScene();
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
        for (int i = 0; i < 5; i++) {
            String strID = "scenePath" + (i + 1);
            String str = nifty.getScreen("start").findNiftyControl(strID, TextField.class).getDisplayedText();

//            System.out.println(str + strID);
            if (str != null && str.length() > 0) {
                base.getSceneManager().addAsset(str);
            }
        }

//        // update entities Lists
//        for (int i = 0; i < 5; i++) {
//            String strID = "entityList" + (i + 1);
//            String str = nifty.getScreen("start").findNiftyControl(strID, TextField.class).getDisplayedText();
//
////            System.out.println(str + strID);
//            if (str != null && str.length() > 0) {
//                base.getSceneManager().addEntitiesList(str);
//            }
//        }

        // update list of all entities
        ConcurrentHashMap<String, String> entList = base.getSceneManager().getEntitiesListsList();
        entitiesListBox.clear();
        for (String str : entList.keySet()) {
            entitiesListBox.addItem(str);
        }

//        screen.getFocusHandler().resetFocusElements();
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
            sceneObjectsListBox.addItem(nameComp.getName() + "(" + id + ")");
            sceneObjectsListBox.sortAllItems();
            setSelectedObjectsList();
        }

//        screen.getFocusHandler().resetFocusElements();
    }

    // This is just visual representation of selected objects
    protected void setSelectedObjectsList() {

        List<Long> selList = base.getSelectionManager().getSelectionList();

        for (Object indexDeselect : sceneObjectsListBox.getSelection()) {
            sceneObjectsListBox.deselectItem(indexDeselect);
        }

        for (Long id : selList) {
            EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
            String objectString = nameComp.getName() + "(" + id + ")";
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
    }

    // select entities from the list of seceneObjectsList
    public void selectEntitiesButton() {
//        List<Long> selList = base.getSelectionManager().getSelectionList();
        base.getSelectionManager().clearSelectionList();
        for (Object obj : sceneObjectsListBox.getSelection()) {
            String objStr = (String) obj;
            long id = Long.valueOf(objStr.substring(objStr.indexOf("(") + 1, objStr.indexOf(")")));
            Node entNode = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();

            // check if entity is in selected layer
            Node selectableNode = (Node) rootNode.getChild("selectableNode");
            boolean isEntityInLayer = false;
            for (Spatial sp : selectableNode.getChildren()) {
                Node selectedLayer = (Node) sp;
                if (entNode.hasAncestor(selectableNode)) {
                    isEntityInLayer = true;
                }
            }

            // if entity is in a selected layer, so it will be selected
            if (isEntityInLayer) {
                base.getSelectionManager().getSelectionList().add(id);
                base.getSelectionManager().createSelectionBox(entNode);
            }

        }
        base.getSelectionManager().calculateSelectionCenter();
        setSelectedObjectsList();
//        screen.getFocusHandler().resetFocusElements();
    }

    public void removeSelectedButton() {
        base.getSelectionManager().clearSelectionList();
        base.getSelectionManager().calculateSelectionCenter();

        for (Object obj : sceneObjectsListBox.getSelection()) {
            String objStr = (String) obj;
            long id = Long.valueOf(objStr.substring(objStr.indexOf("(") + 1, objStr.indexOf(")")));
            base.getSceneManager().removeEntityObject(id);
            sceneObjectsListBox.removeItem(obj);
        }
        setSelectedObjectsList();
//        screen.getFocusHandler().resetFocusElements();
    }

    public void cloneSelectedButton() {
        if (base.getSelectionManager().getSelectionList().size() > 0) {
            base.getSceneManager().cloneSelectedEntities();
        }
//        setSelectedObjectsList();
//        screen.getFocusHandler().resetFocusElements();        

    }

    public void switchLayer(String srtinG) {
        int iInt = Integer.valueOf(srtinG);
        Node activeLayer = base.getLayerManager().getActiveLayer(); // active layer
        Node layerToSwitch = base.getLayerManager().getLayer(iInt); // layer to switch on/off
        Node selectableNode = (Node) rootNode.getChild("selectableNode");

        Object isEnabledObj = layerToSwitch.getUserData("isEnabled");
        boolean isEnabled = (Boolean) isEnabledObj;

        // Switching off
        if (isEnabled == true) {
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
            popupElement.enable();
            nifty.showPopup(nifty.getCurrentScreen(), popupElement.getId(), null);
            screen.getFocusHandler().resetFocusElements();
        } else {
            nifty.closePopup(popupElement.getId());
            popupElement.disable();
            screen.getFocusHandler().resetFocusElements();
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

        // clear selection if olayer is inactive
        Object boolObj = base.getLayerManager().getLayer(iInt).getUserData("isEnabled");
        boolean bool = (Boolean) boolObj;
        if (bool == false) {
            // remove selection boxes
            for (Long idToRemove : base.getSelectionManager().getSelectionList()) {
                base.getSelectionManager().removeSelectionBox((Node) base.getSpatialSystem().getSpatialControl(idToRemove).getGeneralNode());
            }
            base.getSelectionManager().clearSelectionList();
            base.getSelectionManager().calculateSelectionCenter();
        }

        nifty.closePopup(popupElement.getId());
        popupElement.disable();
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

    private void setLight() {

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.8f, -0.6f, -0.08f).normalizeLocal());
        dl.setColor(new ColorRGBA(1, 1, 1, 1));
        rootNode.addLight(dl);

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
        //TODO: implement behavior during runtime
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
