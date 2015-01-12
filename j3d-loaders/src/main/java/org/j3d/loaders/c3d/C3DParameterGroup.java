/*****************************************************************************
 *                         (c) j3d.org 2002 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.c3d;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
// None

/**
 * The representation of a single group of parameters.
 * <p>
 *
 * A parameter group is described by a name and contains a set of individual
 * parameter values.
 *
 *
 * The definition of the file format can be found at:
 * <a href="http://www.c3d.org">http://www.c3d.org/</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class C3DParameterGroup
{
    /** Message when attempting to write to a locked group */
    private static final String LOCKED_GROUP_MSG =
        "This group is locked and cannot be modified";

    /** Message when the added param's ID does not match this group ID */
    private static final String NOT_PARENT_ID_MSG =
        "The ID of the parameter does not match that of this group";

    /**
     * Message when attempting to add an existing param of the same name to
     * this group. Param names must be unique within a group.
     */
    private static final String HAS_SAME_NAME_MSG =
        "A parameter with this name is already a child of this group.";

    /**
     * The name of the group. May not be set at first if this was first created
     * in response to finding a parameter with the given group ID
     */
    private String name;

    /** Indicating if the parameter is locked */
    private boolean locked;

    /** The ID of the group. Group IDs are always negative. */
    private int id;

    /** The description string, if set */
    private String description;

    /** Collection of all parameters added to this group */
    private ArrayList<C3DParameter> parameters;

    /** Alternate mapping of parameter name strings to parameter object */
    private HashMap<String, C3DParameter> parameterMap;

    /**
     * Construct a new group that represents the given name
     *
     * @param name The name this group represents
     * @param locked true if this is a locked group
     * @param id The ID of this group
     * @param desc The description used on the group. May be null
     */
    public C3DParameterGroup(String name, boolean locked, int id, String desc)
    {
        this.name = name;
        this.locked = locked;
        this.id = id;
        this.description = desc;

        parameters = new ArrayList<>();
        parameterMap = new HashMap<>();
    }

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Generate a string representation of this header.
     *
     * @return Information about the header
     */
    public String toString()
    {
        StringBuilder buf = new StringBuilder("C3DParameterGroup: ");
        buf.append(name);
        buf.append(" ID: ");
        buf.append(id);
        buf.append(" locked? ");
        buf.append(locked ? "Yes" : "No");
        buf.append("\n Description: ");

        if(description != null);
            buf.append(description);

        return buf.toString();
    }

    /**
     * Generate a hashCode for this group. The hash is based on the hash of the
     * group name.
     *
     * @return A hash value based on the name of the group
     */
    public int hashCode()
    {
        return name.hashCode();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the name of the group
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of the group locally. Package private as this is only to be
     * used by the parsing process for filling in details of a proxy group that
     * was created because a parameter was read before the group definition.
     *
     * @param name The name of the group to use
     */
    void setName(String name)
    {
        this.name = name;
    }

    /**
     * Check to see if this parameter is marked as being locked (not editable).
     *
     * @return true if this is locked
     */
    public boolean isLocked()
    {
        return locked;
    }

    /**
     * Changed the locked state of the object.
     *
     * @param state true to make the object locked
     */
    public void setLocked(boolean state)
    {
        locked = state;
    }

    /**
     * Get the assigned ID of this parameter. The will only be positive because
     * negative values indicate groups.
     *
     * @return An positive integer parameter ID
     */
    public int getId()
    {
        return id;
    }

    /**
     * Get the description used for this group. If none is set, this returns
     * null.
     *
     * @return The current description string, or null if none
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the description string. A value of null clears the string. An
     * exception is generated if the user is attempting to write a locked
     * group.
     *
     * @param desc The new description string to use
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setDescription(String desc)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_GROUP_MSG);

        description = desc;
    }

    /**
     * Add a new parameter to the group. The id of the parameter must be the
     * positive value of this group's ID (which is negative).
     *
     * @param param The new completely filled out parameter definition
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     * @throws IllegalArgumentException The parameter ID indicates that it is
     *    not a child of this group or there is a parameter of this name
     *    already added
     */
    public void addParameter(C3DParameter param)
        throws IllegalStateException, IllegalArgumentException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_GROUP_MSG);

        if(param.id != -id)
            throw new IllegalArgumentException(NOT_PARENT_ID_MSG);

        String name = param.getName();

        for(C3DParameter p: parameters)
        {
            if(name.equals(p.getName()))
                throw new IllegalArgumentException(HAS_SAME_NAME_MSG);
        }

        parameters.add(param);
    }

    /**
     * Add a new parameter to the group. The id of the parameter must be the
     * positive value of this group's ID (which is negative).
     * <p>
     * Package private as this is only to be
     * used by the parsing process for filling in details of a proxy group that
     * was created because a parameter was read before the group definition.
     *
     * @param desc The new description string to use
     * @throws IllegalArgumentException The parameter ID indicates that it is
     *    not a child of this group or there is a parameter of this name
     *    already added
     */
    void addParameterUnlocked(C3DParameter param)
        throws IllegalArgumentException
    {
        if(param.id != -id)
            throw new IllegalArgumentException(NOT_PARENT_ID_MSG);

        String name = param.getName();

        for(C3DParameter p: parameters)
        {
            if(name.equals(p.getName()))
                throw new IllegalArgumentException(HAS_SAME_NAME_MSG);
        }

        parameters.add(param);
        parameterMap.put(param.getName(), param);
    }

    /**
     * Remove the given parameter from this group. If the parameter was not a
     * child of this group to start with, the request is ignored.
     *
     * @param param The parameter object to remove
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void removeParameter(C3DParameter param)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_GROUP_MSG);

        if(param.id != -id)
            return;

        parameters.remove(param);
        parameterMap.remove(param.getName());
    }

    /**
     * Get a parameter description based on the name string. If there is not a
     * matching description, this returns null. All parameter names are
     * capitalised, so make sure that this name matches.
     *
     * @param name The name string to go looking for
     * @return The C3DParameter instance matching the name, or null
     */
    public C3DParameter getParameter(String name)
    {
        return parameterMap.get(name);
    }

    /**
     * Get all of the parameters for this group.
     *
     * @return A new array containing all the parameter objects
     */
    public C3DParameter[] getParameters()
    {
        C3DParameter[] params = new C3DParameter[parameters.size()];
        parameters.toArray(params);

        return params;
    }
}
