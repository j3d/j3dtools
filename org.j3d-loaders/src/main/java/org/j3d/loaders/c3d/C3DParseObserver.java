/*****************************************************************************
 *                         (c) j3d.org 2002 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.c3d;

// External imports
// None

// Local imports
// None

/**
 * An observer to process the parsing of a C3D file.
 * <p>
 *
 * The observer can be used to extract partial or full information from
 * the stream as it is being read by means of the return value from the
 * implemented methods. At any time user code returning a value of false
 * indicates that the user is finished with the parsing process and does not
 * require any more data from the stream. The parser should terminate the
 * process at this point.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public interface C3DParseObserver
{
    /**
     * The header of the file has been read.
     *
     * @param header The header information
     * @return true if to keep reading
     */
    public boolean headerComplete(C3DHeader header);

    /**
     * The parameter block has been read. The array will be the exact length
     * for the number of groups declared in the file.
     *
     * @param groups The listing of groups that were read
     * @return true if to keep reading
     */
    public boolean parametersComplete(C3DParameterGroup[] groups);

    /**
     *
     */
    public boolean trackDataAvailable(C3DTrajectoryData[] data);
}

