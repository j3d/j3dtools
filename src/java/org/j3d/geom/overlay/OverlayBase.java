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
import java.awt.event.MouseListener;

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
 * The class implements the AWT component listener interface so that it can
 * automatically resize the overlay's base image in response to the canvas
 * changing size. This will ensure that everything is correctly located on the
 * screen after the resize.
 *
 * @author David Yazel, Justin Couch
 * @version $Revision: 1.1 $
 */
public class OverlayBase implements Overlay, ScreenComponent, ComponentListener
{
    // I do not undersand what this is for
    private final static double CONSOLE_Z = 2.1f;

    /** The current background mode. Defaults to copy */
    protected int backgroundMode = BACKGROUND_COPY;
    protected int[] relativePosition = {PLACE_LEFT, PLACE_TOP};

    private BufferedImage backgroundImage;
    private boolean hasAlpha;
    private boolean visible;
    private boolean antialiased;

    /** Canvas bounds occupied by this overlay. */
    private Rectangle bounds;
    private Dimension offset;
    private UpdateManager updateManager;

    /** Canvas we are displayed on */
    private Canvas3D canvas3D;

    /** Drawing area that we scribble our stuff on */
    protected BufferedImage canvas;

    /**
     * The list of sub-overlay areas. Starts pre-created with a zero length
     * array. Blocking is performed using this so it can't be null.
     */
    protected SubOverlay[] subOverlay;         // list of SubOverlay nodes

    protected int activeBuffer = SubOverlay.NEXT_BUFFER;

    protected BranchGroup consoleBranchGroup;        // branch group for overlay
    protected TransformGroup consoleTransformGroup;  // transform group -> screen coords

    // shared resources for the sub-overlays

    private RenderingAttributes renderAttributes;
    private PolygonAttributes polygonAttributes;
    private TextureAttributes textureAttributes;
    private TransparencyAttributes transparencyAttributes;

    // checks for altered elements

    public final static int VISIBLE = 0;
    public final static int POSITION = 1;
    public final static int ACTIVE_BUFFER = 2;
    private boolean[] dirtyCheck = new boolean[3];

    /** Fires appropriate mouse events */
    private ComponentMouseManager mouseManager;

    /** Used to avoid calls to repaint backing up */
    private boolean painting = false;

    /**
     * Creates a new overlay covering the given canvas bounds. Updates are managed
     * automatically. This Overlay must still be attached to the view platform
     * transform.
     *
     * @param canvas3D Canvas being drawn onto
     * @param bounds    Bounds on the canvas covered by the overlay
     */
    public OverlayBase(Canvas3D canvas3D, Rectangle bounds)
    {
        this(canvas3D, bounds, true, false, null);
    }

    public OverlayBase(Canvas3D canvas3D, Rectangle bounds, UpdateManager manager)
    {
        this(canvas3D, bounds, true, false, manager);
    }

    public OverlayBase(Canvas3D canvas3D,
                       Rectangle bounds,
                       boolean clipAlpha,
                       boolean blendAlpha)
    {
        this(canvas3D, bounds, clipAlpha, blendAlpha, null);
    }

    public OverlayBase(Canvas3D canvas3D, Rectangle bounds,
            boolean clipAlpha, boolean blendAlpha,
            UpdateManager updateManager)
    {
        this(canvas3D, bounds, clipAlpha, blendAlpha, updateManager, 2);
    }

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     *
     * @param canvas3D      The canvas the overlay is drawn on
     * @param bounds         The part of the canvas covered by the overlay
     * @param clipAlpha     Should the polygon clip where alpha is zero
     * @param blendAlpha    Should we blend to background where alpha is < 1
     * @param updateManager Responsible for allowing the Overlay to update
     *                       between renders. If this is null a default
     *                       manager is created.
     * @param numBuffers    The number of buffers to generate, the default
     *                       is two.
     */
    public OverlayBase(Canvas3D canvas3D,
                       Rectangle bounds,
                       boolean clipAlpha,
                       boolean blendAlpha,
                       UpdateManager updateManager,
                       int numBuffers)
    {
        this.canvas3D = canvas3D;
        this.visible = true;
        this.antialiased = true;
        this.bounds = bounds;
        this.offset = new Dimension(bounds.x, bounds.y);
        this.hasAlpha = clipAlpha || blendAlpha;
        this.canvas = OverlayUtilities.createBufferedImage(bounds.getSize(), hasAlpha);
        this.mouseManager = new ComponentMouseManager(canvas3D, this);

        canvas3D.addComponentListener(this);

        // define the branch group where we are putting all the sub-overlays

        consoleBranchGroup = new BranchGroup();
        consoleTransformGroup = new TransformGroup();
        consoleTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        consoleBranchGroup.addChild(consoleTransformGroup);

        if(updateManager == null)
        {
            UpdateControlBehavior updateBehavior = new UpdateControlBehavior(this);
            updateBehavior.setSchedulingBounds(new BoundingSphere());
            consoleBranchGroup.addChild(updateBehavior);
            updateManager = updateBehavior ;
        }

        this.updateManager = updateManager;

        // define the rendering attributes used by all sub-overlays

        renderAttributes = new RenderingAttributes();
        if(clipAlpha)
        {
            renderAttributes.setAlphaTestFunction(RenderingAttributes.NOT_EQUAL);
            renderAttributes.setAlphaTestValue(0);
        }

        renderAttributes.setDepthBufferEnable(true);
        renderAttributes.setDepthBufferWriteEnable(true);
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

        // if this needs to support transparancy set up the blend

        if(hasAlpha)
        {
            transparencyAttributes = new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.0f);
            textureAttributes.setTextureBlendColor(new Color4f(0, 0, 0, 1));
        }

        List overlays = OverlayUtilities.subdivide(bounds.getSize(), 16, 256);

        synchronized(subOverlay)
        {
            subOverlay = new SubOverlay[overlays.size()];
            int n = overlays.size();

            for(int i = 0; i < n; i++)
            {
                Rectangle current_space = (Rectangle)overlays.get(i);
                subOverlay[i] = new SubOverlay(current_space, numBuffers);
                consoleTransformGroup.addChild(subOverlay[i].getShape());
            }
        }

        initialize();

        // Dirty everything and an initial WakeupOnActivation will sync everything

        for(int i = dirtyCheck.length - 1; i >= 0; i--)
        {
            dirtyCheck[i] = true;
        }

        repaint();
    }

    protected void initialize()
    {
    }

    public Rectangle getBounds()
    {
        return bounds;
    }

    public UpdateManager getUpdateManager()
    {
        return updateManager;
    }

    public void setUpdateManager(UpdateManager updateManager)
    {
        this.updateManager = updateManager;
        updateManager.updateRequested();
    }

    /**
     * Sets the relative offset of the overlay. How this translates into
     * screen coordinates depends on the value of relativePosition()
     */
    public void setOffset(Dimension offset)
    {
        setOffset(offset.width, offset.height);
    }

    /**
     * Sets the relative offset of the overlay. How this translates into
     * screen coordinates depends on the value of relativePosition()
     */
    public void setOffset(int width, int height)
    {
        if(offset.width != width || offset.height != height)
        {
            synchronized(offset)
            {
                offset.width = width;
                offset.height = height;
                dirty(POSITION);
            }
        }
    }

    /**
     * Sets the relative position of the overlay on the screen using a 2 dimensional array.
     *
     * @param relativePosition[X_PLACEMENT] May be PLACE_LEFT, PLACE_RIGHT, or PLACE_CENTER
     * @param relativePosition[Y_PLACEMENT] May be PLACE_TOP, PLACE_BOTTOM, or PLACE_CENTER
     */
    public void setRelativePosition(int[] relativePositon)
    {
        setRelativePosition(relativePosition[X_PLACEMENT], relativePosition[Y_PLACEMENT]);
    }

    /**
     * Sets the relative position of the overlay on the screen.
     *
     * @param xType May be PLACE_LEFT, PLACE_RIGHT, or PLACE_CENTER
     * @param yType May be PLACE_TOP, PLACE_BOTTOM, or PLACE_CENTER
     */
    public void setRelativePosition(int xType, int yType)
    {
        if(relativePosition[X_PLACEMENT] != xType ||
           relativePosition[Y_PLACEMENT] != yType)
        {
            relativePosition[X_PLACEMENT] = xType;
            relativePosition[Y_PLACEMENT] = yType;
            dirty(POSITION);
        }
    }

    /**
     * Return the root of the overlay and its sub-overlays so it can be
     * added to the scene graph
     */
    public BranchGroup getRoot()
    {
        return consoleBranchGroup;
    }

    /**
     * Sets whether drawing onto this Overlay is anialiased.
     */
    public void setAntialiased(boolean antialiased)
    {
        if(this.antialiased != antialiased)
        {
            this.antialiased = antialiased;
            repaint();
        }
    }

    public boolean isAntialiased()
    {
        return antialiased;
    }

    /**
     * Returns the canvas being drawn on.
     */
    public Canvas3D getCanvas()
    {
        return canvas3D;
    }

    /**
     * Prepares the canvas to be painted.  This should only be called internally
     * or from an owner like the ScrollingOverlay class. paint(Graphics2D g)
     * should be used to paint the OverlayBase.
     */
    protected Graphics2D getGraphics()
    {
        if(backgroundMode == BACKGROUND_COPY && backgroundImage != null)
            canvas.setData(backgroundImage.getRaster());

        Graphics2D g =(Graphics2D)canvas.getGraphics();
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
     * This is called to trigger a repaint of the overlay. This will return once
     * the back buffer has been built, but before the swap.
     */
    public void repaint()
    {
        if(!painting)
        {
            painting = true;

            Graphics2D g = getGraphics();
            paint(g);
            g.dispose();

            updateBuffer(canvas, SubOverlay.NEXT_BUFFER);
            setActiveBuffer(SubOverlay.NEXT_BUFFER);

            painting = false;
        }
        else
        {
            System.err.println("Skipped paint in: " + this);
        }
    }

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

    /**
     * Changes the visibility of the overlay.
     */
    public void setVisible(boolean visible)
    {
        if(this.visible != visible)
        {
            this.visible = visible;
            dirty(VISIBLE);
        }
    }

    public boolean isVisible()
    {
        return visible;
    }

    /**
     * This is where the actualy drawing of the window takes place.  Override
     * this to alter the contents of what is shown in the window.
     */
    public void paint(Graphics2D g)
    {
    }

    protected void setActiveBuffer(int activeBuffer)
    {
        this.activeBuffer = activeBuffer;
        dirty(ACTIVE_BUFFER);
    }

    /**
     * Sets the background to a solid color. If a background image already exists then
     * it will be overwritten with this solid color.  It is completely appropriate to
     * have an alpha component in the color if this is a alpha capable overlay.
     * In general you should only use background images if this is an overlay that is
     * called frequently, since you could always paint it inside the paint()method.
     * BackgroundMode must be in BACKGROUND_COPY for the background to be shown.
     */
    public void setBackgroundColor(Color color)
    {
        int pixels[] = new int[bounds.width * bounds.height];
        int rgb = color.getRGB();
        for(int i = pixels.length - 1; i >= 0; i--)
        {
            pixels[i] = rgb;
        }
        getBackgroundImage().setRGB(0, 0, bounds.width, bounds.height, pixels, 0, bounds.width);
        repaint();
    }

    /**
     * Returns the background for the overlay. Updates to this image will not be shown in
     * the overlay until repaint()is called.
     * BackgroundMode must be in BACKGROUND_COPY for the background to be shown.
     */
    public BufferedImage getBackgroundImage()
    {
        if(backgroundImage == null)
        {
            backgroundImage = OverlayUtilities.createBufferedImage(bounds.getSize(), hasAlpha);
        }
        return backgroundImage;
    }

    /**
     * Sets the background image to the one specified.  It does not have to be the same
     * size as the overlay but the it should be at least as big.
     * BackgroundMode must be in BACKGROUND_COPY for the background to be shown.
     */
    public void setBackgroundImage(BufferedImage backgroundImage)
    {
        if(this.backgroundImage != backgroundImage)
        {
            this.backgroundImage = backgroundImage;
            repaint();
        }
    }

    /**
     * Sets the background mode.  BACKGROUND_COPY will copy the raster data from the
     * background into the canvas before paint()is called. BACKGROUND_NONE will cause
     * the background to be disabled and not used.
     */
    public void setBackgroundMode(int mode)
    {
        if(backgroundMode != mode)
        {
            backgroundMode = mode;
            repaint();
        }
    }

    public void addMouseListener(MouseListener listener)
    {
        mouseManager.addMouseListener(listener);
    }

    public void removeMouseListener(MouseListener listener)
    {
        mouseManager.removeMouseListener(listener);
    }

    private void syncPosition()
    {
        synchronized(bounds)
        {
            Dimension canvas3DSize = canvas3D.getSize();

            OverlayUtilities.repositonBounds(bounds, relativePosition,
                            canvas3DSize, offset);

            // get the field of view and then calculate the width in meters of the
            // screen

            double fov = canvas3D.getView().getFieldOfView();
            double consoleWidth = 2 * Math.tan(fov / 2.0)* CONSOLE_Z;

            // calculate the ratio between the canvas in pixels and the screen in
            // meters and use that to find the height of the screen in meters

            double scale = consoleWidth / canvas3DSize.getWidth();
            double consoleHeight = canvas3DSize.getHeight()* scale;

            // The texture is upside down relative to the canvas so this has to
            // be flipped to be in the right place. bounds needs to have the correct
            // value to be used in Overlays that relay on it to know their position
            // like mouseovers

            Point flippedPoint = new Point(bounds.x,
                           canvas3DSize.height - bounds.height - bounds.y);

            // build the plane offset

            Transform3D planeOffset = new Transform3D();
            planeOffset.setTranslation(new Vector3d(-consoleWidth / 2 + flippedPoint.getX()* scale,
                                -consoleHeight / 2 + flippedPoint.getY()* scale,
                                -CONSOLE_Z));
            planeOffset.setScale(scale);
            consoleTransformGroup.setTransform(planeOffset);

            dirtyCheck[POSITION] = false;
        }
    }

    private void syncVisible()
    {
        renderAttributes.setVisible(visible);
        dirtyCheck[VISIBLE] = false;
    }

    private void syncActiveBuffer()
    {
        synchronized(subOverlay)
        {
            for(int i = subOverlay.length - 1; i >= 0; i--)
            {
                subOverlay[i].setActiveBufferIndex(activeBuffer);
            }
            dirtyCheck[ACTIVE_BUFFER] = false;
        }
    }

    public void dirty(int property)
    {
        dirtyCheck[property] = true;
        if(updateManager != null)
            updateManager.updateRequested();
        else
            System.err.println("Null update manager in: " + this);
    }

    public void update()
    {
        if(dirtyCheck[POSITION])
            syncPosition();

        if(dirtyCheck[VISIBLE])
            syncVisible();

        if(dirtyCheck[ACTIVE_BUFFER])
            syncActiveBuffer();
    }

    //------------------------------------------------------------------------
    // Methods from the ComponentListener interface
    //------------------------------------------------------------------------

    /**
     *
     */
    public void componentResized(ComponentEvent e)
    {
        dirty(POSITION);
    }

    public void componentShown(ComponentEvent e)
    {
    }

    public void componentMoved(ComponentEvent e)
    {
        dirty(POSITION);
    }

    public void componentHidden(ComponentEvent e)
    {
    }
}
