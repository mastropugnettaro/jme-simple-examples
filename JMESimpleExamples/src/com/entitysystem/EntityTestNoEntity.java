package com.entitysystem;


import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;



public class EntityTestNoEntity extends SimpleApplication {

    public static void main(String[] args) {
        EntityTestNoEntity app = new EntityTestNoEntity();
        app.start();
    }

              
    private Node camTrackHelper;

    
    @Override
    public void simpleInitApp() {
        
        setLight();
 
for (int i=0; i<500 ; i++) {
    
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geo = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//        mat.getAdditionalRenderState().setWireframe(true);
        geo.setMaterial(mat);
        Node selectedSp = new Node();
        selectedSp.attachChild(geo);        
        rootNode.attachChild(selectedSp);      
}

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
 
    
 
      


