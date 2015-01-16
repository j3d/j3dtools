/*****************************************************************************
 *                         J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// External imports
// none

// Local imports
// none

/**
 * A utility class that can be used to modify coordinate values of an item
 * of geometry.
 * <p>
 *
 * The utility class may be used as either a single shared instance or as a
 * normal class. Sometimes you have a lot of different code all wanting to
 * do similar modifications simultaneously and so having a single static-only
 * class with synchronised methods would be very bad for performance.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class CoordinateUtils
{
    /** The shared singleton instance, if needed */
    private static CoordinateUtils sharedInstance;

    /**
     * Create a default instance of the utility class.
     */
    public CoordinateUtils()
    {
    }

    /**
     * Fetch the currently shared singleton instance.
     *
     * @return The current instance
     */
    public static CoordinateUtils getSharedInstance()
    {
        if(sharedInstance == null)
            sharedInstance = new CoordinateUtils();

        return sharedInstance;
    }

    /**
     * Translate, in place, the coordintes by the given amount in each
     * direction. It assumes that the array has the coordinate values as
     * a flat array of values.
     *
     * @param coords The source coordinate array to copy
     * @param numCoords The number of valid coordinates in the array
     * @param x The amount to translate in the x axis
     * @param y The amount to translate in the y ayis
     * @param z The amount to translate in the z azis
     */
    public void translate(float[] coords,
                          int numCoords,
                          float x,
                          float y,
                          float z)
    {
        int cnt = 0;

        for(int i = 0; i < numCoords; i++)
        {
            coords[cnt++] += x;
            coords[cnt++] += y;
            coords[cnt++] += z;
        }
    }

    /**
     * Translate, in place, the coordintes by the given amount in each
     * direction. It assumes that the array has the coordinate values as
     * a flat array of values.
     *
     * @param coords The source coordinate array to copy
     * @param numCoords The number of valid coordinates in the array
     * @param x The amount to translate in the x axis
     * @param y The amount to translate in the y ayis
     * @param z The amount to translate in the z azis
     */
    public void translate(float[][] coords,
                          int numCoords,
                          float x,
                          float y,
                          float z)
    {
        for(int i = 0; i < numCoords; i++)
        {
            coords[i][0] += x;
            coords[i][1] += y;
            coords[i][2] += z;
        }
    }


    /**
     * Translate the coordintes by the given amount in each direction and
     * place them in the destination array. It assumes that the array has the
     * coordinate values as a flat array of values.
     *
     * @param srcCoords The source coordinate array to copy
     * @param numCoords The number of valid coordinates in the array
     * @param destCoords The array to copy the values into
     * @param x The amount to translate in the x axis
     * @param y The amount to translate in the y ayis
     * @param z The amount to translate in the z azis
     */
    public void translate(float[] srcCoords,
                          int numCoords,
                          float[] destCoords,
                          float x,
                          float y,
                          float z)
    {
        int cnt = 0;

        for(int i = 0; i < numCoords; i++)
        {
            destCoords[cnt] = srcCoords[cnt] + x;
            cnt++;
            destCoords[cnt] = srcCoords[cnt] + y;
            cnt++;
            destCoords[cnt] = srcCoords[cnt] + y;
            cnt++;
        }
    }

    /**
     * Translate the coordintes by the given amount in each direction and
     * place them in the destination array. It assumes that the array has the
     * coordinate values as a flat array of values.
     *
     * @param srcCoords The source coordinate array to copy
     * @param numCoords The number of valid coordinates in the array
     * @param destCoords The array to copy the values into
     * @param x The amount to translate in the x axis
     * @param y The amount to translate in the y ayis
     * @param z The amount to translate in the z azis
     */
    public void translate(float[][] srcCoords,
                          int numCoords,
                          float[][] destCoords,
                          float x,
                          float y,
                          float z)
    {
        for(int i = 0; i < numCoords; i++)
        {
            destCoords[i][0] = srcCoords[i][0] + x;
            destCoords[i][1] = srcCoords[i][1] + y;
            destCoords[i][2] = srcCoords[i][2] + y;
        }
    }
}
