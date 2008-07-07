/*****************************************************************************
 *                         (c) j3d.org 2002 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.ac3d;

// External imports
// None

// Local imports
// None

/**
 * An observer to process the parsing of an AC3D file.
 * <p>
 *
 * The observer can be used to see the stream of data as it is read from the
 * file rather than wait for it all to be processed and read at the end.
 * <p>
 * At any time user code returning a value of false
 * indicates that the user is finished with the parsing process and does not
 * require any more data from the stream. The parser should terminate the
 * process at this point.
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public interface Ac3dParseObserver
{
    /**
     * A material block has been read from the file.
     *
     * @param mat The material definition
     * @return true if to keep reading
     */
    public boolean materialComplete(Ac3dMaterial mat);

    /**
     * The parameter block has been read. The array will be the exact length
     * for the number of groups declared in the file.
     *
     * @param groups The listing of groups that were read
     * @return true if to keep reading
     */
    public boolean objectComplete(Ac3dObject obj);
}

