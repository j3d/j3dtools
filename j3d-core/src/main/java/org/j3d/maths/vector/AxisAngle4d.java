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
 * Represents an axis with an angular rotation about it.
 *
 * @author justin
 */
public class AxisAngle4d
{
    /** The X coordinate of the direction */
    public double x;

    /** The Y coordinate of the direction */
    public double y;

    /** The Z coordinate of the direction */
    public double z;

    /** The W coordinate of the direction */
    public double angle;

    // ---- Methods defined by Object ----------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof AxisAngle4d))
            return false;

        AxisAngle4d other = (AxisAngle4d)o;

        return other.x == x && other.y == y && other.z == z && other.angle == angle;
    }

    @Override
    public int hashCode()
    {
        long bits = Double.doubleToLongBits(x);
        bits += 31L * bits + Double.doubleToLongBits(y);
        bits += 31L * bits + Double.doubleToLongBits(z);
        bits += 31L * bits + Double.doubleToLongBits(angle);

        return (int)(bits ^ (bits >> 32));
    }

    // ---- Local Methods ----------------------------------------------------

    /**
     * Convenience method to set all the fields at once. This allows for setting of
     * non-normalised axis vector. The axis can be normalised by calling the
     * {@link #normalise()} method.
     *
     * @param px The x coordinate to set
     * @param py The z coordinate to set
     * @param pz The y coordinate to set
     * @param pa The angle amount to set
     */
    public void set(double px, double py, double pz, double pa)
    {
        x = px;
        y = py;
        z = pz;
        angle = pa;
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
}
