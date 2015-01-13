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
class CollisionPair
{
    /** IDs of the two polygons involved */
    int[] polytopes = new int[2];

    /** IDs of the vertices of the two closest vertices */
    int[] closestFeatures = new int[2];

    /** Current distance between the objects */
    float distance;

    /** Is this pair currently active? */
    boolean active;

    /** Is this pair currently colliding? */
    boolean colliding;

    /** Is this pair currently colliding at the bounds only? */
    boolean boundsColliding;

    /** Next user-activated pair */
    CollisionPair nextActive;

    /** Next and previous pairs that have all 3 coordinates overlapping */
    CollisionPair prevNbodyActive;
    CollisionPair nextNbodyActive;

    /** Next/prev pair in list of colliding pairs */
    CollisionPair nextColliding;
    CollisionPair prevColliding;

    /**
     * Separation vector, point from ids[0] to ids[1]. If collision, this is
     * the vector from the closest feature of ids[0] to ids[1] and magnitude
     * of this vector is the distance between them
     */
    double[] separationVector = new double[3];

    /** Vertex indices of the polytopes involve in closest point calc. */
    int[] indxP = new int[4];
    int[] indxQ = new int[4];

    /** Closest point in polytopes P */
    double[] closestPointP = new double[3];

    /** Closest point in polytopes Q */
    double[] closestPointQ = new double[3];

    /**
     * closestpointq - closestpointp is the direction of sepvector and the
     * magnitude of this is the distance solution of closest point.
     */
    double[] lambda = new double[4];

    /* No. of vertex involve in the lambda solution */
    int nLambda;
}

