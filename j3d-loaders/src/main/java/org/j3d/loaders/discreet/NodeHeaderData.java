/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.discreet;

// External imports
// None

// Local imports
// None

/**
 * The node header data for keyframe chunk descriptions.
 * <p>
 *
 * The format of this data read from the file is:
 * <pre>
 * String objname;
 * short flags1;
 * short flags2;
 * short heirarchy;
 * </pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class NodeHeaderData
{
    /** The name of the object */
    public String name;

    /** The position in the heirarchy */
    public int heirarchyPosition;

    /** First set of flag data */
    public int flags1;

    /** Second set of flag data */
    public int flags2;
}
