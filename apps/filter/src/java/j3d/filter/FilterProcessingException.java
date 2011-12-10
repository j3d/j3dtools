/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
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
// None

// Local imports
// None

/**
 * Derived Exception for use within the filter change when a filter would
 * like to exit with a specific error code beyond the basic predefined codes.
 * <p>
 *
 * Effectively this implementation just turns the abstract class into a
 * concrete class without adding any additional implementation.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class FilterProcessingException extends RuntimeException 
{
    /** The name of the filter that generated this exception */
    private String filterName;

    /** The error code associated with that filter */
    private int errorCode;

    /**
     * Create an instance of the exception for the named filter, with the
     * specific error code and no custom message.
     *
     * @param name The name of the filter that generated this exception
     * @param code The error code to associate with this exception
     */
    public FilterProcessingException(String name, int code) 
    {
        filterName = name;
        errorCode = code;
    }

    /**
     * Create an instance of the exception for the named filter, with the
     * specific error code and a custom message.
     *
     * @param name The name of the filter that generated this exception
     * @param code The error code to associate with this exception
     */
    public FilterProcessingException(String name, int code, String message) 
    {
        super(message);

        filterName = name;
        errorCode = code;
    }

    /**
     * Get the error code that was assigned to this exception.
     *
     * @return The error code assigned. Always non-zero
     */
    public int getErrorCode() 
    {
        return errorCode;
    }

    /**
     * Fetch the name of the filter that generated this exception.
     *
     * @return A non-null name of the filter
     */
    public String getFilterName() 
    {
        return filterName;
    }
}
