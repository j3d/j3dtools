/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

/**
 * ParticleFunction is the basic interface for functions that can modify the 
 * fields of a Particle.
 *
 * A function may act on any of the fields of a particle, though typically it
 * is on the size and position. Some movement functions will modify the 
 * force/energy of a particle while others will convert the energy into
 * position deltas.
 *
 * @author Daniel Selman
 * @version $Revision: 1.2 $
 */
public interface ParticleFunction
{
    /**
     * Notification that the system is about to do an update of the particles
     * and to do any system-level initialisation.
     *
     * @param ps The particle system that is being updated
     * @return true if this has done it's updating
     */
    public boolean apply( Particle particle );

    /**
     * Apply this function to the given particle right now.
     *
     * @param particle The particle to apply the function to
     * @return true if the particle 
     */
    public boolean onUpdate( ParticleSystem ps );
}
