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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// Application specific imports
// none

/**
 * Generator for supplying instances of Java3D {@link Texture} texture objects
 * as tiles for large-scale terrains.
 * <p>
 *
 * The implementation of this interface is free to source the texture tiles
 * however it wishes. The tiles may be sourced from one huge image (for
 * example, using JAI) or it may be from multiple image files, stitched
 * together on demand. So long as the generator is able to tell the caller the
 * total size of the texture(s) in pixels, then the generator will get the
 * correct dimensions for each tile.
 * <p>
 *
 * Note that ideally the image size should be a power of two or exactly
 * divisible by the number of grid cells in the terrain data to give power of
 * two textures. The caller does not make any checks on the texture object
 * returned or on the bounds values passed in on the request. It is up to the
 * implementor to check and rescale the tile image to be the appropriate size
 * before handing the tile back to the caller.
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public interface TextureTileGenerator
{
    /**
     * Get the total size of the underlying image in pixels. This should
     * provide valid values (positive, non-zero) all the time as this is used
     * as part of a conversion process so that correct image coordinates are
     * passed when requested.
     */
    public Dimension getTextureSize();

    /**
     * Fetch the texture tile for the given bounds, expresed in image pixel
     * coordinates. The bounds are always guaranteed to be valid values and
     * within the bounds expressed by the {@link #getTextureSize()} method.
     *
     * @param bounds The bounds of the texture tile to supply
     * @return The image to use for the texture corresponding to that bounds
     */
    public BufferedImage getTextureTile(Rectangle bounds);
}
