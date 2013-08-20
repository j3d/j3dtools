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
 * Buffer reader that reads ints (shorts) and floats in the MIPS
 * (big-endian) byte order.
 * <p>
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
class MIPSBufferReader extends BufferReader
{
    //----------------------------------------------------------
    // Methods defined by BufferReader
    //----------------------------------------------------------

    /**
     * Read an short from the buffer, starting at the given byte.
     *
     * @param start The start index in the array
     * @return The value read
     */
    int readShort(int start)
    {
        int ch1 = buffer[start] & 255;
        int ch2 = (buffer[start + 1] & 255);

        return ((ch1 << 8) + ch2);
    }

    /**
     * Read an int from the buffer, starting at the given byte.
     *
     * @param start The start index in the array
     * @return The value read
     */
    int readInt(int start)
    {
        int ch1 = buffer[start] & 255;
        int ch2 = (buffer[start + 1] & 255);
        int ch3 = (buffer[start + 2] & 255);
        int ch4 = (buffer[start + 3] & 255);

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4);
    }
}
