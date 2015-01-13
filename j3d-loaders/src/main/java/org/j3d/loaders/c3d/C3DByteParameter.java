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
 * A single parameter instance that represents String (character) data.
 * <p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class C3DByteParameter extends C3DParameter
{
    /**
     * Construct a new group that represents the given name
     *
     * @param name The name this parameter represents
     * @param locked true if this is a locked group
     * @param id The ID of this parameter
     */
    public C3DByteParameter(String name, boolean locked, int id)
    {
        super(BYTE_TYPE, name, locked, id);
    }

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Generate a string representation of this header.
     *
     * @return Information about the header
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer("C3DByteParameter: ");
        buf.append(name);
        buf.append(" ID: ");
        buf.append(id);
        buf.append(" locked? ");
        buf.append(locked ? "Yes" : "No");
        buf.append("\n Dimensions: ");
        buf.append(dimensions.length);
        buf.append(" (");

        if(dimensions.length > 0)
        {
            buf.append(dimensions[0]);

            for(int i = 1; i < dimensions.length; i++)
            {
                buf.append(", ");
                buf.append(dimensions[i]);
            }
        }

        buf.append(")");
        buf.append("\n Description: ");

        if(description != null);
            buf.append(description);

        return buf.toString();
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the parameter value as a single value.
     *
     * @param d The byte value to use as the value
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(byte d)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = null;
        data = new Byte(d);
    }

    /**
     * Set the parameter value as a single array value.
     *
     * @param d The byte values to use as the value
     * @param dim The dimensions of each value length
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(byte[] d, int[] dim)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = dim;
        data = d;
    }

    /**
     * Set the parameter value as a single dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The byte values to use as the value
     * @param dim The dimensions of each value length
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(byte[][] d, int[] dim)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = dim;
        data = d;
    }


    /**
     * Set the parameter value as a two dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The byte values to use as the value
     * @param dim The dimensions of each value length
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(byte[][][] d, int[] dim)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = dim;
        data = d;
    }


    /**
     * Set the parameter value as a three dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The byte values to use as the value
     * @param dim The dimensions of each value length
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(byte[][][][] d, int[] dim)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = dim;
        data = d;
    }


    /**
     * Set the parameter value as a four dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The byte values to use as the value
     * @param dim The dimensions of each value length
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(byte[][][][][] d, int[] dim)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = dim;
        data = d;
    }


    /**
     * Set the parameter value as a five dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The byte values to use as the value
     * @param dim The dimensions of each value length
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(byte[][][][][][] d, int[] dim)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = dim;
        data = d;
    }


    /**
     * Set the parameter value as a six dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The byte values to use as the value
     * @param dim The dimensions of each value length
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(byte[][][][][][][] d, int[] dim)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = dim;
        data = d;
    }
}
