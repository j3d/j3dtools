/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.terrain.roam;

// Standard imports
import javax.media.j3d.*;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Matrix3f;

// Application specific imports
import org.j3d.terrain.*;
import org.j3d.terrain.roam.*;

import org.j3d.util.frustum.ViewFrustum;

import org.j3d.renderer.java3d.terrain.J3DLandscape;
import org.j3d.renderer.java3d.terrain.AppearanceGenerator;
import org.j3d.renderer.java3d.texture.TextureCreateUtils;

/**
 * ROAM implmentation of a landscape using the split-merge combination
 * algorithm.
 * <p>
 *
 * First patch is at 0,0 in x, z and then patches are laid out along the
 * +ve x axis and the -ve z axis
 *
 * @author Paul Byrne, Justin Couch
 * @version $Revision: 1.1 $
 */
public class J3DSplitMergeLandscape extends ROAMSplitMergeLandscape
    implements J3DLandscape
{
    /** Generator for appearance information. May be null */
    private AppearanceGenerator appearanceGenerator;

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
    private BranchGroup rootGroup;

    /** Utility for wrapping textures */
    private TextureCreateUtils texUtils;

    /**
     * Creates new Landscape based on the view information and the terrain
     * data. If the terrain data is based on static data, the terrain is
     * built using the default patch size.
     *
     * @param view The view frustum looking at this landscape
     * @param data The raw data for the terrain
     * @throws IllegalArgumentException The static grid is not n^2 + 1 in size
     */
    public J3DSplitMergeLandscape(ViewFrustum view, TerrainData data)
    {
        super(view, data);
        init();
    }

    /**
     * Creates new Landscape based on the view information and static terrain
     * data, with a controlable patch size. The patch size must be a power of
     * two. If the terrain data object provides tiled terrain, the patchSize
     * request is ignored. The patchSize must be a power of two, otherwise an
     * exception will be thrown.
     *
     * @param view The view frustum looking at this landscape
     * @param data The raw data for the terrain
     * @param patchSize The number of grid points per patch side, power 2.
     * @throws IllegalArgumentException The patchSize was < 0 or not a power
     *    of two.
     */
    public J3DSplitMergeLandscape(ViewFrustum view,
                                  TerrainData data,
                                  int patchSize)
    {
        super(view, data, patchSize);
        init();
    }

    /**
     * Provide a landscape with a specific appearance generator set. If the
     * generator argument is null, then the default is used.
     *
     * @param view The viewing frustum to see the data with
     * @param data The raw data to view
     * @param gen The generator instance to use
     * @throws IllegalArgumentException either parameter is null
     * @throws IllegalArgumentException The patchSize was < 0 or not a power
     *    of two.
     */
    public J3DSplitMergeLandscape(ViewFrustum view,
                                  TerrainData data,
                                  AppearanceGenerator gen)
    {
        super(view, data);

        init();

        appearanceGenerator = gen;
    }

    /**
     * Provide a landscape with a specific appearance generator set. If the
     * generator argument is null, then the default is used.
     *
     * @param view The viewing frustum to see the data with
     * @param data The raw data to view
     * @param gen The generator instance to use
     * @param patchSize The number of grid points per patch side, power 2.
     * @throws IllegalArgumentException either parameter is null
     * @throws IllegalArgumentException The patchSize was < 0 or not a power
     *    of two.
     */
    public J3DSplitMergeLandscape(ViewFrustum view,
                                  TerrainData data,
                                  int patchSize,
                                  AppearanceGenerator gen)
    {
        super(view, data, patchSize);

        init();
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
    // Methods defined by J3DLandscape
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

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Create a new patch object instance that is located at the given
     * position within the tile.
     *
     * @param eastPosition The east coordinate of the patch
     * @param northPosition The north coordinate of the patch
     * @param xTile The tile coordinate of the patch along the X axis
     * @param yTile The tile coordinate of the patch along the Y axis
     */
    protected ROAMPatch createPatch(int x, int y, int xTile, int yTile)
    {
        AppearanceGenerator app_gen = getAppearanceGenerator();
        Appearance app = null;
        Patch p = null;

        if(app_gen != null)
        {
            TiledTerrainData t_data = (TiledTerrainData)terrainData;
            BufferedImage img = t_data.getTexture(xTile, yTile);
            Texture2D tex = texUtils.createTexture2D(img);

            app = app_gen.createAppearance();
            app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
            app.setTexture(tex);
        }

        p = new Patch(terrainData,
                      patchSize,
                      app,
                      landscapeView,
                      x, y);

        return p;
    }

    /**
     * Update the patch representation to be at the new tile location. This
     * will me replacing the existing texture with a new texture.
     *
     * @param patch The patch instance to update
     * @param xTile The new tile coordinate of the patch along the X axis
     * @param yTile The new tile coordinate of the patch along the Y axis
     */
    protected void updatePatch(ROAMPatch patch, int xTile, int yTile)
    {
        Patch p = (Patch)patch;

        // Not the most efficient representation. Probably want to run with
        // some sort of caching strategy here.
        TiledTerrainData t_data = (TiledTerrainData)terrainData;
        BufferedImage img = t_data.getTexture(xTile, yTile);
        Texture2D tex = texUtils.createTexture2D(img);

        Appearance app = p.getAppearance();
        app.setTexture(tex);
    }

    /**
     * Add the newly created patch to the renderer specific structures. All
     * the ROAM-specific initialization is complete, so just handle the
     * rendering items now.
     */
    protected void addPatch(ROAMPatch patch)
    {
        Patch p = (Patch)patch;
        rootGroup.addChild(p.getSceneGraphObject());
    }

    /**
     * Internal common initialization method.
     */
    private void init()
    {
        tmpPosition = new Vector3f();
        tmpOrientation = new Vector3f();
        tmpMatrix = new Matrix3f();

        rootGroup = new BranchGroup();

        texUtils = new TextureCreateUtils();
    }
}
