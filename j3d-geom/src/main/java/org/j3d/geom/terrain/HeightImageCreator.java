/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.terrain;

// External imports
import java.awt.image.*;

// Local imports
import org.j3d.util.interpolator.ColorInterpolator;

/**
 * A converter utility for changing height map information to and from
 * images.
 * <p>
 *
 * Height maps are designed to use a single colour range - for example
 * grey scale. They do not generate multiple colour values for different
 * heights like the a colour ramp generator might produce. There is one
 * colour for the minimum height and one colour for the maximum and the
 * range blends between them.
 * <p>
 *
 * When generating an image, the values works along the X part of image
 * being equated to the X axis of the coordinates. The Y axis of the
 * image is equivalent to the Z axis of the coordinates.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class HeightImageCreator
{
    private static final byte[] BLACK_BYTES =
        { (byte)0, (byte)0, (byte)0, (byte)255 };
    private static final byte[] WHITE_BYTES =
        { (byte)255, (byte)255, (byte)255, (byte)255 };

    /** The colour of the minimum height */
    private byte[] minColor;

    /** The colour of the maximum height */
    private byte[] maxColor;

    /** Flag to denote if we have to deal with alpha values */
    private boolean hasAlpha;

    /** Interpolator for colour values */
    private ColorInterpolator interpolator;

    /**
     * Create a default map converter. If used to create an image, it
     * will use a grayscale range.
     */
    public HeightImageCreator()
    {
        this(null, null);
    }

    /**
     * Create a new height map convertor that uses color range values
     * when creating an image.If a parameter is null, it will set the
     * color to either black or white depending on which is null.
     *
     * @param min The colour of the minimum height
     * @param max The colour of the maximum height
     */
    public HeightImageCreator(byte[] min, byte[] max)
    {
        minColor = new byte[4];
        maxColor = new byte[4];

        if(min == null || min.length < 4)
        {
            minColor[0] = BLACK_BYTES[0];
            minColor[1] = BLACK_BYTES[1];
            minColor[2] = BLACK_BYTES[2];
            minColor[3] = BLACK_BYTES[3];
        }
        else
        {
            minColor[0] = min[0];
            minColor[1] = min[1];
            minColor[2] = min[2];
            minColor[3] = min[3];
        }

        if(max == null || max.length < 4)
        {
            maxColor[0] = WHITE_BYTES[0];
            maxColor[1] = WHITE_BYTES[1];
            maxColor[2] = WHITE_BYTES[2];
            maxColor[3] = WHITE_BYTES[3];
        }
        else
        {
            maxColor[0] = max[0];
            maxColor[1] = max[1];
            maxColor[2] = max[2];
            maxColor[3] = max[3];
        }

        reconstructInterpolator();
    }

    /**
     * Set the colours for the minimum and maximum height. If a parameter
     * is null, it will set the color to either black or white depending
     * on which is null.
     *
     * @param min The colour of the minimum height
     * @param max The colour of the maximum height
     */
    public void setColorRange(byte[] min, byte[] max)
    {
        if(min == null || min.length < 4)
        {
            minColor[0] = BLACK_BYTES[0];
            minColor[1] = BLACK_BYTES[1];
            minColor[2] = BLACK_BYTES[2];
            minColor[3] = BLACK_BYTES[3];
        }
        else
        {
            minColor[0] = min[0];
            minColor[1] = min[1];
            minColor[2] = min[2];
            minColor[3] = min[3];
        }

        if(max == null || max.length < 4)
        {
            maxColor[0] = WHITE_BYTES[0];
            maxColor[1] = WHITE_BYTES[1];
            maxColor[2] = WHITE_BYTES[2];
            maxColor[3] = WHITE_BYTES[3];
        }
        else
        {
            maxColor[0] = max[0];
            maxColor[1] = max[1];
            maxColor[2] = max[2];
            maxColor[3] = max[3];
        }

        reconstructInterpolator();
    }

    /**
     * Create a greyscale image from a set of terrain points. White
     * will indicate the highest point and black the lowest. The width and
     * height of the image will be equivalent to the size of the terrain
     * array presented.
     *
     * @param data The map of terrain coordinate values
     * @throws NullPointerException The data reference is null
     * @throws IllegalArgumentException Values in the data array are null
     * @return An image representation of the values
     */
    public BufferedImage createGreyScaleImage(float[][] data)
    {
        if(data == null)
            throw new NullPointerException("Data reference is null");

        int img_width = data[0].length;
        int img_height = data.length;
        int pixel_count = img_width * img_height;

        byte[] pixels = new byte[pixel_count];
        int count = 0;
        int work_height;

        float[] range_vals = findHeightRange(data);
        float min = range_vals[0];
        float range = range_vals[1];

        for(int i = 0; i < data.length; i++)
        {
            for(int j = 0; j < data[i].length; j++)
            {
                work_height = (int)((data[i][j] - min) * 255 / range);
                pixels[count++] = (byte)work_height;
            }
        }

        // Greyscale raster has only one component and 8 bits are significant.
        WritableRaster raster = Raster.createPackedRaster(DataBuffer.TYPE_BYTE,
                                                          img_width,
                                                          img_height,
                                                          1,
                                                          8,
                                                          null);

        raster.setDataElements(0, 0, img_width, img_height, pixels);

        BufferedImage image = new BufferedImage(img_width,
                                                img_height,
                                                BufferedImage.TYPE_BYTE_GRAY);
        image.setData(raster);

        return image;
    }

    /**
     * Create a color model image from the set of terrain points. This will
     * use the min and max colours that have been preset. Hints for width and
     * height of the output image can be provided. If the value is -1 then it
     * should be automatically calculated from the underlying data
     *
     * @param data The map of terrain coordinate values
     * @throws NullPointerException The data reference is null
     * @throws IllegalArgumentException Values in the data array are null
     * @return An image representing the heights
     */
    public BufferedImage createColorImage(float[][] data)
    {
        if(data == null)
            throw new NullPointerException("Data reference is null");

        int img_width = data[0].length;
        int img_height = data.length;
        int pixel_count = img_width * img_height;

        int components;
        int img_type;

        if(hasAlpha)
        {
            components = 4;
            img_type = BufferedImage.TYPE_INT_ARGB;
        }
        else
        {
            components = 3;
            img_type = BufferedImage.TYPE_INT_RGB;
        }

        byte[] pixels = new byte[pixel_count * components];

        int count = 0;
        float work_height;

        float[] range_vals = findHeightRange(data);
        float min = range_vals[0];
        float range = range_vals[1];
        float[] real_color;

        if(hasAlpha)
        {
            for(int i = 0; i < data.length; i++)
            {
                for(int j = 0; j < data[i].length; j++)
                {
                    work_height = (data[i][j] - min) / range;
                    real_color = interpolator.floatRGBValue(work_height);

                    pixels[count++] = (byte)(real_color[3] * 255);
                    pixels[count++] = (byte)(real_color[0] * 255);
                    pixels[count++] = (byte)(real_color[1] * 255);
                    pixels[count++] = (byte)(real_color[2] * 255);
                }
            }
        }
        else
        {
            for(int i = 0; i < data.length; i++)
            {
                for(int j = 0; j < data[i].length; j++)
                {
                    work_height = (data[i][j] - min) / range;
                    real_color = interpolator.floatRGBValue(work_height);

                    pixels[count++] = (byte)(real_color[0] * 255);
                    pixels[count++] = (byte)(real_color[1] * 255);
                    pixels[count++] = (byte)(real_color[2] * 255);
                }
            }
        }

        // Create a colour model that represents the type of data we want to
        // generate. Note that we do not use the default RGB model because that
        // assumes that all components are in a single bit-masked int. However,
        // we have color values as individual components and to save a lot of
        // bit flipping we create a colour model that allows us to keep the
        // separately calculated values directly.
        WritableRaster raster =
            Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                                           img_width,
                                           img_height,
                                           components,
                                           null);

        raster.setDataElements(0, 0, img_width, img_height, pixels);

        BufferedImage image = new BufferedImage(img_width,
                                                img_height,
                                                img_type);
        image.setData(raster);

        return image;
    }

    /**
     * Convert an image into a set of terrain points. Ideally, the image
     * should be a greyscale image, but color images are fine. Internally,
     * we convert the color into a greyscale and then use that as the basis
     * for creating the elevation grid.
     * <p>
     * Range values are taken according to the preset values supplied
     * elsewhere.
     *
     * @param img The image to convert
     * @return A height field matching the image
     */

    /**
     * Reconstruct the internal interpolator for the new colour values.
     */
    private void reconstructInterpolator()
    {
        // Simple comparisons because we're dealing with bytes.
        hasAlpha = ((((int)minColor[3] & 0xFF) != 255) ||
                    (((int)maxColor[3] & 0xFF) != 255));

        interpolator = new ColorInterpolator(2);

        float r = (float)((int)minColor[0] & 0xFF) / 255;
        float g = (float)((int)minColor[1] & 0xFF) / 255;
        float b = (float)((int)minColor[2] & 0xFF) / 255;
        float a = (float)((int)minColor[3] & 0xFF) / 255;
        interpolator.addRGBKeyFrame(0, r, g, b, a);

        r = (float)((int)maxColor[0] & 0xFF) / 255;
        g = (float)((int)maxColor[1] & 0xFF) / 255;
        b = (float)((int)maxColor[2] & 0xFF) / 255;
        a = (float)((int)maxColor[3] & 0xFF) / 255;

        interpolator.addRGBKeyFrame(1, r, g, b, a);
    }

    /**
     * Convenience method to find the bounds of the height values
     * passed.
     */
    private float[] findHeightRange(float[][] data)
    {
        float min = data[0][0];
        float max = data[0][0];

        for(int i = data.length; --i >= 0; )
        {
            for(int j = data[i].length; --j >= 0; )
            {
                min = min > data[i][j] ? data[i][j] : min;
                max = max < data[i][j] ? data[i][j] : max;
            }
        }

        float[] ret_val = { min, max - min };

        return ret_val;
    }
}
