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
import java.awt.Dimension;

import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.ArrayList;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Texture2D;

// Application specific imports
// none

/**
 * An texture overlay extension that allows input events to be captured.
 * <p>
 *
 * This class extends the standard texture with the InteractiveOverlay
 * interface so that it may handle mouse and keyboard information. Typically
 * this would be used to place an image on screen like a map and then get
 * the mouse feedback to put the user in that position.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class InteractiveTextureOverlay extends TextureOverlay
    implements InteractiveOverlay
{
    /** Input requester for input events */
    private InputRequester requester;

    /** A buffer to hold listeners till a requester is set */
    private ArrayList buffList;

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     * If the bounds are null, then resize the overlay to fit the canvas
     * and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @throws IllegalArgumentException Both the canvas and bounds are null
     */
    public InteractiveTextureOverlay(Canvas3D canvas, Dimension size)
    {
        super(canvas, size);
        if (buffList == null)
            buffList = new ArrayList();
    }

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     * If the bounds are null, then resize the overlay to fit the canvas
     * and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @param The texture to be displayed. May be null
     * @throws IllegalArgumentException Both the canvas and bounds are null
     */
    public InteractiveTextureOverlay(Canvas3D canvas,
                          Dimension size,
                          Texture2D texture)
    {
        super(canvas, size, texture);

        if (buffList == null)
            buffList = new ArrayList();
    }

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     * If the bounds are null, then resize the overlay to fit the canvas
     * and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @param hasAlpha True if the texture has an alpha component
     * @param updateManager Responsible for allowing the Overlay to update
     *   between renders. If this is null a default manager is created
     * @param The texture to be displayed. May be null
     * @throws IllegalArgumentException Both the canvas and bounds are null
     */
    public InteractiveTextureOverlay(Canvas3D canvas,
                          Dimension size,
                          boolean hasAlpha,
                          UpdateManager updateManager,
                          Texture2D texture)
    {
        super(canvas, size, hasAlpha, updateManager, texture);

        if (buffList == null)
            buffList = new ArrayList();
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

        Object listener;
        for(int i=0; i < buffList.size();i++)
        {
           listener = buffList.get(i);
           if (listener instanceof MouseListener)
              requester.addMouseListener((MouseListener)listener, this);
           else if (listener instanceof MouseMotionListener)
              requester.addMouseMotionListener((MouseMotionListener)listener, this);
           else if (listener instanceof KeyListener)
              requester.addKeyListener((KeyListener)listener, this);

        }

        buffList.clear();
    }

    //------------------------------------------------------------------------
    // Local convenience methods
    //------------------------------------------------------------------------

    /**
     * Request that keyboard focus be sent to this listener object now.
     *
     * @param key The key of the object requesting focus now
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
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void addMouseListener(MouseListener l)
    {
        if(requester != null)
            requester.addMouseListener(l, this);
        else
            buffList.add(l);
    }

    /**
     * Request that the given listener disable mouse events being sent. If the
     * listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void removeMouseListener(MouseListener l)
    {
        if(requester != null)
            requester.removeMouseListener(l, this);
        else
            buffList.remove(l);
    }

    /**
     * Request that the given listener enable mouse motion events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void addMouseMotionListener(MouseMotionListener l)
    {
        if(requester != null)
            requester.addMouseMotionListener(l, this);
        else
            buffList.add(l);
    }

    /**
     * Request that the given listener disable mouse motion events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void removeMouseMotionListener(MouseMotionListener l)
    {
        if(requester != null)
            requester.removeMouseMotionListener(l, this);
        else
            buffList.remove(l);
    }

    /**
     * Request that the given listener enable key events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void addKeyListener(KeyListener l)
    {
        if(requester != null)
            requester.addKeyListener(l, this);
        else
            buffList.add(l);
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
        else
            buffList.remove(l);
    }
}
