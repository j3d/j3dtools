/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util;

// External imports
// None

// Local imports
// None

/**
 * An set of utility functions for taking basic triangle information and
 * returning derived information that is useful for rendering.
 * <P>
 *
 * The tangent generation code is derived from Eric Lengyel's
 * <i>Computing Tangent Space Basis Vectors for an Arbitrary Mesh</i>
 * <a href="http://www.terathon.com/code/tangent.html">
 * http://www.terathon.com/code/tangent.html</a>
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <p>
 * <ul>
 * <li>nullTexCoordMsg: The tangent array parameter was null</li>
 * <li>nullNormalsMsg: The tangent array parameter was null</li>
 * <li>nullCoordsMsg: The tangent array parameter was null</li>
 * <li>nullIndexMsg: The tangent array parameter was null</li>
 * <li>nullTangentMsg: The tangent array parameter was null</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class TriangleUtils
{
    /** The message string the user didn't provide texture coordinates */
    private static final String MISSING_TEXCOORD_PROP =
        "org.j3d.util.interpolator.TriangleUtils.nullTexCoordMsg";

    /** The message string the user didn't provide normals */
    private static final String MISSING_NORMALS_PROP =
        "org.j3d.util.interpolator.TriangleUtils.nullNormalsMsg";

    /** The message string the user didn't provide coordinates */
    private static final String MISSING_COORDS_PROP =
        "org.j3d.util.interpolator.TriangleUtils.nullCoordsMsg";

    /** The message string the user didn't provide coord indices */
    private static final String MISSING_INDEX_PROP =
        "org.j3d.util.interpolator.TriangleUtils.nullIndexMsg";

    /** The message string the user didn't provide tangent */
    private static final String MISSING_TANGENT_PROP =
        "org.j3d.util.interpolator.TriangleUtils.nullTangentMsg";

    /**
     * Generate tangent space vectors and Binormals (Bitangents). Assumes
     * that per-vertex normals are provided and it is index triangles.
     *
     * @param numTriangles The number of triangles to process from the
     *   index list - assuming indexed triangles and nothing more fancy
     *   like strips or fans
     * @param indices The list of vertex indices where each i is the
     *   index, not multiplying in the 3x for flattened coords
     * @param coords The list of coordinates to process as a flat list
     * @param normals The list of normals, one per vertex, flattened
     * @param texCoords The list of 2D texture coordinates, one per
     *   vertex, flattened
     * @param tangents The array to put the returned tangents in, must
     *   be 4x the number of coordinates as it is provided in x,y,z,w
     *   form
     * @throws NullPointerException One of the arrays provided was
     *   null. Message contains the details of which one
     */
    public static void createTangents(int numTriangles,
                                      int[] indices,
                                      float[] coords,
                                      float[] normals,
                                      float[] texCoords,
                                      float[] tangents)
    {
        if(indices == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(MISSING_INDEX_PROP);
            throw new NullPointerException(msg);
        }

        if(coords == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(MISSING_COORDS_PROP);
            throw new NullPointerException(msg);
        }

        if(normals == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(MISSING_NORMALS_PROP);
            throw new NullPointerException(msg);
        }

        if(texCoords == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(MISSING_TEXCOORD_PROP);
            throw new NullPointerException(msg);
        }

        if(tangents == null)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg = intl_mgr.getString(MISSING_TANGENT_PROP);
            throw new NullPointerException(msg);
        }

        // Find the max index:
        int max_index = 0;
        for(int i = 0; i < numTriangles * 3; i++)
        {
            if(indices[i] > max_index)
                max_index = indices[i];
        }

        float[] tan1 = new float[max_index * 3];
        float[] tan2 = new float[max_index * 3];

        for(int i = 0; i < numTriangles; i++)
        {
            float x1 = coords[indices[i * 3 + 1] * 3] -
                       coords[indices[i * 3] * 3];
            float x2 = coords[indices[i * 3 + 2] * 3] -
                       coords[indices[i * 3] * 3];

            float y1 = coords[indices[i * 3 + 1] * 3 + 1] -
                       coords[indices[i * 3] * 3 + 1];
            float y2 = coords[indices[i * 3 + 2] * 3 + 1] -
                       coords[indices[i * 3] * 3 + 1];

            float z1 = coords[indices[i * 3 + 1] * 3 + 2] -
                       coords[indices[i * 3] * 3 + 2];
            float z2 = coords[indices[i * 3 + 2] * 3 + 2] -
                       coords[indices[i * 3] * 3 + 2];

            float s1 = texCoords[indices[i * 2 + 1] * 2] -
                       texCoords[indices[i * 2] * 2];

            float s2 = texCoords[indices[i * 2 + 2] * 2] -
                       texCoords[indices[i * 2] * 2];

            float t1 = texCoords[indices[i * 2 + 1] * 2 + 1] -
                       texCoords[indices[i * 2] * 2 + 1];

            float t2 = texCoords[indices[i * 2 + 2] * 2 + 1] -
                       texCoords[indices[i * 2] * 2 + 1];

            float r = 1.0f / (s1 * t2 - s2 * t1);

            float sdir_x = (t2 * x1 - t1 * x2) * r;
            float sdir_y = (t2 * y1 - t1 * y2) * r;
            float sdir_z = (t2 * z1 - t1 * z2) * r;

            float tdir_x = (s1 * x2 - s2 * x1) * r;
            float tdir_y = (s1 * y2 - s2 * y1) * r;
            float tdir_z = (s1 * z2 - s2 * z1) * r;

            tan1[indices[i * 3]] += sdir_x;
            tan1[indices[i * 3] + 1] += sdir_y;
            tan1[indices[i * 3] + 2] += sdir_z;

            tan2[indices[i * 3]] += tdir_x;
            tan2[indices[i * 3] + 1] += tdir_y;
            tan2[indices[i * 3] + 2] += tdir_z;
        }

        for(int i = 0; i < max_index; i++)
        {
            float t_x = tan1[i * 3];
            float t_y = tan1[i * 3 + 1];
            float t_z = tan1[i * 3 + 2];

            float n_x = normals[i * 3];
            float n_y = normals[i * 3 + 1];
            float n_z = normals[i * 3 + 2];

            // Gram-Schmidt orthogonalize
            // tangent[a].xyz = (t - n * Dot(n, t)).Normalize();

            float dot = n_x * t_x + n_y * t_y + n_z * t_z;

            float gso_x = t_x - n_x * dot;
            float gso_y = t_y - n_y * dot;
            float gso_z = t_z - n_z * dot;

            float n = gso_x * gso_x + gso_y * gso_y + gso_z * gso_z;

            if(n != 0)
            {
                n = 1 / n;

                tangents[i * 4] = gso_x * n;
                tangents[i * 4 + 1] = gso_y * n;
                tangents[i * 4 + 2] = gso_z * n;
            }
            else
            {
                // Not sure on the best thing to put here. Should hopefully
                // never be zero anyway.
                tangents[i * 4] = 0;
                tangents[i * 4 + 1] = 1;
                tangents[i * 4 + 2] = 0;
            }

            // Calculate handedness
            //tangent[a].w = (Dot(Cross(n, t), tan2[a]) < 0.0F) ? -1.0F : 1.0F;

            float c_x = n_y * t_z - n_z * t_y;
            float c_y = n_z * t_x - n_x * t_z;
            float c_z = n_x * t_y - n_y * t_x;

            dot = c_x * tan2[i * 3] +
                  c_y * tan2[i * 3 + 1] +
                  c_z * tan2[i * 3 + 2];

            tangents[i * 4 + 3] = dot < 0 ? -1 : 1;
        }
    }
}
