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
// None

// Local Imports
// None

/**
 * Represents the physical properties of a leaf node that represents the 
 * renderable output.
 * <p/>
 * This information can be used for many properties that describe an object
 * for non-colour purposes, but may also be used for rendering as well. For
 * example, by describing the physical property as brushed steel, the renderer
 * may be able to take that in to account and apply an appropriate shader.
 *
 * @author Justin
 * @version $Revision$
 */
public interface PhysicalProperties
    extends SceneGraphDecoratorObject
{

}
