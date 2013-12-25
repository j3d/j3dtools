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
 * The ParticleInitializer is registered with a ParticleSystem and is
 * responsible for creating and initialising the particles.
 *
 * @author Justin Couch, Daniel Selman
 * @version $Revision: 2.0 $
 */
public interface ParticleInitializer
{
    /**
     * Adjust the maximum number of particles that this initializer is going to
     * work with. This should not normally be called by the end user. The
     * particle system that this initializer is registered with will call this
     * method when it's corresponding method is called.
     *
     * @param maxCount The new maximum particle count to use
     */
    public void setMaxParticleCount(int maxCount);

    /**
     * Fetch the current value of the maximum particle count.
     *
     * @return A value >= 0
     */
    public int getMaxParticleCount();

    /**
     * Change the maximum lifetime of the particles. The lifetime of particles
     * is defined in milliseconds, and must be positive.
     *
     * @param time The new lifetime, in seconds
     * @throws IllegalArgumentException The lifetime is zero or negative
     */
    public void setParticleLifetime(int time)
        throws IllegalArgumentException;

    /**
     * Get the current maximum lifetime of the particles. Time is represented in
     * milliseconds.
     *
     * @return The current particle lifetime, in milliseconds
     */
    public int getParticleLifetime();

    /**
     * Change the variation factor for the emitted particles. This will only
     * effect particles created after this is set, and not before. Variation
     * values are limited to [0,1].
     *
     * @param variation The new variation amount
     * @throws IllegalArgumentException The variation amount was within [0,1]
     */
    public void setLifetimeVariation(float variation)
        throws IllegalArgumentException;

    /**
     * Get the amount of variation in the lifetime of the particles
     * generated.
     *
     * @return The current lifetime variation factor in the range [0,1]
     */
    public float getLifetimeVariation();

    /**
     * Change the variation factor for the particles' properties, and does not
     * effect the lifetime variation. This will only effect particles created
     * after this is set, and not before. Variation may be negative, but
     * results are unknown if it is. Works best if the variation is limited to
     * [0,1].
     *
     * @param variation The new variation amount
     */
    public void setParticleVariation(float variation);

    /**
     * Get the amount of variation in the properties of the particles
     * generated. This does not effect the lifetime, which is set by a
     * different method.
     *
     * @return The current particle variation factor
     */
    public float getParticleVariation();

    /**
     * The number of particles that should be created and initialised this
     * frame. This is called once per frame by the particle system manager.
     * If this is the first frame, the timeDelta value given will be -1.
     *
     * @param timeDelta The delta between the last frame and this one in
     *    milliseconds
     * @return The number of particles to create
     */
    public int numParticlesToCreate(int timeDelta);

    /**
     * Initialize a particle based on the rules defined by this initializer.
     * The particle system may choose to re-initialise previously dead
     * particles. The implementation should not care whether the particle was
     * previously in existance or not.
     *
     * @param particle The particle instance to initialize
     * @return true if the ParticleSytem should keep running
     */
    public boolean initialize(Particle particle);

    /**
     * Set the initial color that that the particle is given. If the emitter does
     * not support the alpha channel, ignore the parameter.
     *
     * @param r The red component of the color
     * @param g The green component of the color
     * @param b The blue component of the color
     * @param alpha The alpha component of the color
     */
    public void setColor(float r, float g, float b, float alpha);

    /**
     * Get the value of the initial colour that particles are set to. The array
     * should be length 4.
     *
     * @param val An array of length 4 to copy the internal values into
     */
    public void getColor(float[] val);

    /**
     * Change the apparent surface area. Surface area is measured in square
     * metres. Surface area must be non-negative otherwise an exception will be
     * generated.
     *
     * @param area The new surface area value to use, in metres squared
     * @throws IllegalArgumentException The surface area value was negative
     */
    public void setSurfaceArea(float area)
        throws IllegalArgumentException;

    /**
     * Get the current surface area assigned to particles.
     *
     * @return A value greater than or equal to zero
     */
    public float getSurfaceArea();

    /**
     * Change the mass of the particle. Mass is measured in kilograms. Mass
     * must be non-negative otherwise an exception will be generated.
     *
     * @param mass The mass of an individual particle
     * @throws IllegalArgumentException The mass value was negative
     */
    public void setMass(float mass) throws IllegalArgumentException;

    /**
     * Get the current mass assigned to each particle.
     *
     * @return A non-negative value representing the mass
     */
    public float getMass();

    /**
     * Change the initial speed that the particles are endowed with. Some
     * emitters may need to have a direction value as well to determine the
     * velocity that the particles are emitted with. Speed may be any value.
     * Negatives are just treated like starting the particles in the opposite
     * direction to those of positive speed. A speed of zero has all particles
     * starting stationary.
     *
     * @param speed The magnitude of the speed to use
     */
    public void setSpeed(float speed);

    /**
     * Get the current speed that particles are initialised with.
     *
     * @return A value of the speed
     */
    public float getSpeed();
}
