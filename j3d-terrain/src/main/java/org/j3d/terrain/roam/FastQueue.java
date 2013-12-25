/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain.roam;

/**
 * A fast bucket-based queue for sorting merge and split queues.
 * <p>
 *
 * The implementation is based on the information at
 * <a href="http://www.cognigraph.com/ROAM_homepage/bucketqueues.html">
 * http://www.cognigraph.com/ROAM_homepage/bucketqueues.html
 * </a>
 *
 * @author  paulby
 * @version
 */
class FastQueue
{
    /** The number of buckets that approximate the sorted queue */
    private static final int BUCKETS = 2048;

    /**
     * The largest number that is 'sorted', all items larger than this
     * number are placed in the maximum bucket
     */
    private static final float TOP = 0.2f;

    /** Flag indicating that we a sorting by diamondVariance */
    private boolean diamondQueue = false;

    private QueueItem[] queueBuckets;

    /** iqmax */
    private int largest = 0;

    /** iqmin */
    private int smallest = BUCKETS;

    /** Whatever our iq current bucket is */
    private int currentBucket;

    /**
     * Create a new queue with for the specified detail/variance comparisons.
     *
     * @param diamondQueue True if this is to handle diamonds (merges)
     */
    public FastQueue(boolean diamondQueue)
    {
        this.diamondQueue = diamondQueue;
        queueBuckets = new QueueItem[BUCKETS + 1];
    }

    /**
     * Add a new item to the queue.
     *
     * @param node The item to add
     */
    public void add(QueueItem node)
    {
        QueueItem existingNode = getList(node);

        node.next = existingNode;
        if(existingNode != null)
            existingNode.prev = node;
        node.prev = null;

        queueBuckets[currentBucket] = node;

        largest = Math.max(largest, currentBucket);
        smallest = Math.min(smallest, currentBucket);
    }

    /**
     * Remove an item from the queue.
     *
     * @param node The node to remove
     */
    public void remove(QueueItem node)
    {
        if(node.prev == null)
        {
            QueueItem existingNode = getList(node);

            if(existingNode == null || existingNode != node)
                return;

            queueBuckets[currentBucket] = node.next;

            if(node.next != null)
                node.next.prev = null;

            node.prev = null;
            node.next = null;
        }
        else
        {
            node.prev.next = node.next;

            if(node.next != null)
                node.next.prev = node.prev;

            node.prev = null;
            node.next = null;
        }
    }

    /**
     * Returns the largest element in the queue
     */
    public QueueItem last()
    {
        // Don't like this, inefficient.
        QueueItem list = queueBuckets[largest];

        while(list == null && largest >= smallest)
        {
            largest--;
            list = queueBuckets[largest];
        }

        return list;
    }

    /**
     * Returns the smallest element in the queue
     */
    public QueueItem first()
    {
        QueueItem list = queueBuckets[smallest];

        while(list == null && smallest <= largest)
        {
            smallest++;
            list = queueBuckets[smallest];
        }

        return list;
    }

    /**
     * Clear everything from the queue.
     */
    public void clear()
    {
        // This doesn't delink the items in each bucket. Hopefully this
        // won't be a problem for either GC or later usage.
        for(int i = 0; i < queueBuckets.length; i++)
        {
            queueBuckets[i] = null;
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Get the list that contains this item.
     */
    private QueueItem getList(QueueItem node)
    {
        float value;
//        if(diamondQueue)
//            value = node.diamondVariance;
//        else
            value = node.variance;

        if(value > TOP)
            currentBucket = BUCKETS;
        else
            currentBucket = (int)((value / TOP) * BUCKETS);

        return queueBuckets[currentBucket];
    }
}

