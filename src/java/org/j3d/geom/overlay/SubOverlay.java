/*****************************************************************************
 *                        Teseract Software, LLP (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.overlay;

// Standard imports
import java.awt.geom.*;
import java.awt.*;

import javax.media.j3d.*;

import java.awt.image.Raster;
import java.awt.image.BufferedImage;

// Application specific imports
// none

/**
 * A SubOverlay is one of the pieces which displays a portion of the
 * overlay.  This is used internally by Overlay and should not be referenced
 * directly.
 *
 * @author David Yazel
 * @version $Revision: 1.1 $
 */
class SubOverlay
{
    /**
     * Represents that a buffer being activated or updated sould be the next
     * avaiable one.
     */
    public final static int NEXT_BUFFER = -1;

    private BufferedImage[] buffer;
    private ImageComponent2D[] bufferHolder;
    private int numBuffers;

    private int activeBufferIndex = 0; // this is the index of the currently active buffer

    private Texture2D texture;        // texture mapped to one double buffer
    private Shape3D shape;            // textured quad used to hold geometry
    private Rectangle space;          // The part of the overlay covered by this suboverlay

    private int transferBuffer[];     // used for transferring scan lines from main image to sub-image

    /**
     * Creates a double buffered suboverlay for the specified region that has
     * no transparency.
     *
     * @param space The area in screen space coords to create this for
     */
    protected SubOverlay(Rectangle space)
    {
        this(space, 2, false, null, null, null, null);
    }

    /**
     * Creates a suboverlay for the specified region that has a given number
     * of buffers and no transparency.
     *
     * @param space The area in screen space coords to create this for
     * @param numBuffers The number of buffers to create
     */
    protected SubOverlay(Rectangle space, int numBuffers)
    {
        this(space, numBuffers, false, null, null, null, null);
    }

    /**
     * Creates a double buffered suboverlay for the specified region with
     * the option to set the transparency.
     *
     * @param space The area in screen space coords to create this for
     * @param hasAlpha true If the overlay should include an alpha component
     */
    protected SubOverlay(Rectangle space, boolean hasAlpha)
    {
        this(space, 2, hasAlpha, null, null, null, null);
    }

    /**
     * Creates a buffered suboverlay for the specified region with
     * the option to set the transparency and number of buffers.
     *
     * @param space The area in screen space coords to create this for
     * @param hasAlpha true If the overlay should include an alpha component
     */
    protected SubOverlay(Rectangle space, int numBuffers, boolean hasAlpha)
    {
        this(space, numBuffers, hasAlpha, null, null, null, null);
    }

    /**
     * Creates the suboverlay with customisable attribute information. If any
     * parameter is null, defaults are used.
     *
     * @param space The area in screen space coords to create this for
     * @param numBuffers The number of buffers to create
     * @param hasAlpha true If the overlay should include an alpha component
     * @param polyAttr PolygonAttributes from the parent overlay
     * @param renderAttr RenderingAttributes from the parent overlay
     * @param texAttr TextureAttributes from the parent overlay
     * @param transAttr TransparencyAttributes from the parent overlay
     */
    protected SubOverlay(Rectangle space,
                         int numBuffers,
                         boolean hasAlpha,
                         PolygonAttributes polyAttr,
                         RenderingAttributes renderAttr,
                         TextureAttributes texAttr,
                         TransparencyAttributes transAttr)
    {
        this.space = space;
        this.numBuffers = numBuffers;
        buffer = new BufferedImage[numBuffers];
        bufferHolder = new ImageComponent2D[numBuffers];

        transferBuffer = new int[space.width];

        // create the two buffers

        int imageComponentType = hasAlpha
                      ? ImageComponent2D.FORMAT_RGBA
                      : ImageComponent2D.FORMAT_RGB;
        Dimension textureSize =
            new Dimension(OverlayUtilities.smallestPower(space.width),
                          OverlayUtilities.smallestPower(space.height));

        for(int i = numBuffers - 1; i >= 0; i--)
        {
            buffer[i] = OverlayUtilities.createBufferedImage(textureSize, hasAlpha);
            bufferHolder[i] = new ImageComponent2D(imageComponentType, buffer[i], true, true);
        }

        Appearance appearance = new Appearance();

        appearance.setPolygonAttributes(polyAttr);
        appearance.setRenderingAttributes(renderAttr);
        appearance.setTextureAttributes(texAttr);
        appearance.setTransparencyAttributes(transAttr);

        Material material = new Material();
        material.setLightingEnable(false);
        appearance.setMaterial(material);

        texture = new Texture2D(Texture.BASE_LEVEL,
                    (hasAlpha ? Texture.RGBA : Texture.RGB),
                    textureSize.width, textureSize.height);
        texture.setBoundaryModeS(Texture.CLAMP);
        texture.setBoundaryModeT(Texture.CLAMP);
        texture.setMagFilter(Texture.FASTEST);
        texture.setMinFilter(Texture.FASTEST);
        texture.setImage(0, bufferHolder[activeBufferIndex]);
        texture.setCapability(Texture.ALLOW_IMAGE_WRITE);

        appearance.setTexture(texture);

        shape = buildShape(appearance, space);
    }

    /**
     * Builds a Shape3D with the specified appearance covering the specified rectangle.
     * The shape is created as a rectangle in the X,Y plane.
     *
     * @param appearance The appearance to use for the shape
     * @param space The coordinates of the shape to create
     * @return A shape3D object representing the given information
     */
    private Shape3D buildShape(Appearance appearance, Rectangle space)
    {
        Texture texture = appearance.getTexture();

        int format = texture == null
                      ? QuadArray.COORDINATES
                      : QuadArray.COORDINATES | QuadArray.TEXTURE_COORDINATE_2;
        QuadArray planeGeometry = new QuadArray(4, format);

        float[] vertices = {
            space.x + space.width, space.y,                 0.0f,
            space.x + space.width, space.y + space.height,  0.0f,
            space.x,               space.y + space.height,  0.0f,
            space.x,               space.y,                 0.0f
        };

        planeGeometry.setCoordinates(0, vertices);

        if(texture != null)
        {
            float w_ratio = space.width / texture.getWidth();
            float h_ratio = space.height / texture.getHeight();

            float[] textureCoordinates = {
                w_ratio, 0.0f,
                w_ratio, h_ratio,
                0.0f,    h_ratio,
                0.0f,    0.0f
            };

            planeGeometry.setTextureCoordinates(0, 0, textureCoordinates);
        }

        Shape3D shape = new Shape3D();
        shape.setGeometry(planeGeometry);
        shape.setAppearance(appearance);

        return shape;
    }

    /**
     * Draws the portion of fullOverlayImage corresponding to space into the
     * buffer at bufferIndex.
     */
    public void updateBuffer(BufferedImage fullOverlayImage, int bufferIndex)
    {
        /* Ok, I have neve done this sort of thing before so I am going to have to step
         * through it. The image coming in is the entire overlay. There are two
         * problems with its current form. A it is too big and B the scan lines are
         * the reverse of what they need to be.
         *
         * So, we are going to read in lines from a subsection of the image and then
         * write them into the buffer in the opposite order of what they came in.
         */
        Dimension size = new Dimension(fullOverlayImage.getWidth(), fullOverlayImage.getHeight());

        if(bufferIndex == NEXT_BUFFER)
            bufferIndex = getNextBufferIndex();

        synchronized(buffer[bufferIndex])
        {
            // For each line in the output buffer
            for (int scanLine = 0; scanLine < space.height; scanLine++)
            {
                // Copy the appropriate line out of the buffer
                fullOverlayImage.getRGB(space.x,
                                        size.height - space.y - space.height + scanLine,
                                        transferBuffer.length, 1,
                                        transferBuffer,
                                        0,
                                        size.width);

                // Put the line into the output
                buffer[bufferIndex].setRGB(0,
                                           space.height - scanLine - 1,
                                           transferBuffer.length, 1,
                                           transferBuffer,
                                           0,
                                           size.width);
            }
        }
    }

    /**
     * Returns the index of the next buffer in line to be painted
     */
    public int getNextBufferIndex()
    {
        return (activeBufferIndex + 1) % numBuffers;
    }

    /**
     * This will change the buffer being displayed. It does not write anything,
     * only switched the image so it must be used carefully. It is intended for
     * use where more than one buffer has been prepped ahead of time. If you
     * do this without having the buffers preprepped then you will get strange
     * things.
     */
    public void setActiveBufferIndex(int activeBufferIndex)
    {
        if(activeBufferIndex == NEXT_BUFFER)
            activeBufferIndex = getNextBufferIndex();

        if(this.activeBufferIndex != activeBufferIndex)
        {
            this.activeBufferIndex = activeBufferIndex;
            texture.setImage(0, bufferHolder[activeBufferIndex]);
        }
    }

    /**
     * Return the shape
     */
    public Shape3D getShape()
    {
        return shape;
    }
}
