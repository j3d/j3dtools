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

import java.util.ArrayList;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

// Local imports
import org.j3d.device.input.InputDevice;
import org.j3d.device.input.DeviceManager;
import org.j3d.device.input.DeviceListener;

import org.j3d.util.ErrorReporter;
import org.j3d.util.DefaultErrorReporter;

/**
 * A USB device.  This device is a navigation only device.
 * <p>
 *
 * TODO: Need to handle dynamic addition/removal of devices when Jinput
 * support it.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class USBManager implements DeviceManager
{
    /** The device list */
    private ArrayList<InputDevice> devices;

    /** List of those who want to know about device changes. */
    private ArrayList<DeviceListener> deviceListeners;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Flag indicating if we've loaded devices or not */
    private boolean deviceListInit;

    /**
     * Construct a new instance of the manager
     */
    public USBManager()
    {
        deviceListInit = false;

        // See if we even have the system available. Not always the case. If
        // we don't disable the device list loading straight away.
        deviceListInit = Package.getPackage("net.java.games.input") == null;

        devices = new ArrayList<InputDevice>();
        deviceListeners = new ArrayList<DeviceListener>();
    }

    //------------------------------------------------------------------------
    // Methods for DeviceManager interface
    //------------------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Get the number of devices discovered.
     *
     * @return The number of devices.
     */
    public int getNumDevices()
    {
        if(!deviceListInit)
            loadDevices();

        return devices.size();
    }

    /**
     * Get the device discovered by this manager.  All devices discovered
     * after this call will be reported to DeviceListeners.
     *
     * @return InputDevice[] An array of discovered devices.
     */
    public InputDevice[] getDevices()
    {
        if(!deviceListInit)
            loadDevices();

        InputDevice[] devs = new InputDevice[devices.size()];

        devices.toArray(devs);

        return devs;
    }

    /**
     * Add a listener for devices additions and removals.
     *
     * @param l The listener.  Nulls and duplicates will be ignored.
     */
    public void addDeviceListener(DeviceListener l)
    {
        if(!deviceListeners.contains(l))
            deviceListeners.add(l);

    }

    /**
     * Remove a listener for device additions and removals.
     *
     * @param l The listener.  Nulls and not found listeners will be ignored.
     */
    public void removeDeviceListener(DeviceListener l)
    {
        deviceListeners.remove(l);
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

    /**
     * Initialise all the devices now.
     */
    private void loadDevices()
    {
        setupProperties();

        ControllerEnvironment ce =
            ControllerEnvironment.getDefaultEnvironment();

        Controller[] ca = ce.getControllers();

        InputDevice device;
        int gamepadCnt = 0;
        int joystickCnt = 0;
        int wheelCnt = 0;
        int sixDOFCnt = 0;

        String name;

        int cnt = ca.length;
        errorReporter.messageReport("Jinput USB devices detected: " + cnt);

        for(int i = 0; i < cnt; ++i )
        {
            Controller dev = ca[i];

            errorReporter.messageReport("device: " + dev.getName() +
                                       " rumblers: " +
                                       dev.getRumblers().length);
            Controller.Type type = dev.getType();

            name = dev.getName();

            if (name == null)
            {
                errorReporter.warningReport("No name for device, skipping", null);
            }
            else if (name.indexOf("RumblePad") > -1 ||
                       name.indexOf("WingMan Cordless Gamepad") > -1 ||
                       name.indexOf("Logitech Dual Action") > -1)
            {
                devices.add(new Gamepad(dev, "Gamepad-" + gamepadCnt));
                gamepadCnt++;
            }
            else if (name.indexOf("Extreme Digital 3D") > -1 ||
                       name.indexOf("Freedom 2.4") > -1)
            {
                devices.add(new Joystick(dev, "Joystick-" + joystickCnt));
                joystickCnt++;
            }
            else if ((name.indexOf("MOMO Racing") > -1) ||
                      name.indexOf("Logitech Racing Wheel") > -1)
            {
                devices.add(new Wheel(dev, "Wheel-" + wheelCnt));
                wheelCnt++;
            }
            else if (name.indexOf("SpaceBall 5000") > -1)
            {
/*
                devices.add(new SixDOF(dev, "SixDOF-" + sixDOFCnt));
                sixDOFCnt++;
*/
            }
            else if (name.startsWith("Mouse") ||
                     name.startsWith("Keyboard"))
            {
                // ignore
            }
            else
            {
                errorReporter.messageReport("Unhandled device. " +
                                            "Printing properties: ");
                printDevice(dev);
            }
        }

        deviceListInit = true;
    }

    /**
     * Set up the system properties needed to configure JInput.
     */
    private void setupProperties()
    {
        try
        {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction ()
                {
                    public Object run()
                    {
                        String osName = System.getProperty("os.name").toLowerCase();

                        if(!(osName.startsWith("windows") ||
                             osName.startsWith("mac") ||
                             osName.startsWith("linux")))
                        {
                            errorReporter.warningReport("No JInput plugin defined for: " + osName, null);
                        }

                        return null;
                    }
                }
            );
        }
        catch (PrivilegedActionException pae)
        {
            errorReporter.warningReport("Error setting Properties in USBManger", null);
        }
    }

    /**
     * Print a devices functions.
     *
     * @param controller
     */
    private void printDevice(Controller c)
    {
        Component[] axes = c.getComponents();

        int len = axes.length;

        for(int j = 0; j < len; j++)
        {
            errorReporter.messageReport("   axes: " + axes[j].getName() +
                                        " type: " +
                                        axes[j].getIdentifier().getName());
        }
    }
}
