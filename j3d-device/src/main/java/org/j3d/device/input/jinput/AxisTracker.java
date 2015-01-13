/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.device.input.jinput;

// External imports
import net.java.games.input.*;

// Local imports
import org.j3d.device.input.*;

/**
 * A tracker implementation sourcing values from a USB axis.
 * <p>
 *
 * This tracker will not put its values into world coordinates.  They will
 * represent velocity direction vectors for navigation.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class AxisTracker extends Tracker
{

    /** What events is this tracker reporting. */
    private static final int mask = MASK_POSITION;

    /** How small of a change to allow */
    private static final float DEVICE_THRESHOLD = 0.02f;

    /** How small of a value to ignore */
    private static final float DEVICE_ZERO = 0.1f;

    /** The tracker state to return */
    private TrackerState tstate;

    /** Are any keys held */
    private boolean inMotion;

    /** Has a button changed state */
    private boolean newButton;

    /** How fast should we rotate. 0 to 2 */
    private float rot_speed;

    /** How fast should we translate.  0 to 2 */
    private float trans_speed;

    private float yVal;
    private float xVal;
    private float xSpeed;
    private float ySpeed;
    private float currOrientX;
    private float currOrientY;

    private Component[] axes;
    private int[] funcs;
    private float[] lastVal;

    public AxisTracker(Component axes[], int[] functions)
    {
        tstate = new TrackerState();
        this.axes = axes;
        funcs = functions;
        trans_speed = 1.6f;
        rot_speed = 0.64f;
        xSpeed = rot_speed;
        ySpeed = trans_speed;
        currOrientX = 0;
        currOrientY = 0;

        lastVal = new float[axes.length];
    }

    //------------------------------------------------------------------------
    // Methods defined by Tracker
    //------------------------------------------------------------------------


    /**
     * Notification that tracker polling is beginning.
     */
    @Override
    public void beginPolling()
    {
        // TODO: Should we double buffer?
    }

    /**
     * Notification that tracker polling is ending.
     */
    @Override
    public void endPolling()
    {
    }

    /**
     * What action types does this sensor return.  This a combination
     * of ACTION masks.
     *
     * @return The currently set action mask.
     */
    @Override
    public int getActionMask()
    {
        return mask;
    }

    /**
     * Get the current state of this tracker.
     *
	 * @param layer The ID of the visual layer to get the state from
	 * @param subLayer The ID of the contained internal layer from the
	 *    main layer to get the state from
     * @param state The current state
     */
    @Override
    public void getState(int layer, int subLayer, TrackerState state)
    {
        updateData();

        state.actionMask = mask;
        state.actionType = tstate.actionType;
        state.devicePos[0] = tstate.devicePos[0];
        state.devicePos[1] = tstate.devicePos[1];
        state.devicePos[2] = tstate.devicePos[2];
        state.deviceOri[0] = tstate.deviceOri[0];
        state.deviceOri[1] = tstate.deviceOri[1];
        state.deviceOri[2] = tstate.deviceOri[2];
        state.worldPos[0] = tstate.worldPos[0];
        state.worldPos[1] = tstate.worldPos[1];
        state.worldPos[2] = tstate.worldPos[2];
        state.worldOri[0] = tstate.worldOri[0];
        state.worldOri[1] = tstate.worldOri[1];
        state.worldOri[2] = tstate.worldOri[2];

        state.buttonMode[0] = ButtonModeConstants.NAV1;
        state.buttonMode[1] = ButtonModeConstants.NAV1;
        state.buttonMode[2] = tstate.buttonMode[2];

        for(int i=0; i < tstate.buttonState.length; i++)
        {
            state.buttonState[i] = tstate.buttonState[i];
        }

        if (inMotion)
        {
            tstate.actionType = TrackerState.TYPE_DRAG;
            tstate.devicePos[0] = xVal * xSpeed;
            tstate.devicePos[1] = yVal * ySpeed;

        }
        else if (newButton)
        {
           tstate.actionType = TrackerState.TYPE_BUTTON;
        }
        else
            tstate.actionType = TrackerState.TYPE_NONE;

        newButton = false;

    }

    public void updateData()
    {
        int len;
        len = axes.length;

        Component axis;
        int funcNum;
        float newVal;

        for(int i=0; i < len; i++)
        {
            axis = axes[i];

            if (axis == null)
                continue;

            // Ignore values which didn't change
            newVal = axis.getPollData();

            if (Math.abs(lastVal[i] - newVal) < DEVICE_ZERO)
                continue;
            lastVal[i] = newVal;
            switch(funcs[i])
            {
                case InputDevice.FUNC_ORIENT_X :
                    tstate.actionType = TrackerState.TYPE_ORIENTATION;
                    currOrientX = newVal;

                    tstate.deviceOri[0] = currOrientY;
                    tstate.deviceOri[1] = currOrientX;
                    tstate.deviceOri[2] = 0;
                    continue;

                case InputDevice.FUNC_ORIENT_Y :
                    tstate.actionType = TrackerState.TYPE_ORIENTATION;
                    currOrientY = -newVal;
                    tstate.deviceOri[0] = currOrientY;
                    tstate.deviceOri[1] = currOrientX;
                    tstate.deviceOri[2] = 0;
                    continue;

                case InputDevice.FUNC_THROTTLE :
                    xSpeed = rot_speed * (1.05f - newVal);
                    ySpeed = trans_speed * (1.05f - newVal);
                    continue;
            }

            tstate.buttonState[0] = true;

            if (!inMotion)
            {
                tstate.actionType = TrackerState.TYPE_PRESS;

                tstate.devicePos[0] = 0.0f;
                tstate.devicePos[1] = 0.0f;
                tstate.devicePos[2] = 0.0f;
            }

            switch(funcs[i])
            {
                case InputDevice.FUNC_NAV_X:
                    inMotion = true;
                    xVal = newVal;
                    break;

                case InputDevice.FUNC_NAV_Y:
                    inMotion = true;
                    yVal = newVal;
                    break;

                case InputDevice.FUNC_NAV_Y_CENTERED:
                    inMotion = true;
                    yVal = 3.0f * newVal;
                    break;
            }

            if (Math.abs(xVal) <= DEVICE_ZERO && Math.abs(yVal) <= DEVICE_ZERO)
            {
                inMotion = false;
                xVal = 0;
                yVal = 0;

                tstate.actionType = TrackerState.TYPE_RELEASE;
                tstate.buttonState[0] = false;
            }
        }
    }
}
