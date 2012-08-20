/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.editor;



/**
 *
 * @author mifth
 */
public class EditorTopTool extends Editor{
    
        private EditorTool editTool;
        
        
        public EditorTopTool () {
            
        }
        
        
    private void doEnableEditTool(EditorTool sceneEditTool) {
        if (editTool != null) {
            editTool.hideMarker();
        }
        editTool = sceneEditTool;

    }        
        
}
