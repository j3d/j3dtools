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
 * @version $Revision: 1.4 $
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
            case GeometryData.TRIANGLES:
                unindexedTriangles(data);
                break;
            case GeometryData.QUADS:
                unindexedQuads(data);
                break;
            case GeometryData.TRIANGLE_STRIPS:
                triangleStrips(data);
                break;
//            case GeometryData.TRIANGLE_FANS:
//                triangleFans(data);
//                break;
            case GeometryData.INDEXED_QUADS:
                indexedQuads(data);
                break;
            case GeometryData.INDEXED_TRIANGLES:
                indexedTriangles(data);
                break;
            case GeometryData.INDEXED_TRIANGLE_STRIPS:
                indexedTriangleStrips(data);
                break;
            case GeometryData.INDEXED_TRIANGLE_FANS:
                indexedTriangleFans(data);
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
    private void unindexedTriangles(GeometryData data)
        throws InvalidArraySizeException
    {
        generateUnindexedTriCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateUnindexedTriNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateUnindexedTriTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateUnindexedTriTexture3D(data);
    }


    /**
     * Generate a new set of points for an unindexed quad array
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void unindexedQuads(GeometryData data)
        throws InvalidArraySizeException
    {
        generateUnindexedQuadCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateUnindexedQuadNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateUnindexedQuadTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateUnindexedQuadTexture3D(data);
    }

    /**
     * Generate a new set of points for an indexed quad array. Uses the same
     * points as an indexed triangle, but repeats the top coordinate index.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void indexedQuads(GeometryData data)
        throws InvalidArraySizeException
    {
        generateIndexedCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateIndexedTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateIndexedTexture3D(data);

        // now let's do the index list
        int index_size = (facetCount * facetCount) * 4;

        if(data.indexes == null)
            data.indexes = new int[index_size];
        else if(data.indexes.length < index_size)
            throw new InvalidArraySizeException("Coordinates",
                                                data.indexes.length,
                                                index_size);

        int[] indexes = data.indexes;
        data.indexesCount = index_size;
        int idx = 0;
        int vtx = 0;

        // each face consists of an anti-clockwise
        for(int i = (facetCount * facetCount); --i >= 0; )
        {
            indexes[idx++] = vtx;
            indexes[idx++] = vtx + facetCount + 1;
            indexes[idx++] = vtx + facetCount + 2;
            indexes[idx++] = vtx + 1;

            vtx++;

            if((i % facetCount) == 0)
                vtx++;
        }
    }

    /**
     * Generate a new set of points for an indexed triangle array
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void indexedTriangles(GeometryData data)
        throws InvalidArraySizeException
    {
        generateIndexedCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateIndexedTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateIndexedTexture3D(data);

        // now let's do the index list
        int index_size = (facetCount * facetCount) * 6;

        if(data.indexes == null)
            data.indexes = new int[index_size];
        else if(data.indexes.length < index_size)
            throw new InvalidArraySizeException("Coordinates",
                                                data.indexes.length,
                                                index_size);

        int[] indexes = data.indexes;
        data.indexesCount = index_size;
        int idx = 0;
        int vtx = 0;

        // each face consists of an anti-clockwise triangle
        for(int i = (facetCount * facetCount); --i >= 0; )
        {
            // triangle 1
            indexes[idx++] = vtx;
            indexes[idx++] = vtx + facetCount + 2;
            indexes[idx++] = vtx + 1;

            // triangle 2
            indexes[idx++] = vtx + facetCount + 1;
            indexes[idx++] = vtx + facetCount + 2;
            indexes[idx++] = vtx;

            vtx++;

            if((i % facetCount) == 0)
                vtx++;
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
    private void triangleStrips(GeometryData data)
        throws InvalidArraySizeException
    {
        generateUnindexedTriStripCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateUnindexedTriStripNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateUnindexedTriStripTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateUnindexedTriTexture3D(data);

        int num_strips = facetCount;

        if(data.stripCounts == null)
            data.stripCounts = new int[num_strips];
        else if(data.stripCounts.length < num_strips)
            throw new InvalidArraySizeException("Strip counts",
                                                data.stripCounts.length,
                                                num_strips);

        for(int i = num_strips; --i >= 0; )
            data.stripCounts[i] = (facetCount + 1) * 2;
    }

    /**
     * Generate a new set of points for a triangle fan array. Each facet on the
     * side of the cone is a single fan, but the patch is one big fan.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void triangleFans(GeometryData data)
        throws InvalidArraySizeException
    {
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
    private void indexedTriangleStrips(GeometryData data)
        throws InvalidArraySizeException
    {
          generateIndexedCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateIndexedTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateIndexedTexture3D(data);

        // now let's do the index list
        int index_size = (facetCount + 1) * facetCount * 2;
        int num_strips = facetCount;

        if(data.indexes == null)
            data.indexes = new int[index_size];
        else if(data.indexes.length < index_size)
            throw new InvalidArraySizeException("Indexes",
                                                data.indexes.length,
                                                index_size);

        if(data.stripCounts == null)
            data.stripCounts = new int[num_strips];
        else if(data.stripCounts.length < num_strips)
            throw new InvalidArraySizeException("Strip counts",
                                                data.stripCounts.length,
                                                num_strips);

        int[] indexes = data.indexes;
        int[] stripCounts = data.stripCounts;
        data.indexesCount = index_size;
        data.numStrips = num_strips;
        int idx = 0;
        int vtx = 0;
        int total_points = (facetCount + 1) * facetCount;

        // The side is one big strip
        for(int i = total_points; --i >= 0; )
        {
            indexes[idx++] = vtx;
            indexes[idx++] = vtx + (facetCount + 1);

            vtx++;
        }

        for(int i = num_strips; --i >= 0; )
            stripCounts[i] = (facetCount + 1) * 2;
  }

    /**
     * Generate a new set of points for an indexed triangle fan array. We
     * build the strip from the existing points, and there's no need to
     * re-order the points for the indexes this time. As for the simple fan,
     * we use the first index, the lower-right corner as the apex for the fan.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void indexedTriangleFans(GeometryData data)
        throws InvalidArraySizeException
    {
        generateIndexedCoordinates(data);

        if((data.geometryComponents & GeometryData.NORMAL_DATA) != 0)
            generateIndexedNormals(data);

        if((data.geometryComponents & GeometryData.TEXTURE_2D_DATA) != 0)
            generateIndexedTexture2D(data);
        else if((data.geometryComponents & GeometryData.TEXTURE_3D_DATA) != 0)
            generateIndexedTexture3D(data);

        // now let's do the index list
        int index_size = (facetCount * facetCount) * 4;
        int num_strips = facetCount * facetCount;

        if(data.indexes == null)
            data.indexes = new int[index_size];
        else if(data.indexes.length < index_size)
            throw new InvalidArraySizeException("Indexes",
                                                data.indexes.length,
                                                index_size);

        if(data.stripCounts == null)
            data.stripCounts = new int[num_strips];
        else if(data.stripCounts.length < num_strips)
            throw new InvalidArraySizeException("Strip counts",
                                                data.stripCounts.length,
                                                num_strips);

        int[] indexes = data.indexes;
        int[] stripCounts = data.stripCounts;
        data.indexesCount = index_size;
        data.numStrips = num_strips;
        int idx = 0;
        int vtx = 0;

        // each face consists of an anti-clockwise quad
        for(int i = (facetCount * facetCount); --i >= 0; )
        {
            indexes[idx++] = vtx + facetCount + 1;
            indexes[idx++] = vtx + facetCount + 2;
            indexes[idx++] = vtx + 1;
            indexes[idx++] = vtx;

            stripCounts[i] = 4;

            vtx++;

            if((i % facetCount) == 0)
                vtx++;
        }
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
    private void generateUnindexedTriCoordinates(GeometryData data)
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

        regeneratePatch();

        // now just build a grid of coordinates
        int cnt;
        int vtx = 0;
        for(int i = 0; i < facetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < facetCount; j++)
            {
                coords[vtx++] = patchCoordinates[i][cnt + 3];
                coords[vtx++] = patchCoordinates[i][cnt + 4];
                coords[vtx++] = patchCoordinates[i][cnt + 5];

                coords[vtx++] = patchCoordinates[i][cnt];
                coords[vtx++] = patchCoordinates[i][cnt + 1];
                coords[vtx++] = patchCoordinates[i][cnt + 2];

                coords[vtx++] = patchCoordinates[i + 1][cnt];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 1];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 2];

                // Now the second triangle for the upper half
                coords[vtx++] = patchCoordinates[i + 1][cnt];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 1];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 2];

                coords[vtx++] = patchCoordinates[i + 1][cnt + 3];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 4];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 5];

                coords[vtx++] = patchCoordinates[i][cnt + 3];
                coords[vtx++] = patchCoordinates[i][cnt + 4];
                coords[vtx++] = patchCoordinates[i][cnt + 5];

                cnt += 3;
            }
        }
    }

    /**
     * Generates new set of points suitable for use in an unindexed array. Each
     * patch coordinate will appear twice in this list. The first half of the
     * array is the top, the second half, the bottom.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedQuadCoordinates(GeometryData data)
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

        regeneratePatch();

        // now just build a grid of coordinates
        int cnt;
        int vtx = 0;
        for(int i = 0; i < facetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < facetCount; j++)
            {
                coords[vtx++] = patchCoordinates[i][cnt + 3];
                coords[vtx++] = patchCoordinates[i][cnt + 4];
                coords[vtx++] = patchCoordinates[i][cnt + 5];

                coords[vtx++] = patchCoordinates[i][cnt];
                coords[vtx++] = patchCoordinates[i][cnt + 1];
                coords[vtx++] = patchCoordinates[i][cnt + 2];

                coords[vtx++] = patchCoordinates[i + 1][cnt];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 1];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 2];

                coords[vtx++] = patchCoordinates[i + 1][cnt + 3];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 4];
                coords[vtx++] = patchCoordinates[i + 1][cnt + 5];

                cnt += 3;
            }
        }
    }

    /**
     * Generates new set of points suitable for use in an unindexed array. Each
     * patch coordinate will appear twice in this list. The first half of the
     * array is the top, the second half, the bottom.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriStripCoordinates(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = getVertexCount(data);

        if(data.coordinates == null)
            data.coordinates = new float[vtx_cnt * 3];
        else if(data.coordinates.length < vtx_cnt * 3)
            throw new InvalidArraySizeException("Coordinates",
                                                data.coordinates.length,
                                                vtx_cnt * 3);


        regeneratePatch();

        float[] coords = data.coordinates;
        data.vertexCount = vtx_cnt;

        int i, j;
        int count = 0;
        int base_count = 0;

        // Start of with one less row (width) here because we don't have two
        // sets of coordinates for those.
        for(i = 0; i < facetCount; i++)
        {
            base_count = 0;

            for(j = 0; j < facetCount + 1; j++)
            {
                coords[count++] = patchCoordinates[i][base_count];
                coords[count++] = patchCoordinates[i][base_count + 1];
                coords[count++] = patchCoordinates[i][base_count + 2];

                coords[count++] = patchCoordinates[i + 1][base_count];
                coords[count++] = patchCoordinates[i + 1][base_count + 1];
                coords[count++] = patchCoordinates[i + 1][base_count + 2];

                base_count += 3;
            }
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
    private void generateIndexedCoordinates(GeometryData data)
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

        regeneratePatch();

        int offset = 0;

        for(int i = 0; i <= facetCount; i++)
        {
            System.arraycopy(patchCoordinates[i],
                             0,
                             coords,
                             offset,
                             numPatchValues);
            offset += numPatchValues;
        }
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
    private void generateUnindexedTriNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);

        regenerateNormals();

        float[] normals = data.normals;
        int cnt;
        int vtx = 0;
        for(int i = 0; i < facetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < facetCount; j++)
            {
                normals[vtx++] = patchNormals[i][cnt + 3];
                normals[vtx++] = patchNormals[i][cnt + 4];
                normals[vtx++] = patchNormals[i][cnt + 5];

                normals[vtx++] = patchNormals[i][cnt];
                normals[vtx++] = patchNormals[i][cnt + 1];
                normals[vtx++] = patchNormals[i][cnt + 2];

                normals[vtx++] = patchNormals[i + 1][cnt];
                normals[vtx++] = patchNormals[i + 1][cnt + 1];
                normals[vtx++] = patchNormals[i + 1][cnt + 2];

                // Now the second triangle for the upper half
                normals[vtx++] = patchNormals[i + 1][cnt];
                normals[vtx++] = patchNormals[i + 1][cnt + 1];
                normals[vtx++] = patchNormals[i + 1][cnt + 2];

                normals[vtx++] = patchNormals[i + 1][cnt + 3];
                normals[vtx++] = patchNormals[i + 1][cnt + 4];
                normals[vtx++] = patchNormals[i + 1][cnt + 5];

                normals[vtx++] = patchNormals[i][cnt + 3];
                normals[vtx++] = patchNormals[i][cnt + 4];
                normals[vtx++] = patchNormals[i][cnt + 5];

                cnt += 3;
            }
        }
    }

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
    private void generateUnindexedQuadNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);

        regenerateNormals();

        // now just build a grid of coordinates
        float[] normals = data.normals;
        int cnt;
        int vtx = 0;
        for(int i = 0; i < facetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < facetCount; j++)
            {
                normals[vtx++] = patchNormals[i][cnt + 3];
                normals[vtx++] = patchNormals[i][cnt + 4];
                normals[vtx++] = patchNormals[i][cnt + 5];

                normals[vtx++] = patchNormals[i][cnt];
                normals[vtx++] = patchNormals[i][cnt + 1];
                normals[vtx++] = patchNormals[i][cnt + 2];

                normals[vtx++] = patchNormals[i + 1][cnt];
                normals[vtx++] = patchNormals[i + 1][cnt + 1];
                normals[vtx++] = patchNormals[i + 1][cnt + 2];

                normals[vtx++] = patchNormals[i + 1][cnt + 3];
                normals[vtx++] = patchNormals[i + 1][cnt + 4];
                normals[vtx++] = patchNormals[i + 1][cnt + 5];

                cnt += 3;
            }
        }
    }

    /**
     * Generate a new set of normals for unindexed points in a triangle strip.
     * Smooth normals are used for all.
     * <p>
     * This must always be called after the coordinate generation. The
     * top normal of the cone is always perpendicular to the face.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateUnindexedTriStripNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = facetCount * (facetCount + 1) * 6;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);

        regenerateNormals();

        int i, j;
        float[] normals = data.normals;
        int count = 0;
        int base_count = 0;

        // Start of with one less row (width) here because we don't have two
        // sets of coordinates for those.
        for(i = 0; i < facetCount; i++)
        {
            base_count = 0;

            for(j = 0; j < facetCount + 1; j++)
            {
                normals[count++] = patchNormals[i][base_count];
                normals[count++] = patchNormals[i][base_count + 1];
                normals[count++] = patchNormals[i][base_count + 2];

                normals[count++] = patchNormals[i + 1][base_count];
                normals[count++] = patchNormals[i + 1][base_count + 1];
                normals[count++] = patchNormals[i + 1][base_count + 2];

                base_count += 3;
            }
        }
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
    private void generateIndexedNormals(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.normals == null)
            data.normals = new float[vtx_cnt];
        else if(data.normals.length < vtx_cnt)
            throw new InvalidArraySizeException("Normals",
                                                data.normals.length,
                                                vtx_cnt);

        // Just copy the values straight into the array.
        regenerateNormals();

        float[] normals = data.normals;
        int offset = 0;

        for(int i = 0; i <= facetCount; i++)
        {
            System.arraycopy(patchNormals[i],
                             0,
                             normals,
                             offset,
                             numNormalValues);
            offset += numNormalValues;
        }
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
    private void generateUnindexedTriTexture2D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 2;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("2D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        regenerateTexcoords();

        float[] tex_coords = data.textureCoordinates;
        int cnt;
        int vtx = 0;

        for(int i = 0; i < facetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < facetCount; j++)
            {
                tex_coords[vtx++] = patchTexcoords[i][cnt + 2];
                tex_coords[vtx++] = patchTexcoords[i][cnt + 3];

                tex_coords[vtx++] = patchTexcoords[i][cnt];
                tex_coords[vtx++] = patchTexcoords[i][cnt + 1];

                tex_coords[vtx++] = patchTexcoords[i + 1][cnt];
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 1];

                // Now the second triangle for the upper half
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt];
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 1];

                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 2];
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 3];

                tex_coords[vtx++] = patchTexcoords[i][cnt + 2];
                tex_coords[vtx++] = patchTexcoords[i][cnt + 3];

                cnt += 2;
            }
        }
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
    private void generateUnindexedQuadTexture2D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 2;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("2D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        regenerateTexcoords();

        // now just build a grid of coordinates
        float[] tex_coords = data.textureCoordinates;
        int cnt;
        int vtx = 0;
        for(int i = 0; i < facetCount; i++)
        {
            cnt = 0;
            for(int j = 0; j < facetCount; j++)
            {
                tex_coords[vtx++] = patchTexcoords[i][cnt + 2];
                tex_coords[vtx++] = patchTexcoords[i][cnt + 3];

                tex_coords[vtx++] = patchTexcoords[i][cnt];
                tex_coords[vtx++] = patchTexcoords[i][cnt + 1];

                tex_coords[vtx++] = patchTexcoords[i + 1][cnt];
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 1];

                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 2];
                tex_coords[vtx++] = patchTexcoords[i + 1][cnt + 3];

                cnt += 2;
            }
        }
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
    private void generateUnindexedTriStripTexture2D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = facetCount * (facetCount + 1) * 4;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("2D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        regenerateTexcoords();

        int i, j;
        float[] texcoords = data.textureCoordinates;
        int count = 0;
        int base_count = 0;

        // Start of with one less row (width) here because we don't have two
        // sets of coordinates for those.
        for(i = 0; i < facetCount; i++)
        {
            base_count = 0;

            for(j = 0; j < facetCount + 1; j++)
            {
                texcoords[count++] = patchTexcoords[i][base_count];
                texcoords[count++] = patchTexcoords[i][base_count + 1];

                texcoords[count++] = patchTexcoords[i + 1][base_count];
                texcoords[count++] = patchTexcoords[i + 1][base_count + 1];

                base_count += 2;
            }
        }
    }

    /**
     * Generate a new set of texCoords for a texCoord set of indexed points.
     * This must always be called after the coordinate generation. The
     * top texCoord of the cone is always perpendicular to the face.
     *
     * @param data The data to patch the calculations on
     * @throws InvalidArraySizeException The array is not big enough to contain
     *   the requested geometry
     */
    private void generateIndexedTexture2D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 2;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("Texcoords",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        // Just copy the values straight into the array.
        regenerateTexcoords();

        float[] tex_coords = data.textureCoordinates;
        int offset = 0;

        for(int i = 0; i <= facetCount; i++)
        {
            System.arraycopy(patchTexcoords[i],
                             0,
                             tex_coords,
                             offset,
                             numTexcoordValues);
            offset += numTexcoordValues;
        }
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
    private void generateUnindexedTriTexture3D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("3D Texture coordinates",
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
    private void generateUnindexedQuadTexture3D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("3D Texture coordinates",
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
    private void generateIndexedTexture3D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = data.vertexCount * 3;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("3D Texture coordinates",
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
    private void generateUnindexedTriStripTexture3D(GeometryData data)
        throws InvalidArraySizeException
    {
        int vtx_cnt = facetCount * (facetCount + 1) * 6;

        if(data.textureCoordinates == null)
            data.textureCoordinates = new float[vtx_cnt];
        else if(data.textureCoordinates.length < vtx_cnt)
            throw new InvalidArraySizeException("2D Texture coordinates",
                                                data.textureCoordinates.length,
                                                vtx_cnt);

        regenerateTexcoords();

        int i, j;
        float[] texcoords = data.textureCoordinates;
        int count = 0;
        int base_count = 0;

        // Start of with one less row (width) here because we don't have two
        // sets of coordinates for those.
        for(i = 0; i < facetCount; i++)
        {
            base_count = 0;

            for(j = 0; j < facetCount + 1; j++)
            {
                texcoords[count++] = patchTexcoords[i][base_count];
                texcoords[count++] = patchTexcoords[i][base_count + 1];
                texcoords[count++] = patchTexcoords[i][base_count + 2];

                texcoords[count++] = patchTexcoords[i + 1][base_count];
                texcoords[count++] = patchTexcoords[i + 1][base_count + 1];
                texcoords[count++] = patchTexcoords[i + 1][base_count + 2];

                base_count += 3;
            }
        }
    }

    /**
     * Regenerate the patch coordinate points. These are the flat circle that
     * makes up the patch of the code. The coordinates are generated patchd on
     * the 2 PI divided by the number of facets to generate.
     */
    private final void regeneratePatch()
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
            controlPointCoordinates[numWidthControlPoints - 1][ncp - 2];;
        patchCoordinates[facetCount][cnt++] =
            controlPointCoordinates[numWidthControlPoints - 1][ncp - 1];;
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