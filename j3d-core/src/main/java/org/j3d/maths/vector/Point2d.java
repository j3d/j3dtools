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
 * Represents a location in 2D space.
 *
 * @author justin
 */
public class Point2d
{
    /** The X coordinate of the location */
    public double x;

    /** The Y coordinate of the location */
    public double y;

    // ---- Methods defined by Object ----------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Point2d))
            return false;

        Point2d other = (Point2d)o;

        return other.x == x && other.y == y;
    }

    @Override
    public int hashCode()
    {
        long bits = Double.doubleToLongBits(x);
        bits += 31L * bits + Double.doubleToLongBits(y);

        return (int)(bits ^ (bits >> 32));
    }

    // ---- Local Methods ----------------------------------------------------

    /**
     * Convenience method to set all the fields at once
     *
     * @param px The x coordinate to set
     * @param py The z coordinate to set
     */
    public void set(double px, double py)
    {
        x = px;
        y = py;
    }

    /**
     * Convenience method to set all the fields at once
     *
     * @param pt The source point to get values from
     */
    public void set(Point2d pt)
    {
        x = pt.x;
        y = pt.y;
    }

    /**
     * Calculate the linear euclidean distance from this point to the supplied
     * point. If the supplied point is null, throws an exception.
     *
     * @param point The other point to find the distance to
     * @return A positive distance number
     */
    public double distance(Point2d point)
    {
        if(point == null)
            throw new IllegalArgumentException("Target point cannot be null");

        double dx = x - point.x;
        double dy = y - point.y;

        return Math.sqrt(dx * dx + dy * dy);
    }
}
