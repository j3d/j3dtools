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

package j3d.filter;

// External imports
// None

// Local Imports
// None

/**
 * Extended scene graph object that defines it's child content in the form 
 * of URIs. 
 * <p/>
 * This core object type allows for the definition of compositions across
 * multiple sources 
 *
 * @author Justin
 * @version $Revision$
 */
public interface URLSceneGraphObject
    extends SceneGraphObject
{

    /** 
     * Set the Base URL that the file containing this node has used to define 
     * the content.
     * 
     * @param url A defined base URL that can be used to for relative 
     *     references that this node has made
     */
    public void setBaseURL(String url);
    
    /**
     * Get the base URL that has been defined for this model. If the URL has
     * not yet been defined, this will be null.
     * 
     * @return The base URL string or null if not defined yet
     */
    public String getBaseURL();
    
    /**
     * Get the list of URIs that define the content of this scene graph object.
     * The list is in order of preference to load, with the highest priority 
     * being the lowest index of the array.
     * 
     * @return A non-null array of URLs to process or null if there are none
     */
    public String[] getURIs();

    /**
     * Ask the state of the load of this node. The value will be one of the
     * constants defined above.
     *
     * @return The current load state of the node
     */
    public int getLoadState();

    /**
     * Set the load state of the node. The value must be one of the constants
     * defined above.
     *
     * @param state The new state of the node
     */
    public void setLoadState(int state);

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This assumes
     * at least some amount of intelligence on the part of the caller, but
     * we also know that we should not pass something dumb to it when we can
     * check what sort of content types it likes to handle. We assume the
     * loader thread is operating in the same context as the one that created
     * the node in the first place and thus knows the general types of items
     * to pass through.
     *
     * @param content The content of the object
     * @throws IllegalArgumentException The content object is not supported
     */
    public void setContent(Object content)
        throws IllegalArgumentException;
}
