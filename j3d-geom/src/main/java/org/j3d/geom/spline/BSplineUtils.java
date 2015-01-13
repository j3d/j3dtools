/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.spline;

// External imports
// none

// Local imports
import org.j3d.geom.InvalidArraySizeException;
import org.j3d.geom.GeometryData;
import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.UnsupportedTypeException;


/**
 * Utility functionality useful for working with BSpline curves and surfaces.
 * <P>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class BSplineUtils
{
    /**
     * Convenience method to return the current multiplicity of the given knot
     * value in the knot array. If the knot does not exist in the array, the
     * multiplicity is given as zero.
     *
     * @param knots The array of current knot values
     * @param k The target knot value
     * @return The multiplicity of the given knot
     */
    public static int getKnotMultiplicity(float[] knots, float k)
    {
        return getKnotMultiplicity(knots, knots.length, k);
    }

    /**
     * Convenience method to return the current multiplicity of the given knot
     * value in the knot array. If the knot does not exist in the array, the
     * multiplicity is given as zero.
     *
     * @param knots The array of current knot values
     * @param numKnots The number of valid knot values in the array
     * @param k The target knot value
     * @return The multiplicity of the given knot
     */
    public static int getKnotMultiplicity(float[] knots, int numKnots, float k)
    {
        int s = 0;

        for(int i = 1; i < numKnots; i++)
        {
            if(k == knots[i])
                s++;
            else if(k < knots[i])
                break;
        }

        return s;
    }

    /**
     * Given the curve data, evaluate the exact point point on the curve in 3D
     * space for the given value of t.
     *
     * @param inputCurve The definition of the curve to interpolate along
     * @param t The position on the curve to calculate for
     * @param point An array to return the positional data in
     */
    public static void interpolatePoint(BSplineCurveData inputCurve,
                                        float t,
                                        float[] point)
    {
        double x = 0;
        double y = 0;
        double z = 0;

        float[] ctrl_pts = inputCurve.controlPoints;

        if(inputCurve.numWeights > 0) {
            double denom = 0;
            double w = 0;

            for(int k = 0; k < inputCurve.numControlPoints; k++) {
                double b = splineBlend(k,
                                       inputCurve.degree,
                                       inputCurve.knots,
                                       t);
                w = inputCurve.weights[k];

                x += ctrl_pts[k * 3] * b * w;
                y += ctrl_pts[k * 3 + 1] * b * w;
                z += ctrl_pts[k * 3 + 2] * b * w;

                denom += b * w;
            }

            if(denom != 0) {
                point[0] = (float)(x / w);
                point[1] = (float)(y / w);
                point[2] = (float)(z / w);
            } else {
                point[0] = (float)x;
                point[1] = (float)y;
                point[2] = (float)z;
            }
        } else {
            for(int k = 0; k < inputCurve.numControlPoints; k++) {
                double b = splineBlend(k,
                                       inputCurve.degree,
                                       inputCurve.knots,
                                       t);

                x += ctrl_pts[k * 3] * b;
                y += ctrl_pts[k * 3 + 1] * b;
                z += ctrl_pts[k * 3 + 2] * b;
            }

            point[0] = (float)x;
            point[1] = (float)y;
            point[2] = (float)z;
        }
    }

    /**
     * Calculate the blending value, this is done recursively.
     *  If the numerator and denominator are 0 the expression is 0.
     *  If the deonimator is 0 the expression is 0.
     *
     *
     */
    private static double splineBlend(int k, int t, float[] u, double v)
    {
        double ret_val;

        if(t == 1)
        {
            ret_val = ((u[k] <= v) && (v < u[k+1])) ? 1 : 0;
        }
        else
        {
            if((u[k+t-1] == u[k]) && (u[k+t] == u[k+1]))
                ret_val = 0;
            else if (u[k+t-1] == u[k])
                ret_val = (u[k+t] - v) / (u[k+t] - u[k+1]) *
                          splineBlend(k+1, t-1, u, v);
            else if (u[k+t] == u[k+1])
                ret_val = (v - u[k]) / (u[k+t-1] - u[k]) *
                          splineBlend(k, t-1, u, v);
            else
                ret_val = (v - u[k]) / (u[k+t-1] - u[k]) *
                          splineBlend(k, t-1, u, v) +
                          (u[k+t] - v) / (u[k+t] - u[k+1]) *
                          splineBlend(k+1, t-1, u, v);
        }

        return ret_val;
    }

    /**
     * Insert a new knot value into the curve. The code will attempt to reuse
     * the arrays already constructed in the output curve, but will reallocated
     * to a large-enough size if needed. If the knot value is outside the range
     * of the current knots, an exception will be generated.
     *
     * @param inputCurve The definition of the curve to add the knot to
     * @param knot The value of the knot to insert
     * @param outputCurve Reference to the definition to put the output in
     * @throws IllegalArgumentException The knot value is out of range
     */
    public static void insertKnot(BSplineCurveData inputCurve,
                                  float knot,
                                  BSplineCurveData outputCurve)
        throws IllegalArgumentException
    {
        if(knot < inputCurve.knots[0])
            throw new IllegalArgumentException("Inserted knot < knot[0]");

        if(knot > inputCurve.knots[inputCurve.numKnots - 1])
            throw new IllegalArgumentException("Inserted knot > last knot");

        // Discover the span that the knot belongs to. The span definition is
        // the range [uk, uk + 1)
        int k = -1;
        for(int i = 1; (i < inputCurve.numKnots) && (k == -1); i++)
        {
            if(knot < inputCurve.knots[i])
                k = i - 1;
        }

        int p = inputCurve.degree;
        int start_point = (k - p + 1) * 3;
        int end_point = k * 3;

        float[] controls = inputCurve.controlPoints;
        float[] knots = inputCurve.knots;

        int num_items = (inputCurve.numControlPoints + 1) * 3;
        if((outputCurve.controlPoints == null) ||
           (outputCurve.controlPoints.length < num_items))
        {
            outputCurve.controlPoints = new float[num_items];
        }


        num_items = inputCurve.numKnots + 1;
        if((outputCurve.knots == null) ||
           (outputCurve.knots.length < num_items))
        {
            outputCurve.knots = new float[num_items];
        }

        outputCurve.numControlPoints = inputCurve.numControlPoints + 1;
        outputCurve.numKnots = num_items;

        // First copy across all the values to and leave a gap.
        if(start_point != 0)
        {
            System.arraycopy(controls,
                             0,
                             outputCurve.controlPoints,
                             0,
                             start_point);

            System.arraycopy(knots, 0, outputCurve.knots, 0, k + 1);
        }

        System.arraycopy(controls,
                         end_point,
                         outputCurve.controlPoints,
                         end_point + 3,
                         (k - p + 1) * 3);

        System.arraycopy(knots,
                         k,
                         outputCurve.knots,
                         k + 1,
                         outputCurve.numKnots - k - 1);

        outputCurve.knots[k + 1] = knot;

        // If the input and output arrays are the same, apply a +1 offset to
        // the normal lookups of the source.
        float ai;
        int q_point = start_point;
        int i = k - p + 1 + ((inputCurve == outputCurve) ? 1 : 0);

        for(int n = k - p + 1; n <= k; n++)
        {

            // Ai = (t - Ui) / (Ui+p - Ui)
            ai = (knot - knots[i]) / (knots[i + p] - knots[i]);

            // Qi = (1 - Ai)Pi-1 + AiPi
            outputCurve.controlPoints[q_point++] =
                (1 - ai) * controls[(i - 1) * 3] + ai * controls[i * 3];

            outputCurve.controlPoints[q_point++] =
                (1 - ai) * controls[(i - 1) * 3 + 1] + ai * controls[i * 3 + 1];

            outputCurve.controlPoints[q_point++] =
                (1 - ai) * controls[(i - 1) * 3 + 2] + ai * controls[i * 3 + 2];

            i++;
        }

        // Set the degree at the end, just in case the user has provided the
        // same reference for input and outputs.
        outputCurve.degree = inputCurve.degree + 1;
    }

    /**
     * Insert a new knot value multiple times into the curve. The code will
     * attempt to reuse the arrays already constructed in the output curve,
     * but will reallocated to a large-enough size if needed. If the knot
     * value is outside the range of the current knots, an exception will
     * be generated.
     *
     * @param inputCurve The definition of the curve to add the knot to
     * @param knot The value of the knot to insert
     * @param times The number of times to insert the knot
     * @param outputCurve Reference to the definition to put the output in
     * @throws IllegalArgumentException The knot value is out of range or
     *    the multiplicity is <= 0
     */
    public static void insertKnot(BSplineCurveData inputCurve,
                                  float knot,
                                  int times,
                                  BSplineCurveData outputCurve)
        throws IllegalArgumentException
    {
        insertKnot(inputCurve, knot, times, outputCurve, null);
    }

    /**
     * Subdivide the input curve into two curves, represented by the two output
     * curve functions. If the fraction is not (0,1) complain
     */
    public static void subdivide(BSplineCurveData inputCurve,
                                 float fraction,
                                 BSplineCurveData outputCurve1,
                                 BSplineCurveData outputCurve2)
        throws IllegalArgumentException
    {
        if(fraction <= 0)
            throw new IllegalArgumentException("subdividing with fraction <= 0");

        if(fraction < inputCurve.knots[0])
            throw new IllegalArgumentException("Subdivision with fraction < knot[0]");

        if(fraction >= inputCurve.knots[inputCurve.numKnots - 1])
            throw new IllegalArgumentException("Subdivision with fraction >= last knot");

        // De Boor's algorithm requires inserting the knot p times at the
        // required place. First, find out the current multiplicity and then
        // ask for p - s insertions.
        int s = getKnotMultiplicity(inputCurve.knots,
                                    inputCurve.numKnots,
                                    fraction);

        // p - s to work out final number of inserts. There might be an issue
        // here if p == s.
        int[] offset = new int[2];
        float[][] Pir = insertKnot(inputCurve,
                                   fraction,
                                   inputCurve.degree - s,
                                   outputCurve1,
                                   offset);

        // The knots for the first curve are [0, Uk) + P+1 copies of u.
        int k = offset[0];
        int p = inputCurve.degree;
        int n = inputCurve.numControlPoints;

        outputCurve1.degree = inputCurve.degree;
        outputCurve2.degree = inputCurve.degree;

        if((outputCurve1.knots == null) || (outputCurve1.knots.length < k + p + 1))
            outputCurve1.knots = new float[k + 1];

        if((outputCurve2.knots == null) || (outputCurve2.knots.length < k + 1))
            outputCurve2.knots = new float[k + 1];

        int i;
        int idx = 0;

        for(i = 0; i < k; i++)
            outputCurve1.knots[idx++] = inputCurve.knots[i];

        for(i = 0; i < p + 1; i++)
            outputCurve1.knots[idx++] = fraction;

        outputCurve1.numKnots = k + p + 1;

        idx = 0;
        for(i = 0; i < p + 1; i++)
            outputCurve2.knots[idx++] = fraction;

        for(i = k; i < inputCurve.numKnots; i++)
            outputCurve2.knots[idx++] = inputCurve.knots[i];

        outputCurve2.numKnots = idx;

        // Now do the control coordinates
        // first half has k-p + p-s values
        // second half has n-k-s + p-s values
        if((outputCurve1.controlPoints == null) ||
           (outputCurve1.controlPoints.length < (k - s) * 3))
           outputCurve1.controlPoints = new float[(k - s) * 3];

        if((outputCurve2.controlPoints == null) ||
           (outputCurve2.controlPoints.length < (n + p - k - 2 * s) * 3))
           outputCurve2.controlPoints = new float[(n + p - k - 2 * s) * 3];

        idx = 0;
        int src = 0;
        int o = offset[1];

        for(i = 0; i < k - p; i++)
        {
            outputCurve1.controlPoints[idx++] = inputCurve.controlPoints[src++];
            outputCurve1.controlPoints[idx++] = inputCurve.controlPoints[src++];
            outputCurve1.controlPoints[idx++] = inputCurve.controlPoints[src++];
        }

        for(i = 0; i < p - s; i++)
        {
            outputCurve1.controlPoints[idx++] = Pir[k - p + i + o][i * 3];
            outputCurve1.controlPoints[idx++] = Pir[k - p + i + o][i * 3 + 1];
            outputCurve1.controlPoints[idx++] = Pir[k - p + i + o][i * 3 + 2];
        }

        outputCurve1.numControlPoints = k - s;

        idx = 0;
        for(i = p - s; i > 0; i--)
        {
            outputCurve2.controlPoints[idx++] = Pir[k - s + o][i * 3];
            outputCurve2.controlPoints[idx++] = Pir[k - s + o][i * 3 + 1];
            outputCurve2.controlPoints[idx++] = Pir[k - s + o][i * 3 + 2];
        }

        src = k - s * 3;
        for(i = k - s; i < inputCurve.numControlPoints; i++)
        {
            outputCurve2.controlPoints[idx++] = inputCurve.controlPoints[src++];
            outputCurve2.controlPoints[idx++] = inputCurve.controlPoints[src++];
            outputCurve2.controlPoints[idx++] = inputCurve.controlPoints[src++];
        }

        outputCurve2.numControlPoints = n + p - k - 2 * s;
    }

    /**
     * Real implementaiton of the insertKnot functionality. Gives access to
     * the generated 2D array of Pi,r values and the offsets needed to work
     * with it, for doing curve subdivision and B-spline to Bezier
     * transformations.
     *
     * @param inputCurve The definition of the curve to add the knot to
     * @param knot The value of the knot to insert
     * @param times The number of times to insert the knot
     * @param outputCurve Reference to the definition to put the output in
     * @param offset Array of length 2 that will be set to the offset for the
     *    values in the returned 2D array and k
     * @return The collection of generated Pi,r values with p[i][r*3]
     * @throws IllegalArgumentException The knot value is out of range or
     *    the multiplicity is <= 0
     */
    private static float[][] insertKnot(BSplineCurveData inputCurve,
                                       float knot,
                                       int times,
                                       BSplineCurveData outputCurve,
                                       int[] offset)
        throws IllegalArgumentException
    {
        if(knot < inputCurve.knots[0])
            throw new IllegalArgumentException("Inserted knot < knot[0]");

        if(knot > inputCurve.knots[inputCurve.numKnots - 1])
            throw new IllegalArgumentException("Inserted knot > last knot");

        if(times <= 0)
            throw new IllegalArgumentException("Multiplicity <= 0");


        // Discover the span that the knot belongs to. The span definition is
        // the range [uk, uk + 1)
        int k = -1;
        int s = 0;
        for(int i = 1; (i < inputCurve.numKnots) && (k == -1); i++)
        {
            if(knot == inputCurve.knots[i])
                s++;
            else if(knot < inputCurve.knots[i])
                k = i - 1;
        }

        // Adjust the initial start k for the first of the knot values so that
        // it is always maintaining [Uk, Uk+1)
        if(s != 0)
            k = k - s + 1;

        if(offset != null)
            offset[0] = k;

        int p = inputCurve.degree;
        int h = times;
        int n = inputCurve.numControlPoints - 1;
        int m = inputCurve.numKnots - 1;

        // Check for h + s <= p requirement
        if(h + s > p)
            throw new IllegalArgumentException("Cannot add more times than degree");

        // If the input and output arrays are the same, apply a +1 offset to
        // the normal lookups of the source.
        float ai;
        float[] controls = inputCurve.controlPoints;
        float[] knots = inputCurve.knots;

        // temp array for putting in the Pi,r points.
        float[][] Pir = new float[p][(h + 1) * 3];
        int i_pos = Pir.length - 1;

/*
System.out.println("n = " + n);
System.out.println("m = " + m);
System.out.println("p = " + p);

System.out.println("k = " + k);
System.out.println("h = " + h);
System.out.println("s = " + s);
*/
        // Let control points Pk, Pk-1, ..., Pk-p be renamed as Pk,0, Pk-1,0,
        // ..., Pk-p,0 by adding a second subscript 0.
        for(int cnt = k; cnt > k - p; cnt--)
        {
            Pir[i_pos][0] = controls[cnt * 3];
            Pir[i_pos][1] = controls[(cnt * 3) + 1];
            Pir[i_pos][2] = controls[(cnt * 3) + 2];
            i_pos--;
        }

        // An offset into the array to correct Pi,r to the Pir array
        int o = k - p;

        for(int r = 1; r <= h; r++)
        {
            for(int i = k - p + r; i < k - s; i++)
            {
                // Ai,r = (t - Ui) / (Ui+p-r+1 - Ui)
                ai = (knot - knots[i]) / (knots[i + p - r + 1] - knots[i]);

                // Pi,r = (1 - Ai,r)Pi-1,r-1 + Ai,rPi,r-1
                Pir[i - o][r * 3] =
                    (1 - ai) * Pir[i - o - 1][(r - 1) * 3] + ai * Pir[i - o][(r - 1) * 3];

                Pir[i - o][(r * 3) + 1] =
                    (1 - ai) * Pir[i - o - 1][(r - 1) * 3 + 1] + ai * Pir[i - o][(r - 1) * 3 + 1];

                Pir[i - o][(r * 3) + 2] =
                    (1 - ai) * Pir[i - o - 1][(r - 1) * 3 + 2] + ai * Pir[i - o][(r - 1) * 3 + 2];
            }
        }

        // Now copy over the arrays to build the points up correctly
        // knots first
        int num_items = inputCurve.numKnots + times;

        if((outputCurve.knots == null) ||
           (outputCurve.knots.length < num_items))
        {
            outputCurve.knots = new float[num_items];
        }

        outputCurve.numKnots = num_items;
        System.arraycopy(knots, 0, outputCurve.knots, 0, k + 1);

        System.arraycopy(knots,
                         k,
                         outputCurve.knots,
                         k + h,
                         outputCurve.numKnots - k - h);

        for(int i = k + 1; i < k + h; i++)
            outputCurve.knots[i] = knot;

        // The new set of control points are constructed from the original ones
        // from P0 to Pk-p, followed by the top edge of the diagram above
        // (i.e., Pk-p+1,1, Pk-p+2,2, ..., Pk-p+h,h), followed by the right
        // edge of the diagram (i.e., Pk-p+h+1,h, Pk-p+h+2,h, ...., Pk-s,h),
        // followed by the bottom edge of the diagram (i.e., Pk-s,h-1, Pk-s,h-2,
        // ...., Pk-s,1), followed by the original control points
        // Pk-s, ..., Pk, ..., Pn.
        int total_control_points = h + n;

        num_items = total_control_points * 3;
        if((outputCurve.controlPoints == null) ||
           (outputCurve.controlPoints.length < num_items))
        {
            outputCurve.controlPoints = new float[num_items];
        }

        outputCurve.numControlPoints = total_control_points;

        // Copy P0 to Pk-p.
        if(k - p + 1 > 0)
        {
            System.arraycopy(controls,
                             0,
                             outputCurve.controlPoints,
                             0,
                             (k - p + 1) * 3);
        }

        // copy Pk-p+1,1 -> Pk-p+h,h
        int last_index = outputCurve.numControlPoints * 3;
        int idx = k - p + 1 * 3;
        int d = 1;

        for(int i = k - p + 1; (i <= k - p + h) && (idx < last_index); i++)
        {
            outputCurve.controlPoints[idx++] = Pir[i - o][d * 3];
            outputCurve.controlPoints[idx++] = Pir[i - o][d * 3 + 1];
            outputCurve.controlPoints[idx++] = Pir[i - o][d * 3 + 2];
            d++;
        }

        // Copy Pk-p+h+1,h -> Pk-s,h
        for(int i = k - p + h + 1; (i <= k - s) && (idx < last_index); i++)
        {
            outputCurve.controlPoints[idx++] = Pir[i - o][h * 3];
            outputCurve.controlPoints[idx++] = Pir[i - o][h * 3 + 1];
            outputCurve.controlPoints[idx++] = Pir[i - o][h * 3 + 2];
        }

        // Copy Pk-s,h-1 -> Pk-s,1
        for(int i = h - 1; (i > 0) && (idx < last_index); i--)
        {
            outputCurve.controlPoints[idx++] = Pir[k - s - o][i * 3];
            outputCurve.controlPoints[idx++] = Pir[k - s - o][i * 3 + 1];
            outputCurve.controlPoints[idx++] = Pir[k - s - o][i * 3 + 2];
        }

        if(idx < last_index)
        {
            int cnt = last_index - 3 - (k - s) * 3 ;

            System.arraycopy(controls,
                             (k - s) * 3,
                             outputCurve.controlPoints,
                             idx + 1,
                             cnt);
        }

        if(offset != null)
            offset[1] = o;

        return Pir;
    }
}
