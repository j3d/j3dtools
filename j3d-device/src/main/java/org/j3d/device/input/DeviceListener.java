/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.device.input;

// External imports
// None

// Local imports

/**
 * Notifies listeners of device additions and removals.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface DeviceListener
{
    /**
     * A new device has been added to a manager.
     *
     * @param device The new device.
     */
    public void deviceAdded(InputDevice device);

    /**
     * A device has been removed.
     *
     * @param device The device removed.
     */
    public void deviceRemoved(InputDevice device);
}
