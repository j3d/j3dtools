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
import javax.vecmath.Point3d;

// Application specific imports
// none

/**
 * Utility class representing a segment of a polygon used in the tesselation
 * routines.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class PolyVertex
{
    /** the index from the source array this vertex corresponds to */
    public int vertexIndex;

    /** Index of the normal from the source data */
    public int normalIndex;

    /** Index of the normal from the source data */
    public int colorIndex;

    /** Index of the normal from the source data */
    public int texCoordIndex;

    /** X coordinate of the vertex */
    public float x;

    /** Y coordinate of the vertex */
    public float y;

    /** Z coordinate of the vertex */
    public float z;

    /** Pointer to the next segment */
    public PolyVertex next;

    /** Pointer to the previous segment */
    public PolyVertex prev;

    /**
     * Prints out the details of this vertex.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer("PolyVertex ");
        buf.append(vertexIndex);
        buf.append(" ni ");
        buf.append(normalIndex);
        buf.append(" ci ");
        buf.append(colorIndex);
        buf.append(" ti ");
        buf.append(texCoordIndex);
        buf.append(" x ");
        buf.append(x);
        buf.append(" y ");
        buf.append(y);
        buf.append(" z ");
        buf.append(z);

        return buf.toString();
    }
}
