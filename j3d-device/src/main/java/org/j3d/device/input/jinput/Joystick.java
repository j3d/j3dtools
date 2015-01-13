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
 * A joystick device implemented using the JInput library.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class Joystick implements JoystickDevice, TrackerDevice
{
    /** How small of a change to allow */
    private static final float DEVICE_THRESHOLD = 0.02f;

    /** The controller backing this gamepad */
    private Controller controller;

    /** The axes of the device */
    private Component[] axes;

    /** The name assigned to this device */
    private String name;

    /** The trackers implemented by this device */
    private Tracker[] trackers;

    /** The mapping of axes to function */
    private int[] controllerFuncs;

    /** lastVals to determine change */
    private float[] lastVals;

    /**
     * Constructor.
     */
    public Joystick(Controller c, String name)
    {
        controller = c;
        this.name = name;

        axes = c.getComponents();

        trackers = new Tracker[1];

        Component[] trackerAxes = new Component[2];
        int[] trackerFuncs = new int[2];
        controllerFuncs = new int[axes.length];
        lastVals = new float[axes.length];


        for(int j=0; j < axes.length; j++)
        {
            // System.out.println("   axes: " + axes[j].getName() + " type: " + axes[j].getIdentifier().getName());
            if (axes[j].getName().equals("X Axis"))
            {
                trackerAxes[0] = axes[j];
                trackerFuncs[0] = FUNC_NAV_X;
                controllerFuncs[j] = FUNC_LEFT_STICK_X;
            }
            else if (axes[j].getName().equals("Y Axis"))
            {
                trackerAxes[1] = axes[j];
                trackerFuncs[1] = FUNC_NAV_Y;
                controllerFuncs[j] = FUNC_LEFT_STICK_Y;
            }
            else if (axes[j].getName().equals("Z Rotation"))
            {
                controllerFuncs[j] = FUNC_LEFT_STICK_Z;
            }
            else if (axes[j].getName().equals("Hat Switch"))
            {
                controllerFuncs[j] = FUNC_LEFT_HAT;
            }
            else if (axes[j].getName().equals("Button 0"))
            {
                controllerFuncs[j] = FUNC_L1_BUTTON;
            }
            else if (axes[j].getName().equals("Rudder"))
            {

//                        axes.add(new ComponentHolder(axes[j], FUNC_ORIENT_X));

            }
            else if (axes[j].getName().equals("Extra"))
            {
//                axes.add(new ComponentHolder(axes[j], FUNC_ORIENT_Y));
            }
            else if (axes[j].getName().equals("Throttle"))
            {
//                axes.add(new ComponentHolder(axes[j], FUNC_THROTTLE));
            }
        }

        AxisTracker at = new AxisTracker(trackerAxes, trackerFuncs);
        trackers[0] = at;
    }

    //------------------------------------------------------------------------
    // Methods for InputDevice interface
    //------------------------------------------------------------------------

    /**
     * Get the name of this device.  Names are of the form class-#.  Valid
     * classes are Gamepad, Joystick, Wheel, Midi, GenericHID.
     *
     * @return The name
     */
    @Override
    public String getName()
    {
        return name;
    }

    //------------------------------------------------------------------------
    // Methods for GamepadDevice interface
    //------------------------------------------------------------------------

    /**
     * Get the current state of this device.  Any arrays too small will be
     * resized.
     *
     * @param state The state structure to fill in.
     */
    @Override
    public void getState(JoystickState state)
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
                case FUNC_LEFT_STICK_X:
                    state.stickX = val;
                    state.stickX_changed = true;
                    break;

                case FUNC_LEFT_STICK_Y:
                    state.stickY = -val;
                    state.stickY_changed = true;
                    break;

                case FUNC_LEFT_STICK_Z:
                    state.stickZ = -val;
                    state.stickZ_changed = true;
                    break;

                case FUNC_LEFT_HAT:
                    if (val == 1.0f)
                    {
                        state.hatX = -1f;
                        state.hatX_changed = true;
                        state.hatY = 0f;
                        state.hatY_changed = true;
                    }
                    else if (val == 0.875f)
                    {
                        state.hatX = -0.5f;
                        state.hatX_changed = true;
                        state.hatY = 0.5f;
                        state.hatY_changed = true;
                    }
                    else if (val == 0.75f)
                    {
                        state.hatX = 0f;
                        state.hatX_changed = true;
                        state.hatY = 1f;
                        state.hatY_changed = true;
                    }
                    else if (val == 0.625f)
                    {
                        state.hatX = 0.5f;
                        state.hatX_changed = true;
                        state.hatY = 0.5f;
                        state.hatY_changed = true;
                    }
                    else if (val == 0.5f)
                    {
                        state.hatX = 1f;
                        state.hatX_changed = true;
                        state.hatY = 0f;
                        state.hatY_changed = true;
                    }
                    else if (val == 0.375f)
                    {
                        state.hatX = 0.5f;
                        state.hatX_changed = true;
                        state.hatY = -0.5f;
                        state.hatY_changed = true;
                    }
                    else if (val == 0.25f)
                    {
                        state.hatX = 0f;
                        state.hatX_changed = true;
                        state.hatY = -1f;
                        state.hatY_changed = true;
                    }
                    else if (val == 0.125f)
                    {
                        state.hatX = -0.5f;
                        state.hatX_changed = true;
                        state.hatY = -0.5f;
                        state.hatY_changed = true;
                    }
                    else
                    {
                        state.hatX = 0f;
                        state.hatX_changed = true;
                        state.hatY = 0f;
                        state.hatY_changed = true;
                    }
                    break;

                case FUNC_THROTTLE:
                    // Need to remap for X3D -1 to 1 values
                    state.throttleSlider = -val;
                    state.throttleSlider_changed = true;
                    break;

                case FUNC_L1_BUTTON:
                    if (val == 1.0f)
                        state.triggerButton = true;
                    else
                        state.triggerButton = false;

                    state.triggerButton_changed = true;
                    break;
            }
        }
    }

    //------------------------------------------------------------------------
    // Methods for TrackerDevice interface
    //------------------------------------------------------------------------

    /**
     * Get the number of trackers.
     *
     * @return The number of trackers.  This cannot change after startup.
     */
    @Override
    public int getTrackerCount()
    {
        return trackers.length;
    }

    /**
     * Get the trackers of this device.
     *
     * @return The trackers.  This cannot changed after startup.
     */
    @Override
    public Tracker[] getTrackers()
    {
        return trackers;
    }
}

