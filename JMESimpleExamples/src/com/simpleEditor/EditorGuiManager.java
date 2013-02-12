/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

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
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import java.util.List;
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
    private Nifty nifty;
    private SimpleApplication application;
    private Node gridNode, rootNode, guiNode;
    private AssetManager assetManager;
    private ViewPort guiViewPort;
    private EditorBaseManager base;

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
        createSimpleGui();
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

        nifty.gotoScreen("start"); // start the screen 
        screen.getFocusHandler().resetFocusElements();



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
            CheckBox cb = screen.findNiftyControl("layer" + (i + 1), CheckBox.class);
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

        Node activeLayer = base.getLayerManager().getActiveLayer();
        // SET THE LAYER ACTIVE (Red color)
        if (activeLayer != null) {
            screen.getFocusHandler().resetFocusElements();
            Element selectImage = screen.findElementByName(base.getLayerManager().getActiveLayer().getName());
            selectImage.startEffect(EffectEventId.onFocus);
        } // SET LAST SELECTED LAYER (IF IT PARSES NOT SO GOOD)
        else if (activeLayer == null && lastEnabled != null) {

            screen.getFocusHandler().resetFocusElements();
            Element selectImage = lastEnabled.getElement();
            selectImage.startEffect(EffectEventId.onFocus);
        }

        
//        // set popup test
//        Element popupElement = nifty.createPopup("popupExit");
//        nifty.showPopup(nifty.getCurrentScreen(), popupElement.getId(), null);

    }

    public void setMoveManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformTool().setTransformType(EditorTransformManager.TransformToolType.MoveTool);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setRotateManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformTool().setTransformType(EditorTransformManager.TransformToolType.RotateTool);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setScaleManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformTool().setTransformType(EditorTransformManager.TransformToolType.ScaleTool);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setGrid() {
        int indexGrid = rootNode.getChildIndex(gridNode);
        if (indexGrid == -1) {
            rootNode.attachChild(gridNode);
        } else {
            rootNode.detachChild(gridNode);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void setMouseSelection() {
        base.getSelectionManager().setSelectionTool(EditorSelectionManager.SelectionToolType.MouseClick);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setRectangleSelection() {
        base.getSelectionManager().setSelectionTool(EditorSelectionManager.SelectionToolType.Rectangle);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setLocalCoords() {
        base.getTransformTool().setTrCoordinates(EditorTransformManager.TransformCoordinates.LocalCoords);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setWorldCoords() {
        base.getTransformTool().setTrCoordinates(EditorTransformManager.TransformCoordinates.WorldCoords);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setViewCoords() {
        base.getTransformTool().setTrCoordinates(EditorTransformManager.TransformCoordinates.ViewCoords);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setAdditiveSelection() {
        base.getSelectionManager().setSelectionMode(EditorSelectionManager.SelectionMode.Additive);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setNormalSelection() {
        base.getSelectionManager().setSelectionMode(EditorSelectionManager.SelectionMode.Normal);
        screen.getFocusHandler().resetFocusElements();
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
                if (selectionList.indexOf(id) > -1) selectionList.remove(id);
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
        screen.getFocusHandler().resetFocusElements();
//        System.out.println("sel" + selectableNode.getChildren().size());
    }

    private void createGrid() {
        gridNode = new Node("gridNode");

        //Create a grid plane
        Geometry g = new Geometry("GRID", new Grid(1001, 1001, 1f));
        Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floor_mat.getAdditionalRenderState().setWireframe(true);
        floor_mat.setColor("Color", new ColorRGBA(0.3f, 0.3f, 0.3f, 0.1f));
        floor_mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        g.setCullHint(Spatial.CullHint.Never);
        g.setShadowMode(RenderQueue.ShadowMode.Off);
        g.setQueueBucket(RenderQueue.Bucket.Transparent);
        g.setMaterial(floor_mat);
        g.center().move(new Vector3f(0f, 0f, 0f));
        gridNode.attachChild(g);

        // Red line for X axis
        final Line xAxis = new Line(new Vector3f(-500f, 0f, 0f), new Vector3f(500f, 0f, 0f));
        xAxis.setLineWidth(2f);
        Geometry gxAxis = new Geometry("XAxis", xAxis);
        gxAxis.setModelBound(new BoundingBox());
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1.0f, 0.2f, 0.2f, 0.2f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gxAxis.setCullHint(Spatial.CullHint.Never);
        gxAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gxAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gxAxis.setMaterial(mat);
        gxAxis.setCullHint(Spatial.CullHint.Never);

        gridNode.attachChild(gxAxis);

        // Blue line for Z axis
        final Line zAxis = new Line(new Vector3f(0f, 0f, -500f), new Vector3f(0f, 0f, 500f));
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

        application.getViewPort().setBackgroundColor(ColorRGBA.Gray);
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
