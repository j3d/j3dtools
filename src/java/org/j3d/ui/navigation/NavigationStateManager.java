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

package org.j3d.ui.navigation;

// Standard imports
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import javax.media.j3d.Canvas3D;

// Application specific imports
import org.j3d.util.ImageLoader;

/**
 * A handler for navigation state to make sure that all the right events are
 * passed around the system and everyone agrees on the same state.
 * <p>
 * This class will change the cursor on the canvas in response to the current
 * mouse and navigation state. There are three cursors used for walk, tilt and
 * pan states. These can be found as:
 * <ul>
 * <li>Pan:  images/navigation/CursorPan.gif</li>
 * <li>Tilt: images/navigation/CursorTilt.gif</li>
 * <li>Walk: images/navigation/CursorWalk.gif</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision $
 */
public class NavigationStateManager
{
    /** The name of the file for the pan cursor image */
    private static final String PAN_CURSOR = "images/navigation/CursorPan.gif";

    /** The name of the file for the tilt cursor image */
    private static final String TILT_CURSOR = "images/navigation/CursorTilt.gif";

    /** The name of the file for the walk cursor image */
    private static final String WALK_CURSOR = "images/navigation/CursorWalk.gif";

    /** The canvas this handler is operating on */
    private Canvas3D canvas;

    /** Cursor used to represent the pan state */
    private Cursor panCursor = null;

    /** Cursor used to represent the tilt state */
    private Cursor tiltCursor = null;

    /** Cursor used to represent the walk state */
    private Cursor walkCursor = null;

    /** The current navigation state either set from us or externally */
    private int navigationState;

    /** The mouse view handler for mouse events */
    private MouseViewHandler mouseHandler;

    /** The mouse view handler for mouse events */
    private NavigationToolbar toolbarHandler;

    //----------------------------------------------------------
    // Inner class definitions
    //----------------------------------------------------------

    // We use two inner classes here that both implement the NavigationState
    // listener methods. As we really need to know which class the event came
    // from it is much easier to deal with it through a set of inner classes
    // like this rather than through other means.

    /**
     * An inner class that represents the listener for the toolbar.
     * <p>
     * All events are passed from here to the canvas and the mouse handler.
     */
    private class ToolbarHandler implements NavigationStateListener
    {
        /**
         * Notification that the panning state has changed to the new state.
         *
         * @param state One of the state values declared here
         */
        public void setNavigationState(int state)
        {
            navigationState = state;

            switch(navigationState)
            {
                case WALK_STATE:
                    canvas.setCursor(walkCursor);
                    break;

                case PAN_STATE:
                    canvas.setCursor(panCursor);
                    break;

                case TILT_STATE:
                    canvas.setCursor(tiltCursor);
                    break;
            }

            mouseHandler.setNavigationState(state);
        }

        /**
         * Callback to ask the listener what navigation state it thinks it is
         * in.
         *
         * @return The state that the listener thinks it is in
         */
        public int getNavigationState()
        {
            return navigationState;
        }
    }

    /**
     * An inner class that represents the listener for the toolbar.
     * <p>
     * All events are passed from here to the canvas and the mouse handler.
     */
    private class MouseHandler implements NavigationStateListener
    {
        /**
         * Notification that the panning state has changed to the new state.
         *
         * @param state One of the state values declared here
         */
        public void setNavigationState(int state)
        {
            navigationState = state;

            switch(navigationState)
            {
                case WALK_STATE:
                    canvas.setCursor(walkCursor);
                    break;

                case PAN_STATE:
                    canvas.setCursor(panCursor);
                    break;

                case TILT_STATE:
                    canvas.setCursor(tiltCursor);
                    break;
            }

            toolbarHandler.setNavigationState(state);
        }

        /**
         * Callback to ask the listener what navigation state it thinks it is
         * in.
         *
         * @return The state that the listener thinks it is in
         */
        public int getNavigationState()
        {
            return navigationState;
        }
    }

    //----------------------------------------------------------
    // Class body
    //----------------------------------------------------------

    /**
     * Create a new state manager that deals with the given canvas. The default
     * state is set to WALK
     *
     * @param canvas The canvas to use
     */
    public NavigationStateManager(Canvas3D canvas)
    {
        if(canvas == null)
            throw new IllegalArgumentException("Null canvas provided");

        this.canvas = canvas;
        loadCursors();

        setNavigationState(NavigationStateListener.WALK_STATE);
    }

    /**
     * Set the toolbar instance to use. Setting a value of null will clear the
     * currently set instance.
     *
     * @param tbr The new toolbar instance to use
     */
    public void setToolbar(NavigationToolbar tbr)
    {
        if(toolbarHandler != null)
            toolbarHandler.setNavigationStateListener(null);

        toolbarHandler = tbr;

        if(toolbarHandler != null)
        {
            toolbarHandler.setNavigationStateListener(new ToolbarHandler());
            toolbarHandler.setNavigationState(navigationState);
        }
    }

    /**
     * Set the toolbar instance to use. Setting a value of null will clear the
     * currently set instance.
     *
     * @param tbr The new toolbar instance to use
     */
    public void setMouseHandler(MouseViewHandler view)
    {
        if(mouseHandler != null)
            mouseHandler.setNavigationStateListener(null);

        mouseHandler = view;

        if(mouseHandler != null)
        {
            mouseHandler.setNavigationStateListener(new MouseHandler());
            mouseHandler.setNavigationState(navigationState);
        }
    }

    /**
     * Notification that the panning state has changed to the new state.
     *
     * @param state One of the state values declared here
     */
    public void setNavigationState(int state)
    {
        navigationState = state;

        switch(navigationState)
        {
            case NavigationStateListener.WALK_STATE:
                canvas.setCursor(walkCursor);
                break;

            case NavigationStateListener.PAN_STATE:
                canvas.setCursor(panCursor);
                break;

            case NavigationStateListener.TILT_STATE:
                canvas.setCursor(tiltCursor);

                break;
        }

        toolbarHandler.setNavigationState(state);
        mouseHandler.setNavigationState(state);
    }

    /**
     * Callback to ask the listener what navigation state it thinks it is
     * in.
     *
     * @return The state that the listener thinks it is in
     */
    public int getNavigationState()
    {
        return navigationState;
    }

    /**
     * Private convenience method to load cursors for use by this handler
     */
    private void loadCursors()
    {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image img = ImageLoader.loadImage(PAN_CURSOR);
        Point center = new Point();

        center.x = img.getWidth(null) / 2;
        center.y = img.getHeight(null) / 2;

        panCursor = tk.createCustomCursor(img, center ,null);

        img = ImageLoader.loadImage(WALK_CURSOR);
        center = new Point();

        center.x = img.getWidth(null) / 2;
        center.y = img.getHeight(null) / 2;

        walkCursor = tk.createCustomCursor(img, center ,null);

        img = ImageLoader.loadImage(TILT_CURSOR);
        center = new Point();

        center.x = img.getWidth(null) / 2;
        center.y = img.getHeight(null) / 2;

        tiltCursor = tk.createCustomCursor(img, center ,null);
    }
}
