/*****************************************************************************
 * Spaceball4000Packet.java
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

package org.j3d.device.input.spaceball.driver;

/**
 * Generates and decodes data packets for Labtec's Spaceball 4000 device
 * according to the Spaceball 4000 Packet Protocol v. 1.2. <p>
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
 * Spaceball and Spaceball 4000 are Trademarks of Labtec Inc.
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 20. Aug. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public class Spaceball4000Packet extends SpaceballPacket
{
    public static final int     ECLIPSE_REGISTER_ERROR_CODE = 0x0001;
    public static final int     EEPROM_CHECKSUM_INCORRECT_ERROR_CODE = 0x0002;
    public static final int     ECLIPSE_TIMED_OUT_ERROR_CODE = 0x0004;
    public static final int     TRANSMIT_TIMEOUT_ERROR_CODE = 0x0008;
    public static final int     RECEIVE_QUEUE_OVERFLOW_ERROR_CODE = 0x0010;
    public static final int     RECEIVE_ERROR_ERROR_CODE = 0x0020;
    public static final int     PACKET_TOO_LONG_ERROR_CODE = 0x0100;
    public static final int     PACKET_IGNORED_ERROR_CODE = 0x0200;
    public static final int     COMMAND_UNRECOGNIZED_ERROR_CODE = 0x0400;

    /**
     * This command causes the Spaceball 4000 to single beep. This is a
     * simple request packet (packet which consists only of a header).
     * Packet size = 2 bytes.
     */
    public static final byte    EMIT_SINGLE_BEEP = 0x53;
    /**
     * This command enables data packets. The Spaceball device will not send any
     * ball movement packets until this command is sent. This is a simple
     * request packet (packet which consists only of a header).Packet size =
     * 2 bytes.
     */
    public static final byte    ENABLE_BALL_DATA = 0x4D;
    /**
     * This command disables data packets. The Spaceball device will cease to
     * send ball movement packets after this command is sent. This is a simple
     * request packet (packet which consists only of a header).Packet size =
     * 2 bytes.
     */
    public static final byte   DISABLE_BALL_DATA = 0x2D;
    /**
     * This command requests the Spaceball 4000-specific reset string. This is a
     * simple request packet (packet which consists only of a header).
     * Packet size = 2 bytes.
     */
    public static final byte    GET_DEVICE_DESCRIPTOR = 0x22;
    /**
     * This command causes the Spaceball to rezero. It takes the current
     * position as the Powersensor rest position.  All subsequent measurements
     * (until reset) are taken relative to this position. This is a simple
     * request packet (packet which consists only of a header).
     * Packet size = 2 byte.
     */
    public static final byte    GET_REZERO_BALL = 0x5A;
    /**
     * This command causes the Spaceball to RESET. This is a simple request
     * packet (packet which consists only of a header).Packet size = 2 bytes.
     */
//   public static final byte  RESET_DEVICE = 0x40;
//   public static final byte  BALL_DATA = 0x44;
//   public static final byte  BUTTON_DATA = 0x4B;
    public static final byte    ADVANCED_BUTTON_DATA = 0x2E;
//   public static final byte  PATTERNED_BEEP = 0x42;
    public static final byte    CUBIC_SENSITIVITY_ENABLE_DISABLE = 0x59;
//   public static final byte  ECHO = 0x25;
//   public static final byte  ERROR = 0x45; /"E"
    public static final byte    ENABLE_DISABLE_AUTO_REZERO = 0x41;
    public static final byte    CHANGE_AUTOMATIC_ZERO_PARAMETERS = 0x41;
    public static final byte    AUTO_REZERO_RESPONSE = 0x61;
    /**
     * This command requests the firmware version and build date. This is a
     * simple request packet (packet which consists only of a header).
     * Packet size = 2 bytes.
     */
    public static final byte    GET_DEVICE_INFORMATION = 0x68;
    public static final byte    GET_DEVICE_INFORMATION_RESPONSE = 0x48;

    /**
     * constant for left handedness
     */
    public static final int     LEFT = 0x4C;
    /**
     * constant for right handedness
     */
    public static final int     RIGHT = 0x52;

    /**
     * constant for cubic sensitivity
     */
    public static final byte    CUBIC_SENSITIVITY_ENABLED = 0x43; // 'C'
    /**
     * constant for standard sensitivity
     */
    public static final byte    STANDARD_SENSITIVITY_ENABLED = 0x53; // 'S'

    private static final byte   AUTO_REZERO_ENABLED = 0x45; // 'E'
    private static final byte   AUTO_REZERO_DISABLED = 0x44; // 'D'


   // includes header, excludes packet terminator
//   private static final int   POSITION_DATA_PACKET_LENGTH = 15;
//   private static final int   BUTTON_DATA_PACKET_LENGTH = 3;
    private static final int    DESCRIPTOR_DATA_PACKET_LENGTH = 34;

    private static final int[ ] ERROR_CODES = new int[ ]
            {
                ECLIPSE_REGISTER_ERROR_CODE,
                EEPROM_CHECKSUM_INCORRECT_ERROR_CODE,
                ECLIPSE_TIMED_OUT_ERROR_CODE,
                TRANSMIT_TIMEOUT_ERROR_CODE,
                RECEIVE_QUEUE_OVERFLOW_ERROR_CODE,
                RECEIVE_ERROR_ERROR_CODE,
                PACKET_TOO_LONG_ERROR_CODE,
                PACKET_IGNORED_ERROR_CODE,
                COMMAND_UNRECOGNIZED_ERROR_CODE
            };

    private static final String[ ]  ERROR_CODE_STRINGS = new String[ ]
            {
                "Eclipse Register Error",
                "Eeprom checksum incorrect",
                "Eclipse timed Out",
                "Transmit timeout",
                "Receive queue overflow",
                "Receive error",
                "Packet too long",
                "Packet Ignored",
                "Command unrecognized"
            };

    private static final int[ ]     ADVANCED_BUTTON_MASK = new int[ ]
            {
                0x0001, 0x0002, 0x0004, 0x0008, 0x0010, 0x0020,
                0x0080, 0x0100, 0x0200, 0x0400, 0x0800, 0x1000
            };

    private static final int        LEFT_HANDEDNESS_DESCRIMINATOR = 0x2000;

    /**
     * This command causes the Spaceball 4000 to use or not to use the cubic
     * sensitivity curve for the ball data transmitted.
     * @param data buffer into which the packet will be copied;
     *      packet size = 3 byte
     * @param offset first byte of packet will be copied at "data[offset]"
     * @param enable true: cubic sensitivity will be enabled
     * @return packet size
     */
    public static int createCubicSensitivityEnableDisablePacket
    (
        final byte[ ]   data,
        final int       offset,
        final boolean   enable
    )
    {
        data[ offset ] = CUBIC_SENSITIVITY_ENABLE_DISABLE;
        data[ offset + 1 ] = ( byte )( enable ? 0x43 : 0x53 );
        data[ offset + 2 ] = PACKET_TERMINATOR;
        return 3;
    }

    /**
     * This command causes the Spaceball 4000 not to use the auto-rezero
     * function in the device.
     * @param data buffer into which the packet will be copied;
     *    packet size = 3 byte
     * @param offset first byte of packet will be copied at "data[offset]"
     * @param enable true: auto rezero will be enabled
     * @return packet size
     */
    public static int createEnableDisableAutoRezeroPacket
    (
        final byte[ ]   data,
        final int       offset,
        final boolean   enable
    )
    {
        data[ offset ] = ENABLE_DISABLE_AUTO_REZERO;
        data[ offset + 1 ] = ( byte )( enable ? 0x45 : 0x44 );
        data[ offset + 2 ] = PACKET_TERMINATOR;
        return 3;
    }

    /**
     * Sets a flag in the EEPROM to enable automatic rezero.
     * @param data buffer into which the packet will be copied;
     *      packet size = 8 byte
     * @param offset first byte of packet will be copied at "data[offset]"
     * @param autoZeroPeriod [ms] 0 <= "autoZeroPeriod" <= 0xFFFF Set to zero to
     *      disable automatic zeroing capabilities.
     * @param autoZeroWindow [raw reading units] 0 <= "autoZeroWindow" <= 0xFF
     * @return packet size
     */
    public static int createChangeAutomaticZeroParametersPacket
    (
        final byte[ ]   data,
        final int       offset,
        final int       autoZeroPeriod,
        final int       autoZeroWindow
    )
    {
        for( int i = 1; i < 7; i ++ )
        {
            data[ offset + i ] = 0x30;
        }
        final String pString = Integer.toHexString( autoZeroPeriod );
        final int pStringLength = pString.length( );
        final String wString = Integer.toHexString( autoZeroWindow );
        final int wStringLength = wString.length( );

        data[ offset ] = CHANGE_AUTOMATIC_ZERO_PARAMETERS;

        for( int i = 0; i < pStringLength; i ++ )
        {
            data[ offset + 5 - pStringLength + i ] =
                ( byte ) pString.charAt( i );
        }
        for( int i = 0; i < wStringLength; i ++ )
        {
            data[ offset + 7 - wStringLength + i ] =
                ( byte ) wString.charAt( i );
        }

        data[ offset + 7 ] = PACKET_TERMINATOR;
        return 8;
    }

    public static void decodeButtonEvent
    (
        final int[ ]                            buttonData,
        final byte[ ]                           readBuffer,
        final Spaceball4000CallbackInterface    caller,
        final int                               packetStart
    )
    {
        final int rcvdButtonData = Spaceball4000Packet.makeWord
        (
            readBuffer[ packetStart + 1 ],
            readBuffer[ packetStart + 2 ]
        );
        decodeButtonData( rcvdButtonData, buttonData, ADVANCED_BUTTON_MASK );
        final int handedness =
            ( rcvdButtonData & LEFT_HANDEDNESS_DESCRIMINATOR ) == 0 ?
            LEFT :
            RIGHT;
        caller.setHandedness( handedness );
    }

    public static void doCallBacks
    (
        final byte[ ]                          readBuffer,
        final Spaceball4000CallbackInterface   caller,
        final int                              dataStart,
        final int                              dataLength,
        final int                              header
    )
    {
        //discard unknown packets
        switch( header )
        {
            case EMIT_SINGLE_BEEP:
            case ENABLE_BALL_DATA:
            case DISABLE_BALL_DATA:
            case GET_REZERO_BALL:
            {
                caller.processRequestResponse( header );
                break;
            }
            case AUTO_REZERO_RESPONSE:
            {
                // decoding like this could be a problem when a corrupted
                // packet is decoded.
                // Disable = AUTO_REZERO_DISABLED
                final boolean enabled =
                    ( readBuffer[ dataStart + 6 ] == AUTO_REZERO_ENABLED );

                final String values =
                    new String( readBuffer, dataStart, dataLength - 1 );
                final int autoZeroPeriod =
                    Integer.parseInt( values . substring( 0, 4 ), 16 );
                final int autoZeroWindow =
                    Integer.parseInt( values . substring( 4, 6 ), 16 );
                caller.processAutoRezeroResponse
                (
                    autoZeroPeriod,
                    autoZeroWindow,
                    enabled
                );
                break;
            }
            case CUBIC_SENSITIVITY_ENABLE_DISABLE:
            {
                caller.processSensitivityResponse( readBuffer[ dataStart ] );
                break;
            }
            case GET_DEVICE_DESCRIPTOR:
            {
                final String text =
                    new String( readBuffer, dataStart, dataLength );
                final int line = Integer.parseInt( text . substring( 0, 1 ) );
                // skip dcoding if length of packet does not match
                if( ( line == 2 )
                    && ( ( dataLength + 1 ) == DESCRIPTOR_DATA_PACKET_LENGTH ) )
                {
                    decodeDeviceDescriptor( caller, text );
                }
                caller.processDeviceInfoResponse( header, line, text );
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
            case GET_DEVICE_INFORMATION_RESPONSE:
            {
                final String text =
                    new String( readBuffer, dataStart, dataLength );
                caller.processDeviceInfoResponse( header, 1, text );
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
                    makeWord
                    (
                        readBuffer[ dataStart ],
                        readBuffer[ dataStart + 1 ]
                    )
                );
                break;
            }
            default:
            {
                caller.processError
                (
                    COMMAND_UNRECOGNIZED_ERROR_CODE,
                    "Spaceball (TM) 4000 device sent unknown packet."
                );
            }
        }
    }

    private static void decodeErrorPacket
    (
        final Spaceball4000CallbackInterface    caller,
        final int                               errorData
    )
    {
        for( int i = 0; i < ERROR_CODES.length; i ++ )
        {
            if( ( errorData & ERROR_CODES[ i ] ) > 0 )
            {
                caller.processError
                (
                    ERROR_CODES[ i ],
                    "Spaceball (TM) 4000: " + ERROR_CODE_STRINGS[ i ]
                );
            }
        }
    }

    private static void decodeDeviceDescriptor
    (
        final Spaceball4000CallbackInterface    caller,
        final String                            description
    )
    {
        final int numOfButtons =
            Integer.parseInt( description.substring( 4, 6 ) );
        final int handedness = description.charAt( 7 );

        // decoding like this could be a problem when a corrupted packet is
        // decoded; disabled = '0'
        final boolean isAutoZeroEnabled = ( description.charAt( 18 ) == '1' );

        final byte sensitivity = ( byte ) description.charAt( 24 );

        final int autoZeroPeriod =
            Integer.parseInt( description.substring( 26, 30 ) );
        final int autoZeroWindow =
            Integer.parseInt( description.substring( 31, 33 ) );
        caller.setHandedness( handedness );
        caller.setNumOfButtons( numOfButtons );
        caller.processAutoRezeroResponse
        (
            autoZeroPeriod,
            autoZeroWindow,
            isAutoZeroEnabled
        );
        caller.processSensitivityResponse( sensitivity );
    }
}