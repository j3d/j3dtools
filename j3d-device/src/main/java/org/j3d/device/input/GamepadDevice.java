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
 * Devices which map data to gamepads.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface GamepadDevice extends ControllerDevice
{
    /**
     * Get the current state of this device.  Any arrays too small will be
     * resized.
     *
     * @param state The state structure to fill in.
     */
    public void getState(GamepadState state);

    /**
     * Set the rumblerX axis.
     *
     * @param val The rumbler strength, from -1 to 1.
     */
    public void setRumblerX(float val);

    /**
     * Set the rumblerY axis.
     *
     * @param val The rumbler strength, from -1 to 1.
     */
    public void setRumblerY(float val);
}
