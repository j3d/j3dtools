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

package j3d.filter.db.inmem;

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
    /** The internal object ID counter */
    private int nodeCounter;
    
    /** 
     * Construct a default instance of this class.
     */
    InMemoryNodeFactory()
    {
        nodeCounter = 1;
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
            case VISUAL_PROPERTIES:
                ret_val = new MemVisualProperties(nodeCounter++);
                break;

            case PHYSICAL_PROPERTIES:
                ret_val = new MemPhysicalProperties(nodeCounter++);
                break;
                
            case EXTERNAL_REFERENCE:
                break;
                
            case LINES:
                ret_val = new MemLineGeometry(nodeCounter++);
                break;
                
            case TRIANGLES:
                ret_val = new MemTriangleGeometry(nodeCounter++);
                break;
                
            case QUADS:
                ret_val = new MemQuadGeometry(nodeCounter++);
                break;
                
            case GROUP:
                ret_val = new MemGroup(nodeCounter++);
                break;
                
            case BASE_COLOR:
                break;
                
            case METADATA:
                break;
                
            case MESH:
                ret_val = new MemMesh(nodeCounter++);
                break;
                
            case TEXTURE:
                break;
                
            case VOLUME:
                break;
                
            case LIGHT:
                break;
                
            case TRANSFORM:
                ret_val = new MemTransformGroup(nodeCounter++);
                break;
        }
        
        return ret_val;
    }
}
