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

// External imports
import java.io.IOException;
import java.net.URL;

// Local imports
// None

/**
 * A representation of global cache for texture instance management.
 * <p>
 *
 * This is the abstract representation of the cache functionality, which may be
 * used across different renderer types. Look in the renderer-specific packages
 * for details on the specific renderer API calls.
 * <p>
 *
 * Different types of cache implementations are allowed (ie different ways of
 * deciding when an texture no longer needs to be in the cache).
 * <p>
 *
 * Internal storage and key management is using strings. The URLs are converted
 * to string form as the key and used to look up items. The filenames are always
 * relative to the classpath. If the filename/url has been loaded as an image
 * component before and then a request is made for a texture, then the previously
 * loaded component is used as the basis for the texture.
 * <p>
 *
 * All fetch methods work in the same way - if the texture has not been
 * previously loaded, then it will be loaded and converted to a BufferedImage
 * using the utilities of this class.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface TextureCache
{
    /**
     * Explicitly remove the named texture and image component from the cache.
     * If the objects have already been freed according to the rules of the
     * cache system, this request is silently ignored.
     *
     * @param filename The name the texture was registered under
     */
    public void releaseTexture(String filename);

    /**
     * Explicitly remove the named texture and image component from the cache.
     * If the objects have already been freed according to the rules of the
     * cache system, this request is silently ignored.
     *
     * @param url The URL the texture was registered under
     */
    public void releaseTexture(URL url);

    /**
     * Clear the entire cache now. It will be empty after this call, forcing
     * all fetch requests to reload the data from the source. Use with
     * caution.
     */
    public void clearAll();

    /**
     * Check to see if a filename is cached for a Texture.
     *
     * @param filename The filename loaded
     * @return Whether the filename is cached as a Texture
     */
    public boolean checkTexture(String filename);
}
