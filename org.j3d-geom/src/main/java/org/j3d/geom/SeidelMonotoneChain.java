/*****************************************************************************
 *                        j3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.geom;

// External imports
// None

// Local imports
// None

/**
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class SeidelMonotoneChain
{
    int vnum;
    int next;
    int prev;

    /** Has this chain be used before */
    boolean marked;

    SeidelMonotoneChain()
    {
        marked = false;
    }

    /**
     * Reset everything back to zero again.
     */
    void clear()
    {
        vnum = 0;
        next = 0;
        prev = 0;
        marked = false;
    }
}
