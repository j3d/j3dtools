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
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// Application specific imports
// None

/**
 * An interface to start and stop the various input events available.
 * <p>
 *
 * Each method takes both a listener and an overlay. The overlay is needed so
 * that we can determine whether to send the mouse events to that overlay or
 * not based on its bounds and the position of the mouse.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface InputRequester
{
    /**
     * Request that keyboard focus be sent to this listener object now.
     *
     * @param key The key of the object requesting focus now
     */
    public void requestFocus(Object key);

    /**
     * Request that the given listener enable mouse events being sent. If the
     * listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void addMouseListener(MouseListener l, Overlay ovl);

    /**
     * Request that the given listener disable mouse events being sent. If the
     * listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void removeMouseListener(MouseListener l, Overlay ovl);

    /**
     * Request that the given listener enable mouse motion events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void addMouseMotionListener(MouseMotionListener l, Overlay ovl);

    /**
     * Request that the given listener disable mouse motion events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void removeMouseMotionListener(MouseMotionListener l, Overlay ovl);

    /**
     * Request that the given listener enable key events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param key A key object used to determine focus requests
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void addKeyListener(KeyListener l, Object key);

    /**
     * Request that the given listener disable key events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param key A key object used to determine focus requests
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void removeKeyListener(KeyListener l, Object key);
}
