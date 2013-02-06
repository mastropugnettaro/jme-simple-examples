/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import java.util.List;

/**
 *
 * @author mifth
 */
public class EditorTransformManager extends AbstractControl {

    private Node transformTool;
    private Node moveTool, rotateTool, scaleTool, collisionPlane;
    private Transform selectedCenter, actionCenter;
    private Node root, guiNode;
    private TransformToolType transformType;
    private PickedAxis pickedAxis;
    private AssetManager assetMan;
    private Application app;
    private boolean isActive = false;
    private boolean useTool;
    private Geometry testGeo;
    private Vector3f deltaMoveVector;
    private EditorBaseManager base;

    protected enum TransformToolType {

        MoveTool, RotateTool, ScaleTool, None
    };

    protected enum TransformCoordinates {

        WorldCoords, LocalCoords, ViewCoords
    };

    protected enum PickedAxis {

        X, Y, Z, XY, XZ, YZ, View, None
    };

    public EditorTransformManager(Application app, EditorBaseManager base) {

        this.app = app;
        this.base = base;
        assetMan = app.getAssetManager();
        root = (Node) app.getViewPort().getScenes().get(0);
        guiNode = (Node) app.getGuiViewPort().getScenes().get(0);

        transformTool = new Node("transformTool");
        root.attachChild(transformTool);

        createManipulators();
        selectedCenter = this.base.getSelectionManager().getSelectionCenter();
//
//        Node nd = (Node) selectedSp;

        pickedAxis = PickedAxis.None;
        transformType = TransformToolType.MoveTool;  //default type

        createCollisionPlane();

    }

    private void createCollisionPlane() {
        float size = 2000;
        Geometry g = new Geometry("plane", new Quad(size, size));
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        mat.getAdditionalRenderState().setWireframe(true);
        g.setMaterial(mat);
        g.setLocalTranslation(-size / 2, -size / 2, 0);
        collisionPlane = new Node();
        collisionPlane.attachChild(g);
        root.attachChild(collisionPlane);

        testGeo = new Geometry("testGeo", new Sphere(10, 10, 0.1f));
        testGeo.setMaterial(mat);
        root.attachChild(testGeo);

    }

    protected void setTransformToolType(TransformToolType type) {
        transformType = type;
        List list = base.getSelectionManager().getSelectionList();
        if (list.size() > 0) {
            transformTool.detachAllChildren(); // if None
            if (type == TransformToolType.MoveTool) {
                transformTool.attachChild(moveTool);
            } else if (type == TransformToolType.RotateTool) {
                transformTool.attachChild(rotateTool);
            } else if (type == TransformToolType.ScaleTool) {
                transformTool.attachChild(scaleTool);
            }
            selectedCenter = base.getSelectionManager().getSelectionCenter();
            if (selectedCenter != null) {
                actionCenter = selectedCenter.clone();
                updateTransform(actionCenter);
            }
        }
    }

    protected TransformToolType getTransformToolType() {
        return transformType;
    }

    protected PickedAxis getpickedAxis() {
        return pickedAxis;
    }

    protected void setPickedAxis(PickedAxis axis) {
        pickedAxis = axis;
    }

    protected void updateTransform(Transform center) {
        Vector3f vec = center.getTranslation().subtract(app.getCamera().getLocation()).normalize().multLocal(1.3f);
        transformTool.setLocalTranslation(app.getCamera().getLocation().add(vec));
        transformTool.setLocalRotation(center.getRotation());
    }

    protected CollisionResult activate() {

        CollisionResult result = null;

        if (transformType != TransformToolType.None) {

            CollisionResults results = new CollisionResults();
            Ray ray = new Ray();
            Vector3f pos = app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0f).clone();
            Vector3f dir = app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0.1f).clone();
            dir.subtractLocal(pos).normalizeLocal();
            ray.setOrigin(pos);
            ray.setDirection(dir);
            transformTool.collideWith(ray, results);

            if (results.size() > 0) {
                result = results.getClosestCollision();

                // Set PickedAxis
                String type = result.getGeometry().getName();
                if (type.indexOf("move") >= 0) {
                    if (type.indexOf("move_x") > 0) {
                        setPickedAxis(EditorTransformManager.PickedAxis.X);
                    } else if (type.indexOf("move_y") > 0) {
                        setPickedAxis(EditorTransformManager.PickedAxis.Y);
                    } else if (type.indexOf("move_z") > 0) {
                        setPickedAxis(EditorTransformManager.PickedAxis.Z);
                    } else if (type.indexOf("move_view") > 0) {
                        setPickedAxis(EditorTransformManager.PickedAxis.View);
                    }
                }


                // select an angle between 0 and 90 degrees (from 0 to 1.57 in radians) (for collisionPlane)
                float angleX = app.getCamera().getDirection().angleBetween(selectedCenter.getRotation().mult(Vector3f.UNIT_X));
                if (angleX > 1.57) {
                    angleX = app.getCamera().getDirection().angleBetween(selectedCenter.getRotation().mult(Vector3f.UNIT_X).negateLocal());
                }

                float angleY = app.getCamera().getDirection().angleBetween(selectedCenter.getRotation().mult(Vector3f.UNIT_Y));
                if (angleY > 1.57) {
                    angleY = app.getCamera().getDirection().angleBetween(selectedCenter.getRotation().mult(Vector3f.UNIT_Y).negateLocal());
                }

                float angleZ = app.getCamera().getDirection().angleBetween(selectedCenter.getRotation().mult(Vector3f.UNIT_Z));
                if (angleZ > 1.57) {
                    angleZ = app.getCamera().getDirection().angleBetween(selectedCenter.getRotation().mult(Vector3f.UNIT_Z).negateLocal());
                }

                //select the less angle for collisionPlane
                float lessAngle = angleX;
                if (lessAngle > angleY) {
                    lessAngle = angleY;
                }
                if (lessAngle > angleZ) {
                    lessAngle = angleZ;
                }

                // set the collision Plane location and rotation
                collisionPlane.setLocalTranslation(selectedCenter.getTranslation());
                collisionPlane.setLocalRotation(selectedCenter.getRotation().clone()); //equals to angleZ
                Quaternion planeRot = collisionPlane.getLocalRotation();

                // rotate the plane for constraints
                if (lessAngle == angleX) {
                    System.out.println("XXXAngle");
                    if (pickedAxis == PickedAxis.X && angleY > angleZ) {
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
                    } else if (pickedAxis == PickedAxis.X && angleY < angleZ) {
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y));
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
                    } else if (pickedAxis == PickedAxis.Y) {
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y));
                    } else if (pickedAxis == PickedAxis.Z) {
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y));
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
                    }
                } else if (lessAngle == angleY) {
                    if (pickedAxis == PickedAxis.X) {
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y));
                    } else if (pickedAxis == PickedAxis.Y && angleX < angleZ) {
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y)); // if angleX>angleY no need to ratate
                    } else if (pickedAxis == PickedAxis.Z) {
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
                    }
                } else if (lessAngle == angleZ) {
                    if (pickedAxis == PickedAxis.X) {
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
                    }
//                if (pickedAxis == PickedAxis.Y) 
                    if (pickedAxis == PickedAxis.Z && angleY < angleX) {
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
                    } else if (pickedAxis == PickedAxis.Z && angleY > angleX) {
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y));
                        collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
                    }
                }

                deltaMoveVector = null;  // prepare for new deltaVector
                isActive = true;

            }
        }

        return result;
    }

    /**
     * Create the axis marker that is selectable
     */
    private void createManipulators() {

        Material mat_red = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_red.setColor("Color", ColorRGBA.Red);

        Material mat_blue = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_blue.setColor("Color", ColorRGBA.Blue);

        Material mat_green = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_green.setColor("Color", ColorRGBA.Green);

        Material mat_white = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_white.setColor("Color", ColorRGBA.White);

        moveTool = (Node) assetMan.loadModel("Models/simpleEditor/manipulators/manipulators_move.j3o");

        moveTool.getChild("move_x").setMaterial(mat_red);
        moveTool.getChild("collision_move_x").setMaterial(mat_red);
        moveTool.getChild("collision_move_x").setCullHint(Spatial.CullHint.Always);
        moveTool.getChild("move_y").setMaterial(mat_blue);
        moveTool.getChild("collision_move_y").setMaterial(mat_blue);
        moveTool.getChild("collision_move_y").setCullHint(Spatial.CullHint.Always);
        moveTool.getChild("move_z").setMaterial(mat_green);
        moveTool.getChild("collision_move_z").setMaterial(mat_green);
        moveTool.getChild("collision_move_z").setCullHint(Spatial.CullHint.Always);
        moveTool.getChild("move_view").setMaterial(mat_white);
        moveTool.getChild("collision_move_view").setMaterial(mat_white);
        moveTool.getChild("collision_move_view").setCullHint(Spatial.CullHint.Always);
        moveTool.scale(0.1f);

        rotateTool = (Node) assetMan.loadModel("Models/simpleEditor/manipulators/manipulators_rotate.j3o");
        rotateTool.getChild("rot_x").setMaterial(mat_red);
        rotateTool.getChild("collision_rot_x").setMaterial(mat_red);
        rotateTool.getChild("collision_rot_x").setCullHint(Spatial.CullHint.Always);
        rotateTool.getChild("rot_y").setMaterial(mat_blue);
        rotateTool.getChild("collision_rot_y").setMaterial(mat_blue);
        rotateTool.getChild("collision_rot_y").setCullHint(Spatial.CullHint.Always);
        rotateTool.getChild("rot_z").setMaterial(mat_green);
        rotateTool.getChild("collision_rot_z").setMaterial(mat_green);
        rotateTool.getChild("collision_rot_z").setCullHint(Spatial.CullHint.Always);
        rotateTool.getChild("rot_view").setMaterial(mat_white);
        rotateTool.getChild("collision_rot_view").setMaterial(mat_white);
        rotateTool.getChild("collision_rot_view").setCullHint(Spatial.CullHint.Always);
        rotateTool.scale(0.1f);

        scaleTool = (Node) assetMan.loadModel("Models/simpleEditor/manipulators/manipulators_scale.j3o");
        scaleTool.getChild("scale_x").setMaterial(mat_red);
        scaleTool.getChild("collision_scale_x").setMaterial(mat_red);
        scaleTool.getChild("collision_scale_x").setCullHint(Spatial.CullHint.Always);
        scaleTool.getChild("scale_y").setMaterial(mat_blue);
        scaleTool.getChild("collision_scale_y").setMaterial(mat_blue);
        scaleTool.getChild("collision_scale_y").setCullHint(Spatial.CullHint.Always);
        scaleTool.getChild("scale_z").setMaterial(mat_green);
        scaleTool.getChild("collision_scale_z").setMaterial(mat_green);
        scaleTool.getChild("collision_scale_z").setCullHint(Spatial.CullHint.Always);
        scaleTool.getChild("scale_view").setMaterial(mat_white);
        scaleTool.getChild("collision_scale_view").setMaterial(mat_white);
        scaleTool.getChild("collision_scale_view").setCullHint(Spatial.CullHint.Always);
        scaleTool.scale(0.1f);


    }

    protected void deactivate() {
        if (pickedAxis != PickedAxis.None) {
            pickedAxis = PickedAxis.None;
            base.getSelectionManager().calculateSelectionCenter();
            selectedCenter = base.getSelectionManager().getSelectionCenter();
            actionCenter = null;
            isActive = false;
            
        }
    }

    protected void translateObjects(float distance) {
        for (Spatial sp : base.getSelectionManager().getSelectionList()) {
            sp.setLocalTranslation(selectedCenter.getTranslation().clone());
            if (pickedAxis == PickedAxis.X) {
                sp.getLocalTranslation().addLocal(selectedCenter.getRotation().getRotationColumn(0).mult(distance));
                actionCenter.setTranslation(selectedCenter.getTranslation().clone().add(selectedCenter.getRotation().getRotationColumn(0).mult(distance)));
            } else if (pickedAxis == PickedAxis.Y) {
                sp.getLocalTranslation().addLocal(selectedCenter.getRotation().getRotationColumn(1).mult(distance));
                actionCenter.setTranslation(selectedCenter.getTranslation().clone().add(selectedCenter.getRotation().getRotationColumn(1).mult(distance)));
            } else if (pickedAxis == PickedAxis.Z) {
                sp.getLocalTranslation().addLocal(selectedCenter.getRotation().getRotationColumn(2).mult(distance));
                actionCenter.setTranslation(selectedCenter.getTranslation().clone().add(selectedCenter.getRotation().getRotationColumn(2).mult(distance)));
            }
        }
    }

    @Override
    protected void controlUpdate(float tpf) {

        if (pickedAxis != PickedAxis.None && selectedCenter != null && isActive) {

            actionCenter = selectedCenter.clone();

            CollisionResults results = new CollisionResults();
            Ray ray = new Ray();
            Vector3f pos = app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0f).clone();
            Vector3f dir = app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0.1f).clone();
            dir.subtractLocal(pos).normalizeLocal();
            ray.setOrigin(pos);
            ray.setDirection(dir);
            collisionPlane.collideWith(ray, results);
            CollisionResult result = results.getClosestCollision();

            // Complex trigonometry formula based on sin(angle)*distance
            if (results.size() > 0) {

                Vector3f contactPoint = result.getContactPoint(); // get a point of collisionPlane

                //set new delteVector if it's not set
                if (deltaMoveVector == null) {
                    deltaMoveVector = selectedCenter.getTranslation().subtract(contactPoint);
                }

                contactPoint = contactPoint.add(deltaMoveVector); // add delta of the picked place

                Vector3f vec1 = contactPoint.subtract(selectedCenter.getTranslation());
                float distanceVec1 = selectedCenter.getTranslation().distance(contactPoint);

                // Picked vector
                Vector3f pickedVec = Vector3f.UNIT_X;
                if (pickedAxis == PickedAxis.Y) {
                    pickedVec = Vector3f.UNIT_Y;
                } else if (pickedAxis == PickedAxis.Z) {
                    pickedVec = Vector3f.UNIT_Z;
                }
                // the main formula for constraint axis
                float angle = vec1.clone().normalizeLocal().angleBetween(selectedCenter.getRotation().mult(pickedVec).normalizeLocal());
                float distanceVec2 = distanceVec1 * FastMath.sin(angle);

                // fix if angle>90 degrees
                Vector3f perendicularVec = collisionPlane.getLocalRotation().mult(Vector3f.UNIT_X).mult(distanceVec2);
                Vector3f checkVec = contactPoint.add(perendicularVec).subtractLocal(contactPoint).normalizeLocal();
                float angleCheck = checkVec.angleBetween(vec1.clone().normalizeLocal());
                if (angleCheck < FastMath.HALF_PI) {
                    perendicularVec.negateLocal();
                }


                // find distance to mave
                float distanceToMove = contactPoint.add(perendicularVec).distance(selectedCenter.getTranslation());
                if (angle > FastMath.HALF_PI) {
                    distanceToMove = -distanceToMove;
                }

                translateObjects(distanceToMove);
//                testGeo.getLocalTranslation().addLocal(perendicularVec);
                System.out.println("Vec: " + testGeo.getWorldTranslation().toString() + "   angle: " + angle);
                
                updateTransform(actionCenter);
            }
        }

        if (!isActive) {
            updateTransform(selectedCenter);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
