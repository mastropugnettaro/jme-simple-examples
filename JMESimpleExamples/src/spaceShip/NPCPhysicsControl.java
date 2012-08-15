/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spaceShip;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 *
 * @author mifth
 */
public class NPCPhysicsControl extends RigidBodyControl {

    private Camera cam;
    private boolean move = true;
    private float angle;
    private float rotationSpeed = 17f;
    private Vector3f randomVec;
    private Geometry geotest;
    private SimpleApplication asm;
    private NPCPath path;
    
    public NPCPhysicsControl(CollisionShape shape, float mass, BulletAppState aps, SimpleApplication asm, NPCPath path) {
        super(shape, mass);
        this.path = path;
        this.asm = asm;
        
        randomVec = path.generatePath();
        
        aps.getPhysicsSpace().addTickListener(physics); 
        
    }
    
    PhysicsTickListener physics = new PhysicsTickListener() {

        public void prePhysicsTick(PhysicsSpace space, float f) {
            
    // Ship Movement
    if (move) {
        applyCentralForce(getPhysicsRotation().mult(Vector3f.UNIT_Z).normalizeLocal().multLocal(25f));
    }
     
    
    // Ship Rotation
    randomVec = path.getPath();
    
    float distance = getPhysicsLocation().clone().distance(randomVec);
    if (distance < 3f) {
        path.setNewPath();
    }
    
    Quaternion qua = new Quaternion();
    qua = getPhysicsRotation();
    qua.lookAt(randomVec.subtract(getPhysicsLocation().clone()), Vector3f.UNIT_Y);
    
    angle = randomVec.subtract(getPhysicsLocation().clone()).normalizeLocal().angleBetween(getPhysicsRotation().clone().mult(Vector3f.UNIT_Z).normalizeLocal());
//    System.out.println(angle);  

      if (angle < 0.001f) {
        return;
      }    
    
    Vector3f dirSpatial = getPhysicsRotation().clone().mult(Vector3f.UNIT_Z);
    Vector3f dirPoint = qua.mult(Vector3f.UNIT_Z);
    Vector3f cross = dirSpatial.crossLocal(dirPoint).normalizeLocal();
 
    Vector3f dirSpatial1 = getPhysicsRotation().clone().mult(Vector3f.UNIT_Y);
    Vector3f dirPoint1 = qua.mult(Vector3f.UNIT_Y);
    Vector3f cross1 = dirSpatial1.crossLocal(dirPoint1).normalizeLocal();
 
    Vector3f dirSpatial2 = getPhysicsRotation().clone().mult(Vector3f.UNIT_X);
    Vector3f dirPoint2 = qua.mult(Vector3f.UNIT_X);
    Vector3f cross2 = dirSpatial2.crossLocal(dirPoint2).normalizeLocal();
 
    applyTorque(cross.addLocal(cross1).addLocal(cross2).normalizeLocal().mult(rotationSpeed * angle));

    }

        
        public void physicsTick(PhysicsSpace space, float f) {

        }
    };

    
}
