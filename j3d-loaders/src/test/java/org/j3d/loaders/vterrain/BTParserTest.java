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

package org.j3d.loaders.vterrain;

import java.io.*;

import org.j3d.exporters.vterrain.BTDatumCode;
import org.j3d.exporters.vterrain.BTExporter;
import org.j3d.exporters.vterrain.BTVersion;
import org.j3d.loaders.HeightMapSource;
import org.j3d.loaders.HeightMapSourceOrigin;
import org.j3d.loaders.UnsupportedFormatException;
import org.j3d.util.DataUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the VTerrain file format parser. These create test input files
 * using the exporter available in this module. Assumption is that the exporter
 * is doing the correct thing.
 *
 * @author justin
 */
public class BTParserTest
{
    final HeightMapSource standardTestDataSource = new HeightMapSource()
    {
        @Override
        public float[][] getHeights()
        {
            return new float[][] {
                { 1.0f, 2.0f },
                { 3.0f, 4.0f }
            };
        }

        @Override
        public float[] getGridStep()
        {
            return new float[] { 1.0f, 2.0f };
        }

        @Override
        public HeightMapSourceOrigin getOriginLocation()
        {
            return HeightMapSourceOrigin.BOTTOM_LEFT;
        }
    };

    @Test(groups = "unit")
    public void testClear() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BTExporter exporter = new BTExporter(BTVersion.VERSION_1_0);
        exporter.export(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BTParser classUnderTest = new BTParser(bis);

        assertNotNull(classUnderTest.parse(), "No results returned");
        assertNotNull(classUnderTest.getHeader(), "No header found");

        classUnderTest.clear();

        assertNull(classUnderTest.getHeader(), "Header was not cleared");
        assertNull(classUnderTest.getHeights(), "Heighfield was not cleared");

        // Should be able to parse again without throwing an exception
        classUnderTest.reset(new ByteArrayInputStream(bos.toByteArray()));
        assertNotNull(classUnderTest.parse(), "Was not able to parse after clearing");
    }

    @Test(groups = "unit", expectedExceptions = IOException.class)
    public void testDoubleParseWithNoClear() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BTExporter exporter = new BTExporter(BTVersion.VERSION_1_0);
        exporter.export(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BTParser classUnderTest = new BTParser(bis);

        assertNotNull(classUnderTest.parse(), "No results returned");
        assertNotNull(classUnderTest.getHeader(), "No header found");

        classUnderTest.parse();
    }

    @Test(groups = "unit")
    public void testSimpleParseV1_0() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BTExporter exporter = new BTExporter(BTVersion.VERSION_1_0);
        exporter.export(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BTParser classUnderTest = new BTParser(bis);
        float[][] result = classUnderTest.parse();
        BTHeader resultHeader = classUnderTest.getHeader();

        assertNotNull(resultHeader, "No header found after parsing");
        assertNotNull(result, "Result array should be provided even for empty file");
        assertEquals(result.length, 0, "Should be no columns returned");
        assertEquals(resultHeader.version, BTVersion.VERSION_1_0, "Incorrect version found");
        assertFalse(resultHeader.utmProjection, "Should not find UTM by default");
        assertFalse(resultHeader.needsExternalProj, "No external projection file needed");
        assertEquals(resultHeader.rows, 0, "Should have no row count in header");
        assertEquals(resultHeader.columns, 0, "Should have no column count in header");
        assertEquals(resultHeader.datum, BTDatumCode.NO_DATUM.getCode(), "No datum should have been defined");
        assertEquals(resultHeader.leftExtent, 0.0, "Should have no left extent");
        assertEquals(resultHeader.rightExtent, 0.0, "Should have no right extent");
        assertEquals(resultHeader.bottomExtent, 0.0, "Should have no bottom extent");
        assertEquals(resultHeader.topExtent, 0.0, "Should have no top extent");
        assertEquals(classUnderTest.getOriginLocation(), HeightMapSourceOrigin.BOTTOM_LEFT, "Wrong origin provided");
    }

    @Test(groups = "unit")
    public void testSimpleParseV1_1() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BTExporter exporter = new BTExporter(BTVersion.VERSION_1_1);
        exporter.export(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BTParser classUnderTest = new BTParser(bis);
        float[][] result = classUnderTest.parse();
        BTHeader resultHeader = classUnderTest.getHeader();

        assertNotNull(resultHeader, "No header found after parsing");
        assertNotNull(result, "Result array should be provided even for empty file");
        assertEquals(result.length, 0, "Should be no columns returned");
        assertEquals(resultHeader.version, BTVersion.VERSION_1_1, "Incorrect version found");
        assertFalse(resultHeader.utmProjection, "Should not find UTM by default");
        assertFalse(resultHeader.needsExternalProj, "No external projection file needed");
        assertEquals(resultHeader.rows, 0, "Should have no row count in header");
        assertEquals(resultHeader.columns, 0, "Should have no column count in header");
        assertEquals(resultHeader.datum, BTDatumCode.NO_DATUM.getCode(), "No datum should have been defined");
        assertEquals(resultHeader.leftExtent, 0.0, "Should have no left extent");
        assertEquals(resultHeader.rightExtent, 0.0, "Should have no right extent");
        assertEquals(resultHeader.bottomExtent, 0.0, "Should have no bottom extent");
        assertEquals(resultHeader.topExtent, 0.0, "Should have no top extent");
    }

    @Test(groups = "unit")
    public void testSimpleParseV1_2() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BTExporter exporter = new BTExporter(BTVersion.VERSION_1_2);
        exporter.export(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BTParser classUnderTest = new BTParser(bis);
        float[][] result = classUnderTest.parse();
        BTHeader resultHeader = classUnderTest.getHeader();

        assertNotNull(resultHeader, "No header found after parsing");
        assertNotNull(result, "Result array should be provided even for empty file");
        assertEquals(result.length, 0, "Should be no columns returned");
        assertEquals(resultHeader.version, BTVersion.VERSION_1_2, "Incorrect version found");
        assertFalse(resultHeader.utmProjection, "Should not find UTM by default");
        assertFalse(resultHeader.needsExternalProj, "No external projection file needed");
        assertEquals(resultHeader.rows, 0, "Should have no row count in header");
        assertEquals(resultHeader.columns, 0, "Should have no column count in header");
        assertEquals(resultHeader.datum, BTDatumCode.NO_DATUM.getCode(), "No datum should have been defined");
        assertEquals(resultHeader.leftExtent, 0.0, "Should have no left extent");
        assertEquals(resultHeader.rightExtent, 0.0, "Should have no right extent");
        assertEquals(resultHeader.bottomExtent, 0.0, "Should have no bottom extent");
        assertEquals(resultHeader.topExtent, 0.0, "Should have no top extent");
    }

    @Test(groups = "unit")
    public void testSimpleParseV1_3() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BTExporter exporter = new BTExporter(BTVersion.VERSION_1_3);
        exporter.export(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BTParser classUnderTest = new BTParser(bis);
        float[][] result = classUnderTest.parse();
        BTHeader resultHeader = classUnderTest.getHeader();

        assertNotNull(resultHeader, "No header found after parsing");
        assertNotNull(result, "Result array should be provided even for empty file");
        assertEquals(result.length, 0, "Should be no columns returned");
        assertEquals(resultHeader.version, BTVersion.VERSION_1_3, "Incorrect version found");
        assertFalse(resultHeader.utmProjection, "Should not find UTM by default");
        assertFalse(resultHeader.needsExternalProj, "No external projection file needed");
        assertEquals(resultHeader.rows, 0, "Should have no row count in header");
        assertEquals(resultHeader.columns, 0, "Should have no column count in header");
        assertEquals(resultHeader.datum, BTDatumCode.NO_DATUM.getCode(), "No datum should have been defined");
        assertEquals(resultHeader.leftExtent, 0.0, "Should have no left extent");
        assertEquals(resultHeader.rightExtent, 0.0, "Should have no right extent");
        assertEquals(resultHeader.bottomExtent, 0.0, "Should have no bottom extent");
        assertEquals(resultHeader.topExtent, 0.0, "Should have no top extent");
    }

    @Test(groups = "unit")
    public void testSimpleParseShortIntHeights() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BTExporter exporter = new BTExporter(BTVersion.VERSION_1_3);
        exporter.exportFloatHeights(false);
        exporter.exportTwoByteHeights(true);
        exporter.setDataSource(standardTestDataSource);
        exporter.export(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BTParser classUnderTest = new BTParser(bis);
        float[][] result = classUnderTest.parse();
        BTHeader resultHeader = classUnderTest.getHeader();

        assertNotNull(resultHeader, "No header found after parsing");
        assertNotNull(result, "Result array should be provided even for empty file");
        assertEquals(result.length, 2, "Wrong column count in parsed heights");
        assertEquals(result[0].length, 2, "Wrong row count in parsed heights column 0");
        assertEquals(result[1].length, 2, "Wrong row count in parsed heights column 1");
        assertEquals(resultHeader.version, BTVersion.VERSION_1_3, "Incorrect version found");
        assertFalse(resultHeader.utmProjection, "Should not find UTM by default");
        assertFalse(resultHeader.needsExternalProj, "No external projection file needed");
        assertEquals(resultHeader.rows, 2, "Incorrect row count in header");
        assertEquals(resultHeader.columns, 2, "Incorrect column count in header");
        assertEquals(resultHeader.datum, BTDatumCode.NO_DATUM.getCode(), "No datum should have been defined");
        assertEquals(resultHeader.leftExtent, 0.0, "Incorrect left extent");
        assertEquals(resultHeader.rightExtent, standardTestDataSource.getGridStep()[0], 0.01, "Incorrect right extent");
        assertEquals(resultHeader.bottomExtent, 0.0, "Incorrect bottom extent");
        assertEquals(resultHeader.topExtent, standardTestDataSource.getGridStep()[1], 0.01, "Incorrect top extent");

        float[][] source_heights = standardTestDataSource.getHeights();

        for(int i = 0; i < source_heights.length; i++)
        {
            for(int j = 0; j < source_heights[i].length; j++)
            {
                assertEquals(result[i][j], source_heights[i][j], 0.001, "Incorrect height saved at " + i + "," + j);
            }
        }
    }

    @Test(groups = "unit")
    public void testSimpleParseNormalIntHeights() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BTExporter exporter = new BTExporter(BTVersion.VERSION_1_3);
        exporter.exportFloatHeights(false);
        exporter.exportTwoByteHeights(false);
        exporter.setDataSource(standardTestDataSource);
        exporter.export(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BTParser classUnderTest = new BTParser(bis);
        float[][] result = classUnderTest.parse();
        BTHeader resultHeader = classUnderTest.getHeader();

        assertNotNull(resultHeader, "No header found after parsing");
        assertNotNull(result, "Result array should be provided even for empty file");
        assertEquals(result.length, 2, "Wrong column count in parsed heights");
        assertEquals(result[0].length, 2, "Wrong row count in parsed heights column 0");
        assertEquals(result[1].length, 2, "Wrong row count in parsed heights column 1");
        assertEquals(resultHeader.version, BTVersion.VERSION_1_3, "Incorrect version found");
        assertFalse(resultHeader.utmProjection, "Should not find UTM by default");
        assertFalse(resultHeader.needsExternalProj, "No external projection file needed");
        assertEquals(resultHeader.rows, 2, "Incorrect row count in header");
        assertEquals(resultHeader.columns, 2, "Incorrect column count in header");
        assertEquals(resultHeader.datum, BTDatumCode.NO_DATUM.getCode(), "No datum should have been defined");
        assertEquals(resultHeader.leftExtent, 0.0, "Incorrect left extent");
        assertEquals(resultHeader.rightExtent, standardTestDataSource.getGridStep()[0], 0.01, "Incorrect right extent");
        assertEquals(resultHeader.bottomExtent, 0.0, "Incorrect bottom extent");
        assertEquals(resultHeader.topExtent, standardTestDataSource.getGridStep()[1], 0.01, "Incorrect top extent");

        float[][] source_heights = standardTestDataSource.getHeights();

        for(int i = 0; i < source_heights.length; i++)
        {
            for(int j = 0; j < source_heights[i].length; j++)
            {
                assertEquals(result[i][j], source_heights[i][j], 0.001, "Incorrect height saved at " + i + "," + j);
            }
        }
    }

    @Test(groups = "unit")
    public void testSimpleParseNormalFloatHeights() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BTExporter exporter = new BTExporter(BTVersion.VERSION_1_3);
        exporter.exportFloatHeights(true);
        exporter.exportTwoByteHeights(false);
        exporter.setDataSource(standardTestDataSource);
        exporter.export(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BTParser classUnderTest = new BTParser(bis);
        float[][] result = classUnderTest.parse();
        BTHeader resultHeader = classUnderTest.getHeader();

        assertNotNull(resultHeader, "No header found after parsing");
        assertNotNull(result, "Result array should be provided even for empty file");
        assertEquals(result.length, 2, "Wrong column count in parsed heights");
        assertEquals(result[0].length, 2, "Wrong row count in parsed heights column 0");
        assertEquals(result[1].length, 2, "Wrong row count in parsed heights column 1");
        assertEquals(resultHeader.version, BTVersion.VERSION_1_3, "Incorrect version found");
        assertFalse(resultHeader.utmProjection, "Should not find UTM by default");
        assertFalse(resultHeader.needsExternalProj, "No external projection file needed");
        assertEquals(resultHeader.rows, 2, "Incorrect row count in header");
        assertEquals(resultHeader.columns, 2, "Incorrect column count in header");
        assertEquals(resultHeader.datum, BTDatumCode.NO_DATUM.getCode(), "No datum should have been defined");
        assertEquals(resultHeader.leftExtent, 0.0, "Incorrect left extent");
        assertEquals(resultHeader.rightExtent, standardTestDataSource.getGridStep()[0], 0.01, "Incorrect right extent");
        assertEquals(resultHeader.bottomExtent, 0.0, "Incorrect bottom extent");
        assertEquals(resultHeader.topExtent, standardTestDataSource.getGridStep()[1], 0.01, "Incorrect top extent");

        float[][] source_heights = standardTestDataSource.getHeights();

        for(int i = 0; i < source_heights.length; i++)
        {
            for(int j = 0; j < source_heights[i].length; j++)
            {
                assertEquals(result[i][j], source_heights[i][j], 0.001, "Incorrect height saved at " + i + "," + j);
            }
        }
    }

    @Test(groups = "unit")
    public void testHeightFetchingSame() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BTExporter exporter = new BTExporter(BTVersion.VERSION_1_3);
        exporter.exportFloatHeights(true);
        exporter.exportTwoByteHeights(false);
        exporter.setDataSource(standardTestDataSource);
        exporter.export(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BTParser classUnderTest = new BTParser(bis);
        float[][] result = classUnderTest.parse();
        float[][] alt_result = classUnderTest.getHeights();
        float[] grid_result = classUnderTest.getGridStep();

        BTHeader resultHeader = classUnderTest.getHeader();

        assertEquals(grid_result[0], resultHeader.rightExtent - resultHeader.leftExtent, 0.01, "Width grid incorrect");
        assertEquals(grid_result[1], resultHeader.topExtent - resultHeader.bottomExtent, 0.01, "Height grid incorrect");

        for(int i = 0; i < result.length; i++)
        {
            for(int j = 0; j < result[i].length; j++)
            {
                assertEquals(alt_result[i][j], result[i][j], 0.001, "Height mismatch at " + i + "," + j);
            }
        }
    }

    @Test(groups = "unit")
    public void testHeightScaling() throws Exception
    {
        final float TEST_SCALE = 0.75f;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BTExporter exporter = new BTExporter(BTVersion.VERSION_1_3);
        exporter.exportFloatHeights(true);
        exporter.exportTwoByteHeights(false);
        exporter.setDataSource(standardTestDataSource);
        exporter.setVerticalScale(TEST_SCALE);
        exporter.export(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BTParser classUnderTest = new BTParser(bis);
        float[][] result = classUnderTest.parse();
        BTHeader resultHeader = classUnderTest.getHeader();

        assertNotNull(resultHeader, "No header found after parsing");
        assertNotNull(result, "Result array should be provided even for empty file");
        assertEquals(result.length, 2, "Wrong column count in parsed heights");
        assertEquals(result[0].length, 2, "Wrong row count in parsed heights column 0");
        assertEquals(result[1].length, 2, "Wrong row count in parsed heights column 1");
        assertEquals(resultHeader.version, BTVersion.VERSION_1_3, "Incorrect version found");
        assertFalse(resultHeader.utmProjection, "Should not find UTM by default");
        assertFalse(resultHeader.needsExternalProj, "No external projection file needed");
        assertEquals(resultHeader.rows, 2, "Incorrect row count in header");
        assertEquals(resultHeader.columns, 2, "Incorrect column count in header");
        assertEquals(resultHeader.datum, BTDatumCode.NO_DATUM.getCode(), "No datum should have been defined");
        assertEquals(resultHeader.leftExtent, 0.0, "Incorrect left extent");
        assertEquals(resultHeader.rightExtent, standardTestDataSource.getGridStep()[0], 0.01, "Incorrect right extent");
        assertEquals(resultHeader.bottomExtent, 0.0, "Incorrect bottom extent");
        assertEquals(resultHeader.topExtent, standardTestDataSource.getGridStep()[1], 0.01, "Incorrect top extent");

        float[][] source_heights = standardTestDataSource.getHeights();

        for(int i = 0; i < source_heights.length; i++)
        {
            for(int j = 0; j < source_heights[i].length; j++)
            {
                assertEquals(result[i][j], source_heights[i][j] * TEST_SCALE, 0.001,
                             "Incorrect scaled height saved at " + i + "," + j);
            }
        }
    }

    @Test(groups = "unit", expectedExceptions = UnsupportedFormatException.class)
    public void testInvalidFileHeader() throws Exception
    {
        File source = DataUtils.lookForFile("vterrain/invalid_header.bt", BTParserTest.class, null);
        FileInputStream fis = new FileInputStream(source);

        BTParser classUnderTest = new BTParser(fis);
        classUnderTest.parse();
    }
}
