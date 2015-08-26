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

package org.j3d.util;

import org.testng.annotations.Test;
import org.xml.sax.InputSource;

import static org.testng.Assert.*;

/**
 * Unit tests for the entity resolved
 *
 * @author justin
 */
public class SAXEntityResolverTest
{
    @Test(groups = "unit")
    public void testResolveSystemID() throws Exception
    {
        final String TEST_SYSTEM_ID = "http://www.j3d.org/dummy/basic.dtd";

        SAXEntityResolver class_under_test = new SAXEntityResolver();
        InputSource result = class_under_test.resolveEntity(null, TEST_SYSTEM_ID);

        assertNull(result, "Should not have found DTD in the root directory");

        class_under_test.setDTDRootDirectory("dtd");

        result = class_under_test.resolveEntity(null, TEST_SYSTEM_ID);

        assertNotNull(result, "Should have found DTD after setting main directory");
    }
}
