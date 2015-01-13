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
 * Each rule comes with 3 pairs of left and right eigenvectors x0, x1, x2 and
 * l0, l1, l2; x0 is trivial (constant 1) and need not be computed explicitly.
 * <p>
 *
 * This class should be overridden by the classes that specialise in generating
 * the coefficients for particular sector types.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
abstract class QuadRule
{
    private int k;

    /** The theta angle for determining flattening */
    final float theta;

    float lambda1;
    float lambda2;

    QuadCoefficients sub;
    QuadCoefficients l0;
    QuadCoefficients l1;
    QuadCoefficients l2;
    QuadCoefficients x1;
    QuadCoefficients x2;

    float[] edgeSub;
    float[] creaseSub;

    /**
     * Construct a new default instance of this class
     *
     * @param k The number of levels of subdivision
     * @param edgePlus true if the QuadCoeff.edge length should be k + 1
     */
    QuadRule(int k, float theta, boolean edgePlus)
    {
        this.k = k;
        this.theta = theta;

        sub = new QuadCoefficients(k, edgePlus);
        l0 = new QuadCoefficients(k, edgePlus);
        l1 = new QuadCoefficients(k, edgePlus);
        l2 = new QuadCoefficients(k, edgePlus);
        x1 = new QuadCoefficients(k, edgePlus);
        x2 = new QuadCoefficients(k, edgePlus);

        // Length needs to be k + 2
        edgeSub = new float[6];
        creaseSub = new float[6];
        creaseSub[0] = 0.5f;
        creaseSub[3] = 0.5f;
    }
}
