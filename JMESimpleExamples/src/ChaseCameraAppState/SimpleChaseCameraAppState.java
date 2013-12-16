/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ChaseCameraAppState;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import static com.jme3.input.ChaseCamera.ChaseCamDown;
import static com.jme3.input.ChaseCamera.ChaseCamMoveLeft;
import static com.jme3.input.ChaseCamera.ChaseCamMoveRight;
import static com.jme3.input.ChaseCamera.ChaseCamToggleRotate;
import static com.jme3.input.ChaseCamera.ChaseCamUp;
import static com.jme3.input.ChaseCamera.ChaseCamZoomIn;
import static com.jme3.input.ChaseCamera.ChaseCamZoomOut;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author mifth
 */
public class SimpleChaseCameraAppState extends AbstractAppState implements ActionListener, AnalogListener {

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
    private boolean doRotate;
    private float horizontRotate, verticalRotate;
//    private Quaternion storedRotation;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
        this.inputManager = app.getInputManager();

        doRotate = false;
        horizontRotate = 0.0f;
        verticalRotate = 0.0f;

        registerWithInput(inputManager);
//        inputManager.addMapping(ChaseCamToggleRotate, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        chaseGeneralNode = new Node("chaseNode");
        chaseCamNode = new Node("chaseCamNode");
        chaseGeneralNode.attachChild(chaseCamNode);
        chaseRotateHelper = new Node("chaaseRotateHelper");
        chaseGeneralNode.attachChild(chaseRotateHelper);


        chaseCamNode.setLocalTranslation(0, 0, 5f);
        chaseCamNode.setLocalRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f));

//        chaseGeneralNode.rotate(0f, 0.5f, 0f); // test

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
    }

    public void onAnalog(String name, float value, float tpf) {

        if (doRotate) {
            if (name.equals(ChaseCamMoveLeft)) {
                horizontRotate = -value;
//                rotateHorizontally(-value);
            } else if (name.equals(ChaseCamMoveRight)) {
                horizontRotate = value;
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

    @Override
    public void update(float tpf) {

        if (doRotate) {
            Quaternion chaseRot = chaseGeneralNode.getLocalRotation().clone();
            chaseGeneralNode.setLocalRotation(new Quaternion());
            chaseRotateHelper.setLocalRotation(chaseRot);
            
            Quaternion xRot = new Quaternion().fromAngleAxis(verticalRotate * 1f, chaseRot.mult(Vector3f.UNIT_X));
            chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(xRot));

            Quaternion yRot = new Quaternion().fromAngleAxis(horizontRotate * 1f, Vector3f.UNIT_Y);
            chaseGeneralNode.setLocalRotation(chaseGeneralNode.getLocalRotation().mult(yRot));
            
            chaseGeneralNode.setLocalRotation(chaseRotateHelper.getWorldRotation().clone());
        }

        app.getCamera().setLocation(chaseCamNode.getWorldTranslation());
        app.getCamera().setRotation(chaseCamNode.getWorldRotation());
    }

    @Override
    public void cleanup() {
        super.cleanup();

    }
}
