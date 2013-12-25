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

import static org.testng.Assert.*;

/**
 * Unit tests for the internationalisation manager.
 *
 * @author justin
 */
@Test(singleThreaded = true)
public class I18nManagerTest
{
    @Test(groups = "unit")
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
        classUnderTest.changeLocale(TEST_LANGUAGE, null, null);

        assertEquals(classUnderTest.getRequestedLanguage(), TEST_LANGUAGE, "Wrong language set after change");
    }

    @Test(groups = "unit", dependsOnMethods = "testSetLanguage")
    public void testSetCountry() throws Exception
    {
        final String TEST_LANGUAGE = "en";
        final String TEST_COUNTRY = "US";

        I18nManager classUnderTest = I18nManager.getManager();
        classUnderTest.changeLocale(TEST_LANGUAGE, TEST_COUNTRY, null);

        assertEquals(classUnderTest.getRequestedCountry(), TEST_COUNTRY, "Wrong country set after change");
    }

    @Test(groups = "unit", dependsOnMethods = "testBasicSetup")
    public void testClearSettings() throws Exception {
        Locale defaultLocale = Locale.getDefault();

        final String TEST_LANGUAGE = "es";
        final String TEST_COUNTRY = "MX";

        I18nManager classUnderTest = I18nManager.getManager();
        classUnderTest.changeLocale(TEST_LANGUAGE, TEST_COUNTRY, null);

        Locale expectedLocale = new Locale(TEST_LANGUAGE, TEST_COUNTRY);

        assertEquals(classUnderTest.getFoundLocale(), expectedLocale, "Wrong set locale");

        classUnderTest.clearLocale();

        assertNotEquals(classUnderTest.getFoundLocale(), expectedLocale, "Still has set locale after clearing");
        assertEquals(classUnderTest.getFoundLocale(), defaultLocale, "Didn't reset to default locale");
    }

    @Test(groups = "unit")
    public void testMultiFileResourceHandling() throws Exception
    {
        final String TEST_APP_NAME = "J3D_UNIT_TEST_APP";
        final String TEST_RESOURCE_FILE_1 = "config.i18n.testResources1";
        final String TEST_RESOURCE_FILE_2 = "config.i18n.testResources2";
        final String TEST_PROP_NAME_1 = "test.properties.first";
        final String TEST_PROP_NAME_2 = "test.properties.second";

        I18nManager classUnderTest = I18nManager.getManager();
        classUnderTest.setApplication(TEST_APP_NAME, TEST_RESOURCE_FILE_1);

        // validate we can't find the second resource yet.
        assertNotNull(classUnderTest.getString(TEST_PROP_NAME_1), "Didn't find initial resource");
        assertNull(classUnderTest.getString(TEST_PROP_NAME_2), "Found the second resource before file loaded");

        classUnderTest.addResource(TEST_RESOURCE_FILE_2);

        assertNotNull(classUnderTest.getString(TEST_PROP_NAME_1), "Initial resource lost after load");
        assertNotNull(classUnderTest.getString(TEST_PROP_NAME_2), "Didn't find second resource after load");
    }

    @Test(groups = "unit", expectedExceptions = IllegalStateException.class)
    public void testAddResourceNoAppSet() throws Exception
    {
        I18nManager classUnderTest = I18nManager.getManager();
        classUnderTest.addResource("some.class.name");
    }

    @Test(groups = "unit", dependsOnMethods = "testClearSettings")
    public void testChangeLocale() throws Exception
    {
        final String TEST_LANGUAGE = "es";
        final String TEST_APP_NAME = "J3D_UNIT_TEST_APP";
        final String TEST_RESOURCE_FILE = "config.i18n.testResources1";
        final String TEST_PROP_NAME = "test.properties.first";

        I18nManager classUnderTest = I18nManager.getManager();
        classUnderTest.setApplication(TEST_APP_NAME, TEST_RESOURCE_FILE);

        String result = classUnderTest.getString(TEST_PROP_NAME);

        assertNotNull(result, "Initial property in default locale not found");

        classUnderTest.changeLocale(TEST_LANGUAGE, null, null);

        assertNotEquals(result, classUnderTest.getString(TEST_PROP_NAME), "Property wrong after locale change");
    }
}
