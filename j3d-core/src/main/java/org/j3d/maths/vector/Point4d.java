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
     * Linearly interpolates between the two points and places the result into this point:
     * <code>this = (1 - alpha) * t1 + alpha * t2</code>. All ranges of alpha are allowed,
     * meaning that this can interpolate beyond the last point in a straight line or before
     * the first point.
     *
     * @param t1 the first point to interpolate from
     * @param t2 the second point to interpolate to
     * @param alpha the alpha interpolation parameter
     */
    public void interpolate(Point4d t1, Point4d t2, double alpha)
    {
        this.x = (1 - alpha) * t1.x + alpha * t2.x;
        this.y = (1 - alpha) * t1.y + alpha * t2.y;
        this.z = (1 - alpha) * t1.z + alpha * t2.z;
        this.w = (1 - alpha) * t1.w + alpha * t2.w;
    }

}
