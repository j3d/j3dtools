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
 * Internal representation of a triangle for subdivision schemes that
 * work purely on triangles.
 * <P>
 *
 * The vertices a to c are in ccw order around the triangle.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class Triangle
{
    /** First vertex of the triangle */
    Vertex a;

    /** Second vertex of the triangle */
    Vertex b;

    /** third vertex of the triangle */
    Vertex c;
}
