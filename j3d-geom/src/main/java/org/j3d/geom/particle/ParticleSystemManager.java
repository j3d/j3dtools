/*****************************************************************************
 *                        Copyright (c) 2001 Daniel Selman
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.particle;

// External imports
import java.util.ArrayList;

// Local imports
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

/**
 * The ParticleSystemManager is a simple manager that controls all of the
 * available particle systems as a single set of updates.
 * <p>
 * The manager needs to have a clock ticking it to drive the updates of the
 * managed particles. It has a List of registered ParticleSystems and calls the
 * update method on each whenever it is triggered.
 *
 * @author Daniel Selman
 * @version $Revision: 2.1 $
 */
public class ParticleSystemManager
{
    /** Listing of the currently registered and active particle systems */
    private ArrayList<ParticleSystem> particleSystems;

    /** Listing of the recently added systems */
    private ArrayList<ParticleSystem> newSystems;

    /** Local reporter to put errors in */
    protected ErrorReporter errorReporter;

    /**
     * Create a new manager, with no systems registered.
     */
    public ParticleSystemManager()
    {
        particleSystems = new ArrayList<>();
        newSystems = new ArrayList<>();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Register an error reporter with the object so that any errors generated
     * by the object can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter)
    {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();

        int size = particleSystems.size();
        for(int n = 0; n < size; n++)
        {
            ParticleSystem system = particleSystems.get(n);
            system.setErrorReporter(reporter);
        }
    }

    /**
     * Update the registered particle systems now. If any registered systems
     * have completed their function, they will be automatically removed.
     */
    public void update()
    {
        long time = System.currentTimeMillis();
        ParticleSystem system;

        for(int n = newSystems.size() - 1; n >= 0; n--)
        {
            system = newSystems.get(n);
            system.initialize(time);
        }

        newSystems.clear();

        for(int n = particleSystems.size() - 1; n >= 0; n--)
        {
            system = particleSystems.get(n);

            if((system != null) && !system.update(time))
            {
                // the system is dead, so we can remove it...
                system.onRemove();
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
        newSystems.add(system);
        system.setErrorReporter(errorReporter);
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
