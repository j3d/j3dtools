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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.Timer;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;

import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;

// Application specific imports
// none

/**
 * A listener and handler responsible for executing all navigation commands
 * from mice.
 * <p>
 *
 * Instead of using behaviors this class does the job in a much smoother way
 * by hooking AWT events directly from the canvas.
 * <p>
 * The class works like a standard VRML browser type navigation system. Press
 * the mouse button and drag to get the viewpoint moving. The more you drag
 * the button away from the start point, the faster the movement. The handler
 * does not recognize the use of the Shift key as a modifier to produce an
 * accelarated movement.
 * <p>
 * This class will not change the cursor on the canvas in response to the
 * current mouse and navigation state. It will only notify the state change
 * listener. The canvas is only used to perform any listener registration and
 * removal.
 *
 * @author <a href="http://www.geocities.com/seregi/index.html">Laszlo Seregi</a><br>
 *   Updated for j3d.org by Justin Couch
 * @version $Revision $
 */
public class MouseViewHandler
    implements MouseListener,
               MouseMotionListener,
               ActionListener,
               NavigationStateListener
{
    /** The canvas this handler is operating on */
    private Canvas3D canvas;

    /** Timer used to control smooth motion of the mouse */
    private Timer timer;

    /** The view that we are moving about. */
    private View view;

    /** The transform group above the view that is being moved each frame */
    private TransformGroup viewTg = new TransformGroup();

    /** The transform that belongs to the view transform group */
    private Transform3D viewTx = new Transform3D();

    /** An observer for information about updates for this transition */
    private FrameUpdateListener updateListener;

    /** An observer for navigation state change information */
    private NavigationStateListener navigationListener;

    /** The current navigation state either set from us or externally */
    private int navigationState = WALK_STATE;

    /** The previous state so we can set it back to normal */
    private int previousState = WALK_STATE;

    // The variables from here down are working variables during the draf
    // process. We declare them as class scope so that we don't generate
    // garbage for every mouse movement. The idea is we just re-use these
    // rather than create and destroy each time.

    /** The translation amount set by the last change in drag value */
    private Vector3d dragTranslationAmt = new Vector3d();

    /** A working value for the current frame's translation of the eye */
    private Vector3d oneFrameTranslation = new Vector3d();

    /** A working value for the current frame's rotation of the eye */
    private Transform3D oneFrameRotation = new Transform3D();

    /** The current translation total from the start of the movement */
    private Vector3d viewTranslation = new Vector3d();

    /** The amount to move the view in mouse coords up/down */
    private double mouseRotationY = 0;

    /** The amount to move the view in mouse coords left/right */
    private double mouseRotationX = 0;

    /** The position where the mouse started it's last press */
    private Point2d startMousePos = new Point2d();

    /** The latest position of the mouse from the last event */
    private Point2d latestMousePos = new Point2d();

    /**
     * The difference between the last mouse point from the last event and
     * where it started
     */
    private Point2d mouseDifference = new Point2d();

    /**
     * Create a new mouse handler with no canvas or view information set. This
     * handler will not do anything until both canvas and view transform
     * references have been set.
     */
    public MouseViewHandler()
    {
        //  Timer:
        timer = new Timer(1000, this);
        timer.setInitialDelay(0);
        timer.setRepeats(true);
        timer.stop();
        timer.setLogTimers(false);
        timer.setCoalesce(false);
    }

    /**
     * Set the view and it's related transform group to use. If a canvas is
     * set and these are non-null the interaction will start immediately.
     * Calling with both values as null will remove them and stop the process
     * of updating the canvas.
     *
     * @param view is the View object that we're modifying.
     * @param tg The transform group above the view object that should be used
     */
    public void setViewInfo(View view, TransformGroup tg)
    {
        if(!((view != null) && (tg != null)) ||
            ((view == null) && (tg == null)))
            throw new IllegalArgumentException("View or TG is null when " +
                                               "the other isn't");

        // If the canvas isn't null then set up the listeners or remove them
        // depending on the input values here.
        if(canvas != null)
        {
            if(view != null)
            {
                canvas.addMouseListener(this);
                canvas.addMouseMotionListener(this);
            }
            else
            {
                this.canvas.removeMouseListener(this);
                this.canvas.removeMouseMotionListener(this);
            }
        }

        this.view = view;
        this.viewTg = tg;
    }

    /**
     * Set the canvas to use for this handler. Setting this will immediately
     * register the listeners and start the operation if there is also a view
     * and transform group. To remove the listener then call this method with
     * the canvas parameter null.
     *
     * @param canvas The new canvas to use for this handler
     */
    public void setCanvas(Canvas3D canvas)
    {
        if(canvas != null)
        {
            if(view != null)
            {
                canvas.addMouseListener(this);
                canvas.addMouseMotionListener(this);
            }
        }
        else
        {
            this.canvas.removeMouseListener(this);
            this.canvas.removeMouseMotionListener(this);
        }

        this.canvas = canvas;
    }

    /**
     * Set the listener for frame update notifications. By setting a value of
     * null it will clear the currently set instance
     *
     * @param l The listener to use for this transition
     */
    public void setFrameUpdateListener(FrameUpdateListener l)
    {
        updateListener = l;
    }

    /**
     * Set the listener for navigation state change notifications. By setting
     * a value of null it will clear the currently set instance
     *
     * @param l The listener to use for change updates
     */
    public void setNavigationStateListener(NavigationStateListener l)
    {
        navigationListener = l;
    }

    //----------------------------------------------------------
    // Methods required by the NavigationStateListener
    //----------------------------------------------------------

    /**
     * Notification that the panning state has changed to the new state.
     *
     * @param state One of the state values declared here
     */
    public void setNavigationState(int state)
    {
        navigationState = state;
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

    //----------------------------------------------------------
    // Methods required by the MouseMotionListener
    //----------------------------------------------------------

    /**
     * Process a mouse drag event to change the current movement value from
     * the previously set value to the new value
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDragged(MouseEvent evt)
    {
        latestMousePos.set(evt.getX(), evt.getY());
        mouseDifference.sub(startMousePos, latestMousePos);

        switch(navigationState)
        {
            case WALK_STATE:
                //  Translate on Z:
                dragTranslationAmt.set(0,0,-mouseDifference.y*4);

                //  Rotate around Y:
                mouseRotationY = mouseDifference.x;
                break;

            case PAN_STATE:
                //  Translate on X,Y:
                dragTranslationAmt.set(-mouseDifference.x * 2,
                                       mouseDifference.y * 2,
                                       0);

                break;

            case TILT_STATE:
                //  Rotate arround X,Y:
                mouseRotationX = mouseDifference.y;
                mouseRotationY = mouseDifference.x;
                break;
        }
    }

    /**
     * Process a mouse press and set the timer going. This will cause the
     * navigation state to be set depending on the mouse button pressed.
     *
     * @param evt The event that caused this method to be called
     */
    public void mousePressed(MouseEvent evt)
    {
        int button = evt.getModifiers();

        previousState = navigationState;

        // Set the cursor:
        if((button & MouseEvent.BUTTON1_MASK) != 0)
            navigationState = WALK_STATE;
        else if((button & MouseEvent.BUTTON2_MASK) != 0)
            navigationState = TILT_STATE;
        else if((button & MouseEvent.BUTTON3_MASK) != 0)
            navigationState = PAN_STATE;

        if(navigationListener != null)
            navigationListener.setNavigationState(navigationState);

        viewTg.getTransform(viewTx);
        viewTx.get(viewTranslation);
        startMousePos.set(evt.getX(), evt.getY());
        timer.start();
    }

    /**
     * Process a mouse release to return all the values back to normal. This
     * places all of the transforms back to identity and sets it as though the
     * nothing had happened.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt)
    {
        timer.stop();
        viewTx.normalize();

        mouseRotationY=0;
        mouseRotationX=0;
        oneFrameRotation.setIdentity();
        oneFrameRotation.setIdentity();
        dragTranslationAmt.scale(0);

        viewTg.getTransform(viewTx);

        navigationState = previousState;

        if(navigationListener != null)
            navigationListener.setNavigationState(navigationState);
    }

    /**
     * Not used by this class
     */
    public void mouseMoved(MouseEvent evt)
    {
    }

    /**
     * Not used by this class
     */
    public void mouseClicked(MouseEvent evt)
    {
    }

    /**
     * Process a mouse enter event. We use this to request focus for the
     * canvas so that mouse handling works nicely.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseEntered(MouseEvent evt)
    {
        canvas.requestFocus();
    }

    /**
     * Not used by this class
     */
    public void mouseExited(MouseEvent evt)
    {
    }

    //----------------------------------------------------------
    // Methods required by the ActionListener
    //----------------------------------------------------------

    /**
     * Process an action event from the timer. This event is only for the time
     * and should not be associated with any other sort of action event like
     * menu callbacks.
     *
     * @param evt The event that caused this action to be called
     */
    public void actionPerformed(ActionEvent actionEvent)
    {
        // Some magic numbers here that I don't know where they came from.
        int frameDelay = 10 + (int)(view.getLastFrameDuration() / 2.0);
        double motionDelay = 0.000005 * frameDelay;

        timer.setDelay(frameDelay);

        //  RotateX:
        oneFrameRotation.rotX(mouseRotationX * motionDelay);
        viewTx.mul(oneFrameRotation);

        //  RotateY:
        oneFrameRotation.rotY(mouseRotationY * motionDelay);
        viewTx.mul(oneFrameRotation,viewTx);

        //  Translation:
        oneFrameTranslation.set(dragTranslationAmt);
        oneFrameTranslation.scale(motionDelay);

        viewTx.transform(oneFrameTranslation);
        viewTranslation.add(oneFrameTranslation);
        viewTx.setTranslation(viewTranslation);

        try
        {
            viewTg.setTransform(viewTx);
        }
        catch(Exception e)
        {
            //check for bad transform:
            System.out.println("Transformgroup set invalid");
            viewTx.rotX(0);
            viewTg.setTransform(viewTx);
            // e.printStackTrace();
        }

        if(updateListener !=null)
            updateListener.viewerPositionUpdated(viewTx);
    }
}
