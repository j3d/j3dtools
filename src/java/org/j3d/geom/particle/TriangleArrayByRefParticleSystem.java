/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.media.j3d.Texture;
import javax.media.j3d.Shape3D;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;

import com.sun.j3d.utils.image.TextureLoader;

import javax.vecmath.Point3d;
import javax.vecmath.Color4f;

/**
 * TriangleArrayByRefParticleSystem creates a BYREF TriangleArray
 * to represent the ParticleSystem.
 *
 * @author Daniel Selman
 * @version $Revision: 1.1 $
 */
public class TriangleArrayByRefParticleSystem extends ByRefParticleSystem
{
   public static final int TRIANGLE_ARRAY_BYREF_PARTICLE_SYSTEM = 1;

   public TriangleArrayByRefParticleSystem( int particleCount, ParticleInitializer particleInitializer, Map environment )
   {
       super( TRIANGLE_ARRAY_BYREF_PARTICLE_SYSTEM, particleInitializer, particleCount, environment );
   }

   public GeometryArray createGeometryArray()
   {
       GeometryArray geomArray = new TriangleArray( particleCount * TriangleArrayByRefParticle.NUM_VERTICES_PER_PARTICLE,
                                                    GeometryArray.COORDINATES |
                                                    GeometryArray.TEXTURE_COORDINATE_2 |
                                                    GeometryArray.BY_REFERENCE |
                                                    GeometryArray.COLOR_4 );
       return geomArray;
   }

   public Appearance createAppearance()
   {
       Appearance app = new Appearance();
       app.setPolygonAttributes( new PolygonAttributes( PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_NONE, 0 ) );
       app.setTransparencyAttributes( new TransparencyAttributes( TransparencyAttributes.NICEST, 0.0f ) );
       app.setTextureAttributes( new TextureAttributes( TextureAttributes.REPLACE, new Transform3D(), new Color4f(), TextureAttributes.FASTEST ) );
       // load the texture image and assign to the appearance
       TextureLoader texLoader = new TextureLoader( "black_white.png", Texture.RGBA, null );
       Texture tex = texLoader.getTexture();
       app.setTexture( tex );
       return app;
   }

   public Particle createParticle( int index )
   {
       return new TriangleArrayByRefParticle( shape, index, positionRefArray, colorRefArray, textureCoordRefArray, normalRefArray );
   }

   protected int getVertexCount()
   {
       return TriangleArrayByRefParticle.NUM_VERTICES_PER_PARTICLE;
   }
}
