/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util;

// Standard imports
// none

// Application specific imports
// none

/**
 * Generalised interface for reporting errors of any kind that happens in
 * the Web3D codebase.
 * <p>
 *
 * Where methods provide both a string and exception, either of the values may
 * be null, but not both at the same time.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface ErrorReporter {

    /**
     * Notification of an informational message from the system. For example,
     * it may issue a message when a URL cannot be resolved.
     *
     * @param msg The text of the message to be displayed
     */
    public void messageReport(String msg);

    /**
     * Notification of a warning in the way the system is currently operating.
     * This is a non-fatal, non-serious error. For example you will get an
     * warning when a value has been set that is out of range.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     * @throws VRMLException This is bad enough that the reporter should stop
     *    what they are currently doing.
     */
    public void warningReport(String msg, Exception e);

    /**
     * Notification of a recoverable error. This is a serious, but non-fatal
     * error, for example trying to locate a needed file or system property.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     * @throws VRMLException This is bad enough that the reporter should stop
     *    what they are currently doing.
     */
    public void errorReport(String msg, Exception e);

    /**
     * Notification of a non-recoverable error that halts the entire system.
     * After you recieve this report the runtime system will no longer
     * function - for example a non-recoverable parsing error. The best way
     * out is to reload the file or restart the application internals.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     * @throws VRMLException This is bad enough that the reporter should stop
     *    what they are currently doing.
     */
    public void fatalErrorReport(String msg, Exception e);
}
