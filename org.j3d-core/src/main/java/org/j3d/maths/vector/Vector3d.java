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
 * Represents a direction vector in 3D space.
 *
 * @author justin
 */
public class Vector3d
{
    /** The X coordinate of the direction */
    public double x;

    /** The X coordinate of the direction */
    public double y;

    /** The X coordinate of the direction */
    public double z;

    // ---- Methods defined by Object ----------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Vector3d))
            return false;

        Vector3d other = (Vector3d)o;

        return other.x == x && other.y == y && other.z == z;
    }

    @Override
    public int hashCode()
    {
        long bits = Double.doubleToLongBits(x);
        bits += 31L * bits + Double.doubleToLongBits(y);
        bits += 31L * bits + Double.doubleToLongBits(z);

        return (int)(bits ^ (bits >> 32));
    }


    // ---- Local Methods ----------------------------------------------------

    /**
     * Convenience method to set all the fields at once
     *
     * @param px The x coordinate to set
     * @param py The z coordinate to set
     * @param pz The y coordinate to set
     */
    public void set(double px, double py, double pz)
    {
        x = px;
        y = py;
        z = pz;
    }

    /**
     * Normalise this vector in place. If the length is zero, ignores
     * the request and leaves it as zero.
     */
    public void normalise()
    {
        double d = x * x + y * y + z * z;

        if(d != 0.0)
        {
            double l = 1 / Math.sqrt(d);

            x *= l;
            y *= l;
            z *= l;
        }
    }

    /**
     * Returns the dot product of this vector with the supplied vector.
     * If the input is null, generates an exception.
     *
     * @param v1 The source vector to calculate the dot product with
     * @return this dot v1
     */
    public double dot(Vector3d v1)
    {
        if(v1 == null)
            throw new IllegalArgumentException("Comparison vector cannot be null in dot product");

        return x * v1.x + y * v1.y + z * v1.z;
    }

    /**
     * Set this vector to the the cross product of the two input vectors
     * - v1 x v2. If either parameter is null, generate an exception.
     *
     * @param v1 The first vector to include in the cross product
     * @param v2 The second vector to include in the cross product
     */
    public void cross(Vector3d v1, Vector3d v2)
    {
        if(v1 == null)
            throw new IllegalArgumentException("First vector cannot be null in cross product");

        if(v2 == null)
            throw new IllegalArgumentException("Second vector cannot be null in cross product");

        x = v1.y * v2.z - v1.z * v2.y;
        y = v1.x * v2.z - v1.z * v2.x;
        z = v1.x * v2.y - v1.y * v2.x;
    }
}
