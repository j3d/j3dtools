/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.util;

// JAXP packages

import java.io.PrintStream;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Common convenience implementation of the SAX ErrorHandler interface.
 * <p>
 * Provides marginally better error handling that prints to the nominated
 * output stream. If no stream is provided, it prints to stdout.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class SAXErrorHandler implements ErrorHandler
{

    /** The output stream to write to */
    private PrintStream output;

    /**
     * Create a new error handler that prints to the standard System.out.
     */
    public SAXErrorHandler()
    {
        output = System.out;
    }

    /**
     * Create an error handler that uses the given output stream for writing
     * debugging messages.
     */
    public SAXErrorHandler(PrintStream out)
    {
        if(out == null)
            output = System.out;
        else
            output = out;
    }

    /**
     * Returns a string describing parse exception details
     *
     * @param spe The exception to extract information from
     * @return A string with formatted information
     */
    private String getParseExceptionInfo(SAXParseException spe)
    {
        String systemId = spe.getSystemId();
        if (systemId == null)
        {
            systemId = "null";
        }

        String info = "URI=" + systemId +
            " Line=" + spe.getLineNumber() +
            ": " + spe.getMessage();
        return info;
    }

    // The following methods are standard SAX ErrorHandler methods.
    // See SAX documentation for more info.

    public void warning(SAXParseException spe) throws SAXException
    {
        System.out.println("Warning: " + getParseExceptionInfo(spe));
    }

    public void error(SAXParseException spe) throws SAXException
    {
        String message = "Error: " + getParseExceptionInfo(spe);
        throw new SAXException(message);
    }

    public void fatalError(SAXParseException spe) throws SAXException
    {
        String message = "Fatal Error: " + getParseExceptionInfo(spe);
        throw new SAXException(message);
    }
}
