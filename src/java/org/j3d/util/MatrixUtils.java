/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.util;

// External imports
import javax.vecmath.*;

// Local imports
// none

/**
 * A utility class that performs various matrix operations on the
 * {@link javax.vecmath} package.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class MatrixUtils
{
    /**
     * Perform a LookAt camera calculation and place it in the given matrix.
     * If using this for a viewing transformation, you should invert() the
     * matrix after the call.
     *
     * @param eye The eye location
     * @param center The place to look at
     * @param up The up vector
     * @param res The result to put the calculation into
     */
    public void lookAt(Point3f eye,
                       Point3f center,
                       Vector3f up,
                       Matrix4f res)
    {
        float f_x = center.x - eye.x;
        float f_y = center.y - eye.y;
        float f_z = center.z - eye.z;

        float d = f_x * f_x + f_y * f_y + f_z * f_z;
        if(d != 0)
        {
            d = 1 / d;
            f_x *= d;
            f_y *= d;
            f_z *= d;
        }

        float up_x = up.x;
        float up_y = up.y;
        float up_z = up.z;

        d = up_x * up_x + up_y * up_y + up_z * up_z;
        if(d != 0)
        {
            d = 1 / d;
            up_x *= d;
            up_y *= d;
            up_z *= d;
        }

        float s_x = f_y * up_z - f_z * up_y;
        float s_y = f_z * up_x - f_x * up_z;
        float s_z = f_x * up_y - f_y * up_x;

        float u_x = up_y * f_z - up_z * f_y;
        float u_y = up_z * f_x - up_x * f_z;
        float u_z = up_x * f_y - up_y * f_x;

        res.m00 = s_x;
        res.m01 = u_x;
        res.m02 = -f_x;
        res.m03 = 0;

        res.m10 = s_y;
        res.m11 = u_y;
        res.m12 = -f_y;
        res.m13 = 0;

        res.m20 = s_z;
        res.m21 = u_z;
        res.m22 = -f_z;
        res.m23 = 0;

        res.m30 = 0;
        res.m31 = 0;
        res.m32 = 0;
        res.m33 = 1;
    }

    /**
     * Set the upper 3x3 matrix based on the given the euler angles.
     *
     * @param angles The set of angles to use, one per axis
     * @param mat The matrix to set the values in
     */
    public void setEuler(Vector3f angles, Matrix4f mat)
    {
        setEuler(angles.x, angles.y, angles.z, mat);
    }

    /**
     * Set the upper 3x3 matrix based on the given the euler angles.
     *
     * @param x The amount to rotate around the X axis
     * @param y The amount to rotate around the Y axis
     * @param z The amount to rotate around the Z axis
     * @param mat The matrix to set the values in
     */
    public void setEuler(float x, float y, float z, Matrix4f mat)
    {
        float a = (float)Math.cos(x);
        float b = (float)Math.sin(x);
        float c = (float)Math.cos(y);
        float d = (float)Math.sin(y);
        float e = (float)Math.cos(z);
        float f = (float)Math.sin(z);
        float a_d = a * d;
        float b_d = b * d;

        mat.m00 = c * e;
        mat.m01 = -c * f;
        mat.m02 = d;
        mat.m03 = 0;

        mat.m10 = b_d * e + a * f;
        mat.m11 = -b_d * f + a * e;
        mat.m12 = -b * c;
        mat.m13 = 0;

        mat.m20 = -a_d * e + b * f;
        mat.m21 = a_d * f + b * e;
        mat.m22 = a * c;
        mat.m23 = 0;

        mat.m30 = 0;
        mat.m31 = 0;
        mat.m33 = 1;
        mat.m32 = 0;
    }

    /**
     * Set the matrix to the rotation about the X axis by the given angle.
     *
     * @param angle The angle to rotate in radians
     * @param mat The matrix to set the values in
     */
    public void rotateX(float angle, Matrix4f mat)
    {
        float a = (float)Math.cos(angle);
        float b = (float)Math.sin(angle);

        mat.m00 = 1;
        mat.m01 = 0;
        mat.m02 = 0;
        mat.m03 = 0;

        mat.m10 = 0;
        mat.m11 = a;
        mat.m12 = -b;
        mat.m13 = 0;

        mat.m20 = 0;
        mat.m21 = b;
        mat.m22 = a;
        mat.m23 = 0;

        mat.m30 = 0;
        mat.m31 = 0;
        mat.m33 = 1;
        mat.m32 = 0;
    }

    /**
     * Set the matrix to the rotation about the Y axis by the given angle.
     *
     * @param angle The angle to rotate in radians
     * @param mat The matrix to set the values in
     */
    public void rotateY(float angle, Matrix4f mat)
    {
        float a = (float)Math.cos(angle);
        float b = (float)Math.sin(angle);

        mat.m00 = a;
        mat.m01 = 0;
        mat.m02 = b;
        mat.m03 = 0;

        mat.m10 = 0;
        mat.m11 = 1;
        mat.m12 = 0;
        mat.m13 = 0;

        mat.m20 = -b;
        mat.m21 = 0;
        mat.m22 = a;
        mat.m23 = 0;

        mat.m30 = 0;
        mat.m31 = 0;
        mat.m33 = 1;
        mat.m32 = 0;
    }

}
