/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.subdivision;

// External imports
// None

// Local imports
// None

/**
 * Internal representation of a quad for subdivision schemes that
 * work purely on quad.
 * <P>
 *
 * The vertices a to d are in ccw order around the quad.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class Quad
{
    /** First vertex of the quad */
    Vertex a;

    /** Second vertex of the quad */
    Vertex b;

    /** third vertex of the quad */
    Vertex c;

    /** Fourth vertex of the quad */
    Vertex d;

}
