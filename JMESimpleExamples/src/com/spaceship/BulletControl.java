/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spaceship;

import com.jme3.bullet.control.GhostControl;
import com.jme3.export.Savable;
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
public class BulletControl extends AbstractControl implements Savable, Cloneable {

    private Geometry bullet;
    private GhostControl ghost;
    private Vector3f vecMove, bornPlace, contactPoint;

    private boolean work = true;
    private float bulletLength;

    
    public BulletControl(Vector3f bornPlace, Vector3f contactPoint, float bulletLength, Geometry bullet) {

        this.bullet = bullet;
        this.bullet.setUserData("Type", "Bullet");
        this.bulletLength = bulletLength;
        this.contactPoint = contactPoint;
        this.bornPlace = bornPlace;
        
        vecMove = bullet.getLocalRotation().mult(Vector3f.UNIT_Z).normalizeLocal().mult(7f);        

        
//        // testRay
//        Geometry geoRay = new Geometry("line", new Line(bullet.getLocalTranslation().clone(), bullet.getLocalTranslation().add(bullet.getLocalRotation().mult(Vector3f.UNIT_Z).normalizeLocal().mult(bulletLength))));
//        Material mat_bullet = new Material(asm.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//        mat_bullet.setColor("Color", ColorRGBA.Red);
//        geoRay.setMaterial(mat_bullet);
//        asm.getRootNode().attachChild(geoRay);
        
    }

    protected void destroy() {
        
        bullet.removeFromParent();
        bullet.removeControl(this);
        work = false;
        bullet = null;
        
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(work) {    
            float distance = bornPlace.distance(bullet.getLocalTranslation());
            
            if(contactPoint != null) {
                System.out.println("eeyyyyy");
            float contactPointDistance = bornPlace.distance(contactPoint);
            
             if (distance >= contactPointDistance) {
                destroy();
                return;                
             }
            }
            
            if(distance >= bulletLength) {
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
