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
import java.util.Random;

// Application specific imports
// None

/**
 * Generates particles that explode from a point in space, in any direction.
 * <p>
 * All particles are generated in the initial valuation and then no further
 * particles are generated.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class ExplosionPointEmitter implements ParticleInitializer
{
    /** Base lifetime in milliseconds */
    private int lifetime;

    /** Number of particles to generate */
    private int particleCount;

    /** The origin to generate the particles at */
    private float[] origin;

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

    /** Random number generator for sign values */
    private Random randomiser;

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
    public ExplosionPointEmitter(int lifetime,
                                 int maxParticleCount,
                                 float[] position,
                                 float[] color,
                                 float velocity,
                                 float variation)
    {
        this.lifetime = lifetime;
        particleCount = maxParticleCount;

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

        initialMass = 0.0000001;
        surfaceArea = 0.0004;

        randomiser = new Random();
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
        int ret_val = particleCount;
        particleCount = 0;

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
        // Vary the alpha channel of the color a bit too
        float rnd = 1 - randomiser.nextFloat() * variation;
        particle.setColor(color[0], color[1], color[2], color[3] * rnd);

        rnd = 1 - randomiser.nextFloat() * variation;
        particle.setCycleTime((int)(lifetime * rnd));

        particle.setPositionAndPrevious(origin[0], origin[1], origin[2]);
        particle.resultantForce.set(0, 0, 0);

        // Set up the initial velocity using a bit of randomness. Uses the same
        // scale factor in each component. If more randomness is desired, this
        // could be used on each component of the velocity.
        rnd = 1 - randomiser.nextFloat() * variation;

        float x_sign = randomiser.nextBoolean() ? 1 : -1;
        float y_sign = randomiser.nextBoolean() ? 1 : -1;
        float z_sign = randomiser.nextBoolean() ? 1 : -1;

        float v_x = rnd * velocity * randomiser.nextFloat() * x_sign;
        float v_y = rnd * velocity * randomiser.nextFloat() * y_sign;
        float v_z = rnd * velocity * randomiser.nextFloat() * z_sign;

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
        // ignored by this implementation
    }
}
