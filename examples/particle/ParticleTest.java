/*****************************************************************************
 *                        Copyright (c) 2002 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This source is based on the SUN HelloUniverse example.
 ****************************************************************************/
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;

// imports for the ParticleSystem
import org.j3d.geom.particle.*;
import java.util.*;

/**
 * ParticleTest shows how you can easily add complex particle-
 * based animation to your Java 3D applications. It adds a 
 * smoke particle system, where each particle is acted on
 * by wind, gravity and is constrained within a BoundingBox.
 * <p>
 * Particles are texture mapped and represented using a single
 * TriangleArray in BY_REFERENCE mode.
 *
 * @author Daniel Selman
 * @version $Revision: 1.1 $
 */
public class ParticleTest extends Applet
{
   private SimpleUniverse u = null;

   public BranchGroup createSceneGraph( )
   {
       // Create the root of the branch graph
       BranchGroup objRoot = new BranchGroup( );

       // Create the TransformGroup node and initialize it to the
       // identity. Enable the TRANSFORM_WRITE capability so that
       // our behavior code can modify it at run time. Add it to
       // the root of the subgraph.
       TransformGroup objTrans = new TransformGroup( );
       objTrans.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
       objRoot.addChild( objTrans );

       // *********************************************************************
       //     begin particle code
       // *********************************************************************

       // create the configuration environment for the ParticleSystems
       Map environment = new Hashtable();

       // create the ParticleInitializer for the ParticleSystem
       // the initializer is used to control how long particles within
       // the system live and how they are reinitialized when they die.
       // this simple initializer lets particles live for 200 iterations
       // and moves them to point 0,0,0 when they die
       ParticleInitializer particleInitializer = 
           new MaxAgePointEmitter( 300, 0, -0.5, 0 );

       // create a "smoke" particle system with 1000 particles
       // the properties for each particle are loaded from a
       // property file ("smoke" is used as a key)
       final int particleCount = 500;
       ParticleSystem smokeParticleSystem = 
           new TriangleArrayByRefParticleSystem( "smoke", particleCount, 
                                                 particleInitializer, 
                                                 environment );

       // create the ParticleSystemManager for the ParticleSystem
       // this instance uses a behavior and will trigger the Particle System
       // every frame
       ParticleSystemManager particleSystemManager  = 
           new ParticleSystemManager( new WakeupOnElapsedFrames( 1 ), environment );

       BoundingSphere bounds =
           new BoundingSphere( new Point3d( 0.0,0.0,0.0 ), 500.0 );

       // set the bounds on the ParticleSystemManager so it will be scheduled
       particleSystemManager.setSchedulingBounds( bounds );

       // add the smokeParticleSystem to its manager
       particleSystemManager.addParticleSystem( smokeParticleSystem );

       // this will cause smokeParticleSystem to run for 2000 frames
       final int maxFrameCount = 2000;
       smokeParticleSystem.addParticleFunction( 
           new FrameCountParticleFunction( maxFrameCount ) );

       // this will apply an upward wind force to the particles
       smokeParticleSystem.addParticleFunction( 
           new WindParticleFunction( new Vector3d( 0.0, 0.003, 0.0 ) ) );

       // this will apply a gravity force to the particles
       smokeParticleSystem.addParticleFunction( 
           new GravityParticleFunction() );

       // this will do basic F=m.a position updates on the particles
       smokeParticleSystem.addParticleFunction( 
           new PhysicsFunction() );

       // this will clamp the position of the particles to within a box
       final double size = 0.8;
       smokeParticleSystem.addParticleFunction( 
           new BoundingBoxParticleFunction( new BoundingBox( 
                                               new Point3d( -size,-size,-size ), 
                                               new Point3d( size, size, size ) ) ) );

       // add the particleSystemManager (Behavior) to the scenegraph
       objTrans.addChild( particleSystemManager );

       // add the geometry for the ParticleSystem to the scenegraph
       objTrans.addChild( smokeParticleSystem.getNode() );

       // *********************************************************************
       //     end particle code
       // *********************************************************************

       // Create a new Behavior object that will perform the
       // desired operation on the specified transform and add
       // it into the scene graph.
       Transform3D yAxis = new Transform3D( );
       Alpha rotationAlpha = new Alpha( -1, 4000 );

       RotationInterpolator rotator =
           new RotationInterpolator( rotationAlpha, objTrans, yAxis,
           0.0f, (float) Math.PI*2.0f );
       rotator.setSchedulingBounds( bounds );
       objRoot.addChild( rotator );

       // Have Java 3D perform optimizations on this scene graph.
       objRoot.compile( );

       return objRoot;
   }

   public ParticleTest( )
   {
   }

   public void init( )
   {
       setLayout( new BorderLayout( ) );
       GraphicsConfiguration config =
           SimpleUniverse.getPreferredConfiguration( );

       Canvas3D c = new Canvas3D( config );
       add( "Center", c );

       // Create a simple scene and attach it to the virtual universe
       BranchGroup scene = createSceneGraph( );
       u = new SimpleUniverse( c );

       // This will move the ViewPlatform back a bit so the
       // objects in the scene can be viewed.
       u.getViewingPlatform( ).setNominalViewingTransform( );

       u.addBranchGraph( scene );
   }

   public void destroy( )
   {
       u.removeAllLocales( );
   }

   //
   // The following allows ParticleTest to be run as an application
   // as well as an applet
   //
   public static void main( String[] args )
   {
       new MainFrame( new ParticleTest( ), 256, 256 );
   }
}
