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
 * Exception for when the input file provided does not match the format
 * that the parser can handle.
 * <P>
 *
 * An unsupported format may be due to many reasons such as:
 * <ul>
 * <li>Specific version not handled</li>
 * <li>File encoding not handled (eg only binary, not ASCII)</li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class UnsupportedFormatException extends RuntimeException {

    /**
     * Create a blank exception with no message
     */
    public UnsupportedFormatException() {
    }

    /**
     * Create an exception that contains the given message.
     *
     * @param msg The message to associate with this exception
     */
    public UnsupportedFormatException(String msg) {
        super(msg);
    }
}
