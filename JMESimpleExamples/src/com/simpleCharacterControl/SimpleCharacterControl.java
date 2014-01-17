/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleCharacterControl;

import SimpleChaseCamera.SimpleCameraState;
import com.jme3.app.Application;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.List;

public class SimpleCharacterControl extends AbstractControl implements PhysicsTickListener {

    private Application app;
    private boolean doMove, doJump = false;
    private Vector3f walkDirection = new Vector3f(0, 0, 0);
    private Quaternion newRotation;
    private int stopTimer = 0;
    private boolean isMoving = false;
    private float angleNormals = 0;
    private PhysicsRayTestResult physicsClosestTets;
    private RigidBodyControl physSp;
    private float jumpSpeed, moveSpeed, moveSlopeSpeed, slopeLimitAngle, stopDamping, collisionShapeHeight;

    public SimpleCharacterControl(Application app, RigidBodyControl physSp, float collisionShapeHeight) {
        this.app = app;
        this.physSp = physSp;
        
        jumpSpeed = 40f;
        moveSpeed = 0.5f;
        moveSlopeSpeed = 0.3f;
        slopeLimitAngle = FastMath.DEG_TO_RAD * 35f;
        stopDamping = 0.8f;
        this.collisionShapeHeight = collisionShapeHeight;
        
        app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().addTickListener(this);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (newRotation != null) {
            spatial.setLocalRotation(newRotation);
            newRotation = null;
        }

//        app.getStateManager().getState(SimpleCameraState.class).getChState().update();
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void prePhysicsTick(PhysicsSpace space, float tpf) {

        if (doMove) {
//            walkDirection = new Vector3f();

            if (physicsClosestTets != null) {
                angleNormals = physicsClosestTets.getHitNormalLocal().normalizeLocal().angleBetween(Vector3f.UNIT_Y);
            }

//            float yCoord = physSp.getLinearVelocity().getY();
//            walkDirection.addLocal(spatial.getLocalTranslation().subtract(app.getCamera().getLocation().clone().setY(spatial.getLocalTranslation().getY())).normalizeLocal().mult(20));

                        if ((angleNormals < slopeLimitAngle  && physicsClosestTets != null) || !physSp.isActive()) {
                physSp.setLinearVelocity(walkDirection.mult(moveSpeed).setY(physSp.getLinearVelocity().getY()));
//                System.out.println(physicsClosestTets.getHitNormalLocal());
            } else {
                physSp.applyCentralForce((walkDirection.mult(moveSlopeSpeed).setY(0f)));
            }
            isMoving = true;

        } else {
            if (isMoving) {
                if (physSp.isActive() && stopTimer < 60 && physicsClosestTets != null) {
                    physSp.setLinearVelocity(physSp.getLinearVelocity().multLocal(new Vector3f(stopDamping, 1, stopDamping)));
                    stopTimer += 1;
                } else {
                    stopTimer = 0;
                    isMoving = false;
                }
            }
        }

        if (doJump) {
            if ((physicsClosestTets != null && angleNormals < slopeLimitAngle) || !physSp.isActive()) {
                physSp.setLinearVelocity(physSp.getLinearVelocity().addLocal(Vector3f.UNIT_Y.mult(jumpSpeed)));
            }
            doJump = false;
        }

//        physSp.setPhysicsRotation(new Quaternion().lookAt(app.getCamera().getDirection().angleBetween(walkDirection), Vector3f.UNIT_Y));
    }

    public void physicsTick(PhysicsSpace space, float tpf) {
        physicsClosestTets = null;
        angleNormals = 0f;
        float closestFraction = 10f;

        if (physSp.isActive()) {
            List<PhysicsRayTestResult> results = space.rayTest(physSp.getPhysicsLocation().add(Vector3f.UNIT_Y.mult(-0.9f * collisionShapeHeight)), 
                    physSp.getPhysicsLocation().add(Vector3f.UNIT_Y.mult(-1.3f * collisionShapeHeight)));
            for (PhysicsRayTestResult physicsRayTestResult : results) {

                if (physicsRayTestResult.getHitFraction() < closestFraction && !physicsRayTestResult.getCollisionObject().getUserObject().equals(spatial)
                        && physicsRayTestResult.getCollisionObject() instanceof GhostControl == false) {
                    physicsClosestTets = physicsRayTestResult;
                }
            }
        }
    }
    
    // DESTROY METHOD
    public void destroy() {
        app = null;
        physicsClosestTets = null;
        walkDirection = null;
        physSp.getPhysicsSpace().removeTickListener(this);
        spatial.removeControl(physSp);
        physSp.getPhysicsSpace().remove(physSp);
        spatial.removeControl(this);
        physSp = null;
    }
    

    public Vector3f getWalkDirection() {
        return walkDirection;
    }

    public void setWalkDirection(Vector3f walkDirection) {
        this.walkDirection = walkDirection;
    }

    public void setNewRotation(Quaternion newRotation) {
        this.newRotation = newRotation;
    }

    public boolean isDoMove() {
        return doMove;
    }

    public void setMove(boolean doMove) {
        this.doMove = doMove;
    }

    public void setJump() {
        this.doJump = true;
    }

    public RigidBodyControl getRigidBody() {
        return physSp;
    }

    public float getJumpSpeed() {
        return jumpSpeed;
    }

    public void setJumpSpeed(float jumpSpeed) {
        this.jumpSpeed = jumpSpeed;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public float getMoveSlopeSpeed() {
        return moveSlopeSpeed;
    }

    public void setMoveSlopeSpeed(float moveSlopeSpeed) {
        this.moveSlopeSpeed = moveSlopeSpeed;
    }

    public float getSlopeLimitAngle() {
        return slopeLimitAngle;
    }

    public void setSlopeLimitAngle(float slopeLimitAngle) {
        this.slopeLimitAngle = slopeLimitAngle;
    }

    public float getStopDamping() {
        return stopDamping;
    }

    public void setStopDamping(float stopDamping) {
        this.stopDamping = stopDamping;
    }
    
    
}
