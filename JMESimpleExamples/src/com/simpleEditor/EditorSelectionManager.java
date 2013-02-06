/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.entitysystem.TransformComponent;
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
    private static List<Long> selectionList = new ArrayList<Long>();
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
    
    protected void selectEntity(long ID, SelectionMode mode) {

        if (mode == SelectionMode.Normal) {
            selectionList.clear();
            selectionList.add(ID);
        } else if (mode == SelectionMode.Additive) {
            if (selectionList.contains(ID)) selectionList.remove(ID);
            else selectionList.add(ID);
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
        else if (selectionList.size() == 1) {
            TransformComponent trLocation = (TransformComponent) base.getEntityManager().getComponent(selectionList.get(0), TransformComponent.class);
            selectionCenter = trLocation.getTransform().clone();
        }
        else if (selectionList.size() > 1) {
            Vector3f posMin = null;
            Vector3f posMax = null;
            Vector3f rotMin = null;
            Vector3f rotMax = null;            
            for (Long ID : selectionList) {
                // POSITION
                TransformComponent trLocation = (TransformComponent) base.getEntityManager().getComponent(ID, TransformComponent.class);
                if (posMin == null) {
                    posMin = trLocation.getLocation().clone();
                    posMax = trLocation.getLocation().clone();
                }
                else {
                    // find max values
                    if (posMax.x < trLocation.getLocation().getX()) posMax.x = trLocation.getLocation().getX();
                    if (posMax.y < trLocation.getLocation().getY()) posMax.y = trLocation.getLocation().getY();
                    if (posMax.z < trLocation.getLocation().getZ()) posMax.z = trLocation.getLocation().getZ();
                    // find min values
                    if (posMin.x > trLocation.getLocation().getX()) posMin.x = trLocation.getLocation().getX();
                    if (posMin.y > trLocation.getLocation().getY()) posMin.y = trLocation.getLocation().getY();
                    if (posMin.z > trLocation.getLocation().getZ()) posMin.z = trLocation.getLocation().getZ();
                    
                }
            }
                selectionCenter.setTranslation(FastMath.interpolateLinear(0.5f, posMin, posMax));
                
                // Rotation
                TransformComponent trLastSelected = (TransformComponent) base.getEntityManager().getComponent(selectionList.get(selectionList.size() - 1), TransformComponent.class);
                selectionCenter.setRotation(trLastSelected.getRotation().clone()); //Local coordinates of the last object            
        }
        
        if (selectionList.size() > 0 && base.getTransformTool().getTransformToolType() != EditorTransformManager.TransformToolType.None) {
            base.getTransformTool().setTransformToolType(EditorTransformManager.TransformToolType.MoveTool);
        }
    }
    
    protected List<Long> getSelectionList() {
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
