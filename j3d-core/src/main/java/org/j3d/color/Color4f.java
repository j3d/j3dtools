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

package org.j3d.color;

/**
 * RGBA colour values represented as floats in the range [0,1].
 *
 * @author justin
 */
public class Color4f
{
    /** Red component of the colour */
    public float r;

    /** Green component of the colour */
    public float g;

    /** Blue component of the colour */
    public float b;

    /** Alpha component of the colour where 0 is opaque and 1 is fully transparent */
    public float a;

    // ---- Methods defined by Object ----------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Color4f))
            return false;

        Color4f other = (Color4f)o;

        return other.r == r && other.g == g && other.b == b && other.a == a;
    }

    @Override
    public int hashCode()
    {
        long bits = Float.floatToIntBits(r);
        bits += 31L * bits + Float.floatToIntBits(g);
        bits += 31L * bits + Float.floatToIntBits(b);
        bits += 31L * bits + Float.floatToIntBits(a);

        return (int)(bits ^ (bits >> 32));
    }

    // ---- Local Methods ----------------------------------------------------

    /**
     * Convenience method to set all the fields at once.
     *
     * @param pr The red component to set
     * @param pg The green component to set
     * @param pb The blue component to set
     * @param pa The alpha component to set
     */
    public void set(float pr, float pg, float pb, float pa)
    {
        r = pr;
        g = pg;
        b = pb;
        a = pa;
    }

}
