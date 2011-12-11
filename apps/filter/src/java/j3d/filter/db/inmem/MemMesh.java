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
import java.util.ArrayList;
import java.util.List;

import j3d.filter.SceneGraphObjectType;
import j3d.filter.graph.*;

/**
 * In memory implementation of the mesh object type
 * <p/>
 * 
 *
 * @author Justin Couch
 * @version $Revision$
 */
class MemMesh extends AbstractSceneGraphObject
    implements Mesh
{
    /** Currently set visual properties. May be null */
    private VisualProperties visualProps;

    /** Currently set physical properties. May be null */
    private PhysicalProperties physicalProps;

    /** The array of geometry that has been added to this system */
    private List<VertexGeometry> geometry;
    
    /**
     * Construct an instance of the mesh
     * 
     * @param id Unique ID that needs to be assigned to this object
     */
    MemMesh(int id)
    {
        super(id, SceneGraphObjectType.MESH);

        geometry = new ArrayList<VertexGeometry>();
    }

    //------------------------------------------------------------------------
    // Methods defined by Mesh
    //------------------------------------------------------------------------

    @Override
    public void addGeometry(VertexGeometry geom)
    {
        if(geom != null && !geometry.contains(geom))
        geometry.add(geom);
    }

    @Override
    public VertexGeometry[] getGeometry()
    {
        VertexGeometry[] ret_val = null;
        
        if(!geometry.isEmpty())
        {
            ret_val = new VertexGeometry[geometry.size()];
            geometry.toArray(ret_val);
        }
        
        return ret_val;
    }

    @Override
    public void setVisualProperties(VisualProperties props)
    {
        visualProps = props;
    }

    @Override
    public VisualProperties getVisualProperties()
    {
        return visualProps;
    }

    @Override
    public void setPhysicalProperties(PhysicalProperties props)
    {
        physicalProps = props;
    }

    @Override
    public PhysicalProperties getPhysicalProperties()
    {
        return physicalProps;
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

}
