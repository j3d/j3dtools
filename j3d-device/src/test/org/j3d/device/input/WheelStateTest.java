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
 * Unit tests for the WheelState class.
 *
 * @author justin
 */
public class WheelStateTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        WheelState class_under_test = new WheelState();

        assertEquals(class_under_test.throttleSlider, 0.0f, "Throttle not defaulted to 0");
        assertEquals(class_under_test.wheelX, 0.0f, "Wheel position not defaulted to 0");
        assertFalse(class_under_test.button1, "Button 1 state not defaulted to off");
        assertFalse(class_under_test.button2, "Button 2 state not defaulted to off");
        assertFalse(class_under_test.button3, "Button 3 state not defaulted to off");
        assertFalse(class_under_test.button4, "Button 4 state not defaulted to off");
        assertFalse(class_under_test.button1_changed, "Button 1 change state not defaulted to off");
        assertFalse(class_under_test.button2_changed, "Button 2 change state not defaulted to off");
        assertFalse(class_under_test.button3_changed, "Button 3 change state not defaulted to off");
        assertFalse(class_under_test.button4_changed, "Button 4 change state not defaulted to off");
        assertFalse(class_under_test.throttleSlider_changed, "Throttle change state not defaulted to off");
        assertFalse(class_under_test.wheelX_changed, "Wheel position change state not defaulted to off");
    }

    @Test(groups = "unit")
    public void testClearChange() throws Exception
    {
        final float TEST_SLIDER_VALUE = (float)Math.random();
        final float TEST_WHEEL_VALUE = (float)Math.random();

        WheelState class_under_test = new WheelState();

        class_under_test.throttleSlider = TEST_SLIDER_VALUE;
        class_under_test.wheelX = TEST_WHEEL_VALUE;
        class_under_test.throttleSlider_changed = true;
        class_under_test.wheelX_changed = true;
        class_under_test.button1 = true;
        class_under_test.button2 = true;
        class_under_test.button3 = true;
        class_under_test.button4 = true;
        class_under_test.button1_changed = true;
        class_under_test.button2_changed = true;
        class_under_test.button3_changed = true;
        class_under_test.button4_changed = true;

        class_under_test.clearChanged();

        assertEquals(class_under_test.throttleSlider, TEST_SLIDER_VALUE, "Throttle should not be cleared");
        assertEquals(class_under_test.wheelX, TEST_WHEEL_VALUE, "Wheel position should not be cleared");
        assertTrue(class_under_test.button1, "Button 1 state should not be changed");
        assertTrue(class_under_test.button2, "Button 2 state should not be changed");
        assertTrue(class_under_test.button3, "Button 3 state should not be changed");
        assertTrue(class_under_test.button4, "Button 4 state should not be changed");

        assertFalse(class_under_test.button1_changed, "Button 1 change state not returned to off");
        assertFalse(class_under_test.button2_changed, "Button 2 change state not returned to off");
        assertFalse(class_under_test.button3_changed, "Button 3 change state not returned to off");
        assertFalse(class_under_test.button4_changed, "Button 4 change state not returned to off");
        assertFalse(class_under_test.throttleSlider_changed, "Throttle change state not returned to off");
        assertFalse(class_under_test.wheelX_changed, "Wheel position change state not returned to off");
    }
}
