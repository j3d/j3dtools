/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.terrain;

// Standard imports
import javax.media.j3d.Appearance;

// Application specific imports
// none

/**
 * Generator of Java3D {@link javax.media.j3d.Appearance} instances for use
 * within the terrain rendering.
 * <p>
 *
 * The generator is used to create instances of the appearance object on
 * demand for the system internal scene graph representation. The generator
 * is required to act in concert with the {@link TerrainData} implementation
 * as the use of this class by the specific terrain rendering algorithms are
 * dependent on the way the data is being handled.
 * <p>
 *
 * Every time the method of this interface is called the implementation is
 * <i>required</i> to generate a new instance of the <code>Appearance</code>
 * object. The reason for this is to allow the internal representation the
 * flexibility to handle either single texture per terrain data or tiled
 * textures. In the case of tiled textures, we need to use the same basic
 * appearance set up for each tile, but a separate
 * {@link javax.media.j3d.Texture} instance is used for each tile. To do that
 * requires a separate <code>appearance</code> instance for each terrain tile.
 * If the <code>TerrainData</code> tells us that there is only a single texture
 * for the entire system, then the rendering code only needs to create one
 * <code>Appearance</code> instance and shares that with all the geometry.
 * <p>
 *
 * Although you are required to generate new instances of the
 * <code>Appearance</code> object, there is no such requirements on the helper
 * classes it uses. For example, you may choose to share instances of
 * {@link javax.media.j3d.Material} or {@link javax.media.j3d.PolygonAttributes}
 * (to change from line to polygon mode for example). This is definitely
 * encouraged because sharing these instances allows Java3D to do a lot of
 * rendering optimisations on top of what you currently have now.
 * <p>
 *
 * Currently this object does not reuse instances. That may be something worth
 * adding in the future.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public interface AppearanceGenerator
{
    /**
     * Create a new appearance instance. The instance must be new each time.
     * The returned object should be configured to whatever capabilities you
     * wish, however, it must not be part of a live scene graph, and the
     * texture object must not yet be set.
     *
     * @return The new appearance instance to use
     */
    public Appearance createAppearance();
}
