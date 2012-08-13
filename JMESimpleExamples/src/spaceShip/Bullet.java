/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spaceShip;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Line;
import java.util.List;

/**
 *
 * @author mifth
 */
public class Bullet extends AbstractControl {

    private Geometry bullet;
    private GhostControl ghost;
    private Vector3f vecMove, bornPlace, contactPoint;
    private BulletAppState state;
    private boolean work = true;
    private float bulletLength;
    
    public Bullet(Vector3f bornPlace, Geometry bullet, BulletAppState state, CollisionShape shape, SimpleApplication asm) {

        this.bullet = bullet;
        this.bullet.setUserData("Type", "Bullet");
        this.state = state;
        this.bornPlace = bornPlace;
        
        vecMove = bullet.getLocalRotation().mult(Vector3f.UNIT_Z).normalizeLocal().mult(7f);        
        bulletLength = 100f;
        
        ghost = new GhostControl(shape);
        this.bullet.addControl(ghost);
        this.state.getPhysicsSpace().add(ghost);
        

//        // testRay
//        Geometry geoRay = new Geometry("line", new Line(bullet.getLocalTranslation().clone(), bullet.getLocalTranslation().add(bullet.getLocalRotation().mult(Vector3f.UNIT_Z).normalizeLocal().mult(bulletLength))));
//        Material mat_bullet = new Material(asm.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//        mat_bullet.setColor("Color", ColorRGBA.Red);
//        geoRay.setMaterial(mat_bullet);
//        asm.getRootNode().attachChild(geoRay);
        
        List<PhysicsRayTestResult> rayTest = this.state.getPhysicsSpace().rayTest(bullet.getLocalTranslation().clone(), bullet.getLocalTranslation().add(bullet.getLocalRotation().mult(Vector3f.UNIT_Z).normalizeLocal().mult(bulletLength)));
        if (rayTest.size() > 0) {
            PhysicsRayTestResult getObject = rayTest.get(0);
            PhysicsCollisionObject collisionObject = getObject.getCollisionObject();
            Spatial sp = (Spatial) collisionObject.getUserObject();

            System.out.println(sp);
            
            // Find CollisionPoint
            CollisionResults results = new CollisionResults();
            Ray ray = new Ray(bullet.getLocalTranslation().clone(), bullet.getLocalRotation().mult(Vector3f.UNIT_Z).normalizeLocal());
            sp.collideWith(ray, results);
             if (results.size() > 0) {
          // The closest collision point is what was truly hit:
          CollisionResult closest = results.getClosestCollision();
            contactPoint = closest.getContactPoint();
                    }

        }        
        
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
            
            if(contactPoint != null) {
                System.out.println("eeyyyyy");
            float contactPointDistance = bornPlace.distance(contactPoint);
            
             if (distance > contactPointDistance) {
                destroy();
                return;                
             }
            }
            
            if(distance > bulletLength) {
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
