/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * PlanarCollisionParticleFunction TODO - not yet implemented.
 *
 * @author Daniel Selman
 * @version $Revision: 1.2 $
 */
public class PlanarCollisionParticleFunction implements ParticleFunction
{
    // plane defintion
    Point3d point;
    Vector3d vector;

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;

    public PlanarCollisionParticleFunction( Point3d point, Vector3d vector )
    {
        enabled = true;
        throw new UnsupportedOperationException();
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
     * @param ps The particle system that is being updated
     * @return true if this has done it's updating
     */
    public boolean newFrame(ParticleSystem ps)
    {
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
        // if the particle is going to pass through the plane
        // we are going to add an impulse force to stop it
        // from doing so
        // we assume that the particle is
        return false;
    }
}
