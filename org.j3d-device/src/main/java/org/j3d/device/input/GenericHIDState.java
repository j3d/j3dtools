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
 * Holds a single generic HID state.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class GenericHIDState extends DeviceState
{
    /** The value of all the axes */
    public float[] axisValue;

    /** Which values have changed since last update */
    public boolean[] changeValue;

    public GenericHIDState()
    {
        axisValue = new float[0];
        changeValue = new boolean[0];
    }
}
