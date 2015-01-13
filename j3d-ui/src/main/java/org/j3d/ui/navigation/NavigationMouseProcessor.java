/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.ui.navigation;

// External imports
import java.awt.Component;
import java.awt.event.MouseEvent;

// Local imports
// none

/**
 * Abstract interface to take mouse events and process them for a navigation
 * response.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface NavigationMouseProcessor
{
    /**
     * Set the ability to use a given state within the handler for a
     * specific mouse button (up to 3). This allows the caller to control
     * exactly what states are allowed to be used and with which buttons.
     * Note that it is quite legal to set all three buttons to the same
     * navigation state
     *
     * @param button The mouse button value from
     *    {@link java.awt.event.MouseEvent}
     * @param state The navigation state to use for that button
     */
    public void setButtonNavigation(int button, int state);

    /**
     * Set the listener for navigation state change notifications. By setting
     * a value of null it will clear the currently set instance
     *
     * @param l The listener to use for change updates
     */
    public void setNavigationStateListener(NavigationStateListener l);

    /**
     * Process a mouse press and set the behavior running. This will cause the
     * navigation state to be set depending on the mouse button pressed.
     *
     * @param evt The event that caused this method to be called
     */
    public void mousePressed(MouseEvent evt);

    /**
     * Process a mouse drag event to change the current movement value from
     * the previously set value to the new value
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDragged(MouseEvent evt);

    /**
     * Process a mouse release to return all the values back to normal. This
     * places all of the transforms back to identity and sets it as though the
     * nothing had happened.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt);
}
