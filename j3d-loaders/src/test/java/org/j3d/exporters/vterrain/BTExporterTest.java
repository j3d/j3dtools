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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.j3d.io.LittleEndianDataInputStream;
import org.j3d.loaders.HeightMapSource;
import org.j3d.loaders.HeightMapSourceOrigin;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the BT exporter
 *
 * @author justin
 */
public class BTExporterTest
{
    private HeightMapSource exportTestSource = new HeightMapSource()
    {

        @Override
        public float[][] getHeights()
        {
            return new float[][]
                {
                    { 1.0f, 2.0f },
                    { 3.0f, 4.0f }
                };
        }

        @Override
        public float[] getGridStep()
        {
            return new float[] { 1.0f, 1.0f };
        }

        @Override
        public HeightMapSourceOrigin getOriginLocation()
        {
            return HeightMapSourceOrigin.BOTTOM_LEFT;
        }
    };

    @Test(groups = "unit")
    public void testBasicUTMConstruction() throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_1;
        final int TEST_UTM_ZONE = 45;

        BTExporter classUnderTest = new BTExporter(TEST_VERSION);
        classUnderTest.setDatum(true, TEST_UTM_ZONE);

        assertEquals(classUnderTest.getVersion(), TEST_VERSION, "Incorrect version stored");
        assertTrue(classUnderTest.usingUTM(), "Incorrect UTM handler flag");
        assertEquals(classUnderTest.getDatum(), TEST_UTM_ZONE, "Incorrect UTM zone stored");
        assertTrue(classUnderTest.isExportingTwoByteHeights(), "Did not default to 2 bytes per point");
        assertFalse(classUnderTest.isExportingFloatHeights(), "Should export integer heights by default");
        assertFalse(classUnderTest.needsExternalProjectionInfo(), "Default should not need external projection");
        assertEquals(classUnderTest.getVerticalScale(), 1.0f, "Default scale should be one");
        assertEquals(classUnderTest.gridColumns(), 0, "Default columns incorrect");
        assertEquals(classUnderTest.gridRows(), 0, "Default rows incorrect");
    }

    @Test(groups = "unit")
    public void testChangedConstruction() throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_2;
        final int TEST_GEODETIC_ID = 6310;
        final float TEST_VERTICAL_SCALE = 0.53f;

        BTExporter classUnderTest = new BTExporter(TEST_VERSION);
        classUnderTest.setDatum(false, TEST_GEODETIC_ID);
        classUnderTest.exportTwoByteHeights(false);
        classUnderTest.exportFloatHeights(true);
        classUnderTest.requireExternalProjectionInfo(true);
        classUnderTest.setVerticalScale(TEST_VERTICAL_SCALE);
        classUnderTest.setGridSize(1, 1);

        assertEquals(classUnderTest.getVersion(), TEST_VERSION, "Incorrect version stored");
        assertFalse(classUnderTest.usingUTM(), "Incorrect UTM handler flag");
        assertEquals(classUnderTest.getDatum(), TEST_GEODETIC_ID, "Incorrect datum stored");

        assertFalse(classUnderTest.isExportingTwoByteHeights(), "Did not change 2byte height flag");
        assertTrue(classUnderTest.isExportingFloatHeights(), "Did not change float height flag");
        assertTrue(classUnderTest.needsExternalProjectionInfo(), "Did not change external projection flag");

        assertEquals(classUnderTest.getVerticalScale(), TEST_VERTICAL_SCALE, "Incorrect scale saved");
        assertEquals(classUnderTest.gridColumns(), 1, "Saved columns incorrect");
        assertEquals(classUnderTest.gridRows(), 1, "Saved rows incorrect");
    }


    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testMissingVersion() throws Exception
    {
        new BTExporter(null);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testNegativeRowCount() throws Exception
    {
        BTExporter classUnderTest = new BTExporter(BTVersion.VERSION_1_3);
        classUnderTest.setGridSize(-1, 3);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testNegativeColumnCount() throws Exception
    {
        BTExporter classUnderTest = new BTExporter(BTVersion.VERSION_1_3);
        classUnderTest.setGridSize(1, -2);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testZeroRowCount() throws Exception
    {
        BTExporter classUnderTest = new BTExporter(BTVersion.VERSION_1_3);
        classUnderTest.setGridSize(0, 3);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testZeroColumnCount() throws Exception
    {
        BTExporter classUnderTest = new BTExporter(BTVersion.VERSION_1_3);
        classUnderTest.setGridSize(1, 0);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testZeroVerticalScale() throws Exception
    {
        BTExporter classUnderTest = new BTExporter(BTVersion.VERSION_1_3);
        classUnderTest.setVerticalScale(0.0f);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testNegativeVerticalScale() throws Exception
    {
        BTExporter classUnderTest = new BTExporter(BTVersion.VERSION_1_3);
        classUnderTest.setVerticalScale(-10.5f);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPositiveUTMZone() throws Exception
    {
        BTExporter classUnderTest = new BTExporter(BTVersion.VERSION_1_3);
        classUnderTest.setDatum(true, 61);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidNegativeUTMZone() throws Exception
    {
        BTExporter classUnderTest = new BTExporter(BTVersion.VERSION_1_3);
        classUnderTest.setDatum(true, -75);
    }

    @Test(groups = "unit")
    public void testBasicExportv1_3() throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_3;
        BTExporter classUnderTest = new BTExporter(TEST_VERSION);

        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        classUnderTest.export(output);

        byte[] data = output.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianDataInputStream resultStream = new LittleEndianDataInputStream(bis);

        // http://vterrain.org/Implementation/Formats/BT.html
        byte[] versionBytes = new byte[10];
        resultStream.read(versionBytes);

        String header = new String(versionBytes);
        assertEquals(header, "binterr1.3", "Incorrect version number written");

        assertEquals(resultStream.readInt(), 0, "Wrong column count written");
        assertEquals(resultStream.readInt(), 0, "Wrong row count written");
        assertEquals(resultStream.readShort(), 2, "Should have 2 byte heights");
        assertEquals(resultStream.readShort(), 0, "Should have integer heights");
        assertEquals(resultStream.readShort(), 0, "Geographic should have degree units");
        assertEquals(resultStream.readShort(), 0, "No UTM zone should be specified");
        assertEquals(resultStream.readShort(), BTDatumCode.NO_DATUM.getCode(), "Default datum should be zero");
        assertEquals(resultStream.readDouble(), 0.0, "Left extent incorrect");
        assertEquals(resultStream.readDouble(), 0.0, "Right extent incorrect");
        assertEquals(resultStream.readDouble(), 0.0, "Bottom extent incorrect");
        assertEquals(resultStream.readDouble(), 0.0, "Top extent incorrect");
        assertEquals(resultStream.readShort(), 0, "External projection flag should be zero");
        assertEquals(resultStream.readFloat(), 1.0f, "Wrong vertical scale");

        // make sure all this buffer is padding zeroes
        for(int i = 0; i < 190; i++)
        {
            assertEquals(resultStream.readByte(), 0, "Padding at byte " + i + " not zero");
        }

        assertEquals(resultStream.read(), -1, "End of stream not found");
    }

    @Test(groups = "unit")
    public void testBasicExportv1_2() throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_2;
        BTExporter classUnderTest = new BTExporter(TEST_VERSION);

        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        classUnderTest.export(output);

        byte[] data = output.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianDataInputStream resultStream = new LittleEndianDataInputStream(bis);

        // http://vterrain.org/Implementation/Formats/BT.html
        byte[] versionBytes = new byte[10];
        resultStream.read(versionBytes);

        String header = new String(versionBytes);
        assertEquals(header, "binterr1.2", "Incorrect version number written");

        assertEquals(resultStream.readInt(), 0, "Wrong column count written");
        assertEquals(resultStream.readInt(), 0, "Wrong row count written");
        assertEquals(resultStream.readShort(), 2, "Should have 2 byte heights");
        assertEquals(resultStream.readShort(), 0, "Should have integer heights");
        assertEquals(resultStream.readShort(), 0, "Geographic should have degree units");
        assertEquals(resultStream.readShort(), 0, "No UTM zone should be specified");
        assertEquals(resultStream.readShort(), BTDatumCode.NO_DATUM.getCode(), "Default datum should be NO_DATUM");
        assertEquals(resultStream.readDouble(), 0.0, "Left extent incorrect");
        assertEquals(resultStream.readDouble(), 0.0, "Right extent incorrect");
        assertEquals(resultStream.readDouble(), 0.0, "Bottom extent incorrect");
        assertEquals(resultStream.readDouble(), 0.0, "Top extent incorrect");
        assertEquals(resultStream.readShort(), 0, "External projection flag should be zero");

        // make sure all this buffer is padding zeroes
        for(int i = 0; i < 194; i++)
        {
            assertEquals(resultStream.readByte(), 0, "Padding at byte " + i + " not zero");
        }

        assertEquals(resultStream.read(), -1, "End of stream not found");
    }

    @Test(groups = "unit")
    public void testBasicExportv1_1() throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_1;
        BTExporter classUnderTest = new BTExporter(TEST_VERSION);

        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        classUnderTest.export(output);

        byte[] data = output.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianDataInputStream resultStream = new LittleEndianDataInputStream(bis);

        // http://vterrain.org/Implementation/Formats/BT.html
        byte[] versionBytes = new byte[10];
        resultStream.read(versionBytes);

        String header = new String(versionBytes);
        assertEquals(header, "binterr1.1", "Incorrect version number written");

        assertEquals(resultStream.readInt(), 0, "Wrong column count written");
        assertEquals(resultStream.readInt(), 0, "Wrong row count written");
        assertEquals(resultStream.readShort(), 2, "Should have 2 byte heights");
        assertEquals(resultStream.readShort(), 0, "Should have integer heights");
        assertEquals(resultStream.readShort(), 0, "Geographic should have degree units");
        assertEquals(resultStream.readShort(), 0, "No UTM zone should be specified");
        assertEquals(resultStream.readShort(), BTDatumCode.NO_DATUM.getCode(), "Default datum should be NO_DATUM");
        assertEquals(resultStream.readDouble(), 0.0, "Left extent incorrect");
        assertEquals(resultStream.readDouble(), 0.0, "Right extent incorrect");
        assertEquals(resultStream.readDouble(), 0.0, "Bottom extent incorrect");
        assertEquals(resultStream.readDouble(), 0.0, "Top extent incorrect");

        // make sure all this buffer is padding zeroes
        for(int i = 0; i < 196; i++)
        {
            assertEquals(resultStream.readByte(), 0, "Padding at byte " + i + " not zero");
        }

        assertEquals(resultStream.read(), -1, "End of stream not found");
    }

    @Test(groups = "unit")
    public void testBasicExportv1_0() throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_0;
        BTExporter classUnderTest = new BTExporter(TEST_VERSION);

        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        classUnderTest.export(output);

        byte[] data = output.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianDataInputStream resultStream = new LittleEndianDataInputStream(bis);

        // http://vterrain.org/Implementation/Formats/BT.html
        byte[] versionBytes = new byte[10];
        resultStream.read(versionBytes);

        String header = new String(versionBytes);
        assertEquals(header, "binterr1.0", "Incorrect version number written");

        assertEquals(resultStream.readInt(), 0, "Wrong column count written");
        assertEquals(resultStream.readInt(), 0, "Wrong row count written");
        assertEquals(resultStream.readInt(), 2, "Should have 2 byte heights");
        assertEquals(resultStream.readShort(), 0, "No UTM zone should be specified");
        assertEquals(resultStream.readShort(), BTDatumCode.NO_DATUM.getCode(), "Default datum should be NO_DATUM");
        assertEquals(resultStream.readFloat(), 0.0f, "Left extent incorrect");
        assertEquals(resultStream.readFloat(), 0.0f, "Right extent incorrect");
        assertEquals(resultStream.readFloat(), 0.0f, "Bottom extent incorrect");
        assertEquals(resultStream.readFloat(), 0.0f, "Top extent incorrect");
        assertEquals(resultStream.readShort(), 0, "Floating point flag incorrect");

        // make sure all this buffer is padding zeroes
        for(int i = 0; i < 212; i++)
        {
            assertEquals(resultStream.readByte(), 0, "Padding at byte " + i + " not zero");
        }

        assertEquals(resultStream.read(), -1, "End of stream not found");
    }

    @Test(groups = "unit", dataProvider = "extents handling")
    public void testDataSourceExtentsHandling(final HeightMapSourceOrigin origin,
                                              double expectedLeft,
                                              double expectedRight,
                                              double expectedBottom,
                                              double expectedTop)
        throws Exception
    {
        HeightMapSource testSource = new HeightMapSource()
        {

            @Override
            public float[][] getHeights()
            {
                return new float[][]
                {
                    { 1.0f, 2.0f },
                    { 3.0f, 4.0f }
                };
            }

            @Override
            public float[] getGridStep()
            {
                return new float[] { 1.0f, 1.0f };
            }

            @Override
            public HeightMapSourceOrigin getOriginLocation()
            {
                return origin;
            }
        };

        final BTVersion TEST_VERSION = BTVersion.VERSION_1_3;
        BTExporter classUnderTest = new BTExporter(TEST_VERSION);
        classUnderTest.setDataSource(testSource);

        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        classUnderTest.export(output);

        byte[] data = output.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianDataInputStream resultStream = new LittleEndianDataInputStream(bis);

        // http://vterrain.org/Implementation/Formats/BT.html
        byte[] versionBytes = new byte[10];
        resultStream.read(versionBytes);

        String header = new String(versionBytes);
        assertEquals(header, "binterr1.3", "Incorrect version number written");

        assertEquals(resultStream.readInt(), 2, "Wrong column count written");
        assertEquals(resultStream.readInt(), 2, "Wrong row count written");
        assertEquals(resultStream.readShort(), 2, "Should have 2 byte heights");
        assertEquals(resultStream.readShort(), 0, "Should have integer heights");
        assertEquals(resultStream.readShort(), 0, "Geographic should have degree units");
        assertEquals(resultStream.readShort(), 0, "No UTM zone should be specified");
        assertEquals(resultStream.readShort(), BTDatumCode.NO_DATUM.getCode(), "Default datum should be zero");
        assertEquals(resultStream.readDouble(), expectedLeft, "Left extent incorrect");
        assertEquals(resultStream.readDouble(), expectedRight, "Right extent incorrect");
        assertEquals(resultStream.readDouble(), expectedBottom, "Bottom extent incorrect");
        assertEquals(resultStream.readDouble(), expectedTop, "Top extent incorrect");

        // Skip these. Let's get to the other interesting part
        resultStream.readShort();
        resultStream.readFloat();

        // make sure all this buffer is padding zeroes
        for(int i = 0; i < 190; i++)
        {
            assertEquals(resultStream.readByte(), 0, "Padding at byte " + i + " not zero");
        }

        // should be 4 height values, each 1.0, as 2 byte integers
        assertEquals(resultStream.readShort(), 1, "Incorrect height at 0,0");
        assertEquals(resultStream.readShort(), 2, "Incorrect height at 0,1");
        assertEquals(resultStream.readShort(), 3, "Incorrect height at 1,0");
        assertEquals(resultStream.readShort(), 4, "Incorrect height at 1,1");

        assertEquals(resultStream.read(), -1, "End of stream not found");

    }

    @Test(groups = "unit")
    public void test2ByteIntegerExport()
        throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_3;
        BTExporter classUnderTest = new BTExporter(TEST_VERSION);
        classUnderTest.exportTwoByteHeights(true);
        classUnderTest.exportFloatHeights(false);
        classUnderTest.setDataSource(exportTestSource);

        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        classUnderTest.export(output);

        byte[] data = output.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianDataInputStream resultStream = new LittleEndianDataInputStream(bis);

        // http://vterrain.org/Implementation/Formats/BT.html
        byte[] versionBytes = new byte[10];
        resultStream.read(versionBytes);

        String header = new String(versionBytes);
        assertEquals(header, "binterr1.3", "Incorrect version number written");

        assertEquals(resultStream.readInt(), 2, "Wrong column count written");
        assertEquals(resultStream.readInt(), 2, "Wrong row count written");
        assertEquals(resultStream.readShort(), 2, "Should have 2 byte heights");
        assertEquals(resultStream.readShort(), 0, "Should have integer heights");

        // skip through the rest of the header here.
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readShort();
        resultStream.readFloat();

        // make sure all this buffer is padding zeroes
        for(int i = 0; i < 190; i++)
        {
            resultStream.readByte();
        }

        // should be 4 height values, each 1.0, as 2 byte integers
        assertEquals(resultStream.readShort(), 1, "Incorrect height at 0,0");
        assertEquals(resultStream.readShort(), 2, "Incorrect height at 0,1");
        assertEquals(resultStream.readShort(), 3, "Incorrect height at 1,0");
        assertEquals(resultStream.readShort(), 4, "Incorrect height at 1,1");

        assertEquals(resultStream.read(), -1, "End of stream not found");
    }

    @Test(groups = "unit")
    public void test2ByteIntegerExportGridOversize()
        throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_3;
        BTExporter classUnderTest = new BTExporter(TEST_VERSION);
        classUnderTest.exportTwoByteHeights(true);
        classUnderTest.exportFloatHeights(false);
        classUnderTest.setDataSource(exportTestSource);

        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        classUnderTest.setGridSize(3, 3);
        classUnderTest.export(output);

        byte[] data = output.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianDataInputStream resultStream = new LittleEndianDataInputStream(bis);

        // http://vterrain.org/Implementation/Formats/BT.html
        byte[] versionBytes = new byte[10];
        resultStream.read(versionBytes);

        String header = new String(versionBytes);
        assertEquals(header, "binterr1.3", "Incorrect version number written");

        assertEquals(resultStream.readInt(), 3, "Wrong column count written");
        assertEquals(resultStream.readInt(), 3, "Wrong row count written");
        assertEquals(resultStream.readShort(), 2, "Should have 2 byte heights");
        assertEquals(resultStream.readShort(), 0, "Should have integer heights");

        // skip through the rest of the header here.
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readShort();
        resultStream.readFloat();

        // make sure all this buffer is padding zeroes
        for(int i = 0; i < 190; i++)
        {
            resultStream.readByte();
        }

        // should be 4 height values, each 1.0, as 2 byte integers
        assertEquals(resultStream.readShort(), 1, "Incorrect height at 0,0");
        assertEquals(resultStream.readShort(), 2, "Incorrect height at 0,1");
        assertEquals(resultStream.readShort(), 0, "Incorrect height at 0,2");
        assertEquals(resultStream.readShort(), 3, "Incorrect height at 1,0");
        assertEquals(resultStream.readShort(), 4, "Incorrect height at 1,1");
        assertEquals(resultStream.readShort(), 0, "Incorrect height at 1,2");
        assertEquals(resultStream.readShort(), 0, "Incorrect height at 2,0");
        assertEquals(resultStream.readShort(), 0, "Incorrect height at 2,1");
        assertEquals(resultStream.readShort(), 0, "Incorrect height at 2,2");

        assertEquals(resultStream.read(), -1, "End of stream not found");
    }

    @Test(groups = "unit")
    public void test4ByteIntegerExportGridOversize()
        throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_3;
        BTExporter classUnderTest = new BTExporter(TEST_VERSION);
        classUnderTest.exportTwoByteHeights(false);
        classUnderTest.exportFloatHeights(false);
        classUnderTest.setDataSource(exportTestSource);

        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        classUnderTest.setGridSize(3, 3);
        classUnderTest.export(output);

        byte[] data = output.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianDataInputStream resultStream = new LittleEndianDataInputStream(bis);

        // http://vterrain.org/Implementation/Formats/BT.html
        byte[] versionBytes = new byte[10];
        resultStream.read(versionBytes);

        String header = new String(versionBytes);
        assertEquals(header, "binterr1.3", "Incorrect version number written");

        assertEquals(resultStream.readInt(), 3, "Wrong column count written");
        assertEquals(resultStream.readInt(), 3, "Wrong row count written");
        assertEquals(resultStream.readShort(), 4, "Should have 2 byte heights");
        assertEquals(resultStream.readShort(), 0, "Should have integer heights");

        // skip through the rest of the header here.
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readShort();
        resultStream.readFloat();

        // make sure all this buffer is padding zeroes
        for(int i = 0; i < 190; i++)
        {
            resultStream.readByte();
        }

        // should be 4 height values, each 1.0, as 2 byte integers
        assertEquals(resultStream.readInt(), 1, "Incorrect height at 0,0");
        assertEquals(resultStream.readInt(), 2, "Incorrect height at 0,1");
        assertEquals(resultStream.readInt(), 0, "Incorrect height at 0,2");
        assertEquals(resultStream.readInt(), 3, "Incorrect height at 1,0");
        assertEquals(resultStream.readInt(), 4, "Incorrect height at 1,1");
        assertEquals(resultStream.readInt(), 0, "Incorrect height at 1,2");
        assertEquals(resultStream.readInt(), 0, "Incorrect height at 2,0");
        assertEquals(resultStream.readInt(), 0, "Incorrect height at 2,1");
        assertEquals(resultStream.readInt(), 0, "Incorrect height at 2,2");

        assertEquals(resultStream.read(), -1, "End of stream not found");
    }

    @Test(groups = "unit")
    public void test4ByteFloatExportGridOversize()
        throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_3;
        BTExporter classUnderTest = new BTExporter(TEST_VERSION);
        classUnderTest.exportTwoByteHeights(false);
        classUnderTest.exportFloatHeights(true);
        classUnderTest.setDataSource(exportTestSource);

        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        classUnderTest.setGridSize(3, 3);
        classUnderTest.export(output);

        byte[] data = output.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianDataInputStream resultStream = new LittleEndianDataInputStream(bis);

        // http://vterrain.org/Implementation/Formats/BT.html
        byte[] versionBytes = new byte[10];
        resultStream.read(versionBytes);

        String header = new String(versionBytes);
        assertEquals(header, "binterr1.3", "Incorrect version number written");

        assertEquals(resultStream.readInt(), 3, "Wrong column count written");
        assertEquals(resultStream.readInt(), 3, "Wrong row count written");
        assertEquals(resultStream.readShort(), 4, "Should have 2 byte heights");
        assertEquals(resultStream.readShort(), 1, "Should have float heights");

        // skip through the rest of the header here.
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readShort();
        resultStream.readFloat();

        // make sure all this buffer is padding zeroes
        for(int i = 0; i < 190; i++)
        {
            resultStream.readByte();
        }

        // should be 4 height values, each 1.0, as 2 byte integers
        assertEquals(resultStream.readFloat(), 1.0f, "Incorrect height at 0,0");
        assertEquals(resultStream.readFloat(), 2.0f, "Incorrect height at 0,1");
        assertEquals(resultStream.readFloat(), 0.0f, "Incorrect height at 0,2");
        assertEquals(resultStream.readFloat(), 3.0f, "Incorrect height at 1,0");
        assertEquals(resultStream.readFloat(), 4.0f, "Incorrect height at 1,1");
        assertEquals(resultStream.readFloat(), 0.0f, "Incorrect height at 1,2");
        assertEquals(resultStream.readFloat(), 0.0f, "Incorrect height at 2,0");
        assertEquals(resultStream.readFloat(), 0.0f, "Incorrect height at 2,1");
        assertEquals(resultStream.readFloat(), 0.0f, "Incorrect height at 2,2");

        assertEquals(resultStream.read(), -1, "End of stream not found");
    }

    @Test(groups = "unit")
    public void test4ByteIntegerExport()
        throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_3;
        BTExporter classUnderTest = new BTExporter(TEST_VERSION);
        classUnderTest.exportTwoByteHeights(false);
        classUnderTest.exportFloatHeights(false);
        classUnderTest.setDataSource(exportTestSource);

        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        classUnderTest.export(output);

        byte[] data = output.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianDataInputStream resultStream = new LittleEndianDataInputStream(bis);

        // http://vterrain.org/Implementation/Formats/BT.html
        byte[] versionBytes = new byte[10];
        resultStream.read(versionBytes);

        String header = new String(versionBytes);
        assertEquals(header, "binterr1.3", "Incorrect version number written");

        assertEquals(resultStream.readInt(), 2, "Wrong column count written");
        assertEquals(resultStream.readInt(), 2, "Wrong row count written");
        assertEquals(resultStream.readShort(), 4, "Should have 4 byte heights");
        assertEquals(resultStream.readShort(), 0, "Should have integer heights");

        // skip through the rest of the header here.
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readShort();
        resultStream.readFloat();

        // make sure all this buffer is padding zeroes
        for(int i = 0; i < 190; i++)
        {
            resultStream.readByte();
        }

        // should be 4 height values, each 1.0, as 2 byte integers
        assertEquals(resultStream.readInt(), 1, "Incorrect height at 0,0");
        assertEquals(resultStream.readInt(), 2, "Incorrect height at 0,1");
        assertEquals(resultStream.readInt(), 3, "Incorrect height at 1,0");
        assertEquals(resultStream.readInt(), 4, "Incorrect height at 1,1");

        assertEquals(resultStream.read(), -1, "End of stream not found");
    }

    @Test(groups = "unit")
    public void test4ByteFloatExport()
        throws Exception
    {
        final BTVersion TEST_VERSION = BTVersion.VERSION_1_3;
        BTExporter classUnderTest = new BTExporter(TEST_VERSION);
        classUnderTest.exportTwoByteHeights(false);
        classUnderTest.exportFloatHeights(true);
        classUnderTest.setDataSource(exportTestSource);

        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        classUnderTest.export(output);

        byte[] data = output.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianDataInputStream resultStream = new LittleEndianDataInputStream(bis);

        // http://vterrain.org/Implementation/Formats/BT.html
        byte[] versionBytes = new byte[10];
        resultStream.read(versionBytes);

        String header = new String(versionBytes);
        assertEquals(header, "binterr1.3", "Incorrect version number written");

        assertEquals(resultStream.readInt(), 2, "Wrong column count written");
        assertEquals(resultStream.readInt(), 2, "Wrong row count written");
        assertEquals(resultStream.readShort(), 4, "Should have 4 byte heights");
        assertEquals(resultStream.readShort(), 1, "Should have float heights");

        // skip through the rest of the header here.
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readShort();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readDouble();
        resultStream.readShort();
        resultStream.readFloat();

        // make sure all this buffer is padding zeroes
        for(int i = 0; i < 190; i++)
        {
            resultStream.readByte();
        }

        // should be 4 height values, each 1.0, as 2 byte integers
        assertEquals(resultStream.readFloat(), 1.0f, "Incorrect height at 0,0");
        assertEquals(resultStream.readFloat(), 2.0f, "Incorrect height at 0,1");
        assertEquals(resultStream.readFloat(), 3.0f, "Incorrect height at 1,0");
        assertEquals(resultStream.readFloat(), 4.0f, "Incorrect height at 1,1");

        assertEquals(resultStream.read(), -1, "End of stream not found");
    }

    @DataProvider(name = "extents handling")
    public Object[][] generateExtentsHandlingData()
    {
        Object[][] ret_val = {
            { HeightMapSourceOrigin.CENTER,       -0.5,  0.5, -0.5, 0.5 },
            { HeightMapSourceOrigin.BOTTOM_LEFT,   0.0,  1.0,  0.0, 1.0 },
            { HeightMapSourceOrigin.BOTTOM_RIGHT, -1.0,  0.0,  0.0, 1.0 },
            { HeightMapSourceOrigin.TOP_LEFT,      0.0,  1.0, -1.0, 0.0 },
            { HeightMapSourceOrigin.TOP_RIGHT,    -1.0,  0.0, -1.0, 0.0 }
        };

        return ret_val;
    }
}

