package entitysystem;


import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;




public class EntityTest extends SimpleApplication {

    public static void main(String[] args) {
        EntityTest app = new EntityTest();
        app.start();
    }

              
    private Node camTrackHelper;
    private EntityManager entityManager = new EntityManager();
    
    @Override
    public void simpleInitApp() {
        
        setLight();

    // Entity stressTest
    for (int i=0; i<500 ; i++) {
    
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geo = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//        mat.getAdditionalRenderState().setWireframe(true);
        geo.setMaterial(mat);
        Node selectedSp = new Node();
        selectedSp.attachChild(geo);        
        rootNode.attachChild(selectedSp); 

        // setup Entity
        long ent = entityManager.createEntity();                
        
        ComponentControl components = entityManager.addComponentControl(ent);
        
        ComponentEntityName name = new ComponentEntityName("ent" + i);
        components.setComponent(name);
        
        ComponentTransform transform = new ComponentTransform(selectedSp.getLocalTransform());
        components.setComponent(transform);
        
        EntitySpatialControl spatialControl = entityManager.addSpatialControl(selectedSp, ent);
        spatialControl.setType(EntitySpatialControl.SpatialType.Node);
        spatialControl.recurseNode();
        
        System.out.println(selectedSp.getUserData("EntityID"));
    }

        // check for hashCode
        System.out.println(entityManager.getComponentControl(1).toString());
        System.out.println(entityManager.getComponentControl(2).toString());
        System.out.println(entityManager.getComponentControl(100).toString());
    
    }

    
 
    
    private void setLight() {
        
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.8f, -0.6f, -0.08f).normalizeLocal());
        dl.setColor(new ColorRGBA(1,1,1,1));
        rootNode.addLight(dl);        
        
        flyCam.setMoveSpeed(30);
        viewPort.setBackgroundColor(ColorRGBA.Gray);   
    }
    


@Override
public void simpleUpdate(float tpf){

}
       
   }
 
    
 
      


