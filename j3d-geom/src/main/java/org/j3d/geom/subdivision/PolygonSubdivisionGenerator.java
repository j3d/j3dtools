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
// none

// Local imports
import org.j3d.util.ObjectArray;

import org.j3d.geom.GeometryData;
import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.InvalidArraySizeException;
import org.j3d.geom.UnsupportedTypeException;

/**
 * Base geometry generator defintion for non-adaptive subdivision-based
 * patches that take arbitrary polygons as the input.
 * <P>
 *
 * This base class is suitable for all schemes that can work with arbitrary
 * polygons as input.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class PolygonSubdivisionGenerator extends SubdivisionGenerator
{
    /** Flag to say if edge structures must also be generated */
    private boolean needsEdges;

    /** List of top-level faces that describe this control mesh */
    protected ObjectArray faces;

    /**
     * Construct a new generator with no control mesh set. The levelMult field
     * describes the multiplier of how many vertices are created at each level
     * from the previous one. For triangle-based schemes, this value is normally
     * 3. The edge flag indicate whether this base class needs to generate edge
     * structures as well as the face and vertex information normally created.
     *
     * @param levelMult The mutliplier of vertices per level
     */
    protected PolygonSubdivisionGenerator(int levelMult, boolean edge)
    {
        super(levelMult);

        needsEdges = edge;
        faces = new ObjectArray();
    }

    /**
     * Set the control mesh for the arbitrary polygon shape using a flat
     * array of coordinates. The face indexes are the simple index values into
     * for the coordinates. It is assumed that to get the correct position in
     * the coordinates array, the index position must be multiplied by 3.
     * <p>
     * If the faceIndexCounts totals are greater than the length of faceIndexes,
     * an exception is generated.
     *
     * @param coordinates The coordinate values to put into the calculation
     * @param faceIndexes The list of indexes used by the faces
     * @param faceIndexCounts The number of faceIndex values to consume for
     *     each face
     * @param numFaces The number of faces to read out of the faceIndexCounts
     *     array
     * @param vertexFlags Array the same length as the coordinates, that contains
     *     a flag for every vertex describing it's disposition. The valid values
     *     are described in {@link SubdivisionTypes}.
     */
    public void setControlMesh(float[] coordinates,
                               int[] faceIndexes,
                               int[] faceIndexCounts,
                               int numFaces,
                               int[] vertexFlags)
    {
        int face_cnt = 0;

        for(int i = 0; i < numFaces; i++)
        {
            Face f = new Face();
            int num_vtx = faceIndexCounts[i];
            f.numVertex = num_vtx;
            f.vertices = new Vertex[num_vtx];

            for(int j = 0; j < num_vtx; j++)
            {
                Vertex v = new Vertex(totalSubdivisions, 0);
                int idx = faceIndexes[face_cnt] * 3;

                v.position[0][0] = coordinates[idx++];
                v.position[0][1] = coordinates[idx++];
                v.position[0][2] = coordinates[idx];

                f.vertices[i] = v;

                face_cnt++;
            }
        }
    }

    /**
     * Set the control mess based on the 2D coordinate array. The array is
     * structured as [vertexIndex][x, y, z].
     *
     * @param coordinates The coordinate values to put into the calculation
     * @param faceIndexes The list of indexes used by the faces
     * @param faceIndexCounts The number of faceIndex values to consume for
     *     each face
     * @param numFaces The number of faces to read out of the faceIndexCounts
     *     array
     * @param vertexFlags Array the same length as the coordinates, that contains
     *     a flag for every vertex describing it's disposition. The valid values
     *     are described in {@link SubdivisionTypes}.
     */
    public void setControlMesh(float[][] coordinates,
                               int[] faceIndexes,
                               int[] faceIndexCounts,
                               int numFaces,
                               int[] vertexFlags)
    {
    }

    /**
     * Add a sector definition to the surface.
     *
     * @param face The index of the face to remove the sector from
     * @param vertex The vertex number in that face
     * @throws IllegalArgumentException The vertex number is out of the range of
     *   valid vertices for this face
     */
    public void addSector(int face,
                          int vertex,
                          int tag,
                          float flatness,
                          float theta,
                          float[] normal,
                          float normalT)
        throws IllegalArgumentException
    {
        Face f = (Face)faces.get(face);

        if(f == null)
            throw new IllegalArgumentException("Unknown face index");

        if(vertex < 0 || vertex >= f.numVertex)
            throw new IllegalArgumentException("Vertex out of range for face");

        f.addSector(vertex, tag, flatness, theta, normal, normalT);
    }

    /**
     * Remove the a sector from the given face/vertex combo.
     *
     * @param face The index of the face to remove the sector from
     * @param vertex The vertex number in that face
     * @throws IllegalArgumentException The vertex number is out of the range of
     *   valid vertices for this face
     */
    public void removeSector(int face, int vertex)
    {
        Face f = (Face)faces.get(face);

        if(f == null)
            throw new IllegalArgumentException("Unknown face index");

        if(vertex < 0 || vertex >= f.numVertex)
            throw new IllegalArgumentException("Vertex out of range for face");

        f.removeSector(vertex);
    }
}
