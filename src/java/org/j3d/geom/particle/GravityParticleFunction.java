/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.vecmath.Vector3d;

/**
 * GravityMovementFunction applied a gravity force to
 * Particles.
 *
 * @author Daniel Selman
 * @version $Revision: 1.2 $
 */
public class GravityParticleFunction implements ParticleFunction
{
    // force of gravity: meters per second
    private Vector3d gravityForce = new Vector3d( 0, -9.8, 0 );

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;

    public GravityParticleFunction()
    {
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

    public boolean onUpdate( ParticleSystem ps )
    {
       return true;
    }

    public boolean apply( Particle particle )
    {
        gravityForce.x = 0;
        gravityForce.y = -9.8;
        gravityForce.z = 0;

        gravityForce.scale( particle.mass );
        particle.resultantForce.add( gravityForce );
        return false;
    }
}
