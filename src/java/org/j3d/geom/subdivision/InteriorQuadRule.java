/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.subdivision;

// Standard imports
// None

// Application specific imports
// None

/**
 * Representation for the eigenvector and weighted coefficients for interior
 * (flat, no-edges) sectors using quads.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class InteriorQuadRule extends QuadRule
{
    /**
     * Construct a new default instance of this class
     */
    InteriorQuadRule(int k, float theta)
    {
        super(k, theta, false);

        // Edge sub coefficient
        edgeSub[0] = 0.375f;  // 3/8
        edgeSub[3] = 0.375f;

        edgeSub[1] = 0.0625f;  // 1/16
        edgeSub[2] = 0.0625f;
        edgeSub[4] = 0.0625f;
        edgeSub[5] = 0.0625f;

        // subCoefficients
        float beta = 1.5f / (float)k;
        float gamma = 0.25f / (float)k;

        sub.center = 1 - beta - gamma;
        float bk = beta / k;
        float gk = gamma / k;

        for (int i = 0; i < k; i++)
        {
            sub.edge[i] = bk;
            sub.face[i] = gk;
        }

        // lambda values. Equal in this case
        float thetaK = (float)(2 * Math.PI / k);
        float An = (float)(1 + Math.cos(thetaK) +
                  Math.cos(Math.PI / k) * Math.sqrt(2 * (9 + Math.cos(thetaK))));

        lambda1 = (An + 4) / 16f;
        lambda2 = lambda1;

        // l1
        float f1 = 1 / (4 * lambda1 - 1);

        for (int i = 0; i < k; i++)
        {
            float s_ik = (float)Math.sin(i * thetaK);

            l1.edge[i] = 4.0f * s_ik;
            l1.face[i] = f1 * (s_ik + (float)Math.sin((i + 1) * thetaK));
        }

        // l0
        l0.center = k / (float)(k + 5);

        for (int i = 0; i < k; i++)
        {
            l0.edge[i] = 4.0f / k / (float)(k + 5);
            l0.face[i] = 1.0f / k / (float)(k + 5);
        }

        // l2
        for (int i = 0; i < k; i++)
        {
            float c_ik = (float)Math.cos(i * thetaK);

            l2.edge[i] = 4.0f * c_ik;
            l2.face[i] = f1 * (c_ik + (float)Math.cos((i + 1) * thetaK));
        }

        // x1
        float normaliser = 1 / (k * (2 + (((float)Math.cos(thetaK) + 1) * f1 * f1)));
        float f1_n = f1 * normaliser;

        for (int i = 0; i < k; i++)
        {
            float s_ik = (float)Math.sin(i * thetaK);

            x1.edge[i] = s_ik * normaliser;
            x1.face[i] = f1_n * (s_ik + (float)Math.sin((i + 1) * thetaK));
        }

        // x2
        for(int i = 0; i < k; i++)
        {
            float c_ik = (float)Math.cos(i * thetaK);

            x2.edge[i] = c_ik * normaliser;
            x2.face[i] = f1_n * (c_ik + (float)Math.cos((i + 1) * thetaK));
        }

    }
}
