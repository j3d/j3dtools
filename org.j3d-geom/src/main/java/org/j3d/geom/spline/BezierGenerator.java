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
 * Geometry generator for generating a single Bezier curve.
 * <P>
 *
 * Bezier curves of all orders are permitted. Order information is derived
 * from the provided controlPoint coordinates.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class BezierGenerator extends GeometryGenerator
{
    /** Default number of segments used in the cone */
    private static final int DEFAULT_FACETS = 16;

    /** An array of factorial values. Resized as needed */
    private static int[] FACTORIALS = { 1, 1, 2, 6, 24, 120, 720 };

    /** The number of sections used around the cone */
    private int facetCount;

    /** Knot values used to generate patches in flat array */
    private float[] controlPointCoordinates;

    /** The number of know values */
    private int numControlPoints;

    /** Coordinates of the generated curve */
    private float[] curveCoordinates;

    /** The number of valid values in the curve array (facetCount + 1 * 3) */
    private int numCurveValues;

    /** Flag to say the curve setup has changed */
    private boolean curveChanged;

    /**
     * Construct a new generator with default settings of 16 line segments over
     * the length of one curve.
     */
    public BezierGenerator()
    {
        this(DEFAULT_FACETS);
    }

    /**
     * Construct a new generator with the specified number of tessellations
     * over the length of the curve, regardless of extents.
     *
     * @param facets The number of facets on the side of the curve
     * @throws IllegalArgumentException The number of facets is less than 3
     */
    public BezierGenerator(int facets)
    {
        if(facets < 3)
            throw new IllegalArgumentException("Number of facets is < 3");

        curveChanged = true;
        facetCount = facets;
        numControlPoints = 0;
        numCurveValues = 0;

        // Assume a basic bezier with only 3 control points;
        controlPointCoordinates = new float[9];
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
     * 3 an exception is generated.
     *
     * @param controlPoints The controlPoint coordinate values
     */
    public void setControlPoints(float[] controlPoints)
    {
        setControlPoints(controlPoints, controlPoints.length / 3);
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

        curveChanged = true;
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

        int coord = 0;
        float div = 1 / (float)facetCount;

        for(int i = 0; i < facetCount; i++)
        {
            calcSinglePoint(i * div, coord);
            coord += 3;
        }

        int ncp = numControlPoints * 3;

        curveCoordinates[numCurveValues - 3] = controlPointCoordinates[ncp - 3];
        curveCoordinates[numCurveValues - 2] = controlPointCoordinates[ncp - 2];
        curveCoordinates[numCurveValues - 1] = controlPointCoordinates[ncp - 1];
    }

    /**
     * Calculate a single point along the bezier curve and place it into
     * the point array.
     */
    private void calcSinglePoint(float mu, int out)
    {
        int k, kn, nn, nkn;
        double blend, muk, munk;
        float x = 0;
        float y = 0;
        float z = 0;

        muk = 1;
        munk = Math.pow(1 - mu, (numControlPoints - 1));

        for(k = 0; k < numControlPoints; k++)
        {
            nn = numControlPoints - 1;
            kn = k;
            nkn = numControlPoints - 1 - k;
            blend = muk * munk;
            muk *= mu;
            munk /= (1 - mu);

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

            x += controlPointCoordinates[k * 3] * blend;
            y += controlPointCoordinates[k * 3 + 1] * blend;
            z += controlPointCoordinates[k * 3 + 2] * blend;
        }

        curveCoordinates[out] = x;
        curveCoordinates[out + 1] = y;
        curveCoordinates[out + 2] = z;
    }
}
