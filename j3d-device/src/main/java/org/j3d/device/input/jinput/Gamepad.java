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
public class Gamepad implements GamepadDevice, TrackerDevice
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
    private Rumbler rumblerX;

    /** The rumblers */
    private Rumbler rumblerY;

    /** The rumbler functions */
    private int[] rumblerFuncs;

    /**
     * Constructor.
     */
    public Gamepad(Controller c, String name)
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

        for(int j = 0; j < len; j++)
        {
            //System.out.println("   axes: " + axes[j].getName() + " type: " + axes[j].getIdentifier().getName());
            axis_name = axes[j].getName();

            if(axis_name.equalsIgnoreCase("X axis") || axis_name.equals("x"))
            {
                trackerAxes[0] = axes[j];
                trackerFuncs[0] = FUNC_NAV_X;
                controllerFuncs[j] = FUNC_LEFT_STICK_X;
            }
            else if(axis_name.equalsIgnoreCase("Y axis") || axis_name.equals("y"))
            {
                trackerAxes[1] = axes[j];
                trackerFuncs[1] = FUNC_NAV_Y;
                controllerFuncs[j] = FUNC_LEFT_STICK_Y;
            }
            else if (axis_name.equals("Rudder"))
            {
                //trackerAxes[0] = axes[j];
                //trackerFuncs[0] = FUNC_ORIENT_X;
                controllerFuncs[j] = FUNC_RIGHT_STICK_X;
            }
            else if (axis_name.equals("Z Rotation"))
            {
                //trackerAxes[0] = axes[j];
                //trackerFuncs[0] = FUNC_ORIENT_X;
                controllerFuncs[j] = FUNC_RIGHT_STICK_X;
            }
            else if (axis_name.equals("Z Axis"))
            {
                //trackerAxes[2] = axes[j];
                //trackerFuncs[2] = FUNC_ORIENT_Y;
                controllerFuncs[j] = FUNC_RIGHT_STICK_Y;
            }
            else if (axis_name.equals("Extra"))
            {
                //trackerAxes[2] = axes[j];
                //trackerFuncs[2] = FUNC_ORIENT_Y;
                controllerFuncs[j] = FUNC_RIGHT_STICK_Y;
            }
            else if (axis_name.equals("Throttle"))
            {
                controllerFuncs[j] = FUNC_THROTTLE;
            }
            else if (axis_name.equals("S Button"))
            {
                controllerFuncs[j] = FUNC_START_BUTTON;
            }
            else if (axis_name.equals("Left Trigger"))
            {
                controllerFuncs[j] = FUNC_L1_BUTTON;
            }
            else if (axis_name.equals("Right Trigger"))
            {
                controllerFuncs[j] = FUNC_R1_BUTTON;
            }
            else if (axis_name.equals("Button 4"))
            {
                controllerFuncs[j] = FUNC_L1_BUTTON;
            }
            else if (axis_name.equals("Button 5"))
            {
                controllerFuncs[j] = FUNC_R1_BUTTON;
            }
            else if (axis_name.equals("Hat Switch"))
            {
                controllerFuncs[j] = FUNC_LEFT_HAT;
            }
            else
            {
                System.out.println("Could not map Gamepad axis: " + axis_name);
                controllerFuncs[j] = FUNC_NONE;
            }
        }

        AxisTracker at = new AxisTracker(trackerAxes, trackerFuncs);
        trackers[0] = at;

        Rumbler[] rumblers = c.getRumblers();
        len = rumblers.length;

        rumblerFuncs = new int[rumblers.length];

        //System.out.println("Rumblers:");

        Component.Identifier iden;

        for(int i=0; i < len; i++)
        {
            name = rumblers[i].getAxisName();
            iden = rumblers[i].getAxisIdentifier();

            if (name == null)
            {

                if (rumblerX == null)
                    rumblerX = rumblers[i];
                else if (rumblerY == null)
                    rumblerX = rumblers[i];

                continue;
            }

            if (name.equals("X axis"))
            {
                rumblerX = rumblers[i];
            } else if (name.equals("Y axis"))
            {
                rumblerY = rumblers[i];
            }
        }
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
    public void getState(GamepadState state)
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
                    state.leftStickX = val;
                    state.leftStickX_changed = true;
                    break;

                case FUNC_LEFT_STICK_Y:
                    state.leftStickY = -val;
                    state.leftStickY_changed = true;
                    break;

                case FUNC_RIGHT_STICK_X:
                    state.rightStickX = val;
                    state.rightStickX_changed = true;
                    break;

                case FUNC_RIGHT_STICK_Y:
                    state.rightStickY = -val;
                    state.rightStickY_changed = true;
                    break;

                case FUNC_LEFT_HAT:
                    if (val == 1.0f)
                    {
                        state.leftHatX = -1f;
                        state.leftHatX_changed = true;
                        state.leftHatY = 0f;
                        state.leftHatY_changed = true;
                    }
                    else if (val == 0.875f)
                    {
                        state.leftHatX = -0.5f;
                        state.leftHatX_changed = true;
                        state.leftHatY = 0.5f;
                        state.leftHatY_changed = true;
                    }
                    else if (val == 0.75f)
                    {
                        state.leftHatX = 0f;
                        state.leftHatX_changed = true;
                        state.leftHatY = 1f;
                        state.leftHatY_changed = true;
                    }
                    else if (val == 0.625f)
                    {
                        state.leftHatX = 0.5f;
                        state.leftHatX_changed = true;
                        state.leftHatY = 0.5f;
                        state.leftHatY_changed = true;
                    }
                    else if (val == 0.5f)
                    {
                        state.leftHatX = 1f;
                        state.leftHatX_changed = true;
                        state.leftHatY = 0f;
                        state.leftHatY_changed = true;
                    }
                    else if (val == 0.375f)
                    {
                        state.leftHatX = 0.5f;
                        state.leftHatX_changed = true;
                        state.leftHatY = -0.5f;
                        state.leftHatY_changed = true;
                    }
                    else if (val == 0.25f)
                    {
                        state.leftHatX = 0f;
                        state.leftHatX_changed = true;
                        state.leftHatY = -1f;
                        state.leftHatY_changed = true;
                    }
                    else if (val == 0.125f)
                    {
                        state.leftHatX = -0.5f;
                        state.leftHatX_changed = true;
                        state.leftHatY = -0.5f;
                        state.leftHatY_changed = true;
                    }
                    else
                    {
                        state.leftHatX = 0f;
                        state.leftHatX_changed = true;
                        state.leftHatY = 0f;
                        state.leftHatY_changed = true;
                    }
                    break;

                case FUNC_THROTTLE:
                    // Need to remap for X3D -1 to 1 values
                    state.throttleSlider = -val;
                    state.throttleSlider_changed = true;
                    break;

                case FUNC_START_BUTTON:
                    if (val == 1.0f)
                        state.startButton = true;
                    else
                        state.startButton = false;

                    state.startButton_changed = true;
                    break;

                case FUNC_L1_BUTTON:
                    if (val == 1.0f)
                        state.l1Button = true;
                    else
                        state.l1Button = false;

                    state.l1Button_changed = true;
                    break;

                case FUNC_R1_BUTTON:
                    if (val == 1.0f)
                        state.r1Button = true;
                    else
                        state.r1Button = false;

                    state.r1Button_changed = true;
                    break;
            }
        }
    }

    /**
     * Set the rumblerX axis.
     *
     * @param val The rumbler strength, from -1 to 1.
     */
    public void setRumblerX(float val)
    {
        if (rumblerX != null)
            rumblerX.rumble(val);
    }

    /**
     * Set the rumblerY axis.
     *
     * @param val The rumbler strength, from -1 to 1.
     */
    public void setRumblerY(float val)
    {
        if (rumblerY != null)
            rumblerY.rumble(val);
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

