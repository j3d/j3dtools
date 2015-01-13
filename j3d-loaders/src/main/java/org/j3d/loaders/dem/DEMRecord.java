/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.dem;

// External imports
// none

// Local imports
// none

/**
 * Representation of the DEM File format Type C record.
 * <p>
 *
 *
 * The definition of the file format can be found at:
 * <a href="http://edcwww.cr.usgs.gov/glis/hyper/guide/1_dgr_dem">
 *  http://edcwww.cr.usgs.gov/glis/hyper/guide/1_dgr_dem
 * </a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class DEMRecord
{
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;

    /** Array index of northing components of a coordinate. */
    public static final int NORTHING = 0;

    /** Array index of easting components of a coordinate. */
    public static final int EASTING = 0;

    /** Unit of measure is radians */
    public static final int RADIANS = 0;

    /** Unit of measure is feet */
    public static final int FEET = 1;

    /** Unit of measure is meters */
    public static final int METERS = 2;

    /** Unit of measure is arc-seconds */
    public static final int ARC_SECONDS = 3;
}
