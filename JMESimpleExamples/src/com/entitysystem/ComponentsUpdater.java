/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.entitysystem;

import com.jme3.math.Transform;

/**
 *
 * @author mifth
 */
public class ComponentsUpdater {
    
    ComponentsControl components;
    private boolean doUpdate = false;
    
    public ComponentsUpdater(ComponentsControl components) {
        
        this.components = components;
        UpdateStateComponent x = (UpdateStateComponent) components.getComponent(UpdateStateComponent.class);
        if (x.getUpdate() == true) doUpdate = true;
        
    }
    
    public Transform getUpdateTransform() {
        Transform transform = null;
        
        if (doUpdate == true) {
            TransformComponent tr = (TransformComponent) components.getComponent(TransformComponent.class);
          if (tr != null) {
            transform = tr.getTransform();
          }
         }
//        System.out.println(transform);
        return transform;
    }
    
    public boolean getDoUpdate() {
        return doUpdate;
    }
    
}
