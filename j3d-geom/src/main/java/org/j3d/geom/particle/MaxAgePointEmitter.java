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
 * Emits particles at a fixed point in space which only have a maximum time
 * limit and color set - all other values are zeroed out.
 *
 * @author Justin Couch, Daniel Selman
 * @version $Revision: 2.0 $
 */
public class MaxAgePointEmitter extends BaseEmitter
{
    /**
     * Number of frames of zero consecutive zero time deltas before we should
     * force the issue of another particle.
     */
    private static final int DELTA_LIMIT = 5;

    /** Number of particles to generate per millisecond */
    private float particlesPerMs;

    /** The origin to generate the particles at */
    private float[] origin;

    /**
     * The zero time delta counter - used to make sure particles are still
     * issued on really high-speed machines.
     */
    private int zeroDeltaCounter;

    /**
     * Used to handle the case when there is a low particle count relative to
     * the lifetime. This is the case when there is less than one particle per
     * millisecond to be generated. Saves up the previous elapsed time since
     * the last particle was generated until we get to the point where one has
     * to be generated.
     */
    private int elapsedZeroParticleTime;

    /**
     * Construct a new emitter instance for a point emitter. The number of
     * particles to create each frame is driven from the maximum particle count
     * divided by the average lifetime.
     *
     * @param maxTime The time length of the particles to exist in milliseconds
     * @param maxParticleCount The maximum number of particles to manage
     * @param position The emitting position in the local space
     * @param color The initial color of particles (4 component)
     * @param variation The amount of variance for the initial values
     */
    public MaxAgePointEmitter(int maxTime,
                              int maxParticleCount,
                              float[] position,
                              float[] color,
                              float variation)
    {
        super(maxTime, maxParticleCount, color, 0, variation);

        // Particle per millisecond is just averaged over the base lifetime
        // Uses the integer division for rounding purposes as we really don't
        // care for high-accuracy in this number - it's just a guide.
        particlesPerMs = (lifetime == 0) ? 1 : (float)particleCount / lifetime;
        elapsedZeroParticleTime = 0;

        this.origin = new float[3];
        this.origin[0] = position[0];
        this.origin[1] = position[1];
        this.origin[2] = position[2];
    }

    /**
     * Adjust the maximum number of particles that this initializer is going to
     * work with. This should not normally be called by the end user. The
     * particle system that this initializer is registered with will call this
     * method when it's corresponding method is called.
     *
     * @param maxCount The new maximum particle count to use
     */
    public void setMaxParticleCount(int maxCount)
    {
        super.setMaxParticleCount(maxCount);

        particlesPerMs = (lifetime == 0) ? 1 : (float)particleCount / lifetime;
    }

    /**
     * Change the maximum lifetime of the particles. The lifetime of particles
     * is defined in milliseconds, and must be positive.
     *
     * @param time The new lifetime, in milliseconds
     * @throws IllegalArgumentException The lifetime is zero or negative
     */
    public void setParticleLifetime(int time)
        throws IllegalArgumentException
    {
        super.setParticleLifetime(time);

        particlesPerMs = (lifetime == 0) ? 1 : (float)particleCount / lifetime;
    }

    /**
     * The number of particles that should be created and initialised this
     * frame. This is called once per frame by the particle system manager.
     * Sends all particles initialially and nothing after that.
     *
     * @param timeDelta The delta between the last frame and this one in
     *    milliseconds
     * @return The number of particles to create
     */
    public int numParticlesToCreate(int timeDelta)
    {
        int ret_val = 0;

        switch(timeDelta)
        {
            case -1:
                // do nothing
                break;

            case 0:
                if(zeroDeltaCounter == DELTA_LIMIT)
                {
                    ret_val = 1;
                    zeroDeltaCounter = 0;
                }
                else
                    zeroDeltaCounter++;
                break;

            default:
                zeroDeltaCounter = 0;
                ret_val = (int)(particlesPerMs * timeDelta);
                if(ret_val == 0)
                {
                    elapsedZeroParticleTime += timeDelta;

                    ret_val = (int)(particlesPerMs * elapsedZeroParticleTime);
                    if(ret_val != 0)
                        elapsedZeroParticleTime = 0;
                }
        }

        return ret_val;
    }

    /**
     * Initialize a particle based on the rules defined by this initializer.
     * The particle system may choose to re-initialise previously dead
     * particles. The implementation should not care whether the particle was
     * previously in existance or not.
     *
     * @param particle The particle instance to initialize
     * @return true if the ParticleSytem should keep running
     */
    public boolean initialize(Particle particle)
    {
        float rnd = 1 - randomiser.nextFloat() * variation;
        particle.setColor(color[0], color[1], color[2], color[3] * rnd);

        rnd = 1 - randomiser.nextFloat() * lifetimeVariation;
        particle.setCycleTime((int)(lifetime * rnd));
        particle.setPosition(origin[0], origin[1], origin[2]);
        particle.resultantForce.set(0, 0, 0);
        particle.velocity.set(0, 0, 0);
        particle.setMass(initialMass);
        particle.setSurfaceArea(surfaceArea);

        return true;
    }

    /**
     * Change the basic position that the particles are being generated from.
     *
     * @param x The x component of the location
     * @param y The y component of the location
     * @param z The z component of the location
     */
    public void setPosition(float x, float y, float z)
    {
        origin[0] = x;
        origin[1] = y;
        origin[2] = z;
    }
}
