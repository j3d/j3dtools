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
 * Enumerated type for the different scene graph object types that can be found
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 * @see SceneGraphObject
 */
public enum SceneGraphObjectType
{
    GROUP, 
    TRANSFORM, 
    MESH, 
    VOLUME,
    LIGHT,
    VISUAL_PROPERTIES, 
    PHYSICAL_PROPERTIES, 
    BASE_COLOR, 
    TEXTURE, 
    METADATA, 
    EXTERNAL_REFERENCE, 
    LINES,
    TRIANGLES,
    QUADS
}
