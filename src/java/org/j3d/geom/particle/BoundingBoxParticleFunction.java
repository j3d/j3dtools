/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

import javax.media.j3d.BoundingBox;
import javax.vecmath.Point3d;

/**
 * The BoundingBoxParticleFunction simply clamps the position of a particle
 * to within a BoundingBox. The BoundingBoxParticleFunction should be applied
 * <b>after</b> any other ParticleFunctions which modify a Particle's position
 * (e.g. PhysicsFunction).
 *
 * @author Daniel Selman
 * @version $Revision: 1.2 $
 */
public class BoundingBoxParticleFunction implements ParticleFunction
{
    private Point3d position = new Point3d();
    private Point3d lowerCorner = new Point3d();
    private Point3d upperCorner = new Point3d();
    private BoundingBox boundingBox;

    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;

    public BoundingBoxParticleFunction( BoundingBox boundingBox )
    {
        this.boundingBox = boundingBox;
        boundingBox.getLower( lowerCorner );
        boundingBox.getUpper( upperCorner );

        enabled = true;
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
     * Apply this function to the given particle right now.
     *
     * @param particle The particle to apply the function to
     * @return true if the particle has changed, false otherwise
     */
    public boolean onUpdate( ParticleSystem ps )
    {
       return true;
    }

    /**
     * Notification that the system is about to do an update of the particles
     * and to do any system-level initialisation.
     *
     * @param ps The particle system that is being updated
     * @return true if this should force another update after this one
     */
    public boolean apply( Particle particle )
    {
        // we constrain the particle to be inside the BoundingBox
        particle.getPosition( position );

        if ( boundingBox.intersect( position ) == false )
        {
            if ( position.x > upperCorner.x )
            {
                position.x = upperCorner.x;
            }

            if ( position.y > upperCorner.y )
            {
                position.y = upperCorner.y;
            }

            if ( position.z > upperCorner.z )
            {
                position.z = upperCorner.z;
            }

            if ( position.x < lowerCorner.x )
            {
                position.x = lowerCorner.x;
            }

            if ( position.y < lowerCorner.y )
            {
                position.y = lowerCorner.y;
            }

            if ( position.z < lowerCorner.z )
            {
                position.z = lowerCorner.z;
            }
        }

        particle.setPosition( position.x, position.y, position.z );

        return false;
    }
}
