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
 * Represents a direction vector in 3D space with the normalised W component.
 *
 * @author justin
 */
public class Vector4d
{
    /** The X coordinate of the direction */
    public double x;

    /** The Y coordinate of the direction */
    public double y;

    /** The Z coordinate of the direction */
    public double z;

    /** The W coordinate of the direction */
    public double w;

    // ---- Methods defined by Object ----------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Vector4d))
            return false;

        Vector4d other = (Vector4d)o;

        return other.x == x && other.y == y && other.z == z && other.w == w;
    }

    @Override
    public int hashCode()
    {
        long bits = Double.doubleToLongBits(x);
        bits += 31L * bits + Double.doubleToLongBits(y);
        bits += 31L * bits + Double.doubleToLongBits(z);
        bits += 31L * bits + Double.doubleToLongBits(w);

        return (int)(bits ^ (bits >> 32));
    }

    // ---- Local Methods ----------------------------------------------------

    /**
     * Convenience method to set all the fields at once
     *
     * @param px The x coordinate to set
     * @param py The z coordinate to set
     * @param pz The y coordinate to set
     * @param pw The w component to set
     */
    public void set(double px, double py, double pz, double pw)
    {
        x = px;
        y = py;
        z = pz;
        w = pw;
    }

    /**
     * Normalise this vector in place. If the length is zero, ignores
     * the request and leaves it as zero.
     */
    public void normalise()
    {
        double d = x * x + y * y + z * z + w * w;

        if(d != 0.0)
        {
            double l = 1 / Math.sqrt(d);

            x *= l;
            y *= l;
            z *= l;
            w *= l;
        }
    }

    /**
     * Returns the dot product of this vector with the supplied vector.
     * If the input is null, generates an exception.
     *
     * @param v1 The source vector to calculate the dot product with
     * @return this dot v1
     */
    public double dot(Vector4d v1)
    {
        if(v1 == null)
            throw new IllegalArgumentException("Comparison vector cannot be null in dot product");

        return x * v1.x + y * v1.y + z * v1.z + w * v1.w;
    }
}
