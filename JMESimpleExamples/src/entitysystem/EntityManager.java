
package entitysystem;

import com.jme3.scene.Spatial;
import java.util.HashMap;


public final class EntityManager {

    private static long idx = 0;
    //TODO: DB

    private static HashMap <Long, Entity> entities = new HashMap<Long, Entity>();
    private static HashMap <Entity, EntitySpatialControl> spatialControl = new HashMap<Entity, EntitySpatialControl>();

    public static Entity createEntity() {
         Entity ent = new Entity(idx++);
         entities.put(idx, ent);
         return ent;
    }

    public Entity getEntity(long ID) {
        return entities.get(ID);
    }
    
    
    public EntitySpatialControl addSpatialControl(Spatial sp, Entity ent) {
         EntitySpatialControl spControl = new EntitySpatialControl(sp, ent, this);
         spatialControl.put(ent, spControl);
         return spControl;
    }    
    
    public EntitySpatialControl getSpatialControl(Entity ent) {
        return spatialControl.get(ent);
    }    

    public void removeSpatialControl(Entity ent) {
        spatialControl.remove(ent);
    }    
    
    
    public void removeEntity(Entity ent) {
        // remove spatial control
        EntitySpatialControl sp = spatialControl.get(ent);
        removeSpatialControl(ent);
        sp.destroy();
        sp = null;
        
        // remove entity
        entities.remove(ent.getId());
        ent.clearComponents();
        ent = null;
    }
    
}
