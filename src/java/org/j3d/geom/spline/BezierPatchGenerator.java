/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.spline;

// Standard imports
// none

// Application specific imports
import org.j3d.geom.GeometryData;
import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.InvalidArraySizeException;
import org.j3d.geom.UnsupportedTypeException;

/**
 * Geometry generator for generating rectangular Bezier patches.
 * <P>
 *
 * Bezier patches of all orders are permitted. Order information is derived
 * from the provided controlPoint coordinates. When generating a patch, the values
 * for the coordinates are nominally provided in the X and Z plane although no
 * explicit checking is performed to ensure that controlPoint coordinates do not
 * self-intersect or do anything nasty. Normals are always generated as the
 * average between the adjacent edges.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class BezierPatchGenerator extends PatchGenerator
{
    /** Default number of segments used in the patch */
    private static final int DEFAULT_FACETS = 16;

    /**
     * Construct a new generator with default settings of 20 grid squares over
     * the length of one surface.
     */
    public BezierPatchGenerator()
    {
        this(DEFAULT_FACETS);
    }

    /**
     * Construct a new generator with the specified number of tessellations
     * over the side of the patch, regardless of extents.
     *
     * @param facets The number of facets on the side of the cone
     * @throws IllegalArgumentException The number of facets is less than 3
     */
    public BezierPatchGenerator(int facets)
    {
        if(facets < 3)
            throw new IllegalArgumentException("Number of facets is < 3");

        facetCount = facets;
    }

    /**
     * Regenerate the patch coordinate points according to the bezier surface
     * function.
     */
    protected void regeneratePatch()
    {
        if(!patchChanged)
            return;

        patchChanged = false;
        numPatchValues = (facetCount + 1) * 3;

        if((patchCoordinates == null) ||
           (numPatchValues > patchCoordinates.length) ||
           (numPatchValues > patchCoordinates[0].length))
        {
            patchCoordinates = new float[facetCount + 1][numPatchValues];
        }

        if(useControlPointWeights)
            regenerateWeightedPatch();
        else
            regenerateStandardPatch();
    }

    /**
     * Regenerate the patch ignoring control point weights.
     */
    private void regenerateStandardPatch()
    {
        double mui,muj,bi,bj;
        int cnt;
        float x, y, z;

        for(int i = 0; i < facetCount; i++)
        {
            mui = i / (double)facetCount;
            cnt = 0;
            for(int j = 0; j < facetCount; j++)
            {
                muj = j / (double)facetCount;
                x = 0;
                y = 0;
                z = 0;

                for(int ki = 0; ki < numWidthControlPoints ; ki++)
                {
                    bi = bezierBlend(ki, mui, numWidthControlPoints - 1);

                    for(int kj = 0; kj < numDepthControlPoints; kj++)
                    {
                        bj = bezierBlend(kj, muj, numDepthControlPoints - 1);
                        int pos = kj * 3;
                        x += (controlPointCoordinates[ki][pos] * bi * bj);
                        y += (controlPointCoordinates[ki][pos + 1] * bi * bj);
                        z += (controlPointCoordinates[ki][pos + 2] * bi * bj);
                    }
                }

                patchCoordinates[i][cnt++] = x;
                patchCoordinates[i][cnt++] = y;
                patchCoordinates[i][cnt++] = z;
            }

            int ncp = numDepthControlPoints * 3;
            x = 0;
            y = 0;
            z = 0;

            for(int ki = 0; ki < numWidthControlPoints ; ki++)
            {
                bi = bezierBlend(ki, mui, numWidthControlPoints - 1);

                for(int kj = 0; kj < numDepthControlPoints; kj++)
                {
                    bj = bezierBlend(kj, 1, numDepthControlPoints - 1);
                    int pos = kj * 3;
                    x += (controlPointCoordinates[ki][pos] * bi * bj);
                    y += (controlPointCoordinates[ki][pos + 1] * bi * bj);
                    z += (controlPointCoordinates[ki][pos + 2] * bi * bj);
                }
            }

            patchCoordinates[i][cnt++] = x;
            patchCoordinates[i][cnt++] = y;
            patchCoordinates[i][cnt++] = z;
        }

        // Calculate the last set of coordinates just based on the width values
        // as a simple bezier curve rather than a surface. mui == 1;
        cnt = 0;
        for(int j = 0; j < facetCount; j++)
        {
            muj = j / (double)facetCount;
            x = 0;
            y = 0;
            z = 0;

            for(int ki = 0; ki < numWidthControlPoints ; ki++)
            {
                bi = bezierBlend(ki, 1, numWidthControlPoints - 1);

                for(int kj = 0; kj < numDepthControlPoints; kj++)
                {
                    bj = bezierBlend(kj, muj, numDepthControlPoints - 1);
                    int pos = kj * 3;
                    x += (controlPointCoordinates[ki][pos] * bi * bj);
                    y += (controlPointCoordinates[ki][pos + 1] * bi * bj);
                    z += (controlPointCoordinates[ki][pos + 2] * bi * bj);
                }
            }

            patchCoordinates[facetCount][cnt++] = x;
            patchCoordinates[facetCount][cnt++] = y;
            patchCoordinates[facetCount][cnt++] = z;
        }

        int ncp = numDepthControlPoints * 3;
        patchCoordinates[facetCount][cnt++] =
            controlPointCoordinates[numWidthControlPoints - 1][ncp - 3];
        patchCoordinates[facetCount][cnt++] =
            controlPointCoordinates[numWidthControlPoints - 1][ncp - 2];
        patchCoordinates[facetCount][cnt++] =
            controlPointCoordinates[numWidthControlPoints - 1][ncp - 1];
    }

    /**
     * Regenerate the patch using control point weights.
     */
    private void regenerateWeightedPatch()
    {
        double mui,muj,bi,bj;
        int cnt;
        float x, y, z;
        float w, denom;
        int pos;

        for(int i = 0; i < facetCount; i++)
        {
            mui = i / (double)facetCount;
            cnt = 0;
            for(int j = 0; j < facetCount; j++)
            {
                muj = j / (double)facetCount;
                x = 0;
                y = 0;
                z = 0;
                denom = 0;

                for(int ki = 0; ki < numWidthControlPoints ; ki++)
                {
                    bi = bezierBlend(ki, mui, numWidthControlPoints - 1);

                    for(int kj = 0; kj < numDepthControlPoints; kj++)
                    {
                        pos = kj * 3;
                        bj = bezierBlend(kj, muj, numDepthControlPoints - 1);
                        w = controlPointWeights[ki][kj];

                        x += (controlPointCoordinates[ki][pos] * bi * bj * w);
                        y += (controlPointCoordinates[ki][pos + 1] * bi * bj * w);
                        z += (controlPointCoordinates[ki][pos + 2] * bi * bj * w);

                        denom += bi * bj * w;
                    }
                }

                if(denom != 0)
                {
                    patchCoordinates[i][cnt++] = x / denom;
                    patchCoordinates[i][cnt++] = y / denom;
                    patchCoordinates[i][cnt++] = z / denom;
                }
                else
                {
                    patchCoordinates[i][cnt++] = x;
                    patchCoordinates[i][cnt++] = y;
                    patchCoordinates[i][cnt++] = z;
                }
            }

            int ncp = numDepthControlPoints * 3;
            x = 0;
            y = 0;
            z = 0;
            denom = 0;

            for(int ki = 0; ki < numWidthControlPoints ; ki++)
            {
                bi = bezierBlend(ki, mui, numWidthControlPoints - 1);

                for(int kj = 0; kj < numDepthControlPoints; kj++)
                {
                    pos = kj * 3;
                    bj = bezierBlend(kj, 1, numDepthControlPoints - 1);
                    w = controlPointWeights[ki][kj];

                    x += (controlPointCoordinates[ki][pos] * bi * bj * w);
                    y += (controlPointCoordinates[ki][pos + 1] * bi * bj * w);
                    z += (controlPointCoordinates[ki][pos + 2] * bi * bj * w);

                    denom += bi * bj * w;
                }
            }

            if(denom != 0)
            {
                patchCoordinates[i][cnt++] = x / denom;
                patchCoordinates[i][cnt++] = y / denom;
                patchCoordinates[i][cnt++] = z / denom;
            }
            else
            {
                patchCoordinates[i][cnt++] = x;
                patchCoordinates[i][cnt++] = y;
                patchCoordinates[i][cnt++] = z;
            }
        }

        // Calculate the last set of coordinates just based on the width values
        // as a simple bezier curve rather than a surface. mui == 1;
        cnt = 0;
        for(int j = 0; j < facetCount; j++)
        {
            muj = j / (double)facetCount;
            x = 0;
            y = 0;
            z = 0;
            denom = 0;

            for(int ki = 0; ki < numWidthControlPoints ; ki++)
            {
                bi = bezierBlend(ki, 1, numWidthControlPoints - 1);

                for(int kj = 0; kj < numDepthControlPoints; kj++)
                {
                    pos = kj * 3;
                    bj = bezierBlend(kj, muj, numDepthControlPoints - 1);
                    w = controlPointWeights[ki][kj];

                    x += (controlPointCoordinates[ki][pos] * bi * bj * w);
                    y += (controlPointCoordinates[ki][pos + 1] * bi * bj * w);
                    z += (controlPointCoordinates[ki][pos + 2] * bi * bj * w);

                    denom += bi * bj * w;
                }
            }

            if(denom != 0)
            {
                patchCoordinates[facetCount][cnt++] = x / denom;
                patchCoordinates[facetCount][cnt++] = y / denom;
                patchCoordinates[facetCount][cnt++] = z / denom;
            }
            else
            {
                patchCoordinates[facetCount][cnt++] = x;
                patchCoordinates[facetCount][cnt++] = y;
                patchCoordinates[facetCount][cnt++] = z;
            }
        }

        int ncp = numDepthControlPoints * 3;
        patchCoordinates[facetCount][cnt++] =
            controlPointCoordinates[numWidthControlPoints - 1][ncp - 3];
        patchCoordinates[facetCount][cnt++] =
            controlPointCoordinates[numWidthControlPoints - 1][ncp - 2];
        patchCoordinates[facetCount][cnt++] =
            controlPointCoordinates[numWidthControlPoints - 1][ncp - 1];
    }

    /**
     * Calculate the blending function of the two curves that contribute to
     * this point.
     */
    private double bezierBlend(int k, double mu, int n) {
        int nn = n;
        int kn = k;
        int nkn = n - k;
        double blend = 1;

        while(nn >= 1)
        {
            blend *= nn;
            nn--;
            if(kn > 1)
            {
                blend /= (double)kn;
                kn--;
            }

            if(nkn > 1)
            {
                blend /= (double)nkn;
                nkn--;
            }
        }

        if(k > 0)
            blend *= Math.pow(mu, (double)k);

        if(n - k > 0)
            blend *= Math.pow(1 - mu, (double)(n - k));

        return blend;
    }
}