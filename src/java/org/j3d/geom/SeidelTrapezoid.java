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
import javax.vecmath.Point2f;

// Local imports
// None

/**
 * Attributes of a trapezoid used in the Siedel triangulation routines.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class SeidelTrapezoid
{
    int leftSegment;
    int rightSegment;

    Point2f hi;
    Point2f lo;

    int u0;
    int u1;
    int d0;
    int d1;
    int sink;
    int usave;
    boolean mergeSideLeft;

    boolean valid;

    SeidelTrapezoid()
    {
        valid = false;
        mergeSideLeft = false;
        hi = new Point2f();
        lo = new Point2f();
    }

    /**
     * Reset everything back to zero again.
     */
    void clear()
    {
        hi.x = 0;
        hi.y = 0;
        lo.x = 0;
        lo.y = 0;

        leftSegment = 0;
        rightSegment = 0;
        u0 = 0;
        u1 = 0;
        d0 = 0;
        d1 = 0;
        sink = 0;
        usave = 0;
        mergeSideLeft = false;
        valid = false;
    }
}