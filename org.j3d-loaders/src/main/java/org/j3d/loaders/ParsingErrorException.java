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
 * Exception for when an internal error was detected by the parsing system.
 * <P>
 *
 * Internal errors may be due to small issues such as invalid number formats
 * eg float instead of int.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class ParsingErrorException extends RuntimeException {

    /**
     * Create a blank exception with no message
     */
    public ParsingErrorException() {
    }

    /**
     * Create an exception that contains the given message.
     *
     * @param msg The message to associate with this exception
     */
    public ParsingErrorException(String msg) {
        super(msg);
    }
}
