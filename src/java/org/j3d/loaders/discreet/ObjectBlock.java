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
 * Representation of a collection of triangle meshes that form a single block
 * of data in the file.
 * <p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class ObjectBlock
{
    /** A name or label associated with this block */
    public String name;

    /** Array of mesh instances forming this block */
    public TriangleMesh[] meshes;

    /** Number of valid items in the mesh list */
    public int numMeshes;

    /**
     * Construct a new instance with meshes initialised to a size of 8.
     */
    public ObjectBlock()
    {
        meshes = new TriangleMesh[8];
    }
}
