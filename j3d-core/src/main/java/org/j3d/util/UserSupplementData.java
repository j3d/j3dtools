/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util;

// External imports
// none

// Local imports
// none

/**
 * A generic holder for the various forms of data that would be stored in a
 * Java3D node's userData.
 * <P>
 *
 * This library makes heavy use of the userData system provided by the core
 * Java3D nodes. The types are varied and often more than one part of this
 * API (or end-applications derived from this API) want to make use of it, and
 * they clash.
 * <p>
 *
 * Several of the APIs within this codebase assume the existance of this class
 * for processing. If you don't provide it and start replacing it with your own
 * structure, things can and will break. If your application is making use of
 * user data as well, it is <i>highly</i> recommended that you extend this
 * class and add extra information as fields.
 *
 * @version 0.1
 */
public class UserSupplementData
{
    /**
     * Geometry data that could be used as the source of terrain following or
     * collision detection. There are a number of classes that could appear
     * here and the user code will need to check which has been provided. The
     * most likely candidates are {@link org.j3d.geom.GeometryData} and
     * {@link org.j3d.ui.navigation.HeightDataSource}.
     */
    public Object geometryData;

    /**
     * Flag to say whether this object or group is eligible for use in
     * collision detection and avoidance routines. If it is detected in a
     * Group node then anything below this group node will not be eligible,
     * thus allowing sub-sections of the scene graph to be marked off within
     * a larger structure. A false value will disable collision detection for
     * this group/shape. Default value is true;
     */
    public boolean collidable;

    /**
     * Flag to say whether this object or group is eligible for use in
     * terrain following routines. If it is detected in a Group node then
     * anything below this group node will not be eligible, thus allowing
     * sub-sections of the scene graph to be marked off within a larger
     * structure. A false value will disable terrain following for
     * this group/shape. Default value is true;
     */
    public boolean isTerrain;

    /**
     * Generic user data. As this class assumes it will take the place of the
     * normal user data, we need somewhere to store generic junk. This is it.
     */
    public Object userData;

    /**
     * Construct a new instance of the data.  Collidable and isTerrain
     * initialised to false.
     */
    public UserSupplementData()
    {
        collidable = true;
        isTerrain = true;
    }
}

