/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

/*
 * @(#)Landscape.java 1.3 02/01/10 09:29:16
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

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Representation of a piece of rendered terrain data.
 * <p>
 *
 * The landscape is used to control what it rendered on screen as the user
 * moves about the virtual environment. This instance does not need to maintain
 * all the polygons on the screen at any one time, but may control them as
 * needed.
 *
 * @author Paul Byrne, Justin Couch
 * @version
 */
public abstract class Landscape extends javax.media.j3d.BranchGroup
{
    /** The current viewing frustum that is seeing the landscape */
    protected ViewFrustum landscapeView;

    /** Raw terrain information to be rendered */
    protected TerrainData terrainData;

    /**
     * Create a new Landscape with the set view and data. If either are not
     * provided, an exception is thrown.
     *
     * @param view The viewing frustum to see the data with
     * @param data The raw data to view
     * @throws IllegalArgumentException either parameter is null
     */
    public Landscape(ViewFrustum view, TerrainData data)
    {
        if(view == null)
            throw new IllegalArgumentException("ViewFrustum not supplied");

        if(data == null)
            throw new IllegalArgumentException("Terrain data not supplied");

        terrainData = data;
        landscapeView = view;
    }

    /**
     * Set the current viewing direction for the user. The user is located
     * at the given point and looking in the given direction. All information
     * is assumed to be in world coordinates.
     *
     * @param position The position the user is in the virtual world
     * @param direction The orientation of the user's gaze
     */
    public abstract void setView(Point3f position, Vector3f direction);
}
