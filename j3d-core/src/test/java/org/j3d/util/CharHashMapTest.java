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

public class CharHashMapTest
{

    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        CharHashMap<Object> classUnderTest = new CharHashMap<>();

        assertEquals(classUnderTest.size(), 0, "Default map should not contain any entries");
        assertTrue(classUnderTest.isEmpty(), "Default map should be empty");
        assertFalse(classUnderTest.contains('a'), "Should not contain any element");
        assertFalse(classUnderTest.containsKey('a'), "Should not contain any key");
        assertNull(classUnderTest.remove('a'), "Cannot remove successfully an entry it does not contain");
        assertNull(classUnderTest.get('a'), "Cannot get an entry it does not contain");
    }

    @Test(groups = "unit")
    public void testPutAndRemove() throws Exception
    {
        final Object TEST_VALUE = new Object();
        final char TEST_KEY = 'f';

        CharHashMap<Object> classUnderTest = new CharHashMap<>();

        classUnderTest.put(TEST_KEY, TEST_VALUE);

        assertEquals(classUnderTest.size(), 1, "Did not correctly add an entry");
        assertFalse(classUnderTest.isEmpty(), "Map should not be empty after adding");
        assertTrue(classUnderTest.containsKey(TEST_KEY), "Key was not found as valid");
        assertTrue(classUnderTest.contains(TEST_VALUE), "contains(Value) was not found in the map");
        assertTrue(classUnderTest.containsValue(TEST_VALUE), "containsValue(Value) was not found in the map");
        assertSame(classUnderTest.get(TEST_KEY), TEST_VALUE, "Fetching key didn't return the same reference");

        // Now remove it and make sure the map is empty
        assertSame(classUnderTest.remove(TEST_KEY), TEST_VALUE, "Removing key didn't return the same reference");

        assertEquals(classUnderTest.size(), 0, "Default map should not contain any entries");
        assertTrue(classUnderTest.isEmpty(), "Default map should be empty");
        assertFalse(classUnderTest.contains(TEST_VALUE), "Should not contain the value after removal");
        assertFalse(classUnderTest.containsKey(TEST_KEY), "Should not contain the key after removal");
        assertNull(classUnderTest.remove(TEST_KEY), "Cannot remove an entry twice");
        assertNull(classUnderTest.get(TEST_KEY), "Cannot get an entry after it was removed");
    }
}
