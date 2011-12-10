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
 * Marker of a scene graph that represents a decorator for the scene. 
 * <p/>
 * Decorators do not provide core rendering capabilities, but add 
 * additional metadata that allows for rendering to occur, or non-
 * renderable information, such as user-provided meta data.
 *
 * @author Justin
 * @version $Revision$
 */
public interface SceneGraphDecoratorObject
    extends SceneGraphObject
{

}
