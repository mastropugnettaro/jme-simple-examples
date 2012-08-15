/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spaceShip;

import com.jme3.asset.AssetManager;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
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
public class NPCPath extends AbstractControl implements Savable, Cloneable {
    
    private Node root, node;
    private Geometry geotest;
    private Vector3f randomVec = new Vector3f();
    private AssetManager asm;
    private boolean update = false;
    
    public NPCPath(Node root, Node node, AssetManager asm) {
        this.node = node;
        this.root = root;
        this.asm = asm;
        
        createPathModel();
        generatePath();
        
        this.node.addControl(this);
        
    }

    Vector3f generatePath() {
      Vector3f vec = new Vector3f((float) Math.random() * 50.0f, (float) Math.random() * 50.0f, ( (float) Math.random() * 50.0f) - 50f);
      randomVec = vec;
      geotest.setLocalTranslation(vec);
      return vec;
    }
    
    Vector3f getPath() {
      return randomVec;
    }    
    
    void setNewPath() {
        update = true;
    }
    
    private void createPathModel() {

        Box b = new Box(Vector3f.ZERO, 0.2f, 0.2f, 0.2f);
        Geometry bullet = new Geometry("Box", b);
        Material mat_bullet = new Material(asm, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_bullet.setColor("Color", ColorRGBA.Cyan);
        bullet.setMaterial(mat_bullet);  
        geotest = new Geometry(null, b);
        geotest.setMaterial(mat_bullet);
        geotest.setUserData("Type", "Point");
        geotest.setLocalTranslation(randomVec);
        root.attachChild(geotest);           
    }

    @Override
    protected void controlUpdate(float tpf) {

        if (update) {
            generatePath();
            update = false;
        }

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
