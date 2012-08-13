/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spaceShip;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author mifth
 */
public class Bullet extends AbstractControl {

    private Geometry bullet;
    private GhostControl ghost;
    private Vector3f vecMove;
    
    public Bullet(Vector3f path, Geometry bullet, BulletAppState state, CollisionShape shape) {

        this.bullet = bullet;

        vecMove = bullet.getLocalRotation().mult(Vector3f.UNIT_Z).normalizeLocal().mult(2f);        
        
        ghost = new GhostControl(shape);
        this.bullet.addControl(ghost);
        state.getPhysicsSpace().add(ghost);
        
    }



    @Override
    protected void controlUpdate(float tpf) {
            bullet.move(vecMove);                            
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
