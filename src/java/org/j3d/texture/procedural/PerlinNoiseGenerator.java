/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.texture.procedural;

import java.util.Random;

/**
 * Computes Perlin Noise for three dimensions.
 * <p>
 *
 * The result is a continuous function that interpolates a smooth path
 * along a series random points. The function is consitent, so given
 * the same parameters, it will always return the same value. The smoothing
 * function is based on the Improving Noise paper presented at Siggraph 2002.
 *
 * Computing noise for one and two dimensions can make use of the 3D problem
 * space by just setting the un-needed dimensions to a fixed value.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class PerlinNoiseGenerator
{
    /** Default seed to use for the random number generation */
    private static final int DEFAULT_SEED = 100;

    /** Default sample size to work with */
    private static final int DEFAULT_SAMPLE_SIZE = 256;

    /** Permutation array */
    private int[] p;

    /**
     * Create a new noise creator with the default seed value
     */
    public PerlinNoiseGenerator()
    {
        this(DEFAULT_SEED);
    }

    /**
     * Create a new noise creator with the given seed value for the randomness
     *
     * @param seed The seed value to use
     */
    public PerlinNoiseGenerator(int seed)
    {
        p = new int[DEFAULT_SAMPLE_SIZE << 1];

        int i, j, k;
        Random rand = new Random(seed);

        // Calculate the table of psuedo-random coefficients.
        for(i = 0; i < DEFAULT_SAMPLE_SIZE; i++)
            p[i] = i;

        // generate the psuedo-random permutation table.
        while(--i > 0)
        {
            k = p[i];
            j = (int)(rand.nextLong() & DEFAULT_SAMPLE_SIZE);
            p[i] = p[j];
            p[j] = k;
        }
    }

    /**
     * Computes noise function for three dimensions at the point (x,y,z).
     *
     * @param x x dimension parameter
     * @param y y dimension parameter
     * @param z z dimension parameter
     * @return the noise value at the point (x, y, z)
     */
    public double noise(double x, double y, double z)
    {
        // Constraint the point to a unit cube
        int uc_x = (int)Math.floor(x) & 255;
        int uc_y = (int)Math.floor(y) & 255;
        int uc_z = (int)Math.floor(z) & 255;

        // Relative location of the point in the unit cube
        double xo = x - Math.floor(x);
        double yo = y - Math.floor(y);
        double zo = z - Math.floor(z);

        // Fade curves for x, y and z
        double u = fade(xo);
        double v = fade(yo);
        double w = fade(zo);

        // Generate a hash for each coordinate to find out where in the cube
        // it lies.
        int a =  p[uc_x] + uc_y;
        int aa = p[a] + uc_z;
        int ab = p[a + 1] + uc_z;

        int b =  p[uc_x + 1] + uc_y;
        int ba = p[b] + uc_z;
        int bb = p[b + 1] + uc_z;

        // blend results from the 8 corners based on the noise function
        double c1 = grad(p[aa], xo, yo, zo);
        double c2 = grad(p[ba], xo - 1, yo, zo);
        double c3 = grad(p[ab], xo, yo - 1, zo);
        double c4 = grad(p[bb], xo - 1, yo - 1, zo);
        double c5 = grad(p[aa + 1], xo, yo, zo - 1);
        double c6 = grad(p[ba + 1], xo - 1, yo, zo - 1);
        double c7 = grad(p[ab + 1], xo, yo - 1, zo - 1);
        double c8 = grad(p[bb + 1], xo - 1, yo - 1, zo - 1);

        return lerp(w, lerp(v, lerp(u, c1, c2), lerp(u, c3, c4)),
                       lerp(v, lerp(u, c5, c6), lerp(u, c7, c8)));
    }

    /**
     * Create a turbulent noise output based on the core noise function. This
     * uses the noise as a base function and is suitable for creating clouds,
     * marble and explosion effects. For example, a typical marble effect would
     * set the colour to be:
     * <pre>
     *    sin(point + turbulence(point) * point.x);
     * </pre>
     */
    public double turbulence(double x, double y, double z, float loF, float hiF)
    {
        double p_x = x + 123.456;
        double p_y = y;
        double p_z = z;
        double t = 0;
        double f;

        for(f = loF; f < hiF; f *= 2)
        {
            t += Math.abs(noise(p_x, p_y, p_z)) / f;

            p_x *= 2;
            p_y *= 2;
            p_z *= 2;
        }

        return t - 0.3;
    }

    private double lerp(double t, double a, double b)
    {
        return a + t * (b - a);
    }

    /**
     * Fade curve calculation which is 6t^5 - 15t^4 + 10t^3. This is the new
     * algorithm, where the old one used to be 3t^2 - 2t^3.
     *
     * @param t The t parameter to calculate the fade for
     * @return the drop-off amount.
     */
    private double fade(double t)
    {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * Calculate the gradient function based on the hash code.
     */
    private double grad(int hash, double x, double y, double z)
    {
        // Convert low 4 bits of hash code into 12 gradient directions.
        int h = hash & 15;
        double u = (h < 8 || h == 12 || h == 13) ? x : y;
        double v = (h < 4 || h == 12 || h == 13) ? y : z;

        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}


