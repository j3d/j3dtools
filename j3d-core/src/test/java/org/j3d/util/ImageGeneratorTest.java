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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.net.URL;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ImageGeneratorTest
{
    @Test(groups = "unit")
    public void testBasicLoading() throws Exception
    {
        final String TEST_IMAGE_NAME = "images/test_image.png";

        ClassLoader cl = ImageGeneratorTest.class.getClassLoader();
        URL url = cl.getResource(TEST_IMAGE_NAME);

        assertNotNull(url, "Unable to find the test image");

        Image srcImage = Toolkit.getDefaultToolkit().createImage(url);

        // Use the MediaTracker to force loading of the original image so
        // that the width and height can be found.
        try
        {
            MediaTracker mt = new MediaTracker(new Canvas());
            mt.addImage(srcImage, 0);
            mt.waitForAll();
        }
        catch(InterruptedException ie)
        {
        }

        ImageProducer testProducer = srcImage.getSource();

        ImageGenerator gen = new ImageGenerator();
        testProducer.startProduction(gen);

        BufferedImage resultImage = gen.getImage();

        testProducer.removeConsumer(gen);

        assertNotNull(resultImage, "No image generated from the loader");
        assertEquals(resultImage.getWidth(), srcImage.getWidth(null), "Didn't copy width properly");
        assertEquals(resultImage.getHeight(), srcImage.getHeight(null), "Didn't copy height properly");
    }
}
