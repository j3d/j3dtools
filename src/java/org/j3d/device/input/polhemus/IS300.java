/******************************************************************
*
*   Copyright (C) Satoshi Konno 1999
*
*   File : IS300.java
*
******************************************************************/

package org.j3d.device.input.polhemus;

// Standard imports
// None

// Application specific imports
// None

/**
 * Implementation of a driver that can read data from an IS300
 * device.
 * <p>
 *
 * @author Satoshi Konno, Justin Couch
 * @version $Revision: 1.1 $
 */
public class IS300 extends Polhemus
{
    public IS300(String deviceName, int baudrate)
    {
        super(deviceName, baudrate);
    }

    public IS300(int device, int baudrate)
    {
        super(device, baudrate);
    }

    public int readActiveReceivers()
    {
        int nReceiver = 0;
        write(activeStationStateCommand);
        byte data[] = read(9).getBytes();
        if (data.length == 9)
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
    }

    public int getDeviceDataLength()
    {
        return 47;
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
