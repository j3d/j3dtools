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

// External imports
import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.SystemColor;

// Local imports
// None

/**
 * A 2D canvas for drawing various spline shapes onto.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class BSplinePanel extends Panel
{
    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public BSplinePanel()
    {
        setLayout(new BorderLayout());
        setBackground(SystemColor.menu);

        BSplineCanvas canvas = new BSplineCanvas();
        ModePanel mode = new ModePanel(canvas);
        FacetCountPanel facet = new FacetCountPanel(canvas);

        add(canvas, BorderLayout.CENTER);

        Panel p1 = new Panel(new BorderLayout());
        p1.add(mode, BorderLayout.SOUTH);
        p1.add(facet, BorderLayout.NORTH);

        add(p1, BorderLayout.EAST);
    }
}
