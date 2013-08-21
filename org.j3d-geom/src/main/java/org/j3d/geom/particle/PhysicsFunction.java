/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

// External imports
// None

// Local imports
// None

/**
 * Movement function that performs basic F=MA calculations for all the
 * physics that have been set up in this frame by other functions.
 * <p>
 * A movement control on the Particles based on their applied resultantForce.
 * <p>
 * This ParticleFunction should be added to the ParticleSystem <i>after</i>
 * any movement functions which are applying forces to Particles.
 * <p>
 * Some basic physics equations:
 * <p>
 *
 * acceleration = force / mass;<br>
 * velocity += acceleration * time_diff;<br>
 * pos += velocity * time_diff;<br>
 * ke = 0.5 * m * v * v<br>
 * p.e (grav) = m * g * h<br>
 * p.e (spring) = 0.5 * k * x * x (x = amount of compression)<br>
 * total mech energy = k.e + p.e. (grav) + p.e. (spring)<br>
 * power = work / time<br>
 * power = force * displacement / time<br>
 * f = m * a<br>
 * f = m * delta v / t<br>
 *
 * @author Daniel Selman, Justin Couch
 * @version $Revision: 2.1 $
 */
public class PhysicsFunction implements ParticleFunction
{
    /**
     * The assumed initial interval between calls to the PhysicsFunction
     * this delta is recalculated every RECALC_INTERVAL frames
     */
    private float deltaTime;

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;

    /**
     * Create a new default physics function to apply to particles.
     */
    public PhysicsFunction()
    {
        enabled = true;

        deltaTime = 1.0f / 40.0f;
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
     * @param deltaT The elapsed time in milliseconds since the last frame
     * @return true if this should force another update after this one
     */
    public boolean newFrame(int deltaT)
    {
        deltaTime = deltaT * 0.001f;

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
        if(particle.mass != 0)
        {
            // A = force / mass
            float a_x = particle.resultantForce.x * deltaTime / particle.mass;
            float a_y = particle.resultantForce.y * deltaTime / particle.mass;
            float a_z = particle.resultantForce.z * deltaTime / particle.mass;

            particle.velocity.x += a_x;
            particle.velocity.y += a_y;
            particle.velocity.z += a_z;
        }

        // get the change in position
        // S' = S + ut
        float x = particle.position.x + particle.velocity.x * deltaTime;
        float y = particle.position.y + particle.velocity.y * deltaTime;
        float z = particle.position.z + particle.velocity.z * deltaTime;

        // update the position
        particle.setPosition(x, y, z);

        return true;
    }
}
