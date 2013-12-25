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
 * @version $Revision: 1.2 $
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
     * An object has been completed.Object calls are top donw -
     * the parent is read and sent, but at the time it is sent does not
     * yet contain the children.
     *
     * @param parent The parent object that contains this surface
     * @param obj The object that was just read
     * @return true if to keep reading
     */
    public boolean objectComplete(Ac3dObject parent, Ac3dObject obj);

    /**
     * A surface definition from the previously declared object
     * has been read.
     *
     * @param obj The parent object that contains this surface
     * @param surf The surface object that has been read
     * @return true if to keep reading
     */
    public boolean surfaceComplete(Ac3dObject obj, Ac3dSurface surf);
}

