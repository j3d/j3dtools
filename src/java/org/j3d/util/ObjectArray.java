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
// None

// Application specific imports
// None

/**
 * Simple dynamic array structure that holds Object instances.
 * <p>
 *
 * Idea is to save implementation weight when we don't really want to use a
 * full java.util collections class, but don't want to have to re-implement
 * the node copy/paste stuff every time.
 * <p>
 *
 * Code is sourced from the <a href="http://www.xj3d.org/">Xj3D project</a>.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ObjectArray
{
    /** The default size of this array */
    private static final int DEFAULT_SIZE = 512;

    /** The increment size of the array */
    private static final int INCREMENT_SIZE = 256;

    /** The number of items in this array currently */
    private int valueCount;

    /** The contents of this array */
    private Object[] array;

    /**
     * Create a new default array with size 512 items
     */
    public ObjectArray()
    {
        this(DEFAULT_SIZE);
    }

    /**
     * Create an array with the given initial size.
     *
     * @param initialSize The size to start with
     */
    public ObjectArray(int initialSize)
    {
        array = new Object[initialSize];
        valueCount = 0;
    }

    /**
     * Get the count of the number of items in the array.
     *
     * @return The number of items in the array
     */
    public int size()
    {
        return valueCount;
    }

    /**
     * Clear the array so that it contains no values
     */
    public void clear()
    {
        for(int i = 0; i < valueCount; i++)
            array[i] = null;

        valueCount = 0;
    }

    /**
     * Add a new value to the array. Will resize the array if needed to
     * accommodate new values.
     *
     * @param newNode the value to be added
     */
    public void add(Object newNode)
    {
        if(valueCount == array.length)
        {
            Object[] newArray =
                new Object[array.length + INCREMENT_SIZE];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }

        array[valueCount++] = newNode;
    }

    /**
     * Add an array of values in bulk to the array. The array should not
     * be null.
     *
     * @param values The values to be added
     */
    public void add(Object[] values)
    {
        int req_size = valueCount + values.length;

        if(req_size >= array.length)
        {
            Object[] newArray = new Object[req_size];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }

        System.arraycopy(values, 0, array, valueCount, values.length);
        valueCount = req_size;
    }

    /**
     * Get the value at the given index.
     *
     * @param index The position to get values from
     * @return The value at that index
     * @throws IndexOutOfBoundsException The index was not legal
     */
    public Object get(int index)
    {
        if((index < 0) || (index >= valueCount))
            throw new IndexOutOfBoundsException();

        return array[index];
    }

    /**
     * Set the value at the given index. If the index is out of the range
     * of the current items, it will generate an index exception.
     *
     * @param index The position to get values from
     * @param value The new value to set
     * @throws IndexOutOfBoundsException The index was not legal
     */
    public void set(int index, Object value)
    {
        if((index < 0) || (index >= valueCount))
            throw new IndexOutOfBoundsException();

        array[index] = value;
    }

    /**
     * Remove the value at the given index.
     *
     * @param index The position to remove the value from
     * @return The value at that index
     * @throws IndexOutOfBoundsException The index was not legal
     */
    public Object remove(int index)
    {
        if((index < 0) || (index >= valueCount))
            throw new IndexOutOfBoundsException();

        Object ret_val = array[index];

        System.arraycopy(array, index + 1, array, index, array.length - index - 1);
        valueCount--;

        return ret_val;
    }

    /**
     * Remove the first instance of the object found from this array. The
     * object equivalence is found by first using a == compare and then if
     * that fails, using the .equals() comparison. If this object is in the
     * array multiple times, only the first instance is deleted.
     *
     * @param obj The object instance to remove
     * @return The object removed, if found. Null if not found
     */
    public Object remove(Object obj)
    {

        // go looking for it:
        int found_index = -1;
        for(int i = 0; (i < valueCount) && (found_index != -1); i++)
        {
            if((array[i] == obj) || array[i].equals(obj));
                found_index = i;
        }

        Object ret_val = null;

        if(found_index != -1)
            ret_val = remove(found_index);

        return ret_val;
    }

    /**
     * Turn the values of this array into a real array. Returns an array with
     * the exact number of items in it. This is a separate copy of the internal
     * array.
     *
     * @return The array of values
     */
    public Object[] toArray()
    {
        Object[] ret_val = new Object[valueCount];

        System.arraycopy(array, 0, ret_val, 0, valueCount);

        return ret_val;
    }

    /**
     * Turn the values of this array into a real array by copying them into
     * the given array if possible. If the array is big enough then it will
     * copy the values straight in. If not, it will ignore that array and
     * create it's own copy and return that. If the passed array is used, the
     * return value will be a reference to the passed array, otherwise it will
     * be the new copy.
     *
     * @param values The array to copy values to
     * @return The array of values
     */
    public Object[] toArray(Object[] values)
    {
        Object[] ret_val = null;

        if(values.length >= valueCount)
            ret_val = values;
        else
            ret_val = new Object[valueCount];

        System.arraycopy(array, 0, ret_val, 0, valueCount);

        return ret_val;
    }
}
