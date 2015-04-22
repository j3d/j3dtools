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

// External imports
import java.awt.Image;
import java.awt.Toolkit;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

// Local imports
// none

/**
 * A convenience class that loads Icons for users and provides caching
 * mechanisms.
 * <p>
 *
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>urlFormatErrorMsg: The filename they give us is treated as a URL
 *     but is badly formatted.</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class ImageLoader
{
    /** Resource for the file not found error message */
    private static final String BAD_URL_MSG_PROP =
        "org.j3d.util.ImageLoader.urlFormatErrorMsg";

    /** The default size of the map */
    private static final int DEFAULT_SIZE = 10;

    /**
     * A hashmap of the loaded image instances. Weak so that we can discard
     * them if if needed because we're running out of memory.
     */
    private static HashMap<String, WeakReference> loadedImages;

    /**
     * A hashmap of the loaded icon instances. Weak so that we can discard
     * them if if needed because we're running out of memory.
     */
    private static HashMap<String, WeakReference> loadedIcons;

    /** Error reporter for sending out messages */
    private static ErrorReporter errorReporter;

    /**
     * Static initialiser to get all the bits set up as needed.
     */
    static
    {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        loadedImages = new HashMap<>(DEFAULT_SIZE);
        loadedIcons = new HashMap<>(DEFAULT_SIZE);
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public static void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Load an icon for the named image file. Looks in the classpath for the
     * image so the path provided must be fully qualified relative to the
     * classpath. Alternatively a fully qualified URL may be provided to fetch
     * an image from an alternative place other than the classpath.
     *
     * @param name The path to load the icon for. If not found,
     *   no image is loaded.
     * @return An icon for the named path.
     */
    public static Icon loadIcon(String name)
    {
        // Check the map for an instance first
        Icon ret_val = null;

        WeakReference ref = loadedIcons.get(name);
        if(ref != null)
        {
            ret_val = (Icon)ref.get();
            if(ret_val == null)
                loadedIcons.remove(name);
        }

        if(ret_val == null)
        {
            Image img = loadImage(name);

            if(img != null)
            {
                ret_val = new ImageIcon(img, name);
                loadedIcons.put(name, new WeakReference<>(ret_val));
            }
        }

        return ret_val;
    }

    /**
     * Load an image for the named image file. Looks in the classpath for the
     * image so the path provided must be fully qualified relative to the
     * classpath. Alternatively a fully qualified URL may be provided to fetch
     * an image from an alternative place other than the classpath.
     *
     * @param name The path to load the icon for. If not found,
     *   no image is loaded.
     * @return An image for the named path.
     */
    public static Image loadImage(String name)
    {
        // Check the map for an instance first
        Image ret_val = null;

        WeakReference ref = loadedImages.get(name);
        if(ref != null)
        {
            ret_val = (Image)ref.get();
            if(ret_val == null)
                loadedIcons.remove(name);
        }

        if(ret_val == null)
        {
            URL url = findFile(name);

            if(url != null)
            {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                ret_val = toolkit.createImage(url);
                loadedImages.put(name, new WeakReference<>(ret_val));
            }
        }

        return ret_val;
    }

    /**
     * Find the path to the resource name by looking for fully qualified
     * and partially qualified names. Works within JWS too.
     *
     * @param filename The name/path to the file to load
     * @return The URL the file that needs to be loaded
     */
    private static URL findFile(final String filename)
    {
        URL ret_val = null;

        // if URL or URI then just get the object
        if(filename.startsWith("http:") ||
            filename.startsWith("file:"))
        {
            try
            {
                ret_val = new URL(filename);
            }
            catch (MalformedURLException ioe)
            {
                I18nManager intl_mgr = I18nManager.getManager();
                String msg = intl_mgr.getString(BAD_URL_MSG_PROP) + filename;

                errorReporter.errorReport(msg, null);
            }
        }
        else
        {
            // try to retrieve from the classpath
            ret_val = AccessController.doPrivileged(
                new PrivilegedAction<URL>()
                {
                    public URL run()
                    {
                        URL url = ClassLoader.getSystemResource(filename);

                        // WebStart fallback
                        if(url == null)
                        {
                            ClassLoader cl = ImageLoader.class.getClassLoader();
                            url = cl.getResource(filename);
                        }

                        return url;
                    }
                }
            );
        }

        return ret_val;
    }

}
