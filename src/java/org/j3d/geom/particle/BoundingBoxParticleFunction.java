/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

// Standard imports
// None

// Application specific imports
// None

/**
 * Clamps the position of a particle to within a BoundingBox.
 * <p>
 * This function should be applied <i>after</i> any other ParticleFunctions
 * which modify a Particle's position (e.g. PhysicsFunction).
 *
 * @author Daniel Selman
 * @version $Revision: 2.0 $
 */
public class BoundingBoxParticleFunction implements ParticleFunction
{
    /** Temp variable for requesting the position from a particle */
    private float[] position;

    /** Location of the lower corner of the bounding box */
    private float[] lowerCorner;

    /** Location of the upper corner of the bounding box */
    private float[] upperCorner;


    /** Flag to say whether or not this function is disabled or not */
    private boolean enabled;

    /**
     * Create a new bounding box function with the given initial bounds
     *
     * @param upper All positive corner of the bounds
     * @param lower All negative corner of the bounds
     */
    public BoundingBoxParticleFunction(float[] upper, float[] lower)
    {
        lowerCorner[0] = lower[0];
        lowerCorner[1] = lower[1];
        lowerCorner[2] = lower[2];

        upperCorner[0] = upper[0];
        upperCorner[1] = upper[1];
        upperCorner[2] = upper[2];

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
     * @param deltaT The elapsed time in milliseconds since the last frame
     * @return true if this should force another update after this one
     */
    public boolean newFrame(int deltaT)
    {
       return true;
    }

    /**
     * Notification that the system is about to do an update of the particles
     * and to do any system-level initialisation.
     *
     * @param particle The particle to apply the function to
     * @return true if this should force another update after this one
     */
    public boolean apply(Particle particle)
    {
        // we constrain the particle to be inside the BoundingBox
        particle.getPosition(position);

        boolean changed = false;

        if(position[0] > upperCorner[0])
        {
            position[0] = upperCorner[0];
            changed = true;
        }

        if(position[1] > upperCorner[1])
        {
            position[1] = upperCorner[1];
            changed = true;
        }

        if(position[2] > upperCorner[2])
        {
            position[2] = upperCorner[2];
            changed = true;
        }

        if(position[0] < lowerCorner[0])
        {
            position[0] = lowerCorner[0];
            changed = true;
        }

        if(position[1] < lowerCorner[1])
        {
            position[1] = lowerCorner[1];
            changed = true;
        }

        if(position[2] < lowerCorner[2])
        {
            position[2] = lowerCorner[2];
            changed = true;
        }

        if(changed)
            particle.setPosition(position[0], position[1], position[2]);

        return true;
    }
}
