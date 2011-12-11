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
import j3d.filter.SceneGraphObjectType;
import j3d.filter.graph.LineGeometry;

/**
 * In memory implementation of a line array of geometry.
 * <p/>
 * Long definition
 *
 * @author Justin Couch
 * @version $Revision$
 */
class MemLineGeometry extends AbstractVertexGeometry
    implements LineGeometry
{

    /**
     * @param id
     */
    MemLineGeometry(int id)
    {
        super(id, SceneGraphObjectType.LINES);
    }

    //------------------------------------------------------------------------
    // Methods defined by LineGeometry
    //------------------------------------------------------------------------

    @Override
    public int getLineCount()
    {
        return vertices.size() / 2;
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
}
