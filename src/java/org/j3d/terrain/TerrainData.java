/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

/*
 * @(#)TerrainData.java 1.1 02/01/10 09:29:18
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    -Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *    -Redistribution in binary form must reproduct the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed,licensed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */
package org.j3d.terrain;

// Standard imports
import javax.media.j3d.Texture;

import javax.vecmath.Point3f;

// Application specific imports
// none

/**
 * This class provides a generic interface to the terrain dataset.
 * <p>
 *
 * The dataset is represented as a regular grid of heights
 *
 * @author  Paul Byrne, Justin Couch
 * @version $Revision: 1.1 $
 */
public interface TerrainData
{
    /**
     * Get the coordinate of the point in the grid.
     *
     * @param coord must be an array of float[3]
     */
    public abstract void getCoordinateFromGrid(float[] coord,
                                               int gridX,
                                               int gridY);

    /**
     * Get the coordinate of the point in the grid
     *
     * @param coord must be an array of float[3], the x, y, and z coordinates
     * will be placed in the first three elements of the array.
     *
     * @param textureCoord must be an array of float[2]
     */
    public abstract void getCoordinateFromGrid(float[] coord,
                                               float[] textureCoord,
                                               int gridX,
                                               int gridY );

    /**
     * Fetch the Texture that is used to cover the entire terrain. If no
     * texture is used, then return null.
     *
     * @return The texture instance to use or null
     */
    public abstract Texture getTexture();

    /**
     * Get the height at the specified grid position
     */
    public abstract float getHeightFromGrid(int gridX, int gridY);

    /**
     * Get the width of the grid.
     */
    public abstract int getGridWidth();

    /**
     * Get the height of the grid.
     */
    public abstract int getGridHeight();

    /**
     * Get the real world distance between consecutive X values in the grid.
     */
    public abstract double getGridXStep();

    /**
     * Get the real world distance between consecutive Y values in the grid.
     */
    public abstract double getGridYStep();
}
