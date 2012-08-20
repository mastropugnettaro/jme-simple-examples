/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.editor;

import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author mifth
 */
public class EditorCameraSets {
    
   private Camera cam;
   private Node spatial;
   private InputManager imputMan;
   private ChaseCamera chaseCam;
   
    public EditorCameraSets (Camera camera, Spatial sp, InputManager impMan) {
        
        cam = camera;
        spatial = (Node) sp;
        imputMan = impMan;
        
        setCameraNow();
//        setOrtho(true);
        
    }
    
 
    
    private void setCameraNow(){
        
    // Enable a chase cam
     chaseCam = new ChaseCamera(cam, spatial, imputMan);

    //Uncomment this to invert the camera's vertical rotation Axis 
    chaseCam.setInvertVerticalAxis(true);

    //Uncomment this to invert the camera's horizontal rotation Axis
//    chaseCam.setInvertHorizontalAxis(true);

    //Comment this to disable smooth camera motion
//    chaseCam.setSmoothMotion(true);
//    chaseCam.setChasingSensitivity(100);
//    chaseCam.setTrailingSensitivity(500);
//    chaseCam.setDragToRotate(false);

    //Uncomment this to disable trailing of the camera 
    //WARNING, trailing only works with smooth motion enabled. It is true by default.
    chaseCam.setTrailingEnabled(false);

    //Uncomment this to look 3 world units above the target
//    chaseCam.setLookAtOffset(Vector3f.UNIT_Y.mult(3));
    chaseCam.setMinVerticalRotation(-FastMath.PI*0.45f);
    chaseCam.setMaxVerticalRotation(FastMath.PI*0.45f);
    //Uncomment this to enable rotation when the middle mouse button is pressed (like Blender)
    //WARNING : setting this trigger disable the rotation on right and left mouse button click
//    chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));

    //Uncomment this to set mutiple triggers to enable rotation of the cam
    //Here spade bar and middle mouse button
    chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
    
    chaseCam.setDefaultDistance(10);
    chaseCam.setMinDistance(0.05f);
    chaseCam.setMaxDistance(500);    
}
 
    
    protected void setOrtho(boolean bool) {
        
      if (bool == true) {
          
         
//         Camera cam2 = cam.clone(); 
         
         cam.setParallelProjection(true);
         float aspect = (float) cam.getWidth() / cam.getHeight();
         float frustumSize = 100f;
         cam.setFrustum(cam.getFrustumNear(), cam.getFrustumFar(), -aspect * frustumSize, aspect * frustumSize, frustumSize, -frustumSize);   
         
      } else if (bool == false) {
          
          cam.setParallelProjection(false);
      }

    }
    
}
