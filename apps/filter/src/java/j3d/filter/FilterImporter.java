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

import org.j3d.util.ErrorReporter;

// External imports
// None

// Local Imports
// None

/**
 * Representation of a file importer for the filter chain
 * <p/>
 * The importer is used to convert a read file format in to the internal database
 * structure.
 *
 * @author Justin
 * @version $Revision$
 */
public interface FilterImporter
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
     * Initialise the importer with the given database and configuration options
     * 
     * @param db The database instance to write to
     * @return A success or failure error code from FilterExitCodes
     */
    public int initialize(GeometryImportDatabase db);
    
    /**
     * Process the contents of the input stream now.
     * 
     * @param is The input stream to use
     * @return A success or failure error code from FilterExitCodes
     * @throws IOException some sort of low level IO error happened during parsing
     *    that is outside the normal exit codes.
     */
    public int parse(InputStream is)
        throws IOException;
}
