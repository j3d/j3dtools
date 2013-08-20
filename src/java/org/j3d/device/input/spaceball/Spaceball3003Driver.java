/*****************************************************************************
 * Spaceball3003Driver.java
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
 * Driver for Labtec's Spaceball 2003/3003 device.
 * NOTE: Implementation not finished and not tested. Any feedback welcome.<p>
 * Spaceball, Spaceball 2003 and Spaceball 3003 are Trademarks of Labtec Inc.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 20. Aug. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski
 */
public class Spaceball3003Driver extends SpaceballDriver
implements Spaceball3003CallbackInterface
{
    private static final int    NUM_OF_BUTTONS = 8;

    public Spaceball3003Driver
    (
        final SerialPort    serialPort,
        final InputStream   inputStream,
        final OutputStream  outputStream
    )
    {
        super
        (
            serialPort,
            inputStream,
            outputStream,
            Spaceball3003Packet.PACKET_TERMINATOR
        );
        setNumOfButtons( NUM_OF_BUTTONS );
    }

    protected void dispatch
    (
        final byte[ ]   readBuffer,
        final int       dataStart,
        final int       dataLength,
        final int       header
    )
    {
        if( header == Spaceball3003Packet.BALL_DATA )
        {
            // if packet length = OK then decode otherwise discard
            if( dataLength == Spaceball3003Packet.POSITION_DATA_PACKET_LENGTH )
            {
                Spaceball3003Packet.
                    decodeBallData( readBuffer, itsPositionData, dataStart );
            }
        }
        else if( header == Spaceball3003Packet.BUTTON_DATA )
        {
            // if packet length = OK then decode otherwise discard
            if( dataLength == Spaceball3003Packet.BUTTON_DATA_PACKET_LENGTH )
            {
                Spaceball3003Packet.
                    decodeButtonEvent( itsButtonData, readBuffer, dataStart );
            }
        }
        else
        {
            Spaceball3003Packet.doCallBacks
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
     * Requests data from the Spaceball 2003/3003 device. Must be called once
     * after {@link SpaceballDriver#open} in order to receive position data.
     * The first data packet will be "null-data" packet (all position data is
     * set to zero).<p>
     * Blocking until response from device received or timeout is over.
     * @param timeout [ms] timeout for response from the Spaceball device
     * @throws IOException if timeout is reached without a response from the
     *      Spaceball device or if an error occurs during reading the serial
     *      port
     */
    public void enableDevice( final int timeout ) throws IOException
    {
        final int length = Spaceball3003Packet.createEnableDevicePacket
        (
            itsWriteBuffer,
            0
        );
        write( itsWriteBuffer, 0, length );
    }

    /**
     * Ceases postion data transfer from the Spaceball device.
     * Blocking until response from the Spaceball device received or timeout
     * is over.
     * @param timeout [ms] timeout for response from the Spaceball device
     * @throws IOException if timeout is reached without a response from the
     *    Spaceball device or if an error occurs during reading the serial
     *    port
     */
    public void disableDevice( final int timeout ) throws IOException
    {
        final int length = Spaceball3003Packet.createSimpleRequestPacket
        (
            itsWriteBuffer,
            0,
            Spaceball3003Packet.DISABLE_BALL_DATA
        );
        write( itsWriteBuffer, 0, length );
    }

    public void rezeroDevice( final int timeout ) throws IOException
    {
        final int length = SpaceballPacket.createSimpleRequestPacket
        (
            itsWriteBuffer,
            0,
            Spaceball3003Packet.GET_REZERO_BALL
        );
        write( itsWriteBuffer, 0, length );
        waitForResponse
        (
            timeout,
            Spaceball3003Packet.GET_REZERO_BALL,
            "rezero device"
        );
    }
}