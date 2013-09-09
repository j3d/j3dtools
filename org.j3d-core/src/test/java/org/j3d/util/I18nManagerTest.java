/*****************************************************************************
 *                        Copyright j3d.org (c) 2009 - 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.util;

import java.util.Locale;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for the internationalisation manager.
 *
 * @author justin
 */
@Test(singleThreaded = true)
public class I18nManagerTest
{
    @Test(groups = "unit", singleThreaded = true)
    public void testBasicSetup() throws Exception
    {
        I18nManager classUnderTest = I18nManager.getManager();

        Locale result = classUnderTest.getFoundLocale();

        assertEquals(result, Locale.getDefault(), "Default locale not correctly set");
        assertNull(classUnderTest.getRequestedLanguage(), "Non-null default language");
        assertNull(classUnderTest.getRequestedLanguageVariant(), "Non-null default language variant");
        assertNull(classUnderTest.getRequestedCountry(), "Non-null default country");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidCountryLength() throws Exception
    {
        I18nManager classUnderTest = I18nManager.getManager();

        classUnderTest.changeLocale("en", "A", null);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidLanguage() throws Exception
    {
        I18nManager classUnderTest = I18nManager.getManager();

        classUnderTest.changeLocale("e", null, null);
    }

    @Test(groups = "unit")
    public void testSetLanguage() throws Exception
    {
        final String TEST_LANGUAGE = "es";

        I18nManager classUnderTest = I18nManager.getManager();


    }
}
