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

import java.io.File;
import java.io.FileInputStream;

import org.j3d.loaders.UnsupportedFormatException;
import org.j3d.loaders.stl.STLASCIIParserTest;
import org.j3d.util.DataUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the VTerrain file format parser
 *
 * @author justin
 */
public class BTParserTest
{

    @Test(groups = "unit")
    public void testSimpleParse() throws Exception
    {

    }

    @Test(groups = "unit", expectedExceptions = UnsupportedFormatException.class)
    public void testInvalidFileHeader() throws Exception
    {
        File source = DataUtils.lookForFile("stl/ascii/missing_endfacet.stl", STLASCIIParserTest.class, null);
        FileInputStream fis = new FileInputStream(source);

        BTParser classUnderTest = new BTParser(fis);
        classUnderTest.parse();
    }
}
