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
 * Attributes of every node in the query structures used in the Siedel
 * triangulation routines.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class SeidelNode
{
    static final int TYPE_X = 1;
    static final int TYPE_Y = 2;
    static final int TYPE_SINK = 3;

    /** Should be set to one of the TYPE_ constants */
    int nodeType;

    /** Index of the segment this node belongs to */
    int segmentIndex;

    Point2f yVal;

    int trapezoidIndex;
    int parent;
    int leftChild;
    int rightChild;

    SeidelNode()
    {
        yVal = new Point2f();
    }

    /**
     * Reset everything back to zero again.
     */
    void clear()
    {
        nodeType = 0;
        segmentIndex = 0;
        trapezoidIndex = 0;
        parent = 0;
        leftChild = 0;
        rightChild = 0;
        yVal.x = 0;
        yVal.y = 0;
    }
}