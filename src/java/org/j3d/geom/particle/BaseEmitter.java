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
import java.util.Random;

// Local imports
// None

/**
 * Common base class that implements the {@link ParticleInitializer} interface
 * for all emitters.
 * <p>
 *
 * Mass and surface area are initialised to be zero. Lifetime variation is
 * always set to zero, unless changed by the user.
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
public abstract class BaseEmitter implements ParticleInitializer
{
    /** Message when the particle lifetime is less than or equal to zero */
    private static final String LIFETIME_ERR =
        "Particle lifetime must be greater than zero";

    /** Message when the mass value is negative */
    private static final String NEG_MASS_ERR = "Mass must be >= zero";

    /** Message when the surface area set is negative */
    private static final String NEG_AREA_ERR = "Surface area must be >= zero";

    /** Message when the lifetime variation is out of limits */
    private static final String LIFE_VAR_ERR =
        "Particle lifetime variation must be within [0,1]";

    /** Base lifetime in milliseconds */
    protected int lifetime;

    /** The maximum number of particles to generate */
    protected int particleCount;

    /** Initial colour to make all particles */
    protected float[] color;

    /** The initial speed of the particles. Should be >= 0 */
    protected float speed;

    /** Amount of variation on the randomness */
    protected float variation;

    /** The amount of lifetime variation permitted */
    protected float lifetimeVariation;

    /** Initial mass that is imparted to all particles */
    protected float initialMass;

    /** Initial surface area given to all particles */
    protected float surfaceArea;

    /** Random number generator for sign values */
    protected Random randomiser;

    /**
     * Construct a new default emitter, with everything initialized to
     * zeroes, except colour, which is white.
     */
    protected BaseEmitter()
    {
        color = new float[] { 1, 1, 1, 1 };
        randomiser = new Random();
    }

    /**
     * Construct a new emitter instance for a point emitter. The number of
     * particles to create each frame is driven from the maximum particle count
     * divided by the average lifetime.
     *
     * @param maxTime The time length of the particles to exist in milliseconds
     * @param maxParticleCount The maximum number of particles to manage
     * @param color The initial color of particles (4 component)
     * @param speed The speed of the particls to start with
     * @param variation The amount of variance for the initial values
     */
    protected BaseEmitter(int maxTime,
                          int maxParticleCount,
                          float[] color,
                          float speed,
                          float variation)
    {
        this();

        if(maxTime <= 0)
            throw new IllegalArgumentException(LIFETIME_ERR);

        lifetime = maxTime;
        particleCount = maxParticleCount;

        this.color[0] = color[0];
        this.color[1] = color[1];
        this.color[2] = color[2];
        this.color[3] = color[3];

        this.speed = speed;
        this.variation = variation;

        initialMass = 0;
        surfaceArea = 0;
    }

    //---------------------------------------------------------------
    // Methods defined by ParticleInitializer
    //---------------------------------------------------------------

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
        particleCount = maxCount;
    }

    /**
     * Fetch the current value of the maximum particle count.
     *
     * @return A value >= 0
     */
    public int getMaxParticleCount()
    {
        return particleCount;
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
        if(time <= 0)
            throw new IllegalArgumentException(LIFETIME_ERR);

        lifetime = time;
    }

    /**
     * Get the current maximum lifetime of the particles. Time is represented in
     * milliseconds.
     *
     * @return The current particle lifetime, in milliseconds
     */
    public int getParticleLifetime()
    {
        return lifetime;
    }

    /**
     * Change the variation factor for the emitted particles. This will only
     * effect particles created after this is set, and not before. Variation
     * values are limited to [0,1].
     *
     * @param variation The new variation amount
     * @throws IllegalArgumentException The variation amount was within [0,1]
     */
    public void setLifetimeVariation(float variation)
        throws IllegalArgumentException
    {
        if(variation < 0 || variation > 1)
            throw new IllegalArgumentException(LIFE_VAR_ERR);

        lifetimeVariation = variation;
    }

    /**
     * Get the amount of variation in the lifetime of the particles
     * generated.
     *
     * @return The current lifetime variation factor in the range [0,1]
     */
    public float getLifetimeVariation()
    {
        return lifetimeVariation;
    }

    /**
     * Change the variation factor for the emitted particles. This will only
     * effect particles created after this is set, and not before. Variation
     * may be negative, but results are unknown if it is. Works best if the
     * variation is limited to [0,1].
     *
     * @param variation The new variation amount
     */
    public void setParticleVariation(float variation)
    {
        this.variation = variation;
    }

    /**
     * Get the amount of variation currently permitted in the particles.
     *
     * @return The current particle variation factor
     */
    public float getParticleVariation()
    {
        return variation;
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
     * Get the value of the initial colour that particles are set to. The array
     * should be length 4.
     *
     * @param val An array of length 4 to copy the internal values into
     */
    public void getColor(float[] val)
    {
        val[0] = color[0];
        val[1] = color[1];
        val[2] = color[2];
        val[3] = color[3];
    }

    /**
     * Change the apparent surface area. Surface area is measured in square
     * metres. Surface area must be non-negative otherwise an exception will be
     * generated.
     *
     * @param area The new surface area value to use, in metres squared
     * @throws IllegalArgumentException The surface area value was negative
     */
    public void setSurfaceArea(float area)
        throws IllegalArgumentException
    {
        if(area < 0)
            throw new IllegalArgumentException(NEG_AREA_ERR);

        surfaceArea = area;
    }

    /**
     * Get the current surface area assigned to particles.
     *
     * @return A value greater than or equal to zero
     */
    public float getSurfaceArea()
    {
        return surfaceArea;
    }

    /**
     * Change the mass of the particle. Mass is measured in kilograms. Mass
     * must be non-negative otherwise an exception will be generated.
     *
     * @param mass The mass of an individual particle
     * @throws IllegalArgumentException The mass value was negative
     */
    public void setMass(float mass) throws IllegalArgumentException
    {
        if(mass < 0)
            throw new IllegalArgumentException(NEG_MASS_ERR);

        initialMass = mass;
    }

    /**
     * Get the current mass assigned to each particle.
     *
     * @return A non-negative value representing the mass
     */
    public float getMass()
    {
        return initialMass;
    }

    /**
     * Change the initial speed that the particles are endowed with. Some
     * emitters may need to have a direction value as well to determine the
     * velocity that the particles are emitted with. Speed may be any value.
     * Negatives are just treated like starting the particles in the opposite
     * direction to those of positive speed. A speed of zero has all particles
     * starting stationary.
     *
     *
     * @param speed The magnitude of the speed to use
     */
    public void setSpeed(float speed)
    {
        this.speed = speed;
    }

    /**
     * Get the current speed that particles are initialised with.
     *
     * @return A value of the speed
     */
    public float getSpeed()
    {
        return speed;
    }
}
