/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain.roam;

// Standard imports
import java.util.TreeSet;

// Application specific imports
// none

/**
 * Abstract representation of functionality that is used to queue up tree node
 * information for roam.
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
interface QueueManager
{
    /**
     * Add a new triangle to the queue.
     *
     * @param node The new node to add
     */
    public void addTriangle(QueueItem node);

    /**
     * Remove the given triangle the queue.
     *
     * @param node The new node to remove
     */
    public void removeTriangle(QueueItem node);

    /**
     * Add a new triangle to the queue.
     *
     * @param node The new node to add
     */
    public void addDiamond(QueueItem node);

    /**
     * Remove the given diamond from the queue.
     *
     * @param node The new node to remove
     */
    public void removeDiamond(QueueItem node);

    /**
     * Clear everything from the queue.
     */
    public void clear();
}
