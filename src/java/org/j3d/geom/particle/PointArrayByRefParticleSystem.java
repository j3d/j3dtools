/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import java.util.Map;

/**
 * PointArrayByRefParticleSystem creates a BYREF PointArray
 * to represent the ParticleSystem.
 *
 *
 * @author Daniel Selman
 * @version $Revision: 1.2 $
 */
public class PointArrayByRefParticleSystem extends ByRefParticleSystem
{
    public PointArrayByRefParticleSystem(
            String name,
            int particleCount,
            ParticleInitializer particleInitializer,
            Map environment )
    {
        super( name, particleInitializer, particleCount, environment );
    }

    public GeometryArray createGeometryArray()
    {
        GeometryArray geomArray =
                new PointArray(
                        particleCount,
                        GeometryArray.COORDINATES
                        | GeometryArray.COORDINATES
                        | GeometryArray.NORMALS
                        | GeometryArray.TEXTURE_COORDINATE_2
                        | GeometryArray.BY_REFERENCE
                        | GeometryArray.COLOR_4 );
        return geomArray;
    }

    public Appearance createAppearance()
    {
        Appearance app = new Appearance();
        app.setPointAttributes( new PointAttributes( 3, false ) );
        return app;
    }

    public Particle createParticle( Map env, String name, int index )
    {
        return new PointArrayByRefParticle( env,
                                            shape,
                                            index,
                                            positionRefArray,
                                            colorRefArray,
                                            textureCoordRefArray,
                                            normalRefArray );
    }

    protected int getVertexCount()
    {
        return 1;
    }
}