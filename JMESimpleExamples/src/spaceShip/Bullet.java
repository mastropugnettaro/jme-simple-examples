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
    private Vector3f vecMove, bornPlace;
    private BulletAppState state;
    private boolean work = true;
    
    public Bullet(Vector3f bornPlace, Geometry bullet, BulletAppState state, CollisionShape shape) {

        this.bullet = bullet;
        this.bullet.setUserData("Type", "Bullet");
        this.state = state;
        this.bornPlace = bornPlace;
        
        vecMove = bullet.getLocalRotation().mult(Vector3f.UNIT_Z).normalizeLocal().mult(5f);        
        
        ghost = new GhostControl(shape);
        this.bullet.addControl(ghost);
        this.state.getPhysicsSpace().add(ghost);
        
    }

    protected void destroy() {
        state.getPhysicsSpace().remove(ghost);
        bullet.removeControl(ghost);
        work = false;
        bullet.removeFromParent();
        bullet.removeControl(this);
        bullet = null;
        
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(work) {    
            float distance = bornPlace.distance(bullet.getLocalTranslation());
            if(distance > 100f) {
                destroy();
                return;
            }
            
             bullet.move(vecMove);                            
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
