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
import javax.media.j3d.TriangleArray;
import java.util.Map;

/**
 * TriangleArrayByRefParticleSystem creates a BYREF TriangleArray
 * to represent the ParticleSystem.
 *
 * @author Daniel Selman
 * @version $Revision: 1.3 $
 */
public class TriangleArrayByRefParticleSystem extends ByRefParticleSystem
{
    public static final int TRIANGLE_ARRAY_BYREF_PARTICLE_SYSTEM = 1;

    /**
     * Create a new particle system with the given number of particles.
     *
     * @param particleCount The number of particles to display
     * @param particleInitializer Initialised to create the particles
     * @param environment Environment setup information
     */
    public TriangleArrayByRefParticleSystem( String name, int particleCount,
                                             ParticleInitializer particleInitializer,
                                             Map environment )
    {
        super( name, particleInitializer, particleCount, environment );
    }

    /**
     * Request to create the geometry needed by this system.
     *
     * @return The object representing the geometry
     */
    public GeometryArray createGeometryArray()
    {
        GeometryArray geomArray =
                new TriangleArray( particleCount * TriangleArrayByRefParticle.NUM_VERTICES_PER_PARTICLE,
                                   GeometryArray.COORDINATES |
                                   GeometryArray.TEXTURE_COORDINATE_2 |
                                   GeometryArray.BY_REFERENCE |
                                   GeometryArray.COLOR_4 );
        return geomArray;
    }

    /**
     * Create a new particle with the given ID.
     *
     * @param index The id of the particle
     * @return A particle corresponding to the given index
     */
    public Particle createParticle( Map env, String name, int index )
    {
        Particle particle = new TriangleArrayByRefParticle( env, shape,
                                                            index,
                                                            positionRefArray,
                                                            colorRefArray,
                                                            textureCoordRefArray,
                                                            normalRefArray );

        assignAttributes( name, particle );
        return particle;
    }

    /**
     * Fetch the number of vertices used in this geometry. Value should
     * always be non-negative.
     *
     * @return The number of vertices
     */
    protected int getVertexCount()
    {
        return TriangleArrayByRefParticle.NUM_VERTICES_PER_PARTICLE;
    }
}
