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
import javax.vecmath.Color4f;
import java.util.Map;

/**
 * Abstract ParticleSystem for handling ByRef GeometryArrays.
 * <p>
 *
 * The entire geometry for the ParticleSystem represented
 * by a single Shape3D. When an update request is received, the particles
 * update the position and color information in the arrays provided by this
 * class and are updated in the underlying geometry.
 * <p>
 * TODO: add support for interleaved arrays and test performance.
 *
 * @author Daniel Selman
 * @version $Revision: 1.4 $
 */
public abstract class ByRefParticleSystem extends ParticleSystem
        implements GeometryUpdater
{
   /** The geometry created for this system */
   protected GeometryArray geometryArray;

   /** Array containing the current position coordinates */
   protected double[] positionRefArray;

   /** Array containing the current texture coordinates */
   protected float[] textureCoordRefArray;

   /** Array containing the current color values */
   protected float[] colorRefArray;

   /** Array containing the current normals */
   protected float[] normalRefArray;

   /** The shape containing the geometry */
   protected Shape3D shape;

   /**
   * Create a new particle system that represents the given type.
   *
   * @param systemType An identifier describing the current system type
   * @param particleInitializer Initialised to create the particles
   * @param environment Environment setup information
   * @param environment Environment setup information
   */
   public ByRefParticleSystem( String systemName,
       ParticleInitializer particleInitializer,
       int particleCount,
       Map environment )
   {
       super( systemName, environment );

       shape = new OrientedShape3D( );
       //shape = new Shape3D();
       initializeArrays( particleCount );
       createParticles( particleInitializer, particleCount );

       geometryArray = createGeometryArray( );
       geometryArray.setCapability( GeometryArray.ALLOW_REF_DATA_WRITE );

       shape.setCapability( Shape3D.ALLOW_APPEARANCE_WRITE );

       shape.setGeometry( geometryArray );
       shape.setAppearance( createAppearance( ) );

       shape.setCollidable( false );
       shape.setPickable( false );
   }

   /**
   * Fetch the scene graph node that represents the particle system.
   *
   * @return The shape containing the particles
   */
   public Node getNode( )
   {
       return shape;
   }

   public void onRemove( )
   {
       Appearance app = new Appearance( );
       RenderingAttributes renderingAttributes = new RenderingAttributes( );
       renderingAttributes.setVisible( false );

       app.setRenderingAttributes( renderingAttributes );
       shape.setAppearance( app );
   }


   /**
   * Request to create the geometry needed by this system.
   *
   * @return The object representing the geometry
   */
   public abstract GeometryArray createGeometryArray( );

   /**
   * Fetch the number of vertices used in this geometry. Value should
   * always be non-negative.
   *
   * @return The number of vertices
   */
   protected abstract int getVertexCount( );


   /**
   * Request to force an update of the geometry now.
   *
   * @return true if the system is currently running
   */
   public boolean update( )
   {
       super.update( );
       running = false;
       geometryArray.updateData( this );
       return running;
   }

   /**
   * Update request on the geometry data that is accessed by reference.
   *
   * @param geometry The geometry object being updated
   */
   public void updateData( Geometry geometry )
   {
       GeometryArray geometryArray = ( GeometryArray ) geometry;

       for ( int n = particleCount - 1; n >= 0; n-- )
       {
           Particle particle = ( Particle ) particles.get( n );
           running |= updateParticle( n, particle );
       }

       geometryArray.setCoordRefDouble( positionRefArray );
       geometryArray.setColorRefFloat( colorRefArray );
       geometryArray.setNormalRefFloat( normalRefArray );
       geometryArray.setTexCoordRefFloat( 0, textureCoordRefArray );
   }

   /**
   * Set up the arrays used internally.
   *
   * @param particleCount The number of particles in use
   */
   synchronized protected void initializeArrays( int particleCount )
   {
       if ( positionRefArray == null )
       {
           int count = getVertexCount( );

           int pos = particleCount * count * ByRefParticle.NUM_COORDS;
           int col = particleCount * count * ByRefParticle.NUM_COLORS;
           int norm = particleCount * count * ByRefParticle.NUM_NORMALS;
           int tex = particleCount * count * ByRefParticle.NUM_TEXTURE_COORDS;

           positionRefArray = new double[pos];
           colorRefArray = new float[col];
           normalRefArray = new float[norm];
           textureCoordRefArray = new float[tex];
       }
   }
}
