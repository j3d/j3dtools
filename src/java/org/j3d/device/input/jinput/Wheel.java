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

package org.j3d.device.input.jinput;

// External imports
import net.java.games.input.*;

// Local imports
import org.j3d.device.input.*;

/**
 * A gamepad device implemented using the JInput library.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class Wheel implements WheelDevice, TrackerDevice
{
    /** How small of a change to allow */
    private static final float DEVICE_THRESHOLD = 0.02f;

    /** The controller backing this gamepad */
    private Controller controller;

    /** The name assigned to this device */
    private String name;

    /** The trackers implemented by this device */
    private Tracker[] trackers;

    /** The mapping of axis to function */
    private int[] controllerFuncs;

    /** The axes of the device */
    private Component[] axes;

    /** lastVals to determine change */
    private float[] lastVals;

    /** The rumblers */
    private Rumbler rumblerWheel;

    /** The rumbler functions */
    private int[] rumblerFuncs;

    /**
     * Constructor.
     */
    public Wheel(Controller c, String name)
    {
        controller = c;
        this.name = name;

        axes = c.getComponents();

        trackers = new Tracker[1];

        Component[] trackerAxes = new Component[2];
        int[] trackerFuncs = new int[2];

        controllerFuncs = new int[axes.length];
        lastVals = new float[axes.length];

        int len = axes.length;
        String axis_name;

        for(int j=0; j < len; j++)
        {
            axis_name = axes[j].getName();

            //System.out.println("   axes: " + axis_name + " type: " + axes[j].getIdentifier().getName());

            if(axis_name.equals("Wheel axis") ||
                axis_name.equalsIgnoreCase("X Axis"))
            {
                trackerAxes[0] = axes[j];
                trackerFuncs[0] = FUNC_NAV_X;
                controllerFuncs[j] = FUNC_WHEEL_X;
            }
            else if((axis_name.equals("Combined pedals") ||
                      axis_name.equalsIgnoreCase("Y Axis")))
            {
                trackerAxes[1] = axes[j];
                trackerFuncs[1] = FUNC_NAV_Y_CENTERED;
                controllerFuncs[j] = FUNC_THROTTLE;
            }
            else if (axis_name.equals("Left Paddle"))
            {
                controllerFuncs[j] = FUNC_BUTTON_1;
            }
            else if (axis_name.equals("Right Paddle"))
            {
                controllerFuncs[j] = FUNC_BUTTON_2;
            }
            else if (axis_name.equals("Button 3"))
            {
                controllerFuncs[j] = FUNC_BUTTON_3;
            }
            else if (axis_name.equals("Button 4"))
            {
                controllerFuncs[j] = FUNC_BUTTON_4;
            }
            else
            {
                controllerFuncs[j] = FUNC_NONE;
            }
        }

        AxisTracker at = new AxisTracker(trackerAxes, trackerFuncs);
        trackers[0] = at;

        Rumbler[] rumblers = c.getRumblers();
        len = rumblers.length;

        rumblerFuncs = new int[rumblers.length];

        //System.out.println("Rumblers:");
        for(int i=0; i < len; i++)
        {
            name = rumblers[i].getAxisName();

            if (name == null)
            {
                if (rumblerWheel == null)
                {
                    rumblerWheel = rumblers[i];
                }
                continue;
            }

            if (name.equals("Wheel axis"))
            {
                rumblerWheel = rumblers[i];
            }
        }
    }

    //------------------------------------------------------------------------
    // Methods for InputDevice interface
    //------------------------------------------------------------------------

    /**
     * Get the name of this device.  Names are of the form class-#.  Valid
     * classes are Wheel, Joystick, Wheel, Midi, GenericHID.
     *
     * @return The name
     */
    public String getName()
    {
        return name;
    }

    //------------------------------------------------------------------------
    // Methods for WheelDevice interface
    //------------------------------------------------------------------------

    /**
     * Get the current state of this device.  Any arrays too small will be
     * resized.
     *
     * @param state The state structure to fill in.
     */
    public void getState(WheelState state)
    {
        controller.poll();

        int len = axes.length;
        float val;

        for(int i=0; i < len; i++)
        {
            val = axes[i].getPollData();
            if (Math.abs(lastVals[i] - val) < DEVICE_THRESHOLD)
                continue;

            lastVals[i] = val;
            switch(controllerFuncs[i])
            {
                case FUNC_WHEEL_X:
                    state.wheelX = val;
                    state.wheelX_changed = true;
                    break;

                case FUNC_THROTTLE:
                    // Need to remap for X3D -1 to 1 values
                    state.throttleSlider = -val;
                    state.throttleSlider_changed = true;
                    break;

                case FUNC_BUTTON_1:
                    if (val == 1.0f)
                        state.button1 = true;
                    else
                        state.button1 = false;

                    state.button1_changed = true;
                    break;

                case FUNC_BUTTON_2:
                    if (val == 1.0f)
                        state.button2 = true;
                    else
                        state.button2 = false;

                    state.button2_changed = true;
                    break;

                case FUNC_BUTTON_3:
                    if (val == 1.0f)
                        state.button3 = true;
                    else
                        state.button3 = false;

                    state.button3_changed = true;
                    break;

                case FUNC_BUTTON_4:
                    if (val == 1.0f)
                        state.button4 = true;
                    else
                        state.button4 = false;

                    state.button4_changed = true;
                    break;
            }
        }
    }

    /**
     * Set the equilibrium point.  This is the direction the wheel will return
     * to if left alone.
     *
     * @param val The wheel direction, from left(-1) to 1(right).
     */
    public void setEquilibriumPoint(float val)
    {
        if (rumblerWheel != null)
            rumblerWheel.rumble(val);
    }

    //------------------------------------------------------------------------
    // Methods for TrackerDevice interface
    //------------------------------------------------------------------------

    /**
     * Get the number of trackers.
     *
     * @return The number of trackers.  This cannot change after startup.
     */
    public int getTrackerCount()
    {
        return trackers.length;
    }

    /**
     * Get the trackers of this device.
     *
     * @return The trackers.  This cannot changed after startup.
     */
    public Tracker[] getTrackers()
    {
        return trackers;
    }
}

