
package com.entitysystem;

import java.util.concurrent.ConcurrentHashMap;


public final class EntityManager {

    private static long idx = 0;

    private static ConcurrentHashMap <Long, ComponentsControl> componentControl = new ConcurrentHashMap <Long, ComponentsControl>();

    public static long createEntity() {
        idx++;
        return idx;
    }

    public static ComponentsControl addComponentControl(long ID) {
       ComponentsControl component = componentControl.get(ID);
        if (component == null) {
            component = new ComponentsControl(ID);
            componentControl.put(ID, component);
        }
        return component;
    }

    public static ComponentsControl getComponentControl(long ID) {
        return componentControl.get(ID);
    }        

    private static void removeComponentControl(long ID) {
        componentControl.get(ID).clearComponents();
        componentControl.remove(ID);
    }        
    
    
    
    public static void  removeEntity(long ID) {
        // remove entity
        removeComponentControl(ID);
    }

    
}
