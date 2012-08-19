
package entitysystem;

import com.jme3.scene.Spatial;
import java.util.HashMap;


public final class EntityManager {

    private static long idx = 0;
    //TODO: DB

    private static HashMap <Long, ComponentControl> componentControl = new HashMap<Long, ComponentControl>();
    private static HashMap <Long, EntitySpatialControl> spatialControl = new HashMap<Long, EntitySpatialControl>();

    public static long createEntity() {
        idx++;
        return idx;
    }

    public ComponentControl addComponentControl(long ID) {
        ComponentControl component = new ComponentControl(ID, this);
        componentControl.put(ID, component);
        return component;
    }

    public ComponentControl getComponentControl(long ID) {
        return componentControl.get(ID);
    }        

    private void removeComponentControl(long ID) {
        componentControl.get(ID).clearComponents();
        componentControl.remove(ID);
    }        
    
    public EntitySpatialControl addSpatialControl(Spatial sp, long ID) {
         EntitySpatialControl spControl = new EntitySpatialControl(sp, ID, this);
         spatialControl.put(ID, spControl);
         return spControl;
    }    
    
    public EntitySpatialControl getSpatialControl(long ID) {
        return spatialControl.get(ID);
    }    

    private void removeSpatialControl(long ID) {
        spatialControl.get(ID).destroy();
        spatialControl.remove(ID);
    }    
    
    
    public void removeEntity(long ID) {
        // remove spatial control
        removeSpatialControl(ID);
        
        // remove entity
        removeComponentControl(ID);
    }

    
}
