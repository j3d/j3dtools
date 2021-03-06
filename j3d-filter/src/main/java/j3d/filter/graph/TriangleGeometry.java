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
// None

// Local Imports
// None

/**
 * Represents a series of points used for triangle. 
 * <p/>
 * Each three consecutive points is a single, non-connected line.
 *
 * @author Justin
 * @version $Revision$
 */
public interface TriangleGeometry
    extends VertexGeometry
{
    /** 
     * Get the count of how many total triangles are provided. Only full
     * triangles are represented - effectively this is 
     * <code>floor(getVertexCount() / 4);</code>
     * 
     * @return a number >= 0
     */
    public int getTriangleCount();
}
