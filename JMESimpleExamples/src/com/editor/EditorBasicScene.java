package com.editor;


import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;



public class EditorBasicScene extends SimpleApplication{

    public static void main(String[] args) {
        EditorBasicScene app = new EditorBasicScene();
        app.start();
    }

              
    private Node camTrackHelper;

    @Override
    public void simpleInitApp() {
        
        createSimpleGui();
        setLight();
        setCamTrack();
        EditorCameraSets camSettings = new EditorCameraSets(cam, camTrackHelper, inputManager);
        createGrid();

        EditorGui gui = new EditorGui();
        stateManager.attach(gui); 
        
        
        // Selected Spatial for a while
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geo = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        geo.setMaterial(mat);
        Node selectedSp = new Node();
        selectedSp.attachChild(geo);        
        rootNode.attachChild(selectedSp);  
        
        selectedSp.addControl(new EditorTool((Application)this, selectedSp));
//        EditorTool edt = new EditorTool(this, selectedSp);
    
    }

    

    
    @Override
    public void simpleUpdate(float tpf) {
       
        
//        nifty.resolutionChanged();        
//        niftyDisplay.reshape(viewPort, cam.getWidth(), cam.getHeight());
//        nifty.update();
//        nifty.enableAutoScaling(cam.getWidth(), cam.getHeight());
        

//        
//        System.out.println(cam.getWidth() + "x" + cam.getHeight());
        
        
    }    
    
    
    
    private void setCamTrack() {
        
        flyCam.setEnabled(false);
        
        camTrackHelper  = new Node("camTrackHelper");
        
        // Red line for X axis
        final Line xAxis = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0.05f, 0f, 0f));
        xAxis.setLineWidth(2f);
        Geometry gxAxis = new Geometry("XAxis", xAxis);
        gxAxis.setModelBound(new BoundingBox());
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", new ColorRGBA(1.0f, 0.0f, 0.0f, 0.1f));
        mat1.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        gxAxis.setQueueBucket(Bucket.Transparent);
        gxAxis.setShadowMode(ShadowMode.Off);
        gxAxis.setMaterial(mat1);
//        gxAxis.setCullHint(CullHint.Never);
        
        camTrackHelper.attachChild(gxAxis);

        
        // Blue line for Y axis
        final Line yAxis = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0.05f, 0f));
        yAxis.setLineWidth(2f);
        Geometry gyAxis = new Geometry("ZAxis", yAxis);
        gyAxis.setModelBound(new BoundingBox());
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", new ColorRGBA(0.0f, 0.0f, 1.0f, 0.1f));
        mat2.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        gyAxis.setQueueBucket(Bucket.Transparent);        
        gyAxis.setShadowMode(ShadowMode.Off);
        gyAxis.setMaterial(mat2);
//        gzAxis.setCullHint(CullHint.Never);
        camTrackHelper.attachChild(gyAxis);               
        
        
        // Blue line for Z axis
        final Line zAxis = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 0.05f));
        zAxis.setLineWidth(2f);
        Geometry gzAxis = new Geometry("ZAxis", zAxis);
        gzAxis.setModelBound(new BoundingBox());
        Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", new ColorRGBA(0.0f, 1.0f, 0.0f, 0.1f));
        mat3.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        gxAxis.setQueueBucket(Bucket.Transparent);        
        gzAxis.setShadowMode(ShadowMode.Off);
        gzAxis.setMaterial(mat3);
//        gzAxis.setCullHint(CullHint.Never);
        camTrackHelper.attachChild(gzAxis);       
        
        CameraNode camnode = new CameraNode("this", cam);
//        camnode.attachChild(camTrackHelper);
        rootNode.attachChild(camnode);
        rootNode.attachChild(camTrackHelper);

    }
    
    private void setLight() {
        
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.8f, -0.6f, -0.08f).normalizeLocal());
        dl.setColor(new ColorRGBA(1,1,1,1));
        rootNode.addLight(dl);        
      
        
        flyCam.setMoveSpeed(30);
        viewPort.setBackgroundColor(ColorRGBA.Gray);   
    }
    

    
    private void createSimpleGui() {

        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize());
        ch.setText("W,A,S,D,Q,Z, MiddleMouseButton, RightMouseButton, Scroll"); // crosshairs
        ch.setColor(new ColorRGBA(1f,0.8f,0.1f,0.3f));
        ch.setLocalTranslation(settings.getWidth()*0.1f,settings.getHeight()*0.1f,0);
        guiNode.attachChild(ch);         

    }
    
    private void createGrid(){
        Node gridNode = new Node("gridNode");
        
        //Create a grid plane
        Geometry g = new Geometry("GRID", new Grid(101, 101, 1f) );
        Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floor_mat.getAdditionalRenderState().setWireframe(true);
        floor_mat.setColor("Color", new ColorRGBA(0.3f, 0.3f, 0.3f, 0.1f));
        floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        g.setShadowMode(ShadowMode.Off);
        g.setQueueBucket(Bucket.Transparent);
        g.setMaterial(floor_mat);
        g.center().move(new Vector3f(0f,0f,0f));
        gridNode.attachChild(g);

        // Red line for X axis
        final Line xAxis = new Line(new Vector3f(-50f, 0f, 0f), new Vector3f(50f, 0f, 0f));
        xAxis.setLineWidth(2f);
        Geometry gxAxis = new Geometry("XAxis", xAxis);
        gxAxis.setModelBound(new BoundingBox());
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1.0f, 0.2f, 0.2f, 0.2f));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        gxAxis.setQueueBucket(Bucket.Transparent);
        gxAxis.setShadowMode(ShadowMode.Off);
        gxAxis.setMaterial(mat);
        gxAxis.setCullHint(CullHint.Never);
        
        gridNode.attachChild(gxAxis);

        // Blue line for Z axis
        final Line zAxis = new Line(new Vector3f(0f, 0f, -50f), new Vector3f(0f, 0f, 50f));
        zAxis.setLineWidth(2f);
        Geometry gzAxis = new Geometry("ZAxis", zAxis);
        gzAxis.setModelBound(new BoundingBox());
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.2f, 1.0f, 0.2f, 0.2f));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        gxAxis.setQueueBucket(Bucket.Transparent);        
        gzAxis.setShadowMode(ShadowMode.Off);
        gzAxis.setMaterial(mat);
        gzAxis.setCullHint(CullHint.Never);
        gridNode.attachChild(gzAxis);

        rootNode.attachChild(gridNode);
        
    }

}
