/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

// Standard imports
import javax.vecmath.Vector3d;

// Application specific imports
// None

/**
 * GravityMovementFunction applied a gravity force to
 * Particles.
 *
 * @author Daniel Selman
 * @version $Revision: 2.0 $
 */
public class GravityParticleFunction implements ParticleFunction
{
    /** accelaration due to gravity: meters per second squared */
    private float[] gravityForce;

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;

    /**
     * Construct the function with gravity acting in the traditional newtonian
     * way - down the -Y axis at 9.8m/s^2.
     */
    public GravityParticleFunction()
    {
        this(0, -9.8f, 0);
    }

    /**
     * Create a new function with gravity set to the given vector and value.
     *
     * @param x The gravity vector to apply along the x axis
     * @param y The gravity vector to apply along the y axis
     * @param z The gravity vector to apply along the z axis
     */
    public GravityParticleFunction(float x, float y, float z)
    {
        gravityForce = new float[] {x, y, z};
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
       return true;
    }

    /**
     * Apply this function to the given particle right now.
     *
     * @param particle The particle to apply the function to
     * @return true if the particle stays alive, false otherwise
     */
    public boolean apply(Particle particle)
    {
        particle.resultantForce.x += gravityForce[0];
        particle.resultantForce.y += gravityForce[1];
        particle.resultantForce.z += gravityForce[2];

        return true;
    }

    //-------------------------------------------------------------
    // Local methods
    //-------------------------------------------------------------

    /**
     * Change the gravity to a new value.
     *
     * @param gravity The vector and magnitude of the new gravity
     */
    public void setGravity(float[] gravity)
    {
        gravityForce[0] = gravity[0];
        gravityForce[1] = gravity[1];
        gravityForce[2] = gravity[2];
    }

    /**
     * Get the current value of gravity.
     *
     * @param val An array of length 3 to copy the values to
     */
    public void getGravity(float[] val)
    {
        val[0] = gravityForce[0];
        val[1] = gravityForce[1];
        val[2] = gravityForce[2];
    }
}
