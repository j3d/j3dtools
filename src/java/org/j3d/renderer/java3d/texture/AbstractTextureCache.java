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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageProducer;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Texture;

import java.awt.image.DataBuffer;

// Application specific imports
import org.j3d.util.ImageUtils;

/**
 * An abstract implementation of the cache with a collection of useful
 * utility methods for any cache implementation.
 * <p>
 *
 * This class does not provide the storage structures for caching as each
 * implementation will have different requirements. It just provides utility
 * methods that most implementations will find useful.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public abstract class AbstractTextureCache implements J3DTextureCache
{
    /** The list of class types we prefer back from the content handler. */
    private static final Class[] CLASS_TYPES =
    {
        ImageProducer.class,
        BufferedImage.class,
        Image.class
    };

    /** Texture utilities class to help do the boring stuff */
    protected TextureCreateUtils texUtils;

    /**
     * Construct a new instance of the empty cache. Empty implementation,
     * does nothing.
     */
    protected AbstractTextureCache()
    {
        texUtils = new TextureCreateUtils();
    }

    //------------------------------------------------------------------------
    // Local methods
    //------------------------------------------------------------------------

    /**
     * Load the image component from the given filename. All images are
     * loaded by-reference. This does not automatically register the component
     * with the internal datastructures. That is the responsibility of the
     * caller.
     *
     * @param filename The name of the file to be loaded
     * @return An ImageComponent instance with byRef true and yUp false
     * @throws IOException Some error reading the file
     */
    protected ImageComponent2D load2DImage(String filename)
        throws IOException
    {
        // first try to locate the image as a fully qualified filename.
        File file = new File(filename);
        URL url;

        if(file.exists())
        {
            url = file.toURL();
        }
        else
        {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            url = cl.getResource(filename);

            if(url == null)
                throw new FileNotFoundException("Couldn't find " + filename);
        }

        return load2DImage(url);
    }

    /**
     * Load the image component from the given url. All images are
     * loaded by-reference. This does not automatically register the component
     * with the internal datastructures. That is the responsibility of the
     * caller.
     *
     * @param url The URL of the file to be loaded
     * @return An ImageComponent instance with byRef true and yUp false
     * @throws IOException Some error reading the URL
     */
    protected ImageComponent2D load2DImage(URL url)
        throws IOException
    {
        Object content = url.getContent(CLASS_TYPES);

        if(content == null)
            throw new FileNotFoundException("No content for " + url);

        return texUtils.create2DImageComponent(content);
    }
}
