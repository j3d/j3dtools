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
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class ObjectMesh
{
    /** Listing of all the sub-objects in the mesh */
    public ObjectBlock[] blocks;

    /** Number of valid blocks available */
    public int numBlocks;

    /**
     * Construct a new instance with blocks initialised to a size of 8.
     */
    public ObjectMesh()
    {
        blocks = new ObjectBlock[8];
    }
}
