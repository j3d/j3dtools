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
 * An input device.  Input devices are further classified as Midi, Tracker,
 * or Controller.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface InputDevice
{
    /** Function constants */
    public static final int FUNC_NONE = -1;
    public static final int FUNC_THROTTLE = 0;
    public static final int FUNC_NAV_X = 1;
    public static final int FUNC_NAV_Y = 2;
    public static final int FUNC_ORIENT_X = 3;
    public static final int FUNC_ORIENT_Y = 4;
    public static final int FUNC_NAV_Y_CENTERED = 5;

    public static final int FUNC_LEFT_STICK_X = 6;
    public static final int FUNC_LEFT_STICK_Y = 7;
    public static final int FUNC_LEFT_STICK_Z = 19;
    public static final int FUNC_RIGHT_STICK_X = 8;
    public static final int FUNC_RIGHT_STICK_Y = 9;
    public static final int FUNC_LEFT_HAT = 10;
    public static final int FUNC_L1_BUTTON = 11;
    public static final int FUNC_R1_BUTTON = 12;
    public static final int FUNC_START_BUTTON = 13;
    public static final int FUNC_WHEEL_X = 14;
    public static final int FUNC_BUTTON_1 = 15;
    public static final int FUNC_BUTTON_2 = 16;
    public static final int FUNC_BUTTON_3 = 17;
    public static final int FUNC_BUTTON_4 = 18;

    public static final int FUNC_VIEWPOINT_NEXT = 50;

    /**
     * Get the name of this device.  Names are of the form class-#.  Valid
     * classes are Gamepad, Joystick, Wheel, Midi, GenericHID.
     *
     * @return The name
     */
     public String getName();
}
