/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2004-
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.hanim;

// External imports
import java.util.ArrayList;

// Local imports
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

/**
 * Utility class for managing a collection of HAnimHumanoids in a scene.
 * <p>
 *
 * The class just provides a single place for all the humanoids to be kept and
 * updated as a single call, rather than having to do your own structure. There's
 * nothing special about this class - just a collection of arrays and methods
 * to do the updates.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class HumanoidManager
{
    /** Our collection of humanoids */
    private ArrayList<HAnimHumanoid> humanoids;

    /** Local reporter to put errors in */
    private ErrorReporter errorReporter;

    /**
     * Create a new, empty, instance of the manager.
     */
    public HumanoidManager()
    {
        humanoids = new ArrayList<HAnimHumanoid>();
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
    }

    /**
     * Add a humanoid to the list to manage. This will check to make sure
     * you're not adding it twice.
     *
     * @param human The humanoid instance to add
     */
    public void addHumanoid(HAnimHumanoid human)
    {
        if(!humanoids.contains(human))
        {
            humanoids.add(human);
            human.setErrorReporter(errorReporter);
        }
    }

    /**
     * Remove a humanoid from the managed list. Requesting the removal of a
     * humanoid that wasn't added in the first place is silently ignored.
     *
     * @param human The humanoid instance to remove
     */
    public void removeHumanoid(HAnimHumanoid human)
    {
        humanoids.remove(human);
    }

    /**
     * Update all the humanoids now.
     */
    public void updateAll()
    {
        for(int i = 0; i < humanoids.size(); i++)
        {
            HAnimHumanoid human = (HAnimHumanoid)humanoids.get(i);
            human.updateSkeleton();
        }
    }

    /**
     * Clear all the humanoids currently being managed.
     */
    public void clear()
    {
        humanoids.clear();
    }
}
