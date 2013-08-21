/*****************************************************************************
 *                          J3D.org Copyright (c) 2000 - 2008
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders;

// External imports
// none

// Local imports
// none

/**
 * Exception for when the format of the input file does not match the
 * expectations of the parser implementation.
 * <P>
 *
 * This error would be thrown when there is absolutely no match between the
 * incoming stream and that required by the parser. This is different to the
 * {@link UnsupportedFormatException} which would be generated when the format
 * is recognised as valid, but not handled due to version or encoding issues.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class InvalidFormatException extends RuntimeException {

    /**
     * Create a blank exception with no message
     */
    public InvalidFormatException() {
    }

    /**
     * Create an exception that contains the given message.
     *
     * @param msg The message to associate with this exception
     */
    public InvalidFormatException(String msg) {
        super(msg);
    }
}
