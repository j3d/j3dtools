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
 * Emits particles at a fixed point in space which only have a maximum time
 * limit and color set - all other values are zeroed out.
 *
 * @author Daniel Selman
 * @version $Revision: 1.3 $
 */
public class MaxAgePointEmitter implements ParticleInitializer
{
    /**
     * Number of frames of zero consecutive zero time deltas before we should
     * force the issue of another particle.
     */
    private static final int DELTA_LIMIT = 5;

    /** Base lifetime in milliseconds */
    private int lifetime;

    /** Number of particles to generate per millisecond */
    private int particlesPerMs;

    /** The origin to generate the particles at */
    private float[] origin;

    /** Initial colour to make all particles */
    private float[] color;

    /** Amount of variation on the randomness */
    private float variation;

    /** Initial mass that is imparted to all particles */
    private double initialMass;

    /** Initial surface area given to all particles */
    private double surfaceArea;

    /** The initial velocity imparted to the particles */
    private float[] initialVelocity;

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
     * @param velocity The speed of the particls to start with
     * @param variation The amount of variance for the initial values
     */
    public MaxAgePointEmitter(int lifetime,
                              int maxParticleCount,
                              float[] position,
                              float[] color,
                              float variation)
    {
        this.lifetime = lifetime;

        // Particle per millisecond is just averaged over the base lifetime
        // Uses the integer division for rounding purposes as we really don't
        // care for high-accuracy in this number - it's just a guide.
        particlesPerMs = maxParticleCount / lifetime;

        this.origin = new float[3];
        this.origin[0] = position[0];
        this.origin[1] = position[1];
        this.origin[2] = position[2];

        this.color = new float[4];
        this.color[0] = color[0];
        this.color[1] = color[1];
        this.color[2] = color[2];
        this.color[3] = color[3];

        this.variation = variation;

        initialMass = 0.0000001;
        surfaceArea = 0.0004;
        initialVelocity = new float[3];
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
        float rnd = 1 - (float)Math.random() * variation;
        particle.setColor(color[0], color[1], color[2], color[3] * rnd);

        rnd = 1 - (float)Math.random() * variation;
        particle.setCycleTime((int)(lifetime * rnd));

        particle.setPositionAndPrevious(origin[0], origin[1], origin[2]);

        particle.resultantForce.set(0, 0, 0);
        particle.velocity.set(0, 0, 0);

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
}
