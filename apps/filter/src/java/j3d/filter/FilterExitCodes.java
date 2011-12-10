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
public class FilterExitCodes
{

    /** Exit code used when no exceptions or errors have occurred */
    public static final int SUCCESS = 0;

    /** Input file not found.  Used when we can't read input file. */
    public static final int FILE_NOT_FOUND = 1;

    /** Invalid input file format. <br> Example: structural problem with
     * the geometry format, meaning the X3D is incorrectly defined, such as
     * geometry without a shape node. <br> Example: field is out of range. */
    public static final int INVALID_INPUT_FILE = 2;

    /** Unable to write output file.  <br> Example: Output file type
     * unknown, or there is a write permission error or similar problem. */
    public static final int CANNOT_WRITE_OUTPUT_FILE = 5;

    /** Invalid filter arguments provided other than file name.<br>
     * Example: arguments are out-of-bounds, or formatted incorrectly. */
    public static final int INVALID_ARGUMENTS = 6;

    /** Invalid filter specified.<br>
     * For debugging purposes only on initial deployment. */
    public static final int INVALID_FILTER_SPECIFIED = 7;

    /** The file contained some content that cannot be converted
     * to useable geometry. <br> Example: X3D Switch nodes or LODs. */
    public static final int NOT_ALL_GEOMETRY_IS_CONVERTABLE = 10;

    /** Exit code when a model is an supported file format.  Usually happens
     with mislabled files or combo formats like VRML 1/2 */
    public static final int UNSUPPORTED_FORMAT = 13;

    /** Recovered a useable file from a incorrect file.  Look at the
     * model closely. */
    public static final int RECOVERED_PARSING = 41;

    /** Exit code to use when application has timed out, exceeding the
     * runtime specified by a -maxRunTime parameter */
     public static final int MAX_RUN_TIME_EXCEEDED = 99;

    /** Unhandled exception.  The software crashed abnormally.
     * Probably error is due to a programming issue */
    public static final int ABNORMAL_CRASH = 101;

    /** The software failed due to lack of memory. */
    public static final int OUT_OF_MEMORY = 102;

    /** The software failed due to an exceptional error condition
     * that is outside ordinary failure modes.<br> Example:
     * failure to find native libraries or other configuration error. */
    public static final int EXCEPTIONAL_ERROR = 103;

}
