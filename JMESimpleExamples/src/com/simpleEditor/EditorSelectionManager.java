/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.entitysystem.EntitySpatialsControl;
import com.entitysystem.TransformComponent;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Quad;
import de.lessvoid.nifty.controls.ListBox;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mifth
 */
public class EditorSelectionManager extends AbstractControl {

    private AssetManager assetMan;
    private Node root, guiNode;
    private Application app;
    private EditorBaseManager base;
    private static List<Long> selectionList = new ArrayList<Long>();
    private Transform selectionCenter = null;
    private SelectionToolType selectionToolType;
    private EditorSelectionTools selectionTools;
    private boolean isActive = false;
    private SelectionMode selectionMode;

    protected enum SelectionToolType {

        All, MouseClick, Rectangle, Polygon
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

        selectionTools = new EditorSelectionTools(this.app, this.base, this);
        selectionToolType = SelectionToolType.MouseClick;
        selectionMode = selectionMode.Normal;

    }

    protected boolean activate() {
        boolean result = false;

        if (selectionToolType == SelectionToolType.MouseClick) {
            selectionTools.selectMouseClick();
            result = true;
        } else if (selectionToolType == SelectionToolType.Rectangle) {
//            selectionTools.drawRectangle();
            isActive = true;
            result = true;
        }

        return result;

    }

    protected void deactivate() {
        if (selectionToolType == SelectionToolType.Rectangle) {
            selectEntities();
            calculateSelectionCenter();
            selectionTools.clearRectangle();
            isActive = false;
            System.out.println("deact");
        }

    }

    protected void selectEntity(long ID, SelectionMode mode) {
        Node nodeToSelect = (Node) base.getSpatialSystem().getSpatialControl(ID).getGeneralNode();
//        List selectedList = base.getGuiManager().getSceneObjectsListBox().getSelection();

        if (mode == SelectionMode.Normal) {
            // remove selection boxes
            for (Long idToRemove : selectionList) {
                removeSelectionBox((Node) base.getSpatialSystem().getSpatialControl(idToRemove).getGeneralNode());
            }
            selectionList.clear();

            // add to selection
            selectionList.add(ID);
            createSelectionBox(nodeToSelect);

//            // deselect all items in the objectsList
//            for (Object obj : selectedList) {
//                base.getGuiManager().getSceneObjectsListBox().deselectItem(obj);
//            }
//            // select item in the objectslist
//            base.getGuiManager().getSceneObjectsListBox().selectItem(nodeToSelect.getName() + " (" + ID + ")");

        } else if (mode == SelectionMode.Additive) {
            if (selectionList.contains(ID)) {
                selectionList.remove(ID);
                removeSelectionBox(nodeToSelect); // remove selection mesh
//                base.getGuiManager().getSceneObjectsListBox().deselectItem(nodeToSelect.getName()  + " (" + ID + ")");
            } else {
                selectionList.add(ID);
                createSelectionBox(nodeToSelect);
//                base.getGuiManager().getSceneObjectsListBox().selectItem(nodeToSelect.getName()  + " (" + ID + ")");
            }
        }
        // Substractive is not implemented        

        base.getGuiManager().setSelectedObjectsList();
    }

    protected void selectEntities() {

//        List selectedList = base.getGuiManager().getSceneObjectsListBox().getSelection();

        List<Node> lst = base.getLayerManager().getLayers();
        Vector2f centerCam = new Vector2f(app.getCamera().getWidth() * 0.5f, app.getCamera().getHeight() * 0.5f);
        Node rectangle = selectionTools.getRectangleSelection();
        Vector3f rectanglePosition = rectangle.getLocalTranslation();

        if (selectionMode == SelectionMode.Normal) {
            // remove selection boxes
            for (Long idToRemove : selectionList) {
                removeSelectionBox((Node) base.getSpatialSystem().getSpatialControl(idToRemove).getGeneralNode());
            }

            // clear selectionList
            selectionList.clear();
//            for (Object obj : selectedList) {
//                base.getGuiManager().getSceneObjectsListBox().deselectItem(obj);
//            }
        }

        for (Node layer : lst) {
            Object boolObj = layer.getUserData("isEnabled");
            boolean bool = (Boolean) boolObj;
            if (bool == true) {
                for (Spatial sp : layer.getChildren()) {

                    Vector3f spScreenPos = app.getCamera().getScreenCoordinates(sp.getWorldTranslation());
                    float spScreenDistance = centerCam.distance(new Vector2f(spScreenPos.getX(), spScreenPos.getY()));

                    if (spScreenPos.getZ() < 1f) {

                        float pointMinX = Math.min(rectanglePosition.getX(), spScreenPos.getX());
                        float pointMaxX = Math.max(rectanglePosition.getX(), spScreenPos.getX());
                        float pointMinY = Math.min(rectanglePosition.getY(), spScreenPos.getY());
                        float pointMaxY = Math.max(rectanglePosition.getY(), spScreenPos.getY());

                        float distX = pointMaxX - pointMinX;
                        float distY = pointMaxY - pointMinY;

                        //add to selection the spatial which is in the rectangle area
                        if (distX <= rectangle.getLocalScale().getX() * 0.5f
                                && distY <= rectangle.getLocalScale().getY() * 0.5f) {
                            Object spIdObj = sp.getUserData("EntityID");
                            long spId = (Long) spIdObj;
                            if (selectionMode == SelectionMode.Additive) {
                                selectEntity(spId, selectionMode);
                            } else if (selectionMode == SelectionMode.Normal) {
                                selectionList.add(spId);
                                Node nodeToSelect = (Node) base.getSpatialSystem().getSpatialControl(spId).getGeneralNode();
                                createSelectionBox(nodeToSelect);
//                                base.getGuiManager().getSceneObjectsListBox().selectItem(nodeToSelect.getName() + " (" + spId + ")");
                            }
                        }
                    }
                }
            }
        }
        // select item in the objectslist        
        if (selectionMode == SelectionMode.Normal) {
            base.getGuiManager().setSelectedObjectsList();
        }
    }

    protected void createSelectionBox(Node nodeSelect) {
        Material mat_box = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_box.setColor("Color", ColorRGBA.Blue);
        WireBox wbx = new WireBox();
//        BoundingBox = new BoundingBox();
        Transform tempScale = nodeSelect.getLocalTransform().clone();
        nodeSelect.setLocalTransform(new Transform());
        wbx.fromBoundingBox((BoundingBox) nodeSelect.getWorldBound());
        nodeSelect.setLocalTransform(tempScale);

        Geometry bx = new Geometry("SelectionTempMesh", wbx);
        bx.setMaterial(mat_box);
        nodeSelect.attachChild(bx);

    }

    protected void removeSelectionBox(Node nodeSelect) {
        nodeSelect.detachChild(nodeSelect.getChild("SelectionTempMesh"));
    }

    protected void clearSelectionList() {
        for (Long id : selectionList) {
            removeSelectionBox((Node)base.getSpatialSystem().getSpatialControl(id).getGeneralNode());
        }
        selectionList.clear();
    }

    protected Transform getSelectionCenter() {
        return selectionCenter;
    }

    protected void setSelectionCenter(Transform selectionTransform) {
        this.selectionCenter = selectionTransform;
    }

    protected boolean isIsActive() {
        return isActive;
    }

    protected void calculateSelectionCenter() {
        if (selectionList.size() == 0) {
            selectionCenter = null;
        } else if (selectionList.size() == 1) {
            Spatial nd = base.getSpatialSystem().getSpatialControl(selectionList.get(0)).getGeneralNode();
            selectionCenter = nd.getLocalTransform().clone();
        } else if (selectionList.size() > 1) {

            if (selectionCenter == null) {
                selectionCenter = new Transform();
            }

            // FIND CENTROID OF center POSITION
            Vector3f centerPosition = new Vector3f();
            for (Long ID : selectionList) {
//                // POSITION
                Spatial ndPos = base.getSpatialSystem().getSpatialControl(ID).getGeneralNode();
                centerPosition.addLocal(ndPos.getWorldTranslation());
            }
            centerPosition.divideLocal(selectionList.size());
            selectionCenter.setTranslation(centerPosition);

            // Rotation of the last selected is Local Rotation (like in Blender)
            Quaternion rot = base.getSpatialSystem().getSpatialControl(selectionList.get(selectionList.size() - 1)).getGeneralNode().getLocalRotation();
//                TransformComponent trLastSelected = (TransformComponent) base.getEntityManager().getComponent(selectionList.get(selectionList.size() - 1), TransformComponent.class);
            selectionCenter.setRotation(rot); //Local coordinates of the last object            
        }

    }

    protected List<Long> getSelectionList() {
        return selectionList;
    }

    public SelectionToolType getSelectionTool() {
        return selectionToolType;
    }

    public void setSelectionTool(SelectionToolType selectionTool) {
        this.selectionToolType = selectionTool;
    }

    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }

    @Override
    protected void controlUpdate(float tpf) {

        if (isActive) {
            selectionTools.drawRectangle();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
