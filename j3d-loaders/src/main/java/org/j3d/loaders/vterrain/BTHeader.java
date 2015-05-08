/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.vterrain;

// External imports
// none

// Local imports
// none

import org.j3d.exporters.vterrain.BTVersion;

/**
 * Representation of the BT File format header information.
 * <p>
 *
 * Not included in the header is data size or floating point flags as these
 * are only needed internally by the parser and not useful to the end user.
 *
 * The definition of the file format can be found at:
 * <a href="http://www.vterrain.org/Implementation/BT.html">
 *  http://www.vterrain.org/Implementation/BT.html
 * </a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class BTHeader
{
    /** The version of the underlying file format that was read. */
    public BTVersion version;

    /** The number of columns of data in the file */
    public int columns;

    /** The number of rows of data in the file */
    public int rows;

    /** True if this file is in UTM projection. False for Geographic. */
    public boolean utmProjection;

    /** The std UTM zone for this file */
    public int utmZone;

    /** The Datum used if not UTM */
    public int datum;

    /** The left-most extent Lat-Long if not UTM. */
    public double leftExtent;

    /** The right-most extent Lat-Long if not UTM. */
    public double rightExtent;

    /** The bottom-most extent Lat-Long if not UTM. */
    public double bottomExtent;

    /** The top-most extent Lat-Long if not UTM. */
    public double topExtent;

    /** True if this needs the external projection file for more information */
    public boolean needsExternalProj;
}
