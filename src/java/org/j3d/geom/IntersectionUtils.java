/*****************************************************************************
 *                           J3D.org Copyright (c) 2000
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
import javax.media.j3d.*;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

// Application specific imports
// none

/**
 * A collection of utility methods to do geometry intersection tests
 * <p>
 *
 * The design of the implementation is focused towards realtime intersection
 * requirements for collision detection and terrain following. We avoid the
 * standard pattern of making the methods static because we believe that you
 * may need multiple copies of this class floating around. Internally it will
 * also seek to reduce the amount of garbage generated by allocating arrays of
 * data and then maintaining those arrays between calls. Arrays are only
 * resized if they need to get bigger. Smaller data than the currently
 * allocated structures will use the existing data. For the same reason, we
 * do not synchronise any of the methods. If you expect to have multiple
 * threads needing to do intersection testing, we suggest you have separate
 * copies of this class as no results are guaranteed if you are accessing this
 * instance with multiple threads.
 * <p>
 *
 * Calculation of the values works by configuring the class for the sort of
 * data that you want returned. For the higher level methods that allow you
 * <p>
 *
 * The ray/polygon intersection test is a combination test. Firstly it will
 * check for the segment intersection if requested. Then, for an infinite ray
 * or an intersecting segment, we use the algorithm defined from the Siggraph
 * paper in their education course:
 * <a href="http://www.education.siggraph.org/materials/HyperGraph/raytrace/raypolygon_intersection.htm">
 * http://www.education.siggraph.org/materials/HyperGraph/raytrace/raypolygon_intersection.htm
 * </a>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class IntersectionUtils
{
    /** A point that we use for working calculations (coord transforms) */
    private Point3d wkPoint;
    private Vector3d wkVec;

    /** Working vectors */
    private Vector3d v0;
    private Vector3d v1;
    private Vector3d normal;
    private Vector3d diffVec;

    /** The current coordinate list that we work from */
    private float[] workingCoords;
    private int[] workingStrips;
    private int[] workingIndexes;

    /** The current 2D coordinate list that we work from */
    private float[] working2dCoords;

    /** Working places for a single quad */
    private float[] wkPolygon;

    /** Place to invert the incoming transform for reverse mappings */
    private Transform3D reverseTx;

    /**
     * Create a default instance of this class with no internal data
     * structures allocated.
     */
    public IntersectionUtils()
    {
        wkPoint = new Point3d();
        wkVec = new Vector3d();
        v0 = new Vector3d();
        v1 = new Vector3d();
        normal = new Vector3d();
        diffVec = new Vector3d();
        wkPolygon = new float[12];
        reverseTx = new Transform3D();
    }

    /**
     * Clear the current internal structures to reduce the amount of memory
     * used. It is recommended you use this method with caution as then next
     * time a user calls this class, all the internal structures will be
     * reallocated. If this is running in a realtime environment, that could
     * be very costly - both allocation and the garbage collection that
     * results from calling this method
     */
    public void clear()
    {
        workingCoords = null;
        working2dCoords = null;
    }

    /**
     * Convenience method to pass in an item of geometry and ask the
     * intersection code to find out what the real geometry type is and
     * process it appropriately. If there is an intersection, the point will
     * contain the exact intersection point on the geometry.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param geom The geometry to test against
     * @param point The intersection point for returning
     * @return true if there was an intersection, false if not
     */
    public boolean rayUnknownGeometry(Point3d origin,
                                      Vector3d direction,
                                      float length,
                                      GeometryArray geom,
                                      Transform3D vworldTransform,
                                      Point3d point)
    {
        if(geom instanceof TriangleArray)
        {
            return rayTriangleArray(origin,
                                    direction,
                                    length,
                                    (TriangleArray)geom,
                                    vworldTransform,
                                    point);
        }
        else if(geom instanceof QuadArray)
        {
            return rayQuadArray(origin,
                                direction,
                                length,
                                (QuadArray)geom,
                                vworldTransform,
                                point);
        }
        else if(geom instanceof TriangleStripArray)
        {
            return rayTriangleStripArray(origin,
                                         direction,
                                         length,
                                         (TriangleStripArray)geom,
                                         vworldTransform,
                                         point);
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
     */
    public boolean rayTriangleArray(Point3d origin,
                                    Vector3d direction,
                                    float length,
                                    TriangleArray geom,
                                    Transform3D vworldTransform,
                                    Point3d point)
    {
        if(!geom.getCapability(GeometryArray.ALLOW_COORDINATE_READ))
            throw new IllegalStateException("Not allowed to read coordinates");

        int vtx_format = geom.getVertexFormat();

        if((vtx_format & GeometryArray.INTERLEAVED) != 0)
            throw new IllegalArgumentException("We can't handle interleaved geometry yet");


        int vtx_count = geom.getVertexCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];


        geom.getCoordinates(0, workingCoords);

        transformCoords(vtx_count, vworldTransform);

        return rayTriangleArray(origin,
                                direction,
                                length,
                                workingCoords,
                                vtx_count / 3,
                                point);
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
     * @return true if there was an intersection, false if not
     */
    public boolean rayQuadArray(Point3d origin,
                                Vector3d direction,
                                float length,
                                QuadArray geom,
                                Transform3D vworldTransform,
                                Point3d point)
    {
        if(!geom.getCapability(GeometryArray.ALLOW_COORDINATE_READ))
            throw new IllegalStateException("Not allowed to read coordinates");

        int vtx_format = geom.getVertexFormat();

        if((vtx_format & GeometryArray.INTERLEAVED) != 0)
            throw new IllegalArgumentException("We can't handle interleaved geometry yet");


        int vtx_count = geom.getVertexCount();

        if((workingCoords == null) || (workingCoords.length != vtx_count * 3))
            workingCoords = new float[vtx_count * 3];

        geom.getCoordinates(0, workingCoords);

        transformCoords(vtx_count, vworldTransform);

        return rayQuadArray(origin,
                            direction,
                            length,
                            workingCoords,
                            vtx_count / 4,
                            point);
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
     * @return true if there was an intersection, false if not
     */
    public boolean rayTriangleStripArray(Point3d origin,
                                         Vector3d direction,
                                         float length,
                                         TriangleStripArray geom,
                                         Transform3D vworldTransform,
                                         Point3d point)
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


        transformCoords(vtx_count, vworldTransform);

        boolean ret_val = rayTriangleStripArray(origin,
                                                direction,
                                                length,
                                                workingCoords,
                                                workingStrips,
                                                strip_count,
                                                point);

        return ret_val;
    }

    /**
     * Test an array of triangles for intersection. Returns the closest
     * intersection point to the origin of the picking ray. Assumes that the
     * coordinates are ordered as [Xn, Yn, Zn] and are translated into the same
     * coordinate system that the the origin and direction are from.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param coords The coordinates of the triangles
     * @param numTris The number of triangles to use from the array
     * @param point The intersection point for returning
     * @return true if there was an intersection, false if not
     */
    public boolean rayTriangleArray(Point3d origin,
                                    Vector3d direction,
                                    float length,
                                    float[] coords,
                                    int numTris,
                                    Point3d point)
    {
        if(coords.length < numTris * 9)
            throw new IllegalArgumentException("coords too small for numCoords");

        // assign the working coords to be big enough for a quadrilateral as
        // that is what we are most likely to see as the biggest item
        if(working2dCoords == null)
            working2dCoords = new float[8];

        double shortest_length = -1;

        for(int i = 0; i < numTris; i++)
        {
            System.arraycopy(coords, i * 9, wkPolygon, 0, 9);

            if(rayPolygonChecked(origin,
                                 direction,
                                 length,
                                 wkPolygon,
                                 3,
                                 wkPoint))
            {
                diffVec.sub(origin, wkPoint);

                if((shortest_length == -1) ||
                   (diffVec.length() < shortest_length))
                {
                    shortest_length = diffVec.length();
                    point.set(wkPoint);
                }
            }
        }

        return (shortest_length != -1);
    }

    /**
     * Test an array of quads for intersection. Returns the closest
     * intersection point to the origin of the picking ray. Assumes that the
     * coordinates are ordered as [Xn, Yn, Zn] and are translated into the same
     * coordinate system that the the origin and direction are from.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param coords The coordinates of the quads
     * @param numQuads The number of quads to use from the array
     * @param point The intersection point for returning
     * @return true if there was an intersection, false if not
     */
    public boolean rayQuadArray(Point3d origin,
                                Vector3d direction,
                                float length,
                                float[] coords,
                                int numQuads,
                                Point3d point)
    {
        if(coords.length < numQuads * 12)
            throw new IllegalArgumentException("coords too small for numCoords");

        // assign the working coords to be big enough for a quadrilateral as
        // that is what we are most likely to see as the biggest item
        if(working2dCoords == null)
            working2dCoords = new float[8];

        double shortest_length = -1;

        for(int i = 0; i < numQuads; i++)
        {
            System.arraycopy(coords, i * 12, wkPolygon, 0, 12);

            if(rayPolygonChecked(origin,
                                 direction,
                                 length,
                                 wkPolygon,
                                 4,
                                 wkPoint))
            {
                diffVec.sub(origin, wkPoint);

                if((shortest_length == -1) ||
                   (diffVec.length() < shortest_length))
                {
                    shortest_length = diffVec.length();
                    point.set(wkPoint);
                }
            }
        }

        return (shortest_length != -1);
    }

    /**
     * Test an array of triangles strips for intersection. Returns the closest
     * intersection point to the origin of the picking ray. Assumes that the
     * coordinates are ordered as [Xn, Yn, Zn] and are translated into the same
     * coordinate system that the the origin and direction are from.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param coords The coordinates of the triangles
     * @param stripCounts The number of polygons in each strip
     * @param numStrips The number of strips to use from the array
     * @param point The intersection point for returning
     * @return true if there was an intersection, false if not
     */
    public boolean rayTriangleStripArray(Point3d origin,
                                         Vector3d direction,
                                         float length,
                                         float[] coords,
                                         int[] stripCounts,
                                         int numStrips,
                                         Point3d point)
    {
        // Add all the strip lengths up first
        int total_coords = 0;

        for(int i = numStrips; --i >= 0; )
            total_coords += stripCounts[i];

        if(coords.length < total_coords * 3)
            throw new IllegalArgumentException("coords too small for numCoords");

        // assign the working coords to be big enough for a quadrilateral as
        // that is what we are most likely to see as the biggest item
        if(working2dCoords == null)
            working2dCoords = new float[8];

        double shortest_length = -1;
        int offset = 0;

        for(int i = 0; i < numStrips; i++)
        {
            offset = i * stripCounts[i] * 3;

            for(int j = 0; j < stripCounts[i] - 2; j++)
            {
                System.arraycopy(coords, offset + j * 3, wkPolygon, 0, 9);

                if(rayPolygonChecked(origin,
                                     direction,
                                     length,
                                     wkPolygon,
                                     3,
                                     wkPoint))
                {
                    diffVec.sub(origin, wkPoint);

                    if((shortest_length == -1) ||
                       (diffVec.length() < shortest_length))
                    {
                        shortest_length = diffVec.length();
                        point.set(wkPoint);
                    }
                }
            }
        }

        return (shortest_length != -1);
    }

    //----------------------------------------------------------
    // Lower level methods for individual polygons
    //----------------------------------------------------------

    /**
     * Test to see if the polygon intersects with the given ray. The
     * coordinates are ordered as [Xn, Yn, Zn]. The algorithm assumes that
     * the points are co-planar. If they are not, the results may not be
     * accurate. The normal is calculated based on the first 3 points of the
     * polygon. We don't do any testing for less than 3 points.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param coords The coordinates of the polygon
     * @param numCoords The number of coordinates to use from the array
     * @param point The intersection point for returning
     * @return true if there was an intersection, false if not
     */
    public boolean rayPolygon(Point3d origin,
                              Vector3d direction,
                              float length,
                              float[] coords,
                              int numCoords,
                              Point3d point)
    {
        if(coords.length < numCoords * 2)
            throw new IllegalArgumentException("coords too small for numCoords");

        if((working2dCoords == null) ||
           (working2dCoords.length < numCoords * 2))
            working2dCoords = new float[numCoords * 2];

        return rayPolygonChecked(origin,
                                 direction,
                                 length,
                                 coords,
                                 numCoords,
                                 point);
    }

    /**
     * Private version of the ray - Polygon intersection test that does not
     * do any bounds checking on arrays and assumes everything is correct.
     * Allows fast calls to this method for internal use as well as more
     * expensive calls with checks for the public interfaces.
     * <p>
     * This method does not use wkPoint.
     *
     * @param origin The origin of the ray
     * @param direction The direction of the ray
     * @param length An optional length for to make the ray a segment. If
     *   the value is zero, it is ignored
     * @param coords The coordinates of the polygon
     * @param numCoords The number of coordinates to use from the array
     * @param point The intersection point for returning
     * @return true if there was an intersection, false if not
     */
    private boolean rayPolygonChecked(Point3d origin,
                                      Vector3d direction,
                                      float length,
                                      float[] coords,
                                      int numCoords,
                                      Point3d point)
    {
        int i, j;

        v0.x = coords[3] - coords[0];
        v0.y = coords[4] - coords[1];
        v0.z = coords[5] - coords[2];

        v1.x = coords[6] - coords[3];
        v1.y = coords[7] - coords[4];
        v1.z = coords[8] - coords[5];

        normal.cross(v0, v1);

        // degenerate polygon?
        if(normal.length() == 0)
            return false;

        double n_dot_dir = normal.dot(direction);

        // ray and plane parallel?
        if(n_dot_dir == 0)
            return false;

        wkVec.x = coords[0];
        wkVec.y = coords[1];
        wkVec.z = coords[2];
        double d = normal.dot(wkVec);

        wkVec.set(origin);
        double n_dot_o = normal.dot(wkVec);

        // t = (d - N.O) / N.D
        double t = (d - n_dot_o) / n_dot_dir;

        // intersection before the origin
        if(t < 0)
            return false;

        // So we have an intersection with the plane of the polygon and the
        // segment/ray. Using the winding rule to see if inside or outside
        // First store the exact intersection point anyway, regardless of
        // whether this is an intersection or not.
        point.x = origin.x + direction.x * t;
        point.y = origin.y + direction.y * t;
        point.z = origin.z + direction.z * t;

        // Intersection point after the end of the segment?
        if((length != 0) && (origin.distance(point) > length))
            return false;

        // bounds check

        // find the dominant axis to resolve to a 2 axis system
        double abs_nrm_x = (normal.x >= 0) ? normal.x : -normal.x;
        double abs_nrm_y = (normal.y >= 0) ? normal.y : -normal.y;
        double abs_nrm_z = (normal.z >= 0) ? normal.z : -normal.z;

        int dom_axis;

        if(abs_nrm_x > abs_nrm_y)
            dom_axis = 0;
        else
            dom_axis = 1;

        if(dom_axis == 0)
        {
            if(abs_nrm_x < abs_nrm_z)
                dom_axis = 2;
        }
        else if(abs_nrm_y < abs_nrm_z)
        {
            dom_axis = 2;
        }

        // Map all the coordinates to the 2D plane. The u and v coordinates
        // are interleaved as u == even indicies and v = odd indicies

        // Steps 1 & 2 combined
        // 1. For NV vertices [Xn Yn Zn] where n = 0..Nv-1, project polygon
        // vertices [Xn Yn Zn] onto dominant coordinate plane (Un Vn).
        // 2. Translate (U, V) polygon so intersection point is origin from
        // (Un', Vn').
        j = 0;

        switch(dom_axis)
        {
            case 0:
                for(i = 0; i < numCoords; i++ )
                {
                    working2dCoords[j++] = coords[i * 3 + 1] - (float)point.y;
                    working2dCoords[j++] = coords[i * 3 + 2] - (float)point.z;
                }
                break;

            case 1:
                for(i = 0; i < numCoords; i++ )
                {
                    working2dCoords[j++] = coords[i * 3]     - (float)point.x;
                    working2dCoords[j++] = coords[i * 3 + 2] - (float)point.z;
                }
                break;

            case 2:
                for(i = 0; i < numCoords; i++ )
                {
                    working2dCoords[j++] = coords[i * 3]     - (float)point.x;
                    working2dCoords[j++] = coords[i * 3 + 1] - (float)point.y;
                }
                break;
        }

        int sh;  // current sign holder
        int nsh; // next sign holder
        float dist;
        int crossings = 0;

        // Step 4.
        // Set sign holder as f(V' o) ( V' value of 1st vertex of 1st edge)
        if(working2dCoords[1] < 0.0)
            sh = -1;
        else
            sh = 1;

        for(i = 0; i < numCoords; i++)
        {
            // Step 5.
            // For each edge of polygon (Ua' V a') -> (Ub', Vb') where
            // a = 0..Nv-1 and b = (a + 1) mod Nv

            // b = (a + 1) mod Nv
            j = (i + 1) % numCoords;

            int i_u = i * 2;           // index of Ua'
            int j_u = j * 2;           // index of Ub'
            int i_v = i * 2 + 1;       // index of Va'
            int j_v = j * 2 + 1;       // index of Vb'

            // Set next sign holder (Nsh) as f(Vb')
            // Nsh = -1 if Vb' < 0
            // Nsh = +1 if Vb' >= 0
            if(working2dCoords[j_v] < 0.0)
                nsh = -1;
            else
                nsh = 1;

            // If Sh <> NSH then if = then edge doesn't cross +U axis so no
            // ray intersection and ignore

            if(sh != nsh)
            {
                // if Ua' > 0 and Ub' > 0 then edge crosses + U' so Nc = Nc + 1
                if((working2dCoords[i_u] > 0.0) && (working2dCoords[j_u] > 0.0))
                {
                    crossings++;
                }
                else if ((working2dCoords[i_u] > 0.0) ||
                         (working2dCoords[j_u] > 0.0))
                {
                    // if either Ua' or U b' > 0 then line might cross +U, so
                    // compute intersection of edge with U' axis
                    dist = working2dCoords[i_u] -
                           (working2dCoords[i_v] *
                            (working2dCoords[j_u] - working2dCoords[i_u])) /
                           (working2dCoords[j_v] - working2dCoords[i_v]);

                    // if intersection point is > 0 then must cross,
                    // so Nc = Nc + 1
                    if(dist > 0)
                        crossings++;
                }

                // Set SH = Nsh and process the next edge
                sh = nsh;
            }
        }

        // Step 6. If Nc odd, point inside else point outside.
        // Note that we have already stored the intersection point way back up
        // the start.
        return ((crossings % 2) == 1);
    }

    /**
     * Convenience method to transform a bunch of coordinates by the vworld
     * coordinates. Operates on the current workingCoords.
     *
     * @param vtxCount The number of verticies to transform
     * @param vworld The coordinate transform to apply
     */
    private void transformCoords(int vtxCount, Transform3D vworld)
    {
        // Now transform them to the world coordinates in place if we have a
        // non-identity matrix
        if(vworld.getBestType() == Transform3D.IDENTITY)
            return;

        int cnt = 0;

        for(int i = vtxCount; --i >= 0; )
        {
            wkPoint.x = workingCoords[cnt];
            wkPoint.y = workingCoords[cnt + 1];
            wkPoint.z = workingCoords[cnt + 2];

            vworld.transform(wkPoint);

            workingCoords[cnt++] = (float)wkPoint.x;
            workingCoords[cnt++] = (float)wkPoint.y;
            workingCoords[cnt++] = (float)wkPoint.z;
        }
    }
}
