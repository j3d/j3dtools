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

// Standard imports
import java.awt.*;
import java.awt.event.*;

// Application Specific imports
// None

/**
 * Simple panel wth 3 radio buttons to allow the user to select the mode that
 * the mouse actions are operating in.
 * <p>
 *
 * There are three modes - add a point, remove a point and draw.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ModePanel extends Panel
    implements ItemListener
{
    /** Listener for the draw mode to use */
    private DrawModeListener listener;

    /** Radio button for draw mode */
    private Checkbox drawButton;

    /** Radio button for add mode */
    private Checkbox addButton;

    /** Radio button for remove mode */
    private Checkbox removeButton;

    /**
     * Create a new mode panel that sends the mode output to the given
     * listener.
     *
     * @param l the listener to use
     */
    public ModePanel(DrawModeListener l)
    {
        super(new GridLayout(3, 1));

        listener = l;

        CheckboxGroup cbg = new CheckboxGroup();

        drawButton = new Checkbox("Move points", cbg, true);
        drawButton.addItemListener(this);

        addButton = new Checkbox("Add points", cbg, false);
        addButton.addItemListener(this);

        removeButton = new Checkbox("Delete points", cbg, false);
        removeButton.addItemListener(this);

        add(drawButton);
        add(addButton);
        add(removeButton);
    }

    /**
     * Callback for when the item state of a button changes.
     *
     * @param evt The event that caused this listener to be called
     */
    public void itemStateChanged(ItemEvent evt)
    {
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            Object src = evt.getSource();

            if(src == drawButton)
                listener.changeMode(DrawModeListener.DRAW);
            else if(src == addButton)
                listener.changeMode(DrawModeListener.ADD);
            else
                listener.changeMode(DrawModeListener.REMOVE);
        }
    }
}
