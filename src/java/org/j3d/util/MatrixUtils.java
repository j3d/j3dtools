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
 * @version $Revision: 1.4 $
 */
public class MatrixUtils
{
    /**
     * Construct a default instance of this class.
     */
    public MatrixUtils()
    {
    }

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
        double d = eye.x - center.x;
        double d1 = eye.y - center.y;
        double d2 = eye.z - center.z;

        double det = d * d + d1 * d1 + d2 * d2;
        if(det != 1)
        {
            det = 1 / Math.sqrt(det);
            d *= det;
            d1 *= det;
            d2 *= det;
        }

        double d4 = up.x;
        double d5 = up.y;
        double d6 = up.z;

        det = (up.x * up.x + up.y * up.y + up.z * up.z);
        if(det != 1)
        {
            det = 1 / Math.sqrt(det);
            d4 *= det;
            d5 *= det;
            d6 *= det;
        }

        double d7 = d5 * d2 - d1 * d6;
        double d8 = d6 * d - d4 * d2;
        double d9 = d4 * d1 - d5 * d;

        det = d7 * d7 + d8 * d8 + d9 * d9;

        if(det != 1)
        {
            det = 1 / Math.sqrt(det);
            d7 *= det;
            d8 *= det;
            d9 *= det;
        }

        d4 = d1 * d9 - d8 * d2;
        d5 = d2 * d7 - d * d9;
        d6 = d * d8 - d1 * d7;

        res.m00 = (float)d7;
        res.m01 = (float)d8;
        res.m02 = (float)d9;
        res.m03 = (float)(-eye.x * res.m00 + -eye.y * res.m01 + -eye.z * res.m02);

        res.m10 = (float)d4;
        res.m11 = (float)d5;
        res.m12 = (float)d6;
        res.m13 = (float)(-eye.x * res.m10 + -eye.y * res.m11 + -eye.z * res.m12);

        res.m20 = (float)d;
        res.m21 = (float)d1;
        res.m22 = (float)d2;
        res.m23 = (float)(-eye.x * res.m20 + -eye.y * res.m21 + -eye.z * res.m22);

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
