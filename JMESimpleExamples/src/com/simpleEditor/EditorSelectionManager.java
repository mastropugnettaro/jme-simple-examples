/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.entitysystem.EntitySpatialsControl;
import com.entitysystem.TransformComponent;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
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
            Spatial nd = base.getSpatialSystem().getSpatialControl(selectionList.get(0)).getGeneralNode();
            selectionCenter = nd.getLocalTransform().clone();
        }
        else if (selectionList.size() > 1) {
            Vector3f posMin = null;
            Vector3f posMax = null;
            Vector3f rotMin = null;
            Vector3f rotMax = null;            
            for (Long ID : selectionList) {
                // POSITION
                Spatial ndPos = base.getSpatialSystem().getSpatialControl(ID).getGeneralNode();
//                TransformComponent trLocation = (TransformComponent) base.getEntityManager().getComponent(idGet, TransformComponent.class);
                if (posMin == null) {
                    posMin = ndPos.getLocalTranslation().clone();
                    posMax = ndPos.getLocalTranslation().clone();
                }
                else {
                    // find max values
                    if (posMax.x < ndPos.getLocalTranslation().getX()) posMax.x = ndPos.getLocalTranslation().getX();
                    if (posMax.y < ndPos.getLocalTranslation().getY()) posMax.y = ndPos.getLocalTranslation().getY();
                    if (posMax.z < ndPos.getLocalTranslation().getZ()) posMax.z = ndPos.getLocalTranslation().getZ();
                    // find min values
                    if (posMin.x > ndPos.getLocalTranslation().getX()) posMin.x = ndPos.getLocalTranslation().getX();
                    if (posMin.y > ndPos.getLocalTranslation().getY()) posMin.y = ndPos.getLocalTranslation().getY();
                    if (posMin.z > ndPos.getLocalTranslation().getZ()) posMin.z = ndPos.getLocalTranslation().getZ();
                    
                }
            }
                selectionCenter.setTranslation(FastMath.interpolateLinear(0.5f, posMin, posMax));
                
                // Rotation of the last selected
                Quaternion rot = base.getSpatialSystem().getSpatialControl(selectionList.get(selectionList.size()-1)).getGeneralNode().getLocalRotation();
//                TransformComponent trLastSelected = (TransformComponent) base.getEntityManager().getComponent(selectionList.get(selectionList.size() - 1), TransformComponent.class);
                selectionCenter.setRotation(rot); //Local coordinates of the last object            
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
