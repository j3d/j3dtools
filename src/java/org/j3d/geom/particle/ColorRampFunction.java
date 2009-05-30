/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

// External imports
// None

// Local imports
import org.j3d.util.interpolator.ColorInterpolator;
import org.j3d.util.I18nManager;

/**
 * Apply a colour change over time to the particle.
 * <p>
 * Particle effects, like fire, like to change the colour of the emitted
 * particles over their lifetime. For example at t=0, the particle starts red
 * but fades to yellow, then transparent over its age. The time values are in
 * seconds and the interpolation is assumed to be in HSV space, although the
 * colors are provided as RGB values.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <p>
 * <ul>
 * <li>rampTimeMsg: Message when the lengths of the ramp and time arrays
 *     exactly match</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public class ColorRampFunction implements ParticleFunction
{
    /** Error message for different array lengths */
    private static final String LENGTH_MSG_PROP =
        "org.j3d.geom.particle.ColorRampFunction.rampTimeMsg";

    /** The colour interpolator we are using */
    private ColorInterpolator interpolator;

    /** A flag to say whether the colour values included an alpha component */
    private boolean hasAlpha;

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;

    /** The current timestamp for this generation */
    private long currentTime;

    /** The time value of the last item */
    private float lastTime;

    /** The last colour value */
    private float[] lastColor;

    /**
     * Create a disabled, default color ramp function with no values defined.
     * By default this will disabled and so an end user must explicitly set it
     * to be enabled if this constructor was called.
     */
    public ColorRampFunction()
    {
        lastColor = new float[4];
        enabled = false;
    }

    /**
     * Create a new colour ramp generator that uses the given times and
     * 3 component colour values for that time for the interpolation. The
     * colors are in a flat array of values and the entire array is used
     * to source the colour values from.
     *
     * @param times The array of times for each color
     * @param ramp The color values at each time
     * @param hasAlpha True if there is 4 component color, false for 3
     * @throws IllegalArgumentException The two arrays have differet length
     */
    public ColorRampFunction(float[] times, float[] ramp, boolean hasAlpha)
    {
        lastColor = new float[4];

        int size = (ramp == null) ? 0 : ramp.length;
        size = hasAlpha ? size / 4 : size / 3;
        setColorRamp(times, ramp, size, hasAlpha);
        enabled = true;
        currentTime = System.currentTimeMillis();
    }

    /**
     * Create a new colour ramp generator that uses the given times and
     * 3 component colour values for that time for the interpolation. The
     * colors are in a flat array of values and only the nominated number of
     * colour values are read from the array.
     *
     * @param times The array of times for each color
     * @param ramp The color values at each time
     * @param hasAlpha True if there is 4 component color, false for 3
     * @param numColors The number of valid items in the array
     * @throws IllegalArgumentException The two arrays have differet length
     */
    public ColorRampFunction(float[] times,
                              float[] ramp,
                              int numColors,
                              boolean hasAlpha)
    {
        lastColor = new float[4];

        setColorRamp(times, ramp, numColors, hasAlpha);
        currentTime = System.currentTimeMillis();
        enabled = true;
    }

    //-------------------------------------------------------------
    // Methods defined by ParticleFunction
    //-------------------------------------------------------------

    /**
     * Check to see if this function has been enabled or not currently.
     *
     * @return True if this is enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Set the enabled state of this function. A disabled function will not
     * be applied to particles during this update.
     *
     * @param state The new enabled state to set it to
     */
    public void setEnabled(boolean state)
    {
        enabled = state;
        currentTime = System.currentTimeMillis();
    }

    /**
     * Notification that the system is about to do an update of the particles
     * and to do any system-level initialisation.
     *
     * @param deltaT The elapsed time in milliseconds since the last frame
     * @return true if this should force another update after this one
     */
    public boolean newFrame(int deltaT)
    {
        currentTime += deltaT;
        return true;
    }

    /**
     * Apply this function to the given particle right now.
     *
     * @param particle The particle to apply the function to
     * @return true if the particle is still alive
     */
    public boolean apply(Particle particle)
    {
        int delta = (int)(currentTime - particle.wallClockBirth);

        float[] col = (delta >= lastTime) ?
                      lastColor :
                      interpolator.floatRGBValue(delta);

        particle.setColor(col[0], col[1], col[2], col[3]);

        return true;
    }

    //-------------------------------------------------------------
    // Local Methods
    //-------------------------------------------------------------

    /**
     * Set the color data for the ramp to the new 3 component values. Time
     * is defined in seconds.
     *
     * @param times The array of times for each color
     * @param ramp The color values at each time
     * @param hasAlpha true if this is 4-component colour, 3 otherwise
     * @throws IllegalArgumentException The two arrays have differet length
     */
    public void setColorRamp(float[] times, float[] ramp, boolean hasAlpha)
    {
        int size = (ramp == null) ? 0 : ramp.length;
        size = hasAlpha ? size / 4 : size / 3;

        setColorRamp(times, ramp, size, hasAlpha);
    }

    /**
     * Set the color data for the ramp to the new 3 component values. Time
     * is defined in seconds.
     *
     * @param times The array of times for each color
     * @param ramp The color values at each time
     * @param numColors The number of valid color values in the array
     * @param hasAlpha True if there is 4 component color, false for 3
     * @throws IllegalArgumentException The two arrays have differet length
     */
    public void setColorRamp(float[] times,
                             float[] ramp,
                             int numColors,
                             boolean hasAlpha)
    {
        if(times.length != numColors)
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg = intl_mgr.getString(LENGTH_MSG_PROP);
            throw new IllegalArgumentException(msg);
        }

        this.hasAlpha = hasAlpha;
        interpolator = new ColorInterpolator(numColors,
                                             ColorInterpolator.HSV_SPACE);

        int idx = 0;

        if(hasAlpha)
        {

            for(int i = 0; i < numColors; i++)
            {
                interpolator.addRGBKeyFrame(times[i] * 1000,
                                            ramp[idx++],
                                            ramp[idx++],
                                            ramp[idx++],
                                            ramp[idx++]);
            }

            lastTime = times[numColors - 1] * 1000;
            lastColor[0] = ramp[idx - 4];
            lastColor[1] = ramp[idx - 3];
            lastColor[2] = ramp[idx - 2];
            lastColor[3] = ramp[idx - 1];
        }
        else
        {
            for(int i = 0; i < numColors; i++)
            {
                interpolator.addRGBKeyFrame(times[i] * 1000,
                                            ramp[idx++],
                                            ramp[idx++],
                                            ramp[idx++],
                                            0);
            }

            lastTime = times[numColors - 1] * 1000;
            lastColor[0] = ramp[idx - 3];
            lastColor[1] = ramp[idx - 2];
            lastColor[2] = ramp[idx - 1];
        }
    }
}
