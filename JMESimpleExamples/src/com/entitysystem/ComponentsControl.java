/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.entitysystem;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mifth
 */
public class ComponentsControl {
    
    private static ConcurrentHashMap <Class<?>, Object> components = new ConcurrentHashMap <Class<?>, Object>();        
    private long ID;
    private EntityManager entityManager;
    
    public ComponentsControl(long ID, EntityManager entityManager) {
        this.ID = ID;
    }
    
    public long getEntityID(ComponentsControl compControl) {
        return ID;
    }
    
    public Object getComponent(Class controlType) {
        return components.get(controlType);
    }

    public void setComponent(Object comp) {
        if (components.get(comp.getClass()) == null) components.put(comp.getClass(), comp);
    }
    
    public void clearComponent(Class componentType){
        components.remove(componentType);
    }
    
    public void clearComponents() {
        components.clear();
    }    
    
    
}
