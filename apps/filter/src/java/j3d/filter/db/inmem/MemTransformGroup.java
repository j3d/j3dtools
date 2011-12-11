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
import java.util.List;

// Local Imports
import j3d.filter.SceneGraphObjectType;
import j3d.filter.graph.SceneGraphStructureObject;
import j3d.filter.graph.TransformGroup;

/**
 * Class summary comment
 * <p/>
 * Long definition
 *
 * @author Justin
 * @version $Revision$
 */
class MemTransformGroup extends AbstractMemoryGroup
    implements TransformGroup
{

    /** The matrix used by this transform */
    private double[] transformMatrix;
    
    /**
     * Construct a new instance of this group type
     * 
     * @param id The internal ID of the object that has been created. 
     */
    MemTransformGroup(int id)
    {
        super(id, SceneGraphObjectType.TRANSFORM);
        
        transformMatrix = new double[16];
        transformMatrix[0] = 1;
        transformMatrix[5] = 1;
        transformMatrix[10] = 1;
        transformMatrix[15] = 1;
    }
    
    //------------------------------------------------------------------------
    // Methods defined by TransformGroup
    //------------------------------------------------------------------------

    @Override
    public void getTransformationMatrix(double[] mat)
    {
        assert mat != null && mat.length > 16 : "output matrix not long enough";
        System.arraycopy(transformMatrix, 0, mat, 0, 15);
    }

    @Override
    public void setTransformationMatrix(double[] mat)
    {
        assert mat != null && mat.length > 16 : "output matrix not long enough";

        System.arraycopy(mat, 0, transformMatrix, 0, 15);
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
}

