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
import java.awt.*;

import java.io.IOException;

// Local imports
import org.j3d.geom.GeometryData;
import org.j3d.geom.GeometryGenerator;


/**
 * Demonstration of the 2D NURBS implementation code.
 * <p>
 * The objects are rendered on screen to show that all the values are correctly
 * generated. There is no capability to navigate around them or to change any
 * of the rendering attributes like the face set.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class Nurbs2DDemo extends DemoFrame
{
    /** The generator of the current spline setup */
    private GeometryGenerator currentGeometry;

    /** Canvas doing BSpline curves */
    private BSplinePanel splinePanel;

    /** Canvas for doing bezier curves */
    private BezierPanel bezierPanel;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public Nurbs2DDemo()
    {
        super("2D Nurbs Demo test window");

        setLayout(new BorderLayout());

        splinePanel = new BSplinePanel();
        bezierPanel = new BezierPanel();

//        add(splinePanel, BorderLayout.CENTER);
        add(bezierPanel, BorderLayout.CENTER);
    }

    public static void main(String[] argv)
    {
        Nurbs2DDemo demo = new Nurbs2DDemo();
        demo.setVisible(true);
    }
}
