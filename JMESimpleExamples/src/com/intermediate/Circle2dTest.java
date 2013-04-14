package com.intermediate;

/*You can get transforms from *.blend files and use your models for it. 
 * Blender could be used as a World Editor or scene composer.
 * Names of JME objects and blend objects should be like:
 * JME names - Box, Sphere
 * blend names - Box, Box.000, Box.001, Box.002.... Sphere, Sphere.000, Sphere.001...
 */
import com.jme3.app.SimpleApplication;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.ModelKey;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.Node;
import com.jme3.scene.shape.*;
import java.awt.Color;

public class Circle2dTest extends SimpleApplication {

    public static void main(String[] args) {
        Circle2dTest app = new Circle2dTest();
        app.start();
    }
    Circle2d circle;

    @Override
    public void simpleInitApp() {

        circle = new Circle2d(assetManager, 1, 5, Color.BLUE, 360, Color.red, 45);
        circle.setLocalTranslation(0, 0, 0);
        guiNode.attachChild(circle);

        circle.rotate(90, 0, 0);
        circle.setLocalTranslation(new Vector3f((float) settings.getWidth() - 100f, 50f, 0));
        circle.scale(70);


        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize());
        ch.setText("Circle2d Mesh Example"); // crosshairs
        ch.setColor(new ColorRGBA(1f, 0.8f, 0.1f, 1f));
        ch.setLocalTranslation(settings.getWidth() * 0.3f, settings.getHeight() * 0.1f, 0);
        guiNode.attachChild(ch);



        flyCam.setMoveSpeed(30);
        viewPort.setBackgroundColor(ColorRGBA.Gray);

    }

    @Override
    public void simpleUpdate(float tpf) {
        
//        circle.generateImage();
    }
}
