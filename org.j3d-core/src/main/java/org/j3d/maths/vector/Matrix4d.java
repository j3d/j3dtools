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
}
