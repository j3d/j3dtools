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

package j3d.examples.nurbs;

// External imports
import java.awt.*;
import java.awt.event.*;

// Local imports
import org.j3d.geom.GeometryData;
import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.spline.BSplineGenerator;

/**
 * A 2D canvas for drawing various spline shapes onto.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class BSplineCanvas extends Canvas
    implements MouseListener,
               MouseMotionListener,
               DrawModeListener,
               FacetCountListener
{
    /** Amount of forgiveness in pixels around the control point */
    private static final int CONTROL_PIXELS = 3;

    /** The generator of the current spline setup */
    private BSplineGenerator currentGenerator;

    /** Holder of data between runs */
    private GeometryData data;

    /**
     * Location of each control point, as a flat array containing 3
     * coordinates, X,Y,Z. Since we are mapping 2D space to 3D, the z
     * coordinate will always be zero for this demo.
     */
    private float[] controlPoints;

    /**
     * Total number of control points. x 3 for the number of items in the
     * controlPoints array.
     */
    private int numControlPoints;

    /**
     * Rectangles in screen coordinates representing each of the control points.
     * For fast mouse testing on mouse press.
     */
    private Rectangle[] pointAreas;

    /** The currently active, selected point */
    private int selectedPoint;

    /** Flag to indicate if we've constructed the right point sizes yet */
    private boolean initialised;

    /** Local drawing mode */
    private int drawMode;

    /** The current max facet count for tracking when to clear geometry data */
    private int maxFacets;

    /**
     * Construct a new demo with no geometry currently showing, but the
     * default type is set to quads.
     */
    public BSplineCanvas()
    {
        // initial array with only 3 control points in it
        controlPoints = new float[9];
        numControlPoints = 3;

        pointAreas = new Rectangle[3];
        selectedPoint = -1;

        data = new GeometryData();
        data.geometryType = GeometryData.LINES;

        currentGenerator = new BSplineGenerator();

        initialised = false;

        addMouseListener(this);
        addMouseMotionListener(this);

        drawMode = DRAW;
        maxFacets = 16;
    }

    /**
     * Process a mouse press event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mousePressed(MouseEvent evt)
    {
        Point p = evt.getPoint();

        for(int i = 0; i < numControlPoints; i++)
        {
            if(pointAreas[i].contains(p))
            {
                selectedPoint = i;
                break;
            }
        }
    }

    /**
     * Process a mouse release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt)
    {
        selectedPoint = -1;
    }

    /**
     * Process a mouse click event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseClicked(MouseEvent evt)
    {
        Point p = evt.getPoint();

        switch(drawMode)
        {
            case DRAW:
                // do nothing
                break;

            case ADD:
                // current mouse coords, add there.
                if(numControlPoints >= pointAreas.length)
                {
                    int new_size = numControlPoints + 2;
                    Rectangle[] tmp = new Rectangle[new_size];
                    System.arraycopy(pointAreas, 0, tmp, 0, numControlPoints);
                    pointAreas = tmp;

                    pointAreas[new_size - 2] = new Rectangle();
                    pointAreas[new_size - 1] = new Rectangle();

                    float[] tmp2 = new float[new_size * 3];
                    System.arraycopy(controlPoints, 0,
                                     tmp2, 0,
                                     numControlPoints * 3);
                    controlPoints = tmp2;
                }

                Rectangle rect = pointAreas[numControlPoints];
                rect.x = p.x - CONTROL_PIXELS;
                rect.y = p.y - CONTROL_PIXELS;
                rect.width = CONTROL_PIXELS * 2;
                rect.height = CONTROL_PIXELS * 2;

                controlPoints[numControlPoints * 3] = p.x;
                controlPoints[numControlPoints * 3 + 1] = p.y;
                controlPoints[numControlPoints * 3 + 2] = 0;

                numControlPoints++;

                currentGenerator.setControlPoints(controlPoints,
                                                  numControlPoints);
                currentGenerator.generateSmoothKnots();
                currentGenerator.generate(data);

                repaint();
                break;


            case REMOVE:
                for(int i = 0; i < numControlPoints; i++)
                {
                    if(pointAreas[i].contains(p))
                    {
                        selectedPoint = i;

                        if(selectedPoint != numControlPoints - 1)
                        {
                            System.arraycopy(controlPoints,
                                             (i + 1) * 3,
                                             controlPoints,
                                             i * 3,
                                             (numControlPoints - 1 - i) * 3);

                            Rectangle tmp = pointAreas[i];
                            System.arraycopy(pointAreas,
                                             i + 1,
                                             pointAreas,
                                             i,
                                             numControlPoints - 1 - i);

                            pointAreas[numControlPoints - 1] = tmp;
                        }

                        numControlPoints--;

                        currentGenerator.setControlPoints(controlPoints,
                                                          numControlPoints);
                        currentGenerator.generateSmoothKnots();
                        currentGenerator.generate(data);

                        repaint();
                        break;
                    }
                }
        }

        selectedPoint = -1;
    }

    /**
     * Process a mouse enter event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseEntered(MouseEvent evt)
    {
    }

    /**
     * Process a mouse exit event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseExited(MouseEvent evt)
    {
    }

    /**
     * Process a mouse movement event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseMoved(MouseEvent evt)
    {
    }

    /**
     * Process a mouse drag event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDragged(MouseEvent evt)
    {
        if(selectedPoint == -1)
            return;

        Rectangle rect = pointAreas[selectedPoint];

        int x = evt.getX();
        int y = evt.getY();

        rect.x = x - CONTROL_PIXELS;
        rect.y = y - CONTROL_PIXELS;

        controlPoints[selectedPoint * 3] = x;
        controlPoints[selectedPoint * 3 + 1] = y;
        controlPoints[selectedPoint * 3 + 2] = 0;

        currentGenerator.setControlPoints(controlPoints, numControlPoints);
        currentGenerator.generate(data);

        repaint();
    }

    /**
     * Notification that the drawing mode is now the new value.
     *
     * @para mode One of the modes above
     */
    public void changeMode(int mode)
    {
        drawMode = mode;
    }

    /**
     * Notification that the user has selected a different number of facets
     * to work with.
     *
     * @para number The new number to use
     */
    public void changeFacetCount(int number)
    {
        if(maxFacets < number)
        {
            maxFacets = number;
            data.coordinates = null;
        }

        currentGenerator.setFacetCount(number);
        currentGenerator.generate(data);

        repaint();
    }

    /**
     * Override update() to prevent the complete clear. Calls paint() directly.
     */
    public void update(Graphics g)
    {
        paint(g);
    }

    /**
     * Do the repaint of the canvas now.
     */
    public void paint(Graphics g)
    {
        if(!initialised)
            initialise();

        int i;
        Dimension d = getSize();

        g.setColor(Color.white);
        g.fillRect(0, 0, d.width, d.height);
        g.setColor(Color.blue);

        // Draw all the control point items because the area has been
        // made visible again or something like that.
        for(i = 0; i < numControlPoints; i++)
        {
            Rectangle rect = pointAreas[i];
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
        }

        // Then the connecting lines.
        for(i = 0; i < numControlPoints - 1; i++)
        {
            int r1 = i * 3;
            int r2 = (i + 1) * 3;

            g.drawLine((int)controlPoints[r1],
                       (int)controlPoints[r1 + 1],
                       (int)controlPoints[r2],
                       (int)controlPoints[r2 + 1]);
        }

        g.setColor(Color.black);

        int cnt = 0;
        int x1, x2, y1, y2;

        for(i = 0; i < (data.vertexCount / 2); i++)
        {
            x1 = (int)data.coordinates[cnt];
            y1 = (int)data.coordinates[cnt + 1];

            cnt += 3;

            x2 = (int)data.coordinates[cnt];
            y2 = (int)data.coordinates[cnt + 1];

            cnt += 3;

            g.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * Do the initial setting up of the control points. We put 3 in at
     * 0, 50%, 50%,0, width,50%  with a couple of pixels margin for the
     * control squares.
     */
    private void initialise()
    {
        if(initialised)
            return;

        initialised = true;

        Dimension d = getSize();

        pointAreas[0] = new Rectangle();
        pointAreas[1] = new Rectangle();
        pointAreas[2] = new Rectangle();


        pointAreas[0].x = 0;
        pointAreas[0].y = d.height / 2 - CONTROL_PIXELS;
        pointAreas[0].width = CONTROL_PIXELS * 2;
        pointAreas[0].height = CONTROL_PIXELS * 2;

        controlPoints[0] = CONTROL_PIXELS;
        controlPoints[1] = d.height / 2;


        pointAreas[1].x = d.width / 2 - CONTROL_PIXELS;
        pointAreas[1].y = 0;
        pointAreas[1].width = CONTROL_PIXELS * 2;
        pointAreas[1].height = CONTROL_PIXELS * 2;

        controlPoints[3] = d.width / 2;
        controlPoints[4] = CONTROL_PIXELS;

        pointAreas[2].x = d.width - CONTROL_PIXELS * 2;
        pointAreas[2].y = d.height / 2;
        pointAreas[2].width = CONTROL_PIXELS * 2;
        pointAreas[2].height = CONTROL_PIXELS * 2;

        controlPoints[6] = d.width - CONTROL_PIXELS;
        controlPoints[7] = d.height / 2;

        currentGenerator.setControlPoints(controlPoints);
        currentGenerator.generateSmoothKnots();
        currentGenerator.generate(data);
    }
}
