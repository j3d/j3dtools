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
 * @version $Revision: 1.3 $
 */
public interface ParticleFunction
{
    /**
     * Check to see if this function has been enabled or not currently.
     *
     * @return True if this is enabled
     */
    public boolean isEnabled();

    /**
     * Set the enabled state of this function. A disabled function will not
     * be applied to particles during this update.
     *
     * @param state The new enabled state to set it to
     */
    public void setEnabled(boolean state);

    /**
     * Notification that the system is about to do an update of the particles
     * and to do any system-level initialisation.
     *
     * @param ps The particle system that is being updated
     * @return true if this should force another update after this one
     */
    public boolean apply(Particle particle);

    /**
     * Apply this function to the given particle right now.
     *
     * @param particle The particle to apply the function to
     * @return true if the particle has changed, false otherwise
     */
    public boolean onUpdate(ParticleSystem ps);
}
