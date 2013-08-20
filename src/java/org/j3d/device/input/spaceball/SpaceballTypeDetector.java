/*****************************************************************************
 * SpaceballTypeDetector.java
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
 * Class to automatically detect the type of the connected Spaceball device and
 * instantiate the appropriate driver.<p>
 * Currently Spaceball 2003, Spaceball 3003 and Spaceball 4000 are detected.<p>
 * Spaceball, Spaceball 2003, Spaceball 3003 and Spaceball 4000 are Trademarks
 *    of Labtec Inc.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 5. May 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski<p>
 */
public class SpaceballTypeDetector
{
    private static final int TYPECHECK_BUFFER_SIZE_IN_BYTE = 150;

    public SpaceballTypeDetector()
    {
    }

    /**
     * Creates the appropriate driver object, claims ownership over the port and
     * opens it for reading and writing. The following settings will be done:
     * Baudrate = 9600, 8 Databits, 1 Stopbit, no parity.<p>
     * Blocking until response from the Spaceball device received or timeout
     * period is over.<p>
     * In order to receive data {@link SpaceballDriver#enableDevice} must be
     * called.<p>
     * @param portDescriptor Must be a descriptor of a serial port.
     *      For Windows 95 and Windows 98 the Java communications API will
     *      always enumerate the serial ports COMM 1 through COMM 4 and the
     *      parallel ports LPT1 and LPT2. For Windows NT the Java communications
     *      API will enumerate the serial ports entered in the Registry and the
     *      parallel port LPT1 and LPT2.
     *      For Solaris the Java communications API enumerates both the actual
     *      port names and the aliases to the ports.
     * @param timeout for the Spaceball device response
     * @return appropriate driver for the connected Spaceball device (cast to
     *      specific type if special functions are needed)
     * @throws IOException If the device could not be detected.
     */
    public static SpaceballDriver createSpaceballDriver
    (
        final String    portDescriptor,
        final int       timeout
    )
    throws NoSuchPortException, PortInUseException, IOException,
        UnsupportedCommOperationException
    {
        SerialPort serialPort = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        SpaceballDriver driver = null;
        try
        {
            serialPort = SpaceballDriver.openPort( portDescriptor );
            inputStream = serialPort.getInputStream( );
            outputStream = serialPort.getOutputStream( );
            SpaceballDriver.pause( 2000 );
            driver = detectType
            (
                portDescriptor,
                serialPort,
                inputStream,
                outputStream
            );
        }
        catch( UnsupportedCommOperationException e1 )
        {
            if( serialPort != null )
            {
                serialPort.close( );
            }
            throw e1;
        }
        catch( IOException e2 )
        {
            if( inputStream != null )
            {
                inputStream.close( );
            }
            if( outputStream != null )
            {
                outputStream.close( );
            }
            if( serialPort != null )
            {
                serialPort.close( );
            }
            throw e2;
        }
        return driver;
    }

    private static SpaceballDriver detectType
    (
        final String        portDescriptor,
        final SerialPort    serialPort,
        final InputStream   inputStream,
        final OutputStream  outputStream
    )
    throws IOException
    {
        final SpaceballDriver driver;
        // get device descriptor (only Spaceball 4000)
        final byte[ ] writeBuffer = new byte[ 2 ];
        SpaceballPacket.createSimpleRequestPacket
        (
            writeBuffer,
            0,
            Spaceball4000Packet.GET_DEVICE_DESCRIPTOR
        );
        outputStream.write( writeBuffer );
        SpaceballDriver.pause( 500 );

        // check if the first 150 bytes received contain the string
        // "Spaceball". If "4000 FLX" is also contained then it's a
        // Spaceball 4000 device otherwise it's a Spaceball 2003/3003 device
        final byte[ ] readBuffer = new byte[ TYPECHECK_BUFFER_SIZE_IN_BYTE ];
        int currentByte = 0;
        long time = System.currentTimeMillis( );
        final long endTime = time + 1000;
        while( currentByte < TYPECHECK_BUFFER_SIZE_IN_BYTE && time < endTime )
        {
            if( inputStream.available() > 0 )
            {
                currentByte += inputStream.read
                (
                    readBuffer,
                    currentByte,
                    TYPECHECK_BUFFER_SIZE_IN_BYTE - currentByte
                );
            }
            time = System.currentTimeMillis( );
        }
        final String text =
            new String( readBuffer, 0, TYPECHECK_BUFFER_SIZE_IN_BYTE );
        // empty inputStream buffer
        while( inputStream.available( ) > 0 )
        {
            inputStream.read( readBuffer );
        }

//        System .out.println( text );

        if( text.indexOf( "Spaceball" ) != -1 )
        {
            if( text.indexOf( "4000 FLX" ) != -1 )
            {
                driver = new Spaceball4000Driver
                (
                    serialPort,
                    inputStream,
                    outputStream
                );
            }
            else
            {
                driver = new Spaceball3003Driver
                (
                    serialPort,
                    inputStream,
                    outputStream
                );
            }
        }
        else
        {
            throw new IOException(
                "Cannot detect supported Spaceball device on port "
                    + portDescriptor);
        }
        return driver;
    }
}