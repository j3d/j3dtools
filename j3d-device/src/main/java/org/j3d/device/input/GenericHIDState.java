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

    // ----- Methods defined by DeviceState ----------------------------------

    @Override
    public void clearChanged()
    {
        for(int i = 0; i < changeValue.length; i++)
        {
            changeValue[i] = false;
        }
    }

    // ----- Local Methods ---------------------------------------------------

    /**
     * Allocate a number of axis items. Replaces the existing assignments
     * with new values that have defaulted to 0 and unchanged.
     *
     * @param count A number that must be non-negative
     */
    public void allocateAxes(int count)
    {
        if(count < 0)
        {
            throw new IllegalArgumentException("Count must be 0 or greater");
        }

        axisValue = new float[count];
        changeValue = new boolean[count];
    }
}
