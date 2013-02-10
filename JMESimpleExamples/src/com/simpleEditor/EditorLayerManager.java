/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mifth
 */
public class EditorLayerManager {

    private AssetManager assetMan;
    private Node root, guiNode;
    private Application app;
    private EditorBaseManager base;
    private Node selectableNode;
    private static List<Node> layersList = new ArrayList<Node>();
    private Node activeLayer;

    public EditorLayerManager(Application app, EditorBaseManager base) {
        this.app = app;
        this.base = base;
        assetMan = app.getAssetManager();
        root = (Node) this.app.getViewPort().getScenes().get(0);
        guiNode = (Node) this.app.getGuiViewPort().getScenes().get(0);
        selectableNode = (Node) root.getChild("selectableNode");
        
        createLayers();
    }

    private void createLayers() {
        for (int i = 0; i < 20; i++) {
            Node layerNode = new Node("layerNode_" + (i + 1));
            layerNode.setUserData("LayerNumber", i + 1);
            layerNode.setUserData("isEnabled", false);
            layerNode.setUserData("isActive", false);
            layersList.add(layerNode);

            // set default active layer
            if (i + 1 == 1) {
                selectableNode.attachChild(layerNode);
                layerNode.setUserData("isEnabled", true);
                layerNode.setUserData("isActive", true);
                activeLayer = layerNode;
            }
        }
    }

    protected Node getLayer(int layerNumber) {
        Node nd = layersList.get(layerNumber-1);  // compensate the list number
        return nd;
    }

    protected List <Node> getLayers() {
        return layersList;
    }
}