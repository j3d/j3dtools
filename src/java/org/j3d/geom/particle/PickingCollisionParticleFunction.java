/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.PickBounds;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * PickingCollisionParticleFunction uses Java 3D picking utilities
 * to implemen collision response for Particles against any geometry
 * within a scenegraph branch. This is an experimental ParticleFunction
 * and may not perform / scale well with complex geometry.
 *
 * @author Daniel Selman
 * @version $Revision: 1.2 $
 */
public class PickingCollisionParticleFunction implements ParticleFunction
{
    BranchGroup pickRoot;
    PickTool pickTool;
    Point3d particlePostion = new Point3d();
    Point3d previousParticlePostion = new Point3d();
    Vector3d deltaPostion = new Vector3d();

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;

    public PickingCollisionParticleFunction( BranchGroup pickRoot )
    {
        this.pickRoot = pickRoot;
        enabled = true;

        pickTool = new PickTool( pickRoot );
        pickTool.setMode( PickTool.BOUNDS );
    }

    //-------------------------------------------------------------
    // Methods defined by ParticleFunction
    //-------------------------------------------------------------

    /**
     * Check to see if this function has been enabled or not currently.
     *
     * @return True if this is enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Set the enabled state of this function. A disabled function will not
     * be applied to particles during this update.
     *
     * @param state The new enabled state to set it to
     */
    public void setEnabled(boolean state)
    {
        enabled = state;
    }

    /**
     * Notification that the system is about to do an update of the particles
     * and to do any system-level initialisation.
     *
     * @param ps The particle system that is being updated
     * @return true if this has done it's updating
     */
    public boolean newFrame( ParticleSystem ps )
    {
       return true;
    }

    /**
     * Apply this function to the given particle right now.
     *
     * @param particle The particle to apply the function to
     * @return true if the particle is still alive
     */
    public boolean apply( Particle particle )
    {
        particle.getPosition( particlePostion );

        // create a PickBounds to pick against
        PickBounds pickBounds = new PickBounds( particle.getBounds() );
        pickTool.setShape( pickBounds, new Point3d( 0, 0, 0 ) );
        PickResult pickResult = pickTool.pickAny();

        if ( pickResult != null )
        {
            // because the collision is assumed to be between something much
            // more massive than ourselves we just negate our velocity
            // negate the force and loose 90% of it
            particle.velocity.negate();
            particle.velocity.scale( particle.collisionVelocity );
            particle.resultantForce.negate();
            particle.resultantForce.scale( particle.collisionForce );
        }

        return false;
    }
}
