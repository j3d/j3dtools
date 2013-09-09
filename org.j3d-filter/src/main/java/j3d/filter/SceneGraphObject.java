/*******************************************************************************
 *               J3D.org Copyright (c) 2000 - 2011
 *                             Java Source
 *  
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *  
 ******************************************************************************/

package j3d.filter;

// External imports
// None

// Local imports
// None

/**
 * Definition of any scene graph object that can be found in the {@link GeometryDatabase}
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface SceneGraphObject
{
    /**
     * Get the unique ID used for this object. The ID is unique per scene graph instance,
     * and may change between runs of the filter. It is just a way of making sure we can
     * tell objects apart.
     * 
     *  @return A unique ID for this object
     */
    public int getID();
    
    /**
     * Get the type of object that this scene graph base type represents
     * 
     * @return a non-null enumerated type
     */
    public SceneGraphObjectType getObjectType();
}
