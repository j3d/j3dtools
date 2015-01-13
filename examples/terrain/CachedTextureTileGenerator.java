/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.j3d.*;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ImageLayout;
import java.util.HashMap;

// Local imports
import org.j3d.terrain.TextureTileGenerator;

/**
 * An example TextureTileGenerator.
 * Caches textures so we don't regenerate them, but never decreases memory usage.
 *
 * @author Alan Hudson
 */
public class CachedTextureTileGenerator implements TextureTileGenerator {
    /** The source image */
    private PlanarImage source;

    /** A simple cache */
    private Texture tCache[][];

    /**
     * Construct a TileGenerator for the specified name.
     *
     * @param filename The texture to tile
     */
    public CachedTextureTileGenerator(String filename)
    {
        source = JAI.create("fileload", filename);

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(source);
        pb.add(null).add(null).add(null).add(null).add(null);

        RenderableImage ren = JAI.createRenderable("renderable", pb);
        RenderedImage image = ren.createDefaultRendering();

        /* Create a texture cache of 8x8 tiles.  2K image / 256 bytes */
        tCache = new Texture[8][8];
    }

    /**
     * Get the size of each texture tile.
     *
     * @return The dimensions of the tile
     */
    public Dimension getTextureSize()
    {
        return new Dimension(256,256);
    }

    /**
     * Get the texture tile for bounded region.
     *
     * @param bounds The region
     */
    public Texture getTextureTile(Rectangle bounds)
    {
        int x = bounds.x / 256;
        int y = bounds.y / 256;

        if (tCache[x][y] != null) {
            return (tCache[x][y]);
        }

        Rectangle rect = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);

        BufferedImage bi = source.getAsBufferedImage(rect, null);

        int format = ImageComponent2D.FORMAT_RGB;

        ImageComponent2D img =
            new ImageComponent2D(format, bi, true, false);

        Texture texture = new Texture2D(Texture.BASE_LEVEL,
                                        Texture.RGB,
                                        img.getWidth(),
                                        img.getHeight());
        texture.setImage(0, img);

        tCache[x][y] = texture;

        return texture;
    }
}
