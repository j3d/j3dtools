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
 * Representation of a collection of keyframes for animating the object.
 * <p>
 *
 * A keyframe consists of
 * <pre>
 * KEYFRAMER CHUNK 0xB000
 *     MESH INFORMATION BLOCK 0xB002
 *     SPOT LIGHT INFORMATION BLOCK 0xB007
 *     FRAMES (START AND END) 0xB008
 * </pre>
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class KeyframeBlock
{
    /** The revision information */
    public int revision;

    /** A string referencing the external keyframes */
    public String filename;

    /** The length of this animation in frames */
    public int animationLength;

    /**
     * The current frame number. Used to determine which single frame will be
     * rendered or will be active when entering the Keyframer.
     */
    public int currentFrame;


    /** The start frame number for this animation. */
    public int startFrame;

    /** The last frame number for this animation. */
    public int endFrame;

    /** Information about the mesh itself */
    public KeyframeMeshBlock meshInfo;

    /** Information about spotlights */
    public KeyframeSpotlightBlock spotlightInfo;

    /** The listing of frames for this animation. Null if none set. */
    public KeyframeFrameBlock[] frames;

    /** The number of valid frames in this block */
    public int numFrames;
}
