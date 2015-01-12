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

import java.util.Collections;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CircularListTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        CircularList classUnderTest = new CircularList();

        assertTrue(classUnderTest.isEmpty(), "Default construction is not empty");
        assertEquals(classUnderTest.size(), 0, "Default constructor doesn't have zero size");

        assertNull(classUnderTest.current(), "Got a non-null object from current()");
        assertNull(classUnderTest.previous(), "Got a non-null object from previous()");
        assertNull(classUnderTest.next(), "Got a non-null object from next()");

        assertFalse(classUnderTest.removeAll(Collections.EMPTY_SET), "Found something to remove from an empty set");
        assertFalse(classUnderTest.addAll(Collections.EMPTY_SET), "Found something to add from an empty set");

        assertFalse(classUnderTest.remove(null), "Found something to remove from a null single object");
        assertFalse(classUnderTest.contains(null), "Should not contain a null");
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testNPEOnEmptyRemove() throws Exception
    {
        CircularList classUnderTest = new CircularList();

        classUnderTest.removeAll(null);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testNPEOnEmptyAddSingle() throws Exception
    {
        CircularList classUnderTest = new CircularList();

        classUnderTest.add(null);
    }

    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void testNPEOnEmptyAddCollection() throws Exception
    {
        CircularList classUnderTest = new CircularList();

        classUnderTest.addAll(null);
    }

    @Test(groups = "unit", dependsOnMethods = "testBasicConstruction")
    public void testSingleListItem() throws Exception
    {
        Object testObject = new Object();

        CircularList<Object> classUnderTest = new CircularList<>();
        classUnderTest.add(testObject);

        assertFalse(classUnderTest.isEmpty(), "List is empty after adding an object");
        assertEquals(classUnderTest.size(), 1, "Incorrect size found");

        assertTrue(classUnderTest.contains(testObject), "Didn't contain the only test object");

        assertSame(classUnderTest.current(), testObject, "current() doesn't return the correct object instance");
        assertSame(classUnderTest.previous(), testObject, "previous() doesn't return the correct object instance");
        assertSame(classUnderTest.previous(), testObject, "Second previous() doesn't return the same object instance");

        assertSame(classUnderTest.next(), testObject, "next() doesn't return the correct object instance");
        assertSame(classUnderTest.next(), testObject, "Second next() doesn't return the same object instance");

        assertTrue(classUnderTest.remove(testObject), "Didn't remove the only test object");
        assertTrue(classUnderTest.isEmpty(), "Removal didn't set the list back to empty");
    }

    @Test(groups = "unit", dependsOnMethods = "testBasicConstruction")
    public void testMultiListItem() throws Exception
    {
        Object testObject1 = new Object();
        Object testObject2 = new Object();

        CircularList<Object> classUnderTest = new CircularList<>();
        classUnderTest.add(testObject1);
        classUnderTest.add(testObject2);

        assertFalse(classUnderTest.isEmpty(), "List is empty after adding objects");
        assertEquals(classUnderTest.size(), 2, "Incorrect size found");

        assertTrue(classUnderTest.contains(testObject1), "Didn't contain test object 1");
        assertTrue(classUnderTest.contains(testObject2), "Didn't contain test object 2");

        assertSame(classUnderTest.current(), testObject1, "current() doesn't return the first object instance added");
        assertSame(classUnderTest.previous(), testObject2, "previous() doesn't return the other object instance");
        assertSame(classUnderTest.previous(), testObject1, "Second previous() doesn't return the original object instance");

        assertSame(classUnderTest.next(), testObject2, "next() doesn't return the second object instance");
        assertSame(classUnderTest.next(), testObject1, "Second next() doesn't return the original object instance");

        assertTrue(classUnderTest.remove(testObject1), "Didn't remove the first test object");
        assertEquals(classUnderTest.size(), 1, "Removed more than one object");
        assertSame(classUnderTest.current(), testObject2, "current() after first remove is not the second object");

        assertTrue(classUnderTest.remove(testObject2), "Didn't remove the second test object");
        assertTrue(classUnderTest.isEmpty(), "Removal didn't set the list back to empty");
    }
}
