/*****************************************************************************
 *                        J3D.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.geom.subdivision;

// Standard imports
import javax.vecmath.Vector3f;

// Application specific imports
import org.j3d.geom.GeometryData;
import org.j3d.geom.GeometryGenerator;
import org.j3d.geom.InvalidArraySizeException;
import org.j3d.geom.UnsupportedTypeException;

/**
 * Abstract type values to describe various properties about subdivision
 * data structures.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface SubdivisionTypes
{
    /** The vertex is labeled as smooth (s = 0) */
    public int SMOOTH_VERTEX = 0;

    /** The vertex is labeled as a dart (s = 1) */
    public int DART_VERTEX = 1;

    /** The vertex is labeled as crease (s = 2) */
    public int CREASE_VERTEX = 2;

    /** The vertex is labeled as a corner (s > 2) */
    public int CORNER_VERTEX = 3;

    /** The edge is a regular edge */
    public int UNTAGGED_EDGE = 0;

    /** The edge is labeled as crease (s = 2) */
    public int CREASE_EDGE = 2;

}
