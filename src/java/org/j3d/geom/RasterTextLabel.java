/*****************************************************************************
 *                      J3D.org Copyright (c) 2000
 *                              Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom;

// Standard imports
import javax.media.j3d.*;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.vecmath.Point3f;

// Application specific imports

/**
 * A text label for labelling objects on screen that uses a Java 3D Raster
 * to produce the overlay effect.
 * <p>
 *
 * If the label text is null, then no label will be displayed. All of the
 * setup will be done, but no raster will be created.
 * <p>
 *
 * The text label can come in a number of flavours depending on how you
 * configure it through the constructors. You may build a label that is
 * only static, always fixed size regardless of text length, and/or may
 * be hidden from other objects v always on the top. Once configured to one
 * of these versions, it cannot be changed.
 * <p>
 *
 * If running dynamic text, the internal images will only resize to a
 * larger size. That is, if a new string comes in that is smaller than
 * the original string, the image will stay the larger length than the
 * original. The idea is to reduce the amount of garbage generated. However,
 * in some instances this may not produce acceptable visual behaviour,
 * so the crop() method is introduced that will force the image size to
 * be reduced to the smallest possible size for the next time a string
 * is set. There are also variants on the setText() methods to do this
 * as well with a flag. Cropping is independent of the fixedSize flag but is
 * still subject to the dynamic flag on the constructor.
 *
 * @author Justin Couch
 * @version $Revision: 2.0 $
 */
public class RasterTextLabel extends Shape3D
{
    /** Message when the text label is not setup for dynamic changes */
    private static final String CANT_CHANGE_MSG =
        "Attempting to make a change to a label that was not originally " +
        "configured to be dynamic";

    /** The clear colour used to clear the background of the image */
    private static final Color CLEAR_COLOR = new Color(0, 0, 0, 0);

    /** The inset between the text and the border if one is required. */
    private static final int BORDER_INSETS = 2;

    /** The current color of the text */
    private Color textColor;

    /** The current color of the border. Null if not in use */
    private Color borderColor;

    /** The background color of the image */
    private Color backgroundColor;

    /** The font of the label. Null if using system default */
    private Font labelFont;

    /** The raster object that we put the information in */
    private Raster raster;

    /** The image component that holds the image used by the raster */
    private ImageComponent2D component;

    /**
     * Flag to say if the implementation should resize the underlying label
     * for each text update or not.
     */
    private boolean adjustImageSize;

    /** Flag indicating if this is a dynamic label */
    private boolean isDynamic;

    /** Underlying image used. Only set if this is a dynamic label */
    private BufferedImage textImage;

    /** The current image width */
    private int imageWidth;

    /** The current image height */
    private int imageHeight;

    /**
     * Create a new blank label with no text. It is located at the origin. It
     * is assumed to be dynamic and always on top.
     */
    public RasterTextLabel()
    {
        this(null, null, true, true, 0, 0, 0, null, null);
    }

    /**
     * Create a new blank label with the given text located at the origin.
     * If the text color is not specified, white is used and the code assumes
     * static text, it will always be on top.
     *
     * @param label The string to use on the label
     * @param col The text color to be drawn in
     */
    public RasterTextLabel(String label, Color col)
    {
        this(label, col, false, true, 0, 0, 0, null, null);
    }

    /**
     * Create a new blank label with the given text located at the origin.
     * If the text color is not specified, white is used and the code assumes
     * static text, it will always be on top.
     *
     * @param label The string to use on the label
     * @param col The text color to be drawn in
     * @param alwaysOnTop true if this should never be obscured by content
     */
    public RasterTextLabel(String label, Color col, boolean alwaysOnTop)
    {
        this(label, col, alwaysOnTop, false, 0, 0, 0, null, null);
    }

    /**
     * Create a new blank label with the given text located at the origin.
     * If the text color is not specified, white is used.
     *
     * @param label The string to use on the label
     * @param col The text color to be drawn in
     * @param dynamic True if this will change text over time
     * @param alwaysOnTop true if this should never be obscured by content
     */
    public RasterTextLabel(String label,
                           Color col,
                           boolean alwaysOnTop,
                           boolean dynamic)
    {
        this(label, col, alwaysOnTop, dynamic, 0, 0, 0, null, null);
    }

    /**
     * Create a new blank label with the given text located at a specific
     * point in 3D world coordinates. The code assumes a static label.
     *
     * @param label The string to use on the label
     * @param col The text color to be drawn in
     * @param alwaysOnTop true if this should never be obscured by content
     * @param x The x world coordinate to place the label
     * @param y The y world coordinate to place the label
     * @param z The z world coordinate to place the label
     */
    public RasterTextLabel(String label,
                           Color col,
                           boolean alwaysOnTop,
                           float x,
                           float y,
                           float z)
    {
        this(label, col, alwaysOnTop, false, x, y, z, null, null);
    }

    /**
     * Create a new blank label with the given text located at a specific
     * point in 3D world coordinates.
     *
     * @param label The string to use on the label
     * @param col The text color to be drawn in
     * @param x The x world coordinate to place the label
     * @param y The y world coordinate to place the label
     * @param z The z world coordinate to place the label
     * @param alwaysOnTop true if this should never be obscured by content
     * @param dynamic True if this will change text over time
     */
    public RasterTextLabel(String label,
                           Color col,
                           boolean alwaysOnTop,
                           boolean dynamic,
                           float x,
                           float y,
                           float z)
    {
        this(label, col, alwaysOnTop, dynamic, x, y, z, null, null);
    }

    /**
     * Create a new blank label with the given text located at a specific
     * point in 3D world coordinates and an option to show a border and
     * selected font. If the border color is specified, it will show a 1
     * pixel wide border in that color. If no font is defined, the system
     * default font will be used.
     *
     * @param label The string to use on the label
     * @param col The text color to be drawn in
     * @param x The x world coordinate to place the label
     * @param y The y world coordinate to place the label
     * @param z The z world coordinate to place the label
     * @param border The color to use for the border or null for none
     * @param font The font to draw the string in or null for default
     * @param dynamic True if this will change text over time
     */
    public RasterTextLabel(String label,
                           Color col,
                           boolean alwaysOnTop,
                           boolean dynamic,
                           float x,
                           float y,
                           float z,
                           Color border,
                           Font font)
    {
        adjustImageSize = false;

        textColor = (col != null) ? col : Color.white;
        borderColor = border;
        labelFont = font;
        isDynamic = dynamic;

        Appearance app = new Appearance();
        RenderingAttributes ra = new RenderingAttributes();
        ra.setAlphaTestFunction(RenderingAttributes.GREATER);

        if(alwaysOnTop)
        {
            ra.setDepthBufferEnable(false);
            ra.setDepthBufferWriteEnable(false);
        }

        app.setRenderingAttributes(ra);
        setAppearance(app);

        // create a disposable 1x1 image so that we can fetch the font
        // metrics associated with the font and text label. This will allow
        // us to determine the real image size. This is kludgy, but I can't
        // think of a better way of doing it!
        BufferedImage tmp_img =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        Graphics graphics = tmp_img.getGraphics();
        FontMetrics fm;

        if(labelFont == null)
            fm = graphics.getFontMetrics();
        else
            fm = graphics.getFontMetrics(labelFont);

        int width = 0;
        int height = 0;

        if(label == null)
        {
            // No label? Create an empty (default) raster object then. This
            // should result in nothing being rendered on screen, but leaves
            // us a placeholder for later.
            raster = new Raster();
            raster.setPosition(new Point3f(x, y, z));
        }
        else
        {
            // now we have the metrics, let's work out how big the label is!
            Rectangle2D dimensions = fm.getStringBounds(label, graphics);

            graphics.dispose();
            tmp_img.flush();
            tmp_img = null;

            width = (int)dimensions.getWidth();
            height = (int)dimensions.getHeight();
            int ascent = fm.getMaxAscent();

            if(border != null)
            {
                width += BORDER_INSETS * 2 + 2; // one pixel border * 2
                height += BORDER_INSETS * 2 + 2;
            }

            textImage = new BufferedImage(width,
                                          height,
                                          BufferedImage.TYPE_INT_ARGB);

            graphics = textImage.getGraphics();

            renderImage(graphics, label, width, height, ascent);

            graphics.dispose();

            component = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA,
                                             textImage);

            raster = new Raster(new Point3f(x, y, z),
                                Raster.RASTER_COLOR,
                                0,
                                0,
                                width,
                                height,
                                component,
                                null);

            // clear the reference if not dynamic
            if(!dynamic)
            {
                component = null;
                textImage = null;
            }
        }

        if(dynamic)
        {
            component.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);
            raster.setCapability(Raster.ALLOW_SIZE_WRITE);
            raster.setCapability(Raster.ALLOW_IMAGE_WRITE);
            raster.clearCapabilityIsFrequent(Raster.ALLOW_IMAGE_WRITE);
        }

        setGeometry(raster);
    }

    /**
     * Set the label string that is to be rendered. This maintains the
     * current text color. If this was not set up to be a dynamic image, an
     * exception is thrown.
     *
     * @param text The string to be rendered
     * @throws IllegalStateException The label was not set up to be dynamic
     *    in the constructor
     */
    public void setText(String text) throws IllegalStateException
    {
        if(!isDynamic)
            throw new IllegalStateException(CANT_CHANGE_MSG);

        updateText(text);
    }

    /**
     * Set the label string that is to be rendered with the option of croping
     * it to the length of the string. This maintains the current text color.
     * If this was not set up to be a dynamic image, an exception is thrown.
     *
     * @param text The string to be rendered
     * @param crop true to crop the underlying raster
     * @throws IllegalStateException The label was not set up to be dynamic
     *    in the constructor
     */
    public void setText(String text, boolean crop) throws IllegalStateException
    {
        if(!isDynamic)
            throw new IllegalStateException(CANT_CHANGE_MSG);

        if(crop)
            textImage = null;

        updateText(text);
    }

    /**
     * Set the label string that is to be rendered and changes the color
     * to the new value. If this was not set up to be a dynamic image, an
     * exception is thrown.
     *
     * @param text The string to be rendered
     * @param col The new color to be used or null for the default (white)
     * @throws IllegalStateException The label was not set up to be dynamic
     *    in the constructor
     */
    public void setText(String text, Color col) throws IllegalStateException
    {
        if(!isDynamic)
            throw new IllegalStateException(CANT_CHANGE_MSG);

        textColor = (col != null) ? col : Color.white;

        updateText(text);
    }

    /**
     * Set the label string that is to be rendered and changes the color
     * to the new value. If this was not set up to be a dynamic image, an
     * exception is thrown.
     *
     * @param text The string to be rendered
     * @param col The new color to be used or null for the default (white)
     * @param crop true to crop the underlying raster
     * @throws IllegalStateException The label was not set up to be dynamic
     *    in the constructor
     */
    public void setText(String text, Color col, boolean crop)
        throws IllegalStateException
    {
        if(!isDynamic)
            throw new IllegalStateException(CANT_CHANGE_MSG);

        if(crop)
            textImage = null;

        textColor = (col != null) ? col : Color.white;

        updateText(text);
    }


    /**
     * Set the condition of whether the implementation should resize the
     * canvas after each new label is set or just stick to a fixed size
     * canvas. A fixed size label is useful when you are making fast updates
     * such as a counter. When this is called, the label will not be resized
     * from it's current dimensions. This may be changed dynamically and will
     * only take effect next time a text string is set and the size is based
     * on the biggest image used to date, not on the next string that is
     * set.
     *
     * @param fixed true if the label size should remain fixed
     * @throws IllegalStateException The label was not set up to be dynamic
     *    in the constructor
     */
    public void fixSize(boolean fixed)
        throws IllegalStateException
    {
        if(!isDynamic)
            throw new IllegalStateException(CANT_CHANGE_MSG);

        adjustImageSize = fixed;
    }

    /**
     * Crop the image used for the raster to the length of the string the next
     * time a string is set. This will not crop the current string, only the
     * next one.
     *
     * @throws IllegalStateException The label was not set up to be dynamic
     *    in the constructor
     */
    public void crop()
        throws IllegalStateException
    {
        if(!isDynamic)
            throw new IllegalStateException(CANT_CHANGE_MSG);

        // easiest way to force a rebuild is to remove the textImage reference
        // completely and treat it like a new image with no text set.
        textImage = null;
    }

    /**
     * Update the raster to display the new text.
     *
     * @param label The new text string to draw
     * @param fontChanged true if the font changed
     */
    private void updateText(String label)
    {
        // If we have no text to display, just set the size to display
        // nothing and leave immediately.
        if(label == null)
        {
            raster.setSize(0, 0);
            return;
        }

        int width = 0;
        int height = 0;
        FontMetrics metrics = null;
        Graphics graphics = null;

        // Work on the null image first because adjustSize may also be set
        // but would crash with a null source image.
        if(textImage == null)
        {
            BufferedImage tmp_img =
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

            graphics = tmp_img.getGraphics();

            if(labelFont == null)
                metrics = graphics.getFontMetrics();
            else
                metrics = graphics.getFontMetrics(labelFont);

            Rectangle2D dimensions = metrics.getStringBounds(label, graphics);

            graphics.dispose();
            tmp_img.flush();

            width = (int)dimensions.getWidth();
            height = (int)dimensions.getHeight();

            if(borderColor != null)
            {
                width += BORDER_INSETS * 2 + 2; // one pixel border * 2
                height += BORDER_INSETS * 2 + 2;
            }

            textImage = new BufferedImage(width,
                                          height,
                                          BufferedImage.TYPE_INT_ARGB);

            graphics = textImage.createGraphics();
            renderImage(graphics, label, width, height, metrics.getAscent());

            component = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA,
                                             textImage);
            component.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);

            raster.setSize(width, height);
            raster.setImage(component);
        }
        else if(adjustImageSize)
        {
            // recalc size and compare against current.
            graphics = textImage.getGraphics();

            if(labelFont == null)
                metrics = graphics.getFontMetrics();
            else
                metrics = graphics.getFontMetrics(labelFont);

            Rectangle2D dimensions = metrics.getStringBounds(label, graphics);

            width = (int)dimensions.getWidth();
            height = (int)dimensions.getHeight();

            if(borderColor != null)
            {
                width += BORDER_INSETS * 2 + 2; // one pixel border * 2
                height += BORDER_INSETS * 2 + 2;
            }

            // So now we know the required size. Is it bigger than the current
            // image size? Resize if new size if bigger than the old.
            if((width > textImage.getWidth(null)) ||
               (height > textImage.getHeight(null)))
            {
                textImage.flush();
                textImage = new BufferedImage(width,
                                              height,
                                              BufferedImage.TYPE_INT_ARGB);

                graphics = textImage.createGraphics();

                // Probably superfluous, but in for correctness.
                if(labelFont == null)
                    metrics = graphics.getFontMetrics();
                else
                    metrics = graphics.getFontMetrics(labelFont);

                renderImage(graphics, label, width, height, metrics.getAscent());

                component = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA,
                                                 textImage);
                component.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);

                raster.setSize(width, height);
                raster.setImage(component);
            }
            else
            {
                renderImage(graphics, label, width, height, metrics.getAscent());

                component.set(textImage);
                raster.setSize(width, height);
            }
        }
        else
        {
            // fixed size
            graphics = textImage.createGraphics();

            if(labelFont == null)
                metrics = graphics.getFontMetrics();
            else
                metrics = graphics.getFontMetrics(labelFont);

            Rectangle2D dimensions = metrics.getStringBounds(label, graphics);

            width = (int)dimensions.getWidth();
            height = (int)dimensions.getHeight();

            if(borderColor != null)
            {
                width += BORDER_INSETS * 2 + 2; // one pixel border * 2
                height += BORDER_INSETS * 2 + 2;
            }

            renderImage(graphics, label, width, height, metrics.getAscent());

            component.set(textImage);
            raster.setSize(width, height);
        }

        graphics.dispose();
    }

    /**
     * Convenience method to render the image given the font information. When
     * this method exits, it will have changed the global imageWidth and
     * imageHeight variables to the given width and height values.
     *
     * @param graphics The graphics context for textImage
     * @param label The string to render
     * @param width The width of the image drawn to
     * @param height The height of the image drawn to
     * @param ascent The ascent of the font in use
     */
    private void renderImage(Graphics graphics,
                             String label,
                             int width,
                             int height,
                             int ascent)
    {
        Graphics2D g = (Graphics2D)graphics;
        g.setComposite(AlphaComposite.Src);

        g.setColor(CLEAR_COLOR);
        g.fillRect(0, 0, imageWidth, imageHeight);

        if(borderColor != null)
        {
            g.setColor(borderColor);
            g.drawRect(0, 0, width - 1, height - 1);

            g.setColor(textColor);
            g.setFont(labelFont);
            g.drawString(label,
                         BORDER_INSETS + 1,
                         ascent + BORDER_INSETS + 1);
        }
        else
        {
            g.setColor(textColor);
            g.setFont(labelFont);
            g.drawString(label, 0, ascent);
        }

        imageWidth = width;
        imageHeight = height;
    }
}