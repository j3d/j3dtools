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
public class Point3d
{
    /** The X coordinate of the location */
    public double x;

    /** The Y coordinate of the location */
    public double y;

    /** The Z coordinate of the location */
    public double z;

    // ---- Methods defined by Object ----------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Point3d))
            return false;

        Point3d other = (Point3d)o;

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
}
