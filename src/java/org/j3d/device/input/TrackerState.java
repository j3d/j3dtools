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
 * Holds a single tracker's state.
 * <p>
 *
 * A sensor is responsible for converting its coordinates into worldCoordinates
 * Needs to return position, orientation and button state
 * action(Picking, Navigation, Orientation)
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class TrackerState extends DeviceState
{

    /** No type event was processed */
    public final static int TYPE_NONE = 0;

    /** The action type was a press */
    public final static int TYPE_PRESS = 2;

    /** The action type was a drag */
    public final static int TYPE_DRAG = 4;

    /** The action type was a click */
    public final static int TYPE_CLICK = 8;

    /** The action type was a release */
    public final static int TYPE_RELEASE = 16;

    /** The action type was a movement */
    public final static int TYPE_MOVE = 32;

    /** The action type was a orientation change */
    public final static int TYPE_ORIENTATION = 64;

    /** Only a button changed state, nothing else */
    public final static int TYPE_BUTTON = 128;

    /** The action type was a wheel rotation */
    public final static int TYPE_WHEEL = 256;


    /** A mask of actions this tracker might return.  Defined in Tracker */
    public int actionMask;

    /** What action is this state record for */
    public int actionType;

    /** The position of the tracker in device coordinates */
    public float[] devicePos;

    /** An orientation vector in device coords */
    public float[] deviceOri;

    /** The position in world coordinates. */
    public float[] worldPos;

    /** The orientation in world coordinates */
    public float[] worldOri;

    // Maximum 16 buttons
    public boolean[] buttonState;

    /** Is picking enabled for this button */
    public boolean[] pickingEnabled;

    /** What navigation mode should this button use */
    public int[] buttonMode;

    /** The number of buttons the tracker has */
    public int numButtons;

    /** The number of 'clicks' a wheel has rotated by */
    public int wheelClicks;

    /** Shift key modifier */
    public boolean shiftModifier;

    /** Alt key modifier */
    public boolean altModifier;

    /** Ctrl key modifier */
    public boolean ctrlModifier;

    /**
     * Create a new default state. The type is set to NONE and all items are
     * initialised to zero arrays.
     */
    public TrackerState()
    {
        actionType = TYPE_NONE;

        devicePos = new float[3];
        deviceOri = new float[3];
        worldPos = new float[3];
        worldOri = new float[3];

        buttonState = new boolean[16];
        buttonMode = new int[16];
        pickingEnabled = new boolean[16];
        numButtons = 0;
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

    /**
     * Generate a string representation of this tracker's current state.
     *
     * @return A string containing all the state detail
     */
    public String toString()
    {

        StringBuilder buf = new StringBuilder("TrackerState: ");
        buf.append("Type: ");
        switch(actionType)
        {
            case TYPE_NONE:
                buf.append("NONE ");
                break;
            case TYPE_PRESS:
                buf.append("PRESS ");
                break;
            case TYPE_DRAG:
                buf.append("DRAG ");
                break;
            case TYPE_CLICK:
                buf.append("CLICK ");
                break;
            case TYPE_RELEASE:
                buf.append("RELEASE ");
                break;
            case TYPE_MOVE:
                buf.append("MOVE ");
                break;
            case TYPE_ORIENTATION:
                buf.append("ORIENTATION ");
                break;
            case TYPE_BUTTON:
                buf.append("BUTTON ");
                break;
            case TYPE_WHEEL:
                buf.append("WHEEL ");
                break;
            default:
                buf.append("Unknown");
        }

        buf.append("\n Device coords: ");
        buf.append(devicePos[0]);
        buf.append(' ');
        buf.append(devicePos[1]);
        buf.append(' ');
        buf.append(devicePos[2]);
        buf.append(' ');

        buf.append("orientation: ");
        buf.append(deviceOri[0]);
        buf.append(' ');
        buf.append(deviceOri[1]);
        buf.append(' ');
        buf.append(deviceOri[2]);

        buf.append("\n World coords: ");
        buf.append(worldPos[0]);
        buf.append(' ');
        buf.append(worldPos[1]);
        buf.append(' ');
        buf.append(worldPos[2]);
        buf.append(' ');

        buf.append("orientation: ");
        buf.append(worldOri[0]);
        buf.append(' ');
        buf.append(worldOri[1]);
        buf.append(' ');
        buf.append(worldOri[2]);

        return buf.toString();
    }
}
