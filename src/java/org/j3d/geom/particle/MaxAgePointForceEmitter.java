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
 * and clearing resultant force and velocity and setting
 * a new randomized force on the Particle.
 *
 * @author Daniel Selman
 * @version $Revision: 1.1 $
 */
public class MaxAgePointForceEmitter implements ParticleInitializer
{
    int maxAge;
    double originX;
    double originY;
    double originZ;

    double forceX;
    double forceY;
    double forceZ;

    double random = 0.5;

    public MaxAgePointForceEmitter( int maxAge, double x, double y, double z,
                                    double forceX, double forceY, double forceZ )
    {
        this.maxAge = maxAge;
        originX = x;
        originY = y;
        originZ = z;

        this.forceX = forceX;
        this.forceY = forceY;
        this.forceZ = forceZ;
    }

    public boolean initialize( Particle particle )
    {
        particle.setColor( 1, 1, 1, ( float ) Math.random() );
        particle.setCycleAge( ( int ) ( Math.random() * maxAge ) );

        particle.setPositionAndPrevious( originX, originY, originZ );
        particle.resultantForce.set( Particle.getRandomNumber( forceX, random ),
                                     Particle.getRandomNumber( forceY, random ),
                                     Particle.getRandomNumber( forceZ, random ) );
        particle.velocity.set( 0, 0, 0 );
        return true;
    }

    public boolean isAlive( Particle particle )
    {
        return ( particle.getCycleAge() < maxAge );
    }
}
