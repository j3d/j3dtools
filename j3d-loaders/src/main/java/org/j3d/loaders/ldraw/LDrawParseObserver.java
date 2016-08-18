/*****************************************************************************
 *                         (c) j3d.org 2002 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.ldraw;

// External imports
// None

// Local imports
// None

/**
 * An observer to process the parsing of a LDraw file.
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
public interface LDrawParseObserver
{
    /**
     * A material block has been read from the file.
     *
     * @param hdr The header definition just completed
     * @return true if to keep reading
     */
    public boolean header(LDrawHeader hdr);

    /**
     * A BFC culling statement has been received and the following is the new state
     *
     * @param ccw true if the following polygons are wound counter clockwise
     * @param cull true if back face culling is to be performed
     * @return true if to keep reading
     */
    public boolean bfcStatement(boolean ccw, boolean cull);

    /**
     * An external file reference has been detected, and these are the details.
     *
     * @param ref The details of the file reference read
     * @return true if to keep reading
     */
    public boolean fileReference(LDrawFileReference ref);

    /**
     * A surface definition has been read.
     *
     * @param rend The renderable definition just read
     * @return true if to keep reading
     */
    public boolean renderable(LDrawRenderable rend);
}
