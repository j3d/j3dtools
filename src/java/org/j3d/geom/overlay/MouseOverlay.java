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
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.media.j3d.Canvas3D;

// Application specific imports
// none

/**
 * An overlay that is used like an ordinary drawing canvas that interacts with
 * the mouse.
 * <P>
 *
 * The class does not automatically register itself for mouse input. That is up
 * to the derived class to make sure it properly registers itself for the right
 * events.
 * <p>
 *
 * <B>Note:</B> This class does not call the <CODE>repaint()</CODE> method
 * after each mouse event. It is the responsibility of the derived class to
 * make sure the screen gets updated with any information regarding the mouse
 * event.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public abstract class MouseOverlay extends InteractiveOverlayBase
    implements MouseListener, MouseMotionListener
{
    /**
     * Creates a new overlay covering the given canvas bounds. It has two
     * buffers. Updates are managed automatically. This Overlay is not usable
     * until you attach it to the view platform transform.
     *
     * @param canvas3D Canvas being drawn onto
     * @param bounds Bounds on the canvas covered by the overlay
     */
    protected MouseOverlay(Canvas3D canvas3D, Rectangle bounds)
    {
        super(canvas3D, bounds);
    }

    /**
     * Constructs an overlay window with an update manager. It has two buffers.
     * This window will not be visible unless it is added to the scene under
     * the view platform transform.
     *
     * @param canvas3D The canvas the overlay is drawn on
     * @param bounds The part of the canvas covered by the overlay
     * @param updateManager Responsible for allowing the Overlay to update
     *   between renders. If this is null a default manager is created
     */
    protected MouseOverlay(Canvas3D canvas3D,
                           Rectangle bounds,
                           UpdateManager updateManager)
    {
        super(canvas3D, bounds, updateManager);
    }

    /**
     * Constructs an overlay window that can have alpha capabilities. This
     * window will not be visible unless it is added to the scene under the
     * view platform transform.
     *
     * @param canvas3D The canvas the overlay is drawn on
     * @param bounds The part of the canvas covered by the overlay
     * @param clipAlpha Should the polygon clip where alpha is zero
     * @param blendAlpha Should we blend to background where alpha is < 1
     * @param hasButtonEvents true to recieve mouse button events
     * @param hasMotionEvents true to recieve mouse motion events
     * @param processAll true to process all events from the behaviour, or
     *    false to use just the last one
     */
    protected MouseOverlay(Canvas3D canvas3D,
                           Rectangle bounds,
                           boolean clipAlpha,
                           boolean blendAlpha)
    {
        super(canvas3D, bounds, clipAlpha, blendAlpha);
    }

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     *
     * @param canvas3D The canvas the overlay is drawn on
     * @param bounds The part of the canvas covered by the overlay
     * @param clipAlpha Should the polygon clip where alpha is zero
     * @param blendAlpha Should we blend to background where alpha is < 1
     * @param updateManager Responsible for allowing the Overlay to update
     *   between renders. If this is null a default manager is created
     */
    protected MouseOverlay(Canvas3D canvas3D,
                           Rectangle bounds,
                           boolean clipAlpha,
                           boolean blendAlpha,
                           UpdateManager updateManager,
                           boolean hasButtonEvents)
    {
        super(canvas3D, bounds, clipAlpha, blendAlpha, updateManager);
    }

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     *
     * @param canvas3D The canvas the overlay is drawn on
     * @param bounds The part of the canvas covered by the overlay
     * @param clipAlpha Should the polygon clip where alpha is zero
     * @param blendAlpha Should we blend to background where alpha is < 1
     * @param updateManager Responsible for allowing the Overlay to update
     *   between renders. If this is null a default manager is created
     * @param numBuffers The number of buffers to generate, the default is two
     */
    protected MouseOverlay(Canvas3D canvas3D,
                           Rectangle bounds,
                           boolean clipAlpha,
                           boolean blendAlpha,
                           UpdateManager updateManager,
                           int numBuffers)
    {
        super(canvas3D, bounds, clipAlpha, blendAlpha, updateManager, numBuffers);
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
    }

    /**
     * Process a mouse release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt)
    {
    }

    /**
     * Process a mouse click event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseClicked(MouseEvent evt)
    {
    }

    /**
     * Process a mouse enter event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseEntered(MouseEvent evt)
    {
    }

    /**
     * Process a mouse exited event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseExited(MouseEvent evt)
    {
    }

    //------------------------------------------------------------------------
    // Methods for MouseMotionListener events
    //------------------------------------------------------------------------

    /**
     * Process a mouse drag event
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDragged(MouseEvent evt)
    {
    }

    /**
     * Process a mouse movement event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseMoved(MouseEvent evt)
    {
    }

    //------------------------------------------------------------------------
    // Local methods
    //------------------------------------------------------------------------
}