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

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import java.util.ArrayList;

// Local imports
import org.j3d.device.input.InputDevice;
import org.j3d.device.input.DeviceManager;
import org.j3d.device.input.DeviceListener;

import org.j3d.util.ErrorReporter;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.I18nManager;

/**
 * A USB device.  This device is a navigation only device.
 * <p>
 *
 * <b>TODO:</b>
 * <ul>
 * <li>Each device string needs to be hardcoded. Should be pulled from a
 *     config file of some sort.</li>
 * <li>Need to handle dynamic addition/removal of devices when Jinput
 *     supports it.</li>
 * </ul>
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>detectIntroMsg: Informational message when we start looking for
 *     devices.</li>
 * <li>detectDeviceListMsg: Informational message writing out the device
 *     details found</li>
 * <li>deviceAxisListMsg: Informational message for a single axis of a
 *     detected device</li>
 * <li>missingDeviceNameMsg: Error message for a device that has no name</li>
 * <li>unknownDeviceMsg: Error message for an unhandled device</li>
 * <li>detectOSFailMsg: Error message when system permissions would not let
 *     us work out which platform we're on</li>
 * <li>unsupportedOSMsg: Error message when we detect a platform, but it is
 *     not one of the supported platforms.</li>
 * </ul>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class USBManager implements DeviceManager
{
    /** Message when starting the device detection */
    private static final String DETECT_INTRO_PROP =
        "org.j3d.device.input.jinput.USBManager.detectIntroMsg";

    /** Message when listing a single device */
    private static final String DETECT_LIST_PROP =
        "org.j3d.device.input.jinput.USBManager.detectDeviceListMsg";

    /** Message when printing out a single axis of the device */
    private static final String DEVICE_AXIS_DESC_PROP =
        "org.j3d.device.input.jinput.USBManager.deviceAxisListMsg";

    private static final String MISSING_DEVICE_NAME_PROP =
        "org.j3d.device.input.jinput.USBManager.missingDeviceNameMsg";

    private static final String UNKNOWN_DEVICE_PROP =
        "org.j3d.device.input.jinput.USBManager.unknownDeviceMsg";

    /** Message when we failed to be able to read the OS properties */
    private static final String DETECT_OS_FAIL_PROP =
        "org.j3d.device.input.jinput.USBManager.detectOSFailMsg";

    /** Message when we didn't find a compatible OS */
    private static final String MISSING_OS_PROP =
        "org.j3d.device.input.jinput.USBManager.unsupportedOSMsg";

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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

        I18nManager intl_mgr = I18nManager.getManager();
        String msg_pattern = intl_mgr.getString(DETECT_INTRO_PROP);
        Locale lcl = intl_mgr.getFoundLocale();

        NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

        Object[] msg_args = { new Float(cnt) };
        Format[] fmts = { n_fmt };
        MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
        msg_fmt.setFormats(fmts);
        String msg = msg_fmt.format(msg_args);

        errorReporter.messageReport(msg);

        String list_pattern = intl_mgr.getString(DETECT_LIST_PROP);
        Format[] list_fmts = { null, n_fmt };
        MessageFormat list_fmt = new MessageFormat(list_pattern, lcl);
        list_fmt.setFormats(fmts);

        for(int i = 0; i < cnt; ++i )
        {
            Controller dev = ca[i];

            Object[] list_args =
            {
                dev.getName(),
                new Integer(dev.getRumblers().length)
            };

            String list_msg = list_fmt.format(list_args);
            errorReporter.messageReport(list_msg);

            Controller.Type type = dev.getType();

            name = dev.getName();

            if(name == null)
            {
                msg = intl_mgr.getString(MISSING_DEVICE_NAME_PROP);
                errorReporter.warningReport(msg, null);
            }
            else if(name.indexOf("RumblePad") > -1 ||
                    name.indexOf("WingMan Cordless Gamepad") > -1 ||
                    name.indexOf("Logitech Dual Action") > -1)
            {
                devices.add(new Gamepad(dev, "Gamepad-" + gamepadCnt));
                gamepadCnt++;
            }
            else if(name.indexOf("Extreme Digital 3D") > -1 ||
                       name.indexOf("Freedom 2.4") > -1)
            {
                devices.add(new Joystick(dev, "Joystick-" + joystickCnt));
                joystickCnt++;
            }
            else if((name.indexOf("MOMO Racing") > -1) ||
                     name.indexOf("Logitech Racing Wheel") > -1)
            {
                devices.add(new Wheel(dev, "Wheel-" + wheelCnt));
                wheelCnt++;
            }
            else if(name.indexOf("SpaceBall 5000") > -1)
            {
/*
                devices.add(new SixDOF(dev, "SixDOF-" + sixDOFCnt));
                sixDOFCnt++;
*/
            }
            else if(name.startsWith("Mouse") ||
                    name.startsWith("Keyboard"))
            {
                // ignore
            }
            else
            {
                msg = intl_mgr.getString(UNKNOWN_DEVICE_PROP);
                errorReporter.messageReport(msg);
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
                    @Override
                    public Object run()
                    {
                        String osName = System.getProperty("os.name").toLowerCase();

                        if(!(osName.startsWith("windows") ||
                             osName.startsWith("mac") ||
                             osName.startsWith("linux")))
                        {
                            I18nManager intl_mgr = I18nManager.getManager();
                            String msg_pattern = intl_mgr.getString(MISSING_OS_PROP);

                            Locale lcl = intl_mgr.getFoundLocale();
                            Object[] msg_args = { osName };
                            Format[] fmts = { null };
                            MessageFormat msg_fmt =
                                new MessageFormat(msg_pattern, lcl);
                            String msg = msg_fmt.format(msg_args);
                            msg_fmt.setFormats(fmts);

                            errorReporter.warningReport(msg, null);
                        }

                        return null;
                    }
                }
            );
        }
        catch (PrivilegedActionException pae)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(DETECT_OS_FAIL_PROP);

            errorReporter.warningReport(msg, null);
        }
    }

    /**
     * Print a devices functions.
     *
     * @param c
     */
    private void printDevice(Controller c)
    {
        Component[] axes = c.getComponents();

        int len = axes.length;

        I18nManager intl_mgr = I18nManager.getManager();
        String msg_pattern = intl_mgr.getString(DEVICE_AXIS_DESC_PROP);
        Locale lcl = intl_mgr.getFoundLocale();

        NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

        Format[] fmts = { null, null };
        MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
        msg_fmt.setFormats(fmts);

        for(int j = 0; j < len; j++)
        {
            Object[] msg_args =
            {
                axes[j].getName(),
                axes[j].getIdentifier().getName()
            };

            String msg = msg_fmt.format(msg_args);
            errorReporter.messageReport(msg);
        }
    }
}
