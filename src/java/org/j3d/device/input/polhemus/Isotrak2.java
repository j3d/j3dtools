/******************************************************************
*
*   Copyright (C) Satoshi Konno 1999
*
*   File : Isotrak2.java
*
******************************************************************/

package org.j3d.device.input.polhemus;

// Standard imports
// None

// Application specific imports
// None

/**
 * Implementation of a driver that can read data from an IsoTrak II
 * device.
 * <p>
 *
 * The IsoTrak II has two 6DOF input sensors.
 *
 * @author Satoshi Konno, Justin Couch
 * @version $Revision: 1.1 $
 */
public class Isotrak2 extends Polhemus
{
    public Isotrak2(String deviceName, int baudrate)
    {
        super(deviceName, baudrate);
    }

    public Isotrak2(int device, int baudrate)
    {
        super(device, baudrate);
    }

    public int readActiveReceivers()
    {
        int nReceiver = 0;
        write(activeStationStateCommand);
        byte data[] = read(7).getBytes();

        if (data.length == 7)
        {
            for (int n=0; n<4; n++)
            {
                if (data[n+3] == '1')
                nReceiver++;
            }
        }
        return nReceiver;
    }

    public void setReceiverOutputFormat()
    {
        String commSetRecvN = "O4,2\r";
        write(commSetRecvN);
    }

    public int getDeviceDataLength()
    {
        return 45;
    }

    public int getDevicePositionDataOffset()
    {
        return 3;
    }

    public int getDeviceRotationDataOffset()
    {
        return 24;
    }
}
