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
 * @version $Revision: 1.1 $
 */
public class GravityParticleFunction implements ParticleFunction
{
    // force of gravity: meters per second
    private Vector3d gravityForce = new Vector3d( 0, -9.8, 0 );

    public GravityParticleFunction()
    {
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
