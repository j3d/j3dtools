/*****************************************************************************
 *                        Yumtech, Inc Copyright (c) 2004-
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.hanim;

// External imports
// None

// Local imports
// None

/**
 * Marker of a class that can act as a parent to a HAnimObject.
 * <p>
 *
 * Used to abstract the parent information away from nodes, as for example,
 * a joint could have either a joint or a humanoid as the parent.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface HAnimObjectParent
{
    /**
     * Notification that the child has changed and will need to recalculate
     * it's vertex positions. A change could be in the transformation matrix,
     * coordinate weights or referenced coordinates (or elsewhere).
     *
     * @param child Reference to the child that has changed
     */
    public void childUpdateRequired(HAnimObject child);

    /**
     * Get the object's index into the greater list of things. For example, a
     * joint needs to be able to update it's matrix in a large array of
     * matrices and it needs to update the same matrix every time.
     *
     * @return The index of the object into global lists
     */
    public int requestNextObjectIndex();
}
