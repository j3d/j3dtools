/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package j3d.examples.nurbs;

// External imports
import java.awt.*;
import java.awt.event.*;

// Local imports
// None

/**
 * Listener for handling drawing mode changes.
 * <p>
 *
 * There are three modes - add a point, remove a point and draw.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface DrawModeListener
{
    /** The mode is to draw and manipulate the existing points */
    public static final int DRAW = 1;

    /** The mode is to add new points */
    public static final int ADD = 2;

    /** The mode is to remove existing points */
    public static final int REMOVE = 3;

    /**
     * Notification that the drawing mode is now the new value.
     *
     * @param mode One of the modes above
     */
    public void changeMode(int mode);
}
