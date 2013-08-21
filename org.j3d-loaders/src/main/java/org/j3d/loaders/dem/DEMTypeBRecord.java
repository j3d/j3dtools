/*****************************************************************************
 *                            (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.dem;

// Standard imports
// none

// Application specific imports
// none

/**
 * Representation of the DEM File format Type B record.
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
public class DEMTypeBRecord extends DEMRecord
{
    /**
     * Flag to say if this record only contains elevation values or if
     * it is the header record for this column. If this is data only, then
     * none of the other values will be set, except the elevation points.
     */
    public boolean isDataOnly;

    /** The row ID that this record belongs to (element 1). Starts at 1. */
    public int rowNumber;

    /**
     * The column ID of DEM profile stored in this record (element 1).
     * Starts at 1.
     */
    public int columnNumber;

    /** The number of rows covered by this record (element 2). */
    public int numRows;

    /** The number of columns covered by this record (element 2). */
    public int numColumns;

    /**
     * The X coord of the  ground planimetric coordinates of the first
     * elevation of this profile (element 3).
     */
    public double firstPositionX;

    /**
     * The Y coord of the  ground planimetric coordinates of the first
     * elevation of this profile (element 3).
     */
    public double firstPositionY;

    /**
     * Elevation of the local datum for the profile (element 4). The values
     * are in the units of measure given by element 9 of the Type A record in
     * this file.
     */
    public double localElevationDatum;

    /**
     * Minimum elevation of this profile (element 5). The values are in the
     * units of measure given by element 9 in logical record type A and are
     * the algebraic result of the method outlined in element 6 of this record.
     */
    public double minElevation;

    /**
     * Maximum elevation of this profile (element 5). The values are in the
     * units of measure given by element 9 in logical record type A and are
     * the algebraic result of the method outlined in element 6 of this record.
     */
    public double maxElevation;

    /**
     * The elevation values stored in this record (element 6). Elevations are
     * expressed in the units of resolution from element 15 of record type A.
     * A value in this array would be multiplied by the "z" spatial resolution
     * (element 15, record type A) "and added to the " elevation oflocal datum
     * for the profile (element 4 of this record) to obtain the elevation for
     * the point.
     */
    public int[] elevations;
}
