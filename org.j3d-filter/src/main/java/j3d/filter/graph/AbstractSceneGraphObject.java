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

package j3d.filter.graph;

// External imports
// None

// Local Imports
import j3d.filter.SceneGraphObject;
import j3d.filter.SceneGraphObjectType;


/**
 * Common base type that can be used for any scenegraph object. 
 * <p/>
 * Provides the
 * raw necessities for a scene graph object class representation.
 *
 * @author Justin Couch
 * @version $Revision$
 */
public abstract class AbstractSceneGraphObject implements SceneGraphObject
{
    /** The unique instance id for this scene object */
    private final int objectId;
    
    /** The scene graph object type */
    private final SceneGraphObjectType objectType;
    
    /**
     * Construct a new abstract type using the provided setup information
     * 
     * @param id The unique ID to associate with this instance
     * @param type The object type that this represents
     */
    protected AbstractSceneGraphObject(int id, SceneGraphObjectType type)
    {
        objectId = id;
        objectType = type;
    }

    //------------------------------------------------------------------------
    // Methods defined by SceneGraphObject
    //------------------------------------------------------------------------

    @Override
    public int getID()
    {
        return objectId;
    }

    @Override
    public SceneGraphObjectType getObjectType()
    {
        return objectType;
    }

    //------------------------------------------------------------------------
    // Methods defined by Object
    //------------------------------------------------------------------------

    @Override
    public int hashCode()
    {
        return objectId;
    }
    
    @Override
    public boolean equals(Object o)
    {
        return o != null && o instanceof SceneGraphObject && 
               ((SceneGraphObject)o).getID() == objectId;
    }
}
