/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util;

// External imports
// none

// Local imports
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
public interface ErrorReporter
{
    /**
     * Notification of an partial message from the system.  When being written
     * out to a display device, a partial message does not have a line
     * termination character appended to it, allowing for further text to
     * appended on that same line.
     *
     * @param msg The text of the message to be displayed
     */
    public void partialReport(String msg);

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
     * @deprecated Use the warningReport(String, Throwable) version of this method
     */
    @Deprecated
    public void warningReport(String msg, Exception e);

    /**
     * Notification of a warning in the way the system is currently operating.
     * This is a non-fatal, non-serious error. For example you will get an
     * warning when a value has been set that is out of range.
     *
     * @param msg The text of the message to be displayed
     * @param th The throwable that caused this warning. May be null
     */
    public void warningReport(String msg, Throwable th);

    /**
     * Notification of a recoverable error. This is a serious, but non-fatal
     * error, for example trying to locate a needed file or system property.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     * @deprecated Use the errorReport(String, Throwable) version of this method
     */
    @Deprecated
    public void errorReport(String msg, Exception e);

    /**
     * Notification of a recoverable error. This is a serious, but non-fatal
     * error, for example trying to locate a needed file or system property.
     *
     * @param msg The text of the message to be displayed
     * @param th The throwable that caused this warning. May be null
     */
    public void errorReport(String msg, Throwable th);

    /**
     * Notification of a non-recoverable error that halts the entire system.
     * After you recieve this report the runtime system will no longer
     * function - for example a non-recoverable parsing error. The best way
     * out is to reload the file or restart the application internals.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     * @deprecated Use the fatalErrorReport(String, Throwable) version of this method
     */
    @Deprecated
    public void fatalErrorReport(String msg, Exception e);

    /**
     * Notification of a non-recoverable error that halts the entire system.
     * After you recieve this report the runtime system will no longer
     * function - for example a non-recoverable parsing error. The best way
     * out is to reload the file or restart the application internals.
     *
     * @param msg The text of the message to be displayed
     * @param th The throwable that caused this warning. May be null
     */
    public void fatalErrorReport(String msg, Throwable th);
}
