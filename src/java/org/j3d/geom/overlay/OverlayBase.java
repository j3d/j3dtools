/*****************************************************************************
 *                 Teseract Software, LLP Copyright(c)2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.overlay;

// Standard imports
import java.awt.*;
import javax.media.j3d.*;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.List;

import javax.vecmath.Color4f;
import javax.vecmath.Vector3d;

// Application specific imports
// none

/**
 * An implementation of the overlay and screen component interfaces to provide
 * a ready-made overlay system.
 * <P>
 *
 * The implementation uses textured objects that are mapped to screen space
 * coordinates. This can be both good and bad. The overlay can operate in
 * one of two modes - fixed size, dynamic size according to the canvas.
 * <P>
 * <B>Fixed Size Overlays</B>
 * <P>
 * The size should be set to a value that is a power of two for
 * the best performance. The code divides the supplied area into smaller
 * sections, with a maximum size of 256 pixels in either direction. Left over
 * pieces are then subdivided into lots that are power of two. The minimum size
 * of one of these pieces is 16 pixels. If you have an odd size, sich as 55
 * pixels, then you get weird artifacts appearing on screen.
 * <p>
 * A fixed size overlay may operate without being given a canvas to work with.
 * This would be used when you are using an overlay manager to work with the
 * overlay instance.
 *
 * <P>
 * <B>Resizable Overlays</B>
 * <P>
 * A resizable overlay is created when the bounds are set to null in the
 * constructor. In this case the overlay then listens for resizing information
 * from the component and resizes the internal subsections to accomodate this.
 * For this system, in order to remain visually accurate, we subdivide down to
 * shapes that are 1 pixel across. Obviously this impacts performance quite
 * dramatically to have so many tiny objects.
 * <P>
 * The class implements the AWT component listener interface so that it can
 * automatically resize the overlay's base image in response to the canvas
 * changing size. This will ensure that everything is correctly located on the
 * screen after the resize.
 * <P>
 *
 * <hr>
 *
 * All overlays start at the origin of 0,0 and have some form of width
 * associated with them. This code does not look after any layout requirements.
 * It is expected the application will take care of the layout management of
 * the overlays.
 *
 * @author David Yazel, Justin Couch
 * @version $Revision: 1.14 $
 */
public abstract class OverlayBase
    implements Overlay, ScreenComponent, ComponentListener
{
    /** I do not undersand what this is for */
    private final static double CONSOLE_Z = 2.1f;

    // checks for altered elements

    /** Mark the visible flag as dirty */
    protected final static int DIRTY_VISIBLE = 0;

    /** Mark the position as dirty and needing correction */
    protected final static int DIRTY_POSITION = 1;

    /** Mark the active buffer as dirty and needing swapping */
    protected final static int DIRTY_ACTIVE_BUFFER = 2;

    /** Mark the size as dirty and needing correction */
    protected final static int DIRTY_SIZE = 3;


    /** The current background mode. Defaults to copy */
    protected int backgroundMode = BACKGROUND_COPY;

    /** The image that sits in the background of the overlay */
    private BufferedImage backgroundImage;

    /** Flag indicating the image(s) have an alpha component to them */
    private boolean hasAlpha;

    /** Flag holding current visibility state */
    private boolean visible;

    /** Flag to say if the drawing should be anti-aliased */
    private boolean antialiased;

    /** The number of fore/back buffers in use */
    private int numBuffers;

    /** The smallest number of pixels per sub-overlay size */
    private final int minDivSize;

    /** Canvas bounds occupied by this overlay. */
    protected Rectangle overlayBounds;

    /** The bounds of the Canvas3D this overlay is rendered in */
    protected Dimension componentSize;

    /** The field of view for the canvas */
    protected double fieldOfView;

    /** The update manager for keeping us in sync */
    private UpdateManager updateManager;

    /** Canvas we are displayed on */
    private Canvas3D canvas3D;

    /** Drawing area that we scribble our stuff on */
    protected BufferedImage canvas;

    /** Background colour of the overlay */
    protected Color backgroundColor;

    /**
     * The list of sub-overlay areas. Starts pre-created with a zero length
     * array. Blocking is performed using this so it can't be null.
     */
    protected SubOverlay[] subOverlay;

    /** The currently active buffer index */
    protected int activeBuffer = SubOverlay.NEXT_BUFFER;

    /** Root branchgroup for the entire overlay system */
    protected BranchGroup consoleBG;

    /**
     * Contains the texture objects from the suboverlays. Each time the window
     * size changes, this instance is thrown away and replaced with a new one.
     * Ensures that we can change over the raster objects. Always set as child
     * 0 of the consoleBG.
     */
    protected BranchGroup overlayTexGrp;

    /** Transformation to make the raster become screen coords as well */
    protected TransformGroup consoleTG;

    // shared resources for the sub-overlays
    private RenderingAttributes renderAttributes;
    private PolygonAttributes polygonAttributes;
    private TextureAttributes textureAttributes;
    private TransparencyAttributes transparencyAttributes;
    private Material material;

    /** List of the dirty flag settings */
    private boolean[] dirtyCheck = new boolean[DIRTY_SIZE + 1];

    /**
     * Flag indicating whether this is a fixed size or resizable overlay. Fixed
     * size is when the user gives us bounds. Resizable when they don't and we
     * track the canvas.
     */
    private boolean fixedSize;

    /** Used to avoid calls to repaint backing up */
    private boolean painting = false;

    /** Flag to say whether initialisation has been completed yet */
    private boolean initComplete;

    /**
     * Creates a new overlay covering the given canvas bounds. It has two
     * buffers. Updates are managed automatically. This Overlay is not usable
     * until you attach it to the view platform transform. If the bounds are
     * null, then resize the overlay to fit the canvas and then track the size
     * of the canvas.
     *
     * @param canvas Canvas being drawn onto
     * @param size The size of the overlay in pixels
     * @throws IllegalArgumentException Both the canvas and bounds are null
     */
    protected OverlayBase(Canvas3D canvas, Dimension size)
    {
        this(canvas, size, true, false, null);
    }

    /**
     * Constructs an overlay window with an update manager. It has two buffers.
     * This window will not be visible unless it is added to the scene under
     * the view platform transform. If the bounds are null, then resize the
     * overlay to fit the canvas and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @param updateManager Responsible for allowing the Overlay to update
     *   between renders. If this is null a default manager is created
     * @throws IllegalArgumentException Both the canvas and bounds are null
     */
    protected OverlayBase(Canvas3D canvas, Dimension size, UpdateManager manager)
    {
        this(canvas, size, true, false, manager);
    }

    /**
     * Constructs an overlay window that can have alpha capabilities. This
     * window will not be visible unless it is added to the scene under the
     * view platform transform. If the bounds are null, then resize the
     * overlay to fit the canvas and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @param clipAlpha Should the polygon clip where alpha is zero
     * @param blendAlpha Should we blend to background where alpha is < 1
     * @throws IllegalArgumentException Both the canvas and bounds are null
     */
    protected OverlayBase(Canvas3D canvas,
                          Dimension size,
                          boolean clipAlpha,
                          boolean blendAlpha)
    {
        this(canvas, size, clipAlpha, blendAlpha, null);
    }

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     * If the bounds are null, then resize the overlay to fit the canvas
     * and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @param clipAlpha Should the polygon clip where alpha is zero
     * @param blendAlpha Should we blend to background where alpha is < 1
     * @param updateManager Responsible for allowing the Overlay to update
     *   between renders. If this is null a default manager is created
     * @throws IllegalArgumentException Both the canvas and bounds are null
     */
    protected OverlayBase(Canvas3D canvas,
                          Dimension size,
                          boolean clipAlpha,
                          boolean blendAlpha,
                          UpdateManager updateManager)
    {
        this(canvas, size, clipAlpha, blendAlpha, updateManager, 2);
    }

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     * If the bounds are null, then resize the overlay to fit the canvas
     * and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @param clipAlpha Should the polygon clip where alpha is zero
     * @param blendAlpha Should we blend to background where alpha is < 1
     * @param updateManager Responsible for allowing the Overlay to update
     *   between renders. If this is null a default manager is created
     * @param numBuffers The number of buffers to generate, the default is two
     * @throws IllegalArgumentException Both the canvas and bounds are null
     */
    protected OverlayBase(Canvas3D canvas,
                          Dimension size,
                          boolean clipAlpha,
                          boolean blendAlpha,
                          UpdateManager updateManager,
                          int numBuffers)
    {
        if(canvas == null && size == null)
            throw new IllegalArgumentException("Both canvas and size null");

        this.numBuffers = numBuffers;

        initComplete = false;
        canvas3D = canvas;

        if(size == null)
        {
            overlayBounds = canvas.getBounds();
            componentSize = canvas.getSize();
            View v = canvas.getView();

            if(v == null)
                fieldOfView = 0.785398;  // PI / 4 == 45 deg
            else
                fieldOfView = v.getFieldOfView();

            fixedSize = false;
            minDivSize = 1;
        }
        else
        {
            overlayBounds = new Rectangle(0, 0, size.width, size.height);
            fixedSize = true;
            minDivSize = 8;

            if(canvas3D != null)
            {
                componentSize = canvas.getSize();
                View v = canvas.getView();

                if(v == null)
                    fieldOfView = 0.785398;  // PI / 4 == 45 deg
                else
                    fieldOfView = v.getFieldOfView();
            }
            else
            {
                componentSize = new Dimension(size);
                fieldOfView = 0.785398;  // PI / 4 == 45 deg
            }

        }

        visible = true;
        antialiased = true;
        hasAlpha = clipAlpha || blendAlpha;

        if(overlayBounds.width != 0 && overlayBounds.height != 0)
        {
            this.canvas = OverlayUtilities.createBufferedImage(size, hasAlpha);
        }

        if(!fixedSize || (canvas3D != null))
            canvas3D.addComponentListener(this);

        // define the branch group where we are putting all the sub-overlays

        consoleBG = new BranchGroup();
        consoleBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        consoleBG.setCapability(BranchGroup.ALLOW_DETACH);

        overlayTexGrp = new BranchGroup();
        overlayTexGrp.setCapability(BranchGroup.ALLOW_DETACH);

        consoleTG = new TransformGroup();
        consoleTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        overlayTexGrp.addChild(consoleTG);
        consoleBG.addChild(overlayTexGrp);

        this.updateManager = updateManager;

        // define the rendering attributes used by all sub-overlays
        renderAttributes = new RenderingAttributes();
        if(clipAlpha)
        {
            renderAttributes.setAlphaTestFunction(RenderingAttributes.NOT_EQUAL);
            renderAttributes.setAlphaTestValue(0);
        }

        renderAttributes.setDepthBufferEnable(false);
        renderAttributes.setDepthBufferWriteEnable(false);
        renderAttributes.setIgnoreVertexColors(true);
        renderAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_READ);
        renderAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);

        // define the polygon attributes for all the sub-overlays
        polygonAttributes = new PolygonAttributes();
        polygonAttributes.setBackFaceNormalFlip(false);
        polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
        polygonAttributes.setPolygonMode(PolygonAttributes.POLYGON_FILL);

        // define the texture attributes for all the sub-overlays
        textureAttributes = new TextureAttributes();
        textureAttributes.setTextureMode(TextureAttributes.REPLACE);
        textureAttributes.setPerspectiveCorrectionMode(TextureAttributes.FASTEST);

        material = new Material();
        material.setLightingEnable(false);

        // if this needs to support transparancy set up the blend
        if(hasAlpha)
        {
            transparencyAttributes =
                new TransparencyAttributes(TransparencyAttributes.BLENDED,
                                           1.0f);
            textureAttributes.setTextureBlendColor(new Color4f(0, 0, 0, 1));
        }

        List overlays = OverlayUtilities.subdivide(size, minDivSize, 256);

        subOverlay = new SubOverlay[overlays.size()];
        int n = overlays.size();

        for(int i = 0; i < n; i++)
        {
            Rectangle current_space = (Rectangle)overlays.get(i);
            subOverlay[i] = new SubOverlay(current_space,
                                           numBuffers,
                                           hasAlpha,
                                           polygonAttributes,
                                           renderAttributes,
                                           textureAttributes,
                                           transparencyAttributes,
                                           material);

            consoleTG.addChild(subOverlay[i].getShape());
        }

        // Dirty everything and an initial WakeupOnActivation will sync everything
        dirtyCheck[DIRTY_VISIBLE] = true;
        dirtyCheck[DIRTY_POSITION] = true;
        dirtyCheck[DIRTY_ACTIVE_BUFFER] = true;
    }

    //------------------------------------------------------------------------
    // Methods from the Overlay interface
    //------------------------------------------------------------------------

    /**
     * Empty method that can be used to provide post construction
     * initialisation.
     */
    public void initialize()
    {
        initComplete = true;
    }

    /**
     * Update the canvas component details of size and field of view settings.
     * This is mainly called when the overlay is part of a larger management
     * system and it needs to inform the overlay of new screen information.
     *
     * @param size The new dimensions of the component
     * @param fov The new field of view for the current view
     */
    public void setComponentDetails(Dimension size, double fov)
    {
        componentSize = size;
        fieldOfView = fov;

        if(fixedSize)
            dirty(DIRTY_POSITION);
        else
            dirty(DIRTY_SIZE);
    }

    /**
     * Sets the location of the top-left corner of the overlay. It will move
     * the overlay to that position on the next update cycle.
     *
     * @param x The x coordinate of the location
     * @param y The y coordinate of the location
     */
    public void setLocation(int x, int y)
    {
        if(overlayBounds.x != x || overlayBounds.y != y)
        {
            overlayBounds.x = x;
            overlayBounds.y = y;
            dirty(DIRTY_POSITION);
        }
    }

    /**
     * Change the size of the texture to the new size. The new size will be
     * in pixels and must be valid >= 0.
     *
     * @param w The new width of the overlay
     * @param h The new height of the overlay
     */
    public void setSize(int w, int h)
    {
        if(overlayBounds.width != w || overlayBounds.height != h)
        {
            overlayBounds.width = w;
            overlayBounds.height = h;
            dirty(DIRTY_SIZE);
        }
    }

    /**
     * Return the root of the overlay and its sub-overlays so it can be
     * added to the scene graph. This should be added to the view transform
     * group of the parent application.
     *
     * @return The J3D branch group that holds the overlay
     */
    public BranchGroup getRoot()
    {
        return consoleBG;
    }

    /**
     * Sets whether drawing onto this Overlay is anialiased.
     *
     * @param antialiased The new setting for anti-aliasing.
     */
    public void setAntialiased(boolean antialiased)
    {
        if(this.antialiased != antialiased)
        {
            this.antialiased = antialiased;
            repaint();
        }
    }

    /**
     * Check to see whether this overlay is currently antialiased.
     *
     * @return true if this overlay is antialiased
     */
    public boolean isAntialiased()
    {
        return antialiased;
    }

    /**
     * Changes the visibility of the overlay.
     *
     * @param visible The new visibility state
     */
    public void setVisible(boolean visible)
    {
        if(this.visible != visible)
        {
            this.visible = visible;
            dirty(DIRTY_VISIBLE);
        }
    }

    /**
     * Returns the visiblity of the Overlay.
     *
     * @return true if the overlay is currently visible
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * Sets the background mode.  BACKGROUND_COPY will copy the raster data from the
     * background into the canvas before paint()is called. BACKGROUND_NONE will cause
     * the background to be disabled and not used.
     *
     * @param mode The new mode to use for the background
     */
    public void setBackgroundMode(int mode)
    {
        if(backgroundMode != mode)
        {
            backgroundMode = mode;
            repaint();
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
     * @param mgr A reference to the new manage instance to use
     */
    public void setUpdateManager(UpdateManager mgr)
    {
        updateManager = mgr;

        if(updateManager == null)
            return;

        // if we have anything dirty, request an update immediately
        for(int i = 0; i < dirtyCheck.length; i++)
        {
            if(dirtyCheck[i])
            {
                updateManager.updateRequested(this);
                break;
            }
        }
    }

    //------------------------------------------------------------------------
    // Methods from the UpdatableEntity interface
    //------------------------------------------------------------------------

    /**
     * Notification from the update manager that something has changed and we
     * should fix up the appropriate bits.
     */
    public void update()
    {
        // Always size first as that may reset the position and we don't need
        // to calculate the position twice.
        if(dirtyCheck[DIRTY_SIZE])
            syncSize();

        if(dirtyCheck[DIRTY_POSITION])
            syncPosition();

        if(dirtyCheck[DIRTY_VISIBLE])
            syncVisible();

        if(dirtyCheck[DIRTY_ACTIVE_BUFFER])
            syncActiveBuffer();
    }

    //------------------------------------------------------------------------
    // Methods from the ScreenComponent interface
    //------------------------------------------------------------------------

    /**
     * Get the bounds of the visible object in screen space coordinates.
     *
     * @return A rectangle representing the bounds in screen coordinates
     */
    public Rectangle getBounds()
    {
        return overlayBounds;
    }

    /**
     * Check to see if the point passed in is contained within the bounds of
     * the overlay.
     *
     * @param p The point to check if it is contained
     * @return true if the point is contained within the bounds of this overlay
     */
    public boolean contains(Point p)
    {
        return overlayBounds.contains(p);
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
        if(canvas3D != null)
        {
            componentSize = canvas3D.getSize();
            View v = canvas3D.getView();

            if(v != null)
                fieldOfView = v.getFieldOfView();
        }

        if(fixedSize)
            dirty(DIRTY_POSITION);
        else
            dirty(DIRTY_SIZE);
    }

    /**
     * Notification that the component has been moved.
     *
     * @param e The event that caused this method to be called
     */
    public void componentMoved(ComponentEvent e)
    {
        dirty(DIRTY_POSITION);
    }

    /**
     * Notification that the component has been shown. This is the component
     * being shown, not the window that it is contained in.
     *
     * @param e The event that caused this method to be called
     */
    public void componentShown(ComponentEvent e)
    {
        repaint();
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
     * Sets the background to a solid color. If a background image already exists then
     * it will be overwritten with this solid color.  It is completely appropriate to
     * have an alpha component in the color if this is a alpha capable overlay.
     * In general you should only use background images if this is an overlay that is
     * called frequently, since you could always paint it inside the paint()method.
     * BackgroundMode must be in BACKGROUND_COPY for the background to be shown.
     *
     * @param color The new color to use
     */
    public void setBackgroundColor(Color color)
    {
        backgroundColor = color;

        if(overlayBounds.width == 0 || overlayBounds.height == 0)
            return;

        updateBackgroundColor();
    }

    /**
     * Returns the background for the overlay. Updates to this image will not
     * be shown in the overlay until repaint()is called.
     * BackgroundMode must be in BACKGROUND_COPY for the background to be shown.
     *
     * @return The image used as the background
     */
    public BufferedImage getBackgroundImage()
    {
        if(backgroundImage == null)
        {
            backgroundImage =
                OverlayUtilities.createBufferedImage(overlayBounds.getSize(),
                                                     hasAlpha);
        }
        return backgroundImage;
    }

    /**
     * Sets the background image to the one specified.  It does not have to be
     * the same size as the overlay but the it should be at least as big.
     * BackgroundMode must be in BACKGROUND_COPY for the background to be shown.
     */
    public void setBackgroundImage(BufferedImage img)
    {
        if(backgroundImage != img)
        {
            backgroundImage = img;
            repaint();
        }
    }

    /**
     * Mark a specific property as being dirty and needing to be rechecked.
     *
     * @param property The index of the property to be updated
     */
    protected void dirty(int property)
    {
        if(!initComplete)
            return;

        dirtyCheck[property] = true;
        if(updateManager != null)
            updateManager.updateRequested(this);
    }

    /**
     * Set the active buffer to the new index.
     *
     * @param bufferIndex The index of the buffer to use
     */
    protected void setActiveBuffer(int bufferIndex)
    {
        activeBuffer = bufferIndex;
        dirty(DIRTY_ACTIVE_BUFFER);
    }

    /**
     * Prepares the canvas to be painted.  This should only be called internally
     * or from an owner like the ScrollingOverlay class. paint(Graphics2D g)
     * should be used to paint the OverlayBase.
     *
     * @return The current graphics context to work with
     */
    protected Graphics2D getGraphics()
    {
        if(backgroundMode == BACKGROUND_COPY && backgroundImage != null)
            canvas.setData(backgroundImage.getRaster());

        Graphics2D g = (Graphics2D)canvas.getGraphics();

        if(antialiased)
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        return g;
    }

    /**
     * This is where the actualy drawing of the window takes place.  Override
     * this to alter the contents of what is shown in the window.
     *
     * @param g The graphics context to paint with
     */
    public void paint(Graphics2D g)
    {
    }

    /**
     * This is called to trigger a repaint of the overlay. This will return once
     * the back buffer has been built, but before the swap.
     */
    public void repaint()
    {
        if(!painting && initComplete)
        {
            painting = true;

            Graphics2D g = getGraphics();
            paint(g);
            g.dispose();

            updateBuffer(canvas, SubOverlay.NEXT_BUFFER);
            setActiveBuffer(SubOverlay.NEXT_BUFFER);

            painting = false;
        }
    }

    /**
     * Force an update of the nominated buffer with the contents of the given
     * image.
     *
     * @param image The contents to display as the image
     * @param bufferIndex The buffer to update
     */
    protected void updateBuffer(BufferedImage image, int bufferIndex)
    {
        synchronized(subOverlay)
        {
            for(int i = subOverlay.length - 1; i >= 0; i--)
            {
                subOverlay[i].updateBuffer(image, bufferIndex);
            }
        }
    }

    //------------------------------------------------------------------------
    // Local convenience methods
    //------------------------------------------------------------------------

    /**
     * Update the background colour on the drawn image now.
     */
    private void updateBackgroundColor()
    {
        int pixels[] = new int[overlayBounds.width * overlayBounds.height];
        int rgb = backgroundColor.getRGB();
        for(int i = pixels.length - 1; i >= 0; i--)
        {
            pixels[i] = rgb;
        }

        getBackgroundImage().setRGB(0,
                                    0,
                                    overlayBounds.width,
                                    overlayBounds.height,
                                    pixels,
                                    0,
                                    overlayBounds.width);
        repaint();
    }

    /**
     * Update the visibility state to either turn on or off the overlay.
     */
    private void syncVisible()
    {
        renderAttributes.setVisible(visible);
        dirtyCheck[DIRTY_VISIBLE] = false;
    }

    /**
     * Update the active buffer to be the new index. Means that someone has
     * requested an update and this is making it happen.
     */
    private void syncActiveBuffer()
    {
        synchronized(subOverlay)
        {
            for(int i = subOverlay.length - 1; i >= 0; i--)
                subOverlay[i].setActiveBufferIndex(activeBuffer);

            dirtyCheck[DIRTY_ACTIVE_BUFFER] = false;
        }
    }

    /**
     * Update the position of the overlay in the overall window. Note that it
     * does not change the size of the overlay, just re-adjusts the transforms
     * in the scene graph so that the overlay maintains the correct position
     * relative the canvas.
     */
    private void syncPosition()
    {
        synchronized(overlayBounds)
        {
            if(componentSize.width == 0 || componentSize.height == 0)
                return;

            if(canvas == null)
                canvas = OverlayUtilities.createBufferedImage(overlayBounds.getSize(),
                                                              hasAlpha);

            if(backgroundColor != null)
                updateBackgroundColor();

            // get the field of view and then calculate the width in meters of the
            // screen
            double c_width = 2 * CONSOLE_Z * Math.tan(fieldOfView * 0.5);

            // calculate the ratio between the canvas in pixels and the screen in
            // meters and use that to find the height of the screen in meters
            double scale = c_width / componentSize.width;
            double c_height = componentSize.height * scale;

            // The texture is upside down relative to the canvas so this has to
            // be flipped to be in the right place. bounds needs to have the correct
            // value to be used in Overlays that relay on it to know their position
            // like mouseovers
            float flipped_x = overlayBounds.x;
            float flipped_y = componentSize.height - overlayBounds.height -
                              overlayBounds.y;

            // build the plane offset
            Transform3D plane_offset = new Transform3D();
            Vector3d loc = new Vector3d(-c_width / 2 + flipped_x * scale,
                                        -c_height / 2 + flipped_y * scale,
                                        -CONSOLE_Z);

            plane_offset.setTranslation(loc);
            plane_offset.setScale(scale);
            consoleTG.setTransform(plane_offset);

            dirtyCheck[DIRTY_POSITION] = false;
        }
    }

    /**
     * Fixup the size of the overlay textures. Resizes and clears the texture
     * to fit the new size. Current implementation is really dumb - just tosses
     * everything and starts again. A more intelligent one would only replace
     * the border parts.
     */
    private void syncSize()
    {
        if(!fixedSize)
            overlayBounds = canvas3D.getBounds();

        if((overlayBounds.width != 0) && (overlayBounds.height != 0))
        {
            overlayTexGrp = new BranchGroup();
            overlayTexGrp.setCapability(BranchGroup.ALLOW_DETACH);

            consoleTG = new TransformGroup();
            consoleTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

            overlayTexGrp.addChild(consoleTG);

            List overlays = OverlayUtilities.subdivide(overlayBounds.getSize(),
                                                       minDivSize,
                                                       256);

            subOverlay = new SubOverlay[overlays.size()];
            int n = overlays.size();

            for(int i = 0; i < n; i++)
            {
                Rectangle current_space = (Rectangle)overlays.get(i);
                subOverlay[i] = new SubOverlay(current_space,
                                               numBuffers,
                                               hasAlpha,
                                               polygonAttributes,
                                               renderAttributes,
                                               textureAttributes,
                                               transparencyAttributes,
                                               material);
                consoleTG.addChild(subOverlay[i].getShape());
            }

            consoleBG.setChild(overlayTexGrp, 0);
        }
        else
        {
            overlayTexGrp = null;
            consoleTG = null;
            subOverlay = new SubOverlay[0];

            consoleBG.setChild(null, 0);
        }


        dirtyCheck[DIRTY_SIZE] = false;

        // now sync the position again as we've replaced the transform
        syncPosition();
    }
}
