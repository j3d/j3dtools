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
 * A single set of material information for each triangle mesh.
 * <p>
 *
 * Material works as a group that consists of a material name from the material
 * library, and then a listing of the number of faces that the material belongs
 * to.
 * <p>
 *
 * The data represented is
 * <pre>
 *     struct {
 *        short string material_name
 *        int numFaces
 *        int[] faceNumbers
 *     }
 * </pre>
 *
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class MaterialData
{
    /** The name of the material to use from the material library */
    public String materialName;

    /** The number of faces this material node effects */
    public int numFaces;

    /** A listing of each face index this material belongs to */
    public int[] faceList;
}
