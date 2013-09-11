/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.ui.collision;

// Standard imports

// Application specific imports


/**
 * Implementation of the Q-Collide n-body collision detection technique.
 * <P>
 *
 *
 * @author Justin Couch
 * @version $Revision $
 */
public class QCollide
{
    /*
     *  Tables to index the Di_V cofactor table in Johnson's Algorithm. The s,i
     *  entry indicates where to store the cofactors computed with Is_C.
     */
    private static int[][][] jo_2  = { { {0,0}, {2,1} }, { {2, 0}, {0, 0} } };

    private static int[][][] jo_3  = { {{0,0}, {3,1}, {4,2}},
                                       {{3,0}, {0,0}, {5,2}},
                                       {{4,0}, {5,1}, {0,0}},
                                       {{0,0}, {0,0}, {6,2}},
                                       {{0,0}, {6,1}, {0,0}},
                                       {{6,0}, {0,0}, {0,0}}};

    private static int[][][] jo_4 = { { {0,0}, {4,1}, {5,2}, {6,3}},
                                        { {4,0}, {0,0}, {7,2}, {8,3}},
                                        { {5,0}, {7,1}, {0,0}, {9,3}},
                                        { {6,0}, {8,1}, {9,2}, {0,0}},
                                        { {0,0}, {0,0},{10,2},{11,3}},
                                        { {0,0},{10,1}, {0,0},{12,3}},
                                        { {0,0},{11,1},{12,2}, {0,0}},
                                        {{10,0}, {0,0}, {0,0},{13,3}},
                                        {{11,0}, {0,0},{13,2}, {0,0}},
                                        {{12,0},{13,1}, {0,0}, {0,0}},
                                        { {0,0}, {0,0}, {0,0},{14,3}},
                                        { {0,0}, {0,0},{14,2}, {0,0}},
                                        { {0,0},{14,1}, {0,0}, {0,0}},
                                        {{14,0}, {0,0}, {0,0}, {0,0}}};


    /**
     *  These tables represent each Is.  The first column of each row indicates
     *  the size of the set.
     */
    private static int[][] Is_2 = { {1,0,0}, {1,1,0}, {2,0,1}};

    private static int[][] Is_3 = { {1,0,0,0}, {1,1,0,0}, {1,2,0,0},
                                    {2,0,1,0}, {2,0,2,0}, {2,1,2,0},
                                    {3,0,1,2}};

    private static int[][] Is_4 = { {1,0,0,0,0}, {1,1,0,0,0}, {1,2,0,0,0},
                                       {1,3,0,0,0}, {2,0,1,0,0}, {2,0,2,0,0},
                                       {2,0,3,0,0}, {2,1,2,0,0}, {2,1,3,0,0},
                                       {2,2,3,0,0}, {3,0,1,2,0}, {3,0,1,3,0},
                                       {3,0,2,3,0}, {3,1,2,3,0}, {4,0,1,2,3}};

    /**
     *  These tables represent each Is complement. The first column of each row
     *  indicates the size of the set.
     */
    private static int[][] IsC_2 = { {1,1}, {1,0}, {0,0}};

    private static int[][] IsC_3 = { {2,1,2}, {2,0,2}, {2,0,1},
                                       {1,2,0}, {1,1,0}, {1,0,0},
                                       {0,0,0}};

    private static int[][] IsC_4 = { {3,1,2,3}, {3,0,2,3}, {3,0,1,3}, {3,0,1,2},
                                     {2,2,3,0}, {2,1,3,0}, {2,1,2,0}, {2,0,3,0},
                                     {2,0,2,0}, {2,0,1,0}, {1,3,0,0}, {1,2,0,0},
                                     {1,1,0,0}, {1,0,0,0}, {0,0,0,0}};

    /** Number of combinatorial values to check */
    private static int[] COMBINATIONS = {0, 0, 3, 7, 15};

    /** Flat list of the vertices in 2D form of a given polytope */
    private CollisionPolytope[] polytopeList;

    /** Current number of polytopes in the valid list */
    private int numPolytopes;

    /** Grid representing bbox collisions current for n polytopes. */
    private boolean[][] bboxCollisionInProgress;

    /** CollisionPair instances looked up by p, q */
    private CollisionPair[] currentCollisions;

    /** Working matrix for the S vector */
    private double[] sVec;

    /** counter for each time searchForSupportVertex called */
    private long currentTime;

    /** Work vectors */
    private float[] wkVec0;
    private float[] wkVec1;

    /** Prev rotation and translation vectors */
    private double[] prevMp;
    private double[] prevMq;
    private double[] previRp;
    private double[] previRq;

    private double[] Cp;
    private double[] V;
    private double[] D_V;
    private double[][] Di_V;
    private int[] P1_ia, P2_ia;

    public QCollide()
    {
        prevMp = new double[16];
        prevMq = new double[16];
        previRp = new double[16];
        previRq = new double[16];

        sVec = new double[3];
        currentTime = 0;

        D_V = new double[15];
        Di_V = new double[15][4];
        P1_ia = new int[1];
        P2_ia = new int[1];
        Cp = new double[3];
        V = new double[4];

        currentCollisions = new CollisionPair[2];
    }

    /**
     * Check for collisions between all of the objects and return true if there
     * has been a collision.
     */
    public boolean checkCollisions()
    {
        return false;
    }

    /**
     * Check a pair of objects for collision. Return true if there has been
     *
     * @param pair The polytope that is being checked - P and Q
     * @param rq The rotational matrix of P
     * @param rq The rotational matrix of Q
     * @param irq The inverese rotational matrix of P
     * @param irq The inverese rotational matrix of Q
     * @param tp The translation vector of P
     * @param tq The translation vector of Q
     */
    private boolean checkCollisionPair(CollisionPair pair,
                                       float[] rp,
                                       float[] rq,
                                       float[] irp,
                                       float[] irq,
                                       float[] tp,
                                       float[] tq)
    {
        // The two current points index
        int p, q;

        int pIdx = pair.polytopes[0];
        int qIdx = pair.polytopes[1];
        CollisionPolytope poly_p = polytopeList[pIdx];
        CollisionPolytope poly_q = polytopeList[qIdx];

        // Is this the first time we've had a collision based on the
        // bounds? If not, fetch the cached copy, else compute S
        if(!pair.colliding)
        {
            float x = tq[0] - tp[0];
            float y = tq[1] - tp[1];
            float z = tq[2] - tp[2];

            float d = x * x + y * y + z * z;
            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                x *= d;
                y *= d;
                z *= d;
            }

            sVec[0] = x;
            sVec[1] = y;
            sVec[2] = z;

            pair = currentCollisions[pIdx][qIdx];
            pair.active = true;
            pair.colliding = true;

            pair.separationVector[0] = x;
            pair.separationVector[1] = y;
            pair.separationVector[2] = z;

//            p =
//            q =
        }
        else
        {
            pair = currentCollisions[pIdx][qIdx];

            sVec[0] = pair.separationVector[0];
            sVec[1] = pair.separationVector[1];
            sVec[2] = pair.separationVector[2];

            p = pair.closestFeatures[0];
            q = pair.closestFeatures[1];
        }

        // Save tx/rotation matrices from prev timestamp before we trash it
        // with the new values.
        System.arraycopy(poly_p.invRotationMatrix, 0, previRp, 0, 16);
        System.arraycopy(poly_q.invRotationMatrix, 0, previRq, 0, 16);

        System.arraycopy(poly_p.transformationMatrix, 0, prevMp, 0, 16);
        System.arraycopy(poly_q.transformationMatrix, 0, prevMq, 0, 16);

        System.arraycopy(irp, 0, poly_p.invRotationMatrix, 0, 16);
        System.arraycopy(irq, 0, poly_q.invRotationMatrix, 0, 16);

        poly_p.transformationMatrix[3] = tp[0];
        poly_p.transformationMatrix[6] = tp[1];
        poly_p.transformationMatrix[9] = tp[2];

        poly_q.transformationMatrix[3] = tq[0];
        poly_q.transformationMatrix[6] = tq[1];
        poly_q.transformationMatrix[9] = tq[2];


        int k = 0;
        int n = 0;
        int prev_p = 0;
        int prev_q = 0;

        // Infinitely loop. Will break out internally
        boolean collision_found = false;
        boolean keep_searching = true;
        boolean repeat = false;

        while(keep_searching)
        {
            k++;
            prev_p = p;
            prev_q = q;

            p = searchForSupportVertex(poly_p.vertices,
                                       poly_p.neighbours,
                                       poly_p.vertexTimestamps,
                                       p,
                                       sVec);

            q = searchForSupportVertex(poly_q.vertices,
                                       poly_q.neighbours,
                                       poly_q.vertexTimestamps,
                                       p,
                                       sVec);

            matrixLeftMult(q, rq, wkVec0);
            matrixLeftMult(p, rp, wkVec1);

            float rk_x = wkVec0[0] + tq[0] - wkVec1[0] - tp[0];
            float rk_y = wkVec0[1] + tq[1] - wkVec1[1] - tp[1];
            float rk_z = wkVec0[2] + tq[2] - wkVec1[2] - tp[2];

            float d = rk_x * rk_x + rk_y * rk_y + rk_z * rk_z;
            if(d != 0)
            {
                d = (float)Math.sqrt(d);
                rk_x /= d;
                rk_y /= d;
                rk_z /= d;
            }

            double dp = rk_x * sVec[0] + rk_y * sVec[1] + rk_z * sVec[2];

            if(dp >= 0 || (p == prev_p && q == prev_q))
            {
                // Save to cache
                pair.closestFeatures[0] = p;
                pair.closestFeatures[1] = q;
                pair.separationVector[0] = sVec[0];
                pair.separationVector[1] = sVec[1];
                pair.separationVector[2] = sVec[2];

                collision_found = false;
                keep_searching = false;
                continue;
            }

            if(visited[p][q])
            {
                if(repeat)
                {
                    // Save to cache
                    pair.closestFeatures[0] = p;
                    pair.closestFeatures[1] = q;
                    pair.separationVector[0] = sVec[0];
                    pair.separationVector[1] = sVec[1];
                    pair.separationVector[2] = sVec[2];

                    collision_found = false;
                    keep_searching = false;
                    continue;
                }

                sVec[0] = w[0];
                sVec[1] = w[1];
                sVec[2] = w[2];

                repeat = true;
                if(n < 4 && !vertexSetP[p] && vertexSetQ[q])
                {
                    vertexSet[n] = p;
                    vertexSet[n] = q;
                    n++;
                }
            }
            else
            {
                repeat = false;
                if(k == 2)
                    w = r1 + r2;

                if(!findHalfPlane(set_r, k, w, r_kx, rk_y, rk_z))
                {
                    if(!pair.colliding)
                    {
                        if(n == 0)
                        {
                            vertexSetP[0] = p;
                            vertexSetQ[0] = p;
                            vertexSetP[1] = prev_p;
                            vertexSetQ[1] = prev_q;

                            n = 2;
                        }

                        calcGilbertPoints(vertexSetP,
                            vertexSetQ,
                            n,
                            lambda,
                            prev_Mp,
                            prev_Mq,
                            prev_irp,
                            prev_irq,
                            wkVec0,
                            wkVec1);
                        sVec[0] = wkVec1[0] - wkVec0[0];
                        sVec[1] = wkVec1[1] - wkVec0[1];
                        sVec[2] = wkVec1[2] - wkVec0[2];

                        p = vertexSetP[0];
                        q = vertexSetQ[0];

                        // Save items in cache
                        pair.closestFeatures[0] = p;
                        pair.closestFeatures[1] = q;
                        pair.separationVector[0] = sVec[0];
                        pair.separationVector[1] = sVec[1];
                        pair.separationVector[2] = sVec[2];
                        pair.colliding = true;
                    }

                    collision_found = true;
                    keep_searching = false;
                    continue;
                }

                // Save vertices p & q in cache
                pair.closestFeatures[0] = p;
                pair.closestFeatures[1] = q;

                sVec[0] -= 2 * dp * rk_x;
                sVec[1] -= 2 * dp * rk_y;
                sVec[2] -= 2 * dp * rk_z;
            }
        }
    }

    /**
     * Search for a supporting vertex in the polytope.
     *
     * @param shape The vertices of the shape in 2D form
     * @param p The initial vertex of the shape
     * @param s The vector in the local coordinate system of the shape
     * @return The index of the supporting vector in the shape
     */
    private int searchForSupportVertex(float[][] shape,
                                       int[][] neighbours,
                                       long[] timestamps,
                                       int p,
                                       float[] s)
    {
        int new_p = p;
        float max = shape[p][0] * s[0] + shape[p][1] * s[1] + shape[p][2] * s[2];

        currentTime++;
        if(currentTime < 0)
        {
            currentTime = 0;
            // reset timestamp of _all_ vertices. Luckily this will happen
            // very, very rarely.
            for(int i = 0; i < polytopeList.length; i++)
                for(int j = 0; j < polytopeList[i].vertexTimestamps.length; j++)
                    polytopeList[i].vertexTimestamps[j] = 0;

        }

        // assign p's timestamp
        timestamps[p] = currentTime;

        do
        {
            p =  new_p;
            int[] v_n = neighbours[p];

            for(int i = 0; i < v_n.length; i++)
            {
                int n_vtx = v_n[i];
                // Has v been visited before?
                if(timestamps[n_vtx] != currentTime)
                {
                    float dp = shape[n_vtx][0] * s[0] +
                               shape[n_vtx][1] * s[1] +
                               shape[n_vtx][2] * s[2];
                    if(dp > max)
                    {
                        max = dp;
                        new_p = p;
                    }

                    timestamps[n_vtx]  = currentTime;
                }
            }
        }
        while(new_p != p);

        return p;
    }

    /**
     * Find the half-plane for the set of vertices, if it exists.
     *
     * @param setR Array to hold vectors
     * @param kVal The size of the setR. Array so the new size can be returned.
     * @param w Normal of the half-plane for setR
     * @param newR Vector to be added to newR
     * @return true if the halfplane can be found after newR is inserted
     */
    private boolean findHalfPlane(float[][] setR, int[] kVal, float[] w, float[] newR)
    {
        int k = kVal[0];

        setR[k][0] = newR[0];
        setR[k][1] = newR[1];
        setR[k][2] = newR[2];

        k++;
        kVal[0] = k;

        float dp = w[0] * newR[0] + w[1] * newR[1] + w[2] * newR[2];
        if(dp >= 0)
            return true;

        // Compute matrix M
        // The rotation matrix from vector (x, y, z) to z axis is equal to
        // normalize each row of the following matrix :
        // [ xz  yz   -(x^2 + y^2)]   [m00 m01 m02]
        // [ -y  x       0        ] = [m10 m11 0  ]
        // [ x   y       z        ]   [m20 m21 m22]

        double m00, m01, m02, m10, m11, m20, m21, m22, t1, t2, t3;

        m20 = newR[0];
        m21 = newR[1];
        m22 = newR[2];

        t2 = m20 * m20 + m21 * m21;
        t1 = Math.sqrt(t2);
        m10 = -m21 / t1;
        m11 = m20 / t1;

        t1 = t2 + m22 * m22;
        t3 = Math.sqrt(t1 * t2);

        m22 /= t3;
        m00 = m20 * m22;
        m01 = m21 * m22;
        m02 = -t2 / t3;

        // Project all points on the Z plane after rotation
        float[] ptr = setR[0];
        double ra_x = ptr[0] * m00 + ptr[1] * m01 + ptr[2]*m02;
        double ra_y = ptr[0] * m10 + ptr[1] * m11;

        ptr = setR[1];
        double rb_x = ptr[0]*m00 + ptr[1]*m01 + ptr[2]*m02;
        double rb_y = ptr[0]*m10 + ptr[1]*m11;

        // Make both vectors point in the same direction
        if((ra_x * rb_y - ra_y * rb_x) < 0)
        {
            double x = ra_x;
            double y = ra_y;
            ra_x = rb_x;
            ra_y = rb_y;
            rb_x = x;
            rb_y = y;
        }

        for(int i = 2; i < k - 1; i++)
        {
            // M * setR[i]
            ptr = setR[i];
            double t_x = ptr[0] * m00 + ptr[1] * m01 + ptr[2] * m02;
            double t_y = ptr[0] * m10 + ptr[1] * m11;

            if((ra_x * t_y - ra_y * t_x) > 0)
            {
                if((rb_x * t_y - rb_y * t_x) > 0)
                {
                    rb_x = t_x;
                    rb_y = t_y;
                }
            }
            else
            {
                if((rb_x * t_y - rb_y * t_x) < 0)
                {
                    ra_x = t_x;
                    ra_y = t_y;
                }
                else
                {
                    return true;
                }
            }
        }

        float dist = (float)Math.sqrt(ra_x * ra_x + ra_y * ra_y);
        ra_x /= dist;
        ra_y /= dist;

        dist = (float)Math.sqrt(rb_x * rb_x + rb_y * rb_y);
        rb_x /= dist;
        rb_y /= dist;

        t1 = (ra_x + ra_x);
        t2 = (rb_y + rb_y);
        w[0] = (float)((t1 * m00 + t2 * m10) / t1);
        w[1] = (float)((t1 * m01 + t2 * m11) / t1);
        w[2] = (float)m02;    // (t1 * m02) / t1

        return false;
    }

    /**
     * The Improved Gilbert Algorithm implementation to find the intersection
     * points. The pair of closest points is given by
     * <pre>
     * closest P = vertexSetP[0] * lambda[0] + ... + vertexSetP[n - 1] * lambda[n - 1]
     * closest Q = vertexSetQ[0] * lambda[0] + ... + vertexSetQ[n - 1] * lambda[n - 1]
     * </pre>
     *
     * @param vertexSetP The initial vertex set of P
     * @param vertexSetQ The initial vertex set of Q
     * @param n The number of items in the vertex sets. Must be <= 4
     * @param lambda Array to return lambda value in
     * @param Mp Transformation matrix of P
     * @param Mq Transformation matrix of Q
     * @param invRp Inverse rotation matrix of P
     * @param invRq Inverse rotation matrix of Q
     * @return the real size of the set needed to comput closestP, Q.
     */
    private int calcGilbertPoints(CollisionPair collision,
                                  float[][] vertexSetP,
                                  float[][] vertexSetQ,
                                  int n,
                                  double[] lambda,
                                  double[][] Mp,
                                  double[][] Mq,
                                  double[][] invRp,
                                  double[][] invRq,
                                  double[] closestP,
                                  double[] closestQ)
    {
        double t1_x, t1_y, t1_z;
        double t2_x, t2_y, t2_z;

        for(int i = 0; i < n; i++)
        {
            // 1. p[i] * Mp
            t1_x = Mp[0][0] * vertexSetP[i][0] + Mp[0][1] * vertexSetP[i][1] +
                   Mp[0][2] * vertexSetP[i][2] + Mp[0][3];
            t1_y = Mp[1][0] * vertexSetP[i][0] + Mp[1][1] * vertexSetP[i][1] +
                   Mp[1][2] * vertexSetP[i][2] + Mp[1][3];
            t1_z = Mp[2][0] * vertexSetP[i][0] + Mp[2][1] * vertexSetP[i][1] +
                   Mp[2][2] * vertexSetP[i][2] + Mp[2][3];

            // 2. q[i] * Mq
            t2_x = Mq[0][0] * vertexSetQ[i][0] + Mq[0][1] * vertexSetQ[i][1] +
                   Mq[0][2] * vertexSetQ[i][2] + Mq[0][3];
            t2_y = Mq[1][0] * vertexSetQ[i][0] + Mq[1][1] * vertexSetQ[i][1] +
                   Mq[1][2] * vertexSetQ[i][2] + Mq[1][3];
            t2_z = Mq[2][0] * vertexSetQ[i][0] + Mq[2][1] * vertexSetQ[i][1] +
                   Mq[2][2] * vertexSetQ[i][2] + Mq[2][3];

            // 2 - 1
            V[k][0] = t2_x - t1_x;
            V[k][1] = t2_y - t1_y;
            V[k][2] = t2_z - t1_z;
        }

        int p = 0;
        int q = 0;

        float[] rp_cp = wkVec0;
        float[] rq_cp = wkVec1;

        // Reset the seenBefore flags of both polytopes.
        CollisionPolytope poly_p = polytopeList[collision.polytopes[0]];
        CollisionPolytope poly_q = polytopeList[collision.polytopes[1]];

        for(int i = poly_p.seenBefore.length; --i >= 0; )
            poly_p.seenBefore[i] = false;

        for(int i = poly_q.seenBefore.length; --i >= 0; )
            poly_q.seenBefore[i] = false;

        // Find the closest point Cp for the simplex V using Johnson's
        // Algorithm. The solution of the affine independent set is saved in
        // vertex. The size of the affine independent set is saved in n, which
        // must be <= 3. The solution of lambda and closest point is saved in
        // lambda[] and Cp. is contains the index f the resulting affine
        // independent set vertex.
        int real_n = n;

        while(true)
        {
            real_n = computeJohnson(V, real_n, lambda, Cp, foundIs);

            // Update vertexSets based on the selected Is values from
            // Johnson's algorithm.
            for(int l = 0; l < real_n; i++)
            {
                int idx = foundIs[l][0];
                vertexSetP[l][0] = vertexSetP[idx][0];
                vertexSetP[l][1] = vertexSetP[idx][1];
                vertexSetP[l][2] = vertexSetP[idx][2];

                vertexSetQ[l][0] = vertexSetQ[idx][0];
                vertexSetQ[l][1] = vertexSetQ[idx][1];
                vertexSetQ[l][2] = vertexSetQ[idx][2];
            }

            // calc invRp * Cp and -invRq * Cp
            rp_cp[0] = invRp[0][0] * Cp[0] + invRp[0][1] * Cp[1] +
                       invRp[0][2] * Cp[2] + invRp[0][3];
            rp_cp[1] = invRp[1][0] * Cp[0] + invRp[1][1] * Cp[1] +
                       invRp[1][2] * Cp[2] + invRp[1][3];
            rp_cp[2] = invRp[2][0] * Cp[0] + invRp[2][1] * Cp[1] +
                       invRp[2][2] * Cp[2] + invRp[2][3];

            rq_cp[0] = -invRq[0][0] * Cp[0] + -invRq[0][1] * Cp[1] +
                       -invRq[0][2] * Cp[2] + -invRq[0][3];
            rq_cp[0] = -invRq[1][0] * Cp[0] + -invRq[1][1] * Cp[1] +
                       -invRq[1][2] * Cp[2] + -invRq[1][3];
            rq_cp[0] = -invRq[2][0] * Cp[0] + -invRq[2][1] * Cp[1] +
                       -invRq[2][2] * Cp[2] + -invRq[2][3];

            // Find supporting vertex of P/Q in the direction of Cp
            p = searchForSupportVertex(poly_p.vertices,
                                       poly_p.neighbours,
                                       poly_p.vertexTimestamps,
                                       p,
                                       rp_cp);
            q = searchForSupportVertex(poly_q.vertices,
                                       poly_q.neighbours,
                                       poly_q.vertexTimestamps,
                                       q,
                                       rq_cp);

            if(poly_p.seenBefore[p] && poly_q.seenBefore[q])
                return 0;

            poly_p.seenBefore[p] = true;
            poly_q.seenBefore[q] = true;

            vertexSetP[n][0] = poly_p.vertices[p][0];
            vertexSetP[n][1] = poly_p.vertices[p][1];
            vertexSetP[n][2] = poly_p.vertices[p][2];

            vertexSetQ[n][0] = poly_q.vertices[q][0];
            vertexSetQ[n][1] = poly_q.vertices[q][1];
            vertexSetQ[n][2] = poly_q.vertices[q][2];

            real_n++;
        }

        return real_n;
    }

    /**
     * left matrix multiply vec * matrix
     */
    private void matrixLeftMult(float[] vec, float[] mat, float[] res)
    {
    }

    /**
     * left matrix multiply matrix * vec
     */
    private void matrixRightMult(float[] vec, float[] mat, float[] res)
    {
    }

    /**
     * Function to compute the point in a polytope closest to the origin in
     * 3-space.  The polytope size m is restricted to 1 < m <= 4.
     *
     * @param V Table of 3-element points containing polytope's vertices
     * @param n Number of points in P
     * @param nearPnt An empty array of size 3 - contains the point in P closest to the origin on exit
     * @param nearIndx An empty array of size 4  - indices for a subset of P which is affinely independent
     *  on exit. See eq. (14)
     * @param lambda An empty array of size 4 - the lambda as in eq. (14) on exit
     * @return The number of entries in nearIndx and lambda.
     */
    private int computeJohnson(double[][] V,
                               int n,
                               double[] nearPnt,
                               int[] nearIndx,
                               double[] lambda,
                               int[] Is)
    {
        int size = 0;

        // Call computeSubDist with appropriate tables according to size of P
        switch(n)
        {
            case 2:
                size = computeSubDist(V, n, jo_2, Is_2, IsC_2, nearPnt,
                                      nearIndx, lambda, Is);
                break;
            case 3:
                size = computeSubDist(V, n, jo_3, Is_3, IsC_3, nearPnt,
                                      nearIndx, lambda, Is);
                break;

            case 4:
                 size = computeSubDist(V, n, jo_4, Is_4, IsC_4, nearPnt,
                                       nearIndx, lambda, Is);
                break;
       }

       return size;
    }

    /**
     * Function to compute the point in a polytope closest to the origin in
     * 3-space. The polytope size m is restricted to 1 < m <= 4.
     *
     * @param V Table of 3-element points containing polytope's vertices
     * @param m Number of points in P
     * @param jo Table of indices for storing Dj_P cofactors in Di_V
     * @param Is Indices into P for all sets of subsets of P
     * @param IsC Indices into P for complement sets of Is
     * @param nearPnt an empty array of size 3 for the point in P closest
     *   to the origin
     * @param nearIndx an empty array of size 4 indices for a subset of P
     *   which is affinely independent.
     * @param lambda an empty array of size 4
     * @return The number of entries in nearIndx and lambda.
     */
    private int computeSubDist(double[][] V,
                               int m,
                               int[][][] jo,
                               int[][] Is,
                               int[][] IsC,
                               double[] nearPnt,
                               int[] nearIndx,
                               double[] lambda,
                               int[] foundIs)
    {
        boolean pass = false;
        boolean fail;
        int i, j, k, isp, is;
        int s = 0;

        // how many subsets in P?
        int stop_index = COMBINATIONS[m];

        // row offsets for IsC and Is
        int c1 = m;
        int c2 = m + 1;

        // Initialize Di_V for singletons
        Di_V[0][0] = 1;
        Di_V[1][1] = 1;
        Di_V[2][2] = 1;
        Di_V[3][3] = 1;

        s = 0;

        // loop through each subset
        while((s < stop_index) && (!pass))
        {
            D_V[s] = 0.0;
            fail = false;
            int num_combos = Is[s][0];

            // loop through all Is
            for(i = 1; i <= num_combos; i++)
            {
                is = Is[s][i];
                if (Di_V[s][is] > 0.0)    // Condition 2 Theorem 2
                    D_V[s] += Di_V[s][is]; // sum from eq. (16)
                else
                    fail = true;
            }

            // loop through all IsC
            for(j = 1; j <= IsC[s][0]; j++)
            {
                int Dj_P = 0;
                k = Is[s][1];
                isp = IsC[s][j];

                for(i = 1; i <= num_combos; i++)
                {
                    is = Is[s][i];

                    // Wk - Wj  eq. (18)
                    double x_0 = V[k][0] - V[isp][0];
                    double x_1 = V[k][1] - V[isp][1];
                    double x_2 = V[k][2] - V[isp][2];

                    // sum from eq. (18)
                    double dot = V[is][0] * x_0 +
                                V[is][1] * x_1 +
                                V[is][2] * x_2;

                    Dj_P += Di_V[s][is] * dot;
                }


                // add new cofactors
                int row = jo[s][isp][0];
                int col = jo[s][isp][1];
                Di_V[row][col] = Dj_P;

                // Condition 3 Theorem 2
                if(Dj_P > 0.0)
                {
                    fail = true;
                    // copy the Is over into the provided array
                    for(int l = 0; l < Is[s].length; l++)
                        foundIs[i] = Is[s][l];
                }
            }

            // Conditions 2 && 3 && 1 Theorem 2
            if((!fail) && (D_V[s] > 0.0))
                pass = true;
            else
                s++;
        }

        if(!pass)
            s = alternateSubDist(V, stop_index, D_V, Di_V, Is, c2);

        nearPnt[0] = 0;
        nearPnt[1] = 0;
        nearPnt[2] = 0;

        j = 0;

        // loop through all Is
        int num_combos = Is[s][0];
        for(i = 1; i <= num_combos; i++)
        {
            is = Is[s][i];
            nearIndx[j] = is;
            lambda[j] = Di_V[s][is] / D_V[s];

            nearPnt[0] += lambda[j] * V[is][0];
            nearPnt[1] += lambda[j] * V[is][1];
            nearPnt[2] += lambda[j] * V[is][2];
            j++;
        }

        return i - 1;
    }

    /**
     * Alternate function to compute the point in a polytope closest to the
     * origin in 3-space when the normal Johnson's Algorithm fails to find an
     * answer. The polytope size m is restricted to 1 < m <= 4. This function is
     * called only when computeSubDist fails.
     *
     * @param stopIndex Number of sets to test.
     * @param D_V Array of determinants for each set.
     * @param Di_V Cofactors for each set.
     * @param Is Indices for each set.
     * @return The index of the set that is numerically closest to eq. (14).
     */
    private int alternateSubDist(double[][] V,
                                 int stopIndex,
                                 double[][] D_V,
                                 double[][] Di_V,
                                 int[][] Is)
    {
        boolean first = true;
        boolean pass = false;
        int is;
        int best_s = -1;
        double best_v_aff = 0;

        for(int s = 0; s < stopIndex; s++)
        {
            pass = true;
            if(D_V[s] > 0.0)
            {
                int num_combos = Is[s][0];
                for(int i = 1; i <= num_combos; i++)
                {
                    is = Is[s][i];
                    if(Di_V[s][is] <= 0)
                        pass = false;
                }
            }
            else
                pass = false;

            if(pass)
            {
                // Compute equation (33) in Gilbert
                int k = Is[s][1];
                double sum = 0;
                int num_combos = Is[s][0];
                for(int i = 1; i <= num_combos; i++)
                {
                    is = Is[s][i];
                    double dot = V[is][0] * V[k][0] +
                                 V[is][1] * V[k][1] +
                                 V[is][2] * V[k][2];

                    sum += Di_V[s][is] * dot;
                }

                double v_aff = Math.sqrt(sum / D_V[s]);
                if(first)
                {
                    best_s = s;
                    best_v_aff = v_aff;
                    first = false;
                }
                else
                {
                    if(v_aff < best_v_aff)
                    {
                       best_s = s;
                       best_v_aff = v_aff;
                    }
                }
            }
        }

        if(best_s == -1)
        {
        //        printf("backup failed\n");
            best_s = 0;
        }

        return best_s;
    }

    /**
     * Function to compute the minimum distance between two convex polytopes in
     * 3-space.
     *
     * @param Mp    - transformation matrix of P
     * @param InvMp - inverse transformation matrix of P
     * @param Mq    - transformation matrix of Q
     * @param InvMp - inverse transformation matrix of Q
     * @param closestP an empty array of size 3 containing the closest
     *    point of polytope P in world coordinates
     * @param closestQ an empty array of size 3 containing the closest
     *    point of polytope Q in world coordinates
     * @param nearIndxP Array of size 4 possibly containing the vertices of
     *    initialization for P. Returns with updated indices into P which
     *    indicate the affinely independent point sets from each polytope which
     *    can be used to compute along with lambda the near points in P1 and P2
     * @param nearIndxQ Array of size 4 possibly containing the vertices of
     *    initialization for Q. Returns with updated indices into Q which
     *    indicate the affinely independent point sets from each polytope which
     *    can be used to compute along with lambda the near points in P1 and P2
     * @param setSize Indicates how many initial points to extract from nearIndxP
     *    and nearIndxQ, must be at least 1.
     * @param VP An empty array of size 3 for the unit vector difference of
     *     the above two near points i.e. closestq - closestp / |closestq - closestp|
     * @param lambda An empty array of size 4.
     * @param m3 A pointer to an int to contain the updated number of indices
     *    for P and Q in nearIndxP, nearIndxP
     * @param distance An array of size 1 for to the closest distance
     *    between P and Q i.e. |closestq - closestp|
     * @param closestFeatures Array of size 2 to continat the index of the
     *    supporting vertices of Q and P in the direction VP and -VP respectively.
     */
    private boolean computeClosestPoint(CollisionPair collidables,
                                        double[][] Mp,
                                        double[][] InvMp,
                                        double[][] Mq,
                                        double[][] InvMq,
                                        double[] closestP,
                                        double[] closestQ,
                                        int[] nearIndxP,
                                        int[] nearIndxQ,
                                        int setSize,
                                        double[] VP,
                                        double[] lambda,
                                        int[] m3,
                                        double[] distance,
                                        int[] closestFeatures)
    {
        int[] I = new int[4];
        int i, j;
        int loopcount = 0;

        double[][] Pk = new double[4][3]; 
        double[][] Pk_subset = new double[4][3];
        double[] Cp = new double[3];
        double lda;
        double[] neg_Vk = new double[3];

        int P1_i = 0;
        int P2_i = 0;
        int lastP1 = 0;
        int lastP2 = 0;
        int prevP1 = 0;
        int prevP2 = 0;

        double t1_x, t1_y, t1_z;
        double t2_x, t2_y, t2_z;

        double[] oldVP = new double[3];

        CollisionPolytope poly_p = polytopeList[collidables.polytopes[0]];
        CollisionPolytope poly_q = polytopeList[collidables.polytopes[1]];
        float[] coords;

        for(int k = 0; k < setSize; k++)
        {
            coords = poly_p.vertices[nearIndxP[k]];

            t1_x = Mp[0][0] * coords[0] + Mp[0][1] * coords[1] +
                   Mp[0][2] * coords[2] + Mp[0][3];
            t1_y = Mp[1][0] * coords[0] + Mp[1][1] * coords[1] +
                   Mp[1][2] * coords[2] + Mp[1][3];
            t1_z = Mp[2][0] * coords[0] + Mp[2][1] * coords[1] +
                   Mp[2][2] * coords[2] + Mp[2][3];

            coords = poly_q.vertices[nearIndxQ[k]];

            t2_x = Mq[0][0] * coords[0] + Mq[0][1] * coords[1] +
                   Mq[0][2] * coords[2] + Mq[0][3];
            t2_y = Mq[1][0] * coords[0] + Mq[1][1] * coords[1] +
                   Mq[1][2] * coords[2] + Mq[1][3];
            t2_z = Mq[2][0] * coords[0] + Mq[2][1] * coords[1] +
                   Mq[2][2] * coords[2] + Mq[2][3];

            Pk[k][0] = t2_x - t1_x;
            Pk[k][1] = t2_y - t1_y;
            Pk[k][2] = t2_z - t1_z;
        }

        P1_i = nearIndxQ[0];
        P2_i = nearIndxP[0];
        int set_size = 0;
        
        while(true)
        {
            if(set_size == 1)
            {
                VP[0] = Pk[0][0];
                VP[1] = Pk[0][1];
                VP[2] = Pk[0][2];

                I[0] = 0;
                lambda[0] = 1.0;
            }
            else
                set_size = computeSubDist(Pk, set_size, VP, I, lambda);

            // extract affine subset of Pk
            for(i = 0; i < set_size; i++)
            {
                j = I[i];
                Pk_subset[i][0] = Pk[j][0];
                Pk_subset[i][1] = Pk[j][1];
                Pk_subset[i][2] = Pk[j][2];
            }

            // load into Pk+1
            for(i = 0; i < set_size; i++)
            {
                Pk[i][0] = Pk_subset[i][0];
                Pk[i][1] = Pk_subset[i][1];
                Pk[i][2] = Pk_subset[i][2];
            }

            for(i = 0; i < set_size; i++)
            {
                // keep track of indices for P1 and P2
                // j is value from member of some Is

                j = I[i];
                nearIndxQ[i] = nearIndxQ[j];
                nearIndxP[i] = nearIndxP[j];
            }

            if(set_size == 4)
                break;

            prevP1 = lastP1;
            prevP2 = lastP2;
            lastP1 = P1_i;
            lastP2 = P2_i;

            neg_Vk[0] = -VP[0];
            neg_Vk[1] = -VP[1];
            neg_Vk[2] = -VP[2];

            // Copy P1_i and P2_i into an array and the update when they
            // come back.
            P1_ia[0] = P1_i;
            P2_ia[0] = P2_i;
            computeHs(Mp, InvMp, Mq, InvMq, VP, neg_Vk, Cp, P1_ia, P2_ia);

            P1_i = P1_ia[0];
            P2_i = P2_ia[0];

            if(((P1_i == lastP1) && (P2_i == lastP2)) ||
               ((P1_i == prevP1) && (P2_i == prevP2)))
                break;

            // Escape valve in case anything goes wrong
            if(loopcount++ > 30)
                break;

            // Union of Pk+1 with Cp
            Pk[i][0] = Cp[0];
            Pk[i][1] = Cp[1];
            Pk[i][2] = Cp[2];

            nearIndxQ[i] = P1_i;
            nearIndxP[i] = P2_i;
            set_size++;
        }

        lastP2 = nearIndxP[0];
        coords = poly_p.vertices[lastP2];
        closestP[0] = coords[0] * lambda[0];
        closestP[1] = coords[1] * lambda[0];
        closestP[2] = coords[2] * lambda[0];

        lastP1 = nearIndxQ[0];
        coords = poly_q.vertices[lastP1];
        closestQ[0] = coords[0] * lambda[0];
        closestQ[1] = coords[1] * lambda[0];
        closestQ[2] = coords[2] * lambda[0];

        for (i = 1; i < set_size; i++)
        {
            lastP2 = nearIndxP[i];
            lda = lambda[i];
            coords = poly_p.vertices[lastP2];

            closestP[0] += coords[0] * lda;
            closestP[1] += coords[1] * lda;
            closestP[2] += coords[2] * lda;

            lastP1 = nearIndxQ[i];
            coords = poly_q.vertices[lastP1];
            closestQ[0] += coords[0] * lda;
            closestQ[1] += coords[1] * lda;
            closestQ[2] += coords[2] * lda;
        }

        t1_x = Mp[0][0] * closestP[0] + Mp[0][1] * closestP[1] +
               Mp[0][2] * closestP[2] + Mp[0][3];
        t1_y = Mp[1][0] * closestP[0] + Mp[1][1] * closestP[1] +
               Mp[1][2] * closestP[2] + Mp[1][3];
        t1_z = Mp[2][0] * closestP[0] + Mp[2][1] * closestP[1] +
               Mp[2][2] * closestP[2] + Mp[2][3];

        t2_x = Mq[0][0] * closestQ[0] + Mq[0][1] * closestQ[1] +
               Mq[0][2] * closestQ[2] + Mq[0][3];
        t2_y = Mq[1][0] * closestQ[0] + Mq[1][1] * closestQ[1] +
               Mq[1][2] * closestQ[2] + Mq[1][3];
        t2_z = Mq[2][0] * closestQ[0] + Mq[2][1] * closestQ[1] +
               Mq[2][2] * closestQ[2] + Mq[2][3];

        closestP[0] = t1_x;
        closestP[1] = t1_y;
        closestP[2] = t1_z;

        closestQ[0] = t2_x;
        closestQ[1] = t2_y;
        closestQ[2] = t2_z;

        closestFeatures[0] = P2_i;
        closestFeatures[1] = P1_i;
        m3[0] = setSize;

        double d = Math.sqrt(VP[0] * VP[0] + VP[1] * VP[1] + VP[2] * VP[2]);
        distance[0] = d;

        boolean ret_val = true;

        if(d > 0.000001)
        {
            d = 1 / d;
            VP[0] *= d;
            VP[1] *= d;
            VP[2] *= d;
            ret_val = false;
        }

        return ret_val;
    }

    /**
     * Function to evaluate the support and contact functions at A for the
     * set difference of two polytopes. See equations (8) & (9).
     *
     * @param Mp Transformation matrix of P
     * @param invMp Inverse transformation matrix of P
     * @param Mq Transformation matrix of Q
     * @param invMq Inverse transformation matrix of Q
     * @param A Vector at which to evaluate support and contact functions.
     * @param negA Negation of vector A
     * @param Cs Empty array of size 3, returns with solution to eq
     * @param P1_i Initial pointer to a vertex in P and returns the index int
     *   P of the computation.
     * @param P2_i Initial pointer to a vertex in P and returns the index int
     *   Q of the computation.
     */
    private void computeHs(CollisionPolytope P,
                           CollisionPolytope Q,
                           double[][] Mp,
                           double[][] invMp,
                           double[][] Mq,
                           double[][] invMq,
                           double[] A,
                           double[] negA,
                           double[] Cs,
                           int[] P1_i,
                           int[] P2_i)
    {
       double[] Cp_1 = new double[3];
       double[] Cp_2 = new double[3];

       P1_i[0] = computeHp(P, Mq, invMq, negA, Cp_1, P1_i[0]);
       P2_i[0] = computeHp(Q, Mp, invMp, A, Cp_2, P2_i[0]);

       Cs[0] = Cp_1[0] - Cp_2[0];
       Cs[1] = Cp_1[1] - Cp_2[1];
       Cs[2] = Cp_1[2] - Cp_2[2];
    }

    /**
     * Function to evaluate the support and contact functions at A for a given
     * polytope. See equations (6) & (7).
     *
     * @param poly The polytope to start looking in for the contact function
     * @param M transformation matrix
     * @param invM Inverse transformation matrix
     * @param A Vector at which support and contact functions will be evaluated
     * @param Cp  Empty 3-element array to copy contact point of P angle.r.t. A
     * @param initVert Initial vertex for local searching
     * @return index of the closest vertex
     */
    private int computeHp(CollisionPolytope poly,
                          double[][] M,
                          double[][] invM,
                          double[] A,
                          double[] Cp,
                          int initVert)
    {
        double transA_x = invM[0][0] * A[0] + invM[0][1] * A[1] +
                          invM[0][2] * A[2] + invM[0][3];
        double transA_y = invM[1][0] * A[0] + invM[1][1] * A[1] +
                          invM[1][2] * A[2] + invM[1][3];
        double transA_z = invM[2][0] * A[0] + invM[2][1] * A[1] +
                          invM[2][2] * A[2] + invM[2][3];

        float[][] coords = poly.vertices;

        double max_dot = coords[initVert][0] * transA_x +
                         coords[initVert][1] * transA_y +
                         coords[initVert][2] * transA_z;

        boolean cont = true;
        int least_index = 0;

        while(cont)
        {
            cont = false;
            for(int i = initVert + 1; i < coords.length; i++)
            {
                double dot = coords[i][0] * transA_x +
                             coords[i][1] * transA_y +
                             coords[i][2] * transA_z;

                if(dot > max_dot)
                {
                    max_dot = dot;
                    least_index = i;
                    cont = true;
                }
            }
        }

        Cp[0] = M[0][0] * coords[least_index][0] +
                M[0][1] * coords[least_index][1] +
                M[0][2] * coords[least_index][2] + M[0][3];
        Cp[1] = M[1][0] * coords[least_index][0] +
                M[1][1] * coords[least_index][1] +
                M[1][2] * coords[least_index][2] + M[1][3];
        Cp[2] = M[2][0] * coords[least_index][0] +
                M[2][1] * coords[least_index][1] +
                M[2][2] * coords[least_index][2] + M[2][3];

        return least_index;
    }
}
