
package com.entitysystem;

import java.util.concurrent.ConcurrentHashMap;


public final class EntityManager {

    private static long idx = 0;

    private static ConcurrentHashMap <Long, ComponentsControl> componentControl = new ConcurrentHashMap <Long, ComponentsControl>();

    public static long createEntity() {
        idx++;
        return idx;
    }

    public ComponentsControl addComponentControl(long ID) {
        ComponentsControl component = new ComponentsControl(ID, this);
        componentControl.put(ID, component);
        return component;
    }

    public ComponentsControl getComponentControl(long ID) {
        return componentControl.get(ID);
    }        

    private void removeComponentControl(long ID) {
        componentControl.get(ID).clearComponents();
        componentControl.remove(ID);
    }        
    
    
    
    public void removeEntity(long ID) {
        // remove entity
        removeComponentControl(ID);
    }

    
}
