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

package j3d.filter.exporter;

// External imports
import java.io.OutputStream;

// Local Imports
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

import j3d.filter.FilterExitCode;
import j3d.filter.FilterExporter;
import j3d.filter.GeometryExportDatabase;

/**
 * Exporter for the Collada v1.4 file format.
 * <p/>
 * Exports a simple 1.4 format.
 *
 * @author Justin
 * @version $Revision$
 */
public class Collada14Exporter
    implements FilterExporter
{
    /** The error reporter for this interface */
    private ErrorReporter reporter;
    
    /** The database that we're going to be reading the scene graph from */
    private GeometryExportDatabase database;
    
    /**
     * Default constructor needed so that reflection works correctly.
     */
    public Collada14Exporter()
    {
        reporter = DefaultErrorReporter.getDefaultReporter();
    }
    
    //------------------------------------------------------------------------
    // Methods defined by FilterExporter
    //------------------------------------------------------------------------

    @Override
    public void setErrorReporter(ErrorReporter eh)
    {
        reporter = eh != null ? eh : DefaultErrorReporter.getDefaultReporter();
    }

    @Override
    public FilterExitCode initialize(GeometryExportDatabase db)
    {
        database = db;
        
        return FilterExitCode.SUCCESS;
    }

    @Override
    public FilterExitCode export(OutputStream os)
    {
        return FilterExitCode.SUCCESS;
    }
    
    //------------------------------------------------------------------------
    // Local Methods
    //------------------------------------------------------------------------
    
}
