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
import javax.media.j3d.OrientedShape3D;

import javax.media.j3d.GeometryUpdater;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.Node;

import javax.vecmath.Vector3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Point3f;

/**
 * Abstract ParticleSystem for handling ByRef GeometryArrays
 * within the entire geometry for the ParticleSystem represented
 * by a single Shape3D.
 *
 * @author Daniel Selman
 * @version $Revision: 1.1 $
 */
public abstract class ByRefParticleSystem extends ParticleSystem implements GeometryUpdater
{
   protected GeometryArray geometryArray;

   protected double positionRefArray[];
   protected float textureCoordRefArray[];
   protected float colorRefArray[];
   protected float normalRefArray[];

   protected Shape3D shape = new OrientedShape3D();

   public ByRefParticleSystem( int systemType, ParticleInitializer particleInitializer, int particleCount, Map environment )
   {
       super( systemType, environment );

       initializeArrays( particleCount );
       createParticles( particleInitializer, particleCount );

       geometryArray = createGeometryArray();
       geometryArray.setCapability( GeometryArray.ALLOW_REF_DATA_WRITE );

       shape.setGeometry( geometryArray );
       shape.setAppearance( createAppearance() );

       shape.setCollidable( false );
       shape.setPickable( false );
       shape.setBoundsAutoCompute( false );
   }

   public Node getNode()
   {
       return shape;
   }

   public abstract GeometryArray createGeometryArray();
   public abstract Appearance createAppearance();
   protected abstract int getVertexCount();

   public boolean update()
   {
       geometryArray.updateData( this );
       return running;
   }

   public void updateData( Geometry geometry )
   {
       GeometryArray geometryArray = (GeometryArray) geometry;

       for( int n = particleCount-1; n >= 0; n-- )
       {
           Particle particle = (Particle) particles.get( n );
           particle.incAge();
           running |= updateParticle( n, particle );
       }

       geometryArray.setCoordRefDouble( positionRefArray );
       geometryArray.setColorRefFloat( colorRefArray );
       geometryArray.setNormalRefFloat( normalRefArray );
       geometryArray.setTexCoordRefFloat( 0, textureCoordRefArray );
   }

   synchronized protected void initializeArrays( int particleCount )
   {
       if ( positionRefArray == null )
       {
           positionRefArray = new double[ particleCount * getVertexCount() * ByRefParticle.NUM_COORDS ];
           colorRefArray = new float [ particleCount * getVertexCount() * ByRefParticle.NUM_COLORS ];
           normalRefArray = new float [ particleCount * getVertexCount() * ByRefParticle.NUM_NORMALS ];
           textureCoordRefArray = new float[ particleCount * getVertexCount() * ByRefParticle.NUM_TEXTURE_COORDS ];
       }
   }
}
