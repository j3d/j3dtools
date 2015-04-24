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

package org.j3d.loaders.stl;

import java.io.File;
import java.net.URL;

import org.j3d.loaders.InvalidFormatException;
import org.j3d.util.DataUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the ASCII file parser for STL
 *
 * @author justin
 */
public class STLASCIIParserTest
{
    @Test(groups = "unit")
    public void testSimpleParse() throws Exception
    {
        File source = DataUtils.lookForFile("stl/ascii/single_facet.stl", STLASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        STLASCIIParser classUnderTest = new STLASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        double[] resultNormal = new double[3];
        double[][] resultCoords = new double[3][3];

        assertTrue(classUnderTest.getNextFacet(resultNormal, resultCoords), "Unable to read the first triangle");

        assertEquals(resultNormal[0], 0.0, "Incorrect normal X component read");
        assertEquals(resultNormal[1], 0.0, "Incorrect normal Y component read");
        assertEquals(resultNormal[2], -1.0, "Incorrect normal Z component read");

        assertFalse(classUnderTest.getNextFacet(resultNormal, resultCoords), "Found additional triangles");

        classUnderTest.close();
    }

    @Test(groups = "unit")
    public void testAlternateEndSolid() throws Exception
    {
        File source = DataUtils.lookForFile("stl/ascii/end_solid.stl", STLASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        STLASCIIParser classUnderTest = new STLASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        double[] resultNormal = new double[3];
        double[][] resultCoords = new double[3][3];

        assertTrue(classUnderTest.getNextFacet(resultNormal, resultCoords), "Unable to read the first triangle");

        assertEquals(resultNormal[0], 0.0, "Incorrect normal X component read");
        assertEquals(resultNormal[1], 0.0, "Incorrect normal Y component read");
        assertEquals(resultNormal[2], -1.0, "Incorrect normal Z component read");

        assertFalse(classUnderTest.getNextFacet(resultNormal, resultCoords), "Found additional triangles");

        classUnderTest.close();
    }

    @Test(groups = "unit", expectedExceptions = InvalidFormatException.class)
    public void testMissingEndFacet() throws Exception
    {
        File source = DataUtils.lookForFile("stl/ascii/missing_endfacet.stl", STLASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        STLASCIIParser classUnderTest = new STLASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        double[] resultNormal = new double[3];
        double[][] resultCoords = new double[3][3];

        classUnderTest.getNextFacet(resultNormal, resultCoords);
    }

    @Test(groups = "unit", expectedExceptions = InvalidFormatException.class)
    public void testMissingEndSolid() throws Exception
    {
        File source = DataUtils.lookForFile("stl/ascii/missing_endsolid.stl", STLASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        STLASCIIParser classUnderTest = new STLASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        double[] resultNormal = new double[3];
        double[][] resultCoords = new double[3][3];

        classUnderTest.getNextFacet(resultNormal, resultCoords);
    }

    @Test(groups = "unit", expectedExceptions = InvalidFormatException.class)
    public void testMissingFacet() throws Exception
    {
        File source = DataUtils.lookForFile("stl/ascii/missing_facet.stl", STLASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        STLASCIIParser classUnderTest = new STLASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        double[] resultNormal = new double[3];
        double[][] resultCoords = new double[3][3];

        classUnderTest.getNextFacet(resultNormal, resultCoords);
    }
    @Test(groups = "unit", expectedExceptions = InvalidFormatException.class)
    public void testMissingLoop() throws Exception
    {
        File source = DataUtils.lookForFile("stl/ascii/missing_loop.stl", STLASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        STLASCIIParser classUnderTest = new STLASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        double[] resultNormal = new double[3];
        double[][] resultCoords = new double[3][3];

        classUnderTest.getNextFacet(resultNormal, resultCoords);
    }
    @Test(groups = "unit", expectedExceptions = InvalidFormatException.class)
    public void testMissingNormal() throws Exception
    {
        File source = DataUtils.lookForFile("stl/ascii/missing_normal.stl", STLASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        STLASCIIParser classUnderTest = new STLASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        double[] resultNormal = new double[3];
        double[][] resultCoords = new double[3][3];

        classUnderTest.getNextFacet(resultNormal, resultCoords);
    }
    @Test(groups = "unit", expectedExceptions = InvalidFormatException.class)
    public void testMissingOuter() throws Exception
    {
        File source = DataUtils.lookForFile("stl/ascii/missing_outer.stl", STLASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        STLASCIIParser classUnderTest = new STLASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        double[] resultNormal = new double[3];
        double[][] resultCoords = new double[3][3];

        classUnderTest.getNextFacet(resultNormal, resultCoords);
    }
    @Test(groups = "unit", expectedExceptions = InvalidFormatException.class)
    public void testMissingVertex() throws Exception
    {
        File source = DataUtils.lookForFile("stl/ascii/missing_vertex.stl", STLASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        STLASCIIParser classUnderTest = new STLASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        double[] resultNormal = new double[3];
        double[][] resultCoords = new double[3][3];

        classUnderTest.getNextFacet(resultNormal, resultCoords);
    }

    @Test(groups = "unit", expectedExceptions = InvalidFormatException.class)
    public void testExtraVertex() throws Exception
    {
        File source = DataUtils.lookForFile("stl/ascii/extra_vertex.stl", STLASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        STLASCIIParser classUnderTest = new STLASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        double[] resultNormal = new double[3];
        double[][] resultCoords = new double[3][3];

        classUnderTest.getNextFacet(resultNormal, resultCoords);
    }
}
