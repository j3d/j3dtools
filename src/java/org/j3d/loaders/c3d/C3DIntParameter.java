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
 * @version $Revision: 1.1 $
 */
public class C3DIntParameter extends C3DParameter
{
    /**
     * Construct a new group that represents the given name
     *
     * @param name The name this parameter represents
     * @param locked true if this is a locked group
     * @param id The ID of this parameter
     */
    public C3DIntParameter(String name, boolean locked, int id)
    {
        super(INT_TYPE, name, locked, id);
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
        StringBuffer buf = new StringBuffer("C3DIntParameter: ");
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
     * @param d The int value to use as the value
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(int d)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = null;
        data = new Integer(d);
    }

    /**
     * Set the parameter value as a single array value.
     *
     * @param d The int values to use as the value
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(int[] d)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = new int[1];
        dimensions[0] = d.length;

        data = d;
    }

    /**
     * Set the parameter value as a single dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The int values to use as the value
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(int[][] d)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = new int[2];
        dimensions[0] = d[0].length;
        dimensions[1] = d.length;

        data = d;
    }


    /**
     * Set the parameter value as a two dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The int values to use as the value
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(int[][][] d)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = new int[3];
        dimensions[0] = d[0][0].length;
        dimensions[1] = d[0].length;
        dimensions[2] = d.length;

        data = d;
    }


    /**
     * Set the parameter value as a three dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The int values to use as the value
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(int[][][][] d)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = new int[4];
        dimensions[0] = d[0][0][0].length;
        dimensions[1] = d[0][0].length;
        dimensions[2] = d[0].length;
        dimensions[3] = d.length;

        data = d;
    }


    /**
     * Set the parameter value as a four dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The int values to use as the value
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(int[][][][][] d)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = new int[5];
        dimensions[0] = d[0][0][0][0].length;
        dimensions[1] = d[0][0][0].length;
        dimensions[2] = d[0][0].length;
        dimensions[3] = d[0].length;
        dimensions[4] = d.length;

        data = d;
    }


    /**
     * Set the parameter value as a five dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The int values to use as the value
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(int[][][][][][] d)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = new int[6];
        dimensions[0] = d[0][0][0][0][0].length;
        dimensions[1] = d[0][0][0][0].length;
        dimensions[2] = d[0][0][0].length;
        dimensions[3] = d[0][0].length;
        dimensions[4] = d[0].length;
        dimensions[5] = d.length;

        data = d;
    }


    /**
     * Set the parameter value as a six dimensioned array value. When
     * setting the value, all items are assumed to be the same length as the
     * first elements of the array.
     *
     * @param d The int values to use as the value
     * @throws IllegalStateException The object is locked and cannot be
     *    changed
     */
    public void setValue(int[][][][][][][] d)
        throws IllegalStateException
    {
        if(locked)
            throw new IllegalStateException(LOCKED_PARAM_MSG);

        dimensions = new int[7];
        dimensions[0] = d[0][0][0][0][0][0].length;
        dimensions[1] = d[0][0][0][0][0].length;
        dimensions[2] = d[0][0][0][0].length;
        dimensions[3] = d[0][0][0].length;
        dimensions[4] = d[0][0].length;
        dimensions[5] = d[0].length;
        dimensions[6] = d.length;

        data = d;
    }
}
