/******************************************************************
*
*   Copyright (C) Satoshi Konno 1999
*
*   File : PolhemusInputDevice.java
*
******************************************************************/

package org.j3d.device.input.polhemus;

// Standard imports
import javax.media.j3d.*;
import javax.vecmath.*;


// Application specific imports
// None

/**
 * Generic Java3D InputDevice implementation for any of the Polhemus
 * tracker devices.
 *
 * @author Satoshi Konno, Justin Couch
 * @version $Revision: 1.1 $
 */
public class PolhemusInputDevice implements InputDevice
{
    private Polhemus        polhemus;
    private Sensor          polhemusSensor[];
    private SensorRead  polhemusSensorRead[];

    private Transform3D initPosTransform[];
    private Transform3D initOriTransform[];

    private int polhemusActiveReceivers;

    private Transform3D polhemusTransform           = new Transform3D();
    private float           polhemusPos[]               = new float[3];
    private float           polhemusOri[]               = new float[3];

    private Transform3D posTransform                = new Transform3D();
    private Transform3D oriTransform                = new Transform3D();
    private Vector3f        posVector                   = new Vector3f();
    private Transform3D trans                           = new Transform3D();

    private float           sensitivity         = 1.0f;
    private float           angularRate         = 1.0f;
    private float           x, y, z;

    public PolhemusInputDevice(Polhemus polhemus) {
        this.polhemus = polhemus;
        polhemusActiveReceivers = polhemus.getActiveReceivers();

        polhemusSensor          = new Sensor[polhemusActiveReceivers];
        polhemusSensorRead  = new SensorRead[polhemusActiveReceivers];
        initPosTransform        = new Transform3D[polhemusActiveReceivers];
        initOriTransform        = new Transform3D[polhemusActiveReceivers];
        for (int n=0; n<polhemusActiveReceivers; n++) {
            polhemusSensor[n] = new Sensor(this);
            polhemusSensorRead[n] = new SensorRead();
            initPosTransform[n] = new Transform3D();
            initOriTransform[n] = new Transform3D();
            getPositionTransform(n+1, initPosTransform[n]);
            getOrientationTransform(n+1, initOriTransform[n]);
        }
        setSensitivity(0.1f);
        setAngularRate(0.01f);
    }

    public boolean initialize()
    {
        for (int i=0; i<3; i++)
        {
            polhemusPos[i]  = 0.0f;
            polhemusOri[i]  = 0.0f;
        }
        return true;
    }

    public void close()
    {
    }

    public int getProcessingMode()
    {
        return DEMAND_DRIVEN;
    }

    public int getSensorCount()
    {
        return polhemusActiveReceivers;
    }

    public Sensor getSensor(int id)
    {
        return polhemusSensor[id];
    }

    public void setProcessingMode(int mode)
    {
    }

    public void getPositionTransform(int n, Transform3D posTrans)
    {
        polhemus.getPosition(n+1, polhemusPos);
        posVector.x = polhemusPos[0];
        posVector.y = polhemusPos[1];
        posVector.z = polhemusPos[2];
        posTrans.setIdentity();
        posTrans.setTranslation(posVector);
    }

    public void getOrientationTransform(int n, Transform3D oriTrans)
    {
        polhemus.getOrientation(n, polhemusOri);
        oriTrans.setIdentity();

        // Polhemus : Z -> Y -> X = Java3D Y -> X -> Z
        trans.setIdentity();
        trans.rotY(Math.toRadians((double)polhemusOri[0]));
        oriTrans.mul(trans);
        trans.setIdentity();
        trans.rotX(Math.toRadians((double)polhemusOri[1]));
        oriTrans.mul(trans);
        trans.setIdentity();
        trans.rotZ(Math.toRadians((double)polhemusOri[2]));
        oriTrans.mul(trans);
    }

    public void pollAndProcessInput()
    {
        for (int n=0; n<polhemusActiveReceivers; n++)
        {
            polhemusSensorRead[n].setTime(System.currentTimeMillis());

            getOrientationTransform(n+1, posTransform);
            getOrientationTransform(n+1, oriTransform);

            polhemusTransform.setIdentity();
            polhemusTransform.mulInverse(initOriTransform[n]);
            polhemusTransform.mul(oriTransform);

            polhemusSensorRead[n].set(polhemusTransform);
            polhemusSensor[n].setNextSensorRead(polhemusSensorRead[n]);
        }
    }

    public void processStreamInput()
    {
    }

    public void setNominalPositionAndOrientation()
    {
        initialize();
        for (int n=0; n<polhemusActiveReceivers; n++)
        {
            polhemusSensorRead[n].setTime(System.currentTimeMillis());
            polhemusTransform.setIdentity();
            polhemusSensorRead[n].set(polhemusTransform);
            polhemusSensor[n].setNextSensorRead(polhemusSensorRead[n]);
        }
    }

    public void setSensitivity(float value)
    {
        sensitivity = value;
    }

    public float getSensitivity()
    {
        return sensitivity;
    }

    public void setAngularRate(float value)
    {
        angularRate = value;
    }

    public float getAngularRate()
    {
        return angularRate;
    }
}
