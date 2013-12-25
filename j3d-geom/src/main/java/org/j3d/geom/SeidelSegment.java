/*****************************************************************************
 *                        j3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.geom;

// External imports
// None

// Local imports
// None

import org.j3d.maths.vector.Point2d;

/**
 * Attributes of a segment used in the Siedel triangulation routines.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class SeidelSegment
{
    /** Endpoint 1 */
    Point2d v0;

    /** Endpoint 2 */
    Point2d v1;

    /** Has the segment been inserted into the trapezoidation yet */
    boolean isInserted;

    /** Index of the root node 1 in the query list */
    int root0;

    /** Index of the root node 2 in the query list */
    int root1;

    /** Index of the next logical segment */
    int next;

    /** Index of the previous logical segment */
    int prev;

    SeidelSegment()
    {
        v0 = new Point2d();
        v1 = new Point2d();
    }

    /**
     * Reset everything back to zero again.
     */
    void clear()
    {
        v0.x = 0;
        v0.y = 0;
        v1.x = 0;
        v1.y = 0;

        root0 = 0;
        root1 = 0;
        next = 0;
        prev = 0;
    }

}
