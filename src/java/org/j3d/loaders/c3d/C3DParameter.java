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
// None

// Local imports
// None

/**
 * Base representation of a single parameter.
 * <p>
 *
 * There are different data types of parameters, and this base class represents
 * all the types, from which individual data types are derived.
 *
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class C3DParameter
{
    /** This parameter type is a character (string) type */
    public static final int CHAR_TYPE = 1;

    /** This parameter type is a byte type */
    public static final int BYTE_TYPE = 2;

    /** This parameter type is a integer type */
    public static final int INT_TYPE = 3;

    /** This parameter type is a float type */
    public static final int FLOAT_TYPE = 4;

    /** Message when attempting to write to a locked group */
    protected static final String LOCKED_PARAM_MSG =
        "This parameter is locked and cannot be modified";

    /** The parameter type of this object */
    private final int paramType;

    /** The name of the parameter */
    protected String name;

    /** Indicating if the parameter is locked */
    protected boolean locked;

    /** The integer ID value of this parameter */
    protected int id;

    /** The description string, if set */
    protected String description;

    /** The dimensions of this parameter data */
    protected int[] dimensions;

    /**
     * The raw data array/object storage. For primitives (parameters with no
     * dimensions), this will be the java.lang object wrapper.
     */
    protected Object data;

    /**
     * Construct a new group that represents the given name
     *
     * @param name The name this parameter represents
     * @param locked true if this is a locked group
     * @param id The ID of this parameter
     */
    protected C3DParameter(int type, String name, boolean locked, int id)
    {
        paramType = type;
        this.name = name;
        this.locked = locked;
        this.id = id;
    }

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Compare this parameter for equality to the given object. Will be
     * equals if the ID, type and name are the same. It does not look at
     * the number of dimensions or the data itself. If this is needed, override
     * this method to add the required functionality.
     *
     * @param o The object instance of compare against
     * @return true if these are the same parameter by the above definition
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof C3DParameter))
            return false;

        C3DParameter p = (C3DParameter)o;

        return (p.id == id) &&
               (p.name.equals(name)) &&
               (p.paramType == paramType);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the base data type of this parameter. Returns one of the _TYPE
     * constants defined at the top of this class.
     *
     * @return A type identifier
     */
    public int getType()
    {
        return paramType;
    }


    /**
     * Get the name of the parameter.
     *
     * @return The name string
     */
    public String getName()
    {
        return name;
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
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        description = desc;
    }

    /**
     * Get the data representation object. Since there are so many different
     * types that could be assigned, this allows returning of any of the types.
     * The derived class will have methods to set the type-specific objects. It
     * will be up to the end user to understand the various parameter types and
     * cast to the appropriate object based on the data type and number of
     * dimensions. If the number of dimensions is 0, this returns a primitive
     * wrapper object for the data type.
     *
     * @return The current parameter object
     */
    public Object getValue()
    {
        return data;
    }

    /**
     * Get the number of dimensions that this object has. In the C3D file
     * format, a maximum of 7 dimensions are allowed. A value of 0 means this
     * is a primitive type.
     *
     * @return A value between 0 and 7
     */
    public int numDimensions()
    {
        return dimensions == null ? 0 : dimensions.length;
    }

    /**
     * Get the descriptions of the size of the dimensions of the object. If the
     * parameter has no dimensions, this returns null, otherwise it will be an
     * array of length {@link #numDimensions()}. Each index in the array
     * contains the number of elements in that dimension. Dimensions are
     * implicitly set from the parameter data.
     *
     * @return The dimensions array or null
     */
    public int[] getDimensions()
    {
        return dimensions;
    }
}
