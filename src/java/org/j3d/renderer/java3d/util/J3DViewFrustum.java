/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.renderer.java3d.util;

// Standard imports
import javax.media.j3d.Transform3D;
import javax.media.j3d.Canvas3D;

import javax.vecmath.Matrix4d;

// Application specific imports
import org.j3d.util.frustum.ViewFrustum;

/**
 * Java3D-specific implementation of the basic
 * {@link org.j3d.util.frustum.ViewFrustum}.
 * <p>
 *
 * Because Java3D can have multiple canvases that view a single scenegraph,
 * the view frustum must be a bit more complex than the traditional case. It
 * must take into account that every canvas has its own viewplatform, and they
 * may not be looking at the same thing. That means, for culling, they will
 * need to assemble the union of all the spaces for each canvas.
 * <p>
 *
 * The frustum is for the previous Java3D frame that has just been rendered.
 *
 * @author Paul Byrne, Justin Couch
 * @version $Revision: 1.1 $
 */
public class J3DViewFrustum extends ViewFrustum
{
    /** All the canvases that this frustum belongs to */
    private Canvas3D[] canvases;

    // Working vars for projection handling */
    private Transform3D leftInverseProjection;
    private Transform3D rightInverseProjection;

    /**
     * Create a new instance that operates on just a single canvas.
     *
     * @param canvas The canvas to use for this frustum
     */
    public J3DViewFrustum(Canvas3D canvas)
    {
        super(1);

        canvases = new Canvas3D[1];
        canvases[0] = canvas;

        init();
    }

    /**
     * Creates new ViewFrustum that represents the collection of all canvases.
     *
     * @param canvasList The list of canvases to view
     */
    public J3DViewFrustum(Canvas3D[] canvasList)
    {
        super(canvasList.length);

        canvases = canvasList;

        init();
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Perform common initialisation routines.
     */
    private void init()
    {
        leftInverseProjection = new Transform3D();
        rightInverseProjection = new Transform3D();
    }

    /**
     * Request from the renderer-specific canvas the inverse projection
     * matrix for the given canvasId.
     *
     * @param id The ID of the canvas
     * @param matrix The matrix to copy the data into
     */
    protected void getInverseWorldProjection(int id, Matrix4d matrix)
    {
        canvases[id].getInverseVworldProjection(leftInverseProjection,
                                                rightInverseProjection);
        leftInverseProjection.get(matrix);
    }
}
