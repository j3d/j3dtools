/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.vecmath.Vector3d;

/**
 * Movement function that performs basic F=M.
 * <p>
 * A movement control on the Particles
 * based on their applied resultantForce. A percentage
 * of the resultantForce is "lost" prior to position
 * calculation to simulate friction/drag.
 * <p>
 * This ParticleFunction should be added to the ParticleSystem
 * *after* any MovementFunctions which are applying
 * forces to Particles.
 * <p>
 * Some basic physics equations:
 * <p>
 *
 * acceleration = force / mass;<br>
 * velocity += acceleration * time_diff;<br>
 * pos += velocity * time_diff;<br>
 * ke = 0.5 * m * v * v<br>
 * p.e (grav) = m * g * h<br>
 * p.e (spring) = 0.5 * k * x * x (x = amount of compression)<br>
 * total mech energy = k.e + p.e. (grav) + p.e. (spring)<br>
 * power = work / time<br>
 * power = force * displacement / time<br>
 * f = m * a<br>
 * f = m * delta v / t<br>
 *
 * @author Daniel Selman
 * @version $Revision: 1.2 $
 */
public class PhysicsFunction implements ParticleFunction
{
   // the assumed initial interval between calls to the PhysicsFunction
   // this delta is recalculated every recalculateInterval frames
   private double deltaTime = 1.0 / 40.0;

   private Vector3d position = new Vector3d( );
   private Vector3d acceleration = new Vector3d( );

   // percentage of force still avalailable after friction lossage
   private double frictionForce = 0.90;

   // percentage of velocity still avilable
   private double frictionVelocity = 0.95;
   private long startTime;
   private static final int recalculateInterval = 256;
   private static long invokeCount = 0;

   public PhysicsFunction( )
   {
       startTime = System.currentTimeMillis( );
   }

   public boolean onUpdate( ParticleSystem ps )
   {
       // review DCS - we don't check ps,
       // so if this PhysicsFunction is shared between
       // multiple ParticleSystems we will get bogus results
       invokeCount++;

       if( invokeCount == recalculateInterval )
       {
           double elapsedSeconds = (System.currentTimeMillis( ) - startTime) / 1000.0;
           deltaTime =  (double) elapsedSeconds / (double) recalculateInterval;

           System.out.println( "PhysicsFunction FPS: " + (int) 1.0 / deltaTime );

           // reset counters
           invokeCount = 0;
           startTime = System.currentTimeMillis( );
       }

      return true;
   }

   public boolean apply( Particle particle )
   {
       particle.resultantForce.scale( particle.frictionForce );
       acceleration.set( particle.resultantForce );

       // get the change in velocity
       acceleration.scale( deltaTime / particle.mass );
       particle.velocity.add( acceleration );

       // get the change in position
       particle.getPosition( position );
       acceleration.set( particle.velocity );
       acceleration.scale( deltaTime );
       position.add( acceleration );

       // update the position
       particle.setPosition( position.x, position.y, position.z );

       particle.velocity.scale( particle.frictionVelocity );

       // return false, the PhysicsFunction should
       // not cause the particle system to keep running
       return false;
   }
}
