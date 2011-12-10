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
import j3d.filter.SceneGraphObject;

// Local Imports
// None

/**
 * Abstract representation of the leaf node in a scene graph that holds 
 * vertex mesh information. 
 * <p/>
 * 
 * A single mesh may be composed of zero or more geometry definitions, 
 * while containing a single appearance.
 * 
 * @author Justin
 * @version $Revision$
 */
public interface Mesh
    extends SceneGraphObject
{
    public void addGeometry(Geometry geom);
    
    public Geometry[] getGeometry();
}
