/*****************************************************************************
 * Spaceball4000Driver.java
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
 * Driver for Labtec's Spaceball 4000 device.<p>
 * Spaceball and Spaceball 4000 are Trademarks of Labtec Inc.
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 29. Oct. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski
 */
public class Spaceball4000Driver extends SpaceballDriver
    implements Spaceball4000CallbackInterface
{
    public Spaceball4000Driver
    (
        final SerialPort    serialPort,
        final InputStream   inputStream,
        final OutputStream  outputStream
    )
    throws IOException
    {
        super
        (
            serialPort,
            inputStream,
            outputStream,
            Spaceball4000Packet.PACKET_TERMINATOR
         );
        getDeviceDescriptor( 1000 );
    }

    protected void dispatch
    (
        final byte[ ]   readBuffer,
        final int       dataStart,
        final int       dataLength,
        final int       header
    )
    {
        if( header == Spaceball4000Packet.BALL_DATA )
        {
            // if packet length = OK then decode otherwise discard
            if( dataLength == Spaceball4000Packet.POSITION_DATA_PACKET_LENGTH )
            {
                Spaceball4000Packet.
                    decodeBallData( readBuffer, itsPositionData, dataStart );
            }
        }
        else if( header == Spaceball4000Packet.ADVANCED_BUTTON_DATA )
        {
            // if packet length = OK then decode otherwise discard
            if( dataLength == Spaceball4000Packet . BUTTON_DATA_PACKET_LENGTH )
            {
                Spaceball4000Packet.decodeButtonEvent
                (
                    itsButtonData,
                    readBuffer,
                    this,
                    dataStart
                );
            }
        }
        // ignore these packets for Spaceball 4000
        else if( header == Spaceball4000Packet.BUTTON_DATA )
        {

        }
        else
        {
            Spaceball4000Packet.doCallBacks
            (
                readBuffer,
                this,
                dataStart + 1,
                dataLength - 1,
                header
            );
        }
    }

    /**
     * Requests data from the Spaceball 4000 device. Must be called once after
     * {@link SpaceballDriver#open} in order to receive position data. The
     * first data packet will be "null-data" packet (all position data is set
     * to zero).<p>
     * Blocking until response from device received or timeout is over.
     * @param timeout [ms] timeout for response from the Spaceball device
     * @throws IOException if timeout is reached without a response from the
     *    Spaceball device or if an error occurs during reading the serial
     *    port
     */
    public void enableDevice( final int timeout ) throws IOException
    {
        final int length = Spaceball4000Packet.createSimpleRequestPacket
        (
            itsWriteBuffer,
            0,
            Spaceball4000Packet.ENABLE_BALL_DATA
        );
        write( itsWriteBuffer, 0, length );
        waitForResponse
        (
            timeout,
            Spaceball4000Packet.ENABLE_BALL_DATA,
            "enable device"
        );
    }

    /**
     * Ceases postion data transfer from the Spaceball device.
     * Blocking until response from the Spaceball device received or timeout
     * is over.
     * @param timeout [ms] timeout for response from the Spaceball device
     * @throws IOException if timeout is reached without a response from the
     *      Spaceball device or if an error occurs during reading the serial
     *      port
     */
    public void  disableDevice( final int timeout ) throws IOException
    {
        final int length = Spaceball4000Packet.createSimpleRequestPacket
        (
            itsWriteBuffer,
            0,
            Spaceball4000Packet.DISABLE_BALL_DATA
        );
        write( itsWriteBuffer, 0, length );
        waitForResponse
        (
            timeout,
            Spaceball4000Packet.DISABLE_BALL_DATA,
            "disable device"
        );
    }

    /**
     * Emits a single beep. Blocking until response from the Spaceball
     * device received or timeout is over.
     * @param timeout for response from the Spaceball device.
     * @throws IOException if timeout is reached without a response from the
     *      Spaceball device or if an error occurs during reading the serial
     *      port
     * @see Spaceball4000Packet#createSimpleRequestPacket
     */
    public void emitSingleBeep( final int timeout ) throws IOException
   {
        emitSingleBeep( );
        waitForResponse
        (
            timeout,
            Spaceball4000Packet.EMIT_SINGLE_BEEP,
            "emit single beep"
        );
    }

    /**
     * Emits a single beep. Does not wait for response from the Spaceball
     * device.
     * Note: non blocking
     * @throws IOException if an error occurs during reading the serial port
     * @see Spaceball4000Packet#createSimpleRequestPacket
     */
   public void emitSingleBeep( ) throws IOException
   {
        final int length = Spaceball4000Packet.createSimpleRequestPacket
        (
            itsWriteBuffer,
            0,
            Spaceball4000Packet.EMIT_SINGLE_BEEP
        );
        write( itsWriteBuffer, 0, length );
    }

    public void rezeroDevice( final int timeout ) throws IOException
    {
        final int length = SpaceballPacket.createSimpleRequestPacket
        (
            itsWriteBuffer,
            0,
            Spaceball4000Packet.GET_REZERO_BALL
        );
        write( itsWriteBuffer, 0, length );
        waitForResponse
        (
            timeout,
            Spaceball4000Packet.GET_REZERO_BALL,
            "rezero device"
        );
    }

    /**
     * @param timeout for response from the Spaceball device.
     * @param enable true = auto rezero enabled, false = auto rezero disabled
     * @throws IOException if timeout is reached without a response from the
     *      Spaceball device or if an error occurs during reading the serial
     *      port
     * @see Spaceball4000Packet#createEnableDisableAutoRezeroPacket
     */
    public void enableAutoRezero( final int timeout, final boolean enable )
    throws IOException
    {
        final int length = Spaceball4000Packet.
            createEnableDisableAutoRezeroPacket( itsWriteBuffer, 0, enable );
        write( itsWriteBuffer, 0, length );
        waitForResponse
        (
            timeout,
            Spaceball4000Packet.AUTO_REZERO_RESPONSE,
            "enable/disable auto rezero"
        );
    }

    /**
     * @param timeout for response from the Spaceball device.
     * @param enable true = cubic sensitivity, false = standard sensitivity
     * @throws IOException if timeout is reached without a response from the
     *      Spaceball device or if an error occurs during reading the serial
     *      port
     * @see Spaceball4000Packet#createCubicSensitivityEnableDisablePacket
     */
    public void enableCubicSensitivity
    (
        final int timeout,
        final boolean enable
    )
    throws IOException
    {
        final int length = Spaceball4000Packet.
            createCubicSensitivityEnableDisablePacket
            (
                itsWriteBuffer,
                0,
                enable
            );
        write( itsWriteBuffer, 0, length );
        waitForResponse
        (
            timeout,
            Spaceball4000Packet.CUBIC_SENSITIVITY_ENABLE_DISABLE,
            "enable/disable cubic sensitivity"
        );
    }

    /**
     * @param timeout for response from the Spaceball device.
     * @throws IOException if timeout is reached without a response from the
     *      Spaceball device or if an error occurs during reading the serial
     *      port
     * @see Spaceball4000Packet#createSimpleRequestPacket
     */
    public void getDeviceDescriptor( final int timeout ) throws IOException
    {
        final int length = Spaceball4000Packet.createSimpleRequestPacket
        (
            itsWriteBuffer,
            0,
            Spaceball4000Packet.GET_DEVICE_DESCRIPTOR
        );
        write( itsWriteBuffer, 0, length );
        waitForResponse
        (
            timeout,
            Spaceball4000Packet.GET_DEVICE_DESCRIPTOR,
            "get device descriptor"
        );
    }

    /**
     * @param timeout for response from the Spaceball device.
     * @throws IOException if timeout is reached without a response from the
     *      Spaceball device or if an error occurs during reading the serial
     *      port
     * @see Spaceball4000Packet#createSimpleRequestPacket
     */
    public void getDeviceInfo( final int timeout ) throws IOException
    {
        final int length = Spaceball4000Packet.createSimpleRequestPacket
        (
            itsWriteBuffer,
            0,
            Spaceball4000Packet.GET_DEVICE_INFORMATION
        );
        write( itsWriteBuffer, 0, length );
        waitForResponse
        (
            timeout,
            Spaceball4000Packet.GET_DEVICE_INFORMATION_RESPONSE,
            "get device info"
        );
    }

    public void processDeviceInfoResponse
    (
        final int       type,
        final int       lineNumber,
        final String    content
    )
    {
        if( type == Spaceball4000Packet.GET_DEVICE_DESCRIPTOR )
        {
            if( lineNumber == 4 )
            {
                processRequestResponse( type );
            }
            System.out.println( content );
        }
        else
        {
            super.processDeviceInfoResponse( type, lineNumber, content );
        }
    }

    /**
     * Overriding methods shall call this method.
     * param value use constants
     *    Spaceball4000Packet#CUBIC_SENSITIVITY_ENABLED,
     *    Spaceball4000Packet#STANDARD_SENSITIVITY_ENABLED;
     *    other values indicate an error during decoding
     */
    public void processSensitivityResponse( final byte value )
    {
        processRequestResponse
        (
            Spaceball4000Packet.CUBIC_SENSITIVITY_ENABLE_DISABLE
        );
    }

    /**
     * Overriding methods shall call this method.
     */
    public void processAutoRezeroResponse
    (
        final int       autoZeroPeriod,
        final int       autoZeroWindow,
        final boolean   enabled
    )
    {
        processRequestResponse( Spaceball4000Packet . AUTO_REZERO_RESPONSE );
    }

    /**
     * @param handedness use constants
     *    Spaceball4000DeviceDescriptor#LEFT,
     *    Spaceball4000DeviceDescriptor#RIGHT
     */
    public void setHandedness( final int handedness )
    {

    }

    public void setNumOfButtons( final int numOfButtons )
    {
        // subtract the button for handedness
        super.setNumOfButtons( numOfButtons - 1 );
    }
}