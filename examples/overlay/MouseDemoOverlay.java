/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

// Standard imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import javax.media.j3d.Canvas3D;

// Application specific imports
import org.j3d.geom.overlay.MouseOverlay;
import org.j3d.geom.overlay.InteractiveOverlayBase;

/**
 * An demo mouse overlay to print out when the mouse goes over the overlay.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class MouseDemoOverlay extends MouseOverlay
{
    /**
     * Creates a new overlay covering the given canvas bounds. It has two
     * buffers. Updates are managed automatically. This Overlay is not usable
     * until you attach it to the view platform transform.
     *
     * @param canvas3D Canvas being drawn onto
     * @param size The size of the overlay
     */
    public MouseDemoOverlay(Canvas3D canvas3D, Dimension size)
    {
        super(canvas3D, size);
    }

    /**
     * Initialise the overlay to build mouse input support
     */
    public void initialize()
    {
        // set the entire background to transparent
        setBackgroundColor(new Color(1.0f,0.0f,0.0f,0.0f));

        addMouseListener(this);
        addMouseMotionListener(this);

        super.initialize();
    }


    public void paint(Graphics2D g)
    {
        g.setColor(new Color(0, 0, 1));
        g.drawRect(0, 0, overlayBounds.width - 1, overlayBounds.height - 1);
    }

    //------------------------------------------------------------------------
    // Methods for MouseListener events
    //------------------------------------------------------------------------

    /**
     * Process a mouse press event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mousePressed(MouseEvent evt)
    {
        System.out.println("Mouse press");
    }

    /**
     * Process a mouse release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt)
    {
        System.out.println("Mouse release");
    }

    /**
     * Process a mouse click event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseClicked(MouseEvent evt)
    {
        System.out.println("Mouse click");
    }

    /**
     * Process a mouse enter event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseEntered(MouseEvent evt)
    {
        System.out.println("Mouse enter");
    }

    /**
     * Process a mouse exited event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseExited(MouseEvent evt)
    {
        System.out.println("Mouse exit");
    }

    //------------------------------------------------------------------------
    // Methods for MouseMotionListener events
    //------------------------------------------------------------------------

    /**
     * Process a mouse drag event
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDragged(MouseEvent evt)
    {
        System.out.println("Mouse drag");
    }

    /**
     * Process a mouse movement event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseMoved(MouseEvent evt)
    {
        System.out.println("Mouse move");
    }

    //------------------------------------------------------------------------
    // Local methods
    //------------------------------------------------------------------------
}