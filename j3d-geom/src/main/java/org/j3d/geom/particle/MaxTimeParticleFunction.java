/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

/**
 * A ParticleFunction that is used to cull particles that have reached their
 * maximum allowed time.
 * <p>
 *
 * Within this particle system architecture, particles are not automatically
 * removed from the scene when their lifetime is reached. This function is used
 * to cull them from visibility at that point by comparing the current time
 * with the particle's death time.
 *
 * @author Daniel Selman
 * @version $Revision: 2.0 $
 */
public class MaxTimeParticleFunction implements ParticleFunction
{
    /** Flag to handle the enabled state */
    private boolean enabled;

    /** The current time this frame. Set during onUpdate() */
    private long currentTime;

    /**
     * Construct a new function that manages a particle's lifetime based on
     * the maximum age it can be
     */
    public MaxTimeParticleFunction()
    {
        enabled = true;
        currentTime = System.currentTimeMillis();
    }

    /**
     * Check to see if this function has been enabled or not currently.
     *
     * @return True if this is enabled
     */
    @Override
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
    @Override
    public void setEnabled(boolean state)
    {
        enabled = state;
        currentTime = System.currentTimeMillis();
    }

    /**
     * Apply this function to the given particle right now.
     *
     * @param particle The particle to apply the function to
     * @return true if the particle has changed, false otherwise
     */
    @Override
    public boolean apply(Particle particle)
    {
        return currentTime < particle.wallClockLife;
    }

    /**
     * Notification that the system is about to do an update of the particles
     * and to do any system-level initialisation.
     *
     * @param deltaT The elapsed time in milliseconds since the last frame
     * @return true if this should force another update after this one
     */
    @Override
    public boolean newFrame(int deltaT)
    {
        currentTime += deltaT;
        return true;
    }
}
