/*****************************************************************************
 *                    j3d.org Copyright (c) 2004  - 2011
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

import org.j3d.util.ErrorReporter;

// External imports
// None

// Local imports
// None

/**
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface GeometryFilter
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
     * Initialise the filter for the given database now. Processing has not yet
     * started for the filter at this time.
     * 
     * @param args The set of arguments that came from the command line used
     *     to provide additional configuration
     * @param db The database instance to use as the source for geometry
     * @return SUCCESS if the filter initialised properly, otherwise the error 
     *    code for the failure type.
     */
    public FilterExitCode initialise(String[] args, GeometryDatabase db);

    /**
     * Process the contents of the database now.
     * 
     * @return Success if the operation completed successfully, otherwise
     *    the error code and the processing terminates
     */
    public FilterExitCode process();
    
    /**
     * Filter processing has completed, so close down anything that this filter
     * may still have open.
     */
    public void shutdown();
}
