/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spaceShip;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 *
 * @author mifth
 */
public class ShipPhysicsControl extends RigidBodyControl {

    private Camera cam;
    private boolean move = false;
    private float angle;
    private float rotationSpeed = 17f;
    
    public ShipPhysicsControl(Camera camera, CollisionShape shape, float mass, BulletAppState aps) {
        super(shape, mass);
        cam = camera;
//        space = aps.getPhysicsSpace();
//        aps.getPhysicsSpace().addCollisionObject(collision);
        aps.getPhysicsSpace().addTickListener(physics);        
        
    }
    
    void makeMove(boolean boo) {
        
        if (boo) move = true;
        else if (!boo) move = false;
    }
    
    
//    PhysicsCollisionObject collision = new PhysicsCollisionObject() {};
    
    PhysicsTickListener physics = new PhysicsTickListener() {

        public void prePhysicsTick(PhysicsSpace space, float f) {
            
    angle = cam.getRotation().mult(Vector3f.UNIT_Z).normalizeLocal().angleBetween(getPhysicsRotation().clone().mult(Vector3f.UNIT_Z).normalizeLocal());
//    System.out.println(angle);
    
    // Ship Movement
    if (move) {
        applyCentralForce(cam.getDirection().normalizeLocal().multLocal(30f));
    }
 
    // Ship Rotation

    if (angle >= 0.01f) {
    Vector3f dirSpatial = getPhysicsRotation().mult(Vector3f.UNIT_Z);
    Vector3f dirCam = cam.getDirection();
    Vector3f cross = dirSpatial.crossLocal(dirCam).normalizeLocal();
 
    Vector3f dirSpatial1 = getPhysicsRotation().mult(Vector3f.UNIT_Y);
    Vector3f dirCam1 = cam.getUp();
    Vector3f cross1 = dirSpatial1.crossLocal(dirCam1).normalizeLocal();
 
    Vector3f dirSpatial2 = getPhysicsRotation().mult(Vector3f.UNIT_X);
    Vector3f dirCam2 = cam.getLeft();
    Vector3f cross2 = dirSpatial2.crossLocal(dirCam2).normalizeLocal();
 
    applyTorque(cross.addLocal(cross1).addLocal(cross2).normalizeLocal().mult(angle* rotationSpeed));
    }
 

        }

        public void physicsTick(PhysicsSpace space, float f) {

        }
    };

    
}
