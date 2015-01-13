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
 * Simple ParticleFunction that causes a ParticleSystem to run for a fixed
 * number of frames from the start of this function, regardless of the
 * particle's set lifetime.
 *
 * @author Daniel Selman
 * @version $Revision: 2.0 $
 */
public class FrameCountParticleFunction implements ParticleFunction
{
    /** The maximum age a particle is allowed to be */
    private int maxAge;

    /** The number of frames currently used */
    private int frameCount = 0;

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;


    public FrameCountParticleFunction(int maxAge)
    {
        enabled = true;
        this.maxAge = maxAge;
    }

    //-------------------------------------------------------------
    // Methods defined by ParticleFunction
    //-------------------------------------------------------------

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
       frameCount++;
       return true;
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
        return (frameCount < maxAge);
    }

    //-------------------------------------------------------------
    // Local methods
    //-------------------------------------------------------------

    /**
     * Reset the function to have a zero frame count again.
     */
    public void resetFrameCount()
    {
        frameCount = 0;
    }
}
