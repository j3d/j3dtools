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
import javax.vecmath.Vector3f;

/**
 * and limits the maximum age based on wall-clock existance time.
 * Generates particles that emit in a specific direction with varying velocity
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class MaxTimePointEmitter implements ParticleInitializer
{
    /**
     * Number of frames of zero consecutive zero time deltas before we should
     * force the issue of another particle.
     */
    private static final int DELTA_LIMIT = 5;

    /** Base lifetime in milliseconds */
    private int lifetime;

    /** Number of particles to generate per millisecond */
    private float particlesPerMs;

    /** The origin to generate the particles at */
    private float[] origin;

    /**
     * The calculated initial velocity of the particles using the direction and
     * velocity variables.
     */
    private float[] initialVelocity;

    /** Initial colour to make all particles */
    private float[] color;

    /** The initial direction of the particles */
    private float[] direction;

    /** The initial velocity of the particles. Should be >= 0 */
    private float velocity;

    /** Amount of variation on the randomness */
    private float variation;

    /** Initial mass that is imparted to all particles */
    private double initialMass;

    /** Initial surface area given to all particles */
    private double surfaceArea;

    /**
     * The zero time delta counter - used to make sure particles are still
     * issued on really high-speed machines.
     */
    private int zeroDeltaCounter;

    /**
     * Construct a new emitter instance for a point emitter. The number of
     * particles to create each frame is driven from the maximum particle count
     * divided by the average lifetime.
     *
     * @param maxTime The time length of the particles to exist in milliseconds
     * @param maxParticleCount The maximum number of particles to manage
     * @param position The emitting position in the local space
     * @param color The initial color of particles (4 component)
     * @param direction The initial direction the particles are sent
     * @param velocity The speed of the particls to start with
     * @param variation The amount of variance for the initial values
     */
    public MaxTimePointEmitter(int lifetime,
                               int maxParticleCount,
                               float[] position,
                               float[] color,
                               float[] direction,
                               float velocity,
                               float variation)
    {
        this.lifetime = lifetime;

        // Particle per millisecond is just averaged over the base lifetime
        // Uses the integer division for rounding purposes as we really don't
        // care for high-accuracy in this number - it's just a guide.
        particlesPerMs = (float)maxParticleCount / lifetime;

        initialVelocity = new float[3];

        this.origin = new float[3];
        this.origin[0] = position[0];
        this.origin[1] = position[1];
        this.origin[2] = position[2];

        this.direction = new float[3];
        this.direction[0] = direction[0];
        this.direction[1] = direction[1];
        this.direction[2] = direction[2];

        this.color = new float[4];
        this.color[0] = color[0];
        this.color[1] = color[1];
        this.color[2] = color[2];
        this.color[3] = color[3];

        this.velocity = velocity;
        this.variation = variation;

        updateVelocity();

        initialMass = 0.0000001;
        surfaceArea = 0.0004;
    }

    /**
     * The number of particles that should be created and initialised this
     * frame. This is called once per frame by the particle system manager.
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
        particle.setColor(color[0], color[1], color[2], color[3]);
        particle.setCycleTime((int) (Math.random() * lifetime));

        particle.setPositionAndPrevious(origin[0], origin[1], origin[2]);
        particle.resultantForce.set(0, 0, 0);

        // Set up the initial velocity using a bit of randomness. Uses the same
        // scale factor in each component. If more randomness is desired, this
        // could be used on each component of the velocity.
        float rnd = (float)Math.random() * variation;

        float v_x = initialVelocity[0] * rnd;
        float v_y = initialVelocity[1] * rnd;
        float v_z = initialVelocity[2] * rnd;

        particle.velocity.set(v_x, v_y, v_z);
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

    /**
     * Set the initial color that that the particle is given. If the emitter does
     * not support the alpha channel, ignore the parameter.
     *
     * @param r The red component of the color
     * @param g The green component of the color
     * @param b The blue component of the color
     * @param alpha The alpha component of the color
     */
    public void setColor(float r, float g, float b, float alpha)
    {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = alpha;
    }

    /**
     * Change the apparent surface area. Surface area is measured in square
     * metres.
     *
     * @param area surface area
     */
    public void setSurfaceArea(double area)
    {
        surfaceArea = area;
    }

    /**
     * Change the mass of the particle. Mass is measured in kilograms.
     *
     * The mass of an individual particle
     */
    public void setMass(double mass)
    {
        initialMass = mass;
    }

    /**
     * Change the initial velocity that the particles are endowed with.
     *
     * @param x The x component of the velocity
     * @param y The y component of the velocity
     * @param z The z component of the velocity
     */
    public void setVelocity(float x, float y, float z)
    {
        initialVelocity[0] = x;
        initialVelocity[1] = y;
        initialVelocity[2] = z;
    }

    /**
     * Change the base velocity of the particles without changing the
     * emission direction. The velocity should be >= 0.
     *
     * @param v The new velocity to use
     */
    public void setVelocity(float v)
    {
        velocity = v;
        updateVelocity();
    }

    /**
     * Change the direction that particles are being emitted from by this this
     * emitter. The base velocity remains unchanged.
     *
     * @param direction The new direction to send the particles
     */
    public void setEmitDirection(float[] direction)
    {
        this.direction[0] = direction[0];
        this.direction[1] = direction[1];
        this.direction[2] = direction[2];
        updateVelocity();
    }

    /**
     * Convenience method to update the velocity array
     */
    private void updateVelocity()
    {
        if(velocity == 0)
        {
            initialVelocity[0] = 0;
            initialVelocity[1] = 0;
            initialVelocity[2] = 0;
        }
        else
        {
            Vector3f vec = new Vector3f(direction);
            vec.normalize();
            vec.scale(velocity);

            initialVelocity[0] = vec.x;
            initialVelocity[1] = vec.y;
            initialVelocity[2] = vec.z;
        }
    }
}
