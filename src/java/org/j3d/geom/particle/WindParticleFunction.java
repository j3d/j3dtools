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
 * WindParticleFunction models a directional wind source.
 * The wind has a direction and linearly attentuates
 * in strength perpendicular to the direction vector.
 * It applies forces to the particles proportional to their
 * surface area.
 * In addition it can be further parametized with a gustiness
 * and swirliness, controlling the strength and direction of the
 * wind force.
 *
 * @author Daniel Selman
 * @version $Revision: 1.1 $
 */
public class WindParticleFunction implements ParticleFunction
{
    // orgin of the wind
    private Point3d windStart = new Point3d( 0, -1000, 0 );
    private Point3d windEnd = new Point3d( 0, 1000, 0 );
    private Vector3d ab;
    private Vector3d ap = new Vector3d();
    private Vector3d crossAbAp = new Vector3d();
    private double abLength;
    private double attenuationStart = 0.2;
    private double attenuationEnd = 0.8;

    // force
    private Vector3d forcePerSquareMeter = new Vector3d( 0.0000, 0.020, 0.00000 );
    private Vector3d currentForcePerSquareMeter = new Vector3d();

    // percentages
    private double gustiness = 0.4;

    private double swirlinessX = -0.0005;
    private double swirlinessY = 0.0005;
    private double swirlinessZ = -0.0005;

    public WindParticleFunction( Vector3d forcePerSquareMeter )
    {
        ab =
            new Vector3d(
                    windEnd.x - windStart.x,
                    windEnd.y - windStart.y,
                    windEnd.z - windStart.z );
        abLength = ab.length();

        this.forcePerSquareMeter = forcePerSquareMeter;
    }

    public boolean onUpdate( ParticleSystem ps )
    {
       return true;
    }

    public boolean apply( Particle particle )
    {
        // calculate the current wind speed
        currentForcePerSquareMeter.x =
                Particle.getRandomNumber( forcePerSquareMeter.x, forcePerSquareMeter.x * gustiness );
        currentForcePerSquareMeter.y =
                Particle.getRandomNumber( forcePerSquareMeter.y, forcePerSquareMeter.y * gustiness );
        currentForcePerSquareMeter.z =
                Particle.getRandomNumber( forcePerSquareMeter.z, forcePerSquareMeter.z * gustiness );

        // calculate distance of the particle from the plane
        particle.getPosition( ap );
        ap.sub( windStart );

        crossAbAp.cross( ab, ap );
        double distance = crossAbAp.length() / abLength;

        if ( distance > attenuationStart )
        {
            if ( distance <= attenuationEnd )
            {
                currentForcePerSquareMeter.scale(
                        ( distance - attenuationStart ) / ( attenuationEnd - attenuationStart ) );
            }
            else
            {
                currentForcePerSquareMeter.set( 0, 0, 0 );
            }
        }

        if ( distance < attenuationEnd )
        {
            // apply the swirliness
            currentForcePerSquareMeter.x += Particle.getRandomNumber( 0, swirlinessX );
            currentForcePerSquareMeter.y += Particle.getRandomNumber( 0, swirlinessY );
            currentForcePerSquareMeter.z += Particle.getRandomNumber( 0, swirlinessZ );
            currentForcePerSquareMeter.scale( particle.surfaceArea );
            particle.resultantForce.add( currentForcePerSquareMeter );
        }

        return false;
    }
}