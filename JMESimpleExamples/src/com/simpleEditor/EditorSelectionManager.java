/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
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
    private Transform selectionCenter = null;
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
        
        calculateSelectionCenter();
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

    protected void calculateSelectionCenter() {
        if (selectionList.size() == 0) selectionCenter = null;
        else if (selectionList.size() == 1) selectionCenter = selectionList.get(0).getWorldTransform();
        else if (selectionList.size() > 1) {
            Vector3f posMin = null;
            Vector3f posMax = null;
            Vector3f rotMin = null;
            Vector3f rotMax = null;            
            for (Spatial obj : selectionList) {
                // POSITION 
                if (posMin == null) {
                    posMin = obj.getWorldTranslation();
                    posMax = obj.getWorldTranslation();
                }
                else {
                    // find max values
                    if (posMax.x < obj.getWorldTranslation().getX()) posMax.x = obj.getWorldTranslation().getX();
                    if (posMax.y < obj.getWorldTranslation().getY()) posMax.y = obj.getWorldTranslation().getY();
                    if (posMax.z < obj.getWorldTranslation().getZ()) posMax.z = obj.getWorldTranslation().getZ();
                    // find min values
                    if (posMin.x > obj.getWorldTranslation().getX()) posMin.x = obj.getWorldTranslation().getX();
                    if (posMin.y > obj.getWorldTranslation().getY()) posMin.y = obj.getWorldTranslation().getY();
                    if (posMin.z > obj.getWorldTranslation().getZ()) posMin.z = obj.getWorldTranslation().getZ();                    
                    
                    selectionCenter.setTranslation(FastMath.interpolateLinear(0.5f, posMin, posMax));
                }
                
                // ROTATION 
                selectionCenter.setRotation(selectionList.get(selectionList.size() - 1).getLocalRotation()); //Local coordinates of the last object
                
            }
        }
    }
    
    protected List<Spatial> getSelectionList() {
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
