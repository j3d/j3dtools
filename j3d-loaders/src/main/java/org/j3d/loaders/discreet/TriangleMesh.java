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
 * Representation of a single triangle mesh.
 * <p>
 *
 * A triangle mesh consists of vertices, face sets connecting the vertices, and
 * a single set of 2D texture coordinates (multitexture not supported by
 * the .3ds format). Tangent and binormals are calculated on the fly from the
 * provided information. The file definition of this chunk is:
 * <pre>
 * TRIANGULAR MESH 0x4100
 *     VERTICES LIST 0x4110
 *     FACES DESCRIPTION 0x4120
 *         FACES MATERIAL 0x4130
 *     MAPPING COORDINATES LIST 0x4140
 *     SMOOTHING GROUP LIST 0x4150
 *     LOCAL COORDINATES SYSTEM 0x4160
 * </pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class TriangleMesh
{
    /** Vertex coordinates, flat style */
    public float[] vertices;

    /** The number of valid vertices in this mesh */
    public int numVertices;

    /** 2D texture coordinates for this object */
    public float[] texCoords;

    /** The number of texture coordinates */
    public int numTexCoords;

    /** Index lists for each face */
    public int[] faces;

    /** Total number of valid faces. */
    public int numFaces;

    /** Index lists for the groups of faces that should be smooth shaded */
    public int[] smoothgroups;

    /** Listing of normals for each vertex */
    public float[] normals;

    /** Listing of tangents for each vertex */
    public float[] tangents;

    /** Listing of binormals for each vertex */
    public float[] binormals;

    /** Local coordinate system reference (a 4x3 matrix)*/
    public float[] localCoords;

    /** Listing of the material groups needed */
    public MaterialData[] materials;

    /** Number of valid material groups available */
    public int numMaterials;

    /**
     * Names of the materials used for a cubic environment map. This will be
     * mapped in the order: front, back, left, right, top, bottom. Will be null
     * if nothing declared.
     */
    public String[] boxMapMaterials;
}
