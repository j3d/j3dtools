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
 * The ParticleInitializer is registered with a ParticleSystem
 * and is responsible for creating and initialising the particles
 *
 * @author Daniel Selman
 * @version $Revision: 1.3 $
 */
public interface ParticleInitializer
{
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
     * Change the basic position that the particles are being generated from.
     *
     * @param x The x component of the location
     * @param y The y component of the location
     * @param z The z component of the location
     */
    public void setPosition(float x, float y, float z);

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
     * Change the apparent surface area. Surface area is measured in square
     * metres.
     *
     * @param surfaceArea surface area
     */
    public void setSurfaceArea(double surfaceArea);

    /**
     * Change the mass of the particle. Mass is measured in kilograms.
     *
     * The mass of an individual particle
     */
    public void setMass(double mass);

    /**
     * Change the initial velocity that the particles are endowed with.
     *
     * @param x The x component of the velocity
     * @param y The y component of the velocity
     * @param z The z component of the velocity
     */
    public void setVelocity(float x, float y, float z);
}
