/******************************************************************
*
*   Copyright (C) Satoshi Konno 1999
*
*   File : SerialPort.java
*
******************************************************************/

package org.j3d.device.input.polhemus;

// Standard imports
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Enumeration;

import javax.comm.SerialPort;
import javax.comm.CommPortIdentifier;

// Application specific imports
// None

/**
 * Representation of a device that uses the serial port for
 * information.
 *
 * @author Satoshi Konno, Justin Couch
 * @version $Revision: 1.1 $
 */
public abstract class SerialPortDevice
{
    public final static int SERIALPORT1 = 0;
    public final static int SERIALPORT2 = 1;
    public final static int SERIALPORT3 = 2;
    public final static int SERIALPORT4 = 3;

    public final static int DATABITS_5 = SerialPort.DATABITS_5;
    public final static int DATABITS_6 = SerialPort.DATABITS_6;
    public final static int DATABITS_7 = SerialPort.DATABITS_7;
    public final static int DATABITS_8 = SerialPort.DATABITS_8;

    public final static int STOPBITS_1 = SerialPort.STOPBITS_1;
    public final static int STOPBITS_2 = SerialPort.STOPBITS_2;
    public final static int STOPBITS_1_5 = SerialPort.STOPBITS_1_5;

    public final static int PARITY_NONE = SerialPort.PARITY_NONE;
    public final static int PARITY_ODD = SerialPort.PARITY_ODD;
    public final static int PARITY_EVEN = SerialPort.PARITY_EVEN;

    private SerialPort mSerialPort = null;
    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;

    /**
     *  @param deviceName   name of the port to open.  ex. "COM1", "/dev/ttyd1"
     *  @param baudrate     baudrate of the port.
     *  <UL>
     *  9600<BR>
     *  19200<BR>
     *  38400<BR>
     *  ....<BR>
     *  </UL>
     *  @param dataBits
     *  <UL>
     *  DATABITS_5: 5 bits<BR>
     *  DATABITS_6: 6 bits<BR>
     *  DATABITS_7: 7 bits<BR>
     *  DATABITS_8: 8 bits<BR>
     *  </UL>
     *  @param stopBits
     *  <UL>
     *  STOPBITS_1: 1 stop bit<BR>
     *  STOPBITS_2: 2 stop bits<BR>
     *  STOPBITS_1_5: 1.5 stop bits<BR>
     *  </UL>
     *  @param parity
     *  <UL>
     *  PARITY_NONE: no parity<BR>
     *  PARITY_ODD: odd parity<BR>
     *  PARITY_EVEN: even parity<BR>
     *  </UL>
     */
    protected SerialPortDevice(String deviceName,
                               int baudrate,
                               int dataBits,
                               int stopBits,
                               int parity)
    {
        open(deviceName, baudrate, dataBits, stopBits, parity);
    }

    /**
     *  @param device number of the port to open.  ex. "COM1", "/dev/ttyd1"
     *  <UL>
     *  SERIAL1<BR>
     *  SERIAL2<BR>
     *  SERIAL3<BR>
     *  SERIAL4<BR>
     *  </UL>
     *  @param baudrate baudrate of the port.
     *  <UL>
     *  9600<BR>
     *  19200<BR>
     *  38400<BR>
     *  ....<BR>
     *  </UL>
     *  @param dataBits
     *  <UL>
     *  DATABITS_5: 5 bits<BR>
     *  DATABITS_6: 6 bits<BR>
     *  DATABITS_7: 7 bits<BR>
     *  DATABITS_8: 8 bits<BR>
     *  </UL>
     *  @param stopBits
     *  <UL>
     *  STOPBITS_1: 1 stop bit<BR>
     *  STOPBITS_2: 2 stop bits<BR>
     *  STOPBITS_1_5: 1.5 stop bits<BR>
     *  </UL>
     *  @param parity
     *  <UL>
     *  PARITY_NONE: no parity<BR>
     *  PARITY_ODD: odd parity<BR>
     *  PARITY_EVEN: even parity<BR>
     *  </UL>
     */
    protected SerialPortDevice(int device,
                               int baudrate,
                               int dataBits,
                               int stopBits,
                               int parity)
    {
        String deviceName = null;
        int nSerialPort = 0;
        Enumeration e = CommPortIdentifier.getPortIdentifiers();

        while(e.hasMoreElements())
        {
            CommPortIdentifier comm = (CommPortIdentifier)e.nextElement();
            if(comm.getPortType() == comm.PORT_SERIAL)
                nSerialPort++;

            if(device < nSerialPort)
            {
                deviceName = comm.getName();
                break;
            }
        }

        if(deviceName != null)
            open(deviceName, baudrate, dataBits, stopBits, parity);
        else
            System.out.println("SerialPort::SerialPort : Couldn't open the serial port !!");
    }

    /**
     * Close the serial port and all associated streams now.
     */
    public void close()
    {
        try
        {
            if(mOutputStream != null)
                mOutputStream.close();

            if(mInputStream != null)
                mInputStream.close();

            if(mSerialPort != null);
                mSerialPort.close();

            mOutputStream = null;
            mInputStream = null;
            mSerialPort = null;
        }
        catch (IOException e)
        {
        };
    }

    /**
     * Write the given string to the serial port now.
     *
     * @param value The value to write
     */
    public void write(String value)
    {
        if(mOutputStream == null)
            return;

        write(value.getBytes(), value.length());
    }

    /**
     * Write the given string to the serial port now.
     *
     * @param value The value to write
     * @param len The number of bytes to write from the array
     */
    public void write(byte[] value, int len)
    {
        if(mOutputStream == null)
            return;

        try
        {
            mOutputStream.write(value, 0, len);
            mOutputStream.flush();
        }
        catch (IOException e)
        {
        };
    }


    public String read(int ndata, long timeOut)
    {
        if(mInputStream == null)
            return null;

        if(ndata <= 0)
            return null;

        long startTime = System.currentTimeMillis();
        long spendTime = 0;
        int nread = 0;
        byte buffer[] = new byte[ndata];

        try
        {
            while(nread < ndata && spendTime < timeOut)
            {
                if(0 < nToRead())
                    nread = mInputStream.read(buffer);

                spendTime = System.currentTimeMillis() - startTime;
            }
        }
        catch (IOException e)
        {
        };

        return new String(buffer, 0, nread);
    }

    /**
     * Read the next ndata bytes from the port, given a maximum timeout of
     * 3000 milliseconds.
     *
     * @param ndata The number of bytes to read
     */
    public String read(int ndata)
    {
        return read(ndata, 3000);
    }

    /**
     * Find out how many bytes are available for reading.
     */
    public int nToRead()
    {
        if(mInputStream == null)
            return 0;

        int nData = -1;
        try
        {
            nData = mInputStream.available();
        }
        catch (IOException e)
        {
        };

        return nData;
    }

    public void waitData(int nWaitData)
    {
        int nData = 0;
        while (true)
        {
            nData = nToRead();
            if(nWaitData <= nData)
                break;
            waitTime(100);
        }
    }

    /**
     * Convenience method to wait for a certain period of time.
     *
     * @param millis The number of milliseconds to sleep for
     */
    public void waitTime(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException ie)
        {
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Internal convenience method to open the serial port for reading.
     *
     * @param deviceName The name of the com port to open
     * @param baudrate The rate to read from the port at
     * @param dataBits How many data bits to read
     * @param stopBits, The type of stop bits in the protocol
     * @param parity Flag for the type of parity checking
     */
    private void open(String deviceName,
                      int baudrate,
                      int dataBits,
                      int stopBits,
                      int parity)
    {
        try
        {
            CommPortIdentifier port =
                (CommPortIdentifier)CommPortIdentifier.getPortIdentifier(deviceName);

            mSerialPort = (SerialPort)port.open("GenericSerialReader", 5000);
            mSerialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
            mSerialPort.setFlowControlMode(javax.comm.SerialPort.FLOWCONTROL_NONE);
            mSerialPort.enableReceiveThreshold(1);
            mSerialPort.enableReceiveTimeout(3000);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
