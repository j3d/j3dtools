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

import org.j3d.device.input.JoystickState;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the WheelState class.
 *
 * @author justin
 */
public class JoystickStateTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        JoystickState class_under_test = new JoystickState();

        assertEquals(class_under_test.throttleSlider, 0.0f, "Throttle not defaulted to 0");
        assertFalse(class_under_test.throttleSlider_changed, "Throttle change state not defaulted to off");
        
        assertEquals(class_under_test.hatX, 0.0f, "Hat X position not defaulted to 0");
        assertEquals(class_under_test.hatY, 0.0f, "Hat Y position not defaulted to 0");
        assertEquals(class_under_test.stickX, 0.0f, "Stick X position not defaulted to 0");
        assertEquals(class_under_test.stickY, 0.0f, "Stick Y position not defaulted to 0");
        assertEquals(class_under_test.stickZ, 0.0f, "Stick Z position not defaulted to 0");
        
        assertFalse(class_under_test.hatX_changed, "Hat X change state not defaulted to off");
        assertFalse(class_under_test.hatY_changed, "Hat Y change state not defaulted to off");
        assertFalse(class_under_test.stickX_changed, "Stick X change state not defaulted to off");
        assertFalse(class_under_test.stickY_changed, "Stick Y change state not defaulted to off");
        assertFalse(class_under_test.stickZ_changed, "Stick Z change state not defaulted to off");
        
        assertFalse(class_under_test.triggerButton, "Trigger state not defaulted to off");
        assertFalse(class_under_test.triggerButton_changed, "Trigger change state not defaulted to off");
    }

    @Test(groups = "unit")
    public void testClearChange() throws Exception
    {
        final float TEST_SLIDER_VALUE = (float)Math.random();
        final float TEST_HAT_X_VALUE = (float)Math.random();
        final float TEST_HAT_Y_VALUE = (float)Math.random();
        final float TEST_STICK_X_VALUE = (float)Math.random();
        final float TEST_STICK_Y_VALUE = (float)Math.random();
        final float TEST_STICK_Z_VALUE = (float)Math.random();

        JoystickState class_under_test = new JoystickState();

        class_under_test.throttleSlider = TEST_SLIDER_VALUE;
        class_under_test.throttleSlider_changed = true;

        class_under_test.triggerButton = true;
        class_under_test.triggerButton_changed = true;
        
        class_under_test.hatX = TEST_HAT_X_VALUE;
        class_under_test.hatY = TEST_HAT_Y_VALUE;
        class_under_test.stickX = TEST_STICK_X_VALUE;
        class_under_test.stickY = TEST_STICK_Y_VALUE;
        class_under_test.stickZ = TEST_STICK_Z_VALUE;
        class_under_test.hatX_changed = true;
        class_under_test.hatY_changed = true;
        class_under_test.stickX_changed = true;
        class_under_test.stickY_changed = true;

        class_under_test.clearChanged();

        assertEquals(class_under_test.throttleSlider, TEST_SLIDER_VALUE, "Throttle should not be cleared");
        assertTrue(class_under_test.triggerButton, "Trigger button state should not be cleared");

        assertEquals(class_under_test.hatX, TEST_HAT_X_VALUE, "Hat X position should not be changed");
        assertEquals(class_under_test.hatY, TEST_HAT_Y_VALUE, "Hat Y position should not be changed");
        assertEquals(class_under_test.stickX, TEST_STICK_X_VALUE, "Stick X position should not be changed");
        assertEquals(class_under_test.stickY, TEST_STICK_Y_VALUE, "Stick Y position should not be changed");
        assertEquals(class_under_test.stickZ, TEST_STICK_Z_VALUE, "Stick X position should not be changed");

        assertFalse(class_under_test.hatX_changed, "Hat X change state not returned to off");
        assertFalse(class_under_test.hatY_changed, "Hat y change state not returned to off");
        assertFalse(class_under_test.stickX_changed, "Stick X change state not returned to off");
        assertFalse(class_under_test.stickY_changed, "Stick Y change state not returned to off");
        assertFalse(class_under_test.stickZ_changed, "Stick Z change state not returned to off");
        assertFalse(class_under_test.throttleSlider_changed, "Throttle change state not returned to off");
        assertFalse(class_under_test.triggerButton_changed, "Trigger button change state not returned to off");
    }
}
