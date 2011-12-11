/*****************************************************************************
 *                     Copyright Yumetech, Inc (c) 2010 - 2011
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.1
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package j3d.filter;

// External imports
// none

// Internal imports
// none

/**
 * Collection of the exit codes for the server tools.
 * Previously, these were located in CDFFilter, but by commonizing
 * as many exit codes as we can we will make all our code bases more
 * consistent.  Other filter chains can extend this class to define
 * project-specific exit codes if desired.<p>

 * If everything is working as intended then exit code is equal to zero, or
 * {@link #SUCCESS}.  If we encounter any abnormalities, errors, or exceptions,
 * then a non-zero exit code should be used.  In general, the lower the value
 * of the non-zero exit code, the more we prioritize catching the error.
 *
 * @author Eric Fickenscher
 * @version $Revision: 1.0 $
 */
public enum FilterExitCode
{

    /** Exit code used when no exceptions or errors have occurred */
    SUCCESS(0),

    /** Input file not found.  Used when we can't read input file. */
    FILE_NOT_FOUND(1),

    /** Invalid input file format. <br> Example: structural problem with
     * the geometry format, meaning the X3D is incorrectly defined, such as
     * geometry without a shape node. <br> Example: field is out of range. */
    INVALID_INPUT_FILE(2),

    /** Unable to write output file.  <br> Example: Output file type
     * unknown, or there is a write permission error or similar problem. */
    CANNOT_WRITE_OUTPUT_FILE(5),

    /** Invalid filter arguments provided other than file name.<br>
     * Example: arguments are out-of-bounds, or formatted incorrectly. */
    INVALID_ARGUMENTS(6),

    /** Invalid filter specified.<br>
     * For debugging purposes only on initial deployment. */
    INVALID_FILTER_SPECIFIED(7),

    /** The importer requested cannot be mapped to a running class */
    INVALID_IMPORTER_SPECIFIED(20),

    /** The importer requested cannot be mapped to a running class */
    INVALID_EXPORTER_SPECIFIED(21),

    /** The database requested cannot be mapped to a running class */
    INVALID_DATABASE_SPECIFIED(22),

    /** The importer requested crashed on creation. */
    IMPORTER_STARTUP_ERROR(23),

    /** The exporter requested crashed on creation. */
    EXPORTER_STARTUP_ERROR(24),

    /** The database requested crashed on creation. */
    DATABASE_STARTUP_ERROR(25),

    /** 
     * The file contained some content that cannot be converted
     * to useable geometry. <br> Example: X3D Switch nodes or LODs. 
     */
    NOT_ALL_GEOMETRY_IS_CONVERTABLE(10),

    /** Exit code when a model is an supported file format.  Usually happens
     with mislabled files or combo formats like VRML 1/2 */
    UNSUPPORTED_FORMAT(13),

    /** Recovered a useable file from a incorrect file.  Look at the
     * model closely. */
    RECOVERED_PARSING(41),

    /** Exit code to use when application has timed out, exceeding the
     * runtime specified by a -maxRunTime parameter */
    MAX_RUN_TIME_EXCEEDED(99),

    /** Unhandled exception.  The software crashed abnormally.
     * Probably error is due to a programming issue */
    ABNORMAL_CRASH(101),

    /** The software failed due to lack of memory. */
    OUT_OF_MEMORY(102),

    /** The software failed due to an exceptional error condition
     * that is outside ordinary failure modes.<br> Example:
     * failure to find native libraries or other configuration error. */
    EXCEPTIONAL_ERROR(103);

    /** The internal error code type for this exit code */
    private final int exitCode;
    
    /**
     * Construct the enumerated type with the predefined value
     * 
     * @param t The value to associate with this type
     */
    FilterExitCode(int t)
    {
        exitCode = t;
    }
    
    /**
     * Get the integer exit value that this represents. The value will be what
     * is sent to System.exit();
     * 
     * @return an exit code between 0 and 255
     */
    public int getCodeValue()
    {
        return exitCode;
    }
}
