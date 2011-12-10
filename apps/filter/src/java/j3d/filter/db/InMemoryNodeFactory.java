/*****************************************************************************
 *                  j3d.org Copyright (c) 2000 - 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.filter.db;

// External imports
// None

// Local Imports
import j3d.filter.SceneGraphObject;
import j3d.filter.SceneGraphObjectType;

/**
 * Internal factory to create new instanes of nodes for the in-memory DB based
 * on the incoming enum
 * <p/>
 * 
 *
 * @author Justin
 * @version $Revision$
 */
class InMemoryNodeFactory
{

    /** 
     * Construct a default instance of this class.
     */
    InMemoryNodeFactory()
    {
        
    }
    
    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

    /**
     * Create a new instance of a node that corresponds to the given enumerated
     * type. If it does not have a mapping, return null.
     * 
     * @param type The type to generate a mapping for
     * @return A new instance of the required class, or null if no mapping
     */
    SceneGraphObject createNewNode(SceneGraphObjectType type)
    {
        SceneGraphObject ret_val = null;
        
        switch(type)
        {
            case APPEARANCE:
                break;
                
            case EXTERNAL_REFERENCE:
                break;
                
            case GEOMETRY:
                break;
                
            case GROUP:
                break;
                
            case MATERIAL:
                break;
                
            case METADATA:
                break;
                
            case SHAPE:
                break;
                
            case TEXTURE:
                break;
                
            case TRANSFORM:
                break;
        }
        
        return ret_val;
    }
}
