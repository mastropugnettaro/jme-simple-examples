
package entitysystem;

import java.util.HashMap;

public final class Entity {

    private static long id;
    private static HashMap <Class<?>, Object> components = new HashMap<Class<?>, Object>();
    
    
    public Entity(long id) {
        this.id = id;
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

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Entity) {
            Entity entity = (Entity) o;
            return entity.getId() == id;
        }
        return super.equals(o);
    }
}
