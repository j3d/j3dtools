/*****************************************************************************
 *                        J3D.org Copyright (c) 2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.j3d.renderer.java3d.device.output.elumens;

// Standard imports
// Application specific imports

/**
 * Notifies code when its safe to update the scenegraph.  No scenegraph
 * changes should be made except during the update method.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public interface SGUpdater {
   /**
    * Notification that it is safe to update the scenegraph.
    */
   public void update();
}