/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.device.input.vfx;

// Standard imports
// None

// Application specific imports
// None

/**
 * Class which implements a global driver interface for the VFX3D handling.
 * <p>
 *
 * The code forms the interface to the native underlying libraries. When using
 * the HMD in stereo mode the calls should be made to this library before
 * initializing Java3D, and after you have closed everything down.
 * <p>
 *
 * The assumption is that there is only one HMD attached to the system at a
 * time, and that in doing so, the application has all the control over the
 * class. A singleton is provided to avoid messy issues with needing to track
 * how many enables and disables have been called when dealing with the native
 * library.
 * <p>
 *
 * Reading tracker data is a polling only operation. It is typically fast enough
 * that it can be used within a tight render loop and does not need external
 * asynchronous handling.
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class VFXDriver
{
    /** Message used for when the device has been shut down */
    private static final String INACTIVE_MSG = "VFX Device has been shutdown";

    /** Global instance that everyone shares */
    private static VFXDriver instance;

    /** Is the driver currently enabled now? */
    private boolean enabled;

    /** Is the device currently active? */
    private boolean deviceActive;

    /**
     * Initialize the driver now
     */
    private VFXDriver()
    {
        deviceActive = true;
        enabled = false;

        System.loadLibrary("vfx_hmd");

        if(!initializeVFX())
             throw new RuntimeException("Cannot init");
    }


    /**
     * Get hold of the single global instance of this driver.
     */
    public static VFXDriver getVFXDriver()
    {
        if(instance == null)
            instance = new VFXDriver();

        return instance;
    }

    /**
     * Enable the VFX driver for a specific type of renderer. Due to the way
     * the VFX driver works, the flag must be set to know whether the system
     * is using the DirectX or OpenGL version of Java3D.
     *
     * @param isDirect3D True if the application is running on the D3D version
     * @throws IllegalStateException The device has been shut down or the
     *    device is currently enabled
     */
    public void enableStereo(boolean isDirect3D)
        throws IllegalStateException
    {
        if(!deviceActive)
            throw new IllegalStateException(INACTIVE_MSG);

        if(enabled)
            throw new IllegalStateException("The stereo system is already running");

        enableVFXStereo(isDirect3D);
        enabled = true;
    }

    /**
     * Disable the stereo output now.
     *
     * @throws IllegalStateException The device has been shut down
     */
    public void disableStereo()
        throws IllegalStateException
    {
        if(!deviceActive)
            throw new IllegalStateException(INACTIVE_MSG);

        disableVFXStereo();
        enabled = false;
    }

    /**
     * Reset the zero position of the tracker to be it's current orientation.
     *
     * @throws IllegalStateException The device has been shut down
     */
    public void resetZeroPosition()
        throws IllegalStateException
    {
        if(!deviceActive)
            throw new IllegalStateException(INACTIVE_MSG);

        resetTrackerZero();
    }

    /**
     * Fetch the current tracking position from the HMD. The values are given
     * as yaw, pitch and roll in the array.
     *
     * @param orientation The array to copy the location information into
     * @throws IllegalStateException The device has been shut down
     */
    public void getTrackerPosition(float[] orientation)
        throws IllegalStateException
    {
        if(!deviceActive)
            throw new IllegalStateException(INACTIVE_MSG);

        readTrackerPosition(orientation);
    }

    /**
     * Check to see if the device has been previously shut down.
     *
     * @return True if the device interface is still running
     */
    public boolean isDeviceActive()
    {
        return deviceActive;
    }

    /**
     * Shut down the entire device now. After calling this, no methods will
     * function correctly and will issue exceptions. It is not possible to
     * restart the device after calling this method.
     *
     * @throws IllegalStateException The device has been shut down
     */
    public void shutdownDevice()
        throws IllegalStateException
    {
        if(!deviceActive)
            throw new IllegalStateException(INACTIVE_MSG);

        shutdown();
        deviceActive = false;
    }

    /**
     * Initialize the internal library and any local variables needed.
     *
     * @return true if the initialization happened correctly
     */
    native boolean initializeVFX();

    /**
     * Native method call to reset the zero position on the tracker.
     */
    native void resetTrackerZero();

    /**
     * Native method call to read the current orientation information.
     */
    native void readTrackerPosition(float[] orientation);

    /**
     * Native call to enable the stereo mode
     */
    native void enableVFXStereo(boolean isDirect3D);

    /**
     * Native call to disable the stereo handling
     */
    native void disableVFXStereo();

    /**
     * Native call to shut down the entire DLL
     */
    native void shutdown();
}