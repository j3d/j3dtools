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
 * Representation of a complete set of meshes to give a single object from
 * the file.
 * <p>
 *
 * A mesh consists of a collection of chunks. Each chunk contains different
 * information, such as vertices, materials etc.
 *
 * @author  Justin Couch
 * @version $Revision: 1.4 $
 */
public class ObjectMesh
{
    /** The master (uniform) scale to apply to all the objects */
    public float masterScale;

    /** The version of the mesh that was read */
    public int meshVersion;

    /** Listing of all the sub-objects in the mesh */
    public ObjectBlock[] blocks;

    /** Number of valid blocks available */
    public int numBlocks;

    /** Listing of all material information */
    public MaterialBlock[] materials;

    /** The number of valid materials available */
    public int numMaterials;

    /** Listing of all keyframe blocks available */
    public KeyframeBlock[] keyframes;

    /** The number of valid keyframe blocks available */
    public int numKeyframes;

    /** Ambient light setting. Null if not set */
    public float[] ambientLight;

    /**
     * Construct a new instance with blocks initialised to a size of 8.
     */
    public ObjectMesh()
    {
        blocks = new ObjectBlock[8];
        materials = new MaterialBlock[4];
        masterScale = 1;
    }
}
