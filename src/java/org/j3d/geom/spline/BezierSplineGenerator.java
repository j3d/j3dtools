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
 * Generalised Bezier Spline curve generator.
 * <P>
 *
 * This class generates 3D coordinates for a single Bezier spline curve
 * controlled by an arbitrary number of control points. The minimum number of
 * points is 3, but any more than that are permitted.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class BezierSplineGenerator
{
    /** Default number of segments used in the patch */
    private static final int DEFAULT_DIVISIONS = 10;

    /**
     * The fractional division along each side [0 - 1]. Stored as 1 / divs so
     * that we can multiply the result rather than divide later
     */
    private float division;

    /**
     * Construct a new generator with default settings of 10 divisions over
     * a single side between two control points.
     */
    public BezierSplineGenerator()
    {
        this(DEFAULT_DIVISIONS);
    }

    /**
     * Construct a new generator with the specified number of tessellations
     * over the side of the curve.
     *
     * @param divs The number of facets on a segment of the curve
     * @throws IllegalArgumentException The number of divisions is less than 3
     */
    public BezierSplineGenerator(int divs)
    {
        if(divs < 3)
            throw new IllegalArgumentException("Number of facets is < 3");

        divisions = 1 / divs;
    }

    /**
     * Create a new set of spline coordinates based on the given control
     * points. The array is presented with the coordinates flattened as
     * [Xn, Yn, Zn] in the width array. The order of the curve is determined
     * by the passed array. If the array is not of minimum length 3 an
     * exception is generated.
     *
     * @param points The control point coordinate values
     * @param curve The points describing the resulting curve
     */
    public setPatchKnots(float[] points, float[] curve)
    {
        if((points == null) || (points.length < 9))
            throw new IllegalArgumentException("Number of control points is < 3");

        if(curve == null)
            throw new IllegalArgumentException("Array for return value null");

        // If they give us an array that is not a power of 3 then just ignore
        // the last part.
        int curve_order = (points.length / 3) - 1;
        int i;
        int cnt = 0;
        int output_cnt =0;

        float[] segment_lengths = new float[curve_order];
        float[] segments = new float[curve_order * 3];

        for(i = 0; i < curve_order; i++)
        {
            float x  = points[cnt + 3] - points[cnt];
            segments[cnt] = x;
            cnt++;

            float y  = points[cnt + 3] - points[cnt];
            segments[cnt] = y;
            cnt++;

            float z  = points[cnt + 3] - points[cnt];
            segments[cnt] = z;
            cnt++;

            segment_lengths[i] = Math.sqrt(x * x + y * y + z * z);
        }

        // now start subdividing each side.
        // The two end points of the currently subdivided line
        float[] p1 = new float[3];
        float[] p2 = new float[3];
        float[] v = new float[3];  // The vector joining the two points
        float[] coord = new float[3]; // final coordinate
        float length;

        cnt = 0;

        // End points are always the end control points of a bezier
        curve[output_cnt++] = points[0];
        curve[output_cnt++] = points[1];
        curve[output_cnt++] = points[2];
        float[] working_points = new float[curve_order * 3];

        for(i = 0; i < curve_order; i++)
        {
            System.arraycopy(segments, 0, working_points, 0, segments.length);

            for(int j = segments.length; --j >= 0; )
            {
                float t1 = segment_lengths[i] * division;
                float t2 = segment_lengths[i + 1] * division;

                p1[0] = t1 * segments[cnt++];
                p1[1] = t1 * segments[cnt++];
                p1[2] = t1 * segments[cnt++];

                p2[0] = t2 * segments[cnt];
                p2[1] = t2 * segments[cnt + 1];
                p2[2] = t2 * segments[cnt + 2];

                v[0] = p2[0] - p1[0];
                v[1] = p2[1] - p1[1];
                v[2] = p2[2] - p1[2];

                coord[0] = p[0] + v[0] * division;
                coord[1] = p[1] + v[1] * division;
                coord[2] = p[2] + v[2] * division;
            }
        }
    }
}