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
 * Represents a quaternion.
 *
 * {@see http://en.wikipedia.org/wiki/Quaternion}
 *
 * @author justin
 */
public class Quat4d
{
    /** Number very close to zero to work out if we're almost there for divzero avoiddance */
    private static final double EPSILON = 0.0000001;

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
        if(!(o instanceof Quat4d))
            return false;

        Quat4d other = (Quat4d)o;

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
     * Convenience method to set the quaternion from an axis angle set.
     * Conversion based on the standard routines documented everywhere,
     * such as the above wikipedia page.
     *
     * @param aa The axis angle to build the quaternion from
     */
    public void set(AxisAngle4d aa)
    {
        double d = Math.sqrt(aa.x * aa.x + aa.y * aa.y + aa.z * aa.z);

        if(d < EPSILON)
        {
            x = 0;
            y = 0;
            z = 0;
            angle = 0;
        }
        else
        {
            d = 1 / d;
            double half_angle = Math.sin(aa.angle * 0.5);

            x = aa.x * d * half_angle;
            y = aa.y * d * half_angle;
            z = aa.z * d * half_angle;
            angle = Math.cos(aa.angle * 0.5);
        }
    }

    /**
     * Get the value of this quaternion as an axis-angle value.
     *
     * @param aa The axis angle object to put the converted values into
     */
    public void get(AxisAngle4d aa)
    {
        double d = x * x + y * y + z * z;

        if(d > EPSILON)
        {
            d = Math.sqrt(d);
            double inv_d = 1 / d;

            aa.x = x * inv_d;
            aa.y = y * inv_d;
            aa.z = z * inv_d;
            aa.angle = 2 * Math.atan2(d, angle);
        }
        else
        {
            aa.x = 0.0f;
            aa.y = 1.0f;
            aa.z = 0.0f;
            aa.angle = 0.0f;
        }
    }

    /**
     * Negate the quaternion in place.
     */
    public void negate()
    {
        x = -x;
        y = -y;
        z = -z;
        angle = -angle;
    }

    /**
     * Normalise this vector in place. If the length is zero, ignores
     * the request and leaves it as zero.
     */
    public void normalise()
    {
        double d = x * x + y * y + z * z;

        if(Math.abs(d) > EPSILON)
        {
            double l = 1 / Math.sqrt(d);

            x *= l;
            y *= l;
            z *= l;
        }
    }

    /**
     * Performs a great circle interpolation between q1 and q2 quaternions
     * and places the result into this quaternion.
     *
     * @param q1 the initial quaternion
     * @param q2 the final quaternion
     * @param alpha the alpha angle interpolation parameter. 0 gives q1 as a result
     *    1.0 gives q2 as a result
     */
    public void interpolate(Quat4d q1, Quat4d q2, double alpha)
    {
        if(q1 == null)
            throw new IllegalArgumentException("First quaternion cannot be null");

        if(q2 == null)
            throw new IllegalArgumentException("Second quaternion cannot be null");

        // From "Advanced Animation and Rendering Techniques"
        // by Watt and Watt pg. 364, function as implemented appeared to be
        // incorrect.  Fails to choose the same quaternion for the double
        // covering. Resulting in change of direction for rotations.
        // Fixed function to negate the first quaternion in the case that the
        // dot product of q1 and this is negative. Second case was not needed.
        double dot = q2.x * q1.x + q2.y * q1.y + q2.z * q1.z + q2.angle * q1.angle;

        if(dot < 0)
        {
            // negate quaternion
            q1.x = -q1.x;
            q1.y = -q1.y;
            q1.z = -q1.z;
            q1.angle = -q1.angle;
            dot = -dot;
        }

        double s1;
        double s2;

        if((1.0 - dot) > EPSILON)
        {
            double cosdot = Math.acos(dot);
            double sincosdot = Math.sin(cosdot);
            s1 = Math.sin((1.0 - alpha) * cosdot) / sincosdot;
            s2 = Math.sin(alpha * cosdot) / sincosdot;
        }
        else
        {
            s1 = 1.0 - alpha;
            s2 = alpha;
        }

        x = s1 * q1.x + s2 * q2.x;
        y = s1 * q1.y + s2 * q2.y;
        z = s1 * q1.z + s2 * q2.z;
        angle = s1 * q1.angle + s2 * q2.angle;
    }
}
