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

package org.j3d.renderer.java3d.texture;

// Standard imports
import java.io.IOException;
import java.net.URL;

import javax.media.j3d.ImageComponent;
import javax.media.j3d.Texture;

// Application specific imports
import org.j3d.texture.TextureCache;

/**
 * A representation of global cache for texture instance management.
 * <p>
 *
 * The cache works at the Java3D Texture or ImageComponent instance level
 * rather than down at the individual images. This allows the VM to discard
 * the lower level image instances if needed, allowing Java3D to do its own
 * management. In addition, it benefits runtime performance of the Java3D
 * scene graph by allowing textures instances to be shared, rather than
 * duplicated.
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
 * @version $Revision: 1.1 $
 */
public interface J3DTextureCache extends TextureCache
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
        throws IOException;

    /**
     * Fetch the texture named by the URL.
     *
     * @param url The URL to read data from
     * @return The texture instance for that URL
     * @throws IOException An I/O error occurred during loading
     */
    public Texture fetchTexture(URL url)
        throws IOException;

    /**
     * Param fetch the imagecomponent named by the filename. The filename may
     * be either absolute or relative to the classpath.
     *
     * @param filename The filename to fetch
     * @return The ImageComponent instance for that filename
     * @throws IOException An I/O error occurred during loading
     */
    public ImageComponent fetchImageComponent(String filename)
        throws IOException;

    /**
     * Fetch the image component named by the URL.
     *
     * @param url The URL to read data from
     * @return The ImageComponent instance for that URL
     * @throws IOException An I/O error occurred during loading
     */
    public ImageComponent fetchImageComponent(URL url)
        throws IOException;

    /**
     * Check to see if a filename is cached for an ImageComponent.
     *
     * @param filename The filename loaded
     * @return Whether the filename is cached as an ImageComponent
     */
    public boolean checkImageComponent(String filename);

    /**
     * Register a texture with the cache assigned to a filename.
     *
     * @param texture The texture to store
     * @param filename The filename to register
     */
    public void registerTexture(Texture texture, String filename);

    /**
     * Register an imagecomponent with the cache assigned to a filename.
     *
     * @param texture The texture to store
     * @param filename The filename to register
     */
    public void registerImageComponent(ImageComponent component, String filename);

}
