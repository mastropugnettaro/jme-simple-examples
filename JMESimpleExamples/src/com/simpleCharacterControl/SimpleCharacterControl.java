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
    private boolean doMove, doJump, hasJumped = false;
    private Vector3f walkDirection = Vector3f.ZERO;
    private Vector3f additiveJumpSpeed = Vector3f.ZERO;
    private Quaternion newRotation;
    private int stopTimer = 0;
    private int jumpTimer = 0;
    private boolean hasMoved = false;
    private float angleNormals = 0;
    private PhysicsRayTestResult physicsClosestTets;
    private RigidBodyControl rigidBody;
    private float jumpSpeedY, moveSpeed, moveSpeedMultiplier, moveSlopeSpeed, slopeLimitAngle, stopDamping, centerToBottomHeight;

    public SimpleCharacterControl(Application app, RigidBodyControl rigidBody, float centerToBottomHeight) {
        this.app = app;
        this.rigidBody = rigidBody;

        jumpSpeedY = 40f;
        moveSpeed = 0.5f;
        moveSpeedMultiplier = 1;
        moveSlopeSpeed = 0.3f;
        slopeLimitAngle = FastMath.DEG_TO_RAD * 45f;
        stopDamping = 0.8f;
        this.centerToBottomHeight = centerToBottomHeight;

        this.rigidBody.getPhysicsSpace().addTickListener(this);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (newRotation != null) {
            spatial.setLocalRotation(newRotation);
//            newRotation = null;
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void prePhysicsTick(PhysicsSpace space, float tpf) {

        if (physicsClosestTets != null) {
            angleNormals = physicsClosestTets.getHitNormalLocal().normalizeLocal().angleBetween(Vector3f.UNIT_Y);

        }

        if (angleNormals < slopeLimitAngle && physicsClosestTets != null && (!doMove && !doJump && !hasJumped)) {
            rigidBody.setFriction(7f);
        } else {
            rigidBody.setFriction(0.3f);
        }

        if (doMove) {

            if ((angleNormals < slopeLimitAngle && physicsClosestTets != null) || !rigidBody.isActive()) {
                rigidBody.setLinearVelocity(walkDirection.mult(moveSpeed * moveSpeedMultiplier).setY(rigidBody.getLinearVelocity().getY()));
//                System.out.println(physicsClosestTets.getHitNormalLocal());
            } else if (angleNormals > slopeLimitAngle && angleNormals < FastMath.DEG_TO_RAD * 80f && physicsClosestTets != null) {
                rigidBody.applyCentralForce((walkDirection.mult(moveSlopeSpeed).setY(0f)));
                //   rigidBody.setLinearVelocity(walkDirection.mult(moveSpeed * moveSpeedMultiplier * 0.5f).setY(rigidBody.getLinearVelocity().getY()));
            } else {
//                physSp.applyCentralForce((walkDirection.mult(moveSlopeSpeed).setY(0f)));
                rigidBody.setLinearVelocity(walkDirection.mult(moveSpeed * moveSpeedMultiplier * 0.5f).setY(rigidBody.getLinearVelocity().getY()));
            }
            hasMoved = true;
            hasJumped = false;
            stopTimer = 0;
            jumpTimer = 0;

        }

        if (jumpTimer > 0) {
            if (jumpTimer > 10) {
                jumpTimer = 0;
            } else {
                jumpTimer++;
            }
        }

        if (doJump && !hasJumped && (physicsClosestTets != null || !rigidBody.isActive())) {
            if ((angleNormals < slopeLimitAngle)) {
//                rigidBody.clearForces();
                rigidBody.setLinearVelocity(rigidBody.getLinearVelocity().add(Vector3f.UNIT_Y.clone().multLocal(jumpSpeedY).addLocal(additiveJumpSpeed)));
//                physSp.applyImpulse(Vector3f.UNIT_Y.mult(jumpSpeed), Vector3f.ZERO);
                hasJumped = true;
                jumpTimer = 1;
            }
        }


        // Stop the char
        if ((hasMoved || hasJumped) && physicsClosestTets != null && angleNormals < slopeLimitAngle && !doMove) {

            if (hasJumped && hasMoved) {
                hasJumped = false;
                jumpTimer = 0;
            }

            if (stopTimer < 30 && jumpTimer == 0) {
//                rigidBody.setLinearDamping(1f);
//                rigidBody.setFriction(10f);
                rigidBody.setLinearVelocity(rigidBody.getLinearVelocity().multLocal(new Vector3f(stopDamping, 1, stopDamping)));
                stopTimer += 1;
            } else {
                if (jumpTimer == 0) {
//                    rigidBody.setLinearDamping(0.5f);
//                    rigidBody.setFriction(0.3f);
                    stopTimer = 0;
                    hasMoved = false;
                    hasJumped = false;
                    jumpTimer = 0;
                }

            }
        } 
//        else {
//            rigidBody.setLinearDamping(0.5f);
//            rigidBody.setFriction(0.3f);
//        }

        if (doJump) {
            doJump = false; // set it after damping
        }
    }

    public void physicsTick(PhysicsSpace space, float tpf) {
        physicsClosestTets = null;
        angleNormals = 0f;
        float closestFraction = centerToBottomHeight * 10f;

        if (rigidBody.isActive()) {
            List<PhysicsRayTestResult> results = space.rayTest(rigidBody.getPhysicsLocation().add(Vector3f.UNIT_Y.mult(-0.8f * centerToBottomHeight)),
                    rigidBody.getPhysicsLocation().add(Vector3f.UNIT_Y.mult(-1.3f * centerToBottomHeight)));
            for (PhysicsRayTestResult physicsRayTestResult : results) {

                if (physicsRayTestResult.getHitFraction() < closestFraction && !physicsRayTestResult.getCollisionObject().getUserObject().equals(spatial)
                        && physicsRayTestResult.getCollisionObject() instanceof GhostControl == false) {
                    physicsClosestTets = physicsRayTestResult;
                    closestFraction = physicsRayTestResult.getHitFraction();
                }
            }
        }
    }

    // DESTROY METHOD
    public void destroy() {
        physicsClosestTets = null;
        walkDirection = null;
        app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().removeTickListener(this);
        spatial.removeControl(rigidBody);
        app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(rigidBody);
        spatial.removeControl(this);
        rigidBody = null;
        app = null;
    }

    public Vector3f getWalkDirection() {
        return walkDirection;
    }

    public void setWalkDirection(Vector3f walkDirection) {
        this.walkDirection = walkDirection;
    }

    public void setRotationInUpdate(Quaternion newRotation) {
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
        return rigidBody;
    }

    public float getJumpSpeed() {
        return jumpSpeedY;
    }

    public void setJumpSpeed(float jumpSpeed) {
        this.jumpSpeedY = jumpSpeed;
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

    public Vector3f getAdditiveJumpSpeed() {
        return additiveJumpSpeed;
    }

    public void setAdditiveJumpSpeed(Vector3f additiveJumpSpeed) {
        this.additiveJumpSpeed = additiveJumpSpeed;
    }

    public float getMoveSpeedMultiplier() {
        return moveSpeedMultiplier;
    }

    public void setMoveSpeedMultiplier(float moveSpeedMultiplier) {
        this.moveSpeedMultiplier = moveSpeedMultiplier;
    }
}
