/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spaceShip;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;

/**
 *
 * @author mifth
 */
public class EnemyManager extends  AbstractControl implements Savable, Cloneable {

    
    private Node enemyNode;
    private BulletAppState bulletAppState;
    private AssetManager asm;
    
    
    public EnemyManager(Node enemyNode, BulletAppState bulletAppState, AssetManager asm) {
    this.enemyNode = enemyNode;
    this.asm = asm;
    this.bulletAppState = bulletAppState;
    
    }
    
    void createEnemy() {
        
        Box b = new Box(Vector3f.ZERO, 0.5f, 0.5f, 1);
        Geometry geomShip = new Geometry("Box", b);
        geomShip.setUserData("Type", "Enemy");
        
        Node enemy = new Node("ship");
        enemy.attachChild(geomShip);
        enemy.setUserData("Type", "Enemy");
        enemyNode.attachChild(enemy);
        
        Material mat = new Material(asm, "Common/MatDefs/Light/Lighting.j3md");
        enemy.setMaterial(mat);  

        // path
        NPCPath path = new NPCPath(enemyNode, enemy, asm);
           
        CollisionShape colShape = new BoxCollisionShape(new Vector3f(1.0f,1.0f,1.0f));
        colShape.setMargin(0.05f);
        NPCPhysicsControl npcControl = new NPCPhysicsControl(colShape, 1, bulletAppState, path);
        npcControl.setDamping(0.7f, 0.9f);
        npcControl.setFriction(0.9f);
//        shipControl.setGravity(new Vector3f(0, 0, 0));
        enemy.addControl(npcControl);
        npcControl.setPhysicsLocation(path.getPath());
        bulletAppState.getPhysicsSpace().add(npcControl);
        npcControl.setEnabled(true);
        
    }    
    
    @Override
    protected void controlUpdate(float tpf) {
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
