/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.discreet;

// External imports
// None

// Local imports
// None

/**
 * Representation of a single camera and its paramaters needed for rendering.
 * <p>
 *
 * Cameras are described using the 0x4700 series of parameters.
 * <p>
 *
 * The block is defined as:
 * <pre>
 * N_CAMERA 0x4700
 *    point         (camera position)
 *    point         (camera target)
 *    float         (camera bank angle)
 *    float         (camera focal length in mm)
 *    CAM_SEE_CONE  0x4710
 *    CAM_RANGES    0x4720
 * </pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class CameraBlock
{
    /** The location of the the camera, */
    public float[] location;

    /** The location the camera is pointing at */
    public float[] target;

    /**
     * The rotation angle in degrees (relative to the local Y axis?) for the
     * camera.
     */
    public float bankAngle;

    /** The focus distance for the camera (in millimetres) */
    public float focus;

    /** Flag indicating whether the camera view cone should be shown */
    public boolean seeOutline;

    /**
     * Atmospheric effect ranges for the camera. Null if not set. Length 2 if
     * set. [0] is near raidus of effect. [0] is far radius of effect. Both
     * must be greater than or equal to zero.
     */
    public float[] ranges;

    /**
     * Create a new camera block and set it up with basic details.
     */
    public CameraBlock()
    {
        target = new float[3];
        location = new float[3];
        seeOutline = false;
    }
}
