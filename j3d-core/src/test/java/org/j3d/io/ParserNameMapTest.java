/*
 * j3d.org Copyright (c) 2001-2014
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.io;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit test for the parse name map util class
 *
 * @author justin
 */
public class ParserNameMapTest
{
    @Test(groups = "unit")
    public void testBasicOperation() throws Exception
    {
        final String TEST_EXTENSION = "ext";
        final String TEST_MIME = "test/ext";

        ParserNameMap classUnderTest = new ParserNameMap();
        classUnderTest.registerType(TEST_EXTENSION, TEST_MIME);

        assertEquals(classUnderTest.getFileExtension(TEST_MIME), TEST_EXTENSION, "Wrong extension found");
        assertNull(classUnderTest.getFileExtension("text/csv"), "Unregistered MIME should not give extenions");

        assertEquals(classUnderTest.getContentTypeFor("SomeFile." + TEST_EXTENSION), TEST_MIME, "Wrong mime found");
        assertNull(classUnderTest.getContentTypeFor("SomeFile.csv"),
                   "Should not have found an MIME type for an unregistered type");
    }

    @Test(groups = "unit")
    public void testNoFileExtension() throws Exception
    {
        ParserNameMap classUnderTest = new ParserNameMap();

        assertNull(classUnderTest.getContentTypeFor("FileNameWithNoDot"), "Should not crash on no extension");
    }
}
