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
 * Marker interface for parts of the scene graph that can exist in a collection
 * but are terminal - They have children but the children are non-structural
 * <p/>
 * Long definition
 *
 * @author Justin
 * @version $Revision$
 */
public interface Leaf extends SceneGraphStructureObject
{

}

