/*****************************************************************************
 *                        Teseract Software, LLP (c) 2001
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

import javax.vecmath.Color4f;
import javax.vecmath.Vector3d;

// Application specific imports
// none

/**
 * An overlay implementation that uses a Java3D
 * {@link javax.media.j3d.Texture2D} object for the renderable surface.
 * <p>
 *
 * This is different to other overlays in this package in that it assumes
 * another part of the application has created the pre-canned texture instance
 * to use rather than internally generating it.
 * <p>
 *
 * <b>Note:</b><br>
 * Textures, by default, don't look too good if you just give it the straight
 * image. In order to have pixel-perfect textures, you should also have the
 * following setup prior to passing the textures to this class:
 * <pre>
 *   texture.setMagFilter(Texture.NICEST);
 *   texture.setMinFilter(Texture.FASTEST);
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class TextureOverlay implements Overlay, ComponentListener
{
    /** I do not undersand what this is for */
    private final static double CONSOLE_Z = 2.1f;

    /** Common texture coordinates used for all overlay instances */
    private static final float[] TEXTURE_COORDS =
    {
        1, 0,
        1, 1,
        0, 1,
        0, 0
    };

    /** Mark the visible flag as dirty */
    protected final static int DIRTY_VISIBLE = 0;

    /** Mark the position as dirty and needing correction */
    protected final static int DIRTY_POSITION = 1;

    /** Mark the active buffer as dirty and needing swapping */
    protected final static int DIRTY_ACTIVE_BUFFER = 2;

    /** Mark the size as dirty and needing correction */
    protected final static int DIRTY_SIZE = 3;


    /** The current background mode. Defaults to copy */
    private int backgroundMode = BACKGROUND_COPY;

    /** Flag holding the visibility state of this overlay */
    private boolean visible;

    /** Flag for the anti-aliased state */
    private boolean antialiased;

    /** Canvas bounds occupied by this overlay. */
    private Rectangle overlayBounds;

    /** The bounds of the Canvas3D this overlay is rendered in */
    private Dimension componentSize;

    /** The field of view for the canvas */
    private double fieldOfView;

    /** The update manager for keeping us in sync */
    private UpdateManager updateManager;

    /** Canvas we are displayed on */
    private Canvas3D canvas3D;

    /** Root branchgroup for the entire overlay system */
    protected BranchGroup consoleBG;

    /** Transformation to make the raster become screen coords as well */
    private TransformGroup consoleTG;

    /** Appearance used by the overlay so that we can change the textures */
    private Appearance appearance;

    /** The geometry of the object to allow resetting */
    private QuadArray geometry;

    /** Rendering attributes to allow the visibility state to change */
    private RenderingAttributes renderAttributes;

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

    /** List of the dirty flag settings */
    private boolean[] dirtyCheck = new boolean[DIRTY_SIZE + 1];

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     * If the bounds are null, then resize the overlay to fit the canvas
     * and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @throws IllegalArgumentException Both the canvas and bounds are null
     */
    public TextureOverlay(Canvas3D canvas, Dimension size)
    {
        this(canvas, size, true, false, null, null);
    }

    /**
     * Constructs an overlay window. This window will not be visible
     * unless it is added to the scene under the view platform transform
     * If the bounds are null, then resize the overlay to fit the canvas
     * and then track the size of the canvas.
     *
     * @param canvas The canvas the overlay is drawn on
     * @param size The size of the overlay in pixels
     * @param The texture to be displayed. May be null
     * @throws IllegalArgumentException Both the canvas and bounds are null
     */
    public TextureOverlay(Canvas3D canvas,
                          Dimension size,
                          Texture2D texture)
    {
        this(canvas, size, true, false, null, texture);
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
     * @param The texture to be displayed. May be null
     * @throws IllegalArgumentException Both the canvas and bounds are null
     */
    public TextureOverlay(Canvas3D canvas,
                          Dimension size,
                          boolean clipAlpha,
                          boolean blendAlpha,
                          UpdateManager updateManager,
                          Texture2D texture)
    {
        if(canvas == null && size == null)
            throw new IllegalArgumentException("Both canvas and size null");

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
        }
        else
        {
            overlayBounds = new Rectangle(0, 0, size.width, size.height);
            fixedSize = true;

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
        boolean hasAlpha = clipAlpha || blendAlpha;

        if(!fixedSize || (canvas3D != null))
            canvas3D.addComponentListener(this);

        // define the branch group where we are putting all the sub-overlays

        consoleBG = new BranchGroup();
        consoleBG.setCapability(BranchGroup.ALLOW_DETACH);

        consoleTG = new TransformGroup();
        consoleTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        consoleBG.addChild(consoleTG);

        this.updateManager = updateManager;

        PolygonAttributes pa;
        TextureAttributes ta;
        TransparencyAttributes trans = null;

        // define the rendering attributes used by all sub-overlays
        renderAttributes = new RenderingAttributes();
        if(clipAlpha)
        {
            renderAttributes.setAlphaTestFunction(RenderingAttributes.NOT_EQUAL);
            renderAttributes.setAlphaTestValue(0);
        }

        renderAttributes.setDepthBufferEnable(false);
        renderAttributes.setDepthBufferWriteEnable(true);
        renderAttributes.setIgnoreVertexColors(true);
        renderAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_READ);
        renderAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);

        // define the polygon attributes for all the sub-overlays
        pa = new PolygonAttributes();
        pa.setBackFaceNormalFlip(false);
        pa.setCullFace(PolygonAttributes.CULL_NONE);
        pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);

        // define the texture attributes for all the sub-overlays
        ta = new TextureAttributes();
        ta.setTextureMode(TextureAttributes.REPLACE);
        ta.setPerspectiveCorrectionMode(TextureAttributes.FASTEST);

        // if this needs to support transparancy set up the blend
        if(hasAlpha)
        {
            trans =
                new TransparencyAttributes(TransparencyAttributes.BLENDED,
                                           1.0f);
            ta.setTextureBlendColor(new Color4f(0, 0, 0, 1));
        }

        // Now let's construct the geometry to match
        appearance = new Appearance();
        appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

        appearance.setPolygonAttributes(pa);
        appearance.setRenderingAttributes(renderAttributes);
        appearance.setTextureAttributes(ta);

        if(trans != null)
            appearance.setTransparencyAttributes(trans);

        if(texture != null)
            appearance.setTexture(texture);

        Material material = new Material();
        material.setLightingEnable(false);
        appearance.setMaterial(material);

        int format = QuadArray.COORDINATES | QuadArray.TEXTURE_COORDINATE_2;
        geometry = new QuadArray(4, format);
        geometry.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);

        float[] vertices =
        {
            overlayBounds.width, 0,                    0,
            overlayBounds.width, overlayBounds.height, 0,
            0,                   overlayBounds.height, 0,
            0,                   0,                    0
        };

        geometry.setCoordinates(0, vertices);
        geometry.setTextureCoordinates(0, 0, TEXTURE_COORDS);

        Shape3D shape = new Shape3D();
        shape.setAppearance(appearance);
        shape.setGeometry(geometry);

        consoleTG.addChild(shape);

        dirtyCheck[DIRTY_VISIBLE] = true;
        dirtyCheck[DIRTY_POSITION] = true;
    }

    /**
     * Post construction initialisation before turning the overlay live. Should
     * always be called by the end user before starting to make use of this
     * overlay instance.
     */
    public void initialize()
    {
        initComplete = true;
    }

    /**
     * Return the root of the Overlay so it can be added to
     * the scene graph. This should be added to the view transform
     * group of the parent application.
     *
     * A branch group representing the overlay
     */
    public BranchGroup getRoot()
    {
        return consoleBG;
    }

    /**
     * Returns the rectangular portion of the canvas that this overlay covers.
     *
     * @return A rectangle representing the bounds
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
    }

    /**
     * Sets the relative offset of the overlay. How this translates into
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
     * Sets whether drawing onto this Overlay is anialiased. If called after
     * the overlay has gone live, it will have no effect.
     *
     * @param state true if this overlay should antialias the lines
     */
    public void setAntialiased(boolean state)
    {
        if(antialiased != state)
        {
            antialiased = state;
        }
    }

    /**
     * Returns whether drawing on this overlay is anti-aliased.
     *
     * @return true if the overlay is antialiased
     */
    public boolean isAntialiased()
    {
        return antialiased;
    }

    /**
     * Changes the visibility of the Overlay.
     *
     * @param visible true to make the overlay visible
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
     * Change the texture to the new version. If the constructor set a bounds
     * then the texture will be sized to fit that. If no bounds were set then
     * the object will be resized to fit the texture's new size.
     *
     * @param tex The new texture object to use
     */
    public void setTexture(Texture tex)
    {
        appearance.setTexture(tex);
    }

    /**
     * Mark a specific property as being dirty and needing to be rechecked.
     *
     * @param property The index of the property to be updated
     */
    private void dirty(int property)
    {
        if(!initComplete)
            return;

        dirtyCheck[property] = true;
        if(updateManager != null)
            updateManager.updateRequested(this);
        else
            System.err.println("Null update manager in: " + this);
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

//            OverlayUtilities.repositonBounds(overlayBounds,
//                                             relativePosition,
//                                             componentSize,
//                                             offset);

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
            renderAttributes.setVisible(true);

            float[] vertices =
            {
                overlayBounds.width, 0,                    0,
                overlayBounds.width, overlayBounds.height, 0,
                0,                   overlayBounds.height, 0,
                0,                   0,                    0
            };

            geometry.setCoordinates(0, vertices);
        }
        else
        {
            renderAttributes.setVisible(false);
        }


        dirtyCheck[DIRTY_SIZE] = false;

        // now sync the position again as we've replaced the transform
        syncPosition();
    }
}
