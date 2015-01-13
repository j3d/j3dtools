/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
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
 * Holds a single Joystick state.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class JoystickState extends GenericHIDState
{
    /** The stickX value */
    public float stickX;

    /** The stickY value */
    public float stickY;

    /** The stickY value */
    public float stickZ;

    /** The hatX value */
    public float hatX;

    /** The hatY value */
    public float hatY;

    /** The throttleSlider value */
    public float throttleSlider;

    /** Did the throttleSlider value change */
    public boolean throttleSlider_changed;

    /** The triggerButton value */
    public boolean triggerButton;

    /** Did triggerButton value change */
    public boolean triggerButton_changed;

    /** Did the hatX value change */
    public boolean hatX_changed;

    /** Did the hatY value change */
    public boolean hatY_changed;

    /** Did the stickX value change */
    public boolean stickX_changed;

    /** Did the stickY value change */
    public boolean stickY_changed;

    /** Did the stickZ value change */
    public boolean stickZ_changed;

    /**
     * Clear changed flags.
     */
    @Override
    public void clearChanged()
    {
        triggerButton_changed = false;
        hatX_changed = false;
        hatY_changed = false;
        stickX_changed = false;
        stickY_changed = false;
        stickZ_changed = false;
        throttleSlider_changed = false;
    }

}
