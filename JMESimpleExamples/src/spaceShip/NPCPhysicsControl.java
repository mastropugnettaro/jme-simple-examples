/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spaceShip;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;

/**
 *
 * @author mifth
 */
public class NPCPhysicsControl extends RigidBodyControl {

    private Camera cam;
    private boolean move = true;
    private float angle;
    private float rotationSpeed = 12f;
    private Vector3f randomVec;
    private Geometry geotest;
    private NPCPath path;
    
    public NPCPhysicsControl(CollisionShape shape, float mass, BulletAppState aps, NPCPath path) {
        super(shape, mass);
        this.path = path;

        
        randomVec = path.generatePath();
        
        aps.getPhysicsSpace().addTickListener(physics); 
        
    }
    
    PhysicsTickListener physics = new PhysicsTickListener() {

        public void prePhysicsTick(PhysicsSpace space, float f) {

    // Ship Rotation
    randomVec = path.getPath();            
    Vector3f pathVec = randomVec.subtract(getPhysicsLocation().clone());
    
    float distance = getPhysicsLocation().clone().distance(randomVec);
    angle = pathVec.normalize().angleBetween(getPhysicsRotation().mult(Vector3f.UNIT_Z).normalizeLocal());
//    System.out.println(angle);              
            
    // Ship Movement
    if (move) {
        applyCentralForce(getPhysicsRotation().mult(Vector3f.UNIT_Z).normalizeLocal().multLocal(25f));
    } else if (distance < 9f) {
        applyCentralForce(pathVec.normalize().multLocal(25f));
    }
    
    if (distance < 3f) {
        path.setNewPath();
    }
    
    Quaternion qua = new Quaternion();
    qua = getPhysicsRotation().clone();
    qua.lookAt(pathVec, Vector3f.UNIT_Y);

      if (angle >= 0.001f) {
    Vector3f dirSpatial = getPhysicsRotation().mult(Vector3f.UNIT_Z);
    Vector3f dirPoint = qua.mult(Vector3f.UNIT_Z);
    Vector3f cross = dirSpatial.crossLocal(dirPoint).normalizeLocal();
 
    Vector3f dirSpatial1 = getPhysicsRotation().mult(Vector3f.UNIT_Y);
    Vector3f dirPoint1 = qua.mult(Vector3f.UNIT_Y);
    Vector3f cross1 = dirSpatial1.crossLocal(dirPoint1).normalizeLocal();
 
    Vector3f dirSpatial2 = getPhysicsRotation().mult(Vector3f.UNIT_X);
    Vector3f dirPoint2 = qua.mult(Vector3f.UNIT_X);
    Vector3f cross2 = dirSpatial2.crossLocal(dirPoint2).normalizeLocal();
 
    applyTorque(cross.addLocal(cross1).addLocal(cross2).normalizeLocal().mult(rotationSpeed * angle));
      }    

    }

        public void physicsTick(PhysicsSpace space, float f) {

        }
    };

}
