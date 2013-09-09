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
 * Provides a mesh with different types of visual material information
 * <p/>
 * Long definition
 *
 * @author Justin
 * @version $Revision$
 */
public interface VisualProperties
    extends SceneGraphDecoratorObject
{

    public void setBaseColours();
    
    public void addTexture();
}
