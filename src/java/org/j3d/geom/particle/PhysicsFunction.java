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
 * Movement function that performs basic F=M.
 * <p>
 * A movement control on the Particles
 * based on their applied resultantForce. A percentage
 * of the resultantForce is "lost" prior to position
 * calculation to simulate friction/drag.
 * <p>
 * This ParticleFunction should be added to the ParticleSystem
 * *after* any MovementFunctions which are applying
 * forces to Particles.
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
 * @author Daniel Selman
 * @version $Revision: 1.5 $
 */
public class PhysicsFunction implements ParticleFunction
{
    private static final int RECALC_INTERVAL = 50;

    /**
     * The assumed initial interval between calls to the PhysicsFunction
     * this delta is recalculated every RECALC_INTERVAL frames
     */
    private double deltaTime;

    /** Current position */
    private float[] position;

    private Vector3d acceleration;

    /** percentage of force still avalailable after friction lossage */
    private double frictionForce;

    /** percentage of velocity still avilable */
    private double frictionVelocity;

    private long startTime;
    private static long invokeCount = 0;

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;

    public PhysicsFunction()
    {
        startTime = System.currentTimeMillis();
        enabled = true;

        position = new float[3];
        acceleration = new Vector3d();

        frictionForce = 0.90;
        frictionVelocity = 0.95;
        deltaTime = 1.0 / 40.0;
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
        // review DCS - we don't check ps,
        // so if this PhysicsFunction is shared between
        // multiple ParticleSystems we will get bogus results
        invokeCount++;

        if(invokeCount == RECALC_INTERVAL)
        {
            long now = System.currentTimeMillis();
            double elapsedSeconds = (double)(now - startTime) / 1000;
            deltaTime =  (double)elapsedSeconds / RECALC_INTERVAL;

            System.out.println("PhysicsFunction FPS: " + (int) 1.0 / deltaTime);

            // reset counters
            invokeCount = 0;
            startTime = now;
        }

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
        acceleration.set(particle.resultantForce);
        // get the change in velocity
        acceleration.scale(deltaTime / particle.mass);
        particle.velocity.add(acceleration);

        // get the change in position
        particle.getPosition(position);
        acceleration.set(particle.velocity);
        acceleration.scale(deltaTime);

        position[0] += acceleration.x;
        position[1] += acceleration.y;
        position[2] += acceleration.z;

        // update the position
        particle.setPosition(position[0], position[1], position[2]);

        return true;
    }
}
