/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.overlay;

// Standard imports
import javax.media.j3d.*;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.util.ArrayList;

import javax.vecmath.Vector3d;

// Application specific imports
// none

/**
 * The overlay manager keeps track of all the overlay's on the screen and
 * makes sure they are updated with the view transform once a frame.
 * <p>
 * The Overlay manager should be placed into the scenegraph where the view
 * transform is. It also assumes that none of the child overlays set have
 * the Canvas3D reference set and so manages that all for them.
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class OverlayManager extends BranchGroup
    implements ComponentListener
{
    /** I do not undersand what this is for */
    private static double CONSOLE_Z = 20f;

    /** The update manager for keeping us in sync */
    private UpdateManager updateManager;

    /** Behaviour for processing interactive events */
    private AWTEventBehavior awtBehavior;

    /** Transformation to make the raster become screen coords as well */
    private TransformGroup consoleTG;

    /** Group containing all the child overlays */
    private BranchGroup windows;

    /** Transform of image plate */
    private Transform3D planeOffset;

    /** Total transform used to correct the overlays each frame */
    private Transform3D worldTransform;

    /** the dimensions of the Canvas3d */
    private Dimension canvasDim;

    /** used to check for dimension changes */
    private Dimension checkDim;

    /** Canvas we are displayed on */
    private Canvas3D canvas3D;

    /** List of all the overlays that are children here */
    private ArrayList overlays;

    /**
     * Create a new manager that works on the given canvas. It does not have
     * an update manager provided, so it will provide its own and add it as
     * part of the scene graph below this BranchGroup.
     *
     * @param canvas The canvas the overlay is drawn on
     */
    public OverlayManager(Canvas3D canvas)
    {
        this(canvas, null);
    }

    /**
     * Constructs an overlay window with an update manager. It has two buffers.
     * This window will not be visible unless it is added to the scene under
     * the view platform transform. If the bounds are null, then resize the
     * overlay to fit the canvas and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param bounds The part of the canvas covered by the overlay
     * @param updateManager Responsible for allowing the Overlay to update
     *   between renders. If this is null a default manager is created
     */
    public OverlayManager(Canvas3D canvas, UpdateManager updateManager)
    {
        canvas3D = canvas;

        canvas.addComponentListener(this);

        BoundingSphere sched_bounds = new BoundingSphere();
        sched_bounds.setRadius(Float.POSITIVE_INFINITY);

        if(updateManager == null)
        {
            UpdateControlBehavior updateBehavior = new UpdateControlBehavior();
            updateBehavior.setSchedulingBounds(sched_bounds);
            addChild(updateBehavior);
            this.updateManager = updateBehavior;
        }
        else
            this.updateManager = updateManager;

        // build the console transform group
        consoleTG = new TransformGroup();
        consoleTG.setCapability(consoleTG.ALLOW_TRANSFORM_WRITE);
//      consoleTG.setTransform(new Transform3D());

        addChild(consoleTG);

        awtBehavior = new AWTEventBehavior();
        awtBehavior.setEnable(false);
        awtBehavior.setSchedulingBounds(sched_bounds);
        addChild(awtBehavior);

        // define the dimensions and transforms used by the overlay manager
        overlays = new ArrayList();
        canvasDim = new Dimension();
        checkDim = new Dimension();
        planeOffset = new Transform3D();
        worldTransform = new Transform3D();

//        calcImagePlate();

        // define the ordered group that will have all the OverlayWindow's.  They
        // are placed in an ordered group so that we can control window stacking.

        windows = new BranchGroup();
        windows.setCapability(OrderedGroup.ALLOW_CHILDREN_EXTEND);
        windows.setCapability(OrderedGroup.ALLOW_CHILDREN_WRITE);
        consoleTG.addChild(windows);
    }

    //------------------------------------------------------------------------
    // Methods from the ComponentListener interface
    //------------------------------------------------------------------------

    /**
     * Notification that the component has been resized.
     *
     * @param e The event that caused this method to be called
     */
    public void componentResized(ComponentEvent e)
    {
        Dimension size = canvas3D.getSize();
        View v = canvas3D.getView();
        double fov = (v != null) ? v.getFieldOfView() : 0.785398;

        int num_overlays = overlays.size();

        for(int i = 0; i < num_overlays; i++)
        {
            Overlay o = (Overlay)overlays.get(i);
            o.setComponentDetails(size, fov);
        }
    }

    /**
     * Notification that the component has been moved.
     *
     * @param e The event that caused this method to be called
     */
    public void componentMoved(ComponentEvent e)
    {
    }

    /**
     * Notification that the component has been shown. This is the component
     * being shown, not the window that it is contained in.
     *
     * @param e The event that caused this method to be called
     */
    public void componentShown(ComponentEvent e)
    {
    }

    /**
     * Notification that the component has been hidden.
     *
     * @param e The event that caused this method to be called
     */
    public void componentHidden(ComponentEvent e)
    {
    }

    //------------------------------------------------------------------------
    // Local utility methods
    //------------------------------------------------------------------------

    /**
     * Initialise the manager, which in turn initializes all the managed
     * overlays.
     */
    public void initialize()
    {
        int num_overlays = overlays.size();

        for(int i = 0; i < num_overlays; i++)
        {
            Overlay o = (Overlay)overlays.get(i);
            o.initialize();
        }
    }

    /**
     * Returns the UpdateManager responsible for seeing that updates to the
     * Overlay only take place between frames.
     *
     * @param The update manage instance for this overlay
     */
    public UpdateManager getUpdateManager()
    {
        return updateManager;
    }

    /**
     * Set the UpdateManager to the new value. If the reference is null, it
     * will clear the current manager.
     *
     * @param updateManager A reference to the new manage instance to use
     */
    public void setUpdateManager(UpdateManager manager)
    {
        if(updateManager instanceof UpdateControlBehavior)
        {
            ((UpdateControlBehavior)updateManager).setEnable(false);
        }

        updateManager = manager;
    }

    /**
     * Sets the position of the specified overlay
     */
    public void setPosition(Overlay overlay, int x, int y)
    {
        overlay.setOffset(x, y);
    }

    /**
     * Called once a frame to update the different overlays
     */
    public void newFrame(Transform3D viewTransform)
    {

        if(overlays.size() == 0)
            return;

        checkScreenSize();
        worldTransform.set(viewTransform);
        worldTransform.mul(planeOffset);
        consoleTG.setTransform(worldTransform);
    }

    /**
     * This adds an overlay into the overlay manager system.
     *
     * @param overlay The overlay to add
     */
    public void addOverlay(Overlay overlay)
    {
        Dimension size = canvas3D.getSize();
        View v = canvas3D.getView();
        double fov = (v != null) ? v.getFieldOfView() : 0.785398;

        overlay.setUpdateManager(updateManager);
        overlay.setComponentDetails(size, fov);

        if(overlay instanceof InteractiveOverlay)
        {
            if(!awtBehavior.getEnable())
                awtBehavior.setEnable(true);

            ((InteractiveOverlay)overlay).setInputRequester(awtBehavior);
        }

        overlays.add(overlay);
        windows.addChild(overlay.getRoot());
    }

    /**
     * This removes the overlay from the overlay system.  The underlying
     * resources will be released.
     *
     * @param overlay The overlay to remove
     */
    public void removeOverlay(Overlay overlay)
    {
        if(overlays.remove(overlay))
        {
            if(overlay instanceof InteractiveOverlay)
                ((InteractiveOverlay)overlay).setInputRequester(null);

            BranchGroup bg = overlay.getRoot();
            bg.detach();

            if(overlays.size() == 0)
                awtBehavior.setEnable(false);
        }

    }

    /**
     * calculates the image plate transformation considering the field of view
     * and the size of the screen.
     */
    private void calcImagePlate()
    {
        // get the field of view and then calculate the width in meters of the
        // screen

        double fov = canvas3D.getView().getFieldOfView();
        double c_width = 2 * CONSOLE_Z * Math.tan(fov * 0.5);

        Dimension canvas_size = canvas3D.getSize();
        canvasDim.setSize(canvas_size);

        if(canvasDim.width != 0)
        {
            // calculate the ratio between the canvas in pixels and the screen
            // in meters and use that to find the height of the screen in
            // meters
            double scale = c_width / canvas_size.getWidth();
            double c_height = canvas_size.getHeight()* scale;

            Vector3d loc =
                new Vector3d(-c_width / 2 , -c_height / 2, -CONSOLE_Z);

            planeOffset.setTranslation(loc);
            planeOffset.setScale(scale);
            consoleTG.setTransform(planeOffset);
        }

        checkDim.setSize(canvas3D.getSize());
    }

    /**
     * Synchronize with the view platform
     */
    private void checkScreenSize()
    {
        canvas3D.getSize(checkDim);
//        if(!checkDim.equals(canvasDim))
//            calcImagePlate();
    }
}