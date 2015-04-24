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

import static org.testng.Assert.*;

/**
 * Unit tests for the data fetch utils
 *
 * @author justin
 */
public class DataUtilsTest
{

    @Test(groups = "unit")
    public void testLookForFileInClasspath() throws Exception
    {
        assertNotNull(DataUtils.lookForFile("images/test_image.png", DataUtils.class, "incorrect"));
    }

    @Test(groups = "unit")
    public void testLookForResource() throws Exception
    {
        assertNotNull(DataUtils.lookForResource("images/test_image.png", DataUtils.class));
    }
}
