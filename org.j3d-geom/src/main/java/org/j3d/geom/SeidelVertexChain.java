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
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class SeidelVertexChain
{
    Point2f point;

    /** Next vertices for the 4 chains */
    int[] nextVertex;

    /** Position of v in the 4 chains */
    int[] vertexPosition;

    int nextFree;

    SeidelVertexChain()
    {
        nextVertex = new int[4];
        vertexPosition = new int[4];
    }

    /**
     * Reset everything back to zero again.
     */
    void clear()
    {
        nextVertex[0] = 0;
        nextVertex[1] = 0;
        nextVertex[2] = 0;
        nextVertex[3] = 0;

        vertexPosition[0] = 0;
        vertexPosition[1] = 0;
        vertexPosition[2] = 0;
        vertexPosition[3] = 0;

        nextFree = 0;
    }
}
