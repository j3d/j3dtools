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

package org.j3d.texture;

// Standard imports
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.net.URL;

import javax.media.j3d.ImageComponent;
import javax.media.j3d.Texture;

// Application specific imports
import org.j3d.util.ImageUtils;

/**
 * A cache for texture instance management where the objects stay according
 * to a Least-Recently-Used algorithm.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class LRUTextureCache implements TextureCache
{
    /**
     * Fetch the texture named by the filename. The filename may be
     * either absolute or relative to the classpath.
     *
     * @param filename The filename to fetch
     * @return The texture instance for that filename
     * @throws IOException An I/O error occurred during loading
     */
    public Texture fetchTexture(String filename)
    {
        return null;
    }

    /**
     * Fetch the texture named by the URL.
     *
     * @param url The URL to read data from
     * @return The texture instance for that URL
     * @throws IOException An I/O error occurred during loading
     */
    public Texture fetchTexture(URL url)
    {
        return null;
    }

    /**
     * Param fetch the imagecomponent named by the filename. The filename may
     * be either absolute or relative to the classpath.
     *
     * @param filename The filename to fetch
     * @return The ImageComponent instance for that filename
     * @throws IOException An I/O error occurred during loading
     */
    public ImageComponent fetchImageComponent(String filename)
    {
        return null;
    }

    /**
     * Fetch the image component named by the URL.
     *
     * @param url The URL to read data from
     * @return The ImageComponent instance for that URL
     * @throws IOException An I/O error occurred during loading
     */
    public ImageComponent fetchImageComponent(URL url)
    {
        return null;
    }

    /**
     * Explicitly remove the named texture and image component from the cache.
     * If the objects have already been freed according to the rules of the
     * cache system, this request is silently ignored.
     *
     * @param filename The name the texture was registered under
     */
    public void releaseTexture(String filename)
    {
    }

    /**
     * Explicitly remove the named texture and image component from the cache.
     * If the objects have already been freed according to the rules of the
     * cache system, this request is silently ignored.
     *
     * @param url The URL the texture was registered under
     */
    public void releaseTexture(URL url)
    {
    }

    /**
     * Clear the entire cache now. It will be empty after this call, forcing
     * all fetch requests to reload the data from the source. Use with
     * caution.
     */
    public void clearAll()
    {
    }
}
