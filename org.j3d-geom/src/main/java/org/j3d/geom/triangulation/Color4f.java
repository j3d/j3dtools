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

package org.j3d.geom.triangulation;

/**
 * Represents a location in RGBA Colour in floating point format.
 *
 * @author justin
 */
public class Color4f
{
    /** The X coordinate of the location */
    public float x;

    /** The Y coordinate of the location */
    public float y;

    /** The Z coordinate of the location */
    public float z;

    /** The Z coordinate of the location */
    public float w;

    // ---- Methods defined by Object ----------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Color4f))
            return false;

        Color4f other = (Color4f)o;

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
     */
    public void set(float px, float py, float pz, float pw)
    {
        x = px;
        y = py;
        z = pz;
        w = pw;
    }

    /**
     * Convenience method to set all the fields at once
     *
     * @param pt The source point to copy data from
     */
    public void set(Color4f pt)
    {
        x = pt.x;
        y = pt.y;
        z = pt.z;
        w = pt.w;
    }
}
