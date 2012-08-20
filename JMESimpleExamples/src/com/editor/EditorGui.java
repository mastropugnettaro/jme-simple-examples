/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.editor;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;




/**
 *
 * @author mifth
 */
public class EditorGui extends AbstractAppState implements ScreenController {
    
    private Screen screen;
    private Nifty  nifty;
    private SimpleApplication application;
    
    public EditorGui() {
    
       
    
}

   @Override
    public void initialize(AppStateManager stateManager, Application app) {

    super.initialize(stateManager, app);
    application=(SimpleApplication)app;
    
      
       
     NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(application.getAssetManager(), 
                                        application.getInputManager(),
                                        application.getAudioRenderer(),
                                        application.getGuiViewPort());
      nifty = niftyDisplay.getNifty();
//     nifty.loadStyleFile("nifty-default-styles.xml");
//     nifty.loadControlFile("nifty-default-controls.xml");        
     nifty.fromXml("Interface/basicGui.xml",  "start", this);


     // attach the nifty display to the gui view port as a processor
     application.getGuiViewPort().addProcessor(niftyDisplay);
     application.getInputManager().setCursorVisible(true);     
     
     nifty.gotoScreen("start"); // start the screen 
     screen.getFocusHandler().resetFocusElements();
     

     
//    Element niftyElement = nifty.getCurrentScreen().findElementByName("button1");
//    niftyElement.getElementInteraction().getPrimary().setOnClickMethod(new NiftyMethodInvoker(nifty, "printGo()", this));
     
     
    // Set Logger for only warnings     
    Logger root = Logger.getLogger("");
    Handler[] handlers = root.getHandlers();
    for (int i = 0; i < handlers.length; i++) {
        if (handlers[i] instanceof ConsoleHandler) {
        ((ConsoleHandler) handlers[i]).setLevel(Level.WARNING);
      }
     }     
     
    }

    public void printGo() {
        System.out.println("XXXXXX");
        
        application.getViewPort().setBackgroundColor(ColorRGBA.randomColor());
        screen.getFocusHandler().resetFocusElements();
//        application.update();
    }   
   
   
    @Override
    public void update(float tpf) {
        //TODO: implement behavior during runtime
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
        //this is called on the OpenGL thread after the AppState has been detached
    }    
    
    
    
    
    public void bind(Nifty nifty, Screen screen) {
    this.nifty = nifty;
    this.screen = screen;
    }

    public void onStartScreen() {
        
    }

    public void onEndScreen() {

    }
    
    
    
}
