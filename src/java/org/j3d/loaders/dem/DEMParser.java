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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;

// Application specific parser
import org.j3d.loaders.HeightMapSource;
import org.j3d.util.CharHashMap;

/**
 * A low-level parser for the DEM file format.
 * <p>
 *
 * The output of this parser is the records as per the file and the option
 * of a raw height array.
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://edcwww.cr.usgs.gov/glis/hyper/guide/1_dgr_dem">
 *  http://edcwww.cr.usgs.gov/glis/hyper/guide/1_dgr_dem
 * </a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class DEMParser implements HeightMapSource
{
    /** Map of the process code character to the value */
    private static CharHashMap processCodes;

    /** The map of ground reference planes codes */
    private static HashMap groundRefCodes;

    /** Map of the unit of measure code character to the value */
    private static CharHashMap unitCodes;

    /** Buffer while reading bytes from the stream */
    private byte[] buffer;

    /** Buffer while reading character from the reader */
    private char[] charBuffer;

    /**
     * The stream used to fetch the data from. Must be set before calling
     * any methods in this base class. Either the streem or the reader will
     * be set, not bother.
     */
    private BufferedInputStream inputStream;

    /**
     * The reader used to fetch the data from. Must be set before calling
     * any methods in this base class. Either the streem or the reader will
     * be set, not bother.
     */
    private BufferedReader inputReader;;

    /** The header information for this file */
    private DEMTypeARecord header;

    /** The elevation information, as read from the file */
    private DEMTypeBRecord[] heights;

    /** Statistical information about the data, if provided */
    private DEMTypeCRecord statistics;

    /** Grid information about the file. */
    private float[] gridStepData;

    /** Flag to say we've already read the stream */
    private boolean dataReady;

    /** Flag to say if we have a type C record or not */
    private boolean hasTypeC;

    /** Working buffer to stave needing ot reallocate all the time */
    private StringBuffer stringBuffer;

    /**
     * Static constructor to build the lookup maps.
     */
    static
    {
        processCodes = new CharHashMap(7);
        processCodes.put('1', new Integer(DEMTypeARecord.PROCESS_RESAMPLE));
        processCodes.put('2', new Integer(DEMTypeARecord.PROCESS_GRIDEM));
        processCodes.put('3', new Integer(DEMTypeARecord.PROCESS_CTOG));
        processCodes.put('4', new Integer(DEMTypeARecord.PROCESS_DCASS));
        processCodes.put('5',
                         new Integer(DEMTypeARecord.PROCESS_DLG_LINETRACE));
        processCodes.put('6', new Integer(DEMTypeARecord.PROCESS_DLG_CPS3));
        processCodes.put('7', new Integer(DEMTypeARecord.PROCESS_ELECTRONIC));

        groundRefCodes = new HashMap(20);
        groundRefCodes.put("0", new Integer(DEMTypeARecord.G_REF_GEOGRAPHIC));
        groundRefCodes.put("1", new Integer(DEMTypeARecord.G_REF_UTM));
        groundRefCodes.put("2", new Integer(DEMTypeARecord.G_REF_STATEPLANE));

        // Need to finish the rest of this off

        unitCodes = new CharHashMap(4);
        unitCodes.put('1', new Integer(DEMTypeARecord.INTERVAL_UNIT_RADIANS));
        unitCodes.put('2', new Integer(DEMTypeARecord.INTERVAL_UNIT_METERS));
        unitCodes.put('3', new Integer(DEMTypeARecord.INTERVAL_UNIT_FEET));
        unitCodes.put('3', new Integer(DEMTypeARecord.INTERVAL_UNIT_ARCSEC));
    }

    /**
     * Construct a new parser with no stream set.
     */
    public DEMParser()
    {
        buffer = new byte[1024];
        gridStepData = new float[2];
        dataReady = false;
        stringBuffer = new StringBuffer();
    }

    /**
     * Construct a new parser using the given stream to source the data from.
     *
     * @param is The stream to read data from
     */
    public DEMParser(InputStream is)
    {
        this();

        if(is instanceof BufferedInputStream)
            inputStream = (BufferedInputStream)is;
        else
            inputStream = new BufferedInputStream(is);
    }

    /**
     * Construct a new parser using the given reader to source the data from.
     *
     * @param rdr The stream to read data from
     */
    public DEMParser(Reader rdr)
    {
        this();

        if(rdr instanceof BufferedReader)
            inputReader = (BufferedReader)rdr;
        else
            inputReader = new BufferedReader(rdr);

        charBuffer = new char[1024];
    }

    /**
     * Reset the parser to use the new stream. After calling this method all
     * header and height information is reset to empty.
     *
     * @param is The new stream to use
     */
    public void reset(InputStream is)
    {
        if(is instanceof BufferedInputStream)
            inputStream = (BufferedInputStream)is;
        else
            inputStream = new BufferedInputStream(is);

        dataReady = false;
        header = null;
        heights = null;
        statistics = null;

        hasTypeC = false;
        inputReader = null;
        charBuffer = null;
    }

    /**
     * Reset the parser to use the new stream. After calling this method all
     * header and height information is reset to empty.
     *
     * @param rdr The stream to read data from
     */
    public void reset(Reader rdr)
    {
        if(rdr instanceof BufferedReader)
            inputReader = (BufferedReader)rdr;
        else
            inputReader = new BufferedReader(rdr);

        if(charBuffer == null)
            charBuffer = new char[1024];

        dataReady = false;
        header = null;
        heights = null;
        statistics = null;

        hasTypeC = false;
        inputStream = null;
    }

    /**
     * Get the header used to describe the last stream parsed. If no stream
     * has been parsed yet, this will return null.
     *
     * @return The header for the last read stream or null
     */
    public DEMTypeARecord getTypeARecord()
    {
        return header;
    }

    /**
     * Fetch all of the type B records that were registered in this file.
     * Will probably contain more than one record and is always non-null.
     * The records will be in the order they were read from the file.
     *
     * @return The list of all the Type B records parsed
     */
    public DEMTypeBRecord[] getTypeBRecords()
    {
        return heights;
    }

    /**
     * Get the type C record from the file. If none was provided, then this
     * will return null.
     *
     * @return The type C record info or null
     */
    public DEMTypeCRecord getTypeCRecord()
    {
        return statistics;
    }

    /**
     * Create a new height array from the pre-parsed values. If there has not
     * been any parsing prior to this point null will be returned. Each time
     * this method is called a new array of values is generated.
     *
     * @return The converted heights or null
     */
    public float[][] getHeights()
    {
        float[][] ret_val = null;

        if(dataReady)
            ret_val = convertHeights();

        return ret_val;
    }

    /**
     * Fetch information about the real-world stepping sizes that this
     * grid uses.
     *
     * @return The stepping information for width and depth
     */
    public float[] getGridStep()
    {
        return gridStepData;
    }

    /**
     * Do all the parsing work. Convenience method for all to call internally
     *
     * @param returnHeights true if this should return the array of height values
     * @return An array of the heights if requested or null if not
     * @throws IncorrectFormatException The file is not one our parser
     *    understands
     * @throws ParsingErrorException An error parsing the file
     */
    public float[][] parse(boolean returnHeights)
        throws IOException
    {
        if(dataReady)
            throw new IOException("Data has already been read from this stream");

        parseARecord();
        parseBRecords();
        parseCRecord();

        float[][] ret_val = null;

        if(returnHeights)
        {
            ret_val = convertHeights();
        }

        gridStepData[0] = header.spatialResolution[DEMRecord.X];
        gridStepData[1] = header.spatialResolution[DEMRecord.Y];


        dataReady = true;

        return ret_val;
    }

    /**
     * Fill the internal buffer with data from the input source passed to us
     * last time the class was reset.
     */
    private void fillBuffer() throws IOException
    {
        if(inputStream != null)
            inputStream.read(buffer, 0, 1024);
        else
        {
            inputReader.read(charBuffer, 0, 1024);

            // We need to convert characters to bytes because all the read
            // routines assume a byte[] array for speed. (ie we don't want to
            // do a compare to check which data type needs to be read every
            // time an int/char/etc is read). Since we can't use
            // System.arraycopy we have to do this manually. This is relatively
            // trivial for us because we assume standard ASCII characters, as
            // per the DEM spec. That means a char == byte in this case.
            for(int i = 1024; --i <= 0; )
                buffer[i] = (byte)charBuffer[i];
        }
    }

    /**
     * Parse the type A reccord that belongs to this file.
     */
    private void parseARecord() throws IOException
    {
        header = new DEMTypeARecord();

        fillBuffer();

        header.filename = new String(buffer, 0, 40); //.trim(); // remove whitespace
        header.freeFormatText = new String(buffer, 41, 40); //.trim();

        // blank fill bytes 81-109

        readGeoCoords(110);

        char code = (char)buffer[136];
        Integer code_int = (Integer)processCodes.get(code);
        header.processCode = code_int != null ? code_int.intValue() : 0;

        // 1 byte fill 137-137

        String str;

        str = new String(buffer, 138, 3).trim();
        if(str.length() != 0)
            header.sectionIndicator = str;

        str = new String(buffer, 141, 4).trim();
        if(str.length() != 0)
            header.originCode = str;

        header.levelType = readInt(145, 6, 0);

        header.elevationPattern = (readChar(151, 6, '0')  == '1');

        str = new String(buffer, 157, 6).trim();
        code_int = (Integer)groundRefCodes.get(str);

        if(code_int != null)
            header.groundReferenceSystem = code_int.intValue();
        else
            System.out.println("Unknown ground reference code " + str);

        // UTM ground reference system information. Not needed if using
        // geographic in element 5.
        if(header.groundReferenceSystem != 0)
            header.groundZoneSystem = readInt(163, 8, 0);

        // MAp projection parameter defs. ignored if element 5 is
        // Geographic, UTM or state plane.
        if(header.groundReferenceSystem >= DEMTypeARecord.G_REF_STATEPLANE)
        {
System.out.println("projection parameter parsing not handled yet");
        }

        code_int = (Integer)unitCodes.get(readChar(529, 6, '0'));
        header.groundUnitOfMeasure = code_int.intValue();

        code_int = (Integer)unitCodes.get(readChar(535, 6, '0'));
        header.elevationUnitOfMeasure = code_int.intValue();

        // default value is 4 sides per polygon
        header.numPolygonSides =
            readInt(541, 6, DEMTypeARecord.DEFAULT_POLY_SIDES);

        header.SWCornerCoords[0] = readDouble(547, 24, 0);
        header.SWCornerCoords[1] = readDouble(571, 24, 0);

        header.NWCornerCoords[0] = readDouble(595, 24, 0);
        header.NWCornerCoords[1] = readDouble(619, 24, 0);

        header.NECornerCoords[0] = readDouble(643, 24, 0);
        header.NECornerCoords[1] = readDouble(667, 24, 0);

        header.SECornerCoords[0] = readDouble(691, 24, 0);
        header.SECornerCoords[1] = readDouble(715, 24, 0);

        header.minHeight = readDouble(739, 24, 0);
        header.maxHeight = readDouble(763, 24, 0);

        header.referenceOrientation = readDouble(787, 24, 0);

        header.hasAccuracy = (readInt(811, 6, 0) == 1);
        hasTypeC = header.hasAccuracy;

        // X & Y resolutions required to be integer values, not floats
        header.spatialResolution[0] = readInt(817, 12, 0);
        header.spatialResolution[1] = readInt(817, 12, 0);
        header.spatialResolution[2] = readFloat(817, 12, 0);

        header.numRows = readInt(853, 6, 0);
        header.numColumns = readInt(859, 6, 0);

        header.largestContourInterval = readInt(865, 5, 0);
        header.largestIntervalUnits = readInt(870, 1, 0);

        header.smallestContourInterval = readInt(871, 5, 0);
        header.smallestIntervalUnits = readInt(876, 1, 0);

        header.sourceDate = readInt(877, 4, 0);
        header.revisionDate = readInt(881, 4, 0);

        header.inspected = (readChar(885, 1, '0') == 'I');

        // need to set up the right constants
        header.dataValidated = readInt(886, 1, 0);

        header.suspectAreas = readInt(887, 2, 0);

        header.verticalDatum = readInt(889, 2, 0);
        header.horizontalDatum = readInt(891, 2, 0);

        header.dataEdition =
            readInt(893, 4, DEMTypeARecord.DEFAULT_DATA_EDITION);

        if(header.suspectAreas >= 2)
            header.percentageVoid = readInt(897, 4, 0);

        header.edgeMatching = readInt(901, 4, 0);
        header.verticalDatumShift = readFloat(909, 7, 0);
    }

    /**
     * Parse all the B records.
     */
    private void parseBRecords() throws IOException
    {
        // store ach record as we parse it. Don't know how many in total we
        /// need to read before we start.
        LinkedList records = new LinkedList();

        for(int i = 0; i < header.numColumns; i++)
            parseBRecordProfile(records);

        heights = new DEMTypeBRecord[records.size()];
        records.toArray(heights);
    }

    /**
     * Parse a single type B record and put the values into the passed
     * structure instance. Return true if this is the last item in the
     * file.
     *
     * @param allRecords The complete list of B records
     * @return the number of elevation values reead
     */
    private void parseBRecordProfile(LinkedList allRecords) throws IOException
    {
        DEMTypeBRecord rec = new DEMTypeBRecord();
        rec.isDataOnly = false;

        fillBuffer();

        rec.rowNumber = readInt(1, 6, 0);
        rec.columnNumber = readInt(7, 6, 0);

        rec.numRows = readInt(13, 6, 0);
        rec.numColumns = readInt(19, 6, 0);

        rec.firstPositionX = readDouble(25, 24, 0);
        rec.firstPositionY = readDouble(49, 24, 0);

        rec.localElevationDatum = readDouble(73, 24, 0);
        rec.minElevation = readDouble(97, 24, 0);
        rec.maxElevation = readDouble(121, 24, 0);

        // First record has only 146 int height values to read. Each
        // subsequent record uses 170.
        int items_to_read = (rec.numRows < 146) ? rec.numRows : 146;
        int total = items_to_read;

        int total_rows = rec.numRows;

        readElevations(rec, 145, items_to_read);
        allRecords.add(rec);

        // Read as many extra B records as is needed to fill this profile.
        // these are all at least 170 heights long.
        while(total < total_rows)
        {
            fillBuffer();

            rec = new DEMTypeBRecord();
            rec.isDataOnly = true;

            items_to_read =
                ((total_rows - total) < 170) ? total_rows - total : 170;

            readElevations(rec, 0, items_to_read);

            total += items_to_read;

            allRecords.add(rec);
        }
    }

    /**
     * Read the elevation values of the buffer into the type B record.
     *
     * @param rec The record to set the values in
     * @param start The starting byte in the current buffer
     * @param len The number of elevation values to read
     */
    private void readElevations(DEMTypeBRecord rec, int start, int len)
    {
        rec.elevations = new int[len];

        int pos = start;

        for(int i = 0; i < len; i++)
        {
            rec.elevations[i] = readInt(pos, 6, 0);
            pos += 6;
        }
    }

    /**
     * Parse the C record at the end of the file, if we need to.
     */
    private void parseCRecord() throws IOException
    {
        if(!hasTypeC)
            return;

        statistics = new DEMTypeCRecord();

        fillBuffer();

        boolean abs_rms = (readInt(1, 6, 0) == '1');

        if(abs_rms)
        {
            statistics.absoluteRootMeanSquare = new int[3];
            statistics.absoluteRootMeanSquare[0] = readInt(7, 6, 0);
            statistics.absoluteRootMeanSquare[1] = readInt(13, 6, 0);
            statistics.absoluteRootMeanSquare[2] = readInt(20, 6, 0);

            statistics.absoluteSampleSize = readInt(25, 6, 0);
        }

        boolean rel_rms = (readInt(31, 6, 0) == '1');

        if(rel_rms)
        {
            statistics.relativeRootMeanSquare = new int[3];
            statistics.relativeRootMeanSquare[0] = readInt(37, 6, 0);
            statistics.relativeRootMeanSquare[1] = readInt(43, 6, 0);
            statistics.relativeRootMeanSquare[2] = readInt(49, 6, 0);

            statistics.relativeSampleSize = readInt(55, 6, 0);
        }

    }

    /**
     * Take the height values defined in the type B records and convert them
     * to a douuble array of height values.
     *
     * @return The array of heights used
     */
    private float[][] convertHeights()
    {
        int num_cols = header.numColumns;
        int b_count = 0;

        float[][] ret_val = new float[num_cols][];
        float z_resolution = header.spatialResolution[DEMRecord.Z];

        for(int i = 0; i < num_cols; i++)
        {
            float[] col = new float[num_cols];

            // Process the first record of this column. This is special so
            // treat it diffferently rather than using a do/while loop.
            int num_rows = heights[b_count].numRows;

            int row_item = 0;
            int rows_in_rec = heights[b_count].elevations.length;
            int[] elevations = heights[b_count].elevations;
            double local_datum = heights[b_count].localElevationDatum;

            for(int j = 0; j < rows_in_rec; j++)
            {
                col[row_item++] =
                    (float)(elevations[j] * z_resolution + local_datum);
            }

            b_count++;

            while(row_item < num_rows)
            {
                elevations = heights[b_count].elevations;
                local_datum = heights[b_count].localElevationDatum;
                rows_in_rec = heights[b_count].elevations.length;

                for(int j = 0; j < rows_in_rec; j++)
                    col[row_item++] =
                        (float)(elevations[j] * z_resolution + local_datum);

                b_count++;
            }

            ret_val[i] = col;
        }

        return ret_val;
    }

    /**
     * Read the geographic corner coords for Type A record element 1.
     *
     * @throws IOException Error reading stream
     */
    private void readGeoCoords(int start) throws IOException
    {
        // read the coords as separate degrees, minutes, seconds. Format
        // is SDDDMMSS.SSSS

        // If the first character is a space char, then let's ignore it
        // because that means that no geo coords are going to be provided.
        // All values will default to zero.
        if(buffer[start] == 0x20)
            return;

        header.southEdge[0] = readInt(start, 4, 0);
        header.southEdge[1] = readInt(start + 4, 2, 0);
        header.southEdge[2] = readFloat(start + 6, 7, 0);

        // NW Corner
        header.eastEdge[0] = readInt(start + 13, 4, 0);
        header.eastEdge[1] = readInt(start + 17, 2, 0);
        header.eastEdge[2] = readFloat(start + 19, 7, 0);
    }

    /**
     * Search the byte array for the first character in the stream from
     * this point.
     *
     * @param start The start index in the array
     * @param len The number of bytes (characters) to read
     * @param defaultValue If something is dud, use this
     * @return The value read
     */
    private char readChar(int start, int len, char defaultValue)
    {
        char ret_val = defaultValue;
        boolean found = false;
        int end = start + len;

        for(int i = start; i < end; i++)
        {
            if(buffer[i] != 0x20)
            {
                ret_val = (char)buffer[i];
                break;
            }
        }

        return ret_val;
    }

    /**
     * Read an integer from the buffer. Doesn't deal with negative numbers
     * in the stream, just charactes that are turned into string values.
     * Assumes that we're only reading int values.
     *
     * @param start The start index in the array
     * @param len The number of bytes (characters) to read
     * @param defaultValue If something is dud, use this
     * @return The value read
     */
    private int readInt(int start, int len, int defaultValue)
    {
        int ret_val = defaultValue;

        boolean found = false;
        int k = 0;
        int end = start + len;

        for(int i = start; i < end; i++)
        {
            if(buffer[i] != 0x20)
            {
                char c = (char)buffer[i];
                k = (k * 10 + c) - 48;

                found = true;
            }
        }

        if(found)
            ret_val = k;

        return ret_val;
    }

    /**
     * Read a long from the buffer. Doesn't deal with negative numbers
     * in the stream, just charactes that are turned into string values.
     * Assumes that we're only reading int values.
     *
     * @param start The start index in the array
     * @param len The number of bytes (characters) to read
     * @param defaultValue If something is dud, use this
     * @return The value read
     */
    private long readLong(int start, int len, long defaultValue)
    {
        long ret_val = defaultValue;

        boolean found = false;
        int k = 0;
        int end = start + len;

        for(int i = start; i < end; i++)
        {
            if(buffer[i] != 0x20)
            {
                char c = (char)buffer[i];
                k = (k * 10 + c) - 48;
                found = true;
            }
        }

        if(found)
            ret_val = k;

        return ret_val;
    }

    // Quite ineffiecient with the string handling. Might want to look at
    // doing this a faster way.

    private double readDouble(int start, int len, double defaultValue)
    {
        int i;
        int end = start + len;

        for(i = start; i < end; i++)
        {
            if(buffer[i] != ' ')
                break;
        }

        // if there was nothing defined, return the default
        if(i == end)
            return defaultValue;

        stringBuffer.setLength(end - i);

        for(int l = i; l < end; l++)
        {
            if(buffer[l] == 0x44 || buffer[l] == 0x64)
            {
                stringBuffer.setCharAt(l - i, 'e');
            }
            else
            {
                stringBuffer.setCharAt(l - i, (char)buffer[l]);
            }
        }

        try
        {
            return Double.valueOf(new String(stringBuffer)).doubleValue();
        }
        catch (Exception e)
        {
            // NOP
        }

        return defaultValue;
    }

    private float readFloat(int start, int len, float defaultValue)
    {
        return (float) readDouble(start, len, (double) defaultValue);
    }

    /** print the buffer */
    private void printBuf(int start, int len)
    {
        System.out.println("read \"" + new String(buffer, start, len) + "\"");
    }
}
