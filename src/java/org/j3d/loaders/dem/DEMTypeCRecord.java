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
 * Representation of the DEM File format Type C record.
 * <p>
 *
 * The implementation of this record type does not quite strictly follow
 * the file format definiton. Instead of using flags to indicate the
 * availability of the data to follow, we just use the existance of the
 * variable value (an array). If the array instance is null, assume that
 * there is no available information.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://edcwww.cr.usgs.gov/glis/hyper/guide/1_dgr_dem">
 *  http://edcwww.cr.usgs.gov/glis/hyper/guide/1_dgr_dem
 * </a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.1 $
 */
public class DEMTypeCRecord extends DEMRecord
{
    /**
     * RMSE of file's datum relative to absolute datum (x, y, z)
     * (element 2). RMSE integer values are in the same unit of measure
     * given by data elements 8 and 9 of record type A.
     */
    public int[] absoluteRootMeanSquare;

    /**
     * Sample size on which statistics for the datum RMSE calculations
     * (element 3). If 0, then accuracy will be assumed to be estimated rather
     * than computed.
     */
    public int absoluteSampleSize;

    /**
     * RMSE of DEM data relative to file's datum (x, y, z) (element 5). RMSE
     * integer values are in the same unit of measure given by data elements 8
     * and 9 of record type A.
     */
    public int[] relativeRootMeanSquare;

    /**
     * Sample size on which statistics for the relative datum RMSE calculations
     * (element 6). If 0, then accuracy will be assumed to be estimated rather
     * than computed.
     */
    public int relativeSampleSize;
}
