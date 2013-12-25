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
 * Holds a single gamepad state.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class WheelState extends GenericHIDState
{
    /** The wheelX value */
    public float wheelX;

    /** The throttleSlider value */
    public float throttleSlider;

    /** The wheelX value */
    public boolean wheelX_changed;

    /** The button1 value */
    public boolean button1;

    /** The button1 value changed */
    public boolean button1_changed;

    /** The button2 value */
    public boolean button2;

    /** The button2 value changed */
    public boolean button2_changed;

    /** The button3 value */
    public boolean button3;

    /** The button3 value changed */
    public boolean button3_changed;

    /** The button4 value */
    public boolean button4;

    /** The button4 value changed */
    public boolean button4_changed;

    /** The throttleSlider value */
    public boolean throttleSlider_changed;

    public WheelState()
    {
    }

    /**
     * Clear changed flags.
     */
    public void clearChanged()
    {
        wheelX_changed = false;
        throttleSlider_changed = false;
        button1_changed = false;
        button2_changed = false;
        button3_changed = false;
        button4_changed = false;
    }
}
