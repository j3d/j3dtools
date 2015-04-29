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

package org.j3d.loaders.obj;

import java.io.File;
import java.net.URL;

import org.j3d.geom.GeometryData;
import org.j3d.util.DataUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * This class does something
 *
 * @author justin
 */
public class OBJASCIIParserTest
{
    @Test(groups = "unit")
    public void testSimpleParse() throws Exception
    {
        File source = DataUtils.lookForFile("obj/single_triangle.obj", OBJASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        OBJASCIIParser classUnderTest = new OBJASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        GeometryData result = classUnderTest.getNextObject();

        assertNotNull(result, "Didn't find the basic object");

        assertEquals(result.geometryType, GeometryData.INDEXED_TRIANGLES, "Wrong basic data type found");
        assertEquals(result.geometryComponents, 0, "Supplimentary data found");
        assertEquals(result.vertexCount, 3, "Wrong number of vertices found");
        assertEquals(result.indexesCount, 3, "Wrong number of indices found");
        assertNull(result.normals, "Normal array was allocated");
        assertNull(result.textureCoordinates, "Tex coord array was allocated");

        result = classUnderTest.getNextObject();

        assertNull(result, "Should not find a second object");

        classUnderTest.close();
    }

    @Test(groups = "unit")
    public void testParseWithNormals() throws Exception
    {
        File source = DataUtils.lookForFile("obj/single_triangle_with_normals.obj", OBJASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        OBJASCIIParser classUnderTest = new OBJASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        GeometryData result = classUnderTest.getNextObject();

        assertNotNull(result, "Didn't find the basic object");

        assertEquals(result.geometryType, GeometryData.INDEXED_TRIANGLES, "Wrong basic data type found");
        assertEquals(result.geometryComponents, GeometryData.NORMAL_DATA, "Didn't tag the normal components");
        assertEquals(result.vertexCount, 3, "Wrong number of vertices found");
        assertEquals(result.indexesCount, 3, "Wrong number of indices found");
        assertNotNull(result.normals, "Normal array was not created");
        assertNull(result.textureCoordinates, "Texture coordinate array should not be created");

        result = classUnderTest.getNextObject();

        assertNull(result, "Should not find a second object");

        classUnderTest.close();
    }

    @Test(groups = "unit")
    public void testParseWithTextures() throws Exception
    {
        File source = DataUtils.lookForFile("obj/single_triangle_with_texcoords.obj", OBJASCIIParserTest.class, null);
        URL testURL = source.toURI().toURL();

        OBJASCIIParser classUnderTest = new OBJASCIIParser();
        assertTrue(classUnderTest.parse(testURL), "Unable to parse the basic test file");

        GeometryData result = classUnderTest.getNextObject();

        assertNotNull(result, "Didn't find the basic object");

        assertEquals(result.geometryType, GeometryData.INDEXED_TRIANGLES, "Wrong basic data type found");
        assertEquals(result.geometryComponents, GeometryData.TEXTURE_2D_DATA, "Didn't tag the texture components");
        assertEquals(result.vertexCount, 3, "Wrong number of vertices found");
        assertEquals(result.indexesCount, 3, "Wrong number of indices found");
        assertNull(result.normals, "Normal array should not be created");
        assertNotNull(result.textureCoordinates, "Texture coordinate array was not created");

        result = classUnderTest.getNextObject();

        assertNull(result, "Should not find a second object");

        classUnderTest.close();
    }
}
