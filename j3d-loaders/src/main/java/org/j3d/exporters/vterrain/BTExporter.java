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

package org.j3d.exporters.vterrain;

import java.io.*;

import org.j3d.io.LittleEndianDataOutputStream;
import org.j3d.loaders.HeightMapSource;

/**
 * Writes out terrain data in the BT format. Export version is selectable,
 * and items are ignored as required by the spec version.
 *
 * @author justin
 */
public class BTExporter
{
    /** Which version of the file to export  */
    private final BTVersion exportVersion;

    /** If the datum reference code is zero, then use this value to specify which UTM zone the data is in */
    private boolean exportUTM;

    /**
     * Either a USGS reference datum code or EPSG code if the exportUTM flag is true, otherwise
     * a value [-60, -1] [1, 60] for a UTM zone ID.
     */
    private int datumCode;

    /** Whether to export grid height data as 2 or 4 byte floating point values */
    private boolean twoByteHeights;

    /** Whether to export heights as floats or integers */
    private boolean floatHeights;

    /** The scale that each vertical point represents, in metres. 1.0 is one metre. */
    private float verticalScale;

    /** Is an external .prj file needed for projection information */
    private boolean externalProjection;

    /** The number of rows to be exported */
    private int rowCount;

    /** The number of columns to be exported */
    private int columnCount;

    /** Source of the height data */
    private HeightMapSource sourceData;

    /**
     * Construct a new instance of the exporter for the given version
     *
     * @param version A non-null version request
     * @throws IllegalArgumentException The version number supplied was null
     */
    public BTExporter(BTVersion version)
    {
        if(version == null)
        {
            throw new IllegalArgumentException("No version number supplied");
        }

        exportVersion = version;
        twoByteHeights = true;
        floatHeights = false;
        externalProjection = false;
        verticalScale = 1.0f;
        rowCount = 0;
        columnCount = 0;
        datumCode = BTDatumCode.NO_DATUM.getCode();
    }

    /**
     * Get the version number of the file format that was requested to be exported
     *
     * @return A non-null version number.
     */
    public BTVersion getVersion()
    {
        return exportVersion;
    }

    /**
     * Check to see if this is using UTM or geodetic projection systems. Will return
     * true if the datum specified is zero.
     */
    public boolean usingUTM()
    {
        return exportUTM;
    }

    /**
     * Set the datum code to use and whether to interpret it as UTM or geodetic projection.
     *
     * @param isUTM true if this is a UTM zone ID, false for a geodetic projection
     * @param zone The ID of the zone to use
     */
    public void setDatum(boolean isUTM, int zone)
    {
        if(isUTM && (zone < -60 || zone > 60))
        {
            throw new IllegalArgumentException("UTM zone must be between -60 and +60");
        }

        exportUTM = isUTM;
        datumCode = zone;
    }

    /**
     * Get the UTM zone ID that this represents. Only valid if {@see #usingUTM()}
     * returns true. The default value is 0, which is also a valid UTM zone.
     *
     * @return A valid UTM zone id between [-60, 60]
     */
    public int getDatum()
    {
        return datumCode;
    }

    /**
     * Get how many bytes are used to export height values. Options are only 2 or 4 bytes,
     * and then combined with the floating point flag for the final export.
     *
     * @return A value of true means 2 bytes per height, a value of false is 4 bytes
     */
    public boolean isExportingTwoByteHeights()
    {
        return twoByteHeights;
    }

    /**
     * Change the height export type between 2 and 4 bytes. By default exports as
     * two bytes.
     *
     * @param enabled true if exporting 2 bytes, false if exporting 4 bytes
     */
    public void exportTwoByteHeights(boolean enabled)
    {
        twoByteHeights = enabled;
    }

    /**
     * Indicates whether floating point or integer values are exported for each grid
     * height value.
     *
     * @return true if floats are exported, false if integers
     */
    public boolean isExportingFloatHeights()
    {
        return floatHeights;
    }

    /**
     * Change the height export between floating point and integer values. By default
     * exports as integer values.
     *
     * @param enabled true to export as floats, false to export as integers
     */
    public void exportFloatHeights(boolean enabled)
    {
        floatHeights = enabled;
    }

    /**
     * Check to see if we have all the projection information needed in this file
     * or if we should reference an external .prj file. By default an external
     * projection file is not needed
     *
     * @return true if an external projection file is needed
     */
    public boolean needsExternalProjectionInfo()
    {
        return externalProjection;
    }

    /**
     * Change whether an external projection file is needed. By default it is
     * not needed.
     *
     * @param need true if we should flag needing the projection file, false for
     *    self-contained
     */
    public void requireExternalProjectionInfo(boolean need)
    {
        externalProjection = need;
    }

    /**
     * Get the vertical scale factor to a metre that is used by the file. By default
     * it is 1.0 meaning heights in the file are in metres.
     *
     * @return The vertical scale factor needed. Must be greater than zero
     */
    public float getVerticalScale()
    {
        return verticalScale;
    }

    /**
     * Set the scale factor from metres for the heights in this file. Default is 1.0.
     *
     * @param scale The new scale and must be greater than zero
     */
    public void setVerticalScale(float scale)
    {
        if(scale <= 0)
        {
            throw new IllegalArgumentException("Grid scale must be greater than zero");
        }

        verticalScale = scale;
    }

    /**
     * Set the size of the export grid. Both values must be greater than zero. By
     * default a 0x0 grid is exported unless explicity listed here. If the
     * values are zero at the point of export, and the height map source is provided
     * it will instead use the height map array sizes provided. If the row or
     * column count provided here is greater than that from the array, the additional
     * columns/rows will be padded with values 0.0.
     *
     * @param rows A value greater than zero
     * @param columns A value greater than zero
     */
    public void setGridSize(int rows, int columns)
    {
        if(rows < 1)
        {
            throw new IllegalArgumentException("Must export at least one row");
        }

        if(columns < 1)
        {
            throw new IllegalArgumentException("Must export at least one column");
        }

        rowCount = rows;
        columnCount = columns;
    }

    /**
     * Get the number of columns to be exported in this grid.
     *
     * @return A number greater than zero.
     */
    public int gridColumns()
    {
        return columnCount;
    }

    /**
     * Get the number of rows to be exported in this grid.
     *
     * @return A number greater than zero.
     */
    public int gridRows()
    {
        return rowCount;
    }

    /**
     * Set the provider of height data that this exporter will generate
     * the content for. Data does not need to be immediately available,
     * but will need to be present by the time the {@link #export} method
     * is called.
     *
     * @param data A non-null reference to a source data provider.
     */
    public void setDataSource(HeightMapSource data)
    {
        sourceData = data;
    }

    /**
     * Export the current configuration as specified in the other calls now
     * to the given stream;
     *
     * @param output The stream to write data to
     * @throws IOException Any form of I/O error while writing
     */
    public void export(OutputStream output) throws IOException
    {
        OutputStream buffered_stream;

        if(output instanceof BufferedOutputStream)
        {
            buffered_stream = new DataOutputStream(output);
        }
        else
        {
            BufferedOutputStream bis = new BufferedOutputStream(output);
            buffered_stream = new DataOutputStream(bis);
        }

        DataOutput data_output = new LittleEndianDataOutputStream(buffered_stream);

        writeVersionHeader(data_output);
        writeGridHeader(data_output);
        writeDataHeader(data_output);
        writeDatumHeader(data_output);
        writeExtentsHeader(data_output);
        writeProjectionHeader(data_output);
        writeHeaderPadding(data_output);
        writeHeightField(data_output);

        buffered_stream.flush();
    }

    private void writeVersionHeader(DataOutput dataOutput) throws IOException
    {
        switch(exportVersion)
        {
            case VERSION_1_0:
                dataOutput.write("binterr1.0".getBytes());
                break;

            case VERSION_1_1:
                dataOutput.write("binterr1.1".getBytes());
                break;

            case VERSION_1_2:
                dataOutput.write("binterr1.2".getBytes());
                break;

            case VERSION_1_3:
                dataOutput.write("binterr1.3".getBytes());
                break;
        }
    }

    private void writeGridHeader(DataOutput dataOutput) throws IOException
    {
        if(((rowCount == 0) || (columnCount == 0)) && (sourceData != null))
        {
            float[][] heights = sourceData.getHeights();
            if(columnCount == 0)
            {
                columnCount = heights.length;
            }

            if(rowCount == 0)
            {
                rowCount = heights[0].length;
            }
        }

        dataOutput.writeInt(columnCount);
        dataOutput.writeInt(rowCount);
    }

    private void writeDataHeader(DataOutput dataOutput) throws IOException
    {
        if(exportVersion != BTVersion.VERSION_1_0)
        {
            dataOutput.writeShort(twoByteHeights ? 2 : 4);
            dataOutput.writeShort(floatHeights ? 1 : 0);

            if(exportVersion == BTVersion.VERSION_1_3)
            {
                if(externalProjection)
                {
                    // TODO: Need flag for horizontal units
                    dataOutput.writeShort(0);
                }
                else
                {
                    dataOutput.writeShort(exportUTM ? 1 : 0);
                }
            }
            else
            {
                // Version 1.1 and 1.2 use a simple binary flag for UTM or Geographic flag
                dataOutput.writeShort(exportUTM ? 1 : 0);
            }
        }
        else
        {
            dataOutput.writeInt(twoByteHeights ? 2 : 4);
        }
    }

    private void writeDatumHeader(DataOutput dataOutput) throws IOException
    {
        if(exportUTM)
        {
            dataOutput.writeShort(datumCode);
            dataOutput.writeShort(0);
        }
        else
        {
            dataOutput.writeShort(0);
            dataOutput.writeShort(datumCode);
        }
    }

    private void writeExtentsHeader(DataOutput dataOutput) throws IOException
    {
        float left = 0;
        float right = 0;
        float bottom = 0;
        float top = 0;

        if(sourceData != null)
        {
            float[] grid_size = sourceData.getGridStep();

            int num_rows = rowCount - 1;
            int num_cols = columnCount - 1;

            switch(sourceData.getOriginLocation())
            {
                case CENTER:
                    float half_rows = num_rows * 0.5f;
                    float half_cols = num_cols * 0.5f;
                    left = -half_cols * grid_size[0];
                    right = half_cols * grid_size[0];
                    bottom = -half_rows * grid_size[1];
                    top = half_rows * grid_size[1];
                    break;

                case BOTTOM_LEFT:
                    // left and bottom default to zero
                    right = num_cols * grid_size[0];
                    top = num_rows * grid_size[1];
                    break;

                case BOTTOM_RIGHT:
                    // right and bottom default to zero
                    left = -num_cols * grid_size[0];
                    top = num_rows * grid_size[1];
                    break;

                case TOP_LEFT:
                    // left and top default to zero
                    right = num_cols * grid_size[0];
                    bottom = -num_rows * grid_size[1];
                    break;

                case TOP_RIGHT:
                    // right and top default to zero
                    left = -num_cols * grid_size[0];
                    bottom = -num_rows * grid_size[1];
                    break;

            }
        }

        if(exportVersion == BTVersion.VERSION_1_0)
        {
            dataOutput.writeFloat(left);
            dataOutput.writeFloat(right);
            dataOutput.writeFloat(bottom);
            dataOutput.writeFloat(top);
        }
        else
        {
            dataOutput.writeDouble(left);
            dataOutput.writeDouble(right);
            dataOutput.writeDouble(bottom);
            dataOutput.writeDouble(top);
        }
    }

    private void writeProjectionHeader(DataOutput dataOutput) throws IOException
    {
        if(exportVersion == BTVersion.VERSION_1_0)
        {
            dataOutput.writeShort(floatHeights ? 1 : 0);
            return;
        }

        if(exportVersion == BTVersion.VERSION_1_1)
        {
            return;
        }

        dataOutput.writeShort(externalProjection ? 1 : 0);

        if(exportVersion == BTVersion.VERSION_1_3)
        {
            dataOutput.writeFloat(verticalScale);
        }
    }

    private void writeHeaderPadding(DataOutput dataOutput) throws IOException
    {
        int pad_count = 0;

        switch(exportVersion)
        {
            case VERSION_1_0:
                pad_count = 212;
                break;

            case VERSION_1_1:
                pad_count = 196;
                break;

            case VERSION_1_2:
                pad_count = 194;
                break;

            case VERSION_1_3:
                pad_count = 190;
                break;
        }

        for(int i = 0; i < pad_count; i++)
        {
            dataOutput.writeByte(0);
        }
    }

    private void writeHeightField(DataOutput dataOutput) throws IOException
    {
        if(sourceData == null)
        {
            return;
        }

        float[][] heights = sourceData.getHeights();

        // columns by rows
        if(twoByteHeights)
        {
            if(floatHeights)
            {
                for(int i = 0; i < columnCount; i++)
                {
                    for(int j = 0; j < rowCount; j++)
                    {
                        if(heights[i].length <= j)
                        {
                            dataOutput.writeShort(0);
                        }
                        else
                        {
                            dataOutput.writeShort((int)heights[i][j]);
                        }
                    }
                }
            }
            else
            {
                for(int i = 0; i < columnCount; i++)
                {
                    if(i < heights.length)
                    {
                        for(int j = 0; j < rowCount; j++)
                        {
                            if(heights[i].length <= j)
                            {
                                dataOutput.writeShort(0);
                            }
                            else
                            {
                                dataOutput.writeShort((int) heights[i][j]);
                            }
                        }
                    }
                    else
                    {
                        for(int j = 0; j < rowCount; j++)
                        {
                            dataOutput.writeShort(0);
                        }
                    }
                }
            }
        }
        else
        {
            if(floatHeights)
            {
                for(int i = 0; i < columnCount; i++)
                {
                    if(i < heights.length)
                    {
                        for(int j = 0; j < rowCount; j++)
                        {
                            if(heights[i].length <= j)
                            {
                                dataOutput.writeFloat(0);
                            }
                            else
                            {
                                dataOutput.writeFloat(heights[i][j]);
                            }
                        }
                    }
                    else
                    {
                        for(int j = 0; j < rowCount; j++)
                        {
                            dataOutput.writeFloat(0);
                        }
                    }
                }
            }
            else
            {
                for(int i = 0; i < columnCount; i++)
                {
                    if(i < heights.length)
                    {
                        for(int j = 0; j < rowCount; j++)
                        {
                            if(heights[i].length <= j)
                            {
                                dataOutput.writeInt(0);
                            }
                            else
                            {
                                dataOutput.writeInt((int) heights[i][j]);
                            }
                        }
                    }
                    else
                    {
                        for(int j = 0; j < rowCount; j++)
                        {
                            dataOutput.writeInt(0);
                        }
                    }
                }
            }
        }
    }
}
