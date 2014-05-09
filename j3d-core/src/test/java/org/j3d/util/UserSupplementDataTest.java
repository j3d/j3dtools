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

package org.j3d.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class UserSupplementDataTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        UserSupplementData classUnderTest = new UserSupplementData();

        assertTrue(classUnderTest.collidable, "Default collidable flag wrong");
        assertTrue(classUnderTest.isTerrain, "Default terrain flag wrong");
        assertNull(classUnderTest.geometryData, "Default has geometry data set");
        assertNull(classUnderTest.userData, "Default has user data set");
    }
}
