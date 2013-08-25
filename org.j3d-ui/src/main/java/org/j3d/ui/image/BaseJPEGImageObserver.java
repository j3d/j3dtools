/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.ui.image;

// Standard imports

import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

// Application specific imports
import org.j3d.ui.CapturedImageObserver;

/**
 * Common setup code for ImageIO handling of the JPEG saving.
 *
 * @author Justin Couch
 * @version $Revision $
 */
public abstract class BaseJPEGImageObserver implements CapturedImageObserver
{
    /** The selected writer for our image output */
    protected ImageWriter targetWriter;

    /** Sets of preconfigured params used when writing the image */
    protected ImageWriteParam writeParams;

    /**
     * Construct a default observer with no filename set and it has not yet
     * captured an image.
     */
    protected BaseJPEGImageObserver()
    {
        Iterator<ImageWriter> jpeg_writers = ImageIO.getImageWritersByMIMEType("image/jpeg");

        while(jpeg_writers.hasNext() && targetWriter == null) {
            ImageWriter possible_writer = jpeg_writers.next();
            ImageWriterSpi spi = possible_writer.getOriginatingProvider();

            Class[] output_types = spi.getOutputTypes();

            for(int i = 0; i < output_types.length; i++) {
                if(ImageOutputStream.class.isAssignableFrom(output_types[i])) {
                    targetWriter = possible_writer;
                    break;
                }
            }
        }

        writeParams = targetWriter.getDefaultWriteParam();
        writeParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParams.setCompressionQuality(0.9f);
    }
}

