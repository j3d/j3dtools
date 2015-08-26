/*
 * j3d.org Copyright (c) 2001-2015
 *                                 Java Source
 *
 *  This source is licensed under the GNU LGPL v2.1
 *  Please read docs/LGPL.txt for more information
 *
 *  This software comes with the standard NO WARRANTY disclaimer for any
 *  purpose. Use it at your own risk. If there's a problem you get to fix it.
 */

package org.j3d.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import static org.testng.Assert.*;

/**
 * Unit tests for the SAX error handler code
 *
 * @author justin
 */
public class SAXErrorHandlerTest
{
    @Test(groups = "unit")
    public void testWarningToCustomStream() throws Exception
    {
        final String TEST_MESSAGE = "Something broke!";

        ByteArrayOutputStream bos = new ByteArrayOutputStream(100);
        PrintStream testStream = new PrintStream(bos);

        SAXErrorHandler class_under_test = new SAXErrorHandler(testStream);

        class_under_test.warning(new SAXParseException(TEST_MESSAGE, null));

        String raw_data = bos.toString();

        assertTrue(raw_data.contains(TEST_MESSAGE), "Test message not written to output");
    }

    @Test(groups = "unit")
    public void testWarningToSystemOut() throws Exception
    {
        final String TEST_MESSAGE = "Something broke again!";

        ByteArrayOutputStream bos = new ByteArrayOutputStream(100);
        PrintStream testStream = new PrintStream(bos);

        PrintStream existing_output = System.out;
        System.setOut(testStream);

        SAXErrorHandler class_under_test = new SAXErrorHandler();

        class_under_test.warning(new SAXParseException(TEST_MESSAGE, null));

        System.setOut(existing_output);

        String raw_data = bos.toString();

        assertTrue(raw_data.contains(TEST_MESSAGE), "Test message not written to output");
    }

    @Test(groups = "unit", expectedExceptions = SAXException.class)
    public void testError() throws Exception
    {
        final String TEST_MESSAGE = "Something broke again!";

        SAXErrorHandler class_under_test = new SAXErrorHandler();

        class_under_test.error(new SAXParseException(TEST_MESSAGE, null));
    }

    @Test(groups = "unit", expectedExceptions = SAXException.class)
    public void testFatal() throws Exception
    {
        final String TEST_MESSAGE = "Something broke again!";

        SAXErrorHandler class_under_test = new SAXErrorHandler();

        class_under_test.fatalError(new SAXParseException(TEST_MESSAGE, null));
    }
}
