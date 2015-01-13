/*****************************************************************************
 *                             (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain.roam;

// External imports
// None

// Local imports
// none

/**
 * Representation of an item in a FastQueue.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
class QueueItem
{

    /** The next item in the queue after this */
    QueueItem next;

    /** The previous item in the queue after this */
    QueueItem prev;

    /** Variance value for a triangle */
    float variance = 0f;

    /** Variance value for the whole diamond */
    float diamondVariance = 0f;
}

