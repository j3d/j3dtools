/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain.roam;

// External imports
// none

// Local imports
// none

/**
 * A queue manager based on the bucket queue implementation.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class FastQueueManager implements QueueManager
{
    private FastQueue triQueue;
    private FastQueue diamondQueue;

    /**
     * Creates new QueueManager.
     */
    public FastQueueManager()
    {
        triQueue = new FastQueue(false);
        diamondQueue = new FastQueue(true);;
    }

    /**
     * Add a new triangle to the queue.
     *
     * @param node The new node to add
     */
    public void addTriangle(QueueItem node)
    {
        triQueue.add(node);
    }

    /**
     * Remove the given triangle the queue.
     *
     * @param node The new node to remove
     */
    public void removeTriangle(QueueItem node)
    {
        triQueue.remove(node);
    }

    /**
     * Add a new triangle to the queue.
     *
     * @param node The new node to add
     */
    public void addDiamond(QueueItem node)
    {
        diamondQueue.add(node);
    }

    /**
     * Remove the given diamond from the queue.
     *
     * @param node The new node to remove
     */
    public void removeDiamond(QueueItem node)
    {
        diamondQueue.remove(node);
    }

    /**
     * Clear everything from the queue.
     */
    public void clear()
    {
        triQueue.clear();
        diamondQueue.clear();
    }

    public TreeNode getSplitCandidate()
    {
        return (TreeNode)triQueue.last();
    }

    public TreeNode getMergeCandidate()
    {
        return (TreeNode)diamondQueue.first();
    }
}
