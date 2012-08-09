/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spaceShip;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 *
 * @author mifth
 */
public class ShipPhysicsControl extends RigidBodyControl implements PhysicsTickListener, PhysicsControl {

    private Camera cam;
    private boolean move = false;
    private float time = 0.0001f;
    private float angle;
    private float rotationSpeed = 6f;
    
    public ShipPhysicsControl(Camera camera, CollisionShape shape, float mass) {
        super(shape, mass);
        cam = camera;
    }
    
    void makeMove(boolean boo) {
        
        if (boo) move = true;
        else if (!boo) move = false;
    }
    
    public void prePhysicsTick(PhysicsSpace space, float f) {

    // Ship Movement
    if (move) {
        applyCentralForce(cam.getDirection().normalizeLocal().multLocal(30f));
    }
 
    // Ship Rotation
    angle = cam.getRotation().clone().mult(Vector3f.UNIT_Z).normalizeLocal().angleBetween(getPhysicsRotation().clone().mult(Vector3f.UNIT_Z).normalizeLocal());
 
//    System.out.println(angle);
    

    if (angle < 0.03f) {
        return;
    }
 
    Vector3f dirSpatial = getPhysicsRotation().clone().mult(Vector3f.UNIT_Z);
    Vector3f dirCam = cam.getDirection();
    Vector3f cross = dirSpatial.crossLocal(dirCam).normalizeLocal();
 
    Vector3f dirSpatial1 = getPhysicsRotation().clone().mult(Vector3f.UNIT_Y);
    Vector3f dirCam1 = cam.getUp();
    Vector3f cross1 = dirSpatial1.crossLocal(dirCam1).normalizeLocal();
 
    Vector3f dirSpatial2 = getPhysicsRotation().clone().mult(Vector3f.UNIT_X);
    Vector3f dirCam2 = cam.getLeft();
    Vector3f cross2 = dirSpatial2.crossLocal(dirCam2).normalizeLocal();
 
    applyTorque(cross.addLocal(cross1).addLocal(cross2).normalizeLocal().mult((angle)* rotationSpeed));

    }

    public void physicsTick(PhysicsSpace space, float f) {
     //   throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setPhysicsSpace(PhysicsSpace space) {
        if (space == null) {
            if (this.space != null) {
                this.space.removeCollisionObject(this);
                this.space.removeTickListener(this);
            }
            this.space = space;
        } else {
            space.addCollisionObject(this);
            space.addTickListener(this);
        }
        this.space = space;
    }

    public PhysicsSpace getPhysicsSpace() {
        return space;
    }  
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }    
    
}
