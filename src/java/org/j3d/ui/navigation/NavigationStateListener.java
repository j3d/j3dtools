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

// Standard imports
// none

// Application specific imports
// none

/**
 * A listener interface used to communicate changes in the navigation state
 * from one handler to another.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface NavigationStateListener
{
    /** The navigation state is Walking */
    public static int WALK_STATE = 1;

    /** The navigation state is Tilt */
    public static int TILT_STATE = 2;

    /** The navigation state is Panning */
    public static int PAN_STATE = 3;

    /** The navigation state is Flying */
    public static int FLY_STATE = 4;

    /** The navigation state is Examine */
    public static int EXAMINE_STATE = 5;

    /** The navigation state is such that there is no navigation */
    public static int NO_STATE = 0;

    /**
     * Notification that the panning state has changed to the new state.
     *
     * @param state One of the state values declared here
     */
    public void setNavigationState(int state);

    /**
     * Callback to ask the listener what navigation state it thinks it is
     * in.
     *
     * @return The state that the listener thinks it is in
     */
    public int getNavigationState();
}
