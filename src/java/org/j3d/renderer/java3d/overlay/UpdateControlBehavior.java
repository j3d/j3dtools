/*****************************************************************************
 *                        Teseract Software, LLP (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.overlay;

// Standard imports
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupOnActivation;
import javax.media.j3d.WakeupOnBehaviorPost;

// Application specific imports
import org.j3d.util.Queue;

/**
 * An implementation of an update manager that uses the Java3D behaviour
 * system.
 * <p>
 *
 *
 * @author Will Holcomb
 * @version $Revision: 1.1 $
 */
public class UpdateControlBehavior extends Behavior
    implements UpdateManager
{
    /** Local ID value for the behavior post flag */
    private int UPDATE_ID = 10007;

    /** The wakeup condition to force the update */
    private WakeupOnBehaviorPost wakeup;

    /** Flag to indicate if we are processing update requests */
    private boolean updating = true;

    /**
     * Flag to indicate is we have let an update slip after being requested
     * due to someone stopping updates.
     */
    private boolean droppedUpdate = false;

    /** A list of the items that are pending update. */
    private Queue itemsToUpdate;

    /**
     * Create a new behavior that manages the update of a single entity
     *
     * @param entity The entity to process update requests for
     */
    public UpdateControlBehavior()
    {
        wakeup = new WakeupOnBehaviorPost(this, UPDATE_ID);
        itemsToUpdate = new Queue();
    }

    //------------------------------------------------------------------------
    // Methods from the UpdateManager interface
    //------------------------------------------------------------------------

    /**
     * Check to see if the manager is making updates right now.
     *
     * @return true if the update process is currently happening
     */
    public boolean isUpdating()
    {
        return updating;
    }

    /**
     * Instruct the system to start or stop the update process. This is used
     * to control the whole threaded update system rather than interact with
     * a single update request.
     *
     * @param updating true to set the update to happen, false to stop
     */
    public void setUpdating(boolean updating)
    {
        if (this.updating != updating)
        {
            this.updating = updating;
            if(updating && droppedUpdate)
            {
                postId(UPDATE_ID);
            }
        }
    }


    /**
     * Request that the manager update this item. This will be scheduled to
     * happen as soon as possible, but won't necessarily happen immediately.
     *
     * @param ue The entity to be updated
     */
    public void updateRequested(UpdatableEntity ue)
    {
        itemsToUpdate.add(ue);

        postId(UPDATE_ID);
    }

    //------------------------------------------------------------------------
    // Methods overridden from the base Behavior class
    //------------------------------------------------------------------------

    /**
     * Initialize the behavior to start working now. Sets up the initial
     * wakeup condition.
     */
    public void initialize()
    {
        wakeupOn(new WakeupOnActivation());
    }

    /**
     * Process the behavior that has been woken up by the given set of
     * conditions.
     *
     * @param conditions The list of conditions satisfied
     */
    public void processStimulus(Enumeration conditions)
    {
        if(updating)
        {
            while(itemsToUpdate.hasNext())
            {
                UpdatableEntity entity =
                    (UpdatableEntity)itemsToUpdate.getNext();
                entity.update();
            }
        }
        else
            droppedUpdate = true;

        wakeupOn(wakeup);
    }
}
