/*
 * j3d.org Copyright (c) 2001-2013
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.maths.vector;

/**
 * Represents a standard 4x4 matrix.
 * </p>
 * Default constructor is set to all zeroes.
 *
 * @author justin
 */
public class Matrix4d
{
    /** First element of first row */
    public double m00;

    /** Second element of first row */
    public double m01;

    /** Third element of first row */
    public double m02;

    /** Fourth element of first row */
    public double m03;

    /** First element of second row */
    public double m10;

    /** Second element of second row */
    public double m11;

    /** Third element of second row */
    public double m12;

    /** Fourth element of second row */
    public double m13;

    /** First element of third row */
    public double m20;

    /** Second element of third row */
    public double m21;

    /** Third element of third row */
    public double m22;

    /** Fourth element of third row */
    public double m23;

    /** First element of fourth row */
    public double m30;

    /** Second element of fourth row */
    public double m31;

    /** Third element of fourth row */
    public double m32;

    /** Fourth element of fourth row */
    public double m33;


    // ---- Methods defined by Object ----------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Matrix4d))
            return false;

        Matrix4d other = (Matrix4d)o;

        return other.m00 == m00 && other.m01 == m01 && other.m02 == m02 && other.m03 == m03 &&
               other.m10 == m10 && other.m11 == m11 && other.m12 == m12 && other.m13 == m13 &&
               other.m20 == m20 && other.m21 == m21 && other.m22 == m22 && other.m23 == m23 &&
               other.m30 == m30 && other.m31 == m31 && other.m32 == m32 && other.m33 == m33;
    }

    @Override
    public int hashCode()
    {
        long bits = Double.doubleToLongBits(m00);
        bits += 31L * bits + Double.doubleToLongBits(m01);
        bits += 31L * bits + Double.doubleToLongBits(m02);
        bits += 31L * bits + Double.doubleToLongBits(m03);

        bits += 31L * bits + Double.doubleToLongBits(m10);
        bits += 31L * bits + Double.doubleToLongBits(m11);
        bits += 31L * bits + Double.doubleToLongBits(m12);
        bits += 31L * bits + Double.doubleToLongBits(m13);

        bits += 31L * bits + Double.doubleToLongBits(m20);
        bits += 31L * bits + Double.doubleToLongBits(m21);
        bits += 31L * bits + Double.doubleToLongBits(m22);
        bits += 31L * bits + Double.doubleToLongBits(m23);

        bits += 31L * bits + Double.doubleToLongBits(m30);
        bits += 31L * bits + Double.doubleToLongBits(m31);
        bits += 31L * bits + Double.doubleToLongBits(m32);
        bits += 31L * bits + Double.doubleToLongBits(m33);

        return (int)(bits ^ (bits >> 32));
    }

    // ---- Local Methods ----------------------------------------------------

    /**
     * Convenience method to set the matrix back to all zeroes.
     */
    public void clear()
    {
        m00 = 0;
        m01 = 0;
        m02 = 0;
        m03 = 0;

        m10 = 0;
        m11 = 0;
        m12 = 0;
        m13 = 0;

        m20 = 0;
        m21 = 0;
        m22 = 0;
        m23 = 0;

        m30 = 0;
        m31 = 0;
        m32 = 0;
        m33 = 0;
    }

    /**
     * Set the matrix to an identity matrix. Replaces any previously set values.
     */
    public void setIdentity()
    {
        m00 = 1.0;
        m01 = 0.0;
        m02 = 0.0;
        m03 = 0.0;

        m10 = 0.0;
        m11 = 1.0;
        m12 = 0.0;
        m13 = 0.0;

        m20 = 0.0;
        m21 = 0.0;
        m22 = 1.0;
        m23 = 0.0;

        m30 = 0.0;
        m31 = 0.0;
        m32 = 0.0;
        m33 = 1.0;
    }

    /**
     * Convenience method to set this matrix object with the content of another
     * matrix object. A null value is ignored.
     *
     * @param src The source matrix to set these values from
     */
    public void set(Matrix4d src)
    {
        if(src == null)
            return;

        m00 = src.m00;
        m01 = src.m01;
        m02 = src.m02;
        m03 = src.m03;

        m10 = src.m10;
        m11 = src.m11;
        m12 = src.m12;
        m13 = src.m13;

        m20 = src.m20;
        m21 = src.m21;
        m22 = src.m22;
        m23 = src.m23;

        m30 = src.m30;
        m31 = src.m31;
        m32 = src.m32;
        m33 = src.m33;
    }

    /**
     * Reset this matrix as a pure translation matrix with the given vector.
     * If the input is null, do nothing.
     *
     * @param src The input vector to set the translation to
     */
    public void set(Vector4d src)
    {
        if(src == null)
            return;

        m00 = 1.0;
        m01 = 0.0;
        m02 = 0.0;
        m03 = src.x;

        m10 = 0.0;
        m11 = 1.0;
        m12 = 0.0;
        m13 = src.y;

        m20 = 0.0;
        m21 = 0.0;
        m22 = 1.0;
        m23 = src.z;

        m30 = 0.0;
        m31 = 0.0;
        m32 = 0.0;
        m33 = src.w;
    }

    /**
     * Reset this matrix as a pure translation matrix with the given vector and
     * the w translation component set to 1.0. If the input is null, do nothing.
     *
     * @param src The input vector to set the translation to
     */
    public void set(Vector3d src)
    {
        if(src == null)
            return;

        m00 = 1.0;
        m01 = 0.0;
        m02 = 0.0;
        m03 = src.x;

        m10 = 0.0;
        m11 = 1.0;
        m12 = 0.0;
        m13 = src.y;

        m20 = 0.0;
        m21 = 0.0;
        m22 = 1.0;
        m23 = src.z;

        m30 = 0.0;
        m31 = 0.0;
        m32 = 0.0;
        m33 = 1.0;
    }

    /**
     * Reset this matrix as a pure scale matrix with the given value applied
     * across all 3 components.
     *
     * @param scale The new scale value to apply
     */
    public void set(double scale)
    {
        m00 = scale;
        m01 = 0.0;
        m02 = 0.0;
        m03 = 0.0;

        m10 = 0.0;
        m11 = scale;
        m12 = 0.0;
        m13 = 0.0;

        m20 = 0.0;
        m21 = 0.0;
        m22 = scale;
        m23 = 0.0;

        m30 = 0.0;
        m31 = 0.0;
        m32 = 0.0;
        m33 = 1.0;
    }

    /**
     * Reset this matrix as a rotational matrix with the given axis angle.
     * If the input is null, do nothing. Sets the translation components
     * back to zero.
     *
     * @param src The input axis angle definition to set the matrix as
     */
    public void set(AxisAngle4d src)
    {
        if(src == null)
            return;

        double length = Math.sqrt(src.x * src.x + src.y*src.y + src.z*src.z);
        
        if(length < 0.00005)
        {
            setIdentity();
        }
        else
        {
            length = 1.0 / length;
            double ax = src.x * length;
            double ay = src.y * length;
            double az = src.z * length;

            double sin_theta = Math.sin(src.angle);
            double cos_theta = Math.cos(src.angle);
            double t = 1.0 - cos_theta;

            double xz = ax * az;
            double xy = ax * ay;
            double yz = ay * az;

            m00 = t * ax * ax + cos_theta;
            m01 = t * xy - sin_theta * az;
            m02 = t * xz + sin_theta * ay;

            m10 = t * xy + sin_theta * az;
            m11 = t * ay * ay + cos_theta;
            m12 = t * yz - sin_theta * ax;

            m20 = t * xz - sin_theta * ay;
            m21 = t * yz + sin_theta * ax;
            m22 = t * az * az + cos_theta;

            m03 = 0.0;
            m13 = 0.0;
            m23 = 0.0;

            m30 = 0.0;
            m31 = 0.0;
            m32 = 0.0;
            m33 = 1.0;
        }
    }

    /**
     * Reset this matrix as a pure rotation matrix with the given 3x3 rotation matrix.
     *
     * @param mat The rotation matrix to use
     */
    public void set(Matrix3d mat)
    {
        if(mat == null)
            return;

        m00 = mat.m00;
        m01 = mat.m01;
        m02 = mat.m02;
        m03 = 0;

        m10 = mat.m10;
        m11 = mat.m11;
        m12 = mat.m12;
        m13 = 0;

        m20 = mat.m20;
        m21 = mat.m21;
        m22 = mat.m22;
        m23 = 0;

        m30 = 0.0;
        m31 = 0.0;
        m32 = 0.0;
        m33 = 1.0;
    }

    /**
     * Set the translation component of this matrix and leave all the rest of it
     * alone. It will return the m33 component back to a value of 1.0 assuming this
     * vector is without the w component.
     *
     * @param src The vector to set the translation to
     */
    public void setTranslation(Vector3d src)
    {
        if(src == null)
            return;

        m03 = src.x;
        m13 = src.y;
        m23 = src.z;
        m33 = 1.0;
    }

    /**
     * Set the translation component of this matrix and leave all the rest of it
     * alone. It will return the m33 component back to a value of 1.0 assuming this
     * vector is without the w component.
     *
     * @param src The vector to set the translation to
     */
    public void setTranslation(Vector4d src)
    {
        if(src == null)
            return;

        m03 = src.x;
        m13 = src.y;
        m23 = src.z;
        m33 = src.w;
    }

    /**
     * Transform the vector from input value and place it in the output. Will be
     * input safe so that input and output are the same object, it doesn't cause
     * any weird values.
     *
     * @param inVec The input vector to transform by this matrix
     * @param outVec The output vector to put the transformed result into
     * @throws IllegalArgumentException Either the input or the output is null
     */
    public void transform(Vector3d inVec, Vector3d outVec)
    {
        if(inVec == null)
            throw new IllegalArgumentException("Input vector cannot be null");

        if(outVec == null)
            throw new IllegalArgumentException("Output vector cannot be null");

        double x = m00 * inVec.x + m01 * inVec.y + m02 * inVec.z + m03;
        double y = m10 * inVec.x + m11 * inVec.y + m12 * inVec.z + m13;
        double z = m20 * inVec.x + m21 * inVec.y + m22 * inVec.z + m23;

        outVec.x = x;
        outVec.y = y;
        outVec.z = z;
    }

    /**
     * Transform the vector from input value and place it in the output, treating
     * it as a normal vector. The 4th component of the vector is treated as zero
     * for this purpose to avoid any translation effect. Will be
     * input safe so that input and output are the same object, it doesn't cause
     * any weird values.
     *
     * @param inVec The input vector to transform by this matrix
     * @param outVec The output vector to put the transformed result into
     * @throws IllegalArgumentException Either the input or the output is null
     */
    public void transformNormal(Vector3d inVec, Vector3d outVec)
    {
        if(inVec == null)
            throw new IllegalArgumentException("Input vector cannot be null");

        if(outVec == null)
            throw new IllegalArgumentException("Output vector cannot be null");

        double x = m00 * inVec.x + m01 * inVec.y + m02 * inVec.z;
        double y = m10 * inVec.x + m11 * inVec.y + m12 * inVec.z;
        double z = m20 * inVec.x + m21 * inVec.y + m22 * inVec.z;

        outVec.x = x;
        outVec.y = y;
        outVec.z = z;
    }

    /**
     * Transform the vector from input value and place it in the output. Will be
     * input safe so that input and output are the same object, it doesn't cause
     * any weird values.
     *
     * @param inVec The input vector to transform by this matrix
     * @param outVec The output vector to put the transformed result into
     * @throws IllegalArgumentException Either the input or the output is null
     */
    public void transform(Vector4d inVec, Vector4d outVec)
    {
        if(inVec == null)
            throw new IllegalArgumentException("Input vector cannot be null");

        if(outVec == null)
            throw new IllegalArgumentException("Output vector cannot be null");

        double x = m00 * inVec.x + m01 * inVec.y + m02 * inVec.z + m03 * inVec.w;
        double y = m10 * inVec.x + m11 * inVec.y + m12 * inVec.z + m13 * inVec.w;
        double z = m20 * inVec.x + m21 * inVec.y + m22 * inVec.z + m23 * inVec.w;
        double w = m30 * inVec.x + m31 * inVec.y + m32 * inVec.z + m33 * inVec.w;

        outVec.x = x;
        outVec.y = y;
        outVec.z = z;
        outVec.w = w;
    }

    /**
     * Transform the vector from input value and place it in the output. Will be
     * input safe so that input and output are the same object, it doesn't cause
     * any weird values.
     *
     * @param inPt The input vector to transform by this matrix
     * @param outPt The output vector to put the transformed result into
     * @throws IllegalArgumentException Either the input or the output is null
     */
    public void transform(Point3d inPt, Point3d outPt)
    {
        if(inPt == null)
            throw new IllegalArgumentException("Input point cannot be null");

        if(outPt == null)
            throw new IllegalArgumentException("Output point cannot be null");

        double x = m00 * inPt.x + m01 * inPt.y + m02 * inPt.z + m03;
        double y = m10 * inPt.x + m11 * inPt.y + m12 * inPt.z + m13;
        double z = m20 * inPt.x + m21 * inPt.y + m22 * inPt.z + m23;

        outPt.x = x;
        outPt.y = y;
        outPt.z = z;
    }

    /**
     * Transform the 4D point from input value and place it in the output. Will be
     * input safe so that input and output are the same object, it doesn't cause
     * any weird values.
     *
     * @param inVec The input point to transform by this matrix
     * @param outVec The output point to put the transformed result into
     * @throws IllegalArgumentException Either the input or the output is null
     */
    public void transform(Point4d inVec, Point4d outVec)
    {
        if(inVec == null)
            throw new IllegalArgumentException("Input point cannot be null");

        if(outVec == null)
            throw new IllegalArgumentException("Output point cannot be null");

        double x = m00 * inVec.x + m01 * inVec.y + m02 * inVec.z + m03 * inVec.w;
        double y = m10 * inVec.x + m11 * inVec.y + m12 * inVec.z + m13 * inVec.w;
        double z = m20 * inVec.x + m21 * inVec.y + m22 * inVec.z + m23 * inVec.w;
        double w = m30 * inVec.x + m31 * inVec.y + m32 * inVec.z + m33 * inVec.w;

        outVec.x = x;
        outVec.y = y;
        outVec.z = z;
        outVec.w = w;
    }

    /**
     * Set this matrix to the left multiplication of the two input matrices.
     * Data safe so that you can use this as one of the inputs
     *
     * @param m1 The left matrix to apply to the multiplication
     * @param m2 The right matrix to apply to the multiplication
     */
    public void mul(Matrix4d m1, Matrix4d m2)
    {
        if(m1 == null)
            throw new IllegalArgumentException("First matrix cannot be null");

        if(m2 == null)
            throw new IllegalArgumentException("Second matrix cannot be null");


        double t00 = m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20 + m1.m03 * m2.m30;
        double t01 = m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21 + m1.m03 * m2.m31;
        double t02 = m1.m00 * m2.m02 + m1.m01 * m2.m12 + m1.m02 * m2.m22 + m1.m03 * m2.m32;
        double t03 = m1.m00 * m2.m03 + m1.m01 * m2.m13 + m1.m02 * m2.m23 + m1.m03 * m2.m33;

        double t10 = m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20 + m1.m13 * m2.m30;
        double t11 = m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21 + m1.m13 * m2.m31;
        double t12 = m1.m10 * m2.m02 + m1.m11 * m2.m12 + m1.m12 * m2.m22 + m1.m13 * m2.m32;
        double t13 = m1.m10 * m2.m03 + m1.m11 * m2.m13 + m1.m12 * m2.m23 + m1.m13 * m2.m33;

        double t20 = m1.m20 * m2.m00 + m1.m21 * m2.m10 + m1.m22 * m2.m20 + m1.m23 * m2.m30;
        double t21 = m1.m20 * m2.m01 + m1.m21 * m2.m11 + m1.m22 * m2.m21 + m1.m23 * m2.m31;
        double t22 = m1.m20 * m2.m02 + m1.m21 * m2.m12 + m1.m22 * m2.m22 + m1.m23 * m2.m32;
        double t23 = m1.m20 * m2.m03 + m1.m21 * m2.m13 + m1.m22 * m2.m23 + m1.m23 * m2.m33;

        double t30 = m1.m30 * m2.m00 + m1.m31 * m2.m10 + m1.m32 * m2.m20 + m1.m33 * m2.m30;
        double t31 = m1.m30 * m2.m01 + m1.m31 * m2.m11 + m1.m32 * m2.m21 + m1.m33 * m2.m31;
        double t32 = m1.m30 * m2.m02 + m1.m31 * m2.m12 + m1.m32 * m2.m22 + m1.m33 * m2.m32;
        double t33 = m1.m30 * m2.m03 + m1.m31 * m2.m13 + m1.m32 * m2.m23 + m1.m33 * m2.m33;

        m00 = t00; m01 = t01; m02 = t02; m03 = t03;
        m10 = t10; m11 = t11; m12 = t12; m13 = t13;
        m20 = t20; m21 = t21; m22 = t22; m23 = t23;
        m30 = t30; m31 = t31; m32 = t32; m33 = t33;
    }

    /**
     * Set this matrix to the transpose of the given matrix. Will handle using this
     * as the input to allow self-transposition. If the argument is null, will
     * generate an exception.
     *
     * @param src The input matrix to generate a transpose from
     */
    public void transpose(Matrix4d src)
    {
        if(src == null)
            throw new IllegalArgumentException("Source matrix cannot be null");

        if(src == this)
        {
            double tmp = m01;
            m01 = m10;
            m10 = tmp;

            tmp = m02;
            m02 = m20;
            m20 = tmp;

            tmp = m03;
            m03 = m30;
            m30 = tmp;

            tmp = m12;
            m12 = m21;
            m21 = tmp;

            tmp = m13;
            m13 = m31;
            m31 = tmp;

            tmp = m23;
            m23 = m32;
            m32 = tmp;
        }
        else
        {
            m00 = src.m00;
            m01 = src.m10;
            m02 = src.m20;
            m03 = src.m30;

            m10 = src.m01;
            m11 = src.m11;
            m12 = src.m21;
            m13 = src.m31;

            m20 = src.m02;
            m21 = src.m12;
            m22 = src.m22;
            m23 = src.m32;

            m30 = src.m03;
            m31 = src.m13;
            m32 = src.m23;
            m33 = src.m33;
        }
    }

    /**
     * Calculate the determinant of this matrix.
     *
     * @return a non-NaN determinant for the matrix
     */
    public double determinant()
    {
        // det = a * |1, 2, 3| - b * | 0, 2, 3 | + c * | 0, 1, 3 | - d * | 0, 1, 2 |
        double retval = 0;

        retval  = m00 * (m11 * m22 * m33+ m12 * m23 * m31 + m13 * m21 * m32 - m13  *  m22 * m31 -m11 * m23 * m32 - m12 * m21 * m33);
        retval -= m01 * (m10 * m22 * m33+ m12 * m23 * m30 + m13 * m20 * m32 - m13  *  m22 * m30 -m10 * m23 * m32 - m12 * m20 * m33);
        retval += m02 * (m10 * m21 * m33+ m11 * m23 * m30 + m13 * m20 * m31 - m13  *  m21 * m30 -m10 * m23 * m31 - m11 * m20 * m33);
        retval -= m03 * (m10 * m21 * m32+ m11 * m22 * m30 + m12 * m20 * m31 - m12  *  m21 * m30 -m10 * m22 * m31 - m11 * m20 * m32);
        
        return retval;
    }
}
