/*****************************************************************************
 *                        j3d.org Copyright (c) 2001 - 20011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.filter;

// External imports
import java.io.*;
import java.util.*;

import java.net.UnknownHostException;

// Local imports
import org.j3d.util.ErrorReporter;

/**
 * An implementation of the ErrorReporter interface that just writes everything
 * to System.err but can be controlled for the amount of output
 * <p>
 *
 * The custom implementation to be used
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class FilterErrorReporter
    implements ErrorReporter
{
    /** The log level needed to get all messages printed out. */
    public static final int PRINT_ALL = 0;

    /** The log level needed to print out warnings and worse. */
    public static final int PRINT_WARNINGS = 1;

    /** The log level needed to print out errors and worse. */
    public static final int PRINT_ERRORS = 2;

    /** The log level needed to print out only fatal errors. */
    public static final int PRINT_FATAL_ERRORS = 3;

    /** Don't print out any messages */
    public static final int PRINT_NONE = 1000000;

    /** The set of exceptions to ignore the stack trace for */
    private HashSet<Class<? extends Exception>> ignoredExceptionTypes;

    /**
     * The current log level of the reporter. Higher number means only the more
     * severe messages are printed out. A level of 0 prints all messages.
     */
    private int logLevel;

    /**
     * Creates a new, default instance of the reporter that will print all
     * messages to the output.
     */
    FilterErrorReporter() 
    {
        this(PRINT_ALL);
    }

    /**
     * Creates a new, that will print messages of the given level to the output.
     *
     * @param level One of the error level constants
     */
    FilterErrorReporter(int level) 
    {
        logLevel = level;

        ignoredExceptionTypes = new HashSet<>();
        ignoredExceptionTypes.add(FileNotFoundException.class);
        ignoredExceptionTypes.add(IOException.class);
        ignoredExceptionTypes.add(UnknownHostException.class);
        ignoredExceptionTypes.add(IllegalArgumentException.class);
        ignoredExceptionTypes.add(ClassNotFoundException.class);
        ignoredExceptionTypes.add(InvalidClassException.class);
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
        if(logLevel < PRINT_WARNINGS)
            System.err.print(msg);
    }

    /**
     * Notification of an informational message from the system. For example,
     * it may issue a message when a URL cannot be resolved.
     *
     * @param msg The text of the message to be displayed
     */
    @Override
    public void messageReport(String msg) 
    {
        if(logLevel < PRINT_WARNINGS) 
        {
            System.err.print("Message: ");
            System.err.println(msg);
        }
    }

    /**
     * Notification of a warning in the way the system is currently operating.
     * This is a non-fatal, non-serious error. For example you will get an
     * warning when a value has been set that is out of range.
     *
     * @param msg The text of the message to be displayed
     * @param th The throwable that caused this warning. May be null
     */
    @Override
    public void warningReport(String msg, Throwable th)
    {
        handleWarningReport(msg, th);
    }

    /**
     * Notification of a recoverable error. This is a serious, but non-fatal
     * error, for example trying to locate a needed file or system property.
     *
     * @param msg The text of the message to be displayed
     * @param th The throwable that caused this warning. May be null
     */
    @Override
    public void errorReport(String msg, Throwable th) 
    {
        handleErrorReport(msg, th);
    }

    /**
     * Notification of a non-recoverable error that halts the entire system.
     * After you recieve this report the runtime system will no longer
     * function - for example a non-recoverable parsing error. The best way
     * out is to reload the file or restart the application internals.
     *
     * @param msg The text of the message to be displayed
     * @param th The throwable that caused this warning. May be null
     */
    @Override
    public void fatalErrorReport(String msg, Throwable th)
    {
        handleFatalErrorReport(msg, th);
    }
    
    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------

    /**
     * Change the current output level to the new value.
     *
     * @param level The new level to use for output
     */
    public void setLogLevel(int level) 
    {
        logLevel = level;
    }

    /**
     * Fetch the currently set log level.
     *
     * @return Whatever the currently set log level is
     */
    public int getLogLevel()
    {
        return logLevel;
    }

    /**
     * Internal processing of the warning report printing.
     * 
     * @param msg The message to be printed
     * @param th Optional throwable to have the trace printed for
     */
    private void handleWarningReport(String msg, Throwable th)
    {
        if(logLevel < PRINT_ERRORS) 
        {
            StringBuilder buf = new StringBuilder("Warning: ");

            if(msg != null) 
            {
                buf.append(msg);
                buf.append('\n');
            }

            if(th != null) 
                printNestedTrace(th, buf);

            System.err.println(buf.toString());
        }
    }

    /**
     * Internal processing of the error report printing.
     * 
     * @param msg The message to be printed
     * @param th Optional throwable to have the trace printed for
     */
    private void handleErrorReport(String msg, Throwable th) 
    {
        if(logLevel < PRINT_FATAL_ERRORS) 
        {
            StringBuilder buf = new StringBuilder("Error: ");

            if(msg != null) 
            {
                buf.append(msg);
                buf.append('\n');
            }

            if(th != null) 
                printNestedTrace(th, buf);

            System.err.println(buf.toString());
        }
    }

    /**
     * Internal processing of the fatal error report printing.
     * 
     * @param msg The message to be printed
     * @param th Optional throwable to have the trace printed for
     */
    private void handleFatalErrorReport(String msg, Throwable th)
    {
        if(logLevel < PRINT_NONE) 
        {
            StringBuilder buf = new StringBuilder("Fatal Error: ");

            if(msg != null) 
            {
                buf.append(msg);
                buf.append('\n');
            }

            if(th != null) 
                printNestedTrace(th, buf);

            System.err.println(buf.toString());
        }
    }
    
    /**
     * Handled nested cause printing. Prints the stack trace for this throwable
     * and if a nested cause is given, that is not in the ignore list, it will
     * recurse in to the next one and print that too.
     * 
     * @param th The throwable instance to print out
     * @param buffer The string buffer to append the stack trace to
     */
    private void printNestedTrace(Throwable th, StringBuilder buffer)
    {
        String txt = th.getMessage();
        if(txt == null)
            txt = th.getClass().getName();

        buffer.append(txt);
        buffer.append('\n');

        if(!ignoredExceptionTypes.contains(th.getClass()))
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            th.printStackTrace(pw);
            buffer.append(sw.toString());
        }
        
        Throwable cause = th.getCause();
        if(cause != null)
        {
            buffer.append("\n Nested Exception is:\n");
            printNestedTrace(cause, buffer);
        }
    }
}
