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
 * An implementation of the ErrorReporter interface that just writes everything
 * to System.out.
 * <p>
 *
 * The default implementation to be used as convenience code for when the end
 * user has not supplied their own instance. By default, any class in this
 * repository that can be given an instance of the handler will use this
 * class, if none are given.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class DefaultErrorReporter implements ErrorReporter
{

    /** Global singleton instance. */
    private static DefaultErrorReporter instance;

    /**
     * Creates a new, default instance of the reporter
     */
    public DefaultErrorReporter()
    {
    }

    /**
     * Fetch the common global instance of the reporter.
     *
     * @return The global instance
     */
    public static ErrorReporter getDefaultReporter()
    {
        if(instance == null)
            instance = new DefaultErrorReporter();

        return instance;
    }

    //-----------------------------------------------------------------------
    // Methods defined by ErrorReporter
    //-----------------------------------------------------------------------
    
    /**
     * Notification of an partial message from the system.  When being written
     * out to a display device, a partial message does not have a line
     * termination character appended to it, allowing for further text to
     * appended on that same line.
     *
     * @param msg The text of the message to be displayed
     */
    @Override
    public void partialReport(String msg)
    {
        System.out.print(msg);        
    }

    /**
     * Notification of an informational message from the system. For example,
     * it may issue a message when a URL cannot be resolved.
     *
     * @param msg The general message string to report. May be null
     */
    @Override
    public void messageReport(String msg)
    {
        System.out.print("Message: ");
        System.out.println(msg);
    }

    /**
     * Notification of a warning in the way the system is currently operating.
     * This is a non-fatal, non-serious error. For example you will get an
     * warning when a value has been set that is out of range.
     *
     * @param msg The text of the message to be displayed
     * @param th The exception that caused this warning. May be null
     */
    @Override
    public void warningReport(String msg, Throwable th)
    {
        System.out.print("Warning: ");
        System.out.println(msg);

        if(th != null)
        {
            System.out.println("Contained message: ");
            System.out.println(th.getMessage());
            th.printStackTrace();
        }
    }

    /**
     * Notification of a recoverable error. This is a serious, but non-fatal
     * error, for example trying to locate a needed file or system property.
     *
     * @param msg The text of the message to be displayed
     * @param th The exception that caused this warning. May be null
     */
    @Override
    public void errorReport(String msg, Throwable th)
    {
        System.out.print("Error: ");
        System.out.println(msg);

        if(th != null)
        {
            System.out.println("Contained message: ");
            System.out.println(th.getMessage());
            th.printStackTrace();
        }
    }

    /**
     * Notification of a non-recoverable error that halts the entire system.
     * After you receive this report the runtime system will no longer
     * function - for example a non-recoverable parsing error. The best way
     * out is to reload the file or restart the application internals.
     *
     * @param msg The text of the message to be displayed
     * @param th The exception that caused this warning. May be null
     */
    @Override
    public void fatalErrorReport(String msg, Throwable th)
    {
        System.out.print("Fatal Error: ");
        System.out.println(msg);

        if(th != null)
        {
            System.out.println("Contained message: ");
            System.out.println(th.getMessage());
            th.printStackTrace();
        }
    }
}
