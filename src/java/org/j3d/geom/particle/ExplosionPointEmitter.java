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
 * MaxAgePointEmitter checks the age of a Particle
 * and reinitializes it by moving it to a point in space
 * and clearing resultant force and velocity.
 *
 * @author Daniel Selman
 * @version $Revision: 1.1 $
 */
public class ExplosionPointEmitter implements ParticleInitializer
{
    double originX;
    double originY;
    double originZ;

    public ExplosionPointEmitter( double x, double y, double z )
    {
        originX = x;
        originY = y;
        originZ = z;
    }

    public boolean initialize( Particle particle )
    {
        particle.setColor( 1, 1, 1, 1 );

        particle.setPositionAndPrevious( originX, originY, originZ );
        particle.resultantForce.set( Particle.getRandomNumber( 0, 1 ),
                                     Particle.getRandomNumber( 0, 1 ),
                                     Particle.getRandomNumber( 0, 1 ) );

        particle.resultantForce.scale( 20 );
        particle.velocity.set( 0, 0, 0 );
        particle.setAlpha( ( float ) Math.random() );

        return true;
    }

    public boolean isAlive( Particle particle )
    {
        return particle.getCycleAge() < 10;
    }
}
