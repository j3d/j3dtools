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

package org.j3d.renderer.java3d.navigation;

// Standard imports
import java.awt.Component;
import java.awt.event.MouseEvent;

// Application specific imports
import org.j3d.ui.navigation.NavigationState;
import org.j3d.ui.navigation.NavigationStateListener;
import org.j3d.ui.navigation.NavigationMouseProcessor;

/**
 * A listener and handler responsible for executing all navigation commands
 * from mice to move a viewpoint around a scene.
 * <p>
 *
 * This class does not contain any direct event handling. Instead it assumes
 * that another class with either derive from it or delegate to it to do the
 * actual movement processing. This allows it to be used as a standard AWT
 * event listener or a Java3D behaviour as required by the software.
 * <p>
 *
 * Separate states are allowed to be set for each button. Once one button is
 * pressed, all the other button presses are ignored. By default, all the
 * buttons start with no state set. The user will have to explicitly set
 * the state for each button to get them to work.
 * <p>
 *
 * Depending on the requirements of the user, the speed scale factor can be computed
 * @version $Revision: 1.1 $
 */
public class NavigationHandler extends NavigationProcessor
    implements NavigationMouseProcessor
{
    /** An observer for navigation state change information */
    private NavigationStateListener navigationListener;

    /**
     * The current navigation state either set from us or externally as
     * the mouse if being dragged around. This is different to the state
     * that a given mouse button will generate
     */
    private int navigationState;

    /** The previous state so we can set it back to normal */
    private int previousState;

    /** The navigation state for use with button 1 */
    private int buttonOneState;

    /** The navigation state for use with button 2 */
    private int buttonTwoState;

    /** The navigation state for use with button 3 */
    private int buttonThreeState;

    /**
     * Flag indicating that we are currently doing something and should
     * ignore any current mouse presses.
     */
    private boolean movementInProgress;

    /** The mouse button that is currently being pressed */
    private int activeButton;

    /** The current movement speed in m/s in the local coordinate system */
    private float speed;

    // Java3D stuff for terrain following and collision detection

    /** Width of the screen currently. Floats used for division accuracy. */
    private float screenWidth;

    /** Width of the screen currently */
    private float screenHeight;

    /** The position where the mouse started it's last press */
    private int startMousePosX;
    private int startMousePosY;

    /** Temporary vector for passing motion information to the base class */
    private float[] vectorTmp;

    /**
     * Create a new mouse handler with no view information set. This
     * handler will not do anything until the view transform
     * references have been set and the navigation modes for at least one
     * mouse button.
     */
    public NavigationHandler()
    {
        previousState = NavigationState.NO_STATE;
        buttonOneState = NavigationState.NO_STATE;
        buttonTwoState = NavigationState.NO_STATE;
        buttonThreeState = NavigationState.NO_STATE;
        movementInProgress = false;

        vectorTmp = new float[3];
    }

    /**
     * Set the ability to use a given state within the handler for a
     * specific mouse button (up to 3). This allows the caller to control
     * exactly what states are allowed to be used and with which buttons.
     * Note that it is quite legal to set all three buttons to the same
     * navigation state
     *
     * @param button The mouse button value from
     *    {@link java.awt.event.MouseEvent}
     * @param state The navigation state to use for that button
     */
    public void setButtonNavigation(int button, int state)
    {
        switch(button)
        {
            case MouseEvent.BUTTON1_MASK:
                buttonOneState = state;
                break;

            case MouseEvent.BUTTON2_MASK:
                buttonTwoState = state;
                break;

            case MouseEvent.BUTTON3_MASK:
                buttonThreeState = state;
                break;
        }
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

    /**
     * Process a mouse press and set the behavior running. This will cause the
     * navigation state to be set depending on the mouse button pressed.
     *
     * @param evt The event that caused this method to be called
     */
    public void mousePressed(MouseEvent evt)
    {
        if(movementInProgress)
            return;

        int button = evt.getModifiers();

        previousState = getNavigationState();
        activeButton = (button & (int)MouseEvent.MOUSE_EVENT_MASK);

        int new_state = NavigationState.NO_STATE;

        // Set the cursor:
        if((button & MouseEvent.BUTTON1_MASK) != 0)
            new_state = buttonOneState;
        else if((button & MouseEvent.BUTTON2_MASK) != 0)
            new_state = buttonTwoState;
        else if((button & MouseEvent.BUTTON3_MASK) != 0)
            new_state = buttonThreeState;

        if(navigationListener != null)
            navigationListener.setNavigationState(navigationState);

        // Set the current navigation state
        setNavigationState(new_state);

        if(new_state == NavigationState.NO_STATE)
            return;

        startMousePosX = evt.getX();
        startMousePosY = evt.getY();

        Component comp = (Component)evt.getSource();
        screenHeight = comp.getHeight();
        screenWidth = comp.getWidth();

        startMove();
    }

    /**
     * Process a mouse drag event to change the current movement value from
     * the previously set value to the new value
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDragged(MouseEvent evt)
    {
        float x_dif = (startMousePosX - evt.getX()) / screenWidth;
        float y_dif = (evt.getY() - startMousePosY) / screenHeight;

        float scale = (float)Math.sqrt(x_dif * x_dif + y_dif * y_dif);

        switch(getNavigationState())
        {
            case NavigationState.FLY_STATE:
                vectorTmp[0] = x_dif;
                vectorTmp[1] = 0;
                vectorTmp[2] = y_dif;
                break;

            case NavigationState.PAN_STATE:
                vectorTmp[0] = -x_dif;
                vectorTmp[1] = y_dif;
                vectorTmp[2] = 0;
                break;

            case NavigationState.TILT_STATE:
                vectorTmp[0] = x_dif;
                vectorTmp[1] = y_dif;
                vectorTmp[2] = 0;
                break;

            case NavigationState.WALK_STATE:
                vectorTmp[0] = x_dif;
                vectorTmp[1] = 0;
                vectorTmp[2] = y_dif;
                break;

            case NavigationState.EXAMINE_STATE:
                vectorTmp[0] = x_dif;
                vectorTmp[1] = 0;
                vectorTmp[2] = y_dif;
                break;

            case NavigationState.NO_STATE:
                break;
        }

        move(vectorTmp, scale);
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
        int button = evt.getModifiers();

        // Ignore this if the released button is not the one doing the
        // work.
        if(movementInProgress &&
           (button & MouseEvent.MOUSE_EVENT_MASK) != activeButton)
        {
            return;
        }

        stopMove();

        movementInProgress = false;
        setNavigationState(previousState);

        if(navigationListener != null)
            navigationListener.setNavigationState(previousState);
    }
}
