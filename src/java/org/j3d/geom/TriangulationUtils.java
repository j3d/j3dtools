/*****************************************************************************
 *                          J3D.org Copyright (c) 2000
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
// none

// Application specific imports
import org.j3d.util.HashSet;
import org.j3d.util.ObjectArray;


/**
 * Utility routines for triangulating arbitrary polygons.
 * <p>
 * The concave triangulation routine is designed for small numbers of vertices
 * as it uses a much simpler, but slower algorithm that is O(kn) where k is the
 * number of concave vertices, rather than the more efficient, but much more
 * complex to implement Seidel's Algorithm. A summary of the implementation can
 * be found
 * <a href="http://www.mema.ucl.ac.be/~wu/FSA2716-2002/project.html">here</a>.
 * <p>
 * If at any time an error is detected in the input geometry, the return value
 * of the triangulation methods will be a negative value. The number will still
 * indicate the number of triangles successfully created for the return, but the
 * negative is used to indicate an error occurred that could not allow for any
 * more triangulation to take place.
 * <p>
 *
 * Seidel's algorithm is described here:
 * http://www.cs.unc.edu/~dm/CODE/GEM/chapter.html
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class TriangulationUtils
{
    /** The default size of the polygon values */
    private static final int DEFAULT_POLY_SIZE = 6;

    /** Triangulator for the simple polygons */
    private EarCutTriangulator ecTriangulator;

    /** Triangulator for the complex polygons */
    private SeidelTriangulator holeTriangulator;

    /** Initialisation size stored for the ecTriangulator */
    private int initSize;

    /**
     * Construct a new instance of the triangulation utilities. Assumes a
     * default max polygon size of 6 vertices.
     */
    public TriangulationUtils()
    {
        this(DEFAULT_POLY_SIZE);
    }

    /**
     * Construct a new instance of the triangulation utilities with a given
     * maximum polygon size hint. A number of internal structures are created
     * and this hint is used to ensure that the structures are large enough and
     * don't need to be dynamically re-created during the triangulation
     * process.
     *
     * @param size Hint to the maximum size of polygon to deal with
     */
    public TriangulationUtils(int size)
    {
        initSize = size;
    }

    /**
     * Triangulate a simple polygon that may have zero or more holes in it.
     * There is no requirement for the polygon to be concave, but it will be
     * limited to two-dimensional coordinate systems. If you have a polygon
     * in 3 dimensions, you will need to project it to 2 dimensions before
     * calling this method.
     * <p>
     * Each part of the polygon is defined by a set of contour points. The
     * outer-most set of points must be defined in counter-clockwise order.
     * All inner points must be defined in clock-wise order.
     *
     */
    public void triangulatePolygon2D(int numContours,
                                     int[] contourCounts,
                                     float[] vertices,
                                     int[] triangles)
    {
        if(holeTriangulator == null)
            holeTriangulator = new SeidelTriangulator();

        holeTriangulator.triangulatePolygon(numContours,
                                            contourCounts,
                                            vertices,
                                            triangles);
    }

    /**
     * Triangulate a concave polygon using an indexed coordinates array. The
     * start index is the index into the indexArray for the first vertex of the
     * polygon. The coordinates are not required to be a closed polygon as the
     * algorithm will automatically close it. The output array is the new indexes
     * to use.
     * <p>
     * If an error occurs, the result will be negative number of triangles.
     *
     * @param coords The coordinates of the face
     * @param startIndex The index of the first coordinate in the face
     * @param numVertex  The number of vertices to read from the list
     * @param coordOutput The array to copy the coord index values to
     * @param normal The normal to this face these vertices are a part of
     * @return The number of triangles in the output array
     */
    public int triangulateConcavePolygon(float[] coords,
                                         int startIndex,
                                         int numVertex,
                                         int[] coordIndex,
                                         int[] coordOutput,
                                         float[] normal)
    {
        if(ecTriangulator == null)
            ecTriangulator = new EarCutTriangulator(initSize);

        return ecTriangulator.triangulateConcavePolygon(coords,
                                                        startIndex,
                                                        numVertex,
                                                        coordIndex,
                                                        0,
                                                        null,
                                                        0,
                                                        null,
                                                        0,
                                                        null,
                                                        coordOutput,
                                                        null,
                                                        null,
                                                        null,
                                                        normal);
    }

    /**
     * Triangulate a concave polygon using an indexed coordinates array.
     * Assumes that the index of the first coordinate in the coordIndex is 0.  Will
     * read every vertex from the coordIndex list (ie: numVertex = coordIndex.length)
     *  The coordinates are not required to be a closed polygon as the
     * algorithm will automatically close it. The output array is the new indexes
     * to use.
     * <p>
     * If an error occurs, the result will be negative number of triangles.
     *
     * @author Eric Fickenscher
     * @param coords The coordinates of the face
     * @param coordOutput The array to copy the coord index values to
     * @param normal The normal to this face these vertices are a part of
     * @return The number of triangles in the output array
     */
    public int triangulateConcavePolygon(float[] coords,
                                         int[] coordIndex,
                                         int[] coordOutput,
                                         float[] normal)
    {
        if(ecTriangulator == null)
            ecTriangulator = new EarCutTriangulator(initSize);

        return ecTriangulator.triangulateConcavePolygon(coords,
                                                        0,
                                                        coordIndex.length,
                                                        coordIndex,
                                                        0,
                                                        null,
                                                        0,
                                                        null,
                                                        0,
                                                        null,
                                                        coordOutput,
                                                        null,
                                                        null,
                                                        null,
                                                        normal);
    }

    /**
     * Triangulate a concave polygon using an indexed coordinates array. The
     * start index is the index into the indexArray for the first vertex of the
     * polygon. The coordinates are not required to be a closed polygon as the
     * algorithm will automatically close it. The output array is the new indexes
     * to use
     * <p>
     * If an error occurs, the result will be negative number of triangles.
     *
     * @param coords The coordinates of the face
     * @param startIndex The index of the first coordinate in the face
     * @param numVertex  The number of vertices to read from the list
     * @param firstNormalIndex The first position of the normalIndex array
     * @param normalIndex The index of normals for each coordinate
     * @param firstColorIndex The first position of the colorIndex array
     * @param colorIndex The index of color for each coordinate
     * @param firstTexCoordIndex The first position of the texCoordIndex array
     * @param texCoordIndex The index of textureCoordinates for each coordinate
     * @param coordOutput The array to copy the coord index values to
     * @param normalOutput The array to copy the normal index values to
     * @param colorOutput The array to copy the color index values to
     * @param texCoordOutput The array to copy the texCoord index values to
     * @param normal The normal to this face these vertices are a part of
     * @return The number of triangles in the output array
     */
    public int triangulateConcavePolygon(float[] coords,
                                         int startIndex,
                                         int numVertex,
                                         int[] coordIndex,
                                         int firstNormalIndex,
                                         int[] normalIndex,
                                         int firstColorIndex,
                                         int[] colorIndex,
                                         int firstTexCoordIndex,
                                         int[] texCoordIndex,
                                         int[] coordOutput,
                                         int[] normalOutput,
                                         int[] colorOutput,
                                         int[] texCoordOutput,
                                         float[] normal)
    {
        if(ecTriangulator == null)
            ecTriangulator = new EarCutTriangulator(initSize);

        return ecTriangulator.triangulateConcavePolygon(coords,
                                                        startIndex,
                                                        numVertex,
                                                        coordIndex,
                                                        firstNormalIndex,
                                                        normalIndex,
                                                        firstColorIndex,
                                                        colorIndex,
                                                        firstTexCoordIndex,
                                                        texCoordIndex,
                                                        coordOutput,
                                                        normalOutput,
                                                        colorOutput,
                                                        texCoordOutput,
                                                        normal);
    }

    /**
     * Triangulate a concave polygon in the given array. The array is a flat
     * array of coordinates of [...Xn, Yn, Zn....] values. The start index is
     * the index into the array of the X component of the first item, while the
     * endIndex is the index into the array of the X component of the last item.
     * The coordinates are not required to be a closed polygon as the algorithm
     * will automatically close it. The output array is indexes into the
     * original array (including compensating for the 3 index values per
     * coordinate)
     * <p>
     * If an error occurs, the result will be negative number of triangles.
     *
     * @param coords The coordinates of the face
     * @param startIndex The index of the first coordinate in the face
     * @param numVertex  The number of vertices to read from the list
     * @param coordOutput The array to copy the coord index values to
     * @param normal The normal to this face these vertices are a part of
     * @return The number of triangles in the output array
     */
    public int triangulateConcavePolygon(float[] coords,
                                         int startIndex,
                                         int numVertex,
                                         int[] coordOutput,
                                         float[] normal)
    {
        if(ecTriangulator == null)
            ecTriangulator = new EarCutTriangulator(initSize);

        return ecTriangulator.triangulateConcavePolygon(coords,
                                                        startIndex,
                                                        numVertex,
                                                        0,
                                                        0,
                                                        0,
                                                        coordOutput,
                                                        null,
                                                        null,
                                                        null,
                                                        normal);
    }

    /**
     * Triangulate a concave polygon in the given array. The array is a flat
     * array of coordinates of [...Xn, Yn, Zn....] values.
     * Assumes that the index of the first coordinate in the face is 0.
     * Will read every vertex from the list (ie: numVertex = coords.length/3)
     * The coordinates are not required to be a closed polygon as the algorithm
     * will automatically close it. The output array is indexes into the
     * original array (including compensating for the 3 index values per
     * coordinate)
     * <p>
     * If an error occurs, the result will be negative number of triangles.
     *
     * @author Eric Fickenscher
     * @param coords The coordinates of the face
     * @param coordOutput The array to copy the coord index values to
     * @param normal The normal to this face these vertices are a part of
     * @return The number of triangles in the output array
     */
    public int triangulateConcavePolygon(float[] coords,
                                         int[] coordOutput,
                                         float[] normal)
    {
        if(ecTriangulator == null)
            ecTriangulator = new EarCutTriangulator(initSize);

        return ecTriangulator.triangulateConcavePolygon(coords,
                                                        0,
                                                        coords.length/3,
                                                        0,
                                                        0,
                                                        0,
                                                        coordOutput,
                                                        null,
                                                        null,
                                                        null,
                                                        normal);
    }


    /**
     * Triangulate a concave polygon in the given array. The array is a flat
     * array of coordinates of [...Xn, Yn, Zn....] values. The start index is
     * the index into the array of the X component of the first item, while the
     * endIndex is the index into the array of the X component of the last item.
     * The coordinates are not required to be a closed polygon as the algorithm
     * will automatically close it. The output array is indexes into the
     * original array (including compensating for the 3 index values per
     * coordinate)
     * <p>
     * If an error occurs, the result will be negative number of triangles.
     *
     * @param coords The coordinates of the face
     * @param startIndex The index of the first coordinate in the face
     * @param numVertex  The number of vertices to read from the list
     * @param firstNormalIndex The first position of the normalIndex array
     * @param firstColorIndex The index of color for each coordinate
     * @param firstTexCoordIndex The first position of the texCoordIndex array
     * @param coordOutput The array to copy the coord index values to
     * @param normalOutput The array to copy the normal index values to
     * @param colorOutput The array to copy the color index values to
     * @param texCoordOutput The array to copy the texCoord index values to
     * @param normal The normal to this face these vertices are a part of
     * @return The number of triangles in the output array
     */
    public int triangulateConcavePolygon(float[] coords,
                                         int startIndex,
                                         int numVertex,
                                         int firstNormalIndex,
                                         int firstColorIndex,
                                         int firstTexCoordIndex,
                                         int[] coordOutput,
                                         int[] normalOutput,
                                         int[] colorOutput,
                                         int[] texCoordOutput,
                                         float[] normal)
    {
        if(ecTriangulator == null)
            ecTriangulator = new EarCutTriangulator(initSize);

        return ecTriangulator.triangulateConcavePolygon(coords,
                                                        startIndex,
                                                        numVertex,
                                                        firstNormalIndex,
                                                        firstColorIndex,
                                                        firstTexCoordIndex,
                                                        coordOutput,
                                                        normalOutput,
                                                        colorOutput,
                                                        texCoordOutput,
                                                        normal);
    }

    /**
     * Clean up the internal cache and reduce it to zero.
     */
    public void clearCachedObjects()
    {
        ecTriangulator.clearCachedObjects();
    }

    /**
     * Check to see if this vertex is a concave vertex or convex. It assumes
     * a right-handed coordinate system and counter-clockwise ordering of the
     * vertices. The coordinate array is assumed to be flat with vertex
     * values stored [ ... Xn, Yn, Zn, ...] with the index values provided
     * assuming that flattened structure (ie Pi = n, Pi+1 = n + 3). The turn
     * direction is given by n . (a x b) <= 0 (always want right turns).
     *
     * @param coords The array to read coodinate values from
     * @param p0 The index of the previous vertex to the one in question
     * @param p The index of the vertex being tested
     * @param p1 The index after the vertex after the one in question
     * @param normal The normal to this face these vertices are a part of
     * @return true if this is a convex vertex, false for concave
     */
    public static boolean isConvexVertex(float[] coords,
                                         int p0,
                                         int p,
                                         int p1,
                                         float[] normal)
    {
        // If is concave in a right-handed system when the cross product is
        // positive. Negative means it is concave
        float x1 = coords[p] - coords[p0];
        float y1 = coords[p + 1] - coords[p0 + 1];
        float z1 = coords[p + 2] - coords[p0 + 2];

        float x2 = coords[p1] - coords[p];
        float y2 = coords[p1 + 1] - coords[p + 1];
        float z2 = coords[p1 + 2] - coords[p + 2];

        float cross_x = y1 * z2 - z1 * y2;
        float cross_y = z1 * x2 - x1 * z2;
        float cross_z = x1 * y2 - y1 * x2;

        // now the dot product with the face normal
        float dot = cross_x * normal[0] +
                    cross_y * normal[1] +
                    cross_z * normal[2];

        return dot >= 0;
    }
}