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
 * Cameras are described using the 0x4700 series of parameters. Not all of them
 * are mapped here currently.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class CameraBlock
{
    /** The location of the the camera, */
    public float[] location;

    /** The location the camera is pointing at */
    public float[] target;

    /** The rotation angle (relative to the local Y axis?) for the camera */
    public float bankAngle;

    /** The focus distance for the camera */
    public float focus;

    /**
     * Create a new camera block and set it up with basic details.
     */
    public CameraBlock()
    {
        target = new float[3];
        location = new float[3];
    }
}
