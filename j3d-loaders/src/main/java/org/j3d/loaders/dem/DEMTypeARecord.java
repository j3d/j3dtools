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
 * Representation of the DEM File format Type A record.
 * <p>
 *
 * The DEM format uses X and Y as the coordinates of the ground plane and
 * Z as the height, which is not your traditional 3D graphics reference
 * system. Values  stored in this record will follow the spec, although the
 * heights in other records will follow the normal 3D conventions.
 * <p>
 *
 * All arrays will be pre-allocated to the correct length, with the exception
 * of the map projection parameters.
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
public class DEMTypeARecord extends DEMRecord
{
    // All values and defaults as per Table 2-1 of spec

    /** Values for Data Element 3 */
    public static final int DEM_1 = 1;
    public static final int DEM_2 = 2;
    public static final int DEM_3 = 3;
    public static final int DEM_4 = 4;

    /** Values for data element 1 - Process code. */
    public static final int PROCESS_RESAMPLE = 1;
    public static final int PROCESS_GRIDEM = 2;
    public static final int PROCESS_CTOG = 3;
    public static final int PROCESS_DCASS = 4;
    public static final int PROCESS_DLG_LINETRACE = 5;
    public static final int PROCESS_DLG_CPS3 = 6;
    public static final int PROCESS_ELECTRONIC = 7;

    /**
     * Codes defining the ground reference system for element 5. Values taken
     * from Appendix 2-G.
     */
    public static final int G_REF_GEOGRAPHIC = 0;
    public static final int G_REF_UTM = 1;
    public static final int G_REF_STATEPLANE = 2;

// 3 - 20 still to do
//    public static final int G_REF_GEOGRAPHIC = 0;

    public static final int INTERVAL_UNIT_RADIANS = 0;
    public static final int INTERVAL_UNIT_METERS = 2;
    public static final int INTERVAL_UNIT_FEET = 1;
    public static final int INTERVAL_UNIT_ARCSEC = 3;

    /** No data validation performed */
    public static final int VALIDATE_NONE = 0;

    /**
     * RMSE computed from test points (record C added), no quantitative
     * test, no interactive DEM editing or review.
     */
    public static final int VALIDATE_RMSE = 1;

    /** Batch process water body edit and RMSE computed from test points. */
    public static final int VALIDATE_BATCH = 2;

    /**
     * Review and edit, including water edit. No RMSE computed from test
     * points.
     */
    public static final int VALIDATE_WATER = 3;

    /**
     * Level 1 DEM's reviewed and edited. Includes water body editing. RMSE
     * computed from test points.
     */
    public static final int VALIDATE_L1_REVIEW = 4;

    /**
     * Level 2 and 3 DEM's reviewed and edited. Includes water body editing
     * and verification or vertical integration of planimetric categories
     * (other than hypsography or hydrography if authorized). RMSE computed
     * from test points.
     */
    public static final int VALIDATE_L2_VERIFIED = 5;

    // Values used for element 25

    /** No suspect or void areas in the data */
    public static final int AREA_SUSPECT_NONE = 0;

    /** Suspect areas in the data */
    public static final int AREA_SUSPECT = 0;

    /** Void areas in the data */
    public static final int AREA_VOID = 0;

    /** Suspect and void areas in the data */
    public static final int AREA_SUSPECT_AND_VOID = 0;

    // Values used for element 26. See Appendix 2-H

    /** Local mean sea level vertical datum */
    public static final int VERTICAL_DATUM_MSL = 1;

    /** National Geodetic Vertical Datum 1929 (NGVD 29) vertical datum */
    public static final int VERTICAL_DATUM_NGVD = 2;

    /** North American Vertical Datum 1988 (NAVD 88)vertical datum */
    public static final int VERTICAL_DATUM_NAVD = 3;

    // Values used for element 27. See Appendix 2-H

    /** North American Datum 1927 (NAD 27) horizontal datum */
    public static final int HORIZONTAL_DATUM_NAD27 = 1;

    /** World Geodetic System 1972 (WGS 72) horizontal datum */
    public static final int HORIZONTAL_DATUM_WGS72 = 2;

    /** World Geodetic System 1984 (WGS 83) horizontal datum */
    public static final int HORIZONTAL_DATUM_WGS84 = 3;

    /** North American Datum 1983 (NAD 83) horizontal datum */
    public static final int HORIZONTAL_DATUM_NAD83 = 4;

    /** Old Hawaii Datum horizontal datum */
    public static final int HORIZONTAL_DATUM_OLDHAWAII = 5;

    /** Puerto Rico Datum horizontal datum */
    public static final int HORIZONTAL_DATUM_PUERTORICO = 6;


    // Defaults

    /** Default pattern code for element 4 */
    public static final int DEFAULT_PATTERN = 1;

    /** Default number of sides in the polygon of element 10 */
    public static final int DEFAULT_POLY_SIDES = 4;

    /** Default angle offset between the two reference systems (element 13). */
    public static final float DEFAULT_REF_SYSTEM_ANGLE = 0;

    /** Default data edition in use. Assumes USGS. */
    public static final int DEFAULT_DATA_EDITION = 1;

    // Variables describing the file contents

    /** Name of the file (element 1). Maximum length of 40 chars. */
    public String filename;

    /** Some free-format text (element 1). Maximum length of 40 chars. */
    public String freeFormatText;

    /**
     * Location of the SE corner, south edge. Northing & easting values
     * represented with separate degrees. minutes & seconds.
     */
    public float[] southEdge = new float[3];

    /**
     * Location of the SE corner, east edge. Northing & easting values
     * represented with separate degrees. minutes & seconds.
     */
    public float[] eastEdge = new float[3];

    /** Code indicating the process used to create values (element 1) */
    public int processCode;

    /** The section of the area this file represent in 30 & 15 minute DEMs. */
    public String sectionIndicator;

    /** Mapping origin code (Element 2). Free-format 4-chars. */
    public String originCode;

    /** The DEM level used (element 3). */
    public int levelType;

    /** The elevation points are random (false) or regular (true)(element 4) */
    public boolean elevationPattern;

    /** Code defining the ground reference system (element 5). */
    public int groundReferenceSystem;

    /**
     * Code defining zone in the ground plane reference system (element 6).
     * This code is specific to the projection system used (eg UTM or state
     * plane). Values defined in Appendix 2-E and 2-F.
     */
    public int groundZoneSystem;

    /**
     * Extra map projection parameters used when not using UTM, Geo or
     * State-plane (element 7). If the projection params are not needed, this
     * is null rather than filling it with 15 zeros as per the file format.
     */
    public float[] mapProjectionParams;

    /** The type of unit of measure used for X,Y values (element 8). */
    public int groundUnitOfMeasure;

    /** The type of unit of measure used for elevation values (element 9). */
    public int elevationUnitOfMeasure;

    /** The number of sides of the polygon that the DEM covers (element 10). */
    public int numPolygonSides = DEFAULT_POLY_SIDES;


    /** Coordinates of the south-west corner (element 11). */
    public double[] SWCornerCoords = new double[2];

    /** Coordinates of the north-west corner (element 11). */
    public double[] NWCornerCoords = new double[2];

    /** Coordinates of the north-east corner (element 11). */
    public double[] NECornerCoords = new double[2];

    /** Coordibates if the south-west corner (element 11). */
    public double[] SECornerCoords = new double[2];

    /** The minimum elevation of the DEM (element 12). */
    public double minHeight;

    /** The maximum elevation of the DEM (element 12). */
    public double maxHeight;

    /**
     * Counterclockwise angle in radians from the primary axis of the
     * reference system to the local reference system (element 13).
     */
    public double referenceOrientation;

    /**
     * Flag to say if accuracy of the elevation is known (element 14).
     * If false, then no information is known. If true then a
     * {@link DEMTypeCRecord} Record C is available to give extra information.
     */
    public boolean hasAccuracy;

    /**
     * Amount of resolution for each axis (element 15). For X and Y axes, the
     * values are required to be integer values, although that cannot be forced
     * by this record.
     */
    public float[] spatialResolution = new float[3];

    /** The number of rows of profiles in the DEM (element 16). */
    public int numRows;

    /** The number of rows of profiles in the DEM (element 16). */
    public int numColumns;

    // Values used after this point are only used by the latest format revision

    /**
     * The largest primary contour interval (element 17). Only set if there
     * are two or more primary intervals (DEM LEvel 2 only)
     */
    public int largestContourInterval;

    /** The units for the largest interval (element 18). */
    public int largestIntervalUnits;

    /**
     * The smallest primary contour interval (element 19). Only set if there
     * are two or more primary intervals (DEM LEvel 2 only)
     */
    public int smallestContourInterval;

    /** The units for the smallest interval (element 20). */
    public int smallestIntervalUnits;

    /** The year the data was created, as an int (element 21). */
    public int sourceDate;

    /** The year the data was last revised or checked (element 22). */
    public int revisionDate;

    /**
     * True if the level 3 quality control process has been followed
     * (element 23).
     */
    public boolean inspected;

    /**
     * Flag indicating the type of validate that has been performed on the
     * data (element 24).
     */
    public int dataValidated;

    /** Flag indicating the state of the data (Element 25) */
    public int suspectAreas;

    /** The veritcal datum in use (element 26) */
    public int verticalDatum;

    /** The horizontal datum in use (element 27) */
    public int horizontalDatum;

    /**
     * Data edition as specified by the generator (element 28). A value between
     * 1 and 99 inclusive. For USGS use this will be set to 1.
     */
    public int dataEdition = DEFAULT_DATA_EDITION;

    /**
     * If element 25 indicates a void, this field contains the percentage
     * of nodes in the file set to void (element 29).
     */
    public int percentageVoid;

    /** Edge match status flag (element 30). */
    public int edgeMatching;

    /**
     * Vertical datum shift (element 31). Value is the average shift value for
     * the four quadrangle corners obtained from program VERTCON. Always add
     * this value to convert to NAVD88.
     */
    public float verticalDatumShift;
}

