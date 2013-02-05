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

/**
 *
 * @author mifth
 */
public class EditorTransformTool extends AbstractControl {

    private Node transformTool;
    private Node moveTool, rotateTool, scaleTool, collisionPlane;
    private Node selectedSp;
    private Node root, guiNode;
    private TransformToolType transformType;
    private PickedAxys pickedAxys;
    private AssetManager assetMan;
    private Application app;
    private boolean isActive = false;
    private boolean useTool;
    private Geometry testGeo;
    private Vector3f deltaMoveVector;

    protected enum TransformToolType {

        moveTransform, rotateTransform, scaleTransform, None
    };

    protected enum PickedAxys {

        X, Y, Z, XY, XZ, YZ, View, None
    };

    public EditorTransformTool(Application app, Node select) {

        this.app = app;
        assetMan = app.getAssetManager();
        root = (Node) app.getViewPort().getScenes().get(0);
        guiNode = (Node) app.getGuiViewPort().getScenes().get(0);

        transformTool = new Node("transformTool");
        root.attachChild(transformTool);

        createManipulators();
        selectedSp = select;

        Node nd = (Node) selectedSp;

        pickedAxys = PickedAxys.None;
        transformType = TransformToolType.moveTransform;

        createCollisionPlane();

    }

    @Override
    protected void controlUpdate(float tpf) {

        if (pickedAxys != PickedAxys.None) {

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
                if (deltaMoveVector == null) deltaMoveVector = selectedSp.getWorldTranslation().subtract(contactPoint);
                
                contactPoint = contactPoint.add(deltaMoveVector); // add delta of the picked place
                
                Vector3f vec1 = contactPoint.subtract(selectedSp.getWorldTranslation());
                float distanceVec1 = selectedSp.getWorldTranslation().distance(contactPoint);
                
                // Picked vector
                Vector3f pickedVec = Vector3f.UNIT_X;
                if (pickedAxys == PickedAxys.Y) pickedVec = Vector3f.UNIT_Y;
                else if (pickedAxys == PickedAxys.Z) pickedVec = Vector3f.UNIT_Z;
                // the main formula for constraint axys
                float angle = vec1.clone().normalizeLocal().angleBetween(selectedSp.getLocalRotation().mult(pickedVec).normalizeLocal());
                float distanceVec2 = distanceVec1 * FastMath.sin(angle);
                
                // Additional perpendicular vector 
                Vector3f perpendicularVec = Vector3f.UNIT_X;
                // fix if angle>90 degrees
                Vector3f moveVec = collisionPlane.getLocalRotation().mult(perpendicularVec).mult(distanceVec2);
                Vector3f checkVec = contactPoint.add(moveVec).subtractLocal(contactPoint).normalizeLocal();
                float angleCheck = checkVec.angleBetween(vec1.clone().normalizeLocal()); 
                if (angleCheck < FastMath.HALF_PI) moveVec = moveVec.negate();
                
                testGeo.setLocalTranslation(contactPoint);
                testGeo.getLocalTranslation().addLocal(moveVec);
                System.out.println("Vec: " + testGeo.getWorldTranslation().toString() + "   angle: " + angle);
            }
        }

        if (selectedSp != null) {
            setTransformPosition();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
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
    }

    protected TransformToolType getTransformToolType() {
        return transformType;
    }

    protected PickedAxys getpickedAxis() {
        return pickedAxys;
    }

    protected void setPickedAxis(PickedAxys axis) {
        pickedAxys = axis;
    }

    protected void setTransformPosition() {
        transformTool.attachChild(moveTool);
        Vector3f vec = selectedSp.getWorldTranslation().subtract(app.getCamera().getLocation()).normalize().multLocal(1.3f);
        moveTool.setLocalTranslation(app.getCamera().getLocation().add(vec));
        moveTool.setLocalRotation(selectedSp.getWorldRotation());
    }

    /**
     * Remove the marker from it's parent (the tools node)
     */
    public void hideMarker() {
        if (transformTool != null) {
            transformTool.setCullHint(Spatial.CullHint.Always);
        }
    }

    public void doUpdateToolsTransformation() {
        if (selectedSp != null) {
            transformTool.setLocalTranslation(selectedSp.getWorldTranslation());
            transformTool.setLocalRotation(selectedSp.getWorldRotation());
        } else {
            transformTool.setLocalTranslation(Vector3f.ZERO);
            transformTool.setLocalRotation(Quaternion.IDENTITY);
        }
    }

    protected CollisionResult pick(Node node) {
        CollisionResults results = new CollisionResults();
        Ray ray = new Ray();
        Vector3f pos = app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0f).clone();
        Vector3f dir = app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0.1f).clone();
        dir.subtractLocal(pos).normalizeLocal();
        ray.setOrigin(pos);
        ray.setDirection(dir);
        node.collideWith(ray, results);
        CollisionResult result = null;

        if (results.size() > 0) {
            result = results.getClosestCollision();
            
            // Set PickedAxys
            String type = result.getGeometry().getName();
            if (type.indexOf("move") >= 0) {
                if (type.indexOf("move_x") > 0) {
                    setPickedAxis(EditorTransformTool.PickedAxys.X);
                } else if (type.indexOf("move_y") > 0) {
                    setPickedAxis(EditorTransformTool.PickedAxys.Y);
                } else if (type.indexOf("move_z") > 0) {
                    setPickedAxis(EditorTransformTool.PickedAxys.Z);
                } else if (type.indexOf("move_view") > 0) {
                    setPickedAxis(EditorTransformTool.PickedAxys.View);
                }
            }
            

            // select an angle between 0 and 90 degrees (from 0 to 1.57 in radians) (for collisionPlane)
            float angleX = app.getCamera().getDirection().angleBetween(selectedSp.getLocalRotation().mult(Vector3f.UNIT_X));
            if (angleX > 1.57) {
                angleX = app.getCamera().getDirection().angleBetween(selectedSp.getLocalRotation().mult(Vector3f.UNIT_X).negateLocal());
            }

            float angleY = app.getCamera().getDirection().angleBetween(selectedSp.getLocalRotation().mult(Vector3f.UNIT_Y));
            if (angleY > 1.57) {
                angleY = app.getCamera().getDirection().angleBetween(selectedSp.getLocalRotation().mult(Vector3f.UNIT_Y).negateLocal());
            }

            float angleZ = app.getCamera().getDirection().angleBetween(selectedSp.getLocalRotation().mult(Vector3f.UNIT_Z));
            if (angleZ > 1.57) {
                angleZ = app.getCamera().getDirection().angleBetween(selectedSp.getLocalRotation().mult(Vector3f.UNIT_Z).negateLocal());
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
            collisionPlane.setLocalTranslation(selectedSp.getWorldTranslation());
            collisionPlane.setLocalRotation(selectedSp.getWorldRotation().clone()); //equals to angleZ
            Quaternion planeRot = collisionPlane.getLocalRotation();
            
            // rotate the plane for constraints
            if (lessAngle == angleX) {
                System.out.println("XXXAngle");
                if (pickedAxys == PickedAxys.X && angleY > angleZ)  collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
                else if (pickedAxys == PickedAxys.X && angleY < angleZ)  {
                    collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y));
                    collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
                }
                else if (pickedAxys == PickedAxys.Y) collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y));
                else if (pickedAxys == PickedAxys.Z) {
                    collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y));
                    collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
                }
            } else if (lessAngle == angleY) {
                if (pickedAxys == PickedAxys.X) {
                    collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
                    collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y));
                }
                else if (pickedAxys == PickedAxys.Y && angleX < angleZ)  collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y)); // if angleX>angleY no need to ratate
                else if (pickedAxys == PickedAxys.Z) collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
            } else if (lessAngle == angleZ) {
                if (pickedAxys == PickedAxys.X) collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
//                if (pickedAxys == PickedAxys.Y) 
                  if (pickedAxys == PickedAxys.Z && angleY < angleX) collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
                  else if (pickedAxys == PickedAxys.Z && angleY > angleX) {
                      collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y));
                      collisionPlane.getLocalRotation().multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
                  }
            }
            
            deltaMoveVector = null;  // prepare for new deltaVector
            
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
}
