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
import java.awt.geom.Rectangle2D;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

// Local imports
// None

/**
 * Data-holder class representing a single polygonalised character that has
 * been created by the {@link CharacterCreator}.
 * <p>
 *
 * Character data is held as a single indexed triangle array representation
 * and is relative to it's own local coordinate system. Character data is
 * presented in the 2D X-Z plane with the normal pointing along the positive Z
 * axis.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class CharacterData
{
    /** List of vertices needed to generate the text. Always a 3D list. */
    public FloatBuffer coordinates;

    /** The number of valid coordinate indices in the list */
    public int numIndex;

    /** The list of indices creating the triangles representing the text */
    public IntBuffer coordIndex;

    /** The 2D bounds of the character - in the X,Y plane. */
    public Rectangle2D.Float bounds;

    /** The generic scale that was applied to get these bounds */
    public float scale;
}
