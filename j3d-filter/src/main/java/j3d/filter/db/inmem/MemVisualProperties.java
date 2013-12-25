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
import j3d.filter.graph.AbstractSceneGraphObject;
import j3d.filter.SceneGraphObjectType;
import j3d.filter.graph.VisualProperties;

/**
 * In memory implementation of the visual properties.
 * <p/>
 * Long definition
 *
 * @author Justin Couch
 * @version $Revision$
 */
class MemVisualProperties extends AbstractSceneGraphObject
    implements VisualProperties
{
    MemVisualProperties(int id)
    {
        super(id, SceneGraphObjectType.VISUAL_PROPERTIES);
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

