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

package org.j3d.util;

// Standard imports
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.net.URL;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

// Application specific imports
// none

/**
 * A convenience class that loads Icons for users and provides caching
 * mechanisms.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ImageLoader
{
    /** The default size of the map */
    private static final int DEFAULT_SIZE = 10;

    /** The image toolkit used to load images with */
    private static Toolkit toolkit;

    /** The classloader used for images */
    private static ClassLoader classLoader;

    /**
     * A hashmap of the loaded image instances. Weak so that we can discard
     * them if if needed because we're running out of memory.
     */
    private static WeakHashMap loadedImages;

    /**
     * A hashmap of the loaded icon instances. Weak so that we can discard
     * them if if needed because we're running out of memory.
     */
    private static WeakHashMap loadedIcons;

    /**
     * Static initialiser to get all the bits set up as needed.
     */
    static
    {
        toolkit = Toolkit.getDefaultToolkit();
        classLoader = ClassLoader.getSystemClassLoader();
        loadedImages = new WeakHashMap(DEFAULT_SIZE);
        loadedIcons = new WeakHashMap(DEFAULT_SIZE);
    }

    /**
     * Load an icon for the named image file. Looks in the classpath for the
     * image so the path provided must be fully qualified relative to the
     * classpath.
     *
     * @param path The path to load the icon for. If not found,
     *   no image is loaded.
     * @return An icon for the named path.
     */
    public static Icon loadIcon(String name)
    {
        // Check the map for an instance first
        Icon ret_val = (Icon)loadedIcons.get(name);

        if(ret_val == null)
        {
            Image img = loadImage(name);

            if(img != null)
            {
                ret_val = new ImageIcon(img, name);
                loadedIcons.put(name, ret_val);
            }
        }

        return ret_val;
    }

    /**
     * Load an image for the named image file. Looks in the classpath for the
     * image so the path provided must be fully qualified relative to the
     * classpath.
     *
     * @param path The path to load the icon for. If not found,
     *   no image is loaded.
     * @return An image for the named path.
     */
    public static Image loadImage(String name)
    {
        // Check the map for an instance first
        Image ret_val = (Image)loadedImages.get(name);

        if(ret_val == null)
        {
            URL url = classLoader.getSystemResource(name);

            if(url != null)
            {
                ret_val = toolkit.createImage(url);
                loadedImages.put(name, ret_val);
            }
        }

        return ret_val;
    }
}
