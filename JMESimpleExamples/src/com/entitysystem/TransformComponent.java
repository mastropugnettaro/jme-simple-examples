/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.entitysystem;

import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;

/**
 *
 * @author mifth
 */
public final class TransformComponent {

    private Transform transform;
    private Vector3f location, scale;
    private Quaternion rotation;

    public TransformComponent(Transform trans) {
        this.transform = trans;
        this.location = trans.getTranslation();
        this.rotation = trans.getRotation();
        this.scale = trans.getScale();
    }

    public Transform getTransform() {
        return transform;
    }
    
    public Vector3f getLocation() {
        return location;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public Vector3f getScale() {
        return scale;
    }    

}