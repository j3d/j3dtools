/******************************************************************
*
*   VRML Library for Java
*
*   Copyright (C) Satoshi Konno 1997-1998
*
******************************************************************/

package org.j3d.device.input.polhemus;

// Standard imports
// None

// Application specific imports
// None

/**
 * Base representation of all devices that are Polhemus products.
 *
 * @author Satoshi Konno, Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class Polhemus extends SerialPortDevice {

    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;

    public static final String toBinaryCommand = "f";
    public static final String toASCIICommand  = "F";
    public static final String toContinuousCommand = "C";
    public static final String toNonContinuousCommand = "c";
    public static final String retrieveStatusCommand = "S";
    public static final String getRecordCommand = "P";
    public static final String systemResetCommand = "W";
    public static final String performBoresightCommand = "B1\r";
    public static final String activeStationStateCommand = "l\r";

    public static final int RECEIVER1 = 1;
    public static final int RECEIVER2 = 2;
    public static final int RECEIVER3 = 3;
    public static final int RECEIVER4 = 4;

    public float[][] mPositionData = new float[4][3];
    public float[][] mRotationData = new float[4][3];

    private int mWaitTime = 0;
    private int mUpdateFlag = 0;
    private int mnReciver = 0;

    /**
     *  @param deviceName name of the port to open.  ex. "COM1", "/dev/ttyd1"
     *  @param baudrate baudrate of the port.
     *  <UL>
     *  9600<BR>
     *  19200<BR>
     *  38400<BR>
     *  ....<BR>
     *  </UL>
     *  @see SerialPortDevice#SerialPort(String deviceName, int baudrate, int dataBits, int stopBits, int parity)
     */
    public Polhemus(String deviceName, int baudrate)
    {
        super(deviceName, baudrate, DATABITS_8, STOPBITS_1, PARITY_NONE);

        setDeviceDataWaitTimeFromBaudRate(baudrate);
        initialize();
    }

    /**
     *  @param device       number of the port to open.  ex. "COM1", "/dev/ttyd1"
     *  <UL>
     *  SERIAL1<BR>
     *  SERIAL2<BR>
     *  SERIAL3<BR>
     *  SERIAL4<BR>
     *  </UL>
     *  @param baudrate     baudrate of the port.
     *  <UL>
     *  9600<BR>
     *  19200<BR>
     *  38400<BR>
     *  ....<BR>
     *  </UL>
     *  @see SerialPortDevice#SerialPort(int serialport, int baudrate, int dataBits, int stopBits, int parity)
     */
    public Polhemus(int device, int baudrate)
    {
        super(device, baudrate, DATABITS_8, STOPBITS_1, PARITY_NONE);
        setDeviceDataWaitTimeFromBaudRate(baudrate);
        initialize();
    }

    abstract public int readActiveReceivers();
    abstract public void setReceiverOutputFormat();
    abstract public int getDeviceDataLength();
    abstract public int getDevicePositionDataOffset();
    abstract public int getDeviceRotationDataOffset();

    public void setDeviceDataWaitTimeFromBaudRate(int baudRate)
    {
        if (115200 <= baudRate)
        {
            setDeviceDataWaitTime(10);
            return;
        }

        if (38400 <= baudRate)
        {
            setDeviceDataWaitTime(20);
            return;
        }

        if (19200 <= baudRate)
        {
            setDeviceDataWaitTime(50);
            return;
        }
        setDeviceDataWaitTime(100);
    }

    public void setDeviceDataWaitTime(int time)
    {
        mWaitTime = time;
    }

    public int getDeviceDataWaitTime()
    {
        return mWaitTime;
    }

    public void initialize()
    {
        int nReceiver = readActiveReceivers();
        setActiveReceivers(nReceiver);

        setReceiverOutputFormat();

        write(toASCIICommand);
        write(toNonContinuousCommand);

        for (int n=0; n<4; n++)
        {
            initData(mPositionData[n]);
            initData(mRotationData[n]);
        }

        setReceiverUpdateFlag(0);
    }

    private void setActiveReceivers(int n)
    {
        mnReciver = n;
    }

    /**
     *  @return the active receiver number
     */
    public int getActiveReceivers()
    {
        return mnReciver;
    }

    /**
     *  @param nReciver     number of reciver. (1 - 4)
     *  <UL>
     *  RECEIVER1<BR>
     *  RECEIVER2<BR>
     *  RECEIVER3<BR>
     *  RECEIVER4<BR>
     *  </UL>
     *  @param pos          the position of reciver.
     */
    public void getPosition(int nReciver, float[] pos)
    {
        int bitFlag = 0x01 << (nReciver-1+4);
        updateData(bitFlag);
        copyData(mPositionData[nReciver-1], pos);
    }

    /**
     *  @param nReciver     number of reciver. (1 - 4)
     *  <UL>
     *  RECEIVER1<BR>
     *  RECEIVER2<BR>
     *  RECEIVER3<BR>
     *  RECEIVER4<BR>
     *  </UL>
     *  @return             the position of reciver.
     */
    public float[] getPosition(int nReciver)
    {
        float[] pos = new float[3];
        getPosition(nReciver, pos);
        return pos;
    }

    /**
     *  @param nReciver     number of reciver. (1 - 4)
     *  <UL>
     *  RECEIVER1<BR>
     *  RECEIVER2<BR>
     *  RECEIVER3<BR>
     *  RECEIVER4<BR>
     *  </UL>
     *  @param euler        the orientation of reciver.
     */
    public void getOrientation(int nReciver, float[] euler)
    {
        int bitFlag = 0x01 << (nReciver-1);
        updateData(bitFlag);
        copyData(mRotationData[nReciver-1], euler);
    }

    /**
     *  @param nReciver     number of reciver. (1 - 4)
     *  <UL>
     *  RECEIVER1<BR>
     *  RECEIVER2<BR>
     *  RECEIVER3<BR>
     *  RECEIVER4<BR>
     *  </UL>
     *  @return             the orientation of reciver.
     */
    public float[] getOrientation(int nReciver)
    {
        float euler[] = new float[3];
        getOrientation(nReciver, euler);
        return euler;
    }

    /////////////////////////////////////////////////////////////////
    // Debug Methods
    /////////////////////////////////////////////////////////////////

/*
    public void write(String data) {
        System.out.println("write = \"" + data + "\"");
        super.write(data);
    }

    public String read(int ndata) {
        String data = super.read(ndata);
        System.out.println("read  = \"" + data + "\"");
        return data;
    }

    public String read(int ndata, long timeOut) {
        String data = super.read(ndata, timeOut);
        System.out.println("read (" + data.length() + ")= \"" + data + "\"");
        return data;
    }
*/

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /////////////////////////////////////////////////////////////////
    //  Sample date : 01   0.89   0.04   1.82  -70.55  41.83 -23.48
    /////////////////////////////////////////////////////////////////

    /**
     * Read the data and process it.
     */
    private void readData()
    {
        int waitTime = getDeviceDataWaitTime();
        int msgLength = getDeviceDataLength();
        int posOffset = getDevicePositionDataOffset();
        int rotOffset = getDeviceRotationDataOffset();
        for (int n=0; n<getActiveReceivers(); n++)
        {
            write(getRecordCommand);
            waitTime(waitTime);
            byte data[] = read(msgLength).getBytes();
            int i;
            if (data.length == msgLength)
            {
                for (i=0; i<3; i++)
                {
                    String value = new String(data, posOffset+(7*i), 7);
                    mPositionData[n][i] = Float.parseFloat(value);
                }

                for (i=0; i<3; i++)
                {
                    String value = new String(data, rotOffset+(7*i), 7);
                    mRotationData[n][i] = Float.parseFloat(value);
                }
            }
        }
        setReceiverUpdateFlag(0xff);
    }

    private void setReceiverUpdateFlag(int flag)
    {
        mUpdateFlag = flag;
    }

    private int getReceiverUpdateFlag()
    {
        return mUpdateFlag;
    }

    private void initData(float data[])
    {
        for (int n=0; n<data.length; n++)
            data[n] = 0.0f;
    }

    private void copyData(float srcData[], float destData[])
    {
        for (int n=0; n<srcData.length; n++)
            destData[n] = srcData[n];
    }

    private void updateData(int updateBit)
    {
        int recvFlag = getReceiverUpdateFlag();

        if ((updateBit & recvFlag) == 0)
            readData();

        int newRecvFlag = getReceiverUpdateFlag();
        newRecvFlag &= ~updateBit;
        setReceiverUpdateFlag(newRecvFlag);
    }
}
