package org.j3d.ui.image;

import java.awt.image.BufferedImage;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * This class does something
 *
 * @author justin
 */
public class BaseJPEGImageObserverTest {
    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        class DummyTestClass extends BaseJPEGImageObserver
        {
            public void testConstruction()
            {
                assertNotNull(targetWriter, "Target writer was not found in construction");
                assertNotNull(writeParams, "No image params created during construction");
            }

            @Override
            public void canvasImageCaptured(BufferedImage img)
            {
                // Do nothing for the test
            }
        }

        DummyTestClass classUnderTest = new DummyTestClass();
        classUnderTest.testConstruction();
    }
}
