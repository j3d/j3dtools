/******************************************************************
*
*   Copyright (C) Satoshi Konno 1999
*
*   File : SensorBehavior.java
*
******************************************************************/

package org.j3d.device.input.polhemus;

// Standard imports
import javax.media.j3d.*;

import java.util.Enumeration;

// Application specific imports
// None


/**
 * Generic behaviour that can be used to drive any sensor output into
 * a tranform in the scene graph.
 *
 * @author Satoshi Konno, Justin Couch
 * @version $Revision: 1.1 $
 */
public class SensorBehavior extends Behavior
{

    private WakeupOnElapsedFrames conditions;
    private TransformGroup transformGroup;
    private Sensor sensor;
    private Transform3D transform;

    public SensorBehavior( TransformGroup tg, Sensor sensor )
    {
        transformGroup = tg;
        this.sensor = sensor;

        conditions = new WakeupOnElapsedFrames(0);
        transform = new Transform3D();
    }

    public void initialize()
    {
        wakeupOn(conditions);
    }

    public void processStimulus(Enumeration criteria)
    {
        sensor.getRead(transform);
        transformGroup.setTransform(transform);
        wakeupOn(conditions);
    }
}
