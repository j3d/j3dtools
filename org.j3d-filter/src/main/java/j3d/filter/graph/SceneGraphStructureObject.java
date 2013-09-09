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
 * Marker of a scene graph that represents the core structure of the graph,
 * rather than a decorator
 * <p/>
 * Structural objects contain information that is useful for grouping and 
 * placing renderable objects in the scene. They may group lower level objects,
 * or organise different ways of rendering them (eg Billboard/LOD), but the
 * effectively result in a rendered object either directly or through their
 * children.
 *
 * @author Justin
 * @version $Revision$
 */
public interface SceneGraphStructureObject
    extends SceneGraphObject
{

}
