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
 * Unit tests for the HashSet implementation
 *
 * @author justin
 */
public class HashSetTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        HashSet<Object> class_under_test = new HashSet<>();

        assertEquals(class_under_test.size(), 0, "Default map should not contain any entries");
        assertTrue(class_under_test.isEmpty(), "Default map should be empty");
        assertFalse(class_under_test.contains(0.6), "Should not contain any element");
        assertFalse(class_under_test.remove(0.6f), "Cannot remove successfully an entry it does not contain");
    }

    @Test(groups = "unit")
    public void testAddAndRemove() throws Exception
    {
        final float TEST_VALUE = 0.57f;

        HashSet<Object> class_under_test = new HashSet<>();

        class_under_test.add(TEST_VALUE);

        assertEquals(class_under_test.size(), 1, "Did not correctly add an entry");
        assertFalse(class_under_test.isEmpty(), "Map should not be empty after adding");
        assertTrue(class_under_test.contains(TEST_VALUE), "contains(Value) was not found in the map");

        // Now remove it and make sure the map is empty
        assertTrue(class_under_test.remove(TEST_VALUE), "Removing key didn't succeed");

        assertEquals(class_under_test.size(), 0, "Default map should not contain any entries");
        assertTrue(class_under_test.isEmpty(), "Default map should be empty");
        assertFalse(class_under_test.contains(TEST_VALUE), "Should not contain the key after removal");
        assertFalse(class_under_test.remove(TEST_VALUE), "Cannot remove an entry twice");
    }

}
