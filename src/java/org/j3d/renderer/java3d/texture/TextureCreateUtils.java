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

package org.j3d.renderer.java3d.texture;

// Standard imports
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageProducer;
import java.awt.image.RenderedImage;

import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;

// Application specific imports
import org.j3d.util.ImageUtils;

/**
 * Convenience class with a collection of useful utility methods taking an
 * image and turning it into a Java3D texture object.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class TextureCreateUtils
{
    /** Internal convenience value of Math.log(2) */
    private static final double LOG_2 = Math.log(2);

    /**
     * Default constructor.
     */
    public TextureCreateUtils()
    {
    }

    /**
     * Given the image, create a texture object from it, resizing the image to
     * up to a power of 2 if needed. The texture created is a basic
     * non-repeating texture, with no filtering information set.
     *
     * @param img The source image to work with
     * @return A texture object to hold the image with
     */
    public Texture2D createTexture2D(RenderedImage img)
    {
        int width = img.getWidth();
        int height = img.getHeight();

        width = nearestPowerTwo(width, true);
        height = nearestPowerTwo(height, true);

        RenderedImage base_img = scaleTexture(img, width, height);
        ImageComponent2D comp = create2DImageComponent(base_img);
        Texture2D ret_val = new Texture2D(Texture2D.BASE_LEVEL,
                                          getTextureFormat(comp),
                                          width,
                                          height);

        ret_val.setImage(0, comp);

        return ret_val;
    }

    /**
     * From the image component format, generate the appropriate texture
     * format.
     *
     * @param comp The image component to get the value from
     * @return The appropriate corresponding texture format value
     */
    public int getTextureFormat(ImageComponent comp)
    {
        int ret_val = Texture.RGBA;

        switch(comp.getFormat())
        {
            case ImageComponent.FORMAT_CHANNEL8:
                // could also be alpha, but we'll punt for now. We really need
                // the user to pass in this information. Need to think of a
                // good way of doing this.
                ret_val = Texture.LUMINANCE;
                break;

            case ImageComponent.FORMAT_LUM4_ALPHA4:
            case ImageComponent.FORMAT_LUM8_ALPHA8:
                ret_val = Texture.LUMINANCE_ALPHA;
                break;

            case ImageComponent.FORMAT_R3_G3_B2:
            case ImageComponent.FORMAT_RGB:
            case ImageComponent.FORMAT_RGB4:
            case ImageComponent.FORMAT_RGB5:
//            case ImageComponent.FORMAT_RGB8:
                ret_val = Texture.RGB;
                break;

            case ImageComponent.FORMAT_RGB5_A1:
            case ImageComponent.FORMAT_RGBA:
            case ImageComponent.FORMAT_RGBA4:
//            case ImageComponent.FORMAT_RGBA8:
                ret_val = Texture.RGBA;
                break;
        }

        return ret_val;
    }

    /**
     * Scale a texture to a new size. Generally used to scale a texture to a
     * power of 2.
     *
     * @param ri The texture to scale
     * @param newWidth The new width
     * @param newHeight The new height
     */
    public RenderedImage scaleTexture(RenderedImage ri,
                                      int newWidth,
                                      int newHeight)
    {
        int width = ri.getWidth();
        int height = ri.getHeight();

        if(width == newWidth && height == newHeight)
            return ri;

        float xScale = (float)newWidth / (float)width;
        float yScale = (float)newHeight / (float)height;

        AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
        AffineTransformOp atop =
            new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        RenderedImage ret_val = ri;

        if(ri instanceof BufferedImage)
        {
            BufferedImage buffImage = (BufferedImage)ri;
            ColorModel cm = buffImage.getColorModel();

            if(cm.hasAlpha())
            {
                ret_val = atop.filter((BufferedImage)ri, null);
            }
            else
            {
                ret_val = new BufferedImage(newWidth,
                                            newHeight,
                                            ((BufferedImage)ri).getType());
                atop.filter((BufferedImage)ri, (BufferedImage)ret_val);
            }
        }
        else
        {
            System.out.println("Can't rescale RenderedImage.");
        }

        return ret_val;
    }

    /**
     * Load the image component from the given object type. All images are
     * loaded by-reference. This does not automatically register the component
     * with the internal datastructures. That is the responsibility of the
     * caller.
     *
     * @param content The object that was loaded and needs to be converted
     * @return An ImageComponent instance with byRef true and yUp false
     */
    public ImageComponent2D create2DImageComponent(Object content)
    {
        if(!(content instanceof ImageProducer) &&
           !(content instanceof BufferedImage) &&
           !(content instanceof Image))
            throw new IllegalArgumentException("Not a valid image type " + content);

        BufferedImage image = null;

        if(content instanceof ImageProducer)
            image = ImageUtils.createBufferedImage((ImageProducer)content);
        else if(content instanceof BufferedImage)
            image = (BufferedImage)content;
        else
            image = ImageUtils.createBufferedImage((Image)content);

        ColorModel cm = image.getColorModel();
        boolean alpha = cm.hasAlpha();

        int format = ImageComponent2D.FORMAT_RGBA;

        switch(image.getType())
        {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_INT_RGB:
                format = ImageComponent2D.FORMAT_RGB;
                break;

            case BufferedImage.TYPE_CUSTOM:
                // no idea what this should be, so default to RGBA
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                format = ImageComponent2D.FORMAT_RGBA;
                break;

            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_USHORT_GRAY:
                format = ImageComponent2D.FORMAT_CHANNEL8;
                break;

            case BufferedImage.TYPE_BYTE_INDEXED:
                if(alpha)
                    format = ImageComponent2D.FORMAT_RGBA;
                else
                    format = ImageComponent2D.FORMAT_RGB;
                break;

            case BufferedImage.TYPE_USHORT_555_RGB:
                format = ImageComponent2D.FORMAT_RGB5;
                break;

            case BufferedImage.TYPE_USHORT_565_RGB:
                format = ImageComponent2D.FORMAT_RGB5;
                break;
        }

        ImageComponent2D ret_val =
            new ImageComponent2D(format, image, true, false);

        return ret_val;
    }

    /**
     * Determine the nearest power of two value for a given argument.
     * This function uses the formal ln(x) / ln(2) = log2(x)
     *
     * @param val The initial size
     * @param scaleUp true to scale the value up, false for down
     * @return The power-of-two-ized value
     */
    public int nearestPowerTwo(int val, boolean scaleUp)
    {
        int log;

        if(scaleUp)
            log = (int) Math.ceil(Math.log(val) / LOG_2);
        else
            log = (int) Math.floor(Math.log(val) / LOG_2);

        return (int)Math.pow(2, log);
    }

    /**
     * Compute the n where 2^n = value.
     *
     * @param value The value to compute.
     */
    private int computeLog(int value)
    {
        int i = 0;

        if(value == 0)
            return -1;

        while(true)
        {
            if(value == 1)
                return i;
            value >>= 1;
            i++;
        }
    }
}
