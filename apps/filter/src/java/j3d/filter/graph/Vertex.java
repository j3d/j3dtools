/*****************************************************************************
 *                        j3d.org Copyright (c) 2004
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
import java.awt.Color;

// Local imports
// None

/**
 * Abstract representation of a generic vertex in the geometry
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class Vertex
{
    /** X coordinate of the vertex */
    public double xCoord;

    /** Y coordinate of the vertex */
    public double yCoord;

    /** Z coordinate of the vertex */
    public double zCoord;

    /** X component of the normal for this vertex */
    public double xNormal;

    /** Y component of the normal for this vertex */
    public double yNormal;

    /** Z component of the normal for this vertex */
    public double zNormal;

    /** W component of the normal for this vertex */
    public double wNormal;

    /** If a per-vertex colour is set, this will be non-null */
    public Color colour;

    /**
     * Array of texture coordinates for this vertex. If no texture
     * coordinates, this will be null.
     */
    public float[][] texCoords;

    /**
     * For each array in the texture coordinates, this is how many dimensions
     * that it has. Should be a value between 1 and 4
     */
    public int[] texCoordDimensions;

    /**
     * Construct a default vertex representation
     */
    public Vertex()
    {
        wNormal = 1;
    }
}
