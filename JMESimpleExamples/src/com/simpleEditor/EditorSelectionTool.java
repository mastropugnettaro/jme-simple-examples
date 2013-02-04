/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpleEditor;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

/**
 *
 * @author mifth
 */
public class EditorSelectionTool {
    
    private EditorBaseParts baseParts;
    
    public EditorSelectionTool(EditorBaseParts baseParts) {
        
        this.baseParts = baseParts;
            
    }
    
    
    protected CollisionResult pickSelection(Camera cam, Vector2f mouseLoc, Node node) {
        CollisionResults results = new CollisionResults();
        Ray ray = new Ray();
        Vector3f pos = cam.getWorldCoordinates(mouseLoc, 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(mouseLoc, 0.1f).clone();
        dir.subtractLocal(pos).normalizeLocal();
        ray.setOrigin(pos);
        ray.setDirection(dir);
        node.collideWith(ray, results);
        CollisionResult result = results.getClosestCollision();
        
        if (results.size() > 0) {
            String geoName = result.getGeometry().getName();
        }
        else System.out.println("NONE");
        
        return result;
    }    
    
}
