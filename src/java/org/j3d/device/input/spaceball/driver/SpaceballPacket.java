/*****************************************************************************
 * SpaceballPacket.java
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
 * Generates and decodes data packets for a Labtec's Spaceball device which
 * formats are independant of the type.<p>
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
 * Spaceball, Spaceball 2003, Spaceball 3003 and Spaceball 4000 are Trademarks
 *    of Labtec Inc.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 20. Aug. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public class SpaceballPacket
{
    // includes header, excludes packet terminator
    public static final int     POSITION_DATA_PACKET_LENGTH = 15;
    public static final int     BUTTON_DATA_PACKET_LENGTH = 3;

    public static final byte    BALL_DATA = 0x44; //"D"
    public static final byte    BUTTON_DATA = 0x4B; //"K"
    public static final byte    PATTERNED_BEEP = 0x42; //"B"
    public static final byte    ERROR = 0x45; //"E"
    public static final byte    ECHO = 0x25; //"%"
    /**
     * This command causes the Spaceball to RESET. This is a simple request
     * packet (packet which consists only of a header).Packet size = 2 bytes.
     */
    public static final byte    RESET_DEVICE = 0x40; //"@"

    //FIXME taken from Spaceball 4000 protocol, maybe needs change to support
    //all features (rezero button) for Spaceball 2003/3003
    public static final int[ ]  BUTTON_MASK = new int[ ]
            {
                0x0001, 0x0002, 0x0004, 0x0008, 0x0100, 0x0200,
                0x0400, 0x1000, 0x0000, 0x0000, 0x0000, 0x0000
            };

    public static final byte    PACKET_TERMINATOR = 0x0D;

    public SpaceballPacket()
    {

    }

    /**
     * This command causes the firmware to emit a beep.  The beep pattern is
     * determined by the data packets within the beep string.
     * @param data buffer into which the packet will be copied;
     *      packet size = 2 byte + length of "pattern"
     * @param offset first byte of packet will be copied at
     *      <code>data[offset]</code>
     * @param pattern Up to 16 upper and lower case letters. Letters that are
     *      sooner in the alphabet stand for shorter beeps or pauses. Lower case
     *      letters mean beep on, upper cae letters mean beep off. The last
     *      character must be beep off.
     * @return packet size
     */
    public static int createPatternedBeepPacket
    (
        final byte[ ]   data,
        final int       offset,
        final char[ ]  pattern
    )
    {
        data[ offset ] = PATTERNED_BEEP;
        final int length = pattern.length <= 16 ? pattern.length : 16;
        for( int i = 0; i < length; i ++ )
        {
            data[ offset + i + 1 ] = ( byte )pattern[ i ];
        }
        data[ offset + length + 1 ] = PACKET_TERMINATOR;
        return length + 2;
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
    public static int createSimpleRequestPacket
    (
        final byte[ ]   data,
        final int       offset,
        final byte      request
    )
    {
        data[ offset ] = request;
        data[ offset + 1 ] = PACKET_TERMINATOR;
        return 2;
    }

    /**
     * Echo this packet. The device responds by returning the same packet
     * received.
     * @param data buffer into which the packet will be copied;
     *      packet size = 2 byte + length of <code>packetData</code>.
     * @param offset first byte of packet will be copied at
     *      <code>data[offset]</code>
     * @param packetData Content of packet or null.
     *    NOTE: The length of the array + 2 must be smaller than the length of
     *    <code>data</code> - offset and the buffer used for receiving packets.
     *    The value 0x0D must not be contained.
     * @return packet size
     */
    public static int createEchoPacket
    (
        final byte[ ]   data,
        final int       offset,
        final byte[ ]   packetData
    )
    {
        data[ offset ] = ECHO;
        final int length = ( packetData == null ) ? 0 : packetData.length;
        for( int i = 0; i < length; i ++ )
        {
            data[ offset + i + 1 ] = packetData[ i ];
        }
        data[ offset + length + 1 ] = PACKET_TERMINATOR;
        return 2 + length;
    }

    /**
     * @param ballData Current position Data; size of array = 7.
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
    public static void decodeBallData
    (
        final byte[ ]   readBuffer,
        final int[ ]    ballData,
        int             packetStart
    )
    {
        ballData[ 0 ] = makeWord
        (
            readBuffer[ ++ packetStart ],
            readBuffer[ ++ packetStart ]
        );
        ballData[ 1 ] = makeWord
        (
            readBuffer[ ++ packetStart ],
            readBuffer[ ++ packetStart ]
        );
        ballData[ 2 ] = makeWord
        (
            readBuffer[ ++ packetStart ],
            readBuffer[ ++ packetStart ]
        );
        ballData[ 3 ] = - makeWord
        (
            readBuffer[ ++ packetStart ],
            readBuffer[ ++ packetStart ]
        );
        ballData[ 4 ] = - makeWord
        (
            readBuffer[ ++ packetStart ],
            readBuffer[ ++ packetStart ]
        );
        ballData[ 5 ] = makeWord
        (
            readBuffer[ ++ packetStart ],
            readBuffer[ ++ packetStart ]
        );
        ballData[ 6 ] = - makeWord
        (
            readBuffer[ ++ packetStart ],
            readBuffer[ ++ packetStart ]
        );
    }

    protected static void decodeButtonData
    (
        final int       data,
        final int[ ]    buttonData,
        final int[ ]    buttonMask
    )
    {
        final int length = buttonMask.length;
        for( int i = 0; i < length; i ++ )
        {
            if( ( data & buttonMask[ i ] ) > 0 )
            {
                buttonData[ i ] = 1;
            }
        }
    }

    protected static int makeWord( final byte firstByte, final byte secondByte )
    {
        short value = ( short )( firstByte << 8 );
        value |= ( short )( secondByte & 0xFF );
        return value;
    }
}