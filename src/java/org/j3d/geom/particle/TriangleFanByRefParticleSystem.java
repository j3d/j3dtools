/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.TriangleFanArray;
import java.util.Map;

/**
 * A particle system that uses a BYREF TriangleFan
 * to represent the ParticleSystem.
 *
 * @author Daniel Selman
 * @version $Revision: 1.1 $
 */
public class TriangleFanByRefParticleSystem extends ByRefParticleSystem
{
    /**
     * Flag for the particle creation using the draw from previous position
     * flag of the particle.
     */
    private boolean usePreviousPosition;

    /**
     * Create a new particle system that uses quads for the particles.
     *
     * @param name An arbitrary string name for ID purposes
     * @param maxParticleCount The maximum number of particles allowed to exist
     * @param drawFromPrevious true if this should draw the particle relative to
     *    the position of last frame
     */
    public TriangleFanByRefParticleSystem(String systemName,
                                        int maxParticleCount,
                                        boolean drawFromPrevious)
    {
        super(systemName, maxParticleCount);

        usePreviousPosition = drawFromPrevious;
    }

    /**
     * Request to create the geometry needed by this system.
     *
     * @return The object representing the geometry
     */
    public GeometryArray createGeometryArray()
    {
        int format = GeometryArray.COORDINATES |
                     GeometryArray.NORMALS |
                     GeometryArray.TEXTURE_COORDINATE_2 |
                     GeometryArray.BY_REFERENCE |
                     GeometryArray.COLOR_4;

        // Setup the vertex count handling.
        int[] fanCount = new int[maxParticleCount];

        for(int i = 0; i < maxParticleCount; i++)
            fanCount[i] = 4;

        GeometryArray geomArray =
            new TriangleFanArray(maxParticleCount * 4, format, fanCount);

        return geomArray;
    }

    /**
     * Request the number of coordinates each particle will use. Used so that
     * the manager can allocate the correct length array.
     *
     * @return The number of coordinates this particle uses
     */
    public int coordinatesPerParticle()
    {
        return 4;
    }

    /**
     * Request the number of color components this particle uses. Should be a
     * value of 4 or 3 to indicate use or not of alpha channel.
     *
     * @return The number of color components in use
     */
    public int numColorComponents()
    {
        return 4;
    }

    /**
     * Request the number of texture coordinate components this particle uses.
     * Should be a value of 2 or 3 to indicate use or not of 2D or 3D textures.
     *
     * @return The number of color components in use
     */
    public int numTexCoordComponents()
    {
        return 2;
    }

    /**
     * Create a new particle with the given ID.
     *
     * @param index The id of the particle
     * @return A particle corresponding to the given index
     */
    public Particle createParticle()
    {
        return new TriangleFanByRefParticle(usePreviousPosition);
    }
}