package spaceShip;


import Basics.*;
import com.bulletphysics.dynamics.RigidBody;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import jme3tools.optimize.GeometryBatchFactory;


public class spaceShip extends SimpleApplication implements ActionListener, AnalogListener{

    public static void main(String[] args) {
        spaceShip app = new spaceShip();
        app.start();
    }

    Geometry geom;     
    Node instNodes = new Node();  
    BulletAppState bulletAppState;
    Node ship;
    RigidBodyControl shipControl;
    float time = 0.0001f;
              
    @Override
    public void simpleInitApp() {
        
        settings.setVSync(true);
        settings.setFrameRate(60);
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, 0, 0));
        
        flyCam.setEnabled(false);
//        flyCam.setMoveSpeed(30);
        viewPort.setBackgroundColor(ColorRGBA.Gray);
        
        UI();
        setAsteroids();
        setShip();
        setLight();
        setCam();
        setupKeys();
        
    }

    
    void UI() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize());
        ch.setText("MiddleMouse, RightMouse"); // crosshairs
        ch.setColor(new ColorRGBA(1f,0.8f,0.1f,1f));
        ch.setLocalTranslation(settings.getWidth()*0.3f,settings.getHeight()*0.1f,0);
        guiNode.attachChild(ch);        
    }
    
    
    void setupKeys() {
    
       //Set up keys and listener to read it
        String[] mappings = new String[]{
            "MoveShip"
        };
        
        inputManager.addListener(this, mappings);
        inputManager.addMapping("MoveShip", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
            
    }
    
    void setCam() {
        ChaseCamera chaseCam = new ChaseCamera(cam, ship, inputManager);
        chaseCam.setDragToRotate(true);
        chaseCam.setTrailingEnabled(false);
        
        chaseCam.setInvertVerticalAxis(true);
        
        chaseCam.setMinVerticalRotation(-FastMath.PI*0.45f);
        chaseCam.setMaxVerticalRotation(FastMath.PI*0.45f);
        
        chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        
        chaseCam.setEnabled(true);
    }
    
    void setLight() {
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.8f, -0.6f, -0.08f).normalizeLocal());
        dl.setColor(new ColorRGBA(0.9f,0.9f,0.7f,1));
        rootNode.addLight(dl);    
        
        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(1.0f,1.0f,1.7f,1));
        rootNode.addLight(al);           
    }
    
    void setShip() {
        
        Box b = new Box(Vector3f.ZERO, 0.5f, 0.5f, 1);
        Geometry geomShip = new Geometry("Box", b);
        
        ship = new Node("ship");
        ship.attachChild(geomShip);
        rootNode.attachChild(ship);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        ship.setMaterial(mat);  
        
        CollisionShape colShape = new BoxCollisionShape(new Vector3f(1.0f,1.0f,1.0f));
        colShape.setMargin(0.005f);
        shipControl = new RigidBodyControl(colShape, 1); 
        shipControl.setDamping(0.5f, 0.9f);
        shipControl.setFriction(0.5f);
//        shipControl.setGravity(new Vector3f(0, 0, 0));
        ship.addControl(shipControl);
        bulletAppState.getPhysicsSpace().add(ship);
        
    }
    
     
    void  setAsteroids() {

        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        geom.setMaterial(mat);

        int numClones = 500;
        
        for (int i=0; i<numClones; i++) {
        
            Geometry gm = geom.clone(false);
            gm.setName("instance"+i);
            gm.setLocalTranslation((float) Math.random() * 50.0f,(float) Math.random() * 30.0f - 15.0f,(float)Math.random() * 50.0f + 10.0f );
            gm.rotate(0, (float) Math.random() * (float)Math.PI, 0);

            CollisionShape colShape = new MeshCollisionShape(gm.getMesh());
            colShape.setMargin(0.005f);
            RigidBodyControl rigControl = new RigidBodyControl(colShape, 0);

            gm.addControl(rigControl);

            bulletAppState.getPhysicsSpace().add(gm); 

            instNodes.attachChild(gm);
            
            
        }

        bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        
        GeometryBatchFactory.optimize(instNodes);  // fps optimization
        rootNode.attachChild(instNodes);        
        
        
    }
    
 
      
@Override
public void simpleUpdate(float tpf)
{
    
    // Rotate the ship to Cam direction
    time += tpf * 0.01f;
    if (time > 0.99) time = 0.001f;
//    float angle = cam.getRotation().clone().mult(Vector3f.UNIT_Z).normalizeLocal().angleBetween(shipControl.getPhysicsRotation().clone().mult(Vector3f.UNIT_Z).normalizeLocal());
    Quaternion shipRot = shipControl.getPhysicsRotation().clone();
    shipRot.slerp(cam.getRotation(), time);        
    shipControl.setPhysicsRotation(shipRot);
          
 }

    public void onAction(String name, boolean isPressed, float tpf) {

    }

    public void onAnalog(String name, float value, float tpf) {
        
        if (name == "MoveShip") {
            // Move the Ship
            shipControl.setLinearVelocity(cam.getDirection().normalizeLocal().multLocal(30f));
            
            //The Variant id I use setPhysicsLocation(). Works without collision.
//            shipControl.setPhysicsLocation(shipControl.getPhysicsLocation().add(cam.getDirection().normalizeLocal().multLocal(0.1f)));            
        }

    }

}
