/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.overlay;

// Standard imports
// none

// Application specific imports
// none

/**
 * An extended version of the overlay that would like to have interactive
 * input from the mouse and/or keyboard.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface InteractiveOverlay extends Overlay {
    /**
     * Set the input requestor so that the overlay may manage when it requires
     * input events. If the system is shutting down or the overlay is being
     * removed, the parameter value may be null to clear a previously held
     * instance.
     *
     * @param req The requestor instance to use or null
     */
    public void setInputRequester(InputRequester req);
}
