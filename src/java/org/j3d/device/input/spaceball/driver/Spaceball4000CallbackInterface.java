/*****************************************************************************
 * Spaceball4000CallbackInterface.java
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
 * Interface for processing events sent from Labtec's Spaceball 4000
 * device.<p>
 * Spaceball and Spaceball 4000 are Trademarks of Labtec Inc.
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version 14. Aug. 2001
 * Copyright (c) Dipl. Ing. Paul Szawlowski
 */
public interface Spaceball4000CallbackInterface
{
    /**
     * @param type use constants
     *      <UL type=disk>
     *          <LI><code>Spaceball4000Packet.EMIT_SINGLE_BEEP</code>
     *          <LI><code>Spaceball4000Packet.ENABLE_BALL_DATA</code>
     *          <LI><code>Spaceball4000Packet.DISABLE_BALL_DATA</code>
     *          <LI><code>Spaceball4000Packet.GET_REZERO_BALL</code>
     *      </UL>
     */
    public void processRequestResponse( final int type );

    /**
     * Method for processing device info events.
     * A device info may consist of up to 4 packets containing a string. This
     * method is called for each packet separately.
     * @param type use constants
     *      <UL type=disc>
     *          <li><code>Spaceball4000Packet.GET_DEVICE_DESCRIPTOR</code>
     *          <OL>
     *              <li>"Spaceball 4000 FLX"
     *              <li>"B:BB H PnP:p Az:a Sns:s OOOO WW"
     *              <UL type=disc>
     *                  <li>BB Number of buttons (in decimal)
     *                  <li>H Handedness of device (L for left, R for right)
     *                  <li>p 0 if PnP (Plug and Play) is disabled, 1 if enabled
     *                  <li>a 0 if AutoZero is disabled, 1 if enabled
     *                  <li>s Sensitivity type: S for standard, C for cubic
     *                  <li>OOOO AutoZero period in Hex (milliseconds)
     *                  <li>WW AutoZero window in raw reading units
     *              </UL>
     *              <li>"VX.xx created on mmm-dd-yyyy"
     *              <UL type=disc>
     *                  <li>X Major firmware version
     *                  <li>xx Minor firmware version
     *                  <li>mmm	Month firmware was created: Jan-Feb-Mar-Apr-May-
     *                      Jun-Jul-Aug-Sep-Nov-Dec)
     *                  <li>dd Day (0-31) firmware was created
     *                  <li>yyyy Year firmware was created
     *              </UL>
     *              <li>"Copyright(C) YYYY Spacetec IMC Corporation."
     *              <UL type=disc>
     *                  <li>YYYY Year firmware was created
     *              </UL>
     *          </OL>
     *      <li>
     *      <code>Spaceball4000Packet.GET_DEVICE_INFORMATION_RESPONSE</code>:
     *          "vFirmware version 2.42 created on 24-Oct-1997"
     *      <li><code>Spaceball4000Packet.RESET_DEVICE</code>: When power is
     *          applied to the device, it will perform its initialization.
     *          During this initialization the current position will be used as
     *          the Zero Position. The firmware will then wait a total of 2
     *          seconds before sending the following strings and emitting a
     *          double beep:
     *          <OL>
     *              <li>"Spaceball alive and well after a AAAAAAA reset."
     *              <UL type=disc>
     *                  <li>AAAAAAA may be one of the following Strings
     *                      depending on the type of reset detected: "poweron ",
     *                      "watchdog", "hardware", "software".
     *              </UL>
     *              <li>"Firmware version 2.42 created on 24-Oct-1997."
     *          </OL>
     *      </UL>
     * @param lineNumber starting at 1; <code>lineNumber</code> = 0 means error
     *      at decoding the line number
     * @param content the received data
     */
    public void processDeviceInfoResponse
    (
        final int       type,
        final int       lineNumber,
        final String    content
    );

    /**
     * Method for processing sensitivity Responses.
     * @param value use constants
     *      <UL>
     *          <LI><code>Spaceball4000Packet.CUBIC_SENSITIVITY_ENABLED</code>
     *          <LI>
     *          <code>Spaceball4000Packet.STANDARD_SENSITIVITY_ENABLED</code>
     *      </UL>
     *      other values indicate an error during decoding
     */
    public void processSensitivityResponse( final byte value );

    /**
     * Method for processing auto rezero responses.
     * @param autoZeroPeriod [ms] 0 <= <code>autoZeroPeriod</code> <= 0xFFFF
     * @param autoZeroWindow [raw reading units] 0 <=
     *      <code>autoZeroWindow</code> < 0xFF
     * @param enabled <code> true</code> if auto-rezero is enabled
     */
    public void processAutoRezeroResponse
    (
        final int      autoZeroPeriod,
        final int      autoZeroWindow,
        final boolean  enabled
    );

    /**
     * Method to process the handedness response.
     * @param handedness use constants
     *  <UL>
     *      <LI><code>Spaceball4000Packet.LEFT</code>
     *      <LI><code>Spaceball4000Packet.RIGHT</code>
     *  </UL>
     */
    public void setHandedness( final int handedness );

   /**
    * Method to process the number of buttons of the device.
    * @param numOfButtons
    */
   public void setNumOfButtons( final int numOfButtons );

    /**
     * Method to process error responses.
     * @param errorCode use constants
     *      <UL>
     *          <LI><code>Spaceball4000Packet.ECLIPSE_REGISTER_ERROR_CODE</code>
     *          <LI><code>Spaceball4000Packet.ECLIPSE_REGISTER_ERROR_CODE</code>
     *          <LI>
     *      <code>Spaceball4000Packet.EEPROM_CHECKSUM_INCORRECT_ERROR_CODE</code>
     *          <LI>
     *          <code>Spaceball4000Packet.ECLIPSE_TIMED_OUT_ERROR_CODE</code>
     *          <LI><code>Spaceball4000Packet.TRANSMIT_TIMEOUT_ERROR_CODE</code>
     *          <LI>
     *      <code>Spaceball4000Packet.RECEIVE_QUEUE_OVERFLOW_ERROR_CODE</code>
     *          <LI><code>Spaceball4000Packet.RECEIVE_ERROR_ERROR_CODE</code>
     *          <LI><code>Spaceball4000Packet.PACKET_TOO_LONG_ERROR_CODE</code>
     *          <LI><code>Spaceball4000Packet.PACKET_IGNORED_ERROR_CODE</code>
     *          <LI>
     *          <code>Spaceball4000Packet.COMMAND_UNRECOGNIZED_ERROR_CODE</code>
     *      </UL>
     */
    public void processError( final int errorCode, final String errorString );

    /**
     * Method to process echo respones.
     */
    public void processEchoResponse( final byte[ ] data );
}