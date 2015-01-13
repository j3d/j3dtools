/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.ui.collision;

// External imports

// Local imports


/**
 * Structure representing a pair of objects involved in a collision
 * <p>
 *
 * Every pair of objects that may interact is represented by a pair node The
 * pair node stores the current closest features for that pair, and the current
 * closest distance. It also contains flags to indicate whether the pair is
 * currently active, which dimensions of the pair's bounding boxes are
 * currently overlapping, and whether the pair is currently colliding. In
 * addition, this pair may be inserted and deleted from several lists, so the
 * node contains points used for traversing these lists.
 *
 * @author Justin Couch
 * @version $Revision $
 */
class CollisionPolytope
{
    /** All the vertices in 2D form */
    float[][] vertices;

    /** Index of neighbour vertices for each vertex */
    int[][] neighbours;

    /** List of visitation timestamps */
    long[] vertexTimestamps;

    /** Transformation matrix (translation + rotation) for this frame */
    double[] transformationMatrix;

    /** Inverse rotation matrix */
    double[] invRotationMatrix;

    /** Should this object be currently considered for collisions? */
    boolean isActive;

    /**
     * Flags for each vertex indicating if it has been seen before in this
     * this run of Gilberts Algorithm. Length should be the same as the vertex
     * list.
     */
    boolean[] seenBefore;

    /**
     * Constructor to create new instances of the simple arrays. Vertices,
     * neighbours, vertexTimestamps and seenBefore are left uninitialised.
     */
    CollisionPolytope()
    {
        transformationMatrix = new double[16];
        invRotationMatrix = new double[16];
    }
}
