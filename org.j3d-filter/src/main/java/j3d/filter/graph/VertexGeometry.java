/*****************************************************************************
 *                  j3d.org Copyright (c) 2000 - 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.filter.graph;

// External imports
import java.util.List;

// Local Imports
// None

/**
 * Abstract representation of geometry that is represented with vertex (3D 
 * point in space) information
 * <p>
 * The list returned here is subject to interpretation of the extended 
 * type interface for the geometry that will be rendered.
 * 
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface VertexGeometry extends Geometry
{
    /**
     * Add a new vertex to the list. This is appended to the end of the list.
     * Null values are ignored, but duplicates are added to the list. 
     * 
     * @param vtx The vertex instance to add
     */
    public void addVertex(Vertex vtx);
    
    /**
     * Remove a vertex at the given index position in the list. If the
     * index is invalid, do nothing.
     * 
     * @param idx The index to remove the geometry at
     */
    public void removeVertex(int idx);
    
    /** 
     * Get the vertices in the order declared for this geometry.
     * 
     * @return A list of vertices, that may be empty.
     */
    public List<Vertex> getVertices();
    
    /**
     * Get the vertex at the given index position. If the index is
     * invalid, return null.
     * 
     * @param idx The index to fetch the geometry at
     * @return The corresponding vertex representation, or null if the index
     *    is bogus
     */
    public Vertex getVertex(int idx);
    
    /**
     * Get the count of how many raw vertices are provided by this geometry.
     * 
     * @return A value >= 0
     */
    public int getVertexCount();
}
