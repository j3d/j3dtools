/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.subdivision;

// External imports
// None

// Local imports
// None

/**
 * Base representation for the eigenvector and weighted rule handling for
 * quads.
 * <p>
 *
 * This class should be overridden by the classes that specialise in generating
 * the coefficients for particular sector types.
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ConvexQuadRule extends QuadRule
{
    /**
     * Construct a new default instance of this class
     */
    ConvexQuadRule(int k, float theta)
    {
        super(k, theta, true);

        int i;

        // edgeSub
        edgeSub[1] = 0.0625f;  // 1/16;
        edgeSub[2] = 0.0625f;
        edgeSub[4] = 0.0625f;
        edgeSub[5] = 0.0625f;

        float thetaK = theta / k;

        edgeSub[3] = 0.375f - 0.25f * (float)Math.cos(thetaK);
        edgeSub[0] = 0.75f - edgeSub[3];

        // sub - everything else is zero
        sub.center = 1;

        // Lambdas
        lambda1 = 0.5f;
        lambda2 = 0.5f;

        // l2
        l2.center = -1;
        l2.edge[0] = 1;

        // l0 - everything else is zero
        l0.center = 1;

        // l1
        l1.center = -1;
        l1.edge[k] = 1;

        // x2
        float i_s_t = 1 / (float)Math.sin(theta);

        for(i = 0; i < k + 1; ++i)
            x2.edge[i] = (float)Math.sin(thetaK * (k - i)) * i_s_t;

        for(i = 0; i < k; i++)
        {
            x2.face[i] = (float)(Math.sin(thetaK * (k - i)) +
                          Math.sin(thetaK * (k - i - 1))) * i_s_t;
        }

        // x1
        System.arraycopy(x2.edge, 0, x1.edge, 0, k);

        for(i = 0; i < k; i++)
        {
            x1.face[i] =
                (float)(Math.sin(thetaK * i) + Math.sin(thetaK * (i + 1))) * i_s_t;
        }
    }
}
