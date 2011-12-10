/*****************************************************************************
 *                        j3d.org Copyright (c) 2004
 *                                   Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.filter;

// External imports
import java.util.Set;

// Local imports
// None

/**
 * Abstract representation of the database that contains the scene graph objects.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface GeometryDatabase
{
    /**
     * Get the object by ID. 
     * 
     * @param id The ID of the object to fetch
     * @return The corresponding object, or null if there isn't a match
     */
    public SceneGraphObject getObject(int id);

    /**
     * Get an object by a custom name that has been associated with it. If no
     * object exists with the matching name, returns null.
     * 
     * @param name The name that is used to reference the object
     * @return The corresponding object, or null if no match
     */
    public SceneGraphObject getObject(String name);
    
    /**
     * Name the given object. The name and object must always be non-null.
     * 
     * @param name The name to associate with the object
     * @param obj The object that is to be named
     * @param overwrite true if this should replace an object if it exists 
     *   with the same name. False if not.
     * @return true if the named object was set (see overwrite parameter)
     * @throws IllegalArgumentException one of the parameters are null
     */
    public boolean nameObject(String name, SceneGraphObject obj, boolean overwrite)
        throws IllegalArgumentException;
    
    /**
     * The referenced object has been changed in some way, update the database 
     * with the changes.If the reference is null, ignore the request.
     * 
     * @param obj The object reference to write the changes
     */
    public void updateObject(SceneGraphObject obj);

    /**
     * Remove the given object from the database as it is no longer needed.
     * If the reference is null, ignore the request.
     * 
     * @param obj The object reference to delete
     */
    public void removeObject(SceneGraphObject obj);

    /**
     * Create a new object of the given type. If the type is null, return 
     * a null object.
     * 
     * @param t The type to create
     * @return A matching empty object instance of the given type
     */
    public SceneGraphObject createObject(SceneGraphObjectType t);
    
    /**
     * Get the total number of root objects in the scene.
     * 
     * @return a number greater than zero
     */
    public int getRootObjectCount();
    
    /**
     * Get the root object at the given index. If the index is out of range
     * returns null.
     * 
     * @param index The index to get the root object for
     * @return The corresponding object, or null if not found
     */
    public SceneGraphObject getRootObject(int index);
    
    /**
     * Get all objects of the given type in the scene
     * 
     * @param type The type of object to get from the scene
     * @return A set of the objects in declaration order in the file. If the source file
     *    requests that everything remain in-order, this will be a sorted set
     */
    public Set<SceneGraphObject> getObjectsOfType(SceneGraphObjectType type);
    
    /**
     * Queue the given URL or part URL string to be loaded as part of the 
     * processing. The URL is an array of to be loaded in preference order.
     * Each will be tried in turn until one is successfully loaded. Once it
     * has been loaded, no further URLs are processed in this array.
     * 
     * @param uri The URL, which may or may not be fully qualified. The array
     *    presents the load order
     */
    public void queueURIToLoad(String[] uri);
}
