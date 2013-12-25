/*****************************************************************************
 *                  j3d.org Copyright (c) 2000 - 2011
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.j3d.util.ErrorReporter;

// External imports
// None

// Local Imports
// None

/**
 * Representation of a file exporter for the filter chain
 * <p/>
 * The exporter is used to convert the internal database to a external output
 * type. Derived types from this can be used for various purposes, such as
 * files, 
 * structure.
 *
 * @author Justin
 * @version $Revision$
 */
public interface FilterExporter
{
    /**
     * Set the error handler to the given instance. If the value is null it
     * will clear the currently set instance and default back to the default
     * handler provided by this library.
     *
     * @param eh The error handler instance to use
     */
    public void setErrorReporter(ErrorReporter eh);

    /**
     * Initialise the exporter with the given database and configuration options
     * 
     * @param db The database instance to read from
     * @return A success or failure error code from FilterExitCode
     */
    public FilterExitCode initialize(GeometryDatabase db);
    
    /**
     * Begin the export now to a generic output stream. It this needs to
     * support unicode, then the implementation must wrap the stream with an
     * appropriate reader.
     * 
     * @param os The stream to export to
     * @return A success or failure error code from FilterExitCode
     */
    public FilterExitCode export(OutputStream os);
}
