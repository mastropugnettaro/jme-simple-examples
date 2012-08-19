
package entitysystem;

import com.jme3.scene.Spatial;
import java.util.HashMap;


public final class EntityManager {

    private static long idx = 0;

    private static HashMap <Long, ComponentsControl> componentControl = new HashMap<Long, ComponentsControl>();
    private static HashMap <Long, SpatialControl> spatialControl = new HashMap<Long, SpatialControl>();

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
    
    public SpatialControl addSpatialControl(Spatial sp, long ID) {
         SpatialControl spControl = new SpatialControl(sp, ID, this);
         spatialControl.put(ID, spControl);
         return spControl;
    }    
    
    public SpatialControl getSpatialControl(long ID) {
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
