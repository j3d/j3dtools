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
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class TriangulationUtils
{
    /** The default size of the polygon values */
    private static final int DEFAULT_POLY_SIZE = 6;

    /** Cache of polygon vertex structures for efficiency */
    private static ObjectArray vertexCache;

    /** Set of concave vertices for this polygon */
    private HashSet concaveVertices;

    /** Normal of the face being processed now */
    private float[] faceNormal;

    /** The current 2D coordinate list that we work from */
    private float[] working2dCoords;

    /** Array for reading out the concave vertices from the hashset */
    private PolyVertex[] tmpArray;

    /**
     * Static construct.
     */
    static
    {
        vertexCache = new ObjectArray();
    }

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
        concaveVertices = new HashSet(size);
        faceNormal = new float[3];
        working2dCoords = new float[6];
        tmpArray = new PolyVertex[size];
    }

    /**
     * Triangulate a concave polygon using an indexed coordinates array. The
     * start index is the index into the indexArray for the first vertex of the
     * polygon. The coordinates are not required to be a closed polygon as the
     * algorithm will automatically close it. The output array is the new indexes
     * to use
     *
     * @param coords The coordinates of the face
     * @param startIndex The index of the first coordinate in the face
     * @param numVertex  The number of vertices to read from the list
     * @param output The array to copy the index values to to describe
     * @param normal The normal to this face these vertices are a part of
     * @return The number of triangles in the output array
     */
    public int triangulateConcavePolygon(float[] coords,
                                         int startIndex,
                                         int numVertex,
                                         int[] indexArray,
                                         int[] output,
                                         float[] normal)
    {
        if(numVertex < 3)
            return 0;
        else if(numVertex == 3)
        {
            output[0] = startIndex;
            output[1] = startIndex + 3;
            output[2] = startIndex + 6;

            return 1;
        }

        faceNormal[0] = normal[0];
        faceNormal[1] = normal[1];
        faceNormal[2] = normal[2];

        if(numVertex > tmpArray.length)
            tmpArray = new PolyVertex[numVertex];

        // More than 3, so work on the ear-splitting algorithm.
        // Build a list of the vertices in a circular list.
        // First vertex, then interior vertices, then last vertex
        int index = indexArray[startIndex];
        PolyVertex first = newVertex();
        first.x = coords[index * 3];
        first.y = coords[index * 3 + 1];
        first.z = coords[index * 3 + 2];
        first.vertexIndex = index;

        if(!isConvexVertex(coords,
                           indexArray[startIndex + numVertex - 1] * 3,
                           indexArray[startIndex] * 3,
                           indexArray[startIndex + 1] * 3,
                           faceNormal))
           concaveVertices.add(first);

        // Interior vertices
        PolyVertex current = first;
        PolyVertex prev = first;

        for(int i = 1; i < numVertex - 1; i++)
        {
            index = indexArray[i];
            current = newVertex();
            current.x = coords[index * 3];
            current.y = coords[index * 3 + 1];
            current.z = coords[index * 3 + 2];
            current.vertexIndex = index;

            if(!isConvexVertex(coords,
                               indexArray[index - 1] * 3,
                               indexArray[index] * 3,
                               indexArray[index + 1] * 3,
                               faceNormal))
                concaveVertices.add(current);

            current.prev = prev;
            prev.next = current;
            prev = current;
        }

        // Last vertex
        index = indexArray[startIndex + numVertex - 1];
        PolyVertex last = newVertex();
        last.x = coords[index * 3];
        last.y = coords[index * 3 + 1];
        last.z = coords[index * 3 + 2];
        last.vertexIndex = index;

        if(!isConvexVertex(coords,
                           indexArray[startIndex + numVertex - 2] * 3,
                           indexArray[startIndex + numVertex - 1] * 3,
                           indexArray[startIndex] * 3,
                           faceNormal))
            concaveVertices.add(last);

        first.prev = last;
        last.next = first;
        last.prev = current;
        current.next = last;

        return triangulate(first, output);
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
     *
     * @param coords The coordinates of the face
     * @param startIndex The index of the first coordinate in the face
     * @param numVertex  The number of vertices to read from the list
     * @param output The array to copy the index values to to describe
     * @param normal The normal to this face these vertices are a part of
     * @return The number of triangles in the output array
     */
    public int triangulateConcavePolygon(float[] coords,
                                         int startIndex,
                                         int numVertex,
                                         int[] output,
                                         float[] normal)
    {
        if(numVertex < 3)
            return 0;
        else if(numVertex == 3)
        {
            output[0] = startIndex;
            output[1] = startIndex + 3;
            output[2] = startIndex + 6;

            return 1;
        }

        faceNormal[0] = normal[0];
        faceNormal[1] = normal[1];
        faceNormal[2] = normal[2];

        if(numVertex > tmpArray.length)
            tmpArray = new PolyVertex[numVertex];

        // More than 3, so work on the ear-splitting algorithm.
        // Build a list of the vertices in a circular list.
        // First vertex, then interior vertices, then last vertex
        PolyVertex first = newVertex();
        first.x = coords[startIndex];
        first.y = coords[startIndex + 1];
        first.z = coords[startIndex + 2];
        first.vertexIndex = startIndex;

        if(!isConvexVertex(coords,
                           startIndex + (numVertex - 1) * 3,
                           startIndex,
                           startIndex + 3,
                           faceNormal))
           concaveVertices.add(first);

        // Interior vertices
        PolyVertex current = first;
        PolyVertex prev = first;
        int vtx = startIndex + 3;

        for(int i = 1; i < numVertex - 1; i++)
        {
            current = newVertex();
            current.x = coords[vtx];
            current.y = coords[vtx + 1];
            current.z = coords[vtx + 2];
            current.vertexIndex = vtx;

            if(!isConvexVertex(coords, vtx - 3, vtx, vtx + 3, faceNormal))
                concaveVertices.add(current);

            current.prev = prev;
            prev.next = current;
            prev = current;

            vtx += 3;
        }

        // Last vertex
        PolyVertex last = newVertex();
        last.x = coords[vtx];
        last.y = coords[vtx + 1];
        last.z = coords[vtx + 2];
        last.vertexIndex = vtx;

        if(!isConvexVertex(coords,
                           vtx - 3,
                           vtx,
                           startIndex,
                           faceNormal))
            concaveVertices.add(last);

        first.prev = last;
        last.next = first;
        last.prev = current;
        current.next = last;

        return triangulate(first, output);
    }

    /**
     * Clean up the internal cache and reduce it to zero.
     */
    public void clearCachedObjects()
    {
        vertexCache.clear();
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

        return dot <= 0;
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Perform the triangulation process now based on the vertex listing.
     *
     * @param first The first vertex of the polygon
     * @param output The array to copy the index values to to describe
     * @return The number of triangles in the output array
     */
    private int triangulate(PolyVertex first, int[] output)
    {
        // Now do the real triangulation algorithm.
        int output_index = 0;

        PolyVertex current = first.next.next;
        while(current != first)
        {
            PolyVertex prev_vtx = current.prev;

            if(isEar(prev_vtx) && !isTriangle(current))
            {
                // Add the triangle to the output
                output[output_index++] = current.vertexIndex;
                output[output_index++] = current.prev.vertexIndex;
                output[output_index++] = current.prev.prev.vertexIndex;

                // Cut the ear from the polygon by removing the previous vertex
                // Leave prev_vtx connected to the two endpoints for the moment
                // as we still need the info for concave calcs
                prev_vtx.prev.next = current;
                current.prev = prev_vtx.prev;

                // Check this line. Algo says to remove current.prev, not
                // current from the concave list, but I think that's wrong.
                if(concaveVertices.contains(current) &&
                   isConvexVertex(current.prev, current, current.next))
                    concaveVertices.remove(current);


                if(concaveVertices.contains(prev_vtx) &&
                   isConvexVertex(prev_vtx.prev, prev_vtx, prev_vtx.next))
                    concaveVertices.remove(current);

                if(prev_vtx == first)
                    current = current.next;

                // now clean up prev_vtx links and return the object to the
                // cache for using next time around.
                prev_vtx.next = null;
                prev_vtx.prev = null;
                freeVertex(prev_vtx);
            }
            else if(isTriangle(current))
                break;
            else
                current = current.next;
        }

        return (output_index / 3);
    }

    /**
     * Find out if this is an ear.
     *
     * @param p The vertex to test for being an ear
     * @return true if this is an ear
     */
    private boolean isEar(PolyVertex p)
    {
        int num_concaves = concaveVertices.size();
        if(num_concaves == 0)
            return true;

        if(isConvexVertex(p.prev, p, p.next))
        {
            // copy out the values from the convex array and test to see
            // if any of them lie inside the triangle
            concaveVertices.toArray(tmpArray);
            boolean found_vertex = false;
            for(int i = 0; i < num_concaves; i++)
            {
                PolyVertex cp = tmpArray[i];
                if((cp != p && cp != p.prev && cp != p.next) &&
                   isPointInTriangle(cp, p.prev, p, p.next))
                    found_vertex = true;
            }

            return !found_vertex;
        }
        else
            return false;
    }

    /**
     * Check to see if the polygon pointed to by p is a triangle. The
     * triangle test is just following the next pointer for 3 jumps. If
     * any of those 3 jumps comes back to the starting vertex then it
     * is a triangle. Otherwise there are more than 3 vertices in the
     * polygon, so it can't be a triangle.
     *
     * @param p The starting point of the polygon to test
     * @return true if the polygon is a triangle
     */
    private boolean isTriangle(PolyVertex p)
    {
        return (p.next == p) || (p.next.next == p);
    }

    /**
     * Alternate version of the convex checking based on the PolyVertex
     * structures. Equivalent to the public method.
     *
     * @param coords The array to read coodinate values from
     * @param p0 The index of the previous vertex to the one in question
     * @param p The index of the vertex being tested
     * @param p1 The index after the vertex after the one in question
     * @param The normal to this face these vertices are a part of
     * @return true if this is a convex vertex, false for concave
     */
    private boolean isConvexVertex(PolyVertex p0,
                                   PolyVertex p,
                                   PolyVertex p1)
    {
        // If is concave in a right-handed system when the cross product is
        // positive. Negative means it is concave
        float x1 =  p.x -  p0.x;
        float y1 =  p.y -  p0.y;
        float z1 =  p.z -  p0.z;

        float x2 =  p1.x -  p.x;
        float y2 =  p1.y -  p.y;
        float z2 =  p1.z -  p.z;

        float cross_x = y1 * z2 - z1 * y2;
        float cross_y = z1 * x2 - x1 * z2;
        float cross_z = x1 * y2 - y1 * x2;

        // now the dot product with the face normal
        float dot = cross_x * faceNormal[0] +
                    cross_y * faceNormal[1] +
                    cross_z * faceNormal[2];

        return dot <= 0;
    }

    /**
     * Special case version of the general ray-polygon intersection test
     * that is used to work out if the suspect point is inside or outside the
     * triangle defined by p0, p and p1.
     */
    private boolean isPointInTriangle(PolyVertex suspect,
                                      PolyVertex p0,
                                      PolyVertex p,
                                      PolyVertex p1)
    {
        int i, j;

        // So we have an intersection with the plane of the polygon and the
        // segment/ray. Using the winding rule to see if inside or outside
        // The exact intersection point is assumed to be the suspect point
        // because nominally the source polygon is planar.

        // bounds check
        // find the dominant axis to resolve to a 2 axis system
        double abs_nrm_x = (faceNormal[0] >= 0) ? faceNormal[0] : -faceNormal[0];
        double abs_nrm_y = (faceNormal[1] >= 0) ? faceNormal[1] : -faceNormal[1];
        double abs_nrm_z = (faceNormal[2] >= 0) ? faceNormal[2] : -faceNormal[2];

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
        switch(dom_axis)
        {
            case 0:
                working2dCoords[5] = p1.z - suspect.z;
                working2dCoords[4] = p1.y - suspect.y;
                working2dCoords[3] = p.z - suspect.z;
                working2dCoords[2] = p.y - suspect.y;
                working2dCoords[1] = p0.z - suspect.z;
                working2dCoords[0] = p0.y - suspect.y;
                break;

            case 1:
                working2dCoords[5] = p1.z - suspect.z;
                working2dCoords[4] = p1.x - suspect.x;
                working2dCoords[3] = p.z - suspect.z;
                working2dCoords[2] = p.x - suspect.x;
                working2dCoords[1] = p0.z - suspect.z;
                working2dCoords[0] = p0.x - suspect.x;
                break;

            case 2:
                working2dCoords[5] = p1.y - suspect.y;
                working2dCoords[4] = p1.x - suspect.x;
                working2dCoords[3] = p.y - suspect.y;
                working2dCoords[2] = p.x - suspect.x;
                working2dCoords[1] = p0.y - suspect.y;
                working2dCoords[0] = p0.x - suspect.x;
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

        // Check for a crossing for each of the 3 edges of the triangle. In
        // this simplified case of the general intersection test, Nv = 3 always
        for(i = 0; i < 3; i++)
        {
            // Step 5.
            // For each edge of polygon (Ua' V a') -> (Ub', Vb') where
            // a = 0..Nv-1 and b = (a + 1) mod Nv

            // b = (a + 1) mod Nv
            j = (i + 1) % 3;

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
     * Fetch a new entry object from the cache. If there are none, create a
     * new one.
     *
     * @return an available entry object
     */
    private synchronized static PolyVertex newVertex()
    {
        return (vertexCache.size() == 0) ?
               new PolyVertex() :
               (PolyVertex)vertexCache.remove(0);
    }

    /**
     * Release this entry back to the cache. Assumes that the entry has been
     * freed of all the links and value before the call.
     *
     * @param e The entry to put back in the list
     */
    private static void freeVertex(PolyVertex e)
    {
        vertexCache.add(e);
    }
}
