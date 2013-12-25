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
import java.util.Random;

// Local imports
import org.j3d.util.I18nManager;

/**
 * WindParticleFunction models a directional wind source.
 * <p>
 * The wind has a direction and speed that control how it effects the
 * individual particle. From the wind speed, a pressure is calculated using
 * the following model:
 * <p>
 * <pre>
 * pressure = 10^(2 * log(speed)) * 0.64615
 * </pre>
 *
 * This is taken from the only I could find that would convert speed to
 * a pressure applied on an object. The location of this convertor is
 * <a href="http://www.cactus2000.de/uk/unit/masswsp.shtml">
 * http://www.cactus2000.de/uk/unit/masswsp.shtml</a>
 * <p>
 *
 * The force applied to the particle is thus proportional to it's surface
 * area. Naturally this does not do a really good model, such as taking into
 * account drag effects, etc, but it should be good-enough to model a gusty
 * wind acting on a lot of small particles.
 * <p>
 *
 * Further parameterisation is provided by allowing gustiness (speed variation)
 * and turbulence (direction variation) per frame, controlling the strength and
 * direction of the wind force.
 *
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <p>
 * <ul>
 * <li>negGustinessMsg: Message when a negative gustiness size is
 *     provided. </li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public class WindParticleFunction implements ParticleFunction
{
    /** Message when the gustiness is negative */
    private static final String NEG_GUSTINESS_MSG =
        "org.j3d.geom.particle.WindParticleFunction.negGustinessMsg.";

    /** Calculated force per square meter based on the wind speed */
    private float pressure;

    /** Current value for this frame based on the gustiness variation */
    private float currentPressure;

    /** How much does the wind vary from the basic speed */
    private float gustiness;

    /** How much the wind tends to swirl about */
    private float turbulence;

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;

    /** The direction the wind is travelling in */
    private float[] direction;

    /** The current speed of the wind */
    private float speed;

    /** Random function to mess with the wind */
    private Random randomiser;

    /**
     * Construct a new default wind particle function. Everything is set to
     * zeroes.
     */
    public WindParticleFunction()
    {
        direction = new float[3];
        pressure = 0;
        currentPressure = 0;

        gustiness = 0;
        turbulence = 0;
        speed = 0;

        enabled = true;
        randomiser = new Random();
    }

    /**
     * Construct a new wind function with the parameters provided.
     *
     * @param direction The direction of the wind
     * @param speed The speed of the wind
     * @param gustiness Speed variation per-frame, non-negative
     * @param turbulence Amount of per-particle variance
     */
    public WindParticleFunction(float[] direction,
                                float speed,
                                float gustiness,
                                float turbulence)
    {
        if(gustiness < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg = intl_mgr.getString(NEG_GUSTINESS_MSG);
            throw new IllegalArgumentException(msg);
        }

        this.direction = new float[3];
        this.direction[0] = direction[0];
        this.direction[1] = direction[1];
        this.direction[2] = direction[2];

        this.speed = speed;
        this.gustiness = gustiness;
        this.turbulence = turbulence;

        randomiser = new Random();

        enabled = true;
        recalculatePressure();
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
        // calculate the current wind pressure from the speed
        currentPressure = pressure * gustiness * randomiser.nextFloat();

        return true;
    }

    /**
     * Apply this function to the given particle right now.
     *
     * @param particle The particle to apply the function to
     * @return true if the particle has changed, false otherwise
     */
    public boolean apply(Particle particle)
    {

        float force_x = currentPressure * direction[0];
        float force_y = currentPressure * direction[1];
        float force_z = currentPressure * direction[2];

        if(turbulence != 0)
        {
            // apply the turbulence
            force_x += randomiser.nextFloat() * turbulence;
            force_y += randomiser.nextFloat() * turbulence;
            force_z += randomiser.nextFloat() * turbulence;
        }

        // Now scale it by the surface area to get back to newtowns of force
        force_x *= particle.surfaceArea;
        force_y *= particle.surfaceArea;
        force_z *= particle.surfaceArea;

        particle.resultantForce.x += force_x;
        particle.resultantForce.y += force_y;
        particle.resultantForce.z += force_z;

        return true;
    }

    //-------------------------------------------------------------
    // Local methods
    //-------------------------------------------------------------

    /**
     * Change the speed that wind is blowing at.
     *
     * @param speed The magnitude of the wind speed to use
     */
    public void setSpeed(float speed)
    {
        this.speed = speed;
        recalculatePressure();
    }

    /**
     * Get the current speed of the wind.
     *
     * @return A value of the speed
     */
    public float getSpeed()
    {
        return speed;
    }

    /**
     * Change the direction of the wind.
     *
     * @param x The x component of the wind direction
     * @param y The y component of the wind direction
     * @param z The z component of the wind direction
     */
    public void setDirection(float x, float y, float z)
    {
        direction[0] = x;
        direction[1] = y;
        direction[2] = z;
    }

    /**
     * Fetch the current direction of the wind speed direction.
     *
     * @param dir An array of length 3 to copy the values into
     */
    public void getDirection(float[] dir)
    {
        dir[0] = direction[0];
        dir[0] = direction[1];
        dir[0] = direction[2];
    }

    /**
     * Change the gustiness that wind is blowing at. Gustiness
     * is a per-frame modifier for the wind speed, so it will be used to
     * control how strong the wind is. However, it should not allow for the
     * wind to blow backwards, so it is limited to non-negative values only.
     *
     * @param gustiness The magnitude of the wind gustiness to use
     * @throws IllegalArgumentException The value was negative
     */
    public void setGustiness(float gustiness)
        throws IllegalArgumentException
    {
        if(gustiness < 0)
        {
            I18nManager intl_mgr = I18nManager.getManager();

            String msg = intl_mgr.getString(NEG_GUSTINESS_MSG);
            throw new IllegalArgumentException(msg);
        }

        this.gustiness = gustiness;
    }

    /**
     * Get the current gustiness that particles effected by.
     *
     * @return A value of the gustiness
     */
    public float getGustiness()
    {
        return gustiness;
    }

    /**
     * Change the turbulence of the wind.
     *
     * @param turbulence The magnitude of the wind turbulence to use
     */
    public void setTurbulence(float turbulence)
    {
        this.turbulence = turbulence;
    }

    /**
     * Get the current turbulence that particles are effected by.
     *
     * @return A value of the turbulence
     */
    public float getTurbulence()
    {
        return turbulence;
    }

    /**
     * Calculate and update the pressure due to the current wind speed.
     */
    private void recalculatePressure()
    {
        pressure = (float)(Math.pow(10, 2 * Math.log(speed)) * 0.64615);
    }
}
