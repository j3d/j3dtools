/*****************************************************************************
 *                        Teseract Software, LLP (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.overlay;

// Standard imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.util.ArrayList;
import java.util.List;

// Application specific imports
// none

/**
 * Utility functionality for creating parts of the overlay system.
 * <p>
 *
 * Although designed mainly for internal use, this could be useful for end
 * users too.
 *
 * @author David Yazel
 * @version $Revision: 1.2 $
 */
public class OverlayUtilities
{
    /** bit definitions for 3-component images */
    private static final int[] BITS_3COMP = {8, 8, 8};

    /** bit definitions for 4-component images */
    private static final int[] BITS_4COMP = {8, 8, 8, 8};

    /**
     * Create a buffered image that uses a 3 component colour model with the
     * option of adding an alpha component.
     *
     * @param size The overall size of the image in pixels
     * @param hasAlpha true if the image should contain an alpha channel
     * @return A matching image instance
     */
    public static BufferedImage createBufferedImage(Dimension size,
                                                    boolean hasAlpha)
    {
        int transparency = hasAlpha ?
                           Transparency.TRANSLUCENT :
                           Transparency.OPAQUE;
        int[] bits = hasAlpha ? BITS_4COMP : BITS_3COMP;

        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);

        ColorModel colorModel =
            new ComponentColorModel(colorSpace,            // Color space
                                    bits,                  // Number of bits per component
                                    hasAlpha,              // Has alpha
                                    false,                 // Alpha premultiplied
                                    transparency,          // Transparency type
                                    DataBuffer.TYPE_BYTE); // Type of transfer buffer

 /* Use this version when JDK 1.4 becomes more popular
        ColorModel colorModel =
            new ComponentColorModel(colorSpace,            // Color space
                                    hasAlpha,              // Has alpha
                                    false,                 // Alpha premultiplied
                                    transparency,          // Transparency type
                                    DataBuffer.TYPE_BYTE); // Type of transfer buffer

*/
        WritableRaster raster =
            colorModel.createCompatibleWritableRaster(size.width, size.height);

        return new BufferedImage(colorModel, // Color model
                                 raster,     // Raster
                                 false,      // Alpha premultiplied
                                 null);      // Hashtable of properties
    }

    /**
     * Subdivides an area into a closest fit set of Rectangle with sides that are
     * powers of 2. All elements will be less than max and greater than the minimum
     * value by threshhold.
     *
     * @param dimension The required total size
     * @param threshold The minimum required size
     * @param max The maximum required size
     * @return A collection of the rectangles needed to construct the total size
     */
    public static List subdivide(Dimension dimension, int threshold, int max)
    {
        return subdivide(dimension.width, dimension.height, threshold, max);
    }

    /**
     * Subdivides an area into a closest fit set of Rectangle with sides that are
     * powers of 2. All elements will be less than max and greater than the minimum
     * value by threshhold.
     *
     * @param threshold The minimum required size
     * @param max The maximum required size
     * @return A collection of the rectangles needed to construct the total size
     */
    public static List subdivide(int width, int height, int threshold, int max)
    {
        List cols = components(width, threshold, max);
        List rows = components(height, threshold, max);
        List parts = new ArrayList();

        int i = 0, j = 0;
        int x = 0, y = 0;
        int row_size = rows.size();
        int col_size = cols.size();

        for(i = 0; i < row_size; i++)
        {
            for(j = 0, x = 0; j < col_size; j++)
            {
                parts.add(new Rectangle(x, y,
                            ((Integer)cols.get(j)).intValue(),
                            ((Integer)rows.get(i)).intValue()));
                x += ((Integer)cols.get(j)).intValue();
            }
            y += ((Integer)rows.get(i)).intValue();
        }

        return parts;
    }

    /**
     * Breaks an integer into powers of 2. The returned list contains a set of
     * Integers that if summed would be a closest fit to to value. Each returned
     * Integer is not greater than a power of 2 by more than threshhold and is not
     * greater than max.
     *
     * @param value The target value to achieve
     * @param threshold The minimum required size of the component total
     * @param max The maximum required size
     * @return A list, in order of the component values
     */
    public static List components(int value, int threshold, int max)
    {
        List components = new ArrayList();
        while (value > 0)
        {
            int p = Math.min(optimalPower(value, threshold, max), value);
            components.add(new Integer(p));
            value -= p;
        }

        return components;
    }

    /**
     * Returns an optimal power of two for the value given.
     * return the largest power of 2 which is less than or equal to the value, OR
     * it will return a larger power of two as long as the difference between
     * that and the value is not greater than the threshhold.
     */
    public static int optimalPower(int value, int threshhold, int max)
    {
        int optimal = 1;
        value = Math.min(value, max);
        while(optimal * 2 - value <= threshhold)
            optimal <<= 1;

        return optimal;
    }

    /**
     * Return the smallest power of 2 greater than value
     */
    public static int smallestPower(int value)
    {
        int n = 1;
        while (n < value)
            n <<= 1;

        return n;
    }
}
