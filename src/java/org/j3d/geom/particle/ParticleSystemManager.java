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
import java.util.ArrayList;

// Application specific imports
// None

/**
 * The ParticleSystemManager is a simple manager that controls all of the
 * available particle systems as a single set of updates.
 * <p>
 * The manager needs to have a clock ticking it to drive the updates of the
 * managed particles. It has a List of registered ParticleSystems and calls the
 * update method on each whenever it is triggered.
 *
 * @author Daniel Selman
 * @version $Revision: 1.4 $
 */
public class ParticleSystemManager
{
    /** Listing of the currently registered and active particle systems */
    private ArrayList particleSystems = new ArrayList();

    /**
     * Create a new manager, with no systems registered.
     */
    public ParticleSystemManager()
    {
        particleSystems = new ArrayList();
    }

    /**
     * Update the registered particle systems now. If any registered systems
     * have completed their function, they will be automatically removed.
     */
    public void update()
    {
        for(int n = particleSystems.size() - 1; n >= 0; n--)
        {
            ParticleSystem particleSystem = (ParticleSystem)particleSystems.get( n );

            if((particleSystem != null) && (particleSystem.update() == false))
            {
                // the system is dead, so we can remove it...
                particleSystem.onRemove();
                particleSystems.remove(n);
            }
        }
    }

    /**
     * Add a new particle system to this manager. No checks are made for
     * duplications.
     *
     * @param system The new system to add to the manager
     */
    public void addParticleSystem(ParticleSystem system)
    {
        particleSystems.add(system);
    }

    /**
     * Remove a system from this manager. If the system is not currently
     * registered, the request is silently ignored.
     *
     * @param system The system instance to remove
     */
    public void removeParticleSystem(ParticleSystem system)
    {
        particleSystems.remove(system);
    }
}
