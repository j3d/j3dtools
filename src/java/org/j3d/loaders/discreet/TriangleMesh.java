/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.discreet;

// External imports
// None

// Local imports
// None

/**
 * Representation of a single triangle mesh
 * <p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class TriangleMesh
{
    /** Vertex coordinates, flat style */
    public float[] vertex;

    /** The number of valid vertices in this mesh */
    int numVertex;

    /** Something???? Vec2 */
    public float[] st;
    public int num_st;

    /** Index lists for each face */
    public int[] face;

    /** Index lists for the groups of faces that should be smooth shaded */
    public int[] smoothgroup;

    /** Total number of valid faces. */
    public int numFace;

    /** Listing of normals for each vertex */
    public float[] normal;

    /** Listing of tangents for each vertex */
    public float[] tangent;

    /** Listing of binormals for each vertex */
    public float[] binormal;
}
