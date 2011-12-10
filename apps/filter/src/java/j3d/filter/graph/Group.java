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
import java.util.List;

// Local Imports
// None

/**
 * Group represents a node that can have additional children added to it.
 * <p/>
 * Children are other structural objects.
 *
 * @author Justin
 * @version $Revision$
 */
public interface Group
    extends SceneGraphStructureObject, BoundedObject
{
    /**
     * Get the list of children of this object.
     * 
     * @return A non-modifiable list of children
     */
    public List<SceneGraphStructureObject> getChildren();
    
    /**
     * Add a new child to this group object. If the child is added a second
     * time, it will appear in the child list multiple times. Adding a value
     * of null will insert a gap in the child list.
     * 
     * @param child The new child to add
     */
    public void addChild(SceneGraphStructureObject child);
    
    /**
     * Remove the given child reference. If the child is in the list twice, it
     * will only remove all instances of that node from the list. Passing in a
     * null value will remove all blank spaces.
     * 
     * @param child The child instance to remove
     */
    public void removeChild(SceneGraphStructureObject child);
}


