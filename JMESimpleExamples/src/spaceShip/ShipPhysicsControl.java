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
    
    public ShipPhysicsControl(Camera camera, CollisionShape shape, float mass) {
        super(shape, mass);
        cam = camera;
    }
    
    void makeMove(boolean boo) {
        
        if (boo) move = true;
        else if (!boo) move = false;
    }
    
    public void prePhysicsTick(PhysicsSpace space, float f) {

      angle = cam.getRotation().clone().mult(Vector3f.UNIT_Z).normalizeLocal().angleBetween(getPhysicsRotation().clone().mult(Vector3f.UNIT_Z).normalizeLocal());        
      System.out.println(angle);
        
        // Ship Movement
        if (move) applyCentralForce(cam.getDirection().normalizeLocal().multLocal(30f));
        
        // Ship Rotation
      if (angle > 0.01f) {
      // Rotate the ship to Cam direction
      time += f * 0.01f;
      if (time > 0.99) time = 0.001f;
      
      Quaternion shipRot = getPhysicsRotation().clone();
      shipRot.slerp(cam.getRotation(), time);        
      setPhysicsRotation(shipRot);
    } else if (angle < 0.01f && angle != 0) {
          time = 0.001f;
          setPhysicsRotation(cam.getRotation());
      }
    
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
