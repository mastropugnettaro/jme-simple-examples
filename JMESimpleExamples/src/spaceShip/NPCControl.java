/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spaceShip;

import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import java.io.IOException;

/**
 *
 * @author mifth
 */
public class NPCControl extends AbstractControl implements Savable, Cloneable {

    private boolean doMove = true;
    private boolean doRotate = true;
    private float angle, distance;
    private ShipPhysicsControl shipControl;
    private Node generalNode, enemyShip;
    private Geometry geotest;
    private Vector3f randomVec = new Vector3f();
    private Vector3f pathVec;
    private Quaternion qua = new Quaternion();    
    private AssetManager asm;
    private float rotateSpeed;

    public NPCControl(Node parentNode, Node enemy, 
            AssetManager asm, ShipPhysicsControl shipControl) {
        this.enemyShip = enemy;
        this.generalNode = parentNode;
        this.asm = asm;

        this.shipControl = shipControl;
        shipControl.setMoveSpeed(25f);
        shipControl.setRotateSpeed(12f);
        rotateSpeed = 12f;

        generateNewPath();
        debugPathModel();

        shipControl.setPhysicsLocation(randomVec);

    }

    Vector3f generateNewPath() {
        Vector3f vec = new Vector3f((float) Math.random() * 50.0f, (float) Math.random() * 50.0f, ((float) Math.random() * 50.0f) - 50f);
        randomVec = vec;
        return vec;
    }

    Vector3f getPath() {
        return randomVec;
    }

    private void debugPathModel() {

        Box b = new Box(Vector3f.ZERO, 0.2f, 0.2f, 0.2f);
        Geometry bullet = new Geometry("Box", b);
        Material mat_bullet = new Material(asm, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_bullet.setColor("Color", ColorRGBA.Cyan);
        bullet.setMaterial(mat_bullet);
        geotest = new Geometry(null, b);
        geotest.setMaterial(mat_bullet);
        geotest.setUserData("Type", "Point");
        geotest.setLocalTranslation(randomVec);
        generalNode.attachChild(geotest);
    }

    @Override
    protected void controlUpdate(float tpf) {

        distance = shipControl.getPhysicsLocation().clone().distance(randomVec);
        pathVec = randomVec.subtract(shipControl.getPhysicsLocation().clone());

        // Enemy Movement
        if (doMove) {
            shipControl.setFlyDirection(shipControl.getPhysicsRotation().mult(Vector3f.UNIT_Z).normalizeLocal());
          if (distance < 9f) {
            shipControl.setFlyDirection(pathVec.normalize());
         }
        }

        if (doRotate) {
//            qua = new Quaternion();
            qua = shipControl.getPhysicsRotation().clone();
            qua.lookAt(pathVec, Vector3f.UNIT_Y);
            angle = pathVec.normalize().angleBetween(shipControl.getPhysicsRotation().mult(Vector3f.UNIT_Z).normalizeLocal());
            shipControl.setViewDirection(qua);
            shipControl.setRotateSpeed(rotateSpeed * angle);
        }
        
        if (distance < 3f) {
            generateNewPath();
        } 
        
        if (geotest != null) {
            geotest.setLocalTranslation(randomVec);
        }        
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Control cloneForSpatial(Spatial spatial) {
//        PlayerControl control = new PlayerControl();
//        //TODO: copy parameters to new Control
//        control.setSpatial(spatial);
//        return control;
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        //TODO: load properties of this Control, e.g.
        //this.value = in.readFloat("name", defaultValue);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
        //TODO: save properties of this Control, e.g.
        //out.write(this.value, "name", defaultValue);
    }
}
