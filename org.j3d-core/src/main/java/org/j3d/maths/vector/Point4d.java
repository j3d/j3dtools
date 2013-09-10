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
 * Represents a location in 3D space.
 *
 * @author justin
 */
public class Point4d
{
    /** The X coordinate of the location */
    public double x;

    /** The Y coordinate of the location */
    public double y;

    /** The Z coordinate of the location */
    public double z;

    /** The W coordinate of the direction */
    public double w;

    // ---- Methods defined by Object ----------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Point4d))
            return false;

        Point4d other = (Point4d)o;

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
}
