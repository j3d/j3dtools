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

package org.j3d.device.input;

import org.j3d.device.input.GenericHIDState;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the base generic HID state class
 *
 * @author justin
 */
public class GenericHIDStateTest
{
    @Test(groups = "unit")
    public void testBasicConstruction()
    {
        GenericHIDState class_under_test = new GenericHIDState();

        assertNotNull(class_under_test.axisValue, "Axis value is not created");
        assertNotNull(class_under_test.changeValue, "Change value flags is not created");
        assertEquals(class_under_test.axisValue.length, 0, "No axis values should be defined");
        assertEquals(class_under_test.changeValue.length, 0, "No change values should be defined");
    }

    @Test(groups = "unit")
    public void testAllocation() throws Exception
    {
        final int TEST_NEW_SIZE = 2;
        final int TEST_UPDATE_SIZE = 1;
        final float TEST_VALUE = (float)Math.random();

        GenericHIDState class_under_test = new GenericHIDState();

        class_under_test.allocateAxes(TEST_NEW_SIZE);

        assertNotNull(class_under_test.axisValue, "Axis value is not created");
        assertNotNull(class_under_test.changeValue, "Change value flags is not created");
        assertEquals(class_under_test.axisValue.length, TEST_NEW_SIZE, "Axis values incorrect size");
        assertEquals(class_under_test.changeValue.length, TEST_NEW_SIZE, "Change values incorrect size");

        for(int i = 0 ; i < TEST_NEW_SIZE; i++)
        {
            assertEquals(class_under_test.axisValue[i], 0.0f, "Axis value not default");
            assertFalse(class_under_test.changeValue[i], "Change value not default");
        }

        // Set a value to the first item and then reallocate. Check that the first item
        // no longer has the value.
        class_under_test.axisValue[0] = TEST_VALUE;
        class_under_test.changeValue[0] = true;

        class_under_test.allocateAxes(TEST_UPDATE_SIZE);

        assertNotNull(class_under_test.axisValue, "Axis value was wiped");
        assertNotNull(class_under_test.changeValue, "Change value flags was wiped");
        assertEquals(class_under_test.axisValue.length, TEST_UPDATE_SIZE, "Axis values incorrect size after change");
        assertEquals(class_under_test.changeValue.length, TEST_UPDATE_SIZE, "Change values incorrect size after change");

        assertEquals(class_under_test.axisValue[0], 0.0f, "Didn't clear axis value");
        assertFalse(class_under_test.changeValue[0], "Didn't clear change value");
    }

    @Test(groups = "unit")
    public void testClearChanged() throws Exception
    {
        final int TEST_NEW_SIZE = 1;
        final float TEST_VALUE = (float)Math.random();

        GenericHIDState class_under_test = new GenericHIDState();

        class_under_test.allocateAxes(TEST_NEW_SIZE);

        // Set a value to the first item and then reallocate. Check that the first item
        // no longer has the value.
        class_under_test.axisValue[0] = TEST_VALUE;
        class_under_test.changeValue[0] = true;

        class_under_test.clearChanged();

        assertEquals(class_under_test.axisValue.length, TEST_NEW_SIZE, "Change should not reallocate axis array");
        assertEquals(class_under_test.changeValue.length, TEST_NEW_SIZE, "Change should not reallocate change array");
        assertEquals(class_under_test.axisValue[0], TEST_VALUE, "Should not have cleared axis value");
        assertFalse(class_under_test.changeValue[0], "Didn't clear change value");
    }
}
