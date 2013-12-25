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
 * Base representation of the buffer reader that does not do any reading
 * itself.
 * <p>
 *
 * Derived versions of this interface do platform-specific reordering of the
 * bytes to generate the right value from the buffer.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
abstract class BufferReader
{
    /** The buffer to read values from */
    protected byte[] buffer;

    /**
     * Set the buffer to the new array reference.
     *
     * @param b The buffer instance to read
     */
    void setBuffer(byte[] b)
    {
        buffer = b;
    }

    /**
     * Read an short from the buffer, starting at the given byte.
     *
     * @param start The start index in the array
     * @return The value read
     */
    abstract int readShort(int start);

    /**
     * Read an int from the buffer, starting at the given byte.
     *
     * @param start The start index in the array
     * @return The value read
     */
    abstract int readInt(int start);

    /**
     * Read a string from the current buffer starting at the given position.
     *
     * @param start The start index in the array
     * @param len The number of bytes (characters) to read
     * @return The string containing that many characters
     */
    String readString(int start, int len)
    {
        char[] val = new char[len];
        int end = start + len;

        int idx = 0;
        for(int i = start; i < end; i++)
            val[idx++] = (char)buffer[i];

        return new String(val);
    }

    /**
     * Read an float from the buffer, starting at the given byte.
     *
     * @param start The start index in the array
     * @return The value read
     */
    float readFloat(int start)
    {
        int bits = readInt(start);

        return Float.intBitsToFloat(bits);
    }
}

