/*****************************************************************************
 *                         J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
import javax.vecmath.Vector3f;

// Application specific imports
// none

/**
 * A utility class that can be used to modify or create normal values of an
 * item of geometry.
 * <p>
 *
 * The utility class may be used as either a single shared instance or as a
 * normal class. Sometimes you have a lot of different code all wanting to
 * do similar modifications simultaneously and so having a single static-only
 * class with synchronised methods would be very bad for performance.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class NormalUtils
{
    /** The shared singleton instance, if needed */
    private static NormalUtils sharedInstance;

    /** Working values for the normal generation */
    private Vector3f normal;
    private Vector3f v0;
    private Vector3f v1;

    /**
     * Create a default instance of the utility class.
     */
    public NormalUtils()
    {
        v0 = new Vector3f();
        v1 = new Vector3f();
        normal = new Vector3f();
    }

    /**
     * Fetch the currently shared singleton instance.
     *
     * @return The current instance
     */
    public NormalUtils getSharedInstance()
    {
        if(sharedInstance == null)
            sharedInstance = new NormalUtils();

        return sharedInstance;
    }

    /**
     * Negate the normals, in place. A negative normal points in the
     * opposite direction to the original value. It assumes that the
     * array has the coordinate values as a flat array of values.
     *
     * @param normals The source coordinate array to copy
     * @param numNormals The number of valid normals in the array
     */
    public void negate(float[] normals, int numNormals)
    {
        int cnt = 0;

        for(int i = 0; i < numNormals; i++)
        {
            normals[cnt] = -normals[cnt++];
            normals[cnt] = -normals[cnt++];
            normals[cnt] = -normals[cnt++];
        }
    }

    /**
     * Negate the normals, in place. A negative normal points in the
     * opposite direction to the original value. It assumes that the
     *
     * @param normals The source coordinate array to copy
     * @param numNormals The number of valid normals in the array
     */
    public void negate(float[][] normals, int numNormals)
    {
        for(int i = 0; i < numNormals; i++)
        {
            normals[i][0] = -normals[i][0];
            normals[i][1] = -normals[i][1];
            normals[i][2] = -normals[i][2];
        }
    }


    /**
     * Translate the coordintes by the given amount in each direction and
     * place them in the destination array. It assumes that the array has the
     * coordinate values as a flat array of values.
     *
     * @param srcNormals The source coordinate array to copy
     * @param numNormals The number of valid coordinates in the array
     * @param destNormals The array to copy the values into
     * @param x The amount to translate in the x axis
     * @param y The amount to translate in the y ayis
     * @param z The amount to translate in the z azis
     */
    public void negate(float[] srcNormals,
                       int numNormals,
                       float[] destNormals)
    {
        int cnt = 0;

        for(int i = 0; i < numNormals; i++)
        {
            destNormals[cnt] = -srcNormals[cnt++];
            destNormals[cnt] = -srcNormals[cnt++];
            destNormals[cnt] = -srcNormals[cnt++];
        }
    }

    /**
     * Translate the coordintes by the given amount in each direction and
     * place them in the destination array. It assumes that the array has the
     * coordinate values as a flat array of values.
     *
     * @param srcNormals The source coordinate array to copy
     * @param numNormals The number of valid coordinates in the array
     * @param destNormals The array to copy the values into
     * @param x The amount to translate in the x axis
     * @param y The amount to translate in the y ayis
     * @param z The amount to translate in the z azis
     */
    public void translate(float[][] srcNormals,
                          int numNormals,
                          float[][] destNormals)
    {
        for(int i = 0; i < numNormals; i++)
        {
            destNormals[i][0] = -srcNormals[i][0];
            destNormals[i][1] = -srcNormals[i][1];
            destNormals[i][2] = -srcNormals[i][2];
        }
    }

    /**
     * Convenience method to create a normal for the given vertex coordinates
     * and normal array. This performs a cross product of the two vectors
     * described by the middle and two end points.
     *
     * @param coords The coordinate array to read values from
     * @param p The index of the middle point
     * @param p1 The index of the first point
     * @param p2 The index of the second point
     * @param normals The array to leave the computed result in
     * @param offset The offset into the normal array to place the normal values
     */
    public void createFaceNormal(float[] coords,
                                 int p,
                                 int p1,
                                 int p2,
                                 float[] normals,
                                 int offset)
    {
        v0.x = coords[p1]     - coords[p];
        v0.y = coords[p1 + 1] - coords[p + 1];
        v0.z = coords[p1 + 2] - coords[p + 2];

        v1.x = coords[p]     - coords[p2];
        v1.y = coords[p + 1] - coords[p2 + 1];
        v1.z = coords[p + 2] - coords[p2 + 2];

        normal.cross(v0, v1);
        normal.normalize();

        normals[offset] = normal.x;
        normals[offset + 1] = normal.y;
        normals[offset + 2] = normal.z;
    }

    /**
     * Convenience method to create a normal for the given vertex coordinates
     * and normal array. This performs a cross product of the two vectors
     * described by the middle and two end points.
     *
     * @param coords The coordinate array to read values from
     * @param p The index of the middle point
     * @param p1 The index of the first point
     * @param p2 The index of the second point
     * @param normals The array to leave the computed result in
     * @param offset The offset into the normal array to place the normal values
     */
    public void createFaceNormal(float[][] coords,
                                 int p,
                                 int p1,
                                 int p2,
                                 float[][] normals,
                                 int offset)
    {
        v0.x = coords[p1][0] - coords[p][0];
        v0.y = coords[p1][1] - coords[p][1];
        v0.z = coords[p1][2] - coords[p][2];

        v1.x = coords[p][0] - coords[p2][0];
        v1.y = coords[p][1] - coords[p2][1];
        v1.z = coords[p][2] - coords[p2][2];

        normal.cross(v0, v1);
        normal.normalize();

        normals[offset][0] = normal.x;
        normals[offset][1] = normal.y;
        normals[offset][2] = normal.z;
    }

    /**
     * Create a normal based on the given vertex position, assuming that it is
     * a point in space, relative to the origin. This will create a normal that
     * points directly along the vector from the origin to the point.
     *
     * @param coords The coordinate array to read values from
     * @param p The index of the point to calculate
     * @return A temporary value containing the normal value
     * @param normals The array to leave the computed result in
     * @param offset The offset into the normal array to place the normal values
     */
    public void createRadialNormal(float[] coords,
                                   int p,
                                   float[] normals,
                                   int offset)
    {
        float x = coords[p];
        float y = coords[p + 1];
        float z = coords[p + 2];

        float mag = x * x + y * y + z * z;

        if(mag != 0.0)
        {
            mag = 1.0f / ((float) Math.sqrt(mag));
            normals[offset] = x * mag;
            normals[offset + 1] = y * mag;
            normals[offset + 2] = z * mag;
        }
        else
        {
            normals[offset] = 0;
            normals[offset + 1] = 0;
            normals[offset + 2] = 0;
        }
    }

    /**
     * Create a normal based on the given vertex position, assuming that it is
     * a point in space, relative to the origin. This will create a normal that
     * points directly along the vector from the origin to the point.
     *
     * @param coords The coordinate array to read values from
     * @param p The index of the point to calculate
     * @return A temporary value containing the normal value
     * @param normals The array to leave the computed result in
     * @param offset The offset into the normal array to place the normal values
     */
    public void createRadialNormal(float[][] coords,
                                   int p,
                                   float[][] normals,
                                   int offset)
    {
        float x = coords[p][0];
        float y = coords[p][1];
        float z = coords[p][2];

        float mag = x * x + y * y + z * z;

        if(mag != 0.0)
        {
            mag = 1.0f / ((float) Math.sqrt(mag));
            normals[offset][0] = x * mag;
            normals[offset][1] = y * mag;
            normals[offset][2] = z * mag;
        }
        else
        {
            normals[offset][0] = 0;
            normals[offset][1] = 0;
            normals[offset][2] = 0;
        }
    }

    /**
     * Create a normal based on the given vertex position, assuming that it is
     * a point in space, relative to the given point. This will create a normal
     * that points directly along the vector from the given point to the
     * coordinate.
     *
     * @param coords The coordinate array to read values from
     * @param p The index of the point to calculate
     * @param origin The origin to calculate relative to
     * @param originOffset The offset into the origin array to use
     */
    public void createRadialNormal(float[] coords,
                                   int p,
                                   float[] origin,
                                   int originOffset,
                                   float[] normals,
                                   int offset)
    {
        float x = coords[p] - origin[originOffset];
        float y = coords[p + 1] - origin[originOffset + 1];
        float z = coords[p + 2] - origin[originOffset + 2];

        float mag = x * x + y * y + z * z;

        if(mag != 0.0)
        {
            mag = 1.0f / ((float) Math.sqrt(mag));
            normals[offset] = x * mag;
            normals[offset + 1] = y * mag;
            normals[offset + 2] = z * mag;
        }
        else
        {
            normals[offset] = 0;
            normals[offset + 1] = 0;
            normals[offset + 2] = 0;
        }
    }

    /**
     * Create a normal based on the given vertex position, assuming that it is
     * a point in space, relative to the given point. This will create a normal
     * that points directly along the vector from the given point to the
     * coordinate.
     *
     * @param coords The coordinate array to read values from
     * @param p The index of the point to calculate
     * @param origin The origin to calculate relative to
     * @param originOffset The offset into the origin array to use
     */
    public void createRadialNormal(float[][] coords,
                                   int p,
                                   float[][] origin,
                                   int originOffset,
                                   float[][] normals,
                                   int offset)
    {
        float x = coords[p][0] - origin[originOffset][0];
        float y = coords[p][1] - origin[originOffset][1];
        float z = coords[p][2] - origin[originOffset][2];

        float mag = x * x + y * y + z * z;

        if(mag != 0.0)
        {
            mag = 1.0f / ((float) Math.sqrt(mag));
            normals[offset][0] = x * mag;
            normals[offset][1] = y * mag;
            normals[offset][2] = z * mag;
        }
        else
        {
            normals[offset][0] = 0;
            normals[offset][1] = 0;
            normals[offset][2] = 0;
        }
    }
}
