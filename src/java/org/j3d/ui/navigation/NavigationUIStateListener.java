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
 * A listener interface used to communicate changes that should occur in the
 * navigation user interface.
 * <p>
 *
 * This listener extends the basic listener to add information that the user
 * interface will find necessary to reflect the internal state. For some
 * applications, they may only want to enable one particular navigation type
 * and disable all others. This standard state listener will not allow them
 * to recieve this information, thus the need for a more detailed listener for
 * pure UI information.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface NavigationUIStateListener extends NavigationStateListener
{
    /**
     * Send a message to enable or disable the user interface ability to
     * change the current navigation state.
     *
     * @param enable true Enable the user interface to change states
     */
    public void enableNavStateSelect(boolean enable);
}
