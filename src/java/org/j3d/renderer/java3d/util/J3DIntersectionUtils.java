/*****************************************************************************
 *                           J3D.org Copyright (c) 2000
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.util;

// Standard imports
import javax.media.j3d.*;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

// Application specific imports
import org.j3d.geom.IntersectionUtils;
import org.j3d.geom.GeometryData;
import org.j3d.util.UserSupplementData;

/**
 * An extension of the basic {@link org.j3d.geom.IntersectionUtils} class to
 * include Java3D-specific extensions for interacting directly with
 * {@link javax.media.j3d.GeometryArray} instances.
 * <p>
 *
 * @see org.j3d.geom.IntersectionUtils
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class J3DIntersectionUtils extends IntersectionUtils
{
    /** Place to invert the incoming transform for reverse mappings */
    private Transform3D reverseTx;

    /**
     * Create a default instance of this class with no internal data
     * structures allocated.
     */
    public J3DIntersectionUtils()
    {
        reverseTx = new Transform3D();
    }

    /**
     * Convenience method to process a {@link GeometryData} and ask the
     * intersection code to find out what the real geometry type is and
     * process it appropriately. If there is an intersection, the point will
     * contain the exact intersection point on the geometry.
     * <P>
     *
     * This code will be much more efficient than the other version because
     * we do not need to reallocate internal arrays all the time or have the
     * need to set capability bits, hurting performance optimisations. If the
     * geometry array does not understand the provided geometry type, it will
     * silently ignore the request and always return false.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param data The geometry to test against
     * @param point The intersection point for returning
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayUnknownGeometry(Point3d origin,
                                      Vector3d direction,
                                      float length,
                                      GeometryData data,
                                      Transform3D vworldTransform,
                                      Point3d point,
                                      boolean intersectOnly)
    {
        boolean ret_val = false;

        reverseTx.invert(vworldTransform);
        transformPicks(reverseTx, origin, direction);

        switch(data.geometryType)
        {
            case GeometryData.TRIANGLES:
                ret_val = rayTriangleArray(pickStart,
                                           pickDir,
                                           length,
                                           data.coordinates,
                                           data.vertexCount / 3,
                                           point,
                                           intersectOnly);
                break;

            case GeometryData.QUADS:
                ret_val = rayQuadArray(pickStart,
                                       pickDir,
                                       length,
                                       data.coordinates,
                                       data.vertexCount / 4,
                                       point,
                                       intersectOnly);
                break;

            case GeometryData.TRIANGLE_STRIPS:
                ret_val = rayTriangleStripArray(pickStart,
                                                pickDir,
                                                length,
                                                data.coordinates,
                                                data.stripCounts,
                                                data.numStrips,
                                                point,
                                                intersectOnly);
                break;

            case GeometryData.TRIANGLE_FANS:
                ret_val = rayTriangleFanArray(pickStart,
                                              pickDir,
                                              length,
                                              data.coordinates,
                                              data.stripCounts,
                                              data.numStrips,
                                              point,
                                              intersectOnly);
                break;

            case GeometryData.INDEXED_QUADS:
                ret_val = rayIndexedQuadArray(pickStart,
                                              pickDir,
                                              length,
                                              data.coordinates,
                                              data.indexes,
                                              data.indexesCount,
                                              point,
                                              intersectOnly);
                break;

            case GeometryData.INDEXED_TRIANGLES:
                ret_val = rayIndexedTriangleArray(pickStart,
                                                  pickDir,
                                                  length,
                                                  data.coordinates,
                                                  data.indexes,
                                                  data.indexesCount,
                                                  point,
                                                  intersectOnly);
                break;
/*
            case GeometryData.INDEXED_TRIANGLE_STRIPS:
                ret_val = rayTriangleArray(pickStart,
                                           pickDir,
                                           length,
                                           data.coordinates,
                                           data.vertexCount,
                                           point,
                                           intersectOnly);
                break;

            case GeometryData.INDEXED_TRIANGLE_FANS:
                ret_val = rayTriangleArray(pickStart,
                                           pickDir,
                                           length,
                                           data.coordinates,
                                           data.vertexCount,
                                           point,
                                           intersectOnly);
                break;
*/
        }

        if(ret_val)
            vworldTransform.transform(point);

        return ret_val;
    }

    /**
     * Convenience method to pass in an item of geometry and ask the
     * intersection code to find out what the real geometry type is and
     * process it appropriately. If there is an intersection, the point will
     * contain the exact intersection point on the geometry.
     * <P>
     *
     * If the userData object for this geometry is an instance of
     * {@link GeometryData} we will use that in preferences to the actual
     * geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayUnknownGeometry(Point3d origin,
                                      Vector3d direction,
                                      float length,
                                      GeometryArray geom,
                                      Transform3D vworldTransform,
                                      Point3d point,
                                      boolean intersectOnly)
    {
        Object userdata = geom.getUserData();

        if(userdata instanceof GeometryData)
        {
            return rayUnknownGeometry(origin,
                                      direction,
                                      length,
                                      (GeometryData)userdata,
                                      vworldTransform,
                                      point,
                                      intersectOnly);
        }
        else if(geom instanceof TriangleArray)
        {
            return rayTriangleArray(origin,
                                    direction,
                                    length,
                                    (TriangleArray)geom,
                                    vworldTransform,
                                    point,
                                    intersectOnly);
        }
        else if(geom instanceof QuadArray)
        {
            return rayQuadArray(origin,
                                direction,
                                length,
                                (QuadArray)geom,
                                vworldTransform,
                                point,
                                intersectOnly);
        }
        else if(geom instanceof TriangleStripArray)
        {
            return rayTriangleStripArray(origin,
                                         direction,
                                         length,
                                         (TriangleStripArray)geom,
                                         vworldTransform,
                                         point,
                                         intersectOnly);
        }
        else if(geom instanceof TriangleFanArray)
        {
            return rayTriangleFanArray(origin,
                                       direction,
                                       length,
                                       (TriangleFanArray)geom,
                                       vworldTransform,
                                       point,
                                       intersectOnly);
        }
        else if(geom instanceof IndexedTriangleArray)
        {
            return rayIndexedTriangleArray(origin,
                                           direction,
                                           length,
                                           (IndexedTriangleArray)geom,
                                           vworldTransform,
                                           point,
                                           intersectOnly);
        }
        else if(geom instanceof IndexedQuadArray)
        {
            return rayIndexedQuadArray(origin,
                                       direction,
                                       length,
                                       (IndexedQuadArray)geom,
                                       vworldTransform,
                                       point,
                                       intersectOnly);
        }

        return false;
    }

    /**
     * Test the intersection of a ray or segment against the given triangle
     * array.If there is an intersection, the point will contain the exact
     * intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @return true if there was an intersection, false if not
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     */
    public boolean rayTriangleArray(Point3d origin,
                                    Vector3d direction,
                                    float length,
                                    TriangleArray geom,
                                    Transform3D vworldTransform,
                                    Point3d point,
                                    boolean intersectOnly)
    {
        if(!geom.getCapability(GeometryArray.ALLOW_COORDINATE_READ))
            throw new IllegalStateException("Not allowed to read coordinates");

        int vtx_format = geom.getVertexFormat();
        int vtx_count = geom.getVertexCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];

        if((vtx_format & GeometryArray.INTERLEAVED) != 0)
        {
            float[] interleaved_data = geom.getInterleavedVertices();
            int step = 0;
            int offset = 0;

            if((vtx_format & GeometryArray.TEXTURE_COORDINATE_2) != 0)
                step += 2 * geom.getTexCoordSetCount();
            else if((vtx_format & GeometryArray.TEXTURE_COORDINATE_3) != 0)
                step += 3 * geom.getTexCoordSetCount();
            else if ( (vtx_format & GeometryArray.TEXTURE_COORDINATE_4) != 0)
                step += 4 * geom.getTexCoordSetCount();

            if((vtx_format & GeometryArray.COLOR_3) != 0)
                step += 3;
            else if((vtx_format & GeometryArray.COLOR_4) != 0)
                step += 4;

            if((vtx_format & GeometryArray.NORMALS) != 0 )
                step += 3;

            for (int i = step; i < interleaved_data.length; i += step)
            {
                workingCoords[offset++] = interleaved_data[i++];
                workingCoords[offset++] = interleaved_data[i++];
                workingCoords[offset++] = interleaved_data[i++];
            }
        }
        else
        {
            // Get non-interleaved coords
            geom.getCoordinates(0, workingCoords);
        }

        reverseTx.invert(vworldTransform);

        transformPicks(reverseTx, origin, direction);
        boolean intersection;

        intersection = rayTriangleArray(pickStart,
                                        pickDir,
                                        length,
                                        workingCoords,
                                        vtx_count / 3,
                                        point,
                                        intersectOnly);

        if(intersection)
            vworldTransform.transform(point);

        return intersection;
    }

    /**
     * Test the intersection of a ray or segment against the given quad
     * array.If there is an intersection, the point will contain the exact
     * intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayQuadArray(Point3d origin,
                                Vector3d direction,
                                float length,
                                QuadArray geom,
                                Transform3D vworldTransform,
                                Point3d point,
                                boolean intersectOnly)
    {
        if(!geom.getCapability(GeometryArray.ALLOW_COORDINATE_READ))
            throw new IllegalStateException("Not allowed to read coordinates");

        int vtx_format = geom.getVertexFormat();
        int vtx_count = geom.getVertexCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 4];

        if((vtx_format & GeometryArray.INTERLEAVED) != 0)
        {
            float[] interleaved_data = geom.getInterleavedVertices();
            int step = 0;
            int offset = 0;

            if((vtx_format & GeometryArray.TEXTURE_COORDINATE_2) != 0)
                step += 2 * geom.getTexCoordSetCount();
            else if((vtx_format & GeometryArray.TEXTURE_COORDINATE_3) != 0)
                step += 3 * geom.getTexCoordSetCount();
            else if ( (vtx_format & GeometryArray.TEXTURE_COORDINATE_4) != 0)
                step += 4 * geom.getTexCoordSetCount();

            if((vtx_format & GeometryArray.COLOR_3) != 0)
                step += 3;
            else if((vtx_format & GeometryArray.COLOR_4) != 0)
                step += 4;

            if((vtx_format & GeometryArray.NORMALS) != 0 )
                step += 3;

            for (int i = step; i < interleaved_data.length; i += step)
            {
                workingCoords[offset++] = interleaved_data[i++];
                workingCoords[offset++] = interleaved_data[i++];
                workingCoords[offset++] = interleaved_data[i++];
                workingCoords[offset++] = interleaved_data[i++];
            }
        }
        else
        {
            // Get non-interleaved coords
            geom.getCoordinates(0, workingCoords);
        }

        reverseTx.invert(vworldTransform);

        transformPicks(reverseTx, origin, direction);
        boolean intersection;

        intersection = rayTriangleArray(pickStart,
                                        pickDir,
                                        length,
                                        workingCoords,
                                        vtx_count / 4,
                                        point,
                                        intersectOnly);

        if(intersection)
            vworldTransform.transform(point);

        return intersection;
    }

    /**
     * Test the intersection of a ray or segment against the given triangle
     * strip array.If there is an intersection, the point will contain the
     * exact intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayTriangleStripArray(Point3d origin,
                                         Vector3d direction,
                                         float length,
                                         TriangleStripArray geom,
                                         Transform3D vworldTransform,
                                         Point3d point,
                                         boolean intersectOnly)
    {
        if(!geom.getCapability(GeometryArray.ALLOW_COORDINATE_READ))
            throw new IllegalStateException("Not allowed to read coordinates");

        int vtx_format = geom.getVertexFormat();

        if((vtx_format & GeometryArray.INTERLEAVED) != 0)
            throw new IllegalArgumentException("We can't handle interleaved geometry yet");


        int vtx_count = geom.getVertexCount();
        int strip_count = geom.getNumStrips();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];

        if((workingStrips == null) || (workingStrips.length != strip_count))
            workingStrips = new int[strip_count];

        geom.getCoordinates(0, workingCoords);
        geom.getStripVertexCounts(workingStrips);

        reverseTx.invert(vworldTransform);

        transformPicks(reverseTx, origin, direction);
        boolean intersection;

        intersection = rayTriangleStripArray(pickStart,
                                             pickDir,
                                             length,
                                             workingCoords,
                                             workingStrips,
                                             strip_count,
                                             point,
                                             intersectOnly);

        if(intersection && !intersectOnly)
            vworldTransform.transform(point);

        return intersection;
    }

    /**
     * Test the intersection of a ray or segment against the given triangle
     * fan array.If there is an intersection, the point will contain the
     * exact intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayTriangleFanArray(Point3d origin,
                                       Vector3d direction,
                                       float length,
                                       TriangleFanArray geom,
                                       Transform3D vworldTransform,
                                       Point3d point,
                                       boolean intersectOnly)
    {
        if(!geom.getCapability(GeometryArray.ALLOW_COORDINATE_READ))
            throw new IllegalStateException("Not allowed to read coordinates");

        int vtx_format = geom.getVertexFormat();

        if((vtx_format & GeometryArray.INTERLEAVED) != 0)
            throw new IllegalArgumentException("We can't handle interleaved geometry yet");


        int vtx_count = geom.getVertexCount();
        int strip_count = geom.getNumStrips();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];

        if((workingStrips == null) || (workingStrips.length != strip_count))
            workingStrips = new int[strip_count];

        geom.getCoordinates(0, workingCoords);
        geom.getStripVertexCounts(workingStrips);

        reverseTx.invert(vworldTransform);

        transformPicks(reverseTx, origin, direction);
        boolean intersection;

        intersection = rayTriangleFanArray(pickStart,
                                           pickDir,
                                           length,
                                           workingCoords,
                                           workingStrips,
                                           strip_count,
                                           point,
                                           intersectOnly);

        if(intersection && !intersectOnly)
            vworldTransform.transform(point);

        return intersection;

    }

    /**
     * Test the intersection of a ray or segment against the given indexed
     * triangle array.If there is an intersection, the point will contain the
     * exact intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayIndexedTriangleArray(Point3d origin,
                                           Vector3d direction,
                                           float length,
                                           IndexedTriangleArray geom,
                                           Transform3D vworldTransform,
                                           Point3d point,
                                           boolean intersectOnly)
    {
        if(!geom.getCapability(GeometryArray.ALLOW_COORDINATE_READ))
            throw new IllegalStateException("Not allowed to read coordinates");

        if(!geom.getCapability(IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ))
            throw new IllegalStateException("Not allowed to read indexes");

        int vtx_format = geom.getVertexFormat();

        if((vtx_format & GeometryArray.INTERLEAVED) != 0)
            throw new IllegalArgumentException("We can't handle interleaved geometry yet");


        int vtx_count = geom.getVertexCount();
        int index_count = geom.getIndexCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];

        if((workingIndicies == null) || (workingIndicies.length != index_count))
            workingIndicies = new int[index_count];

        geom.getCoordinates(0, workingCoords);
        geom.getCoordinateIndices(0, workingIndicies);

        reverseTx.invert(vworldTransform);

        transformPicks(reverseTx, origin, direction);
        boolean intersection;

        intersection = rayIndexedTriangleArray(pickStart,
                                               pickDir,
                                               length,
                                               workingCoords,
                                               workingIndicies,
                                               index_count,
                                               point,
                                               intersectOnly);

        if(intersection && !intersectOnly)
            vworldTransform.transform(point);

        return intersection;

    }

    /**
     * Test the intersection of a ray or segment against the given indexed
     * quad array.If there is an intersection, the point will contain the
     * exact intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @param intersectOnly true if we only want to know if we have a
     *    intersection and don't really care which it is
     * @return true if there was an intersection, false if not
     */
    public boolean rayIndexedQuadArray(Point3d origin,
                                       Vector3d direction,
                                       float length,
                                       IndexedQuadArray geom,
                                       Transform3D vworldTransform,
                                       Point3d point,
                                       boolean intersectOnly)
    {
        if(!geom.getCapability(GeometryArray.ALLOW_COORDINATE_READ))
            throw new IllegalStateException("Not allowed to read coordinates");

        if(!geom.getCapability(IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ))
            throw new IllegalStateException("Not allowed to read indexes");

        int vtx_format = geom.getVertexFormat();

        if((vtx_format & GeometryArray.INTERLEAVED) != 0)
            throw new IllegalArgumentException("We can't handle interleaved geometry yet");


        int vtx_count = geom.getVertexCount();
        int index_count = geom.getIndexCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];

        if((workingIndicies == null) || (workingIndicies.length != index_count))
            workingIndicies = new int[index_count];

        geom.getCoordinates(0, workingCoords);
        geom.getCoordinateIndices(0, workingIndicies);

        reverseTx.invert(vworldTransform);

        transformPicks(reverseTx, origin, direction);
        boolean intersection;

        intersection = rayIndexedQuadArray(pickStart,
                                           pickDir,
                                           length,
                                           workingCoords,
                                           workingIndicies,
                                           index_count,
                                           point,
                                           intersectOnly);

        if(intersection && !intersectOnly)
            vworldTransform.transform(point);

        return intersection;
    }

    //----------------------------------------------------------
    // Lower level methods for individual polygons
    //----------------------------------------------------------

    /**
     * Convenience method to transform the picking coordinates to the local
     * coordinates of the geometry. Takes the coordinates and stores them in
     * the picking variables used internally
     */
    private void transformPicks(Transform3D vw, Point3d start, Vector3d dir)
    {
        vw.transform(start, pickStart);
        vw.transform(dir, pickDir);
    }
}
