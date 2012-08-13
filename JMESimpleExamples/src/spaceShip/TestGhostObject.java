/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package spaceShip;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.util.List;

/**
 *
 * @author tim8dev [at] gmail [dot com]
 */
public class TestGhostObject extends SimpleApplication {

    private BulletAppState bulletAppState;
    private GhostControl ghostControl;
    private Geometry geo;

    public static void main(String[] args) {
        Application app = new TestGhostObject();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().enableDebug(assetManager);

        // Mesh to be shared across several boxes.
        Sphere sph = new Sphere(10, 10, 1f);
        geo = new Geometry("geo", sph);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        geo.setMaterial(mat);
        geo.setLocalTranslation(0,2,1);
        
        rootNode.attachChild(geo);
        
        // CollisionShape to be shared across several boxes.
        CollisionShape shape = new BoxCollisionShape(new Vector3f(1, 1, 1));
        ghostControl = new GhostControl(shape);
        geo.addControl(ghostControl);
        ghostControl.setCollisionGroup(0);
        bulletAppState.getPhysicsSpace().add(ghostControl);
        
        Geometry geo2 = geo.clone(false);
        geo2.setName("geo2");
        geo2.setLocalTranslation(geo2.getLocalTranslation().add(new Vector3f(0.7f,0.7f,0.7f)));
        CollisionShape shape2 = new BoxCollisionShape(new Vector3f(1f, 1f, 1f));
        GhostControl ghost2 = new GhostControl(shape2);
        geo2.addControl(ghost2);
        bulletAppState.getPhysicsSpace().add(ghost2);
        ghost2.setCollisionGroup(1);
        rootNode.attachChild(geo2);

        bulletAppState.getPhysicsSpace().addCollisionListener(listener);
//        ghost2.getPhysicsSpace().addTickListener(tick);
        
    }
    
//    PhysicsTickListener tick = new PhysicsTickListener() {
//
//        public void prePhysicsTick(PhysicsSpace space, float f) {
//            geo.rotate(0f, 0f, -00.1f);
//            rootNode.updateGeometricState();
//        }
//
//        public void physicsTick(PhysicsSpace space, float f) {
//        
//        List<PhysicsCollisionObject> count = ghostControl.getOverlappingObjects();
//        
//        if (count.size() > 0 && count != null) {
//            PhysicsCollisionObject x = ghostControl.getOverlapping(0);
//            Geometry getGeo = (Geometry) x.getUserObject();
//         System.out.println(getGeo.getName());
//        }      
//        }
//    };

    PhysicsCollisionListener listener = new PhysicsCollisionListener() {

        public void collision(PhysicsCollisionEvent event) {
            
            Spatial sp = event.getNodeA();
            System.out.println(sp);
            
        List<PhysicsCollisionObject> count = ghostControl.getOverlappingObjects();        
        if (count.size() > 0 && count != null) {
            PhysicsCollisionObject x = ghostControl.getOverlapping(0);
            Geometry getGeo = (Geometry) x.getUserObject();
//         System.out.println(getGeo.getName());
        } 
       }
    };
    
    @Override
    public void simpleUpdate(float tpf) {
       geo.rotate(0f, 0f, -00.1f);

    }
}
