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
 * Simple ParticleFunction that causes a ParticleSystem
 * to run for a fixed number of frames.
 *
 * @author Daniel Selman
 * @version $Revision: 1.2 $
 */
public class FrameCountParticleFunction implements ParticleFunction
{
    private int maxAge;
    private int frameCount = 0;

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;


    public FrameCountParticleFunction( int maxAge )
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
     * Apply this function to the given particle right now.
     *
     * @param particle The particle to apply the function to
     * @return true if the particle has changed, false otherwise
     */
    public boolean onUpdate( ParticleSystem ps )
    {
       frameCount++;
       return true;
    }

    /**
     * Notification that the system is about to do an update of the particles
     * and to do any system-level initialisation.
     *
     * @param ps The particle system that is being updated
     * @return true if this should force another update after this one
     */
    public boolean apply( Particle particle )
    {
        return( frameCount < maxAge );
    }
}