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
import j3d.filter.graph.Vertex;
import j3d.filter.graph.VertexGeometry;

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
public abstract class AbstractVertexGeometry extends AbstractSceneGraphObject
    implements VertexGeometry
{

    /** The list of vertices in this geometry */
    protected List<Vertex> vertices;
    
    /**
     * Construct a set of geometry for the given type.
     * 
     * @param id The object ID to assign
     * @param type The type of geometry this is
     */
    public AbstractVertexGeometry(int id, SceneGraphObjectType type)
    {
        super(id, type);
        
        vertices = new ArrayList<>();
    }

    //------------------------------------------------------------------------
    // Methods defined by VertexGeometry
    //------------------------------------------------------------------------

    @Override
    public void addVertex(Vertex vtx)
    {
        if(vtx != null)
            vertices.add(vtx);
    }

    @Override
    public void removeVertex(int idx)
    {
        if(idx > 0 && idx < vertices.size())
            vertices.remove(idx);
    }

    @Override
    public List<Vertex> getVertices()
    {
        return Collections.unmodifiableList(vertices);
    }

    @Override
    public Vertex getVertex(int idx)
    {
        Vertex ret_val = null;
        
        if(idx > 0 && idx < vertices.size())
            ret_val = vertices.get(idx);
        
        return ret_val;
    }

    @Override
    public int getVertexCount()
    {
        return vertices.size();
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
}

