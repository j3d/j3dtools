/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.subdivision;

// Standard imports
// None

// Application specific imports
// None

/**
 * Internal representation of the face structure.
 * <p>
 *
 * This face may be used at all levels of the system. Not all data is always
 * supplied. Some data is only allowed at the top level structure - the
 * original control mesh. Data that is used at every level is available as
 * public access. Data that is only available at the top level is hidden
 * and only accesible through method calls. Any time this data is needed, a
 * walk up the parent face tree is performed to get the information from the
 * top-level face. There may be a way of optimising this in the future to
 * prevent having to do the walk all the time, but that will be done later.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class Face
{
    /** Amount to resize any array to */
    private static final in SECTOR_INC = 3;

    /** Pointer to the parent face that this belongs to */
    Face parent;

    /** My index among my parent's list of children */
    int myParentIndex;

    /** My children faces */
    Face[] children;

    /** The size of the face in number of vertices */
    int numVertex;

    /** List of vertices in this face */
    Vertex[] vertices;

    /**
     * Listing of faces that are adjacent to this face. Only used if this
     * is a top-level face rather than one that has been generated through
     * subdivision.
     */
    private Face[] neighbours;

    /**
     * Edge names from the point of view of the adjacent faces. Only used if
     * this is a top-level face rather than one that has been generated through
     * subdivision.
     */
    int[] neighbourEdgeIndex;

    /**
     * List of sector tags for this face. Only used if this is a top-level face
     * rather than one that has been generated through subdivision.
     */
    Sector[] sectors;

    /**
     * List of edge tags for this face. Only used if this is a top-level face
     * rather than one that has been generated through subdivision.
     */
    int[] edgeTags;

    /**
     * The total number of subdivisions to be created. Used as a convenience handle
     * rather than having to ask for it all the time from another source.
     */
    int numSubdivs;

    /** The number of items in the sector array. */
    private int numSectors;

    /** The number of items in the sector array. */
    private int numNeighbours;

    /**
     * Construct a new default instance of the face.
     */
    Face()
    {
        numSubdivs = 0;
        numSectors = 0;
        numNeighbours = 0;
    }

    void setCenterPosition(int d, float[] p)
    {
        Vertex center = centerVertex();

        center.setPosition(d + 1, p);
        center.currentDepth = d + 1;
    }

    void setCenterPosition(int d, float x, float y, float z)
    {
        Vertex center = centerVertex();

        center.setPosition(d + 1, x, y, z);
        center.currentDepth = d + 1;
    }

    void centerPos(int d, float[] res)
    {
        float[] p = centerVertex().getPos(d + 1);

        res[0] = p[0];
        res[1] = p[1];
        res[2] = p[2];
    }

    boolean hasMidPosition(int e, int d)
    {
        Vertex vtx = midVertex(e);
        return vtx.currentDepth <= d + 1;
    }

    void setMidPosition(int e, int d, float[] p)
    {
        Vertex vtx = midVertex(e);
        vtx.setPosition(d + 1, p);
    }

    Vertex centerVertex()
    {
        return (children == null) ? null : children[0].vertices[2];
    }

    int nextEdgeIndex(int e)
    {
        return (e < 0) ? -(-e + numVertex - 2) % numVertex - 1 : e % numVertex + 1;
    }

    int prevEdgeIndex(int e)
    {
        return (e < 0) ? -(-e % numVertex - 1) : (e + numVertex - 2) % numVertex + 1;
    }

    int reverseEdgeIndex(int e)
    {
        return -e;
    }

    int edgeNumToVertexNum(int v)
    {
        return (v == 0) ? numVertex : v;
    }

    int headVertexIndex(int e)
    {
        return (e > 0) ? e % numVertex : -e - 1;
    }

    int tailVertexIndex(int e)
    {
        return (e > 0) ? e - 1 : -e % numVertex;
    }

    Vertex headVertex(int e)
    {
        return vertices[headVertexIndex(e)];
    }

    Vertex tailVertex(int e)
    {
        return vertices[tailVertexIndex(e)];
    }

    /**
     * Find the mid vertex of the given edge.
     */
    Vertex midVertex(int e)
    {
        Face mf = midEdge(e);
        int me = midEdgeIndex(e);

        return mf.headVertex(me);
    }

    Face midEdge(int e)
    {
        int abs_e = e >= 0 ? e : -e;

        return (children == null) ? null : children[abs_e - 1];
    }

    int midEdgeIndex(int e)
    {
        return (children != null) ? 1 : 0;
    }

    Face headSubEdge(int e)
    {
        Face ret_val = null;

        if(children != null)
        {
            int c = (e > 0) ? e % numVertex : -e - 1;
            ret_val = children[c];
        }

        return ret_val;
    }

    int headSubEdgeIndex(int e)
    {
        int ret_val = 0;

        if(children != null)
            ret_val = (e > 0) ? children.length : -1;

        return ret_val;
    }

    Face tailSubEdge(int e)
    {
        Face ret_val = null;

        if(children != null)
        {
            int c = (e > 0) ? e - 1 : -e % numVertex;
            ret_val = children[c];
        }

        return ret_val;
    }

    int tailSubEdgeIndex(int e)
    {
        int ret_val = 0;

        if(children != null)
            ret_val = (e > 0) ? 1 : -numVertex;

        return ret_val;
    }

    Face neighbor(int e)
    {
        Face ret_val;

        if(parent == null)
        {
            if(e > 0)
                ret_val = neighbours[e - 1];
            else
                ret_val = neighbours[-e - 1];
        }
        else
        {
            int nc = 0;
            int ne = 0;

            switch(e)
            {
                case 2:
                    ne = -3;
                    nc = (myParentIndex + 1) % parent.numVertex;
                    break;

                case -2:
                    ne = 3;
                    nc = (myParentIndex + 1) % parent.numVertex;
                    break;

                case 3:
                    ne = -2;
                    nc = (myParentIndex + parent.numVertex -1) %
                         parent.numVertex;
                    break;

                case -3:
                    ne = 2;
                    nc = (myParentIndex + parent.numVertex - 1) %
                         parent.numVertex;
                    break;
            }

            if(ne != 0)
                ret_val = parent.children[nc];
            else
            {
                int pe = parentEdgeIndex(e);

                int npe = parent.neighborIndex(pe);
                Face npt = parent.neighbor(pe);

                // if the npt.children == null that is the test for
                // a leaf node.
                if((npt == null) || npt.children == null)
                {
                  ret_val = null;
                }
                else
                {
                  if(headVertex(e) == headVertex(npe))
                    ret_val = npt.headSubEdge(npe);
                  else
                    ret_val = npt.tailSubEdge(npe);
                }
            }
        }

        return ret_val;
    }

    int neighborIndex(int e)
    {
        int ret_val = 0;
        if(parent == null)
        {
            if(e > 0)
                ret_val = neighbourEdgeIndex[e - 1];
            else
                ret_val = neighbourEdgeIndex[-e - 1];
        }
        else
        {
            switch(e)
            {
                case 2:
                    ret_val = -3;
                    break;

                case -2:
                    ret_val = 3;
                    break;

                case 3:
                    ret_val = -2;
                    break;

                case -3:
                    ret_val = 2;
                    break;

                default:
                    int pe = parentEdgeIndex(e);

                    int npe = parent.neighborIndex(pe);
                    Face npt = parent.neighbor(pe);

                    if((npt != null) && (npt.children != null))
                    {
                        if(headVertex(e) == headVertex(npe))
                            ret_val = npt.headSubEdgeIndex(npe);
                        else
                            ret_val = npt.tailSubEdgeIndex(npe);
                    }
            }
        }

        return ret_val;
    }

    int parentEdgeIndex(int e)
    {
        int ret_val = 0;

        switch(e)
        {
            case 1:
                ret_val = myParentIndex + 1;
                break;

            case -1:
                ret_val = -(myParentIndex + 1);
                break;

            case 4:
                ret_val = (myParentIndex == 0) ? parent.numVertex : myParentIndex;
                break;

            case -4:
                ret_val = (myParentIndex == 0) ? -parent.numVertex : -myParentIndex;
                break;
        }

        return ret_val;
    }

    // Methods for walking back up the tree to get the root-level definitions.
    int vertexTag(int v)
    {
        int ret_val = SubdivisionTypes.SMOOTH_VERTEX;

        if(vertices[v].isSpecial)
        {
            int tv = tlVertexIndex(v);
            Face tf = tlVertex(v);

            if(tf != null)
                return tf.vertexTag(tv);

            Face pt = parent;
            int pe_total = pt.numVertex + 1;
            int pe = pe_total;

            while((pe == pt.numVertex + 1) && (pt != null))
            {
                pe = 1;

                while((pe < pe_total) && (pt.midVertex(pe) != vertices[v]))
                    pe++;

                if(pe == pe_total)
                {
                    pt = pt.parent;
                    pe = pe_total;
                }
            }

            if(pt.edgeTag(pe) != SubdivisionTypes.UNTAGGED_EDGE)
                ret_val = SubdivisionTypes.CREASE_VERTEX;
        }

        return ret_val;
    }

    int edgeTag(int e)
    {
        int ret_val;

        if(headVertex(e).isSpecial)
        {
            // Are we at the top level yet?
            if(parent == null)
                return edgeTags[(e > 0 ? e : -e)  - 1];

            Face tf = tlEdge(e);

            if(tf == null)
                ret_val = SubdivisionTypes.UNTAGGED_EDGE;
            else
            {
                int te = tlEdgeIndex(e);
                ret_val = tf.edgeTags[te];
            }
        }
        else
            ret_val = SubdivisionTypes.UNTAGGED_EDGE;

        return ret_val;
    }

    int sectorTag(int v)
    {
        int ret_val = Sector.UNTAGGED_SECTOR;
        Sector s = sectorInfo(v);

        if(s != null)
            return s.tag;

        return ret_val;
    }

    Sector sectorInfo(int v)
    {
        Sector ret_val = null;

        if(vertices[v].isSpecial)
        {
            // Are we at the top level yet?
            if(parent == null)
                ret_val = sectors[v];

            Face tf = tlVertex(v);

            if(tf != null)
            {
                int tv = tlVertexIndex(v);
                ret_val = tf.sectorInfo(tv);
            }
        }

        return ret_val;
    }

    Face tlVertex(int v)
    {
        Face ret_val = null;

        if(parent == null)
            ret_val = this;
        else
        {
            Face pf = parentVertex(v);
            if(pf != null)
            {
                int pv = parentVertexIndex(v);
                ret_val = pf.tlVertex(pv);
            }
        }

        return ret_val;
    }

    int tlVertexIndex(int v)
    {
        int ret_val = 0;

        if(parent == null)
            ret_val = v;
        else
        {
            Face pf = parentVertex(v);
            if(pf != null)
            {
                int pv = parentVertexIndex(v);
                ret_val = pf.tlVertexIndex(pv);
            }
        }

        return ret_val;
    }

    Face parentVertex(int v)
    {
        Face ret_val = parent;

        if(ret_val != null)
        {
            int num_vtx = ret_val.numVertex;
            for(int i = 0; i < num_vtx; i++)
            {
                if(ret_val.vertices[i] == vertices[i])
                    break;
            }
        }

        return ret_val;
    }

    int parentVertexIndex(int v)
    {
        int ret_val = 0;

        if(parent != null)
        {
            int num_vtx = parent.numVertex;
            for(int i = 0; i < num_vtx; i++)
            {
                if(parent.vertices[i] == vertices[i])
                {
                    ret_val = i;
                    break;
                }
            }
        }

        return ret_val;
    }

    Face tlEdge(int e)
    {
        Face ret_val = null;

        if(parent == null)
        {
            ret_val = this;
        }
        else
        {
            int pe = parentEdgeIndex(e);
            ret_val = parent.tlEdge(pe);
        }

        return ret_val;
    }

    int tlEdgeIndex(int e)
    {
        int ret_val = 0;

        if(parent == null)
        {
            ret_val = e;
        }
        else
        {
            int pe = parentEdgeIndex(e);
            ret_val = parent.tlEdgeIndex(pe);
        }

        return ret_val;
    }

    void makeChildren(int d)
    {
        children = new Face[numVertex];

        Vertex center_v = new Vertex(numSubdivs, d + 1);

        for(int i = 0; i < numVertex; i++)
        {
            children[i] = new Face();
            children[i].myParentIndex = i;
            children[i].parent = this;
            children[i].vertices = new Vertex[4];
            children[i].numSubdivs = numSubdivs;
        }

        // inherit old vertices
        for(int e = 1; e < numVertex + 1; e++)
        {
            int se;
            Face st;

            Vertex mid_v = null;
            Face nt = neighbor(e);
            int ne = neighborIndex(e);

            if(nt != null && nt.children != null)
                mid_v = nt.midVertex(ne);

            if(mid_v == null)
            {
                mid_v = new Vertex(numSubdivs, d + 1);
            }

            st = tailSubEdge(e);
            se = tailSubEdgeIndex(e);

            st.vertices[st.headVertexIndex(se)] = mid_v;
            st.vertices[st.tailVertexIndex(se)] = tailVertex(e);
            st.vertices[st.headVertexIndex(st.nextEdgeIndex(se))] = center_v;

            st = headSubEdge(e);
            se = headSubEdgeIndex(e);

            st.vertices[st.headVertexIndex(se)] = headVertex(e);
            st.vertices[st.tailVertexIndex(se)] = mid_v;
        }
    }

    /**
     * Clean up the face by removing the mid vertices that are used as temporaries
     * by the face.
     */
    void clearFace(int d)
    {
        if(children == null)
            return;

        for(int e = 1; e < numVertex; e++)
        {
            int me = midEdgeIndex(e);
            Face mf = midEdge(e);

            mf.headVertex(me).currentDepth = 0;
            mf.setNormal(mf.headVertexIndex(me), null);

            headVertex(e).setDepth(d);
        }

        // vertex index is 2 for center.
        Face cf = children[0];
        cf.setNormal(cf.headVertexIndex(2), null);
        cf.headVertex(2).currentDepth = 0;
    }

    /**
     * Set the normal to the new value.
     *
     * @param v The index of the vertex to set the normal for
     * @param n The new normal value. Null will clear
     */
    void setNormal(int v, float[] n)
    {
        if(vertices[v].isSpecial)
        {
            Sector s = sectorInfo(v);

            if(s != null)
                s.setNormal(n);
            else if(vertexTag(v) == SubdivisionTypes.SMOOTH_VERTEX)
                vertices[v].setNormal(n, true);
            else
            {
                // we need to find an edge for this vertex...
                int p_vtx = parent.numVertex + 1;
                Face pt = parent;
                int pe = p_vtx;
                while((pe == p_vtx) && (pt != null))
                {
                    pe = 1;
                    while((pe < p_vtx) && (pt.midVertex(pe) != vertices[v]))
                        pe++;

                    if(pe == p_vtx)
                    {
                        pt = pt.parent;
                        pe = pt.numVertex + 1;
                        p_vtx = pe;
                    }
                }

                int ne = pt.neighborIndex(pe);
                Face nf = pt.neighbor(pe);

                //Original code had "if(nf < pt)", which doesn't make sense.
                if(ne < pe)
                    vertices[v].setNormal(n, true);
                else
                    vertices[v].setNormal(n, false);
            }
        }
        else
            vertices[v].setNormal(n, true);
    }

    /**
     * Add a sector to the list. Assumes a check has been done for a valid
     * vertex index before calling this method.
     */
    void addSector(int vertex,
                   int tag,
                   float flatness,
                   float theta,
                   float[] normal,
                   float normalT)
    {
        if((sectors == null) || (sectors.length == numSectors))
        {
            Sector[] tmp = new Sector[numSectors + SECTOR_INC];

            if(sectors != null)
                System.arraycopy(sectors, 0, tmp, 0, numSectors);
            sectors = tmp;
        }

        Sector s = new Sector();
        s.vertexIndex = vertex;
        s.flatness = flatness;
        s.theta = theta;
        s.normalT = normalT;
        s.setNormal(normal);

        sectors[numSectors] = s;
        numSectors++;
    }

    /**
     * Remove the sector indicated by the given vertex number.
     */
    void removeSector(int vertex)
    {
        // no checking for null. Assume the user is not stupid.

        for(int i = 0; i < numSectors; i++)
        {
            if(sectors[i].vertexIndex == vertex)
            {
                System.arraycopy(sectors, i, sectors, i + 1, numSectors - i - 1);
                break;
            }
        }
    }
}
