/*****************************************************************************
 * Spaceball3003Packet.java
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

/**
 * Generates and decodes data packets for Labtec's Spaceball 2003/3003
 * device according to the Spaceball 2003/3003 Packet Protocol (alpha3).<p>
 * NOTE: Some data packets will contain an additional "special character"
 * ('^' - 0x5e) inserted within the data stream.  This escape character notifies
 * the system to modify the following byte.  If the following byte is 'Q',
 * (0x51), 'S' (0x53), or an 'M' (0x4d) the seventh bit should be cleared.
 * (Which should result in 0x11, 0x13, 0x0d respectively.)  If the following
 * character is a second '^' (0x5e) then no modification should be made. (Do not
 * forget to remove the '^' character from the packet buffer.). This is
 * currently not implemented. Therefore certain data packets may be ignored,
 * specially if buttons 3 and 4 are pressed at the same time.<p>
 * NOTE: The Ignore Packet is not implemented.<p>
 * NOTE: Implementation not finished and not tested. Any feedback welcome.<p>
 * Spaceball, Spaceball 2003 and Spaceball 3003 are Trademarks of Labtec Inc.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 14. Aug. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski
 */
public class Spaceball3003Packet extends SpaceballPacket
{
    public static final int HARDWARE_ERROR_CODE_1 = 0x0040; //"@"
    public static final int CALIBRATION_CHECKSUM_ERROR_CODE = 0x0041; //"A"
    public static final int HARDWARE_ERROR_CODE_2 = 0x0042; //"B"
    public static final int HARDWARE_ERROR_CODE_3 = 0x0043; //"C"
    public static final int TRANSMIT_TIMEOUT_ERROR_CODE = 0x0044; //"D"
    public static final int RECEIVE_OVERFLOW_ERROR_ERROR_CODE = 0x0045; //"E"
    public static final int RECEIVE_ERROR_CODE = 0x0046; //"F"
    public static final int BEEPER_QUEUE_OVERFLOW_ERROR_CODE = 0x0047; //"G"
    public static final int PACKET_TOO_LONG_ERROR_CODE = 0x0048; //"H"

    /**
     * This command enables data packets. The Spaceball device will not send any
     * ball movement packets until this command is sent. This is a simple
     * request packet (packet which consists only of a header).Packet size =
     * 2 bytes.
    */
    public static final int  ENABLE_BALL_DATA = 0x4D5353;
    /**
     * This command disables data packets. The Spaceball device will cease to
     * send ball movement packets after this command is sent. This is a simple
     * request packet (packet which consists only of a header).Packet size =
     * 2 bytes.
     */
    public static final byte  DISABLE_BALL_DATA = 0x4D;
    /**
     * This command causes the Spaceball to rezero. It takes the current
     * position as the Powersensor rest position.  All subsequent measurements
     * (until reset) are taken relative to this position. This is a simple
     * request packet (packet which consists only of a header).
     * Packet size = 2 byte.
     */
    public static final byte  GET_REZERO_BALL = 0x5A;

    private static final int[ ] ERROR_CODES = new int[ ]
        {
            HARDWARE_ERROR_CODE_1,
            CALIBRATION_CHECKSUM_ERROR_CODE,
            HARDWARE_ERROR_CODE_2,
            HARDWARE_ERROR_CODE_3,
            TRANSMIT_TIMEOUT_ERROR_CODE,
            RECEIVE_OVERFLOW_ERROR_ERROR_CODE,
            RECEIVE_ERROR_CODE,
            BEEPER_QUEUE_OVERFLOW_ERROR_CODE,
            PACKET_TOO_LONG_ERROR_CODE
        };

    private static final String[ ] ERROR_CODE_STRINGS = new String[ ]
        {
            "Hardware error",
            "Calibration checksum error",
            "Hardware error",
            "Hardware error",
            "Transmit timeout",
            "Receive overflow (host did ot stop for XOFF)",
            "Receive error",
            "Beeper queue overflow",
            "Packet too long"
        };

    public Spaceball3003Packet()
    {
    }

    /**
     * Creates a simple request packet (packet which consists only of a header).
     * @param data buffer into which the packet will be copied;
     *    Packet size = 2 bytes.
     * @param offset first byte of packet will be copied at
     *      <code>data[offset]</code>
     * @param request header for the packet
     * @return packet size
     */
    public static int createEnableDevicePacket
    (
        final byte[ ]   data,
        final int       offset
    )
    {
        data[ offset ] = ( byte ) ENABLE_BALL_DATA;
        data[ offset + 1 ] = ( byte ) ENABLE_BALL_DATA >> 8;
        data[ offset + 2 ] = ( byte ) ENABLE_BALL_DATA >> 16;
        data[ offset + 3 ] = ( byte ) PACKET_TERMINATOR;
        return 4;
    }

    public static void decodeButtonEvent
    (
        final int[ ]    buttonData,
        final byte[ ]   readBuffer,
        final int       packetStart
    )
    {
        final int rcvdButtonData = Spaceball3003Packet.makeWord
        (
            readBuffer[ packetStart + 1 ],
            readBuffer[ packetStart + 2 ]
        );
        decodeButtonData( rcvdButtonData, buttonData, BUTTON_MASK );
    }

    /**
     * @param dataStart start of data packet in <i>readBuffer</i> excluding
     *    header
     * @param dataLength length of data packet excluding header and packet
     *    terminator
     */
    public static void doCallBacks
    (
        final byte[ ]                           readBuffer,
        final Spaceball3003CallbackInterface    caller,
        final int                               dataStart,
        final int                               dataLength,
        final int                               header
    )
    {
        //discard unknown packets
        switch( header )
        {
//            case ENABLE_BALL_DATA:
//            case DISABLE_BALL_DATA:
            case GET_REZERO_BALL:
            {
                caller.processRequestResponse( header );
                break;
            }
            case RESET_DEVICE:
            {
                final String text =
                    new String( readBuffer, dataStart, dataLength );
                final int line = Integer.parseInt( text . substring( 0, 1 ) );
                caller.processDeviceInfoResponse( header, line, text );
                break;
            }
            case ECHO:
            {
                final byte[ ]data = new byte[ dataLength ];
                System.arraycopy( readBuffer, dataStart, data, 0, dataLength );
                caller.processEchoResponse( data );
                break;
            }
            case ERROR:
            {
                decodeErrorPacket
                (
                    caller,
                    new String( readBuffer, dataStart, dataLength )
                );
                break;
            }
            default:
            {
                caller.processError
                (
                    RECEIVE_ERROR_CODE,
                    "Spaceball(TM) 2003/3003 device sent unknown packet."
                );
            }
        }
    }

    private static void decodeErrorPacket
    (
        final Spaceball3003CallbackInterface    caller,
        final String                            errorData
    )
    {
        final int numOfErrorCodes = ERROR_CODES.length;
        for( int i = 0; i < numOfErrorCodes; i ++ )
        {
            if( errorData.indexOf( ERROR_CODES[ i ] ) > 0 )
            {
                caller.processError
                (
                    ERROR_CODES[ i ],
                    "Spaceball(TM) 2003/3003: " + ERROR_CODE_STRINGS[ i ]
                );
            }
        }
    }
}