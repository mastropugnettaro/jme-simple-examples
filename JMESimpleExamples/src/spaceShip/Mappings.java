/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spaceShip;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.scene.Node;




public class Mappings {

    private SimpleApplication asm;
    private Node ship;
    private ShipPhysicsControl shipControl;
    private ShipWeaponControl weaponControl;  
    private InputManager inputManager;
    
    public Mappings (SimpleApplication asm, Node ship) {
        
        
        this.asm = asm;
        this.ship = ship;
        shipControl = this.ship.getControl(ShipPhysicsControl.class);
        weaponControl = this.ship.getControl(ShipWeaponControl.class);
        setupKeys(asm.getInputManager());
        
    }
    
    
    
    private void setupKeys(InputManager inputManager){
        this.inputManager = inputManager;
     
       //Set up keys and listener to read it
        String[] mappings = new String[]{
            "MoveShip",
            "FireBullets",
            "FireRocket"
        };
        
        InputManager input = asm.getInputManager();
        
        input.addMapping("MoveShip", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        input.addMapping("FireBullets", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        input.addListener(anl, mappings);
        input.addListener(acl, mappings);
    }
    
    
    
    AnalogListener anl = new AnalogListener() {
      public void onAnalog(String name, float value, float tpf) {
 
      }    
    };
    
    ActionListener acl = new ActionListener() {
      public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed && "MoveShip".equals(name)) {
            shipControl.makeMove(true);
        } else if (!isPressed && "MoveShip".equals(name)) {
            shipControl.makeMove(false);
        }
        
        if (isPressed && name.equals("FireBullets")) {
            weaponControl.setFireBullets(true);
//          Bullet shipbullets = new Bullet(aim, bullet.clone(false));
        } else if (!isPressed && name.equals("FireBullets")) {
            weaponControl.setFireBullets(false);
        }  
      }            
    };

    
    
}
