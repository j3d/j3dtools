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
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

// Application specific imports
// None

/**
 * WindParticleFunction models a directional wind source.
 * <p>
 * The wind has a direction and linearly attentuates
 * in strength perpendicular to the direction vector.
 * It applies forces to the particles proportional to their
 * surface area.
 * In addition it can be further parametized with a gustiness
 * and swirliness, controlling the strength and direction of the
 * wind force.
 *
 * @author Daniel Selman
 * @version $Revision: 1.4 $
 */
public class WindParticleFunction implements ParticleFunction
{
    // orgin of the wind
    private Point3d windStart;
    private Point3d windEnd;
    private Vector3d ab;
    private Vector3d ap;
    private Vector3d crossAbAp;
    private float[] position;

    private double abLength;
    private double attenuationStart;
    private double attenuationEnd;

    // force
    private Vector3d forcePerSquareMeter;
    private Vector3d currentForcePerSquareMeter;

    // percentages
    private double gustiness;

    private double swirlinessX;
    private double swirlinessY;
    private double swirlinessZ;

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;

    /**
     * Construct a new wind particle function
     */
    public WindParticleFunction(Vector3d forcePerSquareMeter)
    {
        windStart = new Point3d(0, -1000, 0);
        windEnd = new Point3d(0, 1000, 0);
        crossAbAp = new Vector3d();
        ap = new Vector3d();
        position = new float[3];

        attenuationStart = 0.2;
        attenuationEnd = 0.8;

        // force
        forcePerSquareMeter = new Vector3d(0.0000, 0.020, 0.00000);
        currentForcePerSquareMeter = new Vector3d();

        // percentages
        gustiness = 0.4;

        swirlinessX = -0.0005;
        swirlinessY = 0.0005;
        swirlinessZ = -0.0005;

        ab = new Vector3d(windEnd.x - windStart.x,
                          windEnd.y - windStart.y,
                          windEnd.z - windStart.z);

        abLength = ab.length();

        this.forcePerSquareMeter = forcePerSquareMeter;

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
     * @param ps The particle system that is being updated
     * @return true if this should force another update after this one
     */
    public boolean newFrame()
    {
        // calculate the current wind speed
        currentForcePerSquareMeter.x =
                Math.random() * forcePerSquareMeter.x * gustiness;
        currentForcePerSquareMeter.y =
                Math.random() * forcePerSquareMeter.y * gustiness;
        currentForcePerSquareMeter.z =
                Math.random() * forcePerSquareMeter.z * gustiness;

       return true;
    }

    /**
     * Apply this function to the given particle right now.
     *
     * @param particle The particle to apply the function to
     * @return true if the particle has changed, false otherwise
     */
    public boolean apply(Particle particle)
    {
        // calculate distance of the particle from the plane
        particle.getPosition(position);
        ap.x = position[0] - windStart.x;
        ap.y = position[1] - windStart.y;
        ap.z = position[2] - windStart.z;

        crossAbAp.cross(ab, ap);
        double distance = crossAbAp.length() / abLength;

        if(distance > attenuationStart)
        {
            if(distance <= attenuationEnd)
            {
                currentForcePerSquareMeter.scale(
                        (distance - attenuationStart) /
                        (attenuationEnd - attenuationStart));
            }
            else
            {
                currentForcePerSquareMeter.set(0, 0, 0);
            }
        }

        if(distance < attenuationEnd)
        {
            // apply the swirliness
            currentForcePerSquareMeter.x += Math.random() * swirlinessX;
            currentForcePerSquareMeter.y += Math.random() * swirlinessY;
            currentForcePerSquareMeter.z += Math.random() * swirlinessZ;
            currentForcePerSquareMeter.scale(particle.surfaceArea);
            particle.resultantForce.add(currentForcePerSquareMeter);
        }

        return true;
    }
}