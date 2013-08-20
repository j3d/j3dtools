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
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

import javax.vecmath.Color4b;

// Application Specific imports
// none

/**
 * A simple panel for getting colour values
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ColorPanel extends Panel
{
    private TextField redTf;
    private TextField greenTf;
    private TextField blueTf;
    private TextField alphaTf;

    /**
     * Create a new panel using the basic colour
     */
    public ColorPanel(Color4b col)
    {
        super(new GridLayout(1, 4));

        redTf = new TextField(Integer.toString((int)col.x & 0xFF), 3);
        greenTf = new TextField(Integer.toString((int)col.y & 0xFF), 3);
        blueTf = new TextField(Integer.toString((int)col.z & 0xFF), 3);
        alphaTf = new TextField(Integer.toString((int)col.w & 0xFF), 3);

        add(alphaTf);
        add(redTf);
        add(greenTf);
        add(blueTf);
    }

    public Color4b getColor()
    {
        byte a, r, g, b;

        a = (byte)Integer.parseInt(alphaTf.getText());
        r = (byte)Integer.parseInt(redTf.getText());
        g = (byte)Integer.parseInt(greenTf.getText());
        b = (byte)Integer.parseInt(blueTf.getText());

        return new Color4b(r, g, b, a);
    }
}
