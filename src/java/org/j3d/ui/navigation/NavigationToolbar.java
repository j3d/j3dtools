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
import  javax.swing.*;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Application specific imports
import org.j3d.util.ImageLoader;

/**
 * A toolbar for all navigation commands.
 * <p>
 *
 * The toolbar offers three buttons representing the navigation states. As this
 * panel also implements the state listener, these will change in response to
 * the mouse changing. Clicking these buttons will send the appropriate event
 * back to it's registered listener.
 *
 * @author <a href="http://www.geocities.com/seregi/index.html">Laszlo Seregi</a><br>
 *    Updated for j3d.org by Justin Couch
 * @version $Revision: 1.3 $
 */
public class NavigationToolbar extends JPanel
    implements ActionListener, NavigationStateListener
{
    // Constants for images

    /** The name of the file for the pan cursor image */
    private static final String PAN_BUTTON = "images/navigation/ButtonPan.gif";

    /** The name of the file for the tilt cursor image */
    private static final String TILT_BUTTON = "images/navigation/ButtonTilt.gif";

    /** The name of the file for the walk cursor image */
    private static final String WALK_BUTTON = "images/navigation/ButtonWalk.gif";

    // Local variables

    /** The current navigation state either set from us or externally */
    private int navigationState = WALK_STATE;

    /** An observer for navigation state change information */
    private NavigationStateListener navigationListener;

    /** Button group holding the navigation state buttons */
    private ButtonGroup navStateGroup;

    /** Button representing the walk navigation state */
    private JToggleButton walkButton;

    /** Button representing the tilt navigation state */
    private JToggleButton tiltButton;

    /** Button representing the pan navigation state */
    private JToggleButton panButton;

    /**
     * Create a new horizontal navigation toolbar with an empty list of
     * viewpoints.
     */
    public NavigationToolbar()
    {
        this(true);
    }

    /**
     * Create a new navigation toolbar with an empty list of viewpoints but
     * controllable direction for the buttons.
     *
     * @param horizontal True to lay out the buttons horizontally
     */
    public NavigationToolbar(boolean horizontal)
    {
        if(horizontal)
            setLayout(new GridLayout(1, 3));
        else
            setLayout(new GridLayout(3, 1));

        navStateGroup = new ButtonGroup();

        Icon icon = ImageLoader.loadIcon(WALK_BUTTON);
        walkButton = new JToggleButton(icon);
        walkButton.setMargin(new Insets(0,0,0,0));
        walkButton.setToolTipText("Walk");
        walkButton.addActionListener(this);
        navStateGroup.add(walkButton);
        add(walkButton);

        icon = ImageLoader.loadIcon(TILT_BUTTON);
        tiltButton = new JToggleButton(icon);
        tiltButton.setMargin(new Insets(0,0,0,0));
        tiltButton.setToolTipText("Tilt");
        tiltButton.addActionListener(this);
        navStateGroup.add(tiltButton);
        add(tiltButton);

        icon = ImageLoader.loadIcon(PAN_BUTTON);
        panButton = new JToggleButton(icon);
        panButton.setMargin(new Insets(0,0,0,0));
        panButton.setToolTipText("Pan");
        panButton.addActionListener(this);
        navStateGroup.add(panButton);
        add(panButton);
    }

    //----------------------------------------------------------
    // Local public methods
    //----------------------------------------------------------

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
    // Methods required by the ActionListener
    //----------------------------------------------------------

    /**
     * Process an action event on one of the buttons.
     *
     * @param evt The event that caused this method to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        Object src = evt.getSource();

        if(src == walkButton)
        {
            navigationState = WALK_STATE;
            if(navigationListener != null)
                navigationListener.setNavigationState(navigationState);
        }
        else if(src == tiltButton)
        {
            navigationState = TILT_STATE;
            if(navigationListener != null)
                navigationListener.setNavigationState(navigationState);
        }
        else if(src == panButton)
        {
            navigationState = PAN_STATE;
            if(navigationListener != null)
                navigationListener.setNavigationState(navigationState);
        }
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

        switch(navigationState)
        {
            case NavigationStateListener.WALK_STATE:
                walkButton.setSelected(true);
                break;

            case NavigationStateListener.PAN_STATE:
                panButton.setSelected(true);
                break;

            case NavigationStateListener.TILT_STATE:
                tiltButton.setSelected(true);
                break;
        }
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
