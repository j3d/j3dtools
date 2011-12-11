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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import j3d.filter.SceneGraphObjectType;
import j3d.filter.graph.AbstractSceneGraphObject;
import j3d.filter.graph.Group;
import j3d.filter.graph.SceneGraphStructureObject;

// External imports
// None

// Local Imports
// None

/**
 * Class summary comment
 * <p/>
 * Long definition
 *
 * @author Justin
 * @version $Revision$
 */
class MemGroup extends AbstractMemoryGroup
    implements Group
{
    /** The list of children, in order for this group object */
    private List<SceneGraphStructureObject> children;
    
    /**
     * Construct a new instance of the group object 
     * 
     * @param id The internal ID of the object that has been created. 
     */
    MemGroup(int id)
    {
        super(id, SceneGraphObjectType.GROUP);
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
}

