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
import java.lang.ref.WeakReference;
import java.net.URL;
import java.io.IOException;
import java.util.HashMap;

import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.ImageComponent3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.Texture3D;

// Application specific imports
import org.j3d.util.ImageUtils;

/**
 * A cache for texture instance management where the objects stay according
 * to Java's WeakReference system.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class WeakRefTextureCache extends AbstractTextureCache
{
    private HashMap textureMap;
    private HashMap componentMap;

    /**
     * Construct a new instance of the empty cache.
     */
    WeakRefTextureCache()
    {
        textureMap = new HashMap();
        componentMap = new HashMap();
    }

    /**
     * Fetch the texture named by the filename. The filename may be
     * either absolute or relative to the classpath.
     *
     * @param filename The filename to fetch
     * @return The texture instance for that filename
     * @throws IOException An I/O error occurred during loading
     */
    public Texture fetchTexture(String filename)
        throws IOException
    {
        Texture texture = getTexture(filename);

        if(texture == null)
        {
            ImageComponent img = getImageComponent(filename);

            if(img == null)
            {
                img = load2DImage(filename);
                componentMap.put(filename, img);
            }

            int format = getTextureFormat(img);

            if(img instanceof ImageComponent2D)
            {
                texture = new Texture2D(Texture.BASE_LEVEL,
                                        format,
                                        img.getWidth(),
                                        img.getHeight());
            }
            else
            {
                texture = new Texture3D(Texture.BASE_LEVEL,
                                        format,
                                        img.getWidth(),
                                        img.getHeight(),
                                        ((ImageComponent3D)img).getDepth());
            }

            texture.setImage(0, img);

            textureMap.put(filename, texture);
        }

        return texture;
    }

    /**
     * Fetch the texture named by the URL.
     *
     * @param url The URL to read data from
     * @return The texture instance for that URL
     * @throws IOException An I/O error occurred during loading
     */
    public Texture fetchTexture(URL url)
        throws IOException
    {
        String file_path = url.toExternalForm();

        Texture texture = getTexture(file_path);

        if(texture == null)
        {
            ImageComponent img = getImageComponent(file_path);

            if(img == null)
            {
                img = load2DImage(file_path);
                componentMap.put(file_path, img);
            }

            int format = getTextureFormat(img);

            if(img instanceof ImageComponent2D)
            {
                texture = new Texture2D(Texture.BASE_LEVEL,
                                        format,
                                        img.getWidth(),
                                        img.getHeight());
            }
            else
            {
                texture = new Texture3D(Texture.BASE_LEVEL,
                                        format,
                                        img.getWidth(),
                                        img.getHeight(),
                                        ((ImageComponent3D)img).getDepth());
            }

            texture.setImage(0, img);

            textureMap.put(file_path, texture);
        }

        return texture;
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
        throws IOException
    {
        ImageComponent ret_val = getImageComponent(filename);

        if(ret_val == null)
        {
            ret_val = load2DImage(filename);
            componentMap.put(filename, ret_val);
        }

        return ret_val;
    }

    /**
     * Fetch the image component named by the URL.
     *
     * @param url The URL to read data from
     * @return The ImageComponent instance for that URL
     * @throws IOException An I/O error occurred during loading
     */
    public ImageComponent fetchImageComponent(URL url)
        throws IOException
    {
        String file_path = url.toExternalForm();
        ImageComponent ret_val = getImageComponent(file_path);

        if(ret_val == null)
        {
            ret_val = load2DImage(file_path);
            componentMap.put(file_path, ret_val);
        }

        return ret_val;
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
        textureMap.remove(filename);
        componentMap.remove(filename);
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
        String file_path = url.toExternalForm();
        textureMap.remove(file_path);
        componentMap.remove(file_path);
    }

    /**
     * Clear the entire cache now. It will be empty after this call, forcing
     * all fetch requests to reload the data from the source. Use with
     * caution.
     */
    public void clearAll()
    {
        textureMap.clear();
        componentMap.clear();
    }

    //------------------------------------------------------------------------
    // Local methods
    //------------------------------------------------------------------------

    /**
     * Fetch a texture from the maps. Perform checks to make sure it is still
     * a valid reference and clears the map entry if it is not.
     *
     * @param name The name of the texture to get (the map key)
     * @return The Texture instance, if still valid, else null
     */
    private Texture getTexture(String name)
    {
        WeakReference ref = (WeakReference)textureMap.get(name);

        if(ref == null)
            return null;

        Texture ret_val = (Texture)ref.get();
        if(ret_val == null)
            textureMap.remove(name);

        return ret_val;
    }

    /**
     * Fetch an image component from the maps. Perform checks to make sure it
     * is still a valid reference and clears the map entry if it is not.
     *
     * @param name The name of the component to get (the map key)
     * @return The ImageComponent instance, if still valid, else null
     */
    private ImageComponent getImageComponent(String name)
    {
        WeakReference ref = (WeakReference)componentMap.get(name);

        if(ref == null)
            return null;

        ImageComponent ret_val = (ImageComponent)ref.get();
        if(ret_val == null)
            componentMap.remove(name);

        return ret_val;
    }
}
