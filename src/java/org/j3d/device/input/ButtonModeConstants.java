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
 * Button action constants.  Allows a tracker to specify what each
 * button can be used for.
 *
 * Picking is modal whereas all others are specific modes.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class ButtonModeConstants
{
    public static final int NOTHING = 0;
    public static final int PICKING = 1;

    /** Absolute choice of a mode.  Disallowed modes due to NavInfo will be ignored. */
    public static final int WALK = 2;
    public static final int FLY = 3;
    public static final int EXAMINE = 4;
    public static final int PAN = 5;
    public static final int PAN_COLLISION = 5;
    public static final int TILT = 6;

    /** These specify which slot of the NavigationInfo.type field to use */
    public static final int NAV1 = 10;
    public static final int NAV2 = 11;
    public static final int NAV3 = 12;
    public static final int NAV4 = 13;
    public static final int NAV5 = 14;
    public static final int NAV6 = 15;
    public static final int NAV7 = 16;
    public static final int NAV8 = 17;

    /** Action a button might perform */
    public static final int VIEWPOINT_NEXT = 18;
    public static final int VIEWPOINT_PREV = 19;
    public static final int VIEWPOINT_RESET = 20;
}
