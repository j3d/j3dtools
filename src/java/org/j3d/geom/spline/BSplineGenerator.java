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
 * @version $Revision: 1.1 $
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

    /** The number of control points. x3 for the number of array items */
    private int numControlPoints;

    /** Knots on the curve to control weighting. */
    private int[] knots;

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

        // Assume a basic bezier with only 3 control points;
        controlPointCoordinates = new float[9];
        knots = new int[numKnots];
        curveCoordinates = new float[(facets + 1) * 3];
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
     * Set the curve controlPoints. The array is presented with the coordinates
     * flattened as [Xn, Yn, Zn] in the width array. The order of the patch is
     * determined by the passed array. If the arrays are not of minimum length
     * 3 and equal length an exception is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     */
    public void setControlPoints(float[] controlPoints)
    {
        if(controlPoints.length < 3)
            throw new IllegalArgumentException("Depth patch size < 3");

        // second check for consistent lengths of the width patches
        int i;

        if(controlPoints.length > controlPointCoordinates.length)
            controlPointCoordinates = new float[controlPoints.length];

        System.arraycopy(controlPoints,
                         0,
                         controlPointCoordinates,
                         0,
                         controlPoints.length);

        numControlPoints = controlPoints.length / 3;

        curveChanged = true;
    }

    /**
     * Set the curve degree and knots. The degree must be of order 2 or
     * greater. If the array is not of minimum length 3 an exception is
     * generated.
     *
     * @param t The degree of the curve
     * @param knts The knot values to control the curve
     */
    public void setKnots(int t, int[] knts)
    {
        if(t < 2)
            throw new IllegalArgumentException("Degree is < 2");

        if(knts.length < (numControlPoints + t + 1))
            throw new IllegalArgumentException("Knots < 3");

        if(t != degree)
        {
            degree = t;
            curveChanged = true;
        }

        if(knts.length > knots.length)
            knots = new int[knts.length];

        System.arraycopy(knts, 0, knots, 0, knts.length);

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
     *
     * @param t The degree of the curve
     */
    public void generateSmoothKnots()
    {
        // resize if necessary
        numKnots = numControlPoints + degree;
        if(knots.length < numKnots)
            knots = new int[numKnots];

        int j;

        for(j = 0; j < numKnots; j++)
        {
            if(j < degree)
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
        int vtx_cnt = getVertexCount( data);

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
        double increment = (numControlPoints - degree + 1) / (double)facetCount;
        int coord = 0;

        for(int i = 0; i < facetCount; i++)
        {
            calcSinglePoint(interval, coord);
            interval += increment;
            coord += 3;
        }

        int ncp = numControlPoints * 3;
        curveCoordinates[facetCount * 3] = controlPointCoordinates[ncp - 3];
        curveCoordinates[facetCount * 3 + 1] = controlPointCoordinates[ncp - 2];
        curveCoordinates[facetCount * 3 + 2] = controlPointCoordinates[ncp - 1];
    }



    /**
     * Calcykulate the position of a single point on the spline curve.
     * The parameter "v" indicates the position, it ranges from 0 to n-t+2
     *
     */
    private void calcSinglePoint(double v, int out)
    {
        double x = 0;
        double y = 0;
        double z = 0;

        for(int k = 0; k < numControlPoints; k++) {
            double b = splineBlend(k, degree, v);
            x += controlPointCoordinates[k * 3] * b;
            y += controlPointCoordinates[k * 3 + 1] * b;
            z += controlPointCoordinates[k * 3 + 2] * b;
        }

        curveCoordinates[out] = (float)x;
        curveCoordinates[out + 1] = (float)y;
        curveCoordinates[out + 2] = (float)z;
    }

    /**
     * Calculate the blending value, this is done recursively.
     *  If the numerator and denominator are 0 the expression is 0.
     *  If the deonimator is 0 the expression is 0.
     *
     *
     */
    private double splineBlend(int k, int t, double v)
    {
        double ret_val;

        // Do this just to make the maths traceable with the std algorithm
        int[] u = knots;

        if(t == 1)
        {
            ret_val = ((u[k] <= v) && (v < u[k+1])) ? 1 : 0;
        }
        else
        {
            if((u[k+t-1] == u[k]) && (u[k+t] == u[k+1]))
                ret_val = 0;
            else if (u[k+t-1] == u[k])
                ret_val = (u[k+t] - v) / (u[k+t] - u[k+1]) * splineBlend(k+1, t-1, v);
            else if (u[k+t] == u[k+1])
                ret_val = (v - u[k]) / (u[k+t-1] - u[k]) * splineBlend(k, t-1, v);
            else
                ret_val = (v - u[k]) / (u[k+t-1] - u[k]) * splineBlend(k, t-1, v) +
                          (u[k+t] - v) / (u[k+t] - u[k+1]) * splineBlend(k+1, t-1, v);
        }

        return ret_val;
    }
}
