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
import java.awt.event.*;

import javax.media.j3d.*;

import java.awt.AWTEvent;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;


// Application specific imports
// None

/**
 * A Java3D behaviour that traps and funnels AWT events to overlays.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class AWTEventBehavior extends Behavior
    implements InputRequester
{
    /** Value of the rebuild criteria post ID */
    private static final int REBUILD_ID = 1008;

    /** The wakeup condition to force the update */
    private WakeupOnAWTEvent awtCondition;

    /** Listener for dealing with mouse events */
    private ArrayList mouseOverlays;

    /** Mapping of overlays to their mouse listeners */
    private HashMap mouseListeners;

    /** Listener for dealing with mouse motion events */
    private ArrayList motionOverlays;

    /** Mapping of overlays to their mouse motion listeners */
    private HashMap motionListeners;

    /** Current listener for dealing with key events */
    private KeyListener currentKey;

    /** Map of all the keys to the focus listeners */
    private HashMap keyListeners;

    /** Condition for when we want to change the events we are listening for */
    private WakeupOnBehaviorPost postCondition;

    /** The condition to use each frame as needed */
    private WakeupCondition bothConditions;

    /** The last found mouse overlay for optimisation purposes */
    private Overlay lastMouseOverlay;

    /** The last found mouse listener for optimisation purposes */
    private MouseListener lastMouse;

    /** The last found mouse motion overlay for optimisation purposes */
    private Overlay lastMotionOverlay;

    /** The last found mouse motion listener for optimisation purposes */
    private MouseMotionListener lastMotion;

    /**
     * Flag to say if we are in a drag currently and should be tracking the
     * mouse.
     */
    private boolean inDrag;

    /**
     * Create a new behavior that manages the update of a single entity
     *
     * @param entity The entity to process update requests for
     */
    public AWTEventBehavior()
    {
        postCondition = new WakeupOnBehaviorPost(this, REBUILD_ID);

        mouseListeners = new HashMap();
        mouseOverlays = new ArrayList();
        motionListeners = new HashMap();
        motionOverlays = new ArrayList();
        keyListeners = new HashMap();
        inDrag = false;
    }

    /**
     * Request that keyboard focus be sent to this listener object now.
     *
     * @param key The key of the object requesting focus now
     */
    public void requestFocus(Object key)
    {
        if(keyListeners.containsKey(key))
            currentKey = (KeyListener)keyListeners.get(key);
    }

    /**
     * Request that the given listener enable mouse events being sent. If the
     * listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void addMouseListener(MouseListener l, Overlay ovl)
    {
        if(l == null)
            return;

        boolean new_critters = (mouseOverlays.size() == 0);

        mouseListeners.put(ovl, l);
        mouseOverlays.add(ovl);

        if(new_critters)
        {
            rebuildCriteria();
            postId(REBUILD_ID);
        }
    }

    /**
     * Request that the given listener disable mouse events being sent. If the
     * listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void removeMouseListener(MouseListener l, Overlay ovl)
    {
        if(l == null)
            return;

        mouseListeners.remove(ovl);
        mouseOverlays.remove(ovl);

        if(mouseListeners.size() == 0)
            rebuildCriteria();
    }

    /**
     * Request that the given listener enable mouse motion events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void addMouseMotionListener(MouseMotionListener l, Overlay ovl)
    {
        if(l == null)
            return;

        boolean new_critters = (motionListeners.size() == 0);

        motionListeners.put(ovl, l);
        motionOverlays.add(ovl);

        if(new_critters)
        {
            rebuildCriteria();
            postId(REBUILD_ID);
        }
    }

    /**
     * Request that the given listener disable mouse motion events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void removeMouseMotionListener(MouseMotionListener l, Overlay ovl)
    {
        if(l == null)
            return;

        motionListeners.remove(ovl);
        motionOverlays.remove(ovl);

        if(motionListeners.size() == 0)
            rebuildCriteria();
    }

    /**
     * Request that the given listener enable key events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void addKeyListener(KeyListener l, Object key)
    {
        if(l == null)
            return;

        boolean new_critters = (keyListeners.size() == 0);

        if(l != null)
            keyListeners.put(key, l);

        if(new_critters)
        {
            rebuildCriteria();
            postId(REBUILD_ID);
        }
    }

    /**
     * Request that the given listener disable key events being sent.
     * If the listener instance is null, this request is ignored.
     *
     * @param l The listener to manage events for
     * @param ovl The overlay to base the mouse bounds handling on
     */
    public void removeKeyListener(KeyListener l, Object key)
    {
        if(l == null)
            return;

        Object to_go = keyListeners.get(key);

        if(to_go == l)
            keyListeners.remove(key);

        if(keyListeners.size() == 0)
            rebuildCriteria();
    }

    //------------------------------------------------------------------------
    // Methods overridden from the base Behavior class
    //------------------------------------------------------------------------

    /**
     * Initialize the behavior to start working now. Sets up the initial
     * wakeup condition.
     */
    public void initialize()
    {
        if(awtCondition != null)
            wakeupOn(bothConditions);
        else
            wakeupOn(postCondition);
    }

    /**
     * Process the behavior that has been woken up by the given set of
     * conditions.
     *
     * @param conditions The list of conditions satisfied
     */
    public void processStimulus(Enumeration conditions)
    {
        // If the post condition has fired, then this is just to allow us
        // to register a new set of AWT conditions. We don't need to do
        // anything with it. We are only concerned about the AWT conditions
        // and needing further processing
        if(awtCondition.hasTriggered())
        {
            AWTEvent[] events = awtCondition.getAWTEvent();
            MouseListener mouse = null;
            MouseMotionListener motion = null;
            MouseEvent me;

            for(int i = 0; i < events.length; i++)
            {
                switch(events[i].getID())
                {
                    case KeyEvent.KEY_PRESSED:
                        if(currentKey != null)
                            currentKey.keyPressed((KeyEvent)events[i]);
                        break;

                    case KeyEvent.KEY_RELEASED:
                        if(currentKey != null)
                            currentKey.keyReleased((KeyEvent)events[i]);
                        break;

                    case KeyEvent.KEY_TYPED:
                        if(currentKey != null)
                            currentKey.keyTyped((KeyEvent)events[i]);
                        break;

                    case MouseEvent.MOUSE_PRESSED:
                        me = (MouseEvent)events[i];
                        mouse = getMouseListener(me.getPoint());

                        if(mouse != null)
                            mouse.mousePressed(me);
                        break;

                    case MouseEvent.MOUSE_RELEASED:
                        me = (MouseEvent)events[i];
                        mouse = getMouseListener(me.getPoint());

                        if(mouse != null)
                            mouse.mouseReleased(me);
                        break;

                    case MouseEvent.MOUSE_CLICKED:
                        me = (MouseEvent)events[i];
                        mouse = getMouseListener(me.getPoint());

                        if(mouse != null)
                            mouse.mouseClicked(me);
                        break;

                    case MouseEvent.MOUSE_ENTERED:
                        me = (MouseEvent)events[i];
                        mouse = getMouseListener(me.getPoint());

                        if(mouse != lastMouse)
                        {
                            if(lastMouse != null)
                            {
                                lastMouse.mouseExited(cloneEvent(me, true));
                            }

                            if(!inDrag)
                            {
                                mouse.mouseEntered(me);

                                lastMouse = mouse;
                            }
                        }

                        break;

                    case MouseEvent.MOUSE_EXITED:
                        me = (MouseEvent)events[i];
                        mouse = getMouseListener(me.getPoint());

                        if(!inDrag)
                        {
                            if(mouse != null)
                                mouse.mouseExited(me);

                            // We've exited completely, so remove everything
                            lastMouse = null;
                            lastMotion = null;
                        }

                        break;

                    case MouseEvent.MOUSE_DRAGGED:
                        me = (MouseEvent)events[i];

                        inDrag = true;

                        if(lastMotion != null)
                            lastMotion.mouseDragged(me);
                        break;

                    case MouseEvent.MOUSE_MOVED:
                        me = (MouseEvent)events[i];
                        mouse = getMouseListener(me.getPoint());

                        if(mouse != lastMouse)
                        {
                            // If the last and current overlays that we are
                            // over for enter/exit events are not the same then
                            // work out whether we have left or are entering or
                            // maybe doing both.
                            if(lastMouse == null)
                            {
                                // We've just entered a new overlay, so let it
                                // know
                                mouse.mouseEntered(cloneEvent(me, false));
                            }
                            else if(mouse == null)
                            {
                                // The old overlay was valid and now our new
                                // one is not, so that means we've left the
                                // old one.
                                lastMouse.mouseExited(cloneEvent(me, true));
                            }
                            else
                            {
                                lastMouse.mouseExited(cloneEvent(me, true));
                                mouse.mouseEntered(cloneEvent(me, false));
                            }

                            lastMouse = mouse;
                        }
                        else
                        {
                            // The place that we are over is the same for
                            // both. Either they are both null or both valid.
                            // Either way, we check to see if there is now a
                            // motion listener for this event under this point
                            // and send an event off anyway.
                            motion = getMotionListener(me.getPoint());

                            if(motion != null)
                                motion.mouseMoved(me);

                            lastMotion = motion;
                        }
                        break;
                }
            }
        }

        if(awtCondition != null)
            wakeupOn(bothConditions);
        else
            wakeupOn(postCondition);
    }

    /**
     * Check the various input criteria and see what we should be using
     * currently.
     */
    private void rebuildCriteria()
    {
        long mask = 0;

        // For mouse listeners, we need both button and motion events because
        // our windows are smaller than the entire canvas so we need to track
        // the mouse within the extents of the canvas any time we need to know
        // simple enter/exit events.
        //
        // Conversely, if we have mouse movement events only without anyone
        // wanting enter/exit events, we can do that because we still only
        // need to track the drag events and can determine who to send them
        // to at the appropriate time.
        if(mouseListeners.size() != 0)
            mask = AWTEvent.MOUSE_EVENT_MASK |
                   AWTEvent.MOUSE_MOTION_EVENT_MASK;
        else if(motionListeners.size() != 0)
            mask = AWTEvent.MOUSE_MOTION_EVENT_MASK;

        if(keyListeners.size() != 0)
            mask |= AWTEvent.KEY_EVENT_MASK;

        if(mask != 0)
        {
            awtCondition = new WakeupOnAWTEvent(mask);
            WakeupCriterion[] tmp = { awtCondition, postCondition };

            bothConditions = new WakeupOr(tmp);
        }
        else if(awtCondition != null)
        {
            // means we should clear the old condition
            awtCondition = null;
            bothConditions = null;
        }
    }

    /**
     * returns the first overlay that contains the specified point.  If the
     * windows are stacked then the top most overlay will be picked.
     *
     * @param p The point to get the mouse overlay for
     * @return The mouse listener for the overlay under this point or null
     */
    private MouseListener getMouseListener(Point p)
    {
        if((lastMouseOverlay != null) && lastMouseOverlay.contains(p))
            return lastMouse;

        int n = mouseOverlays.size();
        Overlay w = null;

        for(int i = 0;i < n; i++)
        {
            w = (Overlay)mouseOverlays.get(i);
            if(w.isVisible() && w.contains(p))
                break;

            w = null;
        }

        if(w != null)
            lastMouseOverlay = w;
        else
            lastMouseOverlay = null;

        MouseListener l = (MouseListener)mouseListeners.get(w);

        return l;
    }

    /**
     * returns the first overlay that contains the specified point.  If the
     * windows are stacked then the top most overlay will be picked.
     *
     * @param p The point to get the mouse overlay for
     * @return The mouse listener for the overlay under this point or null
     */
    private MouseMotionListener getMotionListener(Point p)
    {
        if((lastMotionOverlay != null) && lastMotionOverlay.contains(p))
            return lastMotion;

        int n = motionOverlays.size();
        Overlay w = null;

        for(int i = 0;i < n; i++)
        {
            w = (Overlay)motionOverlays.get(i);
            if(w.isVisible() && w.contains(p))
                break;

            w = null;
        }

        if(w != null)
            lastMotionOverlay = w;
        else
            lastMotionOverlay = null;

        MouseMotionListener l = (MouseMotionListener)motionListeners.get(w);

        return l;
    }

    /**
     * Convenience method to create a new clone of a mouse event, but setting
     * the ID field to either enter or exit.
     *
     * @param evt The source event to copy data from
     * @param exit true if this is an exit event, false for enter
     * @return A cloned, new instance of the event
     */
    private MouseEvent cloneEvent(MouseEvent evt, boolean exit)
    {
        return new MouseEvent(evt.getComponent(),
                              exit ? MouseEvent.MOUSE_EXITED :
                                     MouseEvent.MOUSE_ENTERED,
                              evt.getWhen(),
                              evt.getModifiers(),
                              evt.getX(),
                              evt.getY(),
                              evt.getClickCount(),
                              evt.isPopupTrigger(),
                              evt.getButton());
    }
}
