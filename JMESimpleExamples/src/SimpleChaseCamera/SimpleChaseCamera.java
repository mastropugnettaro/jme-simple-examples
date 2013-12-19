/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SimpleChaseCamera;

import com.jme3.app.Application;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author mifth
 */
public class SimpleChaseCamera implements ActionListener, AnalogListener {

    private Node chaseGeneralNode, chaseCamNode, chaseRotateHelper;
    private Application app;
    private InputManager inputManager;
    public final static String ChaseCamDown = "ChaseCamDown";
    public final static String ChaseCamUp = "ChaseCamUp";
    public final static String ChaseCamZoomIn = "ChaseCamZoomIn";
    public final static String ChaseCamZoomOut = "ChaseCamZoomOut";
    public final static String ChaseCamMoveLeft = "ChaseCamMoveLeft";
    public final static String ChaseCamMoveRight = "ChaseCamMoveRight";
    public final static String ChaseCamToggleRotate = "ChaseCamToggleRotate";
    private boolean doRotate, doVerticalConstraint, doZoom, zoomIn;
    private float horizontRotate, verticalRotate, verticalUpLimit, verticalDownLimit;
    private float rotateSpeed, zoomStep, zoomMax, zoomMin;

    public SimpleChaseCamera(Application app, InputManager inputManager) {
        this.app = app;
        this.inputManager = inputManager;

        doRotate = false;
        doZoom = false;
        zoomIn = false;

        horizontRotate = 0.0f;
        verticalRotate = 0.0f;
        verticalUpLimit = FastMath.QUARTER_PI;
        verticalDownLimit = 0.01f;
        doVerticalConstraint = true;
        rotateSpeed = 1.0f;

        zoomStep = 1.7f;
        zoomMin = 2;
        zoomMax = 50;

        chaseGeneralNode = new Node("chaseNode");
        chaseCamNode = new Node("chaseCamNode");
        chaseGeneralNode.attachChild(chaseCamNode);
        chaseRotateHelper = new Node("chaaseRotateHelper");
        chaseGeneralNode.attachChild(chaseRotateHelper);


        chaseCamNode.setLocalTranslation(0, 0, 5f);
        chaseCamNode.setLocalRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f));

        registerWithInput(inputManager);
    }

    /**
     * Registers inputs with the input manager
     *
     * @param inputManager
     */
    public final void registerWithInput(InputManager inputManager) {

        String[] inputs = {ChaseCamToggleRotate,
            ChaseCamDown,
            ChaseCamUp,
            ChaseCamMoveLeft,
            ChaseCamMoveRight,
            ChaseCamZoomIn,
            ChaseCamZoomOut};

//        this.inputM = inputManager;
//        if (!invertYaxis) {
        inputManager.addMapping(ChaseCamDown, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping(ChaseCamUp, new MouseAxisTrigger(MouseInput.AXIS_Y, false));
//        } else {
//            inputManager.addMapping(ChaseCamDown, new MouseAxisTrigger(MouseInput.AXIS_Y, false));
//            inputManager.addMapping(ChaseCamUp, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
//        }
        inputManager.addMapping(ChaseCamZoomIn, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(ChaseCamZoomOut, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
//        if (!invertXaxis) {
        inputManager.addMapping(ChaseCamMoveLeft, new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping(ChaseCamMoveRight, new MouseAxisTrigger(MouseInput.AXIS_X, false));
//        } else {
//            inputManager.addMapping(ChaseCamMoveLeft, new MouseAxisTrigger(MouseInput.AXIS_X, false));
//            inputManager.addMapping(ChaseCamMoveRight, new MouseAxisTrigger(MouseInput.AXIS_X, true));
//        }
        inputManager.addMapping(ChaseCamToggleRotate, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
//        inputManager.addMapping(ChaseCamToggleRotate, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        inputManager.addListener(this, inputs);
    }

    private void rotateHorizontally(float value) {
        chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(new Quaternion().fromAngleAxis(value * 1f, Vector3f.UNIT_Y)));

    }

    private void rotateVertically(float value) {
        chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(new Quaternion().fromAngleAxis(value * 1f, Vector3f.UNIT_X)));
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(ChaseCamToggleRotate) && isPressed) {
            doRotate = true;
//            if (storedRotation == null) {
//                storedRotation = chaseCamNode.getLocalRotation().clone();
//            }
        } else if (name.equals(ChaseCamToggleRotate) && !isPressed) {
            doRotate = false;
//            storedRotation = null;
            horizontRotate = 0;
            verticalRotate = 0;
        }

        if (name.equals(ChaseCamZoomIn) && isPressed) {
            doZoom = true;
            zoomIn = true;
        } else if (name.equals(ChaseCamZoomOut) && isPressed) {
            doZoom = true;
            zoomIn = false;
        }
    }

    public void onAnalog(String name, float value, float tpf) {

        if (doRotate) {
            if (name.equals(ChaseCamMoveLeft)) {
                horizontRotate = value;
//                rotateHorizontally(-value);
            } else if (name.equals(ChaseCamMoveRight)) {
                horizontRotate = -value;
//                rotateHorizontally(value);
            } else if (name.equals(ChaseCamUp)) {
                verticalRotate = value;
//                rotateVertically(value);
            } else if (name.equals(ChaseCamDown)) {
                verticalRotate = -value;
//                rotateVertically(-value);
            }
        }

    }

    public void update() {

        if (doRotate) {

            // HORIZONTAL
            Quaternion chaseRot = chaseGeneralNode.getLocalRotation().clone();
            chaseGeneralNode.setLocalRotation(new Quaternion());
            chaseRotateHelper.setLocalRotation(chaseRot);

            Quaternion yRot = new Quaternion().fromAngleAxis(horizontRotate * rotateSpeed, Vector3f.UNIT_Y);
            chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(yRot));
            chaseGeneralNode.setLocalRotation(chaseRotateHelper.getWorldRotation());


            // VERTICAL
            Quaternion xRot = new Quaternion().fromAngleAxis(verticalRotate * rotateSpeed, Vector3f.UNIT_X);
            chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(xRot));


            // VERTICAL LIMITATION
            if (doVerticalConstraint) {
                float angleVerticalNow = chaseCamNode.getWorldTranslation().subtract(chaseGeneralNode.getLocalTranslation()).normalizeLocal().
                        angleBetween(Vector3f.UNIT_Y);

                if (angleVerticalNow < verticalUpLimit || angleVerticalNow > verticalDownLimit + FastMath.HALF_PI) {
                    float rotateToVertical = verticalUpLimit - angleVerticalNow; // rotateUp

                    if (angleVerticalNow > verticalDownLimit + FastMath.HALF_PI) {
                        rotateToVertical = (verticalDownLimit - angleVerticalNow) + FastMath.HALF_PI;
                    } // if rotateDown

                    Quaternion xRotAgain = chaseGeneralNode.getLocalRotation().clone().fromAngleAxis(rotateToVertical, Vector3f.UNIT_X);
                    chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(xRotAgain));
                }
            }


            horizontRotate = 0;
            verticalRotate = 0;
        }

        if (doZoom) {
            Vector3f zoomVec = Vector3f.UNIT_Z.clone().multLocal(zoomStep);

            if (zoomIn) {
                chaseCamNode.setLocalTranslation(chaseCamNode.getLocalTranslation().add(zoomVec.negateLocal()));
            } else {
                chaseCamNode.setLocalTranslation(chaseCamNode.getLocalTranslation().add(zoomVec));
            }

            if (chaseCamNode.getLocalTranslation().z > zoomMax) {
                chaseCamNode.setLocalTranslation(new Vector3f(0, 0, zoomMax));
            } else if (chaseCamNode.getLocalTranslation().z < zoomMin) {
                chaseCamNode.setLocalTranslation(new Vector3f(0, 0, zoomMin));
            }

            doZoom = false;
        }

        app.getCamera().setLocation(chaseCamNode.getWorldTranslation());
        app.getCamera().setRotation(chaseCamNode.getWorldRotation());
    }

    public Node getChaseGeneralNode() {
        return chaseGeneralNode;
    }

    public float getRotateSpeed() {
        return rotateSpeed;
    }

    public void setRotateSpeed(float rotateSpeed) {
        this.rotateSpeed = rotateSpeed;
    }

    public float getVerticalUpLimit() {
        return verticalUpLimit;
    }

    public void setVerticalUpLimit(float verticalUpLimit) {
        this.verticalUpLimit = verticalUpLimit;
    }

    public float getVerticalDownLimit() {
        return verticalDownLimit;
    }

    public void setVerticalDownLimit(float verticalDownLimit) {
        this.verticalDownLimit = verticalDownLimit;
    }

    public float getZoomMax() {
        return zoomMax;
    }

    public void setZoomMax(float zoomMax) {
        this.zoomMax = zoomMax;
    }

    public float getZoomMin() {
        return zoomMin;
    }

    public void setZoomMin(float zoomMin) {
        this.zoomMin = zoomMin;
    }

    public float getZoomStep() {
        return zoomStep;
    }

    public void setZoomStep(float zoomStep) {
        this.zoomStep = zoomStep;
    }

    public boolean isDoVerticalConstraint() {
        return doVerticalConstraint;
    }

    public void setDoVerticalConstraint(boolean doVerticalConstraint) {
        this.doVerticalConstraint = doVerticalConstraint;
    }
    
}