/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.device.input;

// Standard imports
import javax.media.j3d.InputDevice;
import javax.media.j3d.Sensor;
import javax.media.j3d.Transform3D;

import javax.vecmath.Vector3f;

// Application specific imports
// none

/**
 * An extension of the standard Sensor specific to our file input device
 * <P>
 *
 * There is no implementation at the moment. A work in progress.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
class FileInputSensor extends Sensor
{
    /** Default number of SensorRead objects to keep around */
    private static final int NUM_SENSOR_READS = 5;

    /**
     * Construct a default Sensor implementation. This has no buttons and the
     * default sensor read size set by the base class
     *
     * @param dev The device that this sensor belongs to
     */
    FileInputSensor(InputDevice dev)
    {
        super(dev);
    }

    /**
     * Construct a sensor that has the given number of buttons. The sensor
     * read count is set to the internal default size: 5
     *
     * @param dev The device that this sensor belongs to
     * @param buttons The number of buttons this holds
     */
    FileInputSensor(InputDevice dev, int buttons)
    {
        super(dev, NUM_SENSOR_READS, buttons);
    }
}
