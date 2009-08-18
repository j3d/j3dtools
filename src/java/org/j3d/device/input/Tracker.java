/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
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
 * An abstract representation of a tracker.  A tracker is a sensor
 * on a device. A device may contain many trackers.
 * <p>
 *
 * With the introduction of layers and compositing, tracker state in the
 * world coordinates is dependent on the layer that is being processed. For
 * each layer, the device will have a different set of world coordinates.
 * Within a given rendered frame a tracker may be called multiple times; once
 * for each layer/sublayer combo and asked for the state information relative
 * to that layer.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public abstract class Tracker
{

    /** This sensor only issues button events */
    public final static int MASK_NONE = 0;

    /** This sensor can pick items in the scene */
    public final static int MASK_PICKING = 2;

    /** This sensor can change position in the scene */
    public final static int MASK_POSITION = 4;

    /** This sensor can change orientation in the scene */
    public final static int MASK_ORIENTATION = 8;

    /**
     * Can the action type of this sensor change over time?
     *
     * @return Is this an action figure?
     */
    public boolean actionChanges()
    {
        return false;
    }

    /**
     * What action types does this sensor return.  This a combination
     * of ACTION masks.
     *
     * @return The action mask.
     */
    public abstract int getActionMask();

    /**
     * Notification that tracker polling is beginning.
     */
    public abstract void beginPolling();

    /**
     * Notification that tracker polling is ending.
     */
    public abstract void endPolling();

    /**
     * Get the current state of this tracker.
     *
     * @param layer The ID of the layer to get the state for
     * @param subLayer The ID of the sub layer within the parent layer
     * @param state The current state
     */
    public abstract void getState(int layer, int subLayer, TrackerState state);
}
