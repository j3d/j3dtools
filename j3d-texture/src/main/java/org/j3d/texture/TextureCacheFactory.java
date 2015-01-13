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
// None

// Local imports
// None

/**
 * Abstract representation of what a factory for texture caches would look
 * like.
 * <p>
 *
 * Different types of cache implementations are allowed (ie different ways of
 * deciding when an texture no longer needs to be in the cache).
 * <p>
 *
 * The factory also supports the concept of the "default cache". This is used
 * when you want a simple system that doesn't really care about the cache type
 * used and just wants to use this class as a global singleton for storing the
 * texture information. The default cache type can be controlled through either
 * directly setting the value in this class, or using a system property. By
 * defining a value for the property
 * <pre>
 *   org.j3d.texture.DefaultCacheType
 * </pre>
 *
 * with one of the values (case-sensitive) <code>fixed</code>, <code>lru</code>
 * or <code>weakref</code>. Setting the type through the method call will
 * override this setting. However, the cache type can only be set once. All
 * further attempts will result in an exception.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface TextureCacheFactory
{
    /** ID for a fixed contents cache implementation */
    public static final int FIXED_CACHE = 1;

    /** ID for a Least-Recently-Used cache implementation */
    public static final int LRU_CACHE = 2;

    /** ID for a cache implementation using weak references */
    public static final int WEAKREF_CACHE = 3;

    /** The last ID for global, inbuilt cache types */
    public static final int LAST_CACHE_ID = 10;

    /** The system property name */
    public static final String DEFAULT_CACHE_PROP =
        "org.j3d.texture.DefaultCacheType";
}
