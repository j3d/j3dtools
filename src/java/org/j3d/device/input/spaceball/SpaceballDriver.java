/*****************************************************************************
 * SpaceballDriver.java
 * Java Source
 *
 * This source is licensed under the GNU LGPL v2.1.
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 *
 * Copyright (c) 2001 Dipl. Ing. P. Szawlowski
 * University of Vienna, Dept. of Medical Computer Sciences
 ****************************************************************************/

package org.j3d.device.input.spaceball;

import javax.comm.*;
import java.io.*;

/**
 * Driver for Labtec's Spaceball device.
 * Does not support multithreading. All methods must be called from a single
 * thread.<p>
 * Stores only the latest received position data, which can be obtained with
 * the {@link #getLastPositionValues} method. The event of pressing a button is
 * stored until {@link #getLastButtonValues} is called. Then all button states
 * are cleared, i. e. release events are ignored. It might happen that very fast
 * button press/release events will not be detected.<p>
 * Use {@link SpaceballTypeDetector#createSpaceballDriver} to open a serial and
 * to obtain an appropriate <code>SpaceballDriver</code> object. After use
 * {@link #close} must be called to release the serial port.<p>
 * Uses the javax.comm extension package, which must be installed.<p>
 * Spaceball, Spaceball 2003, Spaceball 3003 and Spaceball 4000 are Trademarks
 *    of Labtec Inc.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 12. Nov. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public abstract class SpaceballDriver
{
    /**
     * Current position Data.
     * index 0: period of data (1/16th milliseconds),
     * index 1 xTranslationForce x-axis translation force,
     * index 2 yTranslationForce y-axis translation force,
     * index 3 zTranslationForce z-axis translation force,
     * index 4 xRotationalForce x-axis rotation force (positive values: counter
     *      clockwise rotation),
     * index 5 yRotationalForce y-axis rotation force (positive values: counter
     *      clockwise rotation),
     * index 6 zRotationalForce z-axis rotation force (positive values: counter
     *      clockwise rotation)
     */
    protected final int[ ] itsPositionData = new int[ ]{ 0, 0, 0, 0, 0, 0, 0 };
    /**
     * button pressed: value > 0, button not pressed: value = 0.
     */
    protected final int[ ] itsButtonData = new int[ MAX_NUM_OF_BUTTONS ];

    /**
     * use this buffer for packets send to the Spaceball device.
     */
    protected final byte[ ] itsWriteBuffer =
            new byte[ WRITE_BUFFER_SIZE_IN_BYTE ];
    private final byte[ ]   itsReadBuffer =
            new byte[ READ_BUFFER_SIZE_IN_BYTE ];

    // if buffers for read or write operations are too small increase the
    // appropriate value
    private static final int WRITE_BUFFER_SIZE_IN_BYTE = 30;
    private static final int READ_BUFFER_SIZE_IN_BYTE = 60;

    // if used for a Spaceball device with more than 20 buttons increase this
    // number
    private static final int MAX_NUM_OF_BUTTONS = 20;

    private static final int[ ] INIT_BUTTON_DATA =
            new int[ MAX_NUM_OF_BUTTONS ];

    private SerialPort   itsSerialPort = null;
    private InputStream  itsInputStream = null;
    private OutputStream itsOutputStream = null;

    private int itsNumOfButtons = 0;
    private int itsCurrentReadBufferOffset = 0;
    private int itsResponseFlag = 0;

    private int itsPacketTerminator = 0;

    public SpaceballDriver
    (
        final SerialPort     serialPort,
        final InputStream    inputStream,
        final OutputStream   outputStream,
        final int            packetTerminator
    )
    {
        itsSerialPort = serialPort;
        itsInputStream = inputStream;
        itsOutputStream = outputStream;
        itsPacketTerminator = packetTerminator;
    }

    /**
     * Closes and frees serial port. Call after using the device.
     */
    public void close( ) throws IOException
    {
        if( itsInputStream != null )
        {
            itsInputStream.close( );
        }
        if( itsOutputStream != null )
        {
            itsOutputStream.close( );
        }
        if( itsSerialPort != null )
        {
            itsSerialPort.close( );
        }
        itsSerialPort = null;
        itsInputStream = null;
        itsOutputStream = null;
    }

    /**
     * Requests data from the Spaceball device. Must be called once after
     * {@link #open} in order to receive position data. The first data packet
     * will be "null-data" packet (all position data is set to zero)
     * Blocking until response from device received or timeout is over.
     * @param timeout [ms] timeout for response from the Spaceball device
     * @throws IOException if timeout is reached without a response from the
     *    Spaceball device or if an error occurs during reading the serial
     *    port
     */
    public abstract void enableDevice( final int timeout ) throws IOException;

    /**
     * Ceases postion data transfer from the Spaceball device.
     * Blocking until response from the Spaceball device received or timeout
     * is over.
     * @param timeout [ms] timeout for response from the Spaceball device
     * @throws IOException if timeout is reached without a response from the
     *      Spaceball device or if an error occurs during reading the serial
     *      port
     */
    public abstract void disableDevice( final int timeout ) throws IOException;

    /**
     * Reads data from serial port. This method must be called periodically to
     * process data sent from the Spaceball device.
     * @throws IOException if an error occurs during reading the serial port
     */
    public void read( ) throws IOException
    {
        int length = itsInputStream.available( );
        int offset = itsCurrentReadBufferOffset;
        final int packetTerminator = itsPacketTerminator;
        final byte[ ] readBuffer = itsReadBuffer;
        while( length > 0 )
        {
            final int readLength = itsInputStream.
                read( readBuffer, offset, READ_BUFFER_SIZE_IN_BYTE - offset );
//            System.out.print("rcvd ");
//            printBuffer( readBuffer, readLength + offset );
            offset =
                decode( readBuffer, readLength + offset, packetTerminator );
            length -= readLength;
        }
        itsCurrentReadBufferOffset = offset;
    }

    /**
     * @return number of buttons supported by this device
     */
    public int getNumOfButtons( )
    {
        return itsNumOfButtons;
    }

    /**
     * Convenience method to enable an application to change port settings if
     * necessary.
     * @return handle to the SerialPort in use or null if {@link #open} was not
     * called before or was not successfull or after a call of {@link #close}.
     */
    public SerialPort getSerialPort( )
    {
        return itsSerialPort;
    }

    /**
     * @param positionData Max. size of array = 6.
     */
    public void getLastPositionValues( final int[ ] positionData )
    {
        final int length = positionData.length;
        System.arraycopy( itsPositionData, 1, positionData, 0, length );
    }

    /**
     * @param buttonData button pressed: value > 0, button not pressed:
     *      value = 0; Max. size of array = 12.
     */
    public void getLastButtonValues( final int[ ] buttonData )
    {
        final int length = buttonData.length;
        System.arraycopy( itsButtonData, 0, buttonData, 0, length );
        System.
            arraycopy( INIT_BUTTON_DATA, 0, itsButtonData, 0, itsNumOfButtons );
    }

    /**
     * Emits a patterend beep. Does not wait for response from the Spaceball
     * device.
     * Note: non blocking
     * @throws IOException if timeout is reached without a response from the
     *      Spaceball device or if an error occurs during reading the serial
     *      port
     * @see SpaceballPacket#createPatternedBeepPacket
     */
    public void emitPatternedBeep( final char[ ] pattern ) throws IOException
    {
        final int length = SpaceballPacket.
            createPatternedBeepPacket( itsWriteBuffer, 0, pattern );
        write( itsWriteBuffer, 0, length );
    }

    /**
     * Resets device. Blocking until response from the Spaceball device
     * received or timeout is over.
     * @param timeout for response from the Spaceball device.
     * @throws IOException if timeout is reached without a response from the
     *      Spaceball device or if an error occurs during reading the serial
     *      port
     * @see SpaceballPacket#createSimpleRequestPacket
     */
    public void resetDevice( final int timeout ) throws IOException
    {
        final int length = SpaceballPacket.createSimpleRequestPacket
        (
            itsWriteBuffer,
            0,
            SpaceballPacket.RESET_DEVICE
        );
        write( itsWriteBuffer, 0, length );
        waitForResponse
        (
            timeout,
            SpaceballPacket.RESET_DEVICE,
            "reset device"
        );
    }

    protected abstract void dispatch
    (
        final byte[ ]   readBuffer,
        final int       dataStart,
        final int       dataLength,
        final int       header
    );

    /**
     * Notify driver that expected event from Spaceball device received for
     * blocking requests. Overriding methods shall call this method.
     */
    public void processRequestResponse( final int type )
    {
        if( type == itsResponseFlag )
        {
            itsResponseFlag = 0;
        }
    }

    protected void setNumOfButtons( final int numOfButtons )
    {
        itsNumOfButtons = numOfButtons;
    }

    public void processError
    (
        final int       errorCode,
        final String    errorString
    )
    {
        System.err.println( "Spaceball (TM) driver: " + errorString );
    }

    /**
     * Overriding methods shall call
     * {@link SpaceballDriver#processRequestResponse}.
     */
    public void processDeviceInfoResponse
    (
        final int       type,
        final int       lineNumber,
        final String    content
    )
    {
        processRequestResponse( type );
        System.out.println( content );
    }

    /**
     * Default action if an echo event from Spaceball device was received.
     * Overriding methods shall call this method.
     */
    public void processEchoResponse( final byte[ ] data )
    {
        processRequestResponse( SpaceballPacket.ECHO );
    }

    public static SerialPort openPort( final String portDescriptor )
    throws NoSuchPortException, PortInUseException,
    UnsupportedCommOperationException
    {
        final CommPortIdentifier portId =
            CommPortIdentifier.getPortIdentifier( portDescriptor );
        if( portId.getPortType( ) != CommPortIdentifier.PORT_SERIAL )
        {
            throw new UnsupportedCommOperationException
            (
                "Spaceball (TM) driver: "+ portDescriptor
                    + " is not a serial port."
            );
        }
        final CommPort port = portId.open( "Java Spaceball (TM) driver", 2000 );
        final SerialPort serialPort = (SerialPort) port;
        serialPort.setSerialPortParams
        (
            9600,
            SerialPort.DATABITS_8,
            SerialPort.STOPBITS_1,
            SerialPort.PARITY_NONE
        );
        return serialPort;
    }

    /**
     * @param buffer buffer containing the data to be written to the serial
     *    interface
     * @param offset start offset of "buffer"
     * @param length number of bytes to write
     */
    protected final void write
    (
        final byte[ ] buffer,
        final int offset,
        final int length
    )
    throws IOException
    {
//      System.out.print("send ");
//      printBuffer( buffer, length );
        itsOutputStream.write( itsWriteBuffer, offset, length );
        itsOutputStream.flush( );
    }

    /**
     * Sends a request to the Spaceball device and wait until response is
     * received or timeout period is over. This method is blocking.
     * @param timeout [ms] timeout for response from the Spaceball device
     * @param request use constants
     * @param description description of request
     */
    protected final void waitForResponse
    (
        final int       timeout,
        final int       request,
        final String    description
    )
    throws IOException
    {
        itsResponseFlag = request;
        final long time = System.currentTimeMillis() + timeout;
        boolean error = System.currentTimeMillis() > time;
        while( ( ! error ) && ( itsResponseFlag != 0 ) )
        {
            read( );
            error = System.currentTimeMillis() > time;
        }
        if( error )
        {
            throw new IOException
            (
               "Spaceball (TM) driver: Timeout without response ("
                    + description + ")."
            );
        }
    }

    protected static void printBuffer( final byte[ ] buffer, final int length )
    {
        for( int i = 0; i < length; i ++ )
        {
            final String data = Integer.toHexString( buffer[ i ] );
            System.out.print( data + " " );
        }
        System.out.println( );
    }

    public static void pause( final int timeInMillis )
    {
        try
        {
            Thread.sleep( timeInMillis );
        }
        catch( InterruptedException e )
        {

        }
    }

    /**
     * Decodes <i>readBufferFillLevel</i> bytes of <i>readBuffer</i>. If the
     * last packet contained in <i>readBuffer</i> is incomplete, it will be
     * moved to the beginning of <i>readBuffer</i>. Concatenate new data into
     * <i>readBuffer</i> using the returned offset.
     * @param readBuffer The content of the last packet will be moved to the
     *      beginning of the buffer if the packet is not complete yet.
     * @param readBufferFillLevel number of bytes to process
     * @param caller object to receive notification of decoded events.
     * @return length of incomplete last packet (in bytes)
     */
    private int decode
    (
        final byte[ ]   readBuffer,
        final int       readBufferFillLevel,
        final int       packetTerminator
    )
    {
        int packetStart = 0;
        int packetEnd = searchForPacketEnd
        (
            readBuffer,
            0,
            readBufferFillLevel,
            packetTerminator
        );
        // while a whole packet is contained in readBuffer
        while( packetEnd > 0 )
        {
            final int header = readBuffer[ packetStart ];
            final int length = packetEnd - packetStart;
            // do device specific action according to the event
            dispatch( readBuffer, packetStart, length, header );
            // next packet starts at end of this packet
            packetStart = packetEnd + 1;
            packetEnd = searchForPacketEnd
            (
                readBuffer,
                packetStart,
                readBufferFillLevel,
                packetTerminator
            );
        }
        final int rest = readBufferFillLevel - packetStart;
        // if the last packet is not finished yet -> move content of this
        // package to beginning of readBuffer
        if( rest > 0 )
        {
            System.arraycopy( readBuffer, packetStart, readBuffer, 0, rest );
        }
        return rest;
    }

    /**
     * @return index of end of packet or -1 if no end of packet was detected
     */
    private static int searchForPacketEnd
    (
        final byte[ ] readBuffer,
        int offset,
        final int bufferLength,
        final int packetTerminator
    )
    {
        while( ( offset < bufferLength )
            && ( readBuffer[ offset ] != packetTerminator ) )
        {
            offset ++;
        }
        return offset == bufferLength ? -1 : offset;
    }
}