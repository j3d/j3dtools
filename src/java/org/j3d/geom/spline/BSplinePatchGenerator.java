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
 * Geometry generator for generating rectangular BSpline patches.
 * <P>
 *
 * BSpline patches of all orders are permitted. Order information is derived
 * from the provided knot coordinates. When generating a patch, the values
 * for the coordinates are nominally provided in the X and Z plane although no
 * explicit checking is performed to ensure that knot coordinates do not
 * self-intersect or do anything nasty. Normals are always generated as the
 * average between the adjacent edges.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class BSplinePatchGenerator extends PatchGenerator
{
    /** Default number of segments used in the patch */
    private static final int DEFAULT_FACETS = 16;

    /** Default degree of the curve */
    private static final int DEFAULT_DEGREE = 3;

    /** Knots on the width curve to control weighting. */
    private int[] widthKnots;

    /** The number of knot coordinates in the width. */
    private int numWidthKnots;

    /** The degree of the width curve to generate. Must be positive. */
    private int widthDegree;

    /** Knots on the width curve to control weighting. */
    private int[] depthKnots;

    /** The number of knot coordinates in the depth. */
    private int numDepthKnots;

    /** The degree of the depth curve to generate. Must be positive. */
    private int depthDegree;

    /**
     * Construct a new generator with default settings of 20 grid squares over
     * the length of one surface.
     */
    public BSplinePatchGenerator()
    {
        this(DEFAULT_FACETS, DEFAULT_DEGREE, DEFAULT_DEGREE);
    }

    /**
     * Construct a new generator with the specified number of tessellations
     * over the side of the patch, regardless of extents and default degree
     * of 3 for both width and depth.
     *
     * @param facets The number of facets on the side of the cone
     * @throws IllegalArgumentException The number of facets is less than 3
     */
    public BSplinePatchGenerator(int facets)
    {
        this(facets, DEFAULT_DEGREE, DEFAULT_DEGREE);
    }

    /**
     * Construct a new generator with the specified number of tessellations
     * over the side of the patch, regardless of extents and the same degree
     * for both width and depth.
     *
     * @param facets The number of facets on the side of the cone
     * @param t The degree of the curve > 1
     * @throws IllegalArgumentException The number of facets is less than 3
     */
    public BSplinePatchGenerator(int facets, int t)
    {
        this(facets, t, t);
    }

    /**
     * Construct a new generator with the specified number of tessellations
     * over the side of the patch, regardless of extents and specific degree
     * for both width and depth.
     *
     * @param facets The number of facets on the side of the cone
     * @param tWidth The degree of the curve in the width direction > 1
     * @param tDepth The degree of the curve in the depth direction > 1
     * @throws IllegalArgumentException The number of facets is less than 3
     */
    public BSplinePatchGenerator(int facets, int tWidth, int tDepth)
    {
        if(facets < 3)
            throw new IllegalArgumentException("Number of facets is < 3");

        if(tWidth < 2)
            throw new IllegalArgumentException("Width degree is < 2");

        if(tDepth < 2)
            throw new IllegalArgumentException("Depth degree is < 2");

        patchChanged = true;
        facetCount = facets;

        widthDegree = tWidth;
        depthDegree = tDepth;

        numWidthKnots = widthDegree;
        numDepthKnots = depthDegree;

        widthKnots = new int[numWidthKnots];
        depthKnots = new int[numDepthKnots];
    }

    /**
     * Set the bezier patch knots. The array is presented as [depth][width]
     * with the coordinates flattened as [Xn, Yn, Zn] in the width array. The
     * order of the patch is determined by the passed array. If the arrays are
     * not of minimum length 3 and equal length an exception is generated.
     *
     * @param tWidth The degree in the width direction
     * @param wKnots The knot coordinate values in the width direction
     * @param tDepth The degree in the depth direction
     * @param dKnots The knot coordinate values in the depth direction
     */
    public void setPatchKnots(int tWidth,
                              float[] wKnots,
                              int tDepth,
                              int[] dKnots)
    {
        if(tWidth < 2)
            throw new IllegalArgumentException("Width degree is < 2");

        if(wKnots.length < (numWidthControlPoints + tWidth + 1))
            throw new IllegalArgumentException("Width Knots < 3");

        if(tDepth < 2)
            throw new IllegalArgumentException("Depth degree is < 2");

        if(dKnots.length < (numDepthControlPoints + tDepth + 1))
            throw new IllegalArgumentException("Depth Knots < 3");

        widthDegree = tWidth;
        depthDegree = tDepth;

        if(wKnots.length > widthKnots.length)
            widthKnots = new int[wKnots.length];

        if(dKnots.length > depthKnots.length)
            depthKnots = new int[dKnots.length];

        System.arraycopy(wKnots, 0, widthKnots, 0, wKnots.length);
        System.arraycopy(dKnots, 0, depthKnots, 0, dKnots.length);

        numWidthKnots = wKnots.length;
        numDepthKnots = dKnots.length;

        patchChanged = true;
    }

    /**
     * Get the degree of the curve being generated.
     *
     * @return A value >= 2
     */
    public int getWidthDegree()
    {
        return widthDegree;
    }

    /**
     * Get the degree of the curve being generated.
     *
     * @return A value >= 2
     */
    public int getDepthDegree()
    {
        return depthDegree;
    }

    /**
     * Convenience method to set knots that give a better looking curve shape and
     * set a new degree for the curve directly.
     * The traditional way of setting these is to have knot[i] = i, but
     * whenever curves change this results in a lot of extra calculations. This
     * smoothing function will localize the changes at any particular breakpoint
     * in the line.
     *
     * @param tWidth The degree of the curve in the width direction
     * @param tDepth The degree of the curve in the depth direction
     */
    public void generateSmoothKnots(int tWidth, int tDepth)
    {
        if(tWidth < 2)
            throw new IllegalArgumentException("Width degree is < 2");

        if(tDepth < 2)
            throw new IllegalArgumentException("Depth degree is < 2");

        if(tWidth != widthDegree)
        {
            widthDegree = tWidth;
            patchChanged = true;
        }

        if(tDepth != depthDegree)
        {
            depthDegree = tDepth;
            patchChanged = true;
        }

        generateSmoothKnots();
    }

    /**
     * Convenience method to set knots that give a better looking curve shape
     * using the existing curve degree. The traditional way of setting these is
     * to have knot[i] = i, but whenever curves change this results in a lot of
     * extra calculations. This smoothing function will localize the changes at
     * any particular breakpoint in the line.
     *
     * @param t The degree of the curve
     */
    public void generateSmoothKnots()
    {
        // resize if necessary
        numWidthKnots = numWidthControlPoints + widthDegree;
        if(widthKnots.length < numWidthKnots)
            widthKnots = new int[numWidthKnots];

        int j;

        for(j = 0; j < numWidthKnots; j++)
        {
            if(j < widthDegree)
                widthKnots[j] = 0;
            else if(j < numWidthControlPoints)
                widthKnots[j] = j - widthDegree + 1;
            else if(j >= numWidthControlPoints)
                widthKnots[j] = numWidthControlPoints - widthDegree + 1;
        }

        numDepthKnots = numDepthControlPoints + depthDegree;
        if(depthKnots.length < numDepthKnots)
            depthKnots = new int[numDepthKnots];

        for(j = 0; j < numDepthKnots; j++)
        {
            if(j < depthDegree)
                depthKnots[j] = 0;
            else if(j < numDepthControlPoints)
                depthKnots[j] = j - depthDegree + 1;
            else if(j >= numDepthControlPoints)
                depthKnots[j] = numDepthControlPoints - depthDegree + 1;
        }
    }

    /**
     * Regenerate the patch coordinate points according to the NURBS
     * surface function.
     */
    protected final void regeneratePatch()
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
        int i, j, ki, kj;
        double i_inter, i_inc;
        double j_inter, j_inc;
        double bi,bj;
        double x, y, z;
        int cnt, p_cnt;
        int last = facetCount * 3;
        int last_depth = (numDepthControlPoints - 1) * 3;
        int last_width = numWidthControlPoints - 1;

       // Step size along the curve
       i_inc = (numWidthControlPoints - widthDegree + 1) / (double)facetCount;
       j_inc = (numDepthControlPoints - depthDegree + 1) / (double)facetCount;

        i_inter = 0;
        for(i = 0; i < facetCount; i++)
        {
            j_inter = 0;
            p_cnt = 0;
            for(j = 0; j < facetCount; j++)
            {
                x = 0;
                y = 0;
                z = 0;
                cnt = 0;
                kj = 0;

                for(ki = 0; ki < numWidthControlPoints; ki++)
                {
                    cnt = 0;
                    for(kj = 0; kj < numDepthControlPoints; kj++)
                    {
                        bi = splineBlend(ki, widthDegree, false, i_inter);
                        bj = splineBlend(kj, depthDegree, true, j_inter);
                        x += controlPointCoordinates[ki][cnt++] * bi * bj;
                        y += controlPointCoordinates[ki][cnt++] * bi * bj;
                        z += controlPointCoordinates[ki][cnt++] * bi * bj;
                    }
                }

                patchCoordinates[i][p_cnt++] = (float)x;
                patchCoordinates[i][p_cnt++] = (float)y;
                patchCoordinates[i][p_cnt++] = (float)z;

                j_inter += j_inc;
            }

            i_inter += i_inc;
        }


        // Process the last row along the depth.
        i_inter = 0;

        for(i = 0; i < facetCount; i++)
        {
            x = 0;
            y = 0;
            z = 0;

            for(ki = 0; ki < numWidthControlPoints; ki++)
            {
                bi = splineBlend(ki, widthDegree, false, i_inter);
                x += controlPointCoordinates[ki][last_depth] * bi;
                y += controlPointCoordinates[ki][last_depth + 1] * bi;
                z += controlPointCoordinates[ki][last_depth + 2] * bi;
            }

            patchCoordinates[i][last] = (float)x;
            patchCoordinates[i][last + 1] = (float)y;
            patchCoordinates[i][last + 2] = (float)z;
            i_inter += i_inc;
        }

        patchCoordinates[facetCount][last] =
            controlPointCoordinates[last_width][last_depth];
        patchCoordinates[facetCount][last + 1] =
            controlPointCoordinates[last_width][last_depth + 1];
        patchCoordinates[facetCount][last + 2] =
            controlPointCoordinates[last_width][last_depth + 2];

        // Process the last row along the width.
        j_inter = 0;
        for(j = 0; j < facetCount; j++)
        {
            x = 0;
            y = 0;
            z = 0;
            cnt = 0;
            for(kj = 0; kj < numDepthControlPoints; kj++)
            {
                bj = splineBlend(kj, depthDegree, true, j_inter);
                x += controlPointCoordinates[last_width][cnt++] * bj;
                y += controlPointCoordinates[last_width][cnt++] * bj;
                z += controlPointCoordinates[last_width][cnt++] * bj;
            }

            patchCoordinates[facetCount][j * 3] = (float)x;
            patchCoordinates[facetCount][j * 3 + 1] = (float)y;
            patchCoordinates[facetCount][j * 3 + 2] = (float)z;
            j_inter += j_inc;
        }

        patchCoordinates[facetCount][last] =
            controlPointCoordinates[last_width][last_depth];
        patchCoordinates[facetCount][last + 1] =
            controlPointCoordinates[last_width][last_depth + 1];
        patchCoordinates[facetCount][last + 2] =
            controlPointCoordinates[last_width][last_depth + 2];
    }

    /**
     * Regenerate the patch using control point weights.
     */
    private void regenerateWeightedPatch()
    {
        int i, j, ki, kj;
        double i_inter, i_inc;
        double j_inter, j_inc;
        double bi,bj;
        double x, y, z;
        float w;
        int cnt, p_cnt;
        int last = facetCount * 3;
        int last_depth = (numDepthControlPoints - 1) * 3;
        int last_width = numWidthControlPoints - 1;

       // Step size along the curve
       i_inc = (numWidthControlPoints - widthDegree + 1) / (double)facetCount;
       j_inc = (numDepthControlPoints - depthDegree + 1) / (double)facetCount;

        i_inter = 0;
        for(i = 0; i < facetCount; i++)
        {
            j_inter = 0;
            p_cnt = 0;
            for(j = 0; j < facetCount; j++)
            {
                x = 0;
                y = 0;
                z = 0;
                cnt = 0;
                kj = 0;

                for(ki = 0; ki < numWidthControlPoints; ki++)
                {
                    cnt = 0;
                    for(kj = 0; kj < numDepthControlPoints; kj++)
                    {
                        bi = splineBlend(ki, widthDegree, false, i_inter);
                        bj = splineBlend(kj, depthDegree, true, j_inter);
                        w = controlPointWeights[ki][kj];
                        x += controlPointCoordinates[ki][cnt++] * bi * bj * w;
                        y += controlPointCoordinates[ki][cnt++] * bi * bj * w;
                        z += controlPointCoordinates[ki][cnt++] * bi * bj * w;
                    }
                }

                patchCoordinates[i][p_cnt++] = (float)x;
                patchCoordinates[i][p_cnt++] = (float)y;
                patchCoordinates[i][p_cnt++] = (float)z;

                j_inter += j_inc;
            }

            i_inter += i_inc;
        }


        // Process the last row along the depth.
        i_inter = 0;

        for(i = 0; i < facetCount; i++)
        {
            x = 0;
            y = 0;
            z = 0;

            for(ki = 0; ki < numWidthControlPoints; ki++)
            {
                bi = splineBlend(ki, widthDegree, false, i_inter);
                w = controlPointWeights[ki][numDepthControlPoints];
                x += controlPointCoordinates[ki][last_depth] * bi * w;
                y += controlPointCoordinates[ki][last_depth + 1] * bi * w;
                z += controlPointCoordinates[ki][last_depth + 2] * bi * w;
            }

            patchCoordinates[i][last] = (float)x;
            patchCoordinates[i][last + 1] = (float)y;
            patchCoordinates[i][last + 2] = (float)z;
            i_inter += i_inc;
        }

        // Process the last row along the width.
        j_inter = 0;
        for(j = 0; j < facetCount; j++)
        {
            x = 0;
            y = 0;
            z = 0;
            cnt = 0;
            for(kj = 0; kj < numDepthControlPoints; kj++)
            {
                bj = splineBlend(kj, depthDegree, true, j_inter);
                w = controlPointWeights[last_width][kj];
                x += controlPointCoordinates[last_width][cnt++] * bj * w;
                y += controlPointCoordinates[last_width][cnt++] * bj * w;
                z += controlPointCoordinates[last_width][cnt++] * bj * w;
            }

            patchCoordinates[facetCount][j * 3] = (float)x;
            patchCoordinates[facetCount][j * 3 + 1] = (float)y;
            patchCoordinates[facetCount][j * 3 + 2] = (float)z;
            j_inter += j_inc;
        }

        patchCoordinates[facetCount][last] =
            controlPointCoordinates[last_width][last_depth];
        patchCoordinates[facetCount][last + 1] =
            controlPointCoordinates[last_width][last_depth + 1];
        patchCoordinates[facetCount][last + 2] =
            controlPointCoordinates[last_width][last_depth + 2];
    }


    /**
     * Calculate the blending value for the spline recursively.
     * If the numerator and denominator are 0 the result is 0.
     */
    private double splineBlend(int k, int t, boolean useDepth, double v)
    {
        double ret_val;

        // Do this just to make the maths traceable with the std algorithm
        int[] u = useDepth ? depthKnots : widthKnots;

        if(t == 1)
        {
            ret_val = ((u[k] <= v) && (v < u[k+1])) ? 1 : 0;
        }
        else
        {
            if((u[k+t-1] == u[k]) && (u[k+t] == u[k+1]))
                ret_val = 0;
            else if(u[k+t-1] == u[k])
                ret_val = (u[k+t] - v) /
                          (u[k+t] - u[k+1]) * splineBlend(k+1, t-1, useDepth, v);
            else if(u[k+t] == u[k+1])
                ret_val = (v - u[k]) /
                          (u[k+t-1] - u[k]) * splineBlend(k, t-1, useDepth, v);
            else
                ret_val = (v - u[k]) /
                          (u[k+t-1] - u[k]) * splineBlend(k, t-1, useDepth, v) +
                          (u[k+t] - v) /
                          (u[k+t] - u[k+1]) * splineBlend(k+1, t-1, useDepth, v);
        }

        return ret_val;
    }
}
