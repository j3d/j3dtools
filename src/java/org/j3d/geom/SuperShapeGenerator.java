/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
// none

// Application specific imports
// None

/**
 * Generalised shape generator that is capable of generating almost any 2D
 * shape using a standard algorithm.
 * <P>
 *
 * This class generates 3D coordinates for a single flat object in the X,Y
 * plane with a Z value of zero. Because this is only generating 2D shapes
 * in reality, it only supports the LINE_ geometry types. All Triangle and
 * quad forms are not supported.
 * <p>
 *
 * The basic equation for a supershape comes from the standard form that you
 * are familiar with for any ellipsoid.
 * <pre>
 *    (x / a)^2 + (y / b)^2 = r^2
 * </pre>
 *
 * Turning this into polar coordinates, the equation becomes<br>
 * <img src="doc-files/supershape_equation.png" width="500" height="85"><br>
 * Below are some example shapes from the various coordinates.
 *
 * <table>
 * <tr><td colspan="6">m = 0. This results in circles, namely r = 1</td></tr>
 * <tr><td colspan="6"><img src="doc-files/circle.png"></td></tr>
 * <tr><td colspan="6">n1 = n2 = n3 = 1 Increasing m adds rotational symmetry
 *     to the shape. This is generally the case for other values of the n
 *     parameters. The curves are repeated in sections of the circle of angle
 *     2 * pi/m, this is apparent in most of the following examples for integer
 *     values of m.</td></tr>
 * <tr align="center"><td>m = 1</td><td>m = 2</td><td>m = 3</td>
 *     <td>m = 4</td><td>m = 5</td><td>m = 6</td></tr>
 * </tr>
 * <tr><td><img src="doc-files/1_1_1_1.png"></td>
 *     <td><img src="doc-files/2_1_1_1.png"></td>
 *     <td><img src="doc-files/3_1_1_1.png"></td>
 *     <td><img src="doc-files/4_1_1_1.png"></td>
 *     <td><img src="doc-files/5_1_1_1.png"></td>
 *     <td><img src="doc-files/6_1_1_1.png"></td>
 * </tr>
 * <tr><td colspan="6">If n<sub>1</sub> is slightly larger than n<sub>2</sub>
 *     and n<sub>3</sub> then bloated forms result. The examples on the right
 *     have n<sub>1</sub> = 40 and n<sub>2</sub> = n<sub>3</sub> = 10.</td></tr>
 * <tr align="center"><td>m = 1</td><td>m = 2</td><td>m = 3</td>
 *     <td>m = 4</td><td>m = 5</td><td>m = 6</td></tr>
 * </tr>
 * <tr><td><img src="doc-files/1_03_03_03.png"></td>
 *     <td><img src="doc-files/2_03_03_03.png"></td>
 *     <td><img src="doc-files/3_03_03_03.png"></td>
 *     <td><img src="doc-files/4_03_03_03.png"></td>
 *     <td><img src="doc-files/5_03_03_03.png"></td>
 *     <td><img src="doc-files/6_03_03_03.png"></td>
 * </tr>
 * <tr><td colspan="6">Polygonal shapes are achieved with very large values
 *     of n<sub>1</sub> and large but equal values for n<sub>2</sub> and
 *     n<sub>3</sub>. </td></tr>
 * <tr align="center"><td>m = 3,<br/> n1 = 1000,<br/> n2 = 1980,<br/> n3 = 1980</td>
 *     <td>m = 4,<br/> n1 = 1000,<br/> n2 = 1000,<br/> n3 = 1000</td>
 *     <td>m = 5,<br/> n1 = 1000,<br/> n2 = 620,<br/> n3 = 620</td>
 *     <td>m = 6,<br/> n1 = 1000,<br/> n2 = 390,<br/> n3 = 390</td>
 * </tr>
 * <tr><td><img src="doc-files/3_1000_1980_1980.png"></td>
 *     <td><img src="doc-files/4_1000_1000_1000.png"></td>
 *     <td><img src="doc-files/5_1000_620_620.png"></td>
 *     <td><img src="doc-files/6_1000_390_390.png"></td>
 * </tr>
 * <tr><td colspan="6">Asymmetric forms can be created by using different
 *     values for the n's. The following example have n<sub>1</sub> = 60,
 *     n<sub>2</sub> = 55 and n<sub>3</sub> = 30. </td></tr>
 * <tr align="center"><td>m = 3</td><td>m = 4</td><td>m = 5</td><td>m = 6</td></tr>
 * <tr><td><img src="doc-files/3_60_55_30.png"></td>
 *     <td><img src="doc-files/4_60_55_30.png"></td>
 *     <td><img src="doc-files/5_60_55_30.png"></td>
 *     <td><img src="doc-files/6_60_55_30.png"></td>
 * </tr>
 * <tr><td colspan="6">For non integral values of m the form is still closed
 *     for rational values. The following are example with n<sub>1</sub> =
 *     n<sub>2</sub> = n<sub>3</sub> = 0.3. The max angle needs to extend from
 *     0 to 12 pi. </td></tr>
 * <tr align="center"><td>m = 1/6</td>
 *     <td>m = 7/6</td><td>m = 13/6</td><td>m = 19/6</td></tr>
 * <tr><td><img src="doc-files/016666_03_03_03.png"></td>
 *     <td><img src="doc-files/116666_03_03_03.png"></td>
 *     <td><img src="doc-files/216666_03_03_03.png"></td>
 *     <td><img src="doc-files/316666_03_03_03.png"></td>
 * </tr>
 * <tr><td colspan="6">Smooth starfish shapes result from smaller values of
 *     n<sub>1</sub> than the n<sub>2</sub> and n<sub>3</sub>. The following
 *     examples have m=3 and n2 = n3 = 1.7.</td></tr>
 * <tr align="center"><td>n<sub>1</sub>=0.50</td><td>n<sub>1</sub>=0.20</td>
 *     <td>n<sub>1</sub>=0.10</td><td>n<sub>1</sub>=0.02</td></tr>
 * <tr><td><img src="doc-files/3_05_17_17.png"></td>
 *     <td><img src="doc-files/3_02_17_17.png"></td>
 *     <td><img src="doc-files/3_01_17_17.png"></td>
 *     <td><img src="doc-files/3_002_17_17.png"></td>
 * </tr>
 * </table>
 *
 * The original idea as well as all the pictures are stolen from Paul
 * Bourke's SuperShape page:
 * <a href="http://astronomy.swin.edu.au/~pbourke/curves/supershape/">
 * http://astronomy.swin.edu.au/~pbourke/curves/supershape/</a>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class SuperShapeGenerator extends GeometryGenerator
{
    /** Default number of segments used in the patch */
    private static final int DEFAULT_FACETS = 50;

    /** The number of sections used around the cone */
    private int facetCount;

    /** Coordinates of the generated curve */
    private float[] curveCoordinates;

    /** The number of valid values in the curve array (facetCount + 1 * 3) */
    private int numCurveValues;

    /** Flag to say the curve setup has changed */
    private boolean curveChanged;

    /** The m coefficient */
    private double m;

    /** The n1 coefficient used for the order of the square root */
    private double n1;

    /** The n2 coefficient used for the power of the cos term */
    private double n2;

    /** The n3 coefficient used for the power of the sin term */
    private double n3;

    /** The 1/a coefficient of the cos term pre-calculated */
    private double one_on_a;

    /** The 1/b coefficient of the sin term pre-calculated */
    private double one_on_b;

    /** The maximum angle value to use for any rotational components */
    private double maxAngle;

    /**
     * Construct a new generator with default settings of 10 divisions over
     * a single side between two control points.
     */
    public SuperShapeGenerator()
    {
        this(DEFAULT_FACETS);
    }

    /**
     * Construct a new generator with the specified number of tessellations
     * over the side of the curve. The default coefficients will generate a
     * circle. The maximum angle defaults to 2 * pi.
     *
     * @param facets The number of facets on a segment of the curve
     * @throws IllegalArgumentException The number of divisions is less than 3
     */
    public SuperShapeGenerator(int facets)
    {
        if(facets < 3)
            throw new IllegalArgumentException("Number of facets is < 3");

        curveChanged = true;
        facetCount = facets;
        numCurveValues = 0;
        one_on_a = 1;
        one_on_b = 1;
        m = 1;
        n1 = 1;
        n2 = 1;
        n3 = 1;
        maxAngle = Math.PI * 2;

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
     * Set the coefficient values to generate a new shape. The values a and b
     * must not be zero and will generate an error if they are.
     *
     */
    public void setCoefficients(double m,
                                double n1,
                                double n2,
                                double n3,
                                double a,
                                double b)
    {
        if(a == 0)
            throw new IllegalArgumentException("a is zero");

        if(b == 0)
            throw new IllegalArgumentException("b is zero");

        this.m = m;
        this.n1 = n1;
        this.n2 = n2;
        this.n3 = n3;
        one_on_a = 1 / a;
        one_on_b = 1 / b;
    }

    /**
     * Set the maximum angle to use for phi. Set separately because for the m
     * most part, this will be a value of 2 * pi and never change. No error
     * checking is performed.
     *
     * @param angle The maximum angle to use in radians
     */
    public void setMaxAngle(double angle)
    {
        maxAngle = angle;
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

        double div = maxAngle / (facetCount + 1);
        int coord = 0;

        for(int i = 0; i < facetCount; i++)
        {
            calculatePoint(div * i, coord);
            coord += 3;
        }

        // First is same as last point.
        curveCoordinates[coord++] = curveCoordinates[0];
        curveCoordinates[coord++] = curveCoordinates[1];
    }

    /**
     * Generate a single curve coordinate value point. Place it in the given offset of the
     * coordinate array.
     *
     * @param phi The rotational angle to use
     * @param offset The offset index into the ccordinate array
     */
    private void calculatePoint(double phi, int offset)
    {
        double r;
        double t1, t2;

        t1 = Math.cos(m * phi / 4) * one_on_a;
        t1 = Math.abs(t1);
        t1 = Math.pow(t1, n2);

        t2 = Math.sin(m * phi / 4) * one_on_b;
        t2 = Math.abs(t2);
        t2 = Math.pow(t2, n3);

        r = Math.pow(t1 + t2, 1 / n1);

        // Only ever set x and y. Z is always zero.
        if(Math.abs(r) == 0)
        {
            curveCoordinates[offset] = 0;
            curveCoordinates[offset + 1] = 0;
        } else {
            r = 1 / r;
            curveCoordinates[offset] = (float)(r * Math.cos(phi));
            curveCoordinates[offset + 1] = (float)(r * Math.sin(phi));
        }
    }
}