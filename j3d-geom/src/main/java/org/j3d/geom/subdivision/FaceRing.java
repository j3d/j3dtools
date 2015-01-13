/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.subdivision;

// External imports
// None

// Local imports
// None

/**
 * Internal representation of the faces in the vertex ring.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class FaceRing
{
    /** Flag to say if this face represents a closed face of vertices */
    boolean isClosed;

    /** starting edge number (0 for closed rings) */
    int startEdge;

    /** Face where we started to collect the ring (center face) */
    Face centerFace;

    /** edge where we started */
    int edgeId;

    /** Number of edges in use */
    private int numEdge;

    /** Edges related to this ring */
    private int[] edgeIndexes;

    /** Number of faces in use */
    int numFace;

    /** List of faces in this ring */
    Face[] faces;

    /** The current depth this ring is operating at */
    int depth;

    /**
     * Create a new ring structure that has the specified maximum number of
     */
    FaceRing(int maxSize)
    {
        edgeIndexes = new int[maxSize];
        faces = new Face[maxSize];
        depth = 0;
    }

    /**
     * Rebuild the ring structure based on the presented face and the edge vertex
     * number as the start point.
     */
    void rebuild(Face f, int e, int d)
    {
        depth = d;
        switch(f.vertexTag(f.headVertexIndex(e)))
        {
            case SubdivisionTypes.SMOOTH_VERTEX:
                rebuildFromRing(f, e);
                break;

            case SubdivisionTypes.CREASE_VERTEX:
            case SubdivisionTypes.CORNER_VERTEX:
                rebuildFromSector(f, e);
        }
    }

    Vertex vertex(int i)
    {
        Vertex ret_val = null;

        if(i < numEdge)
            ret_val = faces[i].headVertex(edgeIndexes[i]);
        else
        {
            int idx = faces[i - 1].prevEdgeIndex(edgeIndexes[i-1]);
            ret_val = faces[i - 1].tailVertex(idx);
        }

        return ret_val;
    }

    /**
     * Get the face at index i. Takes into accound the looped nature
     *
     * @param i The face index
     * @return The face at that point
     */
    Face getFace(int i)
    {
        return (i < numVertex()) ? faces[i] : faces[i - 1];
    }

    /**
     * Get the index of the face at index i. Takes into accound the
     * looped nature.
     *
     * @param i The face index
     * @return The face at that point
     */
    int getFaceIndex(int i)
    {
        return (i < numEdge) ?
               edgeIndexes[i] :
               -faces[i - 1].prevEdgeIndex(edgeIndexes[i - 1]);
    }

    /**
     * Get the edge corresponding to the face in getFace()
     *
     * @param i The face index
     * @return The edge number
     */
    int getFaceEdge(int i)
    {
        return (i < numVertex()) ?
                edgeIndexes[i] :
                faces[i - 1].prevEdgeIndex(i - 1);
    }

    /**
     * Get the center vertex from the list
     */
    int getCenterFaceIndex()
    {
        return centerFace.headVertexIndex(edgeId);
    }

    /**
     * Get the center vertex from the list
     */
    Face getCenterFace()
    {
        return centerFace;
    }

    Vertex getCenterVertex()
    {
        return centerFace == null ? null : centerFace.headVertex(edgeId);
    }

    int numVertex()
    {
        return isClosed ? numEdge : numEdge + 1;
    }

    void rebuildFromRing(Face f, int e)
    {
        int cei = -e;  // current edge index
        Face current_face = f;
        int edge_idx = 0;
        int face_idx = 0;
        centerFace = f;
        startEdge = e;
        edgeId = 0;
        isClosed = true;

        do
        {
            // make sure that the tail of the current edge is the center
            edgeIndexes[edge_idx] = cei;
            faces[face_idx] = current_face;

            edge_idx++;
            face_idx++;

            // get the name of the current edge from the point of view
            // of the adjacent polygon, and get the other edge of the
            // adjacent polygon which has _center as an endpoint;
            // the direction is chosen so that center is the tail of the other edge
            int cf_nei = current_face.nextEdgeIndex(-cei);
            cei = current_face.neighborIndex(cf_nei);
            current_face = current_face.neighbor(cf_nei);

            // until we run into the boundary or return to the initial vertex
        }
        while(current_face != null && current_face != centerFace);

        // if we hit the boundary, return to the original edge
        // and go in the opppsite direction;
        // as we go, update the edge number edge(), so that it
        // always points to the initial edge

        if(current_face == null)
        {
            isClosed = false;
            current_face = centerFace;
            cei = -e;

            do
            {
                int c = cei;
                cei = current_face.neighborIndex(c);
                current_face = current_face.neighbor(c);

                if(current_face != null)
                {
                    cei = current_face.nextEdgeIndex(-cei);

                    edgeIndexes[edge_idx] = cei;
                    faces[face_idx] = current_face;

                    edge_idx++;
                    face_idx++;
                    startEdge++;
                }
            }
            while(current_face != null);
        }

        numEdge = edge_idx;
        numFace = face_idx;
    }

    /**
     * Rebuild the ring as a sector because this face borders on a crease or
     * corner.
     */
    private void rebuildFromSector(Face f, int e)
    {
        int cei = -e;  // current edge index
        Face current_face = f;
        int edge_idx = 0;
        int face_idx = 0;
        centerFace = f;
        startEdge = e;
        edgeId = 0;
        isClosed = true;

        do
        {
            edgeIndexes[edge_idx] = cei;
            faces[face_idx] = current_face;

            edge_idx++;
            face_idx++;

            // get the name of the current edge from the point of view
            // of the adjacent polygon, and get the other edge of the
            // adjacent polygon which has _center as an endpoint;
            // the direction is chosen so that center is the tail of the other edge
            int cf_nei = current_face.nextEdgeIndex(-cei);
            cei = current_face.neighborIndex(cf_nei);
            current_face = current_face.neighbor(cf_nei);

            // until we run into the boundary or return to the initial vertex
        }
        while((current_face != null) &&
              (current_face != centerFace) &&
              (current_face.edgeTag(cei) == SubdivisionTypes.UNTAGGED_EDGE));

        // if we hit the boundary, return to the original edge
        // and go in the opppsite direction;
        // as we go, update the edge number edge(), so that it
        // always points to the initial edge

        if((current_face == null) ||
           (current_face.edgeTag(cei) != SubdivisionTypes.UNTAGGED_EDGE))
        {
            isClosed = false;
            current_face = centerFace;
            cei = -e;

            do
            {
                int cf_cei = cei;
                cei = current_face.neighborIndex(cei);
                current_face = current_face.neighbor(cei);

                if((current_face != null) &&
                   (current_face.edgeTag(cei) == SubdivisionTypes.UNTAGGED_EDGE))
                {
                    cei = current_face.nextEdgeIndex(-cei);
                    edgeIndexes[edge_idx] = cei;
                    faces[face_idx] = current_face;

                    edge_idx++;
                    face_idx++;
                    startEdge++;
                }
            }
            while((current_face != null) &&
                  (current_face.edgeTag(cei) == SubdivisionTypes.UNTAGGED_EDGE));
        }
    }
}
