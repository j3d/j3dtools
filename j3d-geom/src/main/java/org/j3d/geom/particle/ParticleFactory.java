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

// Local imports
// None

/**
 * Abstract representation of a mechanism to create instances of Particles.
 * <p>
 * A ParticleFactory is registered with a ParticleSystem and should
 * initialize the fields of the Particle based on the physical entity being
 * modelled (rain, dust, stones etc)
 *
 * @author Daniel Selman
 * @version $Revision: 2.0 $
 */
public interface ParticleFactory
{
    /**
     * Request the number of coordinates each particle will use. Used so that
     * the manager can allocate the correct length array.
     *
     * @return The number of coordinates this particle uses
     */
    public int coordinatesPerParticle();

    /**
     * Request the number of color components this particle uses. Should be a
     * value of 4 or 3 to indicate use or not of alpha channel.
     *
     * @return The number of color components in use
     */
    public int numColorComponents();

    /**
     * Request the number of texture coordinate components this particle uses.
     * Should be a value of 2 or 3 to indicate use or not of 2D or 3D textures.
     *
     * @return The number of color components in use
     */
    public int numTexCoordComponents();

    /**
     * Create a new particle instance.
     *
     * @return The new instance created
     */
    public Particle createParticle();
}
