/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.media.j3d.*;
import java.util.Map;

/**
 * LineArrayByRefParticleSystem creates a BYREF LineArray
 * to represent the ParticleSystem. This can be used for rain type
 * effects by also setting Particle.RENDER_FROM_PREVIOUS_POSITION in
 * the environment Map.
 *
 * @author Daniel Selman
 * @version $Revision: 1.1 $
 */
public class LineArrayByRefParticleSystem extends ByRefParticleSystem
{
    /**
     * Create a new particle system with the given number of particles.
     *
     * @param particleCount The number of particles to display
     * @param particleInitializer Initialised to create the particles
     * @param environment Environment setup information
     */
    public LineArrayByRefParticleSystem( String name, int particleCount,
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
                new LineArray( particleCount * LineArrayByRefParticle.NUM_VERTICES_PER_PARTICLE,
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
        Particle particle = new LineArrayByRefParticle( env, shape,
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
        return LineArrayByRefParticle.NUM_VERTICES_PER_PARTICLE;
    }


    public Appearance createAppearance()
    {
        Appearance app = super.createAppearance();
        app.setPolygonAttributes( new PolygonAttributes( PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_NONE, 0 ) );
        app.setLineAttributes( new LineAttributes( 3, LineAttributes.PATTERN_SOLID, false ) );

        return app;
    }
}
