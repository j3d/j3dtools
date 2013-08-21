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
public class GamepadState extends GenericHIDState
{
    /** The l1Button value */
    public boolean l1Button;

    /** The leftHatX value */
    public float leftHatX;

    /** The leftHatY value */
    public float leftHatY;

    /** The r1Button value */
    public boolean r1Button;

    /** The leftStickX value */
    public float leftStickX;

    /** The leftStickY value */
    public float leftStickY;

    /** The rightStickX value */
    public float rightStickX;

    /** The rightStickY value */
    public float rightStickY;

    /** The startButton value */
    public boolean startButton;

    /** The throttleSlider value */
    public float throttleSlider;

    /** The l1Button value */
    public boolean l1Button_changed;

    /** The leftHatX value */
    public boolean leftHatX_changed;

    /** The leftHatY value */
    public boolean leftHatY_changed;

    /** The r1Button value */
    public boolean r1Button_changed;

    /** The leftStickX value */
    public boolean leftStickX_changed;

    /** The leftStickY value */
    public boolean leftStickY_changed;

    /** The rightStickX value */
    public boolean rightStickX_changed;

    /** The rightStickY value */
    public boolean rightStickY_changed;

    /** The startButton value */
    public boolean startButton_changed;

    /** The throttleSlider value */
    public boolean throttleSlider_changed;

    public GamepadState()
    {
    }

    /**
     * Clear changed flags.
     */
    public void clearChanged()
    {
        l1Button_changed = false;
        r1Button_changed = false;
        startButton_changed = false;
        leftHatX_changed = false;
        leftHatY_changed = false;
        leftStickX_changed = false;
        leftStickY_changed = false;
        rightStickX_changed = false;
        rightStickY_changed = false;
        throttleSlider_changed = false;
    }
}
