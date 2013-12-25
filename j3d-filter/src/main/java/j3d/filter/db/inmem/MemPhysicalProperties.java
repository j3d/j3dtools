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

import j3d.filter.SceneGraphObjectType;
import j3d.filter.graph.AbstractSceneGraphObject;
import j3d.filter.graph.VisualProperties;

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
public class MemPhysicalProperties extends AbstractSceneGraphObject
    implements VisualProperties
{

    /**
     * @param id
     */
    public MemPhysicalProperties(int id)
    {
        super(id, SceneGraphObjectType.PHYSICAL_PROPERTIES);
    }

    //------------------------------------------------------------------------
    // Methods defined by GeometryDatabase
    //------------------------------------------------------------------------
    
    @Override
    public void setBaseColours()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addTexture()
    {
        // TODO Auto-generated method stub

    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
}



