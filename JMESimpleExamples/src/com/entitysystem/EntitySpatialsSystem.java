/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.entitysystem;

import com.entitysystem.ComponentsControl;
import com.jme3.scene.Spatial;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mifth
 */


public final class EntitySpatialsSystem {
    
    private static ConcurrentHashMap <Long, EntitySpatialsControl_2> spatialControl = new ConcurrentHashMap <Long, EntitySpatialsControl_2>();    

    public EntitySpatialsSystem() {
        
    }
    
    public static EntitySpatialsControl_2 addSpatialControl(Spatial sp, long ID, ComponentsControl control) {
         EntitySpatialsControl_2 spControl = new EntitySpatialsControl_2(sp, ID, control);
         spatialControl.put(ID, spControl);
         return spControl;
    }    
    
    public static EntitySpatialsControl_2 getSpatialControl(long ID) {
        return spatialControl.get(ID);
    }    

    public static void removeSpatialControl(long ID) {
        spatialControl.get(ID).destroy();
        spatialControl.remove(ID);
    }    
    
}
