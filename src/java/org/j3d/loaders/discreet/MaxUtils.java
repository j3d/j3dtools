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
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

// Local imports
import org.j3d.io.BlockDataInputStream;

/**
 * Utility class for working on the parsed 3DS objects to create additional
 * data structures.
 * <p>
 *
 * This class can be used to take the mesh structure and generate appropriate
 * normals, bi normals and tangent space information.
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class MaxUtils
{
    /** Global set of face normals for reuse */
    private float[] faceNormals;

    /** Global set of vertex counts for reuse */
    private int[] vertexCounts;

    /** Global set of vertex face lookups for reuse */
    private int[][] vertexFaces;

    /**
     * Do all the parsing work. Convenience method for all meshes to call
     * internally the <code>calcNormals()</code>. Binormal and tangent space
     * information are not calculated.
     *
     * @param mesh The mesh to process for normals
     */
    public void calcAllNormals(ObjectMesh mesh)
    {
        // once parsed, generate all the normals etc
        for(int i = 0; i < mesh.numBlocks; i++)
        {
            ObjectBlock block = mesh.blocks[i];
            for(int j = 0; j < block.numMeshes; j++)
                calcNormals(block.meshes[j]);
        }
    }

    // Generic internal methods.

    /**
     * Calculate the binormals tangents for a given tri-mesh. Will use the
     * smoothgroup and normal information, if present to make smooth or
     * hard edges for each face.
     */
    public void calcBiNormalsAndTangents(TriangleMesh mesh)
    {
        float[] tangent_face = new float[mesh.numFaces * 3];
        float[] face_binormals = new float[mesh.numFaces * 3];

        if((vertexCounts == null) || vertexCounts.length < mesh.numVertices)
            vertexCounts = new int[mesh.numVertices];

        if((vertexFaces == null) || vertexFaces.length < mesh.numVertices)
            vertexFaces = new int[mesh.numVertices][];

        mesh.tangents = new float[mesh.numFaces * 9];
        mesh.binormals = new float[mesh.numFaces * 9];

        int[] face = mesh.faces;
        int[] sgroup = mesh.smoothgroups;
        float[] vertex = mesh.vertices;
        float[] tex_coords = mesh.texCoords;
        float[] tangent = mesh.tangents;
        float[] binormal = mesh.binormals;

        if(tex_coords == null)
        {
            mesh.texCoords = new float[mesh.numVertices * 2];
            tex_coords = mesh.texCoords;
        }

        for(int i = 0; i < mesh.numFaces; i++)
        {
            int j = i * 3;
            int v0 = face[j + 0];
            int v1 = face[j + 1];
            int v2 = face[j + 2];
            vertexCounts[v0]++;
            vertexCounts[v1]++;
            vertexCounts[v2]++;

            float e0_x = vertex[v1 * 3] - vertex[v0 * 3];
            float e0_y = vertex[v1 * 3 + 1] - vertex[v0 * 3 + 1];
            float e0_z = vertex[v1 * 3 + 2] - vertex[v0 * 3 + 2];

            float e1_x = vertex[v0 * 3] - vertex[v2 * 3];
            float e1_y = vertex[v0 * 3 + 1] - vertex[v2 * 3 + 1];
            float e1_z = vertex[v0 * 3 + 2] - vertex[v2 * 3 + 2];

            float cp_x = e0_y * e1_z - e0_z * e1_y;
            float cp_y = e0_z * e1_x - e0_x * e1_z;
            float cp_z = e0_x * e1_y - e0_y * e1_x;

            e0_y = tex_coords[v1 * 2] - tex_coords[v0 * 2];
            e0_z = tex_coords[v1 * 2 + 1] - tex_coords[v0 * 2 + 1];

            e1_y = tex_coords[v0 * 2] - tex_coords[v2 * 2];
            e1_z = tex_coords[v0 * 2 + 1] - tex_coords[v2 * 2 + 1];

            for(int k = 0; k < 3; k++)
            {
                e0_x = vertex[v1 * 3 + k] - vertex[v0 * 3 + k];
                e1_x = vertex[v2 * 3 + k] - vertex[v0 * 3 + k];

                cp_x = e0_y * e1_z - e0_z * e1_y;
                cp_y = e0_z * e1_x - e0_x * e1_z;
                cp_z = e0_x * e1_y - e0_y * e1_x;

                tangent_face[i * 3 + k] = -cp_y / cp_x;
                face_binormals[i * 3 + k] = -cp_z / cp_x;
            }

            float x = tangent_face[i * 3];
            float y = tangent_face[i * 3 + 1];
            float z = tangent_face[i * 3 + 2];
            float d = x * x + y * y + z * z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                tangent_face[i * 3] *= d;
                tangent_face[i * 3 + 1] *= d;
                tangent_face[i * 3 + 2] *= d;
            }

            x = face_binormals[i * 3];
            y = face_binormals[i * 3 + 1];
            z = face_binormals[i * 3 + 2];
            d = x * x + y * y + z * z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                face_binormals[i * 3] *= d;
                face_binormals[i * 3 + 1] *= d;
                face_binormals[i * 3 + 2] *= d;
            }

            float n_x = tangent_face[i * 3 + 1] * face_binormals[i * 3 + 2] -
                        tangent_face[i * 3 + 2] * face_binormals[i * 3 + 1];
            float n_y = tangent_face[i * 3 + 2] * face_binormals[i * 3] -
                        tangent_face[i * 3] * face_binormals[i * 3 + 2];
            float n_z = tangent_face[i * 3] * face_binormals[i * 3 + 1] -
                        tangent_face[i * 3 + 1] * face_binormals[i * 3];

            d = n_x * n_x + n_y * n_y + n_z * n_z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                n_x *= d;
                n_y *= d;
                n_z *= d;
            }

            face_binormals[i * 3] = n_y * tangent_face[i * 3 + 2] -
                                   n_z * tangent_face[i * 3 + 1];

            face_binormals[i * 3 + 1] = n_z * tangent_face[i * 3] -
                                       n_x * tangent_face[i * 3 + 2];
            face_binormals[i * 3 + 2] = n_x * tangent_face[i * 3 + 1] -
                                       n_y * tangent_face[i * 3];
        }

        for(int i = 0; i < mesh.numVertices; i++)
        {
            vertexFaces[i] = new int[vertexCounts[i] + 1];
            vertexFaces[i][0] = vertexCounts[i];
        }

        for(int i = 0; i < mesh.numFaces; i++)
        {
            int j = i * 3;
            int v0 = face[j + 0];
            int v1 = face[j + 1];
            int v2 = face[j + 2];
            vertexFaces[v0][vertexCounts[v0]--] = i;
            vertexFaces[v1][vertexCounts[v1]--] = i;
            vertexFaces[v2][vertexCounts[v2]--] = i;
        }

        boolean do_smooth = (sgroup != null);

        for(int i = 0; i < mesh.numFaces; i++)
        {
            int j = i * 3;
            int v0 = face[j + 0];
            int v1 = face[j + 1];
            int v2 = face[j + 2];

            for(int k = 1; k <= vertexFaces[v0][0]; k++)
            {
                int l = vertexFaces[v0][k];
                if(l == i || (do_smooth && ((sgroup[i] & sgroup[l]) != 0)))
                {
                    int p1 = j * 3;
                    int l1 = l * 3;

                    tangent[p1] += tangent_face[l1];
                    tangent[p1 + 1] += tangent_face[l1 + 1];
                    tangent[p1 + 2] += tangent_face[l1 + 2];

                    binormal[p1] += face_binormals[l1];
                    binormal[p1 + 1] += face_binormals[l1 + 1];
                    binormal[p1 + 2] += face_binormals[l1 + 2];
                }
            }

            for(int k = 1; k <= vertexFaces[v1][0]; k++)
            {
                int l = vertexFaces[v1][k];
                if(l == i || (do_smooth && ((sgroup[i] & sgroup[l]) != 0)))
                {
                    int p1 = (j + 1) * 3;
                    int l1 = l * 3;

                    tangent[p1] += tangent_face[l1];
                    tangent[p1 + 1] += tangent_face[l1 + 1];
                    tangent[p1 + 2] += tangent_face[l1 + 2];

                    binormal[p1] += face_binormals[l1];
                    binormal[p1 + 1] += face_binormals[l1 + 1];
                    binormal[p1 + 2] += face_binormals[l1 + 2];
                }
            }

            for(int k = 1; k <= vertexFaces[v2][0]; k++)
            {
                int l = vertexFaces[v2][k];
                if(l == i || (do_smooth && ((sgroup[i] & sgroup[l]) != 0)))
                {
                    int p1 = (j + 2) * 3;
                    int l1 = l * 3;

                    tangent[p1] += tangent_face[l1];
                    tangent[p1 + 1] += tangent_face[l1 + 1];
                    tangent[p1 + 2] += tangent_face[l1 + 2];

                    binormal[p1] += face_binormals[l1];
                    binormal[p1 + 1] += face_binormals[l1 + 1];
                    binormal[p1 + 2] += face_binormals[l1 + 2];
                }
            }
        }

        int num_calc = mesh.numFaces * 3;
        for(int i = 0; i < num_calc; i++)
        {
            float x = tangent[i * 3];
            float y = tangent[i * 3 + 1];
            float z = tangent[i * 3 + 2];
            float d = x * x + y * y + z * z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                tangent[i * 3] *= d;
                tangent[i * 3 + 1] *= d;
                tangent[i * 3 + 2] *= d;
            }
        }

        for(int i = 0; i < num_calc; i++)
        {
            float x = binormal[i * 3];
            float y = binormal[i * 3 + 1];
            float z = binormal[i * 3 + 2];
            float d = x * x + y * y + z * z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                binormal[i * 3] *= d;
                binormal[i * 3 + 1] *= d;
                binormal[i * 3 + 2] *= d;
            }
        }
    }

    /**
     * Calculate the normals for a given tri-mesh. Will use the smoothgroup
     * information, if present to make smooth or hard edges for each face.
     *
     * @param mesh The mesh to use the data from
     */
    public void calcNormals(TriangleMesh mesh)
    {
        if((faceNormals == null) || faceNormals.length < mesh.numFaces * 3)
            faceNormals = new float[mesh.numFaces * 3];

        if((vertexCounts == null) || vertexCounts.length < mesh.numVertices)
            vertexCounts = new int[mesh.numVertices];

        if((vertexFaces == null) || vertexFaces.length < mesh.numVertices)
            vertexFaces = new int[mesh.numVertices][];

        if((mesh.normals == null) || (mesh.normals.length < mesh.numFaces * 9))
            mesh.normals = new float[mesh.numFaces * 9];

        float[] normal = mesh.normals;
        int[] face = mesh.faces;
        float[] vertex = mesh.vertices;
        int[] sgroup = mesh.smoothgroups;


        // find all the contributing faces for this set.
        for(int i = 0; i < mesh.numFaces; i++)
        {
            int v0 = face[i * 3];
            int v1 = face[i * 3 + 1];
            int v2 = face[i * 3 + 2];
            vertexCounts[v0]++;
            vertexCounts[v1]++;
            vertexCounts[v2]++;

            float e0_x = vertex[v1 * 3] - vertex[v0 * 3];
            float e0_y = vertex[v1 * 3 + 1] - vertex[v0 * 3 + 1];
            float e0_z = vertex[v1 * 3 + 2] - vertex[v0 * 3 + 2];

            float e1_x = vertex[v0 * 3] - vertex[v2 * 3];
            float e1_y = vertex[v0 * 3 + 1] - vertex[v2 * 3 + 1];
            float e1_z = vertex[v0 * 3 + 2] - vertex[v2 * 3 + 2];

            float cp_x = e0_y * e1_z - e0_z * e1_y;
            float cp_y = e0_z * e1_x - e0_x * e1_z;
            float cp_z = e0_x * e1_y - e0_y * e1_x;

            float d = cp_x * cp_x + cp_y * cp_y + cp_z * cp_z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                faceNormals[i * 3] = cp_x * d;
                faceNormals[i * 3 + 1] = cp_y * d;
                faceNormals[i * 3 + 2] = cp_z * d;
            }
        }

        for(int i = 0; i < mesh.numVertices; i++)
        {
            if(vertexFaces[i] == null ||
               vertexFaces[i].length < vertexCounts[i] + 1)
                vertexFaces[i] = new int[vertexCounts[i] + 1];

            vertexFaces[i][0] = vertexCounts[i];
        }

        for(int i = 0; i < mesh.numFaces; i++)
        {
            int v0 = face[i * 3];
            int v1 = face[i * 3 + 1];
            int v2 = face[i * 3 + 2];
            vertexFaces[v0][vertexCounts[v0]--] = i;
            vertexFaces[v1][vertexCounts[v1]--] = i;
            vertexFaces[v2][vertexCounts[v2]--] = i;
        }

        boolean do_smooth = (sgroup != null);

        for(int i = 0; i < mesh.numFaces; i++)
        {
            int v0 = face[i * 3];
            int v1 = face[i * 3 + 1];
            int v2 = face[i * 3 + 2];

            for(int k = 1; k <= vertexFaces[v0][0]; k++)
            {
                int l = vertexFaces[v0][k];
                if(l == i || (do_smooth && ((sgroup[i] & sgroup[l]) != 0)))
                {
                    int p1 = i * 3 * 3;
                    int l1 = l * 3;

                    normal[p1] += faceNormals[l1];
                    normal[p1 + 1] += faceNormals[l1 + 1];
                    normal[p1 + 2] += faceNormals[l1 + 2];
                }
            }

            for(int k = 1; k <= vertexFaces[v1][0]; k++)
            {
                int l = vertexFaces[v1][k];
                if(l == i || (do_smooth && ((sgroup[i] & sgroup[l]) != 0)))
                {
                    int p1 = (i * 3 + 1) * 3;
                    int l1 = l * 3;

                    normal[p1] += faceNormals[l1];
                    normal[p1 + 1] += faceNormals[l1 + 1];
                    normal[p1 + 2] += faceNormals[l1 + 2];
                }
            }

            for(int k = 1; k <= vertexFaces[v2][0]; k++)
            {
                int l = vertexFaces[v2][k];
                if(l == i || (do_smooth && ((sgroup[i] & sgroup[l]) != 0)))
                {
                    int p1 = (i * 3 + 2) * 3;
                    int l1 = l * 3;

                    normal[p1] += faceNormals[l1];
                    normal[p1 + 1] += faceNormals[l1 + 1];
                    normal[p1 + 2] += faceNormals[l1 + 2];
                }
            }
        }

        int num_calc = mesh.numFaces * 3;
        for(int i = 0; i < num_calc; i++)
        {
            float x = normal[i * 3];
            float y = normal[i * 3 + 1];
            float z = normal[i * 3 + 2];
            float d = x * x + y * y + z * z;

            if(d != 0)
            {
                d = 1 / (float)Math.sqrt(d);
                normal[i * 3] *= d;
                normal[i * 3 + 1] *= d;
                normal[i * 3 + 2] *= d;
            }
        }
    }
}
