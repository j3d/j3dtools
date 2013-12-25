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
// None

/**
 * Devices which map data to trackers.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface TrackerDevice extends ControllerDevice
{
    /**
     * Get the number of trackers.
     *
     * @return The number of trackers.  This cannot change after startup.
     */
    public int getTrackerCount();

    /**
     * Get the trackers of this device.
     *
     * @return The trackers.  This cannot changed after startup.
     */
    public Tracker[] getTrackers();
}
