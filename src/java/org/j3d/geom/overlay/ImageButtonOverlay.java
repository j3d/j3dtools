/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.overlay;

// Standard imports
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.media.j3d.Canvas3D;

// Application specific imports
// None

/**
 * An overlay with a clickable image that acts like a button.
 * <p>
 *
 * The overlay will take up to four images. These images are supplied in an
 * array in the order:
 * <OL>
 * <LI>Inactive</LI>
 * <LI>Active</LI>
 * <LI>Clicked (mouse pressed)</LI>
 * <LI>Mouseover (mouse over but no button press)</LI>
 * </OL>
 *
 * Any of these images can be left as null in the array and the result is that
 * the image will not change for that particular action.
 *
 * @author Will Holcomb, Justin Couch
 * @version $Revision: 1.1 $
 */
public class ImageButtonOverlay extends MouseOverlay
{
    /** Total number of buffers we need for the images */
    private static final int NUM_BUFFERS = 4;

    /** Index of the inactive image */
    public static final int INACTIVE_IMAGE = 0;

    /** Index of the active image */
    public static final int ACTIVE_IMAGE = 1;

    /** Index of the mouse clicked image */
    public static final int CLICKED_IMAGE = 2;

    /** Index of the mouse over image */
    public static final int MOUSEOVER_IMAGE = 3;

    /** Set of flags indicating which images were provided */
    private boolean[] hasImage;

    /** Flag indicating the mouse is over the button currently */
    private boolean mouseOver = false;

    /**
     * Flag indicating the mouse has clicked the button to a permanently
     * down state like a toggle button action.
     */
    private boolean stuck = false;

    /** Flag indicating the button has been clicked */
    private boolean clicked = false;

    /**
     * Create a new overlay that uses the given images placed in the
     * space. Alpha is assumed to be on, but there is no blending.
     *
     * @param canvas The canvas to put the overlay on
     * @param imageSpace The space on the canvas to place the image
     * @param images The array of images to use (non-null)
     */
    public ImageButtonOverlay(Canvas3D canvas,
                              Rectangle imageSpace,
                              BufferedImage[] images)
    {
        this(canvas, imageSpace, true, false, images);
    }

    /**
     * Create a new button overlay where you get to control the alpha setting
     * with the images.
     *
     * @param canvas The canvas to put the overlay on
     * @param imageSpace The space on the canvas to place the image
     * @param clipAlpha true if the image contains alpha
     * @param blendAlpha true if you want to blend the image with the
     *    background of the overlay (ie let the color show through)
     * @param images The array of images to use (non-null)
     */
    public ImageButtonOverlay(Canvas3D canvas,
                              Rectangle imageSpace,
                              boolean clipAlpha,
                              boolean blendAlpha,
                              BufferedImage[] images)
    {
        super(canvas,
              imageSpace,
              clipAlpha,
              blendAlpha,
              null,
              NUM_BUFFERS);

        hasImage = new boolean[NUM_BUFFERS];
        mouseOver = false;
        stuck = false;
        clicked = false;

        for(int i = 0; i < images.length; i++)
        {
            hasImage[i] = images[i] != null;
System.out.println("hasImage " + hasImage[i]);

            if(hasImage[i])
                updateBuffer(images[i], i);
        }

        setActiveBuffer(INACTIVE_IMAGE);
    }

    /**
     * Initialise the overlay to build mouse input support
     */
    public void initialize()
    {
        addMouseListener(this);

        super.initialize();
    }

    //------------------------------------------------------------------------
    // Methods for MouseListener events
    //------------------------------------------------------------------------

    /**
     * Process a mouse press event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mousePressed(MouseEvent evt)
    {
        clicked = true;
        switchButtons();
    }

    /**
     * Process a mouse release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt)
    {
        clicked = false;
        switchButtons();
    }

    /**
     * Process a mouse click event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseClicked(MouseEvent evt)
    {
        stuck = !stuck;
        clicked = false;
        switchButtons();
    }

    /**
     * Process a mouse enter event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseEntered(MouseEvent evt)
    {
        mouseOver = true;
        switchButtons();
    }

    /**
     * Process a mouse exited event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseExited(MouseEvent evt)
    {
        mouseOver = false;
        clicked = false;
        switchButtons();
    }

    //------------------------------------------------------------------------
    // Local methods
    //------------------------------------------------------------------------

    /**
     * Called on a state change to update the buttons
     */
    private synchronized void switchButtons()
    {
        if(mouseOver && clicked && hasImage[CLICKED_IMAGE])
        {
            setActiveBuffer(CLICKED_IMAGE);
        }
        else if(mouseOver && hasImage[MOUSEOVER_IMAGE])
        {
            setActiveBuffer(MOUSEOVER_IMAGE);
        }
        else if(stuck && hasImage[ACTIVE_IMAGE])
        {
            setActiveBuffer(ACTIVE_IMAGE);
        }
        else if(hasImage[INACTIVE_IMAGE])
        {
            setActiveBuffer(INACTIVE_IMAGE);
        }
        else
        {
            System.err.println("No images to choose from in ButtonOverlay");
        }
    }

    public void repaint()
    {
        // Prevent painting
    }
}
