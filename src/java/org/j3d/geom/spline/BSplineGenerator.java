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
import org.j3d.geom.InvalidArraySizeException;
import org.j3d.geom.GeometryData;
import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.UnsupportedTypeException;


/**
 * Geometry generator for generating a single B-Splinecurve.
 * <P>
 *
 * BSpline curves of all orders are permitted.
 * <p>
 *
 * The implementation of the algorithm is based on Paul Bourke's C code found
 * at http://astronomy.swin.edu.au/~pbourke/curves/spline/.
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class BSplineGenerator extends GeometryGenerator
{
    /** Default number of segments used in the cone */
    private static final int DEFAULT_FACETS = 16;

    /** Default degree of the curve */
    private static final int DEFAULT_DEGREE = 3;

    /** An array of factorial values. Resized as needed */
    private static int[] FACTORIALS = { 1, 1, 2, 6, 24, 120, 720 };

    /** The number of sections used around the cone */
    private int facetCount;

    /** Control points values used to generate patches in flat array */
    private float[] controlPointCoordinates;

    /**
     * The control point weightings on the patch. This will be the same
     * size as controlPointCoordinates.
     */
    protected float[] controlPointWeights;


    /** The number of control points. x3 for the number of array items */
    private int numControlPoints;

    /** Knots on the curve to control weighting. */
    private float[] knots;

    /** The number of knot coordinates. */
    private int numKnots;

    /** The degree of the curve to generate. Must be positive. */
    private int degree;

    /** Coordinates of the generated curve */
    private float[] curveCoordinates;

    /** The number of valid values in the curve array (facetCount + 1 * 3) */
    private int numCurveValues;

    /** Flag to say the curve setup has changed */
    private boolean curveChanged;

    /** Should we use control point weights. Defaults to false. */
    protected boolean useControlPointWeights;

    /**
     * Construct a new generator of degree 3 with default number of segments.
     * The default number of segments is 16, regardless of the length of the
     * line.
     */
    public BSplineGenerator()
    {
        this(DEFAULT_DEGREE, DEFAULT_FACETS);
    }

    /**
     * Construct a new generator with the given degree but fixed number of
     * segments.
     *
     * @param t The degree of the curve > 1
     * @throws IllegalArgumentException The degree < 2
     */
    public BSplineGenerator(int t)
    {
        this(t, DEFAULT_FACETS);
    }

    /**
     * Construct a new generator with the specified number of tessellations
     * over the length of the curve, regardless of extents.
     *
     * @param t The degree of the curve > 1
     * @param facets The number of facets on the side of the curve
     * @throws IllegalArgumentException The number of facets is less than 3 or
     *   degree < 2
     */
    public BSplineGenerator(int t, int facets)
    {
        if(facets < 3)
            throw new IllegalArgumentException("Number of facets is < 3");

        if(t < 2)
            throw new IllegalArgumentException("Degree is < 2");

        degree = t;
        curveChanged = true;
        facetCount = facets;
        numControlPoints = 0;
        numKnots = degree;
        numCurveValues = 0;

        // Assume a basic bspline with only 3 control points;
        controlPointCoordinates = new float[9];
        controlPointWeights = new float[3];
        knots = new float[numKnots];
        curveCoordinates = new float[(facets + 1) * 3];
        useControlPointWeights = false;
    }

    /**
     * Set the flag to say that calculations should be using the control
     * point weights. Initially this starts as false, so if the user wants
     * to create a rational surface then they should call this method with a
     * value of true.
     *
     * @param state true if the weights should be used
     */
    public void enableControlPointWeights(boolean state)
    {
        useControlPointWeights = state;
    }

    /**
     * Get the current setting of the control point weight usage flag.
     *
     * @return true if the control point weights are in use
     */
    public boolean hasControlPointWeights()
    {
        return useControlPointWeights;
    }

    /**
     * Change the number of facets used to create this cone. This will cause
     * the geometry to be regenerated next time they are asked for.
     * The minimum number of facets is 3.
     *
     * @param facets The number of facets on the side of the cone
     * @throws IllegalArgumentException The number of facets is less than 3
     */
    public void setFacetCount(int facets)
    {
        if(facets < 3)
            throw new IllegalArgumentException("Number of facets is < 3");

        if(facetCount != facets)
            curveChanged = true;

        facetCount = facets;
    }

    /**
     * Set the control point weights to use with the existing control points.
     * Will automatically turn on the use of control point weight calculations
     * (rational form).
     *
     * @param weights Weight values to go with the points
     */
    public void setWeights(float[] weights)
    {
        setWeights(weights, weights.length);
    }

    /**
     * Set the control point weights to use with the existing control points.
     * Will automatically turn on the use of control point weight calculations
     * (rational form).
     *
     * @param weights Weight values to go with the points
     */
    public void setWeights(double[] weights)
    {
        setWeights(weights, weights.length);
    }

    /**
     * Set the control point weights to use with the existing control points.
     * Will automatically turn on the use of control point weight calculations
     * (rational form).
     *
     * @param weights Weight values to go with the points
     * @param numValid The number of valid points in the array
     */
    public void setWeights(float[] weights, int numValid)
    {
        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           (controlPointWeights.length < numValid))
        {
            controlPointWeights = new float[numValid];
        }

        System.arraycopy(weights,
                         0,
                         controlPointWeights,
                         0,
                         numValid);

        curveChanged = true;
        useControlPointWeights = true;
    }

    /**
     * Set the control point weights to use with the existing control points.
     * Will automatically turn on the use of control point weight calculations
     * (rational form).
     *
     * @param weights Weight values to go with the points
     * @param numValid The number of valid points in the array
     */
    public void setWeights(double[] weights, int numValid)
    {
        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           (controlPointWeights.length < numValid))
        {
            controlPointWeights = new float[numValid];
        }

        for(int i = 0; i < numValid; i++)
            controlPointWeights[i] = (float)weights[i];

        curveChanged = true;
        useControlPointWeights = true;
    }

    /**
     * Set the curve controlPoints. The array is presented with the coordinates
     * flattened as [Xn, Yn, Zn] in the width array. The order of the patch is
     * determined by the passed array. If the arrays are not of minimum length
     * 3 an exception is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     */
    public void setControlPoints(float[] controlPoints)
    {
        setControlPoints(controlPoints, controlPoints.length / 3);
    }

    /**
     * Set the curve controlPoints. The array is presented with the coordinates
     * flattened as [Xn, Yn, Zn] in the width array. The order of the patch is
     * determined by the passed array. If the arrays are not of minimum length
     * 3 an exception is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     */
    public void setControlPoints(double[] controlPoints)
    {
        setControlPoints(controlPoints, controlPoints.length / 3);
    }

    /**
     * Set the curve controlPoints. The array is presented with the coordinates
     * flattened as [Xn, Yn, Zn] in the width array. The order of the patch is
     * determined by the passed array. If the arrays are not of minimum length
     * 3 an exception is generated.
     * <p>
     * Will automatically turn on the use of control point weight calculations
     * (rational form).
     *
     * @param controlPoints The controlPoint coordinate values
     * @param weights Weight values to go with the points
     */
    public void setControlPoints(float[] controlPoints, float[] weights)
    {
        setControlPoints(controlPoints, controlPoints.length / 3, weights);
    }

    /**
     * Set the curve controlPoints. The array is presented with the coordinates
     * flattened as [Xn, Yn, Zn] in the width array. The order of the patch is
     * determined by the passed array. If the arrays are not of minimum length
     * 3 an exception is generated.
     * <p>
     * Will automatically turn on the use of control point weight calculations
     * (rational form).
     *
     * @param controlPoints The controlPoint coordinate values
     * @param weights Weight values to go with the points
     */
    public void setControlPoints(double[] controlPoints, double[] weights)
    {
        setControlPoints(controlPoints, controlPoints.length / 3, weights);
    }

    /**
     * Set the curve controlPoints from a subset of the given array. The array
     * is presented with the coordinates flattened as [Xn, Yn, Zn] in the width
     * array. The order of the patch is determined by the and number of points.
     * If the arrays are not of minimum length 3 an exception is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     * @param numValid The number of valid points in the array
     */
    public void setControlPoints(float[] controlPoints, int numValid)
    {
        if(numValid < 1)
            throw new IllegalArgumentException("Number of valid points < 1");

        if(numValid * 3 > controlPointCoordinates.length)
            controlPointCoordinates = new float[numValid * 3];

        System.arraycopy(controlPoints,
                         0,
                         controlPointCoordinates,
                         0,
                         numValid * 3);

        numControlPoints = numValid;

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           (numValid > controlPointWeights.length))
        {
            float[] tmp = new float[controlPoints.length];

            if(controlPointWeights.length != 0) {
                int cnt = numValid < controlPointWeights.length ?
                          numValid :
                          controlPointWeights.length;

                System.arraycopy(controlPointWeights, 0, tmp, 0, cnt);
            }

            controlPointWeights = tmp;
        }

        curveChanged = true;
    }

    /**
     * Set the curve controlPoints from a subset of the given array. The array
     * is presented with the coordinates flattened as [Xn, Yn, Zn] in the width
     * array. The order of the patch is determined by the and number of points.
     * If the arrays are not of minimum length 3 an exception is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     * @param numValid The number of valid points in the array
     */
    public void setControlPoints(double[] controlPoints, int numValid)
    {
        if(numValid < 1)
            throw new IllegalArgumentException("Number of valid points < 1");

        if(numValid * 3 > controlPointCoordinates.length)
            controlPointCoordinates = new float[numValid * 3];

        for(int i = 0; i < numValid * 3; )
        {
            controlPointCoordinates[i] = (float)controlPoints[i];
            i++;
            controlPointCoordinates[i] = (float)controlPoints[i];
            i++;
            controlPointCoordinates[i] = (float)controlPoints[i];
            i++;
        }

        numControlPoints = numValid;

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           (numValid > controlPointWeights.length))
        {
            float[] tmp = new float[numValid];

            if(controlPointWeights.length != 0) {
                int cnt = numValid < controlPointWeights.length ?
                          numValid :
                          controlPointWeights.length;

                System.arraycopy(controlPointWeights, 0, tmp, 0, cnt);
            }

            controlPointWeights = tmp;
        }

        curveChanged = true;
    }

    /**
     * Set the curve controlPoints from a subset of the given array. The array
     * is presented with the coordinates flattened as [Xn, Yn, Zn] in the width
     * array. The order of the patch is determined by the and number of points.
     * If the arrays are not of minimum length 3 an exception is generated.
     * <p>
     * Will automatically turn on the use of control point weight calculations
     * (rational form).
     *
     * @param controlPoints The controlPoint coordinate values
     * @param numValid The number of valid points in the array
     * @param weights Weight values to go with the points
     */
    public void setControlPoints(float[] controlPoints,
                                 int numValid,
                                 float[] weights)
    {
        if(numValid < 1)
            throw new IllegalArgumentException("Number of valid points < 1");

        if(numValid * 3 > controlPointCoordinates.length)
            controlPointCoordinates = new float[numValid * 3];

        System.arraycopy(controlPoints,
                         0,
                         controlPointCoordinates,
                         0,
                         numValid * 3);

        numControlPoints = numValid;

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           (controlPointWeights.length < numValid))
        {
            controlPointWeights = new float[numValid];
        }

        System.arraycopy(weights,
                         0,
                         controlPointWeights,
                         0,
                         numValid);

        curveChanged = true;
        useControlPointWeights = true;
    }

    /**
     * Set the curve controlPoints from a subset of the given array. The array
     * is presented with the coordinates flattened as [Xn, Yn, Zn] in the width
     * array. The order of the patch is determined by the and number of points.
     * If the arrays are not of minimum length 3 an exception is generated.
     * <p>
     * Will automatically turn on the use of control point weight calculations
     * (rational form).
     *
     * @param controlPoints The controlPoint coordinate values
     * @param numValid The number of valid points in the array
     * @param weights Weight values to go with the points
     */
    public void setControlPoints(double[] controlPoints,
                                 int numValid,
                                 double[] weights)
    {
        if(numValid < 1)
            throw new IllegalArgumentException("Number of valid points < 1");

        if(numValid * 3 > controlPointCoordinates.length)
            controlPointCoordinates = new float[numValid * 3];

        for(int i = 0; i < numValid * 3; )
        {
            controlPointCoordinates[i] = (float)controlPoints[i];
            i++;
            controlPointCoordinates[i] = (float)controlPoints[i];
            i++;
            controlPointCoordinates[i] = (float)controlPoints[i];
            i++;
        }

        numControlPoints = numValid;

        // Adjust the control point weight size if needed.
        if((controlPointWeights == null) ||
           (controlPointWeights.length < numValid))
        {
            controlPointWeights = new float[numValid];
        }

        for(int i = 0; i < numValid; i++)
            controlPointWeights[i] = (float)weights[i];

        curveChanged = true;
        useControlPointWeights = true;
    }

    /**
     * Set the curve degree and knots. The degree must be of order 2 or
     * greater. If the array is not of minimum length 3 an exception is
     * generated.
     *
     * @param n The degree of the curve
     * @param knts The knot values to control the curve
     */
    public void setKnots(int n, float[] knts)
    {
        if(n < 2)
            throw new IllegalArgumentException("Degree is < 2");

        if(knts.length < (numControlPoints + n + 1))
            throw new IllegalArgumentException("knts.length < n + k + 1");

        degree = n;

        if(knts.length > knots.length)
            knots = new float[knts.length];

        System.arraycopy(knts, 0, knots, 0, knts.length);

        numKnots = knots.length;
        curveChanged = true;
    }

    /**
     * Set the curve degree and knots. The degree must be of order 2 or
     * greater. If the array is not of minimum length 3 an exception is
     * generated.
     *
     * @param n The degree of the curve
     * @param knts The knot values to control the curve
     */
    public void setKnots(int n, double[] knts)
    {
        if(n < 2)
            throw new IllegalArgumentException("Degree is < 2");

        if(knts.length < (numControlPoints + n + 1))
            throw new IllegalArgumentException("knts.length < n + k + 1");

        degree = n;

        if(knts.length > knots.length)
            knots = new float[knts.length];

        for(int i = 0; i < knts.length; i++)
            knots[i] = (float)knts[i];

        numKnots = knots.length;
        curveChanged = true;
    }

    /**
     * Get the degree of the curve being generated.
     *
     * @return A value >= 2
     */
    public int getDegree()
    {
        return degree;
    }
    
    /**
     * Return a copy of the knot values.
     *
     * @return The knot values
     */
    public float[] getKnots()
    {
        float[] knts = null;
        if (knots != null) 
        {
            knts = new float[knots.length];
            for(int i = 0; i < knots.length; i++)
            {
                knts[i] = (float)knots[i];
            }
        }
        else {
            knts = new float[0];
        }
        return(knts);
    }

    /**
     * Convenience method to set knots that give a better looking curve shape and
     * set a new degree for the curve directly.
     * The traditional way of setting these is to have knot[i] = i, but
     * whenever curves change this results in a lot of extra calculations. This
     * smoothing function will localize the changes at any particular breakpoint
     * in the line.
     *
     * @param t The degree of the curve
     */
    public void generateSmoothKnots(int t)
    {
        if(t < 2)
            throw new IllegalArgumentException("Degree is < 2");

        if(t != degree)
        {
            degree = t;
            curveChanged = true;
        }

        generateSmoothKnots();
    }

    /**
     * Convenience method to set knots that give a better looking curve shape
     * using the existing curve degree. The traditional way of setting these is
     * to have knot[i] = i, but whenever curves change this results in a lot of
     * extra calculations. This smoothing function will localize the changes at
     * any particular breakpoint in the line.
     */
    public void generateSmoothKnots()
    {
        // resize if necessary
        numKnots = numControlPoints + degree + 1;
        if(knots.length < numKnots)
            knots = new float[numKnots];

        int j;

        for(j = 0; j < numKnots; j++)
        {
            if(j <= degree)
                knots[j] = 0;
            else if(j < numControlPoints)
                knots[j] = j - degree + 1;
            else if(j >= numControlPoints)
                knots[j] = numControlPoints - degree + 1;
        }
    }

    /**
     * Get the number of vertices that this generator will create for the
     * curve. This is just the number of facets + 1.
     *
     * @return The vertex count for the object
     * @throws UnsupportedTypeException The generator cannot handle the type
     *   of geometry you have requested
     */
    public int getVertexCount(GeometryData data)
        throws UnsupportedTypeException
    {
        int ret_val = 0;

        switch(data.geometryType)
        {
            case GeometryData.LINES:
                ret_val = (facetCount + 1) * 2;
                break;
            case GeometryData.LINE_STRIPS:
            case GeometryData.INDEXED_LINES:
            case GeometryData.INDEXED_LINE_STRIPS:
                ret_val = facetCount + 1;
                break;

            default:
                throw new UnsupportedTypeException("Unknown geometry type: " +
                                                   data.geometryType);
        }

        return ret_val;
    }

    /**
     * Generate a new set of geometry items patchd on the passed data. If the
     * data does not contain the right minimum array lengths an exception will
     * be generated. If the array reference is null, this will create arrays
     * of the correct length and assign them to the return value.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     * @throws UnsupportedTypeException The generator cannot handle the type
     *   of geometry you have requested
     */
    public void generate(GeometryData data)
        throws UnsupportedTypeException, InvalidArraySizeException
    {
        switch(data.geometryType)
        {
            case GeometryData.LINES:
                unindexedLines(data);
                break;
            case GeometryData.LINE_STRIPS:
                lineStrips(data);
                break;
            case GeometryData.INDEXED_LINES:
                indexedLines(data);
                break;
            case GeometryData.INDEXED_LINE_STRIPS:
                indexedLineStrips(data);
                break;

            default:
                throw new UnsupportedTypeException("Unknown geometry type: " +
                                                   data.geometryType);
        }
    }

   /**
     * Generate a new set of points for an unindexed quad array
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void unindexedLines(GeometryData data)
        throws InvalidArraySizeException
    {
        generateUnindexedLineCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateUnindexedLineNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateLineTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateLineTexture3D(data);
    }


    /**
     * Generate a new set of points for an indexed triangle array
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void indexedLines(GeometryData data)
        throws InvalidArraySizeException
    {
        generateIndexedLineCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedLineNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateLineTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateLineTexture3D(data);

        int idx_cnt = (facetCount + 1) * 2;

        if(data.indexes == null)
            data.indexes = new int[idx_cnt];
        else if(data.indexes.length < idx_cnt)
            throw new InvalidArraySizeException("Index values",
                                                data.coordinates.length,
                                                idx_cnt);

        int[] indexes = data.indexes;
        data.indexesCount = idx_cnt;

        int idx = 0;

        for(int i = 0; i < idx_cnt; )
        {
            indexes[i++] = idx++;
            indexes[i++] = idx;
        }
    }

    /**
     * Generate a new set of points for a triangle strip array. Each side is a
     * strip of two faces.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void lineStrips(GeometryData data)
        throws InvalidArraySizeException
    {
        generateIndexedLineCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedLineNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateLineTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateLineTexture3D(data);

        if(data.stripCounts == null)
            data.stripCounts = new int[1];
        else if(data.stripCounts.length < 1)
            throw new InvalidArraySizeException("Strip counts",
                                                data.stripCounts.length,
                                                1);

        data.numStrips = 1;
        data.stripCounts[0] = facetCount + 1;
    }

    /**
     * Generate a new set of points for an indexed triangle strip array. We
     * build the strip from the existing points, and there's no need to
     * re-order the points for the indexes this time.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void indexedLineStrips(GeometryData data)
        throws InvalidArraySizeException
    {
        generateIndexedLineCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedLineNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateLineTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateLineTexture3D(data);

        int idx_cnt = (facetCount + 1) * 2;

        if(data.indexes == null)
            data.indexes = new int[idx_cnt];
        else if(data.indexes.length < idx_cnt)
            throw new InvalidArraySizeException("Index values",
                                                data.indexes.length,
                                                idx_cnt);

        int[] indexes = data.indexes;
        data.indexesCount = idx_cnt;

        int idx = 0;

        for(int i = 0; i < idx_cnt; )
        {
            indexes[i++] = idx++;
            indexes[i++] = idx;
        }

        if(data.stripCounts == null)
            data.stripCounts = new int[1];
        else if(data.stripCounts.length < 1)
            throw new InvalidArraySizeException("Strip counts",
                                                data.stripCounts.length,
                                                1);

        data.numStrips = 1;
        data.stripCounts[0] = facetCount + 1;
    }

    //------------------------------------------------------------------------
    // Coordinate generation routines
    //------------------------------------------------------------------------

    /**
     * Generates new set of points suitable for use in an unindexed array. Each
     * patch coordinate will appear twice in this list. The first half of the
     * array is the top, the second half, the bottom.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedLineCoordinates(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = getVertexCount(data);

        if(data.coordinates == null)
            data.coordinates = new float[vtx_cnt * 3];
        else if(data.coordinates.length < vtx_cnt * 3)
            throw new InvalidArraySizeException("Coordinates",
                                                data.coordinates.length,
                                                vtx_cnt * 3);

        float[] coords = data.coordinates;
        data.vertexCount = vtx_cnt;

        regenerateCurve();

        int vtx = 0;
        int c_count = 0;

        for(int i = 0; i < facetCount; i++)
        {
            coords[vtx] = curveCoordinates[c_count];
            vtx++;
            coords[vtx] = curveCoordinates[c_count + 1];
            vtx++;
            coords[vtx] = curveCoordinates[c_count + 2];
            vtx++;

            coords[vtx] = curveCoordinates[c_count + 3];
            vtx++;
            coords[vtx] = curveCoordinates[c_count + 4];
            vtx++;
            coords[vtx] = curveCoordinates[c_count + 5];
            vtx++;

            c_count += 3;
        }
    }

    /**
     * Generate a new set of points for use in an indexed array. The first
     * index will always be the cone tip - parallel for each face so that we
     * can get the smoothing right. If the array is to use the bottom,
     * a second set of coordinates will be produced separately for the patch
     * so that independent surface normals can be used. These values will
     * start at vertexCount / 2 with the first value as 0,0,0 (the center of
     * the patch) and then all the following values as the patch.
     */
    private void generateIndexedLineCoordinates(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = getVertexCount(data);

        if(data.coordinates == null)
            data.coordinates = new float[vtx_cnt * 3];
        else if(data.coordinates.length < vtx_cnt * 3)
            throw new InvalidArraySizeException("Coordinates",
                                                data.coordinates.length,
                                                vtx_cnt * 3);

        float[] coords = data.coordinates;
        data.vertexCount = vtx_cnt;

        regenerateCurve();

        // Copy the raw coords, the make the set of indicies for it
        System.arraycopy(curveCoordinates, 0, coords, 0, numCurveValues);
    }

    //------------------------------------------------------------------------
    // Normal generation routines
    //------------------------------------------------------------------------

    /**
     * Generate a new set of normals for a normal set of unindexed points.
     * Smooth normals are used for the sides at the average between the faces.
     * Bottom normals always point down.
     * <p>
     * This must always be called after the coordinate generation. The
     * top normal of the cone is always perpendicular to the face.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedLineNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);
    }

    /**
     * Generate a new set of normals for a normal set of indexed points.
     * Handles both flat and smooth shading of normals. Flat just has them
     * perpendicular to the face. Smooth has them at the value at the
     * average between the faces. Bottom normals always point down.
     * <p>
     * This must always be called after the coordinate generation. The
     * top normal of the cone is always perpendicular to the face.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateIndexedLineNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);
    }

    //------------------------------------------------------------------------
    // Texture coordinate generation routines
    //------------------------------------------------------------------------

    /**
     * Generate a new set of texCoords for a normal set of unindexed points. Each
     * normal faces directly perpendicular for each point. This makes each face
     * seem flat.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateLineTexture2D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 2;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("2D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        float[] texCoords = data.textureCoordinates;
    }

    /**
     * Generate a new set of texCoords for a normal set of unindexed points. Each
     * normal faces directly perpendicular for each point. This makes each face
     * seem flat.
     * <p>
     * This must always be called after the coordinate generation.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateLineTexture3D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 2;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("3D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        float[] texCoords = data.textureCoordinates;
    }

    /**
     * Regenerate the patch coordinate points. These are the flat circle that
     * makes up the patch of the code. The coordinates are generated patchd on
     * the 2 PI divided by the number of facets to generate.
     */
    private final void regenerateCurve()
    {
        if(!curveChanged)
            return;

        curveChanged = false;

        numCurveValues = (facetCount + 1) * 3;

        if((curveCoordinates == null) ||
           (numCurveValues > curveCoordinates.length))
        {
            curveCoordinates = new float[numCurveValues];
        }

        double interval = 0;
//        double increment = knots[numKnots - 1] / (double)(facetCount - 1);
        double increment = (numControlPoints - degree + 1) / (double)(facetCount + 1);
        int coord = 0;

        if(useControlPointWeights)
        {
            for(int i = 0; i < facetCount; i++)
            {
                calcSingleRationalPoint(interval, coord);
                interval += increment;
                coord += 3;
            }

            int last = (numControlPoints - 1) * 3;
            curveCoordinates[coord] = controlPointCoordinates[last];
            curveCoordinates[coord + 1] = controlPointCoordinates[last + 1];
            curveCoordinates[coord + 2] = controlPointCoordinates[last + 2];
        }
        else
        {
            for(int i = 0; i < facetCount; i++)
            {
                calcSingleNonRationalPoint(interval, coord);
                interval += increment;
                coord += 3;
            }

            int last = (numControlPoints - 1) * 3;
            curveCoordinates[coord] = controlPointCoordinates[last];
            curveCoordinates[coord + 1] = controlPointCoordinates[last + 1];
            curveCoordinates[coord + 2] = controlPointCoordinates[last + 2];
        }
    }

    /**
     * Calculate the position of a single point on the spline curve.
     * The parameter "v" indicates the position, it ranges from 0 to n-t+2
     *
     * @param t the position along the curve
     * @param out The location to put the curve point in the output array
     */
    private void calcSingleNonRationalPoint(double t, int out)
    {
        double x = 0;
        double y = 0;
        double z = 0;
        int order = degree + 1;

        for(int k = 0; k < numControlPoints; k++) {
            double b = splineBlend(k, order, t);

            x += controlPointCoordinates[k * 3] * b;
            y += controlPointCoordinates[k * 3 + 1] * b;
            z += controlPointCoordinates[k * 3 + 2] * b;
        }

        curveCoordinates[out] = (float)x;
        curveCoordinates[out + 1] = (float)y;
        curveCoordinates[out + 2] = (float)z;
    }

    /**
     * Calculate the position of a single point on the rational spline curve.
     * The parameter "v" indicates the position, it ranges from 0 to n-t+2
     *
     * @param t the position along the curve
     * @param out The location to put the curve point in the output array
     */
    private void calcSingleRationalPoint(double t, int out)
    {
        double x = 0;
        double y = 0;
        double z = 0;
        double w = 0;
        double denom = 0;
        int order = degree + 1;

        for(int k = 0; k < numControlPoints; k++) {
            double b = splineBlend(k, order, t);
            w = controlPointWeights[k];

            x += controlPointCoordinates[k * 3] * b * w;
            y += controlPointCoordinates[k * 3 + 1] * b * w;
            z += controlPointCoordinates[k * 3 + 2] * b * w;

            denom += b * w;
        }

        if(denom != 0)
        {
            curveCoordinates[out] = (float)(x / denom);
            curveCoordinates[out + 1] = (float)(y / denom);
            curveCoordinates[out + 2] = (float)(z / denom);
        }
        else
        {
            curveCoordinates[out] = (float)x;
            curveCoordinates[out + 1] = (float)y;
            curveCoordinates[out + 2] = (float)z;
        }
    }

    /**
     * Calculate the blending value, this is done recursively.
     *  If the numerator and denominator are 0 the expression is 0.
     *  If the deonimator is 0 the expression is 0.
     *
     * @param i The basis function to check
     * @param k The order of the curve
     * @param t The position along the curve to check ie N(t)
     */
    //private double splineBlend(int i, int k, double t)
    public double splineBlend(int i, int k, double t)
    {
        double ret_val;
        // Do this just to make the maths traceable with the std algorithm
        float[] u = knots;

        if(k == 1)
        {
            ret_val = ((u[i] <= t) && (t < u[i+1])) ? 1 : 0;
        }
        else
        {
            double b1 = splineBlend(i, k-1, t);
            double b2 = splineBlend(i+1, k-1, t);

            double d1 = u[i+k-1] - u[i];
            double d2 = u[i+k] - u[i+1];

            double e, f;

            e = (b1 != 0) ? (t - u[i]) / d1 * b1 : 0;
            f = (b2 != 0) ? (u[i+k] - t) / d2  * b2 : 0;

            ret_val = e + f;
/*
            if(d1 == 0 && d2 == 0)
                ret_val = 0;
            else if(d1 == 0)
                ret_val = (u[i+k] - t) / d2 * splineBlend(i+1, k-1, t);
            else if(d2 == 0)
                ret_val = (t - u[i]) / d1 * splineBlend(i, k-1, t);
            else
                ret_val = (t - u[i]) / d1 * splineBlend(i, k-1, t) +
                          (u[i+k] - t) / d2 * splineBlend(i+1, k-1, t);
*/
        }

        return ret_val;
    }
}
