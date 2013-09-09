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

package j3d.filter.filters;

import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

import j3d.filter.FilterExitCode;
import j3d.filter.GeometryDatabase;
import j3d.filter.GeometryFilter;

// External imports
// None

// Local Imports
// None

/**
 * Simple identity filter that does nothing at all.
 * <p/>
 * Long definition
 *
 * @author Justin
 * @version $Revision$
 */
public class IdentityFilter
    implements GeometryFilter
{
    /** The error reporter for this filter */
    private ErrorReporter reporter;
    
    /**
     * Construct a default instance of this filter
     */
    public IdentityFilter()
    {
        reporter = DefaultErrorReporter.getDefaultReporter();        
    }
    
    //------------------------------------------------------------------------
    // Methods defined by GeometryFilter
    //------------------------------------------------------------------------

    @Override
    public void setErrorReporter(ErrorReporter eh)
    {
        reporter = eh != null ? eh : DefaultErrorReporter.getDefaultReporter();
    }
    
    @Override
    public FilterExitCode initialise(String[] args, GeometryDatabase db)
    {
        reporter.messageReport("Identity Filter initialise() called");
        
        return FilterExitCode.SUCCESS;
    }

    @Override
    public FilterExitCode process()
    {
        reporter.messageReport("Identity Filter process() called");

        return FilterExitCode.SUCCESS;
    }

    @Override
    public void shutdown()
    {
        reporter.messageReport("Identity Filter shutdown() called");
    }
}
