/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.overlay;

// Standard imports
import java.awt.Dimension;

import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.media.j3d.Canvas3D;

// Application specific imports
// none

/**
 * An implementation of the interactive overlay.
 * <P>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class InteractiveOverlayBase extends OverlayBase
    implements InteractiveOverlay
{
    /** Input requester for input events */
    private InputRequester requester;

    /**
     * Creates a new overlay covering the given canvas bounds. It has two
     * buffers. Updates are managed automatically. This Overlay is not usable
     * until you attach it to the view platform transform. If the bounds are
     * null, then resize the overlay to fit the canvas and then track the size
     * of the canvas.
     *
     * @param canvas Canvas being drawn onto
     * @param size The size of the overlay in pixels
     */
    protected InteractiveOverlayBase(Canvas3D canvas, Dimension size)
    {
        super(canvas, size);
    }

    /**
     * Constructs an overlay window with an update manager. It has two buffers.
     * This window will not be visible unless it is added to the scene under
     * the view platform transform. If the bounds are null, then resize the
     * overlay to fit the canvas and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @param manager Responsible for allowing the Overlay to update
     *   between renders. If this is null a default manager is created
     */
    protected InteractiveOverlayBase(Canvas3D canvas,
                                     Dimension size,
                                     UpdateManager manager)
    {
        super(canvas, size, manager);
    }

    /**
     * Constructs an overlay window that can have alpha capabilities. This
     * window will not be visible unless it is added to the scene under the
     * view platform transform. If the bounds are null, then resize the
     * overlay to fit the canvas and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @param clipAlpha Should the polygon clip where alpha is zero
     * @param blendAlpha Should we blend to background where alpha is < 1
     */
    protected InteractiveOverlayBase(Canvas3D canvas,
                                     Dimension size,
                                     boolean clipAlpha,
                                     boolean blendAlpha)
    {
        super(canvas, size, clipAlpha, blendAlpha);
    }

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     * If the bounds are null, then resize the overlay to fit the canvas
     * and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @param clipAlpha Should the polygon clip where alpha is zero
     * @param blendAlpha Should we blend to background where alpha is < 1
     * @param updateManager Responsible for allowing the Overlay to update
     *   between renders. If this is null a default manager is created
     */
    protected InteractiveOverlayBase(Canvas3D canvas,
                       Dimension size,
                       boolean clipAlpha,
                       boolean blendAlpha,
                       UpdateManager updateManager)
    {
        super(canvas, size, clipAlpha, blendAlpha, updateManager);
    }

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     * If the bounds are null, then resize the overlay to fit the canvas
     * and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @param clipAlpha Should the polygon clip where alpha is zero
     * @param blendAlpha Should we blend to background where alpha is < 1
     * @param updateManager Responsible for allowing the Overlay to update
     *   between renders. If this is null a default manager is created
     * @param numBuffers The number of buffers to generate, the default is two
     */
    protected InteractiveOverlayBase(Canvas3D canvas,
                                     Dimension size,
                                     boolean clipAlpha,
                                     boolean blendAlpha,
                                     UpdateManager updateManager,
                                     int numBuffers)
    {
        super(canvas,
              size,
              clipAlpha,
              blendAlpha,
              updateManager,
              numBuffers);
    }

    //------------------------------------------------------------------------
    // Methods from the InteractiveOverlay interface
    //------------------------------------------------------------------------

    /**
     * Set the input requestor so that the overlay may manage when it requires
     * input events. If the system is shutting down or the overlay is being
     * removed, the parameter value may be null to clear a previously held
     * instance.
     *
     * @param req The requestor instance to use or null
     */
    public void setInputRequester(InputRequester req)
    {
        requester = req;
    }

    //------------------------------------------------------------------------
    // Local convenience methods
    //------------------------------------------------------------------------

    /**
     * Request that keyboard focus be sent to this listener object now.
     */
    public void requestFocus()
    {
        if(requester != null)
            requester.requestFocus(this);
    }

    /**
     * Request that the given listener enable mouse events being sent. If the
     * listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     */
    public void addMouseListener(MouseListener l)
    {
        if(requester != null)
            requester.addMouseListener(l, this);
    }

    /**
     * Request that the given listener disable mouse events being sent. If the
     * listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     */
    public void removeMouseListener(MouseListener l)
    {
        if(requester != null)
            requester.removeMouseListener(l, this);
    }

    /**
     * Request that the given listener enable mouse motion events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     */
    public void addMouseMotionListener(MouseMotionListener l)
    {
        if(requester != null)
            requester.addMouseMotionListener(l, this);
    }

    /**
     * Request that the given listener disable mouse motion events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     */
    public void removeMouseMotionListener(MouseMotionListener l)
    {
        if(requester != null)
            requester.removeMouseMotionListener(l, this);
    }

    /**
     * Request that the given listener enable key events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     */
    public void addKeyListener(KeyListener l)
    {
        if(requester != null)
            requester.addKeyListener(l, this);
    }

    /**
     * Request that the given listener disable key events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     */
    public void removeKeyListener(KeyListener l)
    {
        if(requester != null)
            requester.removeKeyListener(l, this);
    }
}
