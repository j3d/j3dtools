/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.terrain;

// Standard imports
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;

import javax.vecmath.Matrix3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

// Application specific imports
import org.j3d.renderer.java3d.navigation.FrameUpdateListener;
import org.j3d.terrain.Landscape;
import org.j3d.terrain.TerrainData;
import org.j3d.util.frustum.ViewFrustum;

/**
 * Representation of a Java3D-specific additional interfaces to the basic
 * Landscape interface.
 * <p>
 *
 * The landscape is used to control what it rendered on screen as the user
 * moves about the virtual environment. This instance does not need to maintain
 * all the polygons on the screen at any one time, but may control them as
 * needed.
 * <p>
 *
 * This object is independent of the culling algorithm. It represents something
 * that can be placed in a scenegraph and have view information passed to it
 * without the need to know the specific algorithm in use. To implement a
 * specific algorithm (eg ROAM) you would extend this class and implement the
 * {@link #setView(Tuple3f, Vector3f)} method. Every time that the scene
 * changes, you will be notified by this method. That means you should perform
 * any culling/LOD and update the scene graph at this point. This will be
 * called at most once per frame.
 * <p>
 *
 * For convenience, this class also implements {@link FrameUpdateListener} from
 * the {@link org.j3d.ui.navigation} package so that you can have fast, quick
 * navigation implementation in your code. If you wish to use your own custom
 * user input code, then there is no penalty for doing so. Simply call one of
 * the <code>setView()</code> methods directly with the transformation
 * information.
 * <p>
 *
 * If you are going to use this class with the navigation code, then you
 * should also make the internal geometry not pickable, and make this item
 * pickable. In this way, the navigation code will find this top-level
 * terrain definition and use it directly to make the code much faster. None
 * of these capabilities are set within this implementation, so it is up to
 * the third-party code to make it so via calls to the appropriate methods.
 * <p>
 *
 * The landscape provides an appearance generator for letting the end user
 * application control appearance settings. If this is not set then particular
 * implementation is free to do what it likes.
 *
 * @author Justin Couch, based on original ideas from Paul Byrne
 * @version $Revision: 1.1 $
 */
public abstract class J3DAbstractLandscape extends Landscape
    implements FrameUpdateListener, J3DLandscape
{
    /** Generator for appearance information. May be null */
    protected AppearanceGenerator appearanceGenerator;

    /**
     * Temporary variable to hold the position information extracted from
     * the full transform class.
     */
    private Vector3f tmpPosition;

    /**
     * Temporary variable to hold the orientation information extracted from
     * the matrix class.
     */
    private Vector3f tmpOrientation;

    /**
     * Temporary variable to hold the orientation matrix extracted from
     * the full transform class.
     */
    private Matrix3f tmpMatrix;

    /** Geometry used to represent this terrain at the J3D level */
    protected BranchGroup rootGroup;

    /**
     * Create a new Landscape with the set view and data. If either are not
     * provided, an exception is thrown. Uses the default appearance generator.
     *
     * @param view The viewing frustum to see the data with
     * @param data The raw data to view
     * @throws IllegalArgumentException either parameter is null
     */
    public J3DAbstractLandscape(ViewFrustum view, TerrainData data)
    {
        super(view, data);

        tmpPosition = new Vector3f();
        tmpOrientation = new Vector3f();
        tmpMatrix = new Matrix3f();

        rootGroup = new BranchGroup();
    }

    /**
     * Provide a landscape with a specific appearance generator set. If the
     * generator argument is null, then the default is used.
     *
     * @param view The viewing frustum to see the data with
     * @param data The raw data to view
     * @param gen The generator instance to use
     * @throws IllegalArgumentException either parameter is null
     */
    public J3DAbstractLandscape(ViewFrustum view,
                                TerrainData data,
                                AppearanceGenerator gen)
    {
        this(view, data);

        appearanceGenerator = gen;
    }

    //----------------------------------------------------------
    // Methods required by FrameUpdateListener
    //----------------------------------------------------------

    /**
     * The transition from one point to another is completed. Use this to
     * update the transformation.
     *
     * @param t3d The position of the final viewpoint
     */
    public void transitionEnded(Transform3D t3d)
    {
        landscapeView.viewingPlatformMoved();
        setView(t3d);
    }

    /**
     * The frame has just been updated with the latest view information.
     * Update the landscape rendered values now.
     *
     * @param t3d The position of the viewpoint now
     */
    public void viewerPositionUpdated(Transform3D t3d)
    {
        landscapeView.viewingPlatformMoved();
        setView(t3d);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the current view location information based on a transform matrix.
     * Only the position and orientation information are extracted from this
     * matrix. Any shear or scale is ignored. Effectively, this transform
     * should be the view transform (particularly if you are using navigation
     * code from this codebase in the {@link org.j3d.ui.navigation} package.
     *
     * @param t3d The transform to use as the view position
     */
    public void setView(Transform3D t3d)
    {
        t3d.get(tmpMatrix, tmpPosition);
        tmpOrientation.set(0, 0, -1);
        tmpMatrix.transform(tmpOrientation);

        setView(tmpPosition, tmpOrientation);
    }

    /**
     * Set the appearance generator to create new appearanace items. If null
     * is passed, it clears the current appearance settings
     *
     * @param gen The new generator instance to use
     */
    public void setAppearanceGenerator(AppearanceGenerator gen)
    {
        appearanceGenerator = gen;
    }

    /**
     * Get the currently set appearance generator. If not set, returns null.
     *
     * @return The current generator instance
     */
    public AppearanceGenerator getAppearanceGenerator()
    {
        return appearanceGenerator;
    }

    /**
     * Get the geometry group that this terrain is being rendered to.
     *
     * @return The parent group for all the terrain
     */
    public BranchGroup getSceneGraphObject()
    {
        return rootGroup;
    }
}
