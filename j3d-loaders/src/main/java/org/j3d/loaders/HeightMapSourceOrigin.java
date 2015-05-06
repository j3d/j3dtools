/*
 * j3d.org Copyright (c) 2001-2015
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.loaders;

/**
 * Enumeration of where the {@link HeightMapSource} will place the origin that
 * the grid points are located at. This is needed to calculate the X/Y locations
 * for each grid point based on the grid step.
 *
 * @author justin
 */
public enum HeightMapSourceOrigin
{
    /** Origin is at the centre of the grid, so split the row/columns evenly about it */
    CENTER,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}
