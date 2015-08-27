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

import org.j3d.device.input.GamepadState;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the WheelState class.
 *
 * @author justin
 */
public class GamepadStateTest
{
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        GamepadState class_under_test = new GamepadState();

        assertEquals(class_under_test.throttleSlider, 0.0f, "Throttle not defaulted to 0");
        assertFalse(class_under_test.throttleSlider_changed, "Throttle change state not defaulted to off");
        
        assertEquals(class_under_test.leftHatX, 0.0f, "Stick X position not defaulted to 0");
        assertEquals(class_under_test.leftHatY, 0.0f, "Stick Y position not defaulted to 0");

        assertEquals(class_under_test.leftStickX, 0.0f, "Stick X position not defaulted to 0");
        assertEquals(class_under_test.leftStickY, 0.0f, "Stick Y position not defaulted to 0");
        assertEquals(class_under_test.rightStickX, 0.0f, "Stick Z position not defaulted to 0");
        assertEquals(class_under_test.rightStickY, 0.0f, "Hat X position not defaulted to 0");
        
        assertFalse(class_under_test.leftStickX_changed, "Stick X change state not defaulted to off");
        assertFalse(class_under_test.leftStickY_changed, "Stick Y change state not defaulted to off");
        assertFalse(class_under_test.rightStickX_changed, "Stick Z change state not defaulted to off");
        assertFalse(class_under_test.rightStickY_changed, "Hat X change state not defaulted to off");
        
        assertFalse(class_under_test.l1Button, "Trigger state not defaulted to off");
        assertFalse(class_under_test.l1Button_changed, "Trigger change state not defaulted to off");
        assertFalse(class_under_test.r1Button, "Trigger state not defaulted to off");
        assertFalse(class_under_test.r1Button_changed, "Trigger change state not defaulted to off");

        assertFalse(class_under_test.startButton, "Trigger state not defaulted to off");
        assertFalse(class_under_test.startButton_changed, "Trigger change state not defaulted to off");
    }

    @Test(groups = "unit")
    public void testClearChange() throws Exception
    {
        final float TEST_SLIDER_VALUE = (float)Math.random();
        final float TEST_HAT_X_VALUE = (float)Math.random();
        final float TEST_HAT_Y_VALUE = (float)Math.random();
        final float TEST_LEFT_STICK_X_VALUE = (float)Math.random();
        final float TEST_LEFT_STICK_Y_VALUE = (float)Math.random();
        final float TEST_RIGHT_STICK_X_VALUE = (float)Math.random();
        final float TEST_RIGHT_STICK_Y_VALUE = (float)Math.random();

        GamepadState class_under_test = new GamepadState();

        class_under_test.throttleSlider = TEST_SLIDER_VALUE;
        class_under_test.throttleSlider_changed = true;

        class_under_test.l1Button = true;
        class_under_test.l1Button_changed = true;
        
        class_under_test.r1Button = true;
        class_under_test.r1Button_changed = true;

        class_under_test.startButton = true;
        class_under_test.startButton_changed = true;

        class_under_test.leftHatX = TEST_HAT_X_VALUE;
        class_under_test.leftHatY = TEST_HAT_Y_VALUE;

        class_under_test.leftStickX = TEST_LEFT_STICK_X_VALUE;
        class_under_test.leftStickY = TEST_LEFT_STICK_Y_VALUE;
        class_under_test.rightStickX = TEST_RIGHT_STICK_X_VALUE;
        class_under_test.rightStickY = TEST_RIGHT_STICK_Y_VALUE;
        class_under_test.leftStickX_changed = true;
        class_under_test.leftStickY_changed = true;
        class_under_test.rightStickX_changed = true;
        class_under_test.rightStickY_changed = true;

        class_under_test.clearChanged();

        assertEquals(class_under_test.throttleSlider, TEST_SLIDER_VALUE, "Throttle should not be cleared");
        assertTrue(class_under_test.l1Button, "Trigger button state should not be cleared");
        assertTrue(class_under_test.r1Button, "Trigger button state should not be cleared");
        assertTrue(class_under_test.startButton, "Start button state should not be cleared");

        assertEquals(class_under_test.leftHatX, TEST_HAT_X_VALUE, "Stick X position should not be changed");
        assertEquals(class_under_test.leftHatY, TEST_HAT_Y_VALUE, "Stick Y position should not be changed");

        assertEquals(class_under_test.leftStickX, TEST_LEFT_STICK_X_VALUE, "Stick X position should not be changed");
        assertEquals(class_under_test.leftStickY, TEST_LEFT_STICK_Y_VALUE, "Stick Y position should not be changed");
        assertEquals(class_under_test.rightStickX, TEST_RIGHT_STICK_X_VALUE, "Stick X position should not be changed");
        assertEquals(class_under_test.rightStickY, TEST_RIGHT_STICK_Y_VALUE, "Hat X position should not be changed");

        assertFalse(class_under_test.leftStickX_changed, "Stick X change state not returned to off");
        assertFalse(class_under_test.leftStickY_changed, "Stick Y change state not returned to off");
        assertFalse(class_under_test.rightStickX_changed, "Stick Z change state not returned to off");
        assertFalse(class_under_test.rightStickY_changed, "Hat X change state not returned to off");
        assertFalse(class_under_test.throttleSlider_changed, "Throttle change state not returned to off");

        assertFalse(class_under_test.l1Button_changed, "Trigger button change state not returned to off");
        assertFalse(class_under_test.r1Button_changed, "Trigger button change state not returned to off");
        assertFalse(class_under_test.startButton_changed, "Trigger button change state not returned to off");
    }
}
