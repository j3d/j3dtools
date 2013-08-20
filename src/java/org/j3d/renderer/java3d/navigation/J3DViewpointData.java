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

package org.j3d.renderer.java3d.navigation;

// Standard imports
import javax.media.j3d.TransformGroup;

// Application specific imports
import org.j3d.ui.navigation.ViewpointData;

/**
 * A class to represent viewpoint information so that we can put it on screen,
 * move around to it etc etc.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class J3DViewpointData extends ViewpointData
{
    /** The transform group above the view */
    public TransformGroup viewTg;

    /**
     * Create a new data object initialised to the set of values.
     *
     * @param name The name to use
     * @param id The id of this viewpoint
     * @param tg The transformgroup for the view
     */
    public J3DViewpointData(String name, int id, TransformGroup tg)
    {
        super(name, id);

        this.viewTg = tg;
    }
}
