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
 * @version $Revision: 1.1 $
 */
class FileInputSensor extends Sensor
{
    /** Default number of SensorRead objects to keep around */
    private static final int NUM_SENSOR_READS = 5;

    FileInputSensor(InputDevice dev)
    {
        super(dev);
    }

    FileInputSensor(InputDevice dev, int buttons)
    {
        super(dev, NUM_SENSOR_READS, buttons);
    }

}
