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

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the base tracker state class
 *
 * @author justin
 */
public class TrackerStateTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        TrackerState class_under_test = new TrackerState();

        assertEquals(class_under_test.actionMask, 0, "No mask should be set by default");
        assertEquals(class_under_test.actionType, 0, "No type should be set by default");
        assertEquals(class_under_test.numButtons, 0, "No button count should be set by default");
        assertEquals(class_under_test.wheelClicks, 0, "No wheel click count should be set by default");
        assertFalse(class_under_test.altModifier, "Alt modifier should not be set by default");
        assertFalse(class_under_test.shiftModifier, "Shift modifier should not be set by default");
        assertFalse(class_under_test.ctrlModifier, "Control modifier should not be set by default");

        assertNotNull(class_under_test.worldOri, "World orientation is null");
        assertNotNull(class_under_test.worldPos, "World position is null");

        assertNotNull(class_under_test.deviceOri, "Device orientation is null");
        assertNotNull(class_under_test.devicePos, "Device position is null");
    }
}
