/*****************************************************************************
 *                  j3d.org Copyright (c) 2000 - 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.filter.db.inmem;

// External imports
import java.util.*;

// Local Imports
import j3d.filter.GeometryDatabase;
import j3d.filter.GeometryDatabaseManager;
import j3d.filter.GeometryImportDatabase;
import j3d.filter.SceneGraphObject;
import j3d.filter.SceneGraphObjectType;

import org.j3d.util.IntHashMap;


/**
 * Implementation of the geometry database manager and database combined in to a single
 * class, where the database is implemented entirely in memory.
 * <p/>
 * 
 *
 * @author Justin
 * @version $Revision$
 */
public class InMemoryDatabase
    implements GeometryDatabase, GeometryDatabaseManager
{
    /** Unsorted database of objects */
    private IntHashMap<SceneGraphObject> objectMap;

    /** When objects get named, put them here */
    private HashMap<String, SceneGraphObject> namedObjectMap;
    
    /** In-order list of URLs that still need to be processed */
    private List<String[]> pendingURLs;
    
    /** Map from the object type to the collection of objects in the scene that contain it */
    private Map<SceneGraphObjectType, Set<SceneGraphObject>> objectTypeMap;
    
    /** Node factory used to create new instances */
    private InMemoryNodeFactory nodeFactory;
    
    /**
     * Construct a new empty database implementation.
     */
    public InMemoryDatabase()
    {
         objectMap = new IntHashMap<>();
         namedObjectMap = new HashMap<>();
         objectTypeMap = new HashMap<>();
         pendingURLs = new ArrayList<>();
         
         nodeFactory = new InMemoryNodeFactory();
    }
  
    //------------------------------------------------------------------------
    // Methods defined by GeometryDatabaseManager
    //------------------------------------------------------------------------

    /**
     * Fetch the database interface that will sent to the various filters etc
     * Multiple calls to this class should return the same instance each time.
     * 
     * @return a non-null filter instance
     */
    public GeometryDatabase getDatabase()
    {
        return this;
    }
    
    @Override
    public void importBegins()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void importCompleted()
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * Notification that the import has started.
     */
    public void exportBegins()
    {
        
    }
    
    /**
     * Notification that the import has finished. 
     */
    public void exportCompleted()
    {
        
    }

    //------------------------------------------------------------------------
    // Methods defined by GeometryImportDatabase
    //------------------------------------------------------------------------

//    @Override
    public String[] getNextURI()
    {
        // TODO Auto-generated method stub
        return null;
    }

    //------------------------------------------------------------------------
    // Methods defined by GeometryDatabase
    //------------------------------------------------------------------------

    @Override
    public SceneGraphObject getObject(int id)
    {
        return objectMap.get(id);
    }

    @Override
    public SceneGraphObject getObject(String name)
    {
        return namedObjectMap.get(name);
    }

    @Override
    public boolean nameObject(String name, SceneGraphObject obj, boolean overwrite) 
        throws IllegalArgumentException
    {
        if(!overwrite)
        {
            if(namedObjectMap.containsKey(name))
                return false;
        }
        
        namedObjectMap.put(name, obj);
        
        return true;
    }

    @Override
    public void updateObject(SceneGraphObject obj)
    {
        if(obj == null)
            return;
        
    }

    @Override
    public void removeObject(SceneGraphObject obj)
    {
        if(obj == null)
            return;
        
        objectMap.remove(obj.getID());
        if(namedObjectMap.containsValue(obj))
        {
            Set<String> nuke_keys = new HashSet<>();
            
            for(Map.Entry<String, SceneGraphObject> entry: namedObjectMap.entrySet())
            {
                if(entry.getValue().equals(obj))
                    nuke_keys.add(entry.getKey());
            }
            
            for(String s: nuke_keys)
            {
                namedObjectMap.remove(nuke_keys);
            }
        }
    }

    @Override
    public SceneGraphObject createObject(SceneGraphObjectType type)
    {
        SceneGraphObject ret_val = nodeFactory.createNewNode(type);
        
        if(ret_val != null)
        {
            Set<SceneGraphObject> values = objectTypeMap.get(type);
            if(values == null)
            {
                values = new HashSet<>();
                objectTypeMap.put(type, values);
            }
            
            values.add(ret_val);
        }
        return ret_val;
    }

    @Override
    public Set<SceneGraphObject> getObjectsOfType(SceneGraphObjectType type)
    {
        // TODO Auto-generated method stub
        return Collections.unmodifiableSet(objectTypeMap.get(type));
    }

    @Override
    public void queueURIToLoad(int id, String[] uri)
    {
        // TODO: Very simple test for now. Doesn't really deal with alternate URLs
        // or partial versus fully qualified URLs.
        if(!pendingURLs.contains(uri))
            pendingURLs.add(uri);

    }

    @Override
    public int getRootObjectCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public SceneGraphObject getRootObject(int index)
    {
        // TODO Auto-generated method stub
        return null;
    }

    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------

}
