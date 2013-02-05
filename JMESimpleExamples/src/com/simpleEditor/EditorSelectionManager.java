/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.math.Transform;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mifth
 */
public class EditorSelectionManager extends AbstractControl{

    private AssetManager assetMan;
    private Node root, guiNode;
    private Application app;
    private EditorBaseManager base;
    private static List<Spatial> selectionList = new ArrayList<Spatial>();
    private Transform selectionCenter;
    private SelectionToolType selectionTool;

    protected enum SelectionToolType {
        All, Mouse, Rectangle, Polygon
    };    
   
    protected enum SelectionMode {
        Normal, Additive, Substractive
    };        
    
    public EditorSelectionManager(Application app, EditorBaseManager base) {

        this.app = app;
        this.base = base;
        assetMan = this.app.getAssetManager();
        root = (Node) this.app.getViewPort().getScenes().get(0);
        guiNode = (Node) this.app.getGuiViewPort().getScenes().get(0);

    }
    
    protected void selectEntity(Spatial sp, SelectionMode mode) {

        if (mode == SelectionMode.Normal) {
            selectionList.add(sp);
        } else if (mode == SelectionMode.Additive) {
            if (selectionList.contains(sp)) selectionList.remove(sp);
            else selectionList.add(sp);
        }
        // Substractive is not implemented        
    }
    
    protected void clearSelectionList() {
        selectionList.clear();
    }
    
    protected Transform getSelectionCenter() {
        return selectionCenter;
    }

    protected void setSelectionCenter(Transform selectionTransform) {
        this.selectionCenter = selectionTransform;
    }

    protected static List<Spatial> getSelectionList() {
        return selectionList;
    }

    public SelectionToolType getSelectionTool() {
        return selectionTool;
    }

    public void setSelectionTool(SelectionToolType selectionTool) {
        this.selectionTool = selectionTool;
    }    

    @Override
    protected void controlUpdate(float tpf) {

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }
    
}
