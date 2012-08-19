/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entitysystem;

import com.jme3.scene.Spatial;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mifth
 */


public class EntitySpatials {
    
    private static ConcurrentHashMap <Long, EntitySpatialsControl> spatialControl = new ConcurrentHashMap <Long, EntitySpatialsControl>();    

    public EntitySpatials() {
        
    }
    
    public EntitySpatialsControl addSpatialControl(Spatial sp, long ID) {
         EntitySpatialsControl spControl = new EntitySpatialsControl(sp, ID);
         spatialControl.put(ID, spControl);
         return spControl;
    }    
    
    public EntitySpatialsControl getSpatialControl(long ID) {
        return spatialControl.get(ID);
    }    

    public void removeSpatialControl(long ID) {
        spatialControl.get(ID).destroy();
        spatialControl.remove(ID);
    }    
    
}
