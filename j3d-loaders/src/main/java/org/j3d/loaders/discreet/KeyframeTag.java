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
 * Base class representing the common data between all the keyframe tag types.
 * <p>
 *
 * A keyframe tag block consists of
 * <pre>
 *     NODE ID 0xB030
 *     NODE HEADER 0xB010
 * </pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class KeyframeTag
{
    /** An identifier for the node */
    public int nodeId;

    /** Node header data, if specified */
    public NodeHeaderData nodeHeader;
}
