/*****************************************************************************
 * Spaceball3003CallbackInterface.java
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
 * Interface for processing events sent from Labtec's Spaceball 3003/2003
 * device.<p>
 * NOTE: Implementation not finished and not tested. Any feedback welcome.<p>
 * Spaceball, Spaceball 2003 and Spaceball 3003 are Trademarks of Labtec Inc.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 21. Aug. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski
 */
public interface Spaceball3003CallbackInterface
{
    /**
     * @param type use constants
     *      <UL type=disk>
     *          <LI><code>Spaceball3003Packet.GET_REZERO_BALL</code>
     *      </UL>
     */
    public void processRequestResponse( final int type );

    public void processDeviceInfoResponse
    (
        final int       type,
        final int       lineNumber,
        final String    content
    );

   /**
    * Method to process error responses.
    * @param errorCode use constants
    *   <UL>
    *       <LI><code>Spaceball3003Packet.</code>
    *       <LI><code>Spaceball3003Packet.HARDWARE_ERROR_CODE_1</code>
    *       <LI><code>Spaceball3003Packet.CALIBRATION_CHECKSUM_ERROR_CODE</code>
    *       <LI><code>Spaceball3003Packet.HARDWARE_ERROR_CODE_2</code>
    *       <LI><code>Spaceball3003Packet.HARDWARE_ERROR_CODE_3</code>
    *       <LI><code>Spaceball3003Packet.TRANSMIT_TIMEOUT_ERROR_CODE</code>
    *       <LI>
    *       <code>Spaceball3003Packet.RECEIVE_OVERFLOW_ERROR_ERROR_CODE</code>
    *       <LI><code>Spaceball3003Packet.RECEIVE_ERROR_CODE</code>
    *       <LI><code>
    *       Spaceball3003Packet.BEEPER_QUEUE_OVERFLOW_ERROR_CODE</code>
    *       <LI><code>Spaceball3003Packet.PACKET_TOO_LONG_ERROR_CODE</code>
    *   </UL>
    */
    public void processError( final int errorCode, final String errorString );

    /**
     * Method to process echo respones.
     */
    public void processEchoResponse( final byte[ ] data );
}