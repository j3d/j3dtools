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
 * Representation for the eigenvector and weighted coefficients for crease
 * sectors using quads.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class CreaseQuadRule extends QuadRule
{
    /**
     * Construct a new default instance of this class
     */
    CreaseQuadRule(int k, float theta)
    {
        super(k, theta, true);

        int i;
        // edgeSub
        edgeSub[1] = 0.0625f;  // 1/16
        edgeSub[2] = 0.0625f;
        edgeSub[4] = 0.0625f;
        edgeSub[5] = 0.0625f;

        float thetaK = (float)Math.PI / k;

        edgeSub[3] = 0.375f - 0.25f * (float)Math.cos(thetaK);
        edgeSub[0] = 0.75f - edgeSub[3];   // 1 - 2/8 - 3/8 - 1/4 * cos(k)

        // sub
        // sub.face and sub.edge are all zeros except
        sub.center = 0.75f;   // 6/8;
        sub.edge[0] = 0.125f;  // 1/8;
        sub.edge[k] = 0.125f;

        // lambdas
        lambda1 = (k == 1) ? 0.25f : 0.5f;
        lambda2 = 0.5f;

        // l0
        l0.center = 2.0f / 3;
        l0.edge[0] = 1.0f / 6;
        l0.edge[k] = l0.edge[0];

        // l1
        if(k == 1)
        {
            l1.center = 6;
            l1.edge[0] = -3;
            l1.edge[1] = -3;
        }
        else
        {
            float c_k = (float)Math.cos(thetaK);
            float R = (c_k + 1) / (float)(Math.sin(thetaK) * (3 + c_k) * k);

            l1.center = 4 * R * (-1 + c_k);
            l1.edge[0] = -R * (1 + 2 * c_k);
            l1.edge[k] = l1.edge[0];

            for(i = 1; i <= k-1; i++)
            {
                l1.edge[i] = 4 / ((3 + c_k) * k) * (float)Math.sin(i * thetaK);
            }

            for(i = 0; i <= k-1; i++)
            {
                l1.face[i] =
                    (float)(Math.sin(i * thetaK) + Math.sin((i + 1) * thetaK)) /
                             ((3 + c_k) * k);
            }
        }

        // l2
        if(k == 1)
        {
            l2.edge[0] = -1;
            l2.edge[1] = 1;
        }
        else
        {
            l2.edge[0] = 0.5f;
            l2.edge[k] = -0.5f;
        }

        // x1
        if(k == 1)
        {
            x1.center = 1f / 18;
            x1.edge[0] =  -2f / 18;
            x1.edge[1] =  -2f / 18;
            x1.face[0] =  -5f / 18;
        }
        else
        {
            for(i = 0; i <= k; i++)
                x1.edge[i] = (float)Math.sin(thetaK * i);

            // reuse edge[i] to save calcs. First term is the same
            for(i = 0; i < k; i++)
              x1.face[i] = x1.edge[i] + (float)Math.sin(thetaK * (i+1));
        }

        // x2
        if(k == 1)
        {
            x2.edge[0] = -0.5f;
            x2.edge[1] = 0.5f;
        }
        else
        {
            for(i = 0; i <= k; i++)
                x2.edge[i] = (float)Math.cos(thetaK * i);

            // reuse edge[i] to save calcs. First term is the same
            for(i = 0; i < k; i++)
                x2.face[i] = x2.edge[i] + (float)Math.cos(thetaK * (i + 1));
        }
    }
}
