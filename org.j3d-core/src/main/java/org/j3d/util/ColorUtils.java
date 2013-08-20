/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util;

// Standard imports
import javax.vecmath.Color4f;

// Application specific imports
// none

/**
 * An set of utility functions that convert from one color space to another.
 * <P>
 *
 * The RGB<->HSV color space conversions have been taken from Foley & van Dam
 * <i>Computer Graphics Principles and Practice, 2nd Edition</i>, Addison
 * Wesley, 1990.
 * <p>
 *
 * The RGB <-> YUV colorspace conversion is based on the formulas found at
 * <a href="http://astronomy.swin.edu.au/~pbourke/colour/convert/">
 * http://astronomy.swin.edu.au/~pbourke/colour/convert/</a>
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <p>
 * <ul>
 * <li>invalidHMsg: The combination of S and H is invalid when S is zero. </li>
 * </ul>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.9 $
 */
public class ColorUtils
{
    /** The message string when s == 0 and h != NaN */
    private static final String INVALID_H_MSG_PROP =
        "org.j3d.util.interpolator.ColorUtils.invalidHMsg";

    /**
     * Change an RGB color to HSV color. We don't bother converting the alpha
     * as that stays the same regardless of color space.
     *
     * @param rgb The array of RGB components to convert
     * @param hsv An array to return the colour values with
     */
    public static void convertRGBtoHSV(float[] rgb, float[] hsv)
    {
        convertRGBtoHSV(rgb[0], rgb[1], rgb[2], hsv);
    }

    /**
     * Change an RGB color to HSV color. We don't bother converting the alpha
     * as that stays the same regardless of color space.
     *
     * @param r The r component of the color
     * @param g The g component of the color
     * @param b The b component of the color
     * @param hsv An array to return the HSV colour values in
     */
    public static void convertRGBtoHSV(float r, float g, float b, float[] hsv)
    {
        float h = 0;
        float s = 0;
        float v = 0;

        float max = (r > g) ? r : g;
        max = (max > b) ? max : b;

        float min = (r < g) ? r : g;
        min = (min < b) ? min : b;

        v = max;    // this is the value v

        // Calculate the saturation s
        if(max == 0)
        {
            s = 0;
            h = Float.NaN;  // h => UNDEFINED
        }
        else
        {
            // Chromatic case: Saturation is not 0, determine hue
            float delta = max - min;
            s = delta / max;

            if(r == max)
            {
                // resulting color is between yellow and magenta
                h = (g - b) / delta ;
            }
            else if(g == max)
            {
                // resulting color is between cyan and yellow
                h = 2 + (b - r) / delta;
            }
            else if(b == max)
            {
                // resulting color is between magenta and cyan
                h = 4 + (r - g) / delta;
            }

            // convert hue to degrees and make sure it is non-negative
            h *= 60;
            if(h < 0)
                h += 360;
        }

        // now assign everything....
        hsv[0] = h;
        hsv[1] = s;
        hsv[2] = v;
    }

    /**
     * Change an HSV color to RGB color. We don't bother converting the alpha
     * as that stays the same regardless of color space.
     *
     * @param hsv The HSV component of the color as an array
     * @param rgb An array to return the RGB colour values in
     */
    public static void convertHSVtoRGB(float[] hsv, float[] rgb)
    {
        convertHSVtoRGB(hsv[0], hsv[1], hsv[2], rgb);
    }

    /**
     * Change an HSV color to RGB color. We don't bother converting the alpha
     * as that stays the same regardless of color space.
     *
     * @param h The h component of the color
     * @param s The s component of the color
     * @param v The v component of the color
     * @param rgb An array to return the RGB colour values in
     */
    public static void convertHSVtoRGB(float h, float s, float v, float[] rgb)
    {
        float r = 0;
        float g = 0;
        float b = 0;

        if(s == 0)
        {
            // this color in on the black white center line <=> h = UNDEFINED
            if(Float.isNaN(h))
            {
                // Achromatic color, there is no hue
                r = v;
                g = v;
                b = v;
            }
            else
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(INVALID_H_MSG_PROP);

                throw new IllegalArgumentException(msg);
            }
        }
        else
        {
            if(h == 360)
            {
                // 360 is equiv to 0
                h = 0;
            }

            // h is now in [0,6)
            h = h /60;
            int i = (int)Math.floor(h);
            float f = h - i;             //f is fractional part of h
            float p = v * (1 - s);
            float q = v * (1 - (s * f));
            float t = v * (1 - (s * (1 - f)));

            switch(i)
            {
                case 0:
                   r = v;
                   g = t;
                   b = p;
                   break;

                case 1:
                   r = q;
                   g = v;
                   b = p;
                   break;

                case 2:
                   r = p;
                   g = v;
                   b = t;
                   break;

                case 3:
                   r = p;
                   g = q;
                   b = v;
                   break;

                case 4:
                   r = t;
                   g = p;
                   b = v;
                   break;

                case 5:
                   r = v;
                   g = p;
                   b = q;
                   break;
            }
        }

        // now assign everything....
        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;
    }

    /**
     * Change an RGB color to YUV (YCrCb) color. The colour value conversion is
     * independent of the colour range. Colours could be 0-1 or 0-255.
     *
     * @param rgb The array of RGB components to convert
     * @param yuv An array to return the colour values with
     */
    public static void convertRGBtoYUV(float[] rgb, float[] yuv)
    {
        float r = rgb[0];
        float g = rgb[1];
        float b = rgb[2];

        yuv[0] = (0.299f * r) + (0.587f * g) + (0.114f * b);
        yuv[1] = (-0.169f * r) - (0.331f * g) + (0.5f * b);
        yuv[2] = (0.5f * r) - (0.419f * g) - (0.081f * b);
    }

    /**
     * Change an YUV (YCrCb) color to RGB color. The colour value conversion is
     * independent of the colour range. Colours could be 0-1 or 0-255.
     *
     * @param yuv The array of YUV components to convert
     * @param rgb An array to return the colour values with
     */
    public static void convertYUVtoRGB(float[] yuv, float[] rgb)
    {
        float y = yuv[0];
        float u = yuv[1];
        float v = yuv[2];

        rgb[0] = y + 1.140f * v;
        rgb[1] = y - 0.394f * u - 0.581f * v;
        rgb[2] = y + 2.028f * u;
    }

    /**
     * Change an RGB color to YIQ (JPEG) color. The colour value conversion is
     * independent of the colour range. Colours could be 0-1 or 0-255.
     *
     * @param rgb The array of RGB components to convert
     * @param yiq An array to return the colour values with
     */
    public static void convertRGBtoYIQ(float[] rgb, float[] yiq)
    {
        float r = rgb[0];
        float g = rgb[1];
        float b = rgb[2];

        yiq[0] = (0.299f * r) + (0.587f * g) + (0.114f * b);
        yiq[1] = (0.596f * r) - (0.274f * g) - (0.322f * b);
        yiq[2] = (0.212f * r) - (0.523f * g) - (0.311f * b);
    }

    /**
     * Change an YIQ (JPEG) color to RGB color. The colour value conversion is
     * independent of the colour range. Colours could be 0-1 or 0-255.
     *
     * @param yiq The array of YIQ components to convert
     * @param rgb An array to return the colour values with
     */
    public static void convertYIQtoRGB(float[] yiq, float[] rgb)
    {
        float y = yiq[0];
        float i = yiq[1];
        float q = yiq[2];

        rgb[0] = y + 0.956f * i + 0.621f * q;
        rgb[1] = y - 0.272f * i - 0.647f * q;
        rgb[2] = y - 1.105f * i + 1.702f * q;
    }
}
