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
 * RGBA colour values represented as bytes in the range [0,255].
 *
 * @author justin
 */
public class Color4b
{
    /** Red component of the colour */
    public byte r;

    /** Green component of the colour */
    public byte g;

    /** Blue component of the colour */
    public byte b;

    /** Alpha component of the colour where 0 is opaque and 1 is fully transparent */
    public byte a;

    // ---- Methods defined by Object ----------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Color4b))
            return false;

        Color4b other = (Color4b)o;

        return other.r == r && other.g == g && other.b == b && other.a == a;
    }

    @Override
    public int hashCode()
    {
        return (r << 24) + (g << 16) + (b << 8) + a;
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
    public void set(byte pr, byte pg, byte pb, byte pa)
    {
        r = pr;
        g = pg;
        b = pb;
        a = pa;
    }

}
