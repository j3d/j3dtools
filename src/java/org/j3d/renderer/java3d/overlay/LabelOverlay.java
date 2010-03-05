/*****************************************************************************
 *                 Teseract Software, LLP Copyright(c)2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.overlay;

// External imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

import javax.media.j3d.Canvas3D;

// Local imports
// none

/**
 * An overlay that renders a text label.
 * <P>
 *
 * The text alignment relative to the label may be controlled in both the
 * horizontal and vertical alignment.
 * <p>
 *
 * By default, the font is Helvetica, Plain, 14 point, and the text colour
 * is white. Alignment of the text is set to the top-left of the overlay.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class LabelOverlay extends OverlayBase
{
    /** If the user doesn't supply a font, use this one */
    private static final Font DEFAULT_FONT =
        new Font("Helvetica", Font.PLAIN, 14);

    /** If the user does not supply a colour, use this one */
    private static final Color DEFAULT_COLOR = Color.white;

    /** The  value for the LEFT horizontal alignment */
    public static final int LEFT_ALIGN = 1;

    /** The  value for the RIGHT horizontal alignment */
    public static final int RIGHT_ALIGN = 2;

    /** The  value for the CENTER horizontal and vertical alignments */
    public static final int CENTER_ALIGN = 3;

    /** The  value for the TOP vertical alignment */
    public static final int TOP_ALIGN = 4;

    /** The  value for the BOTTOM vertical alignment */
    public static final int BOTTOM_ALIGN = 5;

    /** Message when the alignment value provided is incorrect */
    private static final String BAD_ALIGN_MSG =
        "The alignment value provided is unknown: ";

    // Class vars

    /** The string we are using to draw the text with */
    private String text;

    /** The rendered string, trimmed if needed to the right length */
    private String renderedText;

    /** The number of characters the user wants to be painted */
    private int visibleLength;

    /** The font to render the text in */
    private Font font;

    /** Rendering context to generate text size calculations. */
    private FontRenderContext renderContext;

    /** The colour to use to render the font with */
    private Color color;

    /** The vertical alignment direction */
    private int verticalAlignment;

    /** The horizontal alignment direction */
    private int horizontalAlignment;

    /** The current X position to render the text in */
    private int textX;

    /** The current Y position to render the text in */
    private int textY;

    /**
     * Create a new, simple label overlay that does not contain any text.
     * The default colour and font is used and it is aligned to the top-left
     * of the overlay.
     *
     * @param canvas The canvas for this overlay to live on
     * @param size The size of the overlay in pixels
     */
    public LabelOverlay(Canvas3D canvas, Dimension size)
    {
        this(canvas,
             size,
             null,
             DEFAULT_FONT,
             DEFAULT_COLOR,
             LEFT_ALIGN,
             TOP_ALIGN,
             null);
    }

    /**
     * Create a label overlay that displays the given text on the given
     * screen space. The default colour and font is used and it is aligned to
     * the top-left of the overlay. A null string will not be rendered.
     *
     * @param canvas The canvas for this overlay to live on
     * @param size The size of the overlay in pixels
     * @param str The string to render or null
     */
    public LabelOverlay(Canvas3D canvas, Dimension size, String str)
    {
        this(canvas,
             size,
             str,
             DEFAULT_FONT,
             DEFAULT_COLOR,
             LEFT_ALIGN,
             TOP_ALIGN,
             null);
    }

    /**
     * Create a customised label overlay that uses the given attributes of
     * font and colour styles. If the font or colour values are null then
     * the defaults are used.
     *
     * @param canvas The canvas for this overlay to live on
     * @param size The size of the overlay in pixels
     * @param str The string to render or null
     * @param font The font to use
     * @param color the color to render the text in
     * @param hAlign The horizontal alignment (LEFT, RIGHT, CENTER)
     * @param vAlign The vertical alignment (TOP, BOTTOM, CENTER)
     * @throws IllegalArguementException The alignment value given is
     *     not valid.
     */
    public LabelOverlay(Canvas3D canvas,
                        Dimension size,
                        String str,
                        Font font,
                        Color color,
                        int hAlign,
                        int vAlign) {
        this(canvas, size, str, font, color, hAlign, vAlign, null);
    }

    /**
     * Create a customised label overlay that includes a specialised update
     * manager to control when items are updated.
     *
     * @param canvas The canvas for this overlay to live on
     * @param size The size of the overlay in pixels
     * @param str The string to render or null
     * @param font The font to use
     * @param color the color to render the text in
     * @param hAlign The horizontal alignment (LEFT, RIGHT, CENTER)
     * @param vAlign The vertical alignment (TOP, BOTTOM, CENTER)
     * @param manager The manger to use to control updates
     * @throws IllegalArguementException The alignment value given is
     *     not valid.
     */
    public LabelOverlay(Canvas3D canvas,
                        Dimension size,
                        String str,
                        Font font,
                        Color color,
                        int hAlign,
                        int vAlign,
                        UpdateManager manager)
    {
        super(canvas, size, manager);
        this.font = (font == null) ? DEFAULT_FONT : font;
        this.color = (color == null) ? DEFAULT_COLOR : color;
        this.text = str;
        renderedText = text;

        visibleLength = -1;
        renderContext = new FontRenderContext(null, true, true);

        setVerticalAlignment(vAlign);
        setHorizontalAlignment(hAlign);

        resize();
        repositionText();
    }

    //------------------------------------------------------------------------
    // Methods defined by OverlayBase
    //------------------------------------------------------------------------

    /**
     * Repaint the overlay now. Overrides the base class to provide text
     * rendering.
     *
     * @param g The graphics context to paint with
     */
    public void paint(Graphics2D g)
    {
        if(renderedText == null)
            return;

        g.setColor(color);
        g.setFont(font);
        g.drawString(renderedText, textX, textY);
    }

    /**
     * Set the insets for this overlay. Note that this will not force a
     * dirty condition. A derived class will need to override this method and
     * make any size recalculations and dirty bit handling if this is needed.
     *
     * @param left The new left inset value to use
     * @param right The new right inset value to use
     * @param top The new left top value to use
     * @param bottom The new bottom inset value to use
     */
    public void setInsets(int left, int top, int right, int bottom)
    {
        super.setInsets(left, top, right, bottom);
        resize();
        repositionText();
    }

    //------------------------------------------------------------------------
    // Local utility methods
    //------------------------------------------------------------------------

    /**
     * Set the vertical alignment of the text in this overlay.
     *
     * @param align One of TOP, BOTTOM, CENTER
     * @throws IllegalArguementException The alignment value given is
     *     not valid.
     */
    public void setVerticalAlignment(int align)
    {
        switch(align)
        {
            case TOP_ALIGN:
            case BOTTOM_ALIGN:
            case CENTER_ALIGN:
                verticalAlignment = align;
                break;

            default:
                throw new IllegalArgumentException(BAD_ALIGN_MSG);
        }

        repositionText();
    }

    /**
     * Fetch the current vertical alignment setting.
     *
     * @return One of TOP, BOTTOM, CENTER
     */
    public int getVerticalAlignment()
    {
        return verticalAlignment;
    }

    /**
     * Set the horizontal alignment of the text in this overlay.
     *
     * @param align One of LEFT, RIGHT, CENTER
     * @throws IllegalArguementException The alignment value given is
     *     not valid.
     */
    public void setHorizontalAlignment(int align)
    {
        switch(align)
        {
            case LEFT_ALIGN:
            case RIGHT_ALIGN:
            case CENTER_ALIGN:
                horizontalAlignment = align;
                break;

            default:
                throw new IllegalArgumentException(BAD_ALIGN_MSG);
        }

        repositionText();
    }

    /**
     * Fetch the current horizontal alignment setting.
     *
     * @return One of LEFT, RIGHT, CENTER
     */
    public int getHorizontalAlignment()
    {
        return horizontalAlignment;
    }

    /**
     * Change the rendering color of the text to be rendered.
     *
     * @param c The new colour to use
     */
    public void setColor(Color c)
    {
        if(!color.equals(c))
        {
            color = c;
            repaint();
        }
    }

    /**
     * Change the font used by the text to be rendered.
     *
     * @param f The new font to use
     */
    public void setFont(Font f)
    {
        if(!font.equals(f))
        {
            font = f;
            resize();
            repositionText();
            repaint();
        }
    }

    /**
     * Set the text to the new string. The visible length does not change so if
     * you want to change the length, you should reset the visible length too
     *
     * @param str The new string to use
     */
    public void setText(String str)
    {
        text = str;

        switch(visibleLength) {
            case -1:
                renderedText = text;
                break;

            case 0:
                renderedText = null;
                break;

            default:
                renderedText = text.substring(0, visibleLength);
                break;
        }

        resize();
        repositionText();
        repaint();
    }

    /**
     * Get the number of characters that are rendered from the given string.
     * If the value is -1 the entire string is printed.
     *
     * @return The current number of visble characters
     */
    public int getVisibleLength()
    {
        return visibleLength;
    }

    /**
     * Set the number of visible characters to the given size. A size of zero
     * effectively stops the string being rendered. If the value is -1 the
     * entire string is printed.
     *
     * @param length The number of characters to be shown
     */
    public void setVisibleLength(int length)
    {
        visibleLength = length;

        switch(visibleLength) {
            case -1:
                renderedText = text;
                break;

            case 0:
                renderedText = null;
                break;

            default:
                renderedText = text.substring(0, visibleLength);
                break;
        }

        resize();
        repositionText();
        repaint();
    }

    /**
     * Recalculate the size of the overlay needed to hold the string. We
     * don't recalculate if this is a fixed-size overlay.
     */
    private void resize()
    {
        if(fixedSize)
            return;

        LineMetrics lm = font.getLineMetrics(renderedText, renderContext);
        Rectangle2D text_bounds = font.getStringBounds(renderedText,
                                                       renderContext);
        float width = 0;
        float height = 0;

        if(text_bounds instanceof Rectangle)
        {
            width = ((Rectangle)text_bounds).width;
            height = ((Rectangle)text_bounds).height;
        }
        else if(text_bounds instanceof Rectangle2D.Float)
        {
            width = ((Rectangle.Float)text_bounds).width;
            height = ((Rectangle.Float)text_bounds).height;
        }
        else if(text_bounds instanceof Rectangle2D.Double)
        {
            width = (float)((Rectangle.Double)text_bounds).width;
            height = (float)((Rectangle.Double)text_bounds).height;
        }

        width += leftInset + rightInset;
        height += bottomInset + topInset;

        setSize((int)width, (int)height);
    }

    /**
     * Recalculate the textX and textY positions of the text in the overlay.
     */
    private void repositionText()
    {
        if(renderedText == null)
            return;

        LineMetrics lm = font.getLineMetrics(renderedText, renderContext);
        float x = 0;
        float y = 0;

        switch(verticalAlignment)
        {
            case TOP_ALIGN:
                y = lm.getAscent() + lm.getLeading() + topInset;
                break;

            case BOTTOM_ALIGN:
                y = overlayBounds.height - lm.getDescent() -
                    lm.getLeading() - bottomInset;
                break;

            case CENTER_ALIGN:
                y = (overlayBounds.height * 0.5f) + (lm.getHeight() * 0.5f);
                break;
        }

        Rectangle2D text_bounds = font.getStringBounds(renderedText,
                                                       renderContext);
        float width = 0;

        if(text_bounds instanceof Rectangle)
            width = ((Rectangle)text_bounds).width;
        else if(text_bounds instanceof Rectangle2D.Float)
            width = ((Rectangle.Float)text_bounds).width;
        else if(text_bounds instanceof Rectangle2D.Double)
            width = (float)((Rectangle.Double)text_bounds).width;

        switch(horizontalAlignment)
        {
            case LEFT_ALIGN:
                x = leftInset;
                break;

            case RIGHT_ALIGN:
                x = overlayBounds.width - width - rightInset;
                break;

            case CENTER_ALIGN:
                x = (overlayBounds.width * 0.5f) - (width * 0.5f);
                break;
        }

        textX = (int)x;
        textY = (int)y;
    }
}
