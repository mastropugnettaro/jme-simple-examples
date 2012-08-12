/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spaceShip;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;

/**
 *
 * @author mifth
 */
public class Rocket {
    
    public Rocket() {
        
    }
    
    
    PhysicsCollisionListener listener = new PhysicsCollisionListener() {

        public void collision(PhysicsCollisionEvent event) {

       }
    };    
    
}
