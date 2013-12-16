package ChaseCameraAppState;


import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;



public class SimpleChaseCameraTest extends SimpleApplication {

    public static void main(String[] args) {
        SimpleChaseCameraTest app = new SimpleChaseCameraTest();
        AppSettings aps = new AppSettings(true);
        aps.setVSync(true);
        aps.setResolution(800, 600);
        app.setSettings(aps);
        app.start();
    }

    Geometry geom;
   
              
    @Override
    public void simpleInitApp() {
        Box b = new Box(Vector3f.ZERO, 0.5f, 0.5f, 0.5f);
        geom = new Geometry("Box", b);
        geom.updateModelBound();

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        geom.setMaterial(mat);
//        geom.setLocalTranslation(0,2,1);
        rootNode.attachChild(geom);
        
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.8f, -0.6f, -0.08f).normalizeLocal());
        dl.setColor(new ColorRGBA(1,1,1,1));
        rootNode.addLight(dl);        
      
        
//        flyCam.setMoveSpeed(30);
        flyCam.setEnabled(false);
        viewPort.setBackgroundColor(ColorRGBA.Gray);   
        
        SimpleChaseCameraAppState chState = new SimpleChaseCameraAppState();
        stateManager.attach(chState);
        
        ChaseCamera chk;
//        chk.setDragToRotate(true);
        
    }

    
     
      
@Override
public void simpleUpdate(float tpf)
{
          
 }

}
