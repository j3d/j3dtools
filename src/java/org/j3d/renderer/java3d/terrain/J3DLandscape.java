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
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface J3DLandscape
{
    /**
     * Set the current view location information based on a transform matrix.
     * Only the position and orientation information are extracted from this
     * matrix. Any shear or scale is ignored. Effectively, this transform
     * should be the view transform (particularly if you are using navigation
     * code from this codebase in the {@link org.j3d.renderer.java3d.navigation} package.
     *
     * @param t3d The transform to use as the view position
     */
    public void setView(Transform3D t3d);

    /**
     * Set the appearance generator to create new appearanace items. If null
     * is passed, it clears the current appearance settings
     *
     * @param gen The new generator instance to use
     */
    public void setAppearanceGenerator(AppearanceGenerator gen);

    /**
     * Get the currently set appearance generator. If not set, returns null.
     *
     * @return The current generator instance
     */
    public AppearanceGenerator getAppearanceGenerator();

    /**
     * Get the geometry group that this terrain is being rendered to.
     *
     * @return The parent group for all the terrain
     */
    public BranchGroup getSceneGraphObject();
}
