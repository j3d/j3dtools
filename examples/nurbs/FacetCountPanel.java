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
 * Simple panel allowing the user to enter text to change the number of
 * rendered facets for the curve.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class FacetCountPanel extends Panel
    implements ActionListener
{
    /** Listener for the change in values */
    private FacetCountListener listener;

    /** Text field where the count was entered */
    private TextField countTf;

    /**
     * Create a new mode panel that sends the mode output to the given
     * listener.
     *
     * @param l the listener to use
     */
    public FacetCountPanel(FacetCountListener l)
    {
        super(new GridLayout(2, 1));

        listener = l;

        countTf = new TextField("16", 3);

        Label l1 = new Label("Facets");

        Panel p1 = new Panel(new BorderLayout());
        p1.add(l1, BorderLayout.WEST);
        p1.add(countTf, BorderLayout.EAST);

        Button button = new Button("Update");
        button.addActionListener(this);

        add(p1);
        add(button);
    }

    /**
     * Callback for when the item state of a button changes.
     *
     * @param evt The event that caused this listener to be called
     */
    public void actionPerformed(ActionEvent evt)
    {
        String int_str = countTf.getText();

        try
        {
            int val = Integer.parseInt(int_str);
            if(val < 0)
                System.out.println("Can't have negative numbers");
            else
                listener.changeFacetCount(val);
        }
        catch(NumberFormatException nfe)
        {
            System.out.println("Integer numbers only please");
        }
    }
}
