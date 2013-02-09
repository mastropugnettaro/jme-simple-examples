/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

/**
 *
 * @author mifth
 */
public class EditorLayerManager {

    private AssetManager assetMan;
    private Node root, guiNode;
    private Application app;
    private EditorBaseManager base;
    private Node layerNode_1, layerNode_2, layerNode_3, layerNode_4, layerNode_5, layerNode_6,
            layerNode_7, layerNode_8, layerNode_9, layerNode_10, layerNode_11, layerNode_12, layerNode_13,
            layerNode_14, layerNode_15, layerNode_16, layerNode_17, layerNode_18, layerNode_19, layerNode_20;

    public EditorLayerManager(Application app, EditorBaseManager base) {
        this.app = app;
        this.base = base;
        assetMan = app.getAssetManager();
        root = (Node) this.app.getViewPort().getScenes().get(0);
        guiNode = (Node) this.app.getGuiViewPort().getScenes().get(0);

        layerNode_1 = new Node("layerNode_1");
        layerNode_1.setUserData("LayerNumber", 1);
        Node selectableNode = (Node) root.getChild("selectableNode");
        selectableNode.attachChild(layerNode_1);

        layerNode_2 = new Node("layerNode_2");
        layerNode_2.setUserData("LayerNumber", 2);
        layerNode_3 = new Node("layerNode_3");
        layerNode_2.setUserData("LayerNumber", 3);
        layerNode_4 = new Node("layerNode_4");
        layerNode_2.setUserData("LayerNumber", 4);
        layerNode_5 = new Node("layerNode_5");
        layerNode_2.setUserData("LayerNumber", 5);
        layerNode_6 = new Node("layerNode_6");
        layerNode_2.setUserData("LayerNumber", 6);
        layerNode_7 = new Node("layerNode_7");
        layerNode_2.setUserData("LayerNumber", 7);
        layerNode_8 = new Node("layerNode_8");
        layerNode_2.setUserData("LayerNumber", 8);
        layerNode_9 = new Node("layerNode_9");
        layerNode_2.setUserData("LayerNumber", 9);
        layerNode_10 = new Node("layerNode_10");
        layerNode_2.setUserData("LayerNumber", 10);
        layerNode_11 = new Node("layerNode_11");
        layerNode_2.setUserData("LayerNumber", 11);
        layerNode_12 = new Node("layerNode_12");
        layerNode_2.setUserData("LayerNumber", 12);
        layerNode_13 = new Node("layerNode_13");
        layerNode_2.setUserData("LayerNumber", 13);
        layerNode_14 = new Node("layerNode_14");
        layerNode_2.setUserData("LayerNumber", 14);
        layerNode_15 = new Node("layerNode_15");
        layerNode_2.setUserData("LayerNumber", 15);
        layerNode_16 = new Node("layerNode_16");
        layerNode_2.setUserData("LayerNumber", 16);
        layerNode_17 = new Node("layerNode_17");
        layerNode_2.setUserData("LayerNumber", 17);
        layerNode_18 = new Node("layerNode_18");
        layerNode_2.setUserData("LayerNumber", 18);
        layerNode_19 = new Node("layerNode_19");
        layerNode_2.setUserData("LayerNumber", 19);
        layerNode_20 = new Node("layerNode_20");
        layerNode_2.setUserData("LayerNumber", 20);
    }

    protected Node getLayer(int layerNumber) {

        Node nd = null;
        
        if (layerNumber == 1) nd = layerNode_1;
        else if (layerNumber == 2) nd = layerNode_2;
        else if (layerNumber == 3) nd = layerNode_3;
        else if (layerNumber == 4) nd = layerNode_4;
        else if (layerNumber == 5) nd = layerNode_5;
        else if (layerNumber == 6) nd = layerNode_6;
        else if (layerNumber == 7) nd = layerNode_7;
        else if (layerNumber == 8) nd = layerNode_8;
        else if (layerNumber == 9) nd = layerNode_9;
        else if (layerNumber == 10) nd = layerNode_10;
        else if (layerNumber == 11) nd = layerNode_11;
        else if (layerNumber == 12) nd = layerNode_12;
        else if (layerNumber == 13) nd = layerNode_13;
        else if (layerNumber == 14) nd = layerNode_14;
        else if (layerNumber == 15) nd = layerNode_15;
        else if (layerNumber == 16) nd = layerNode_16;
        else if (layerNumber == 17) nd = layerNode_17;
        else if (layerNumber == 18) nd = layerNode_18;
        else if (layerNumber == 19) nd = layerNode_19;
        else if (layerNumber == 20) nd = layerNode_20;

        return nd;
    }
    
}
