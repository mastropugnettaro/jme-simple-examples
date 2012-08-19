/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entitysystem;

import java.util.HashMap;

/**
 *
 * @author mifth
 */
public class ComponentControl {
    
    private static HashMap <Class<?>, Object> components = new HashMap<Class<?>, Object>();        
    private long ID;
    private EntityManager entityManager;
    
    public ComponentControl(long ID, EntityManager entityManager) {
        this.ID = ID;
    }
    
    public long getEntityID(ComponentControl compControl) {
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
