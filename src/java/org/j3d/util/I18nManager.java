/*****************************************************************************
 *                        Copyright j3d.org (c) 2009
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

// External imports
import java.util.prefs.*;

import java.util.Locale;
import java.util.ResourceBundle;

// Internal imports
// None

/**
 * Manager of all internationalisation in the toolkit.
 * <p>
 *
 * This class acts as a singleton manager for internationalisation within
 * j3d.org Code and all applications built on it. It may be used for any string
 * resource that may require internationalisation - not just text on buttons
 * but also error messages, log messages etc.
 *
 * <p>
 * <b>External Application Usage</b>
 * <p>
 * An application should initialise this manager as soon as possible after
 * startup - ideally before anything else happens. The initialisation ensures
 * that the right resources are loaded before people start making queries.
 * The initialisation process should provide an application name as a
 * reference point for internationalisation preferences that are stored to
 * preserve settings between runs of the application. In no application name
 * is provided, then the preferences are not stored and it will need to be
 * reset each time the application runs. The reason for the application name
 * is that this manager is shared in a toolkit with many different potential
 * end applications making use of it. The application names allows us to
 * create separate sets of settings on a per-application basis rather than
 * relying on global settings for the user's computer.
 * <p>
 *
 * The second direct customisation point is to provide a base name for the
 * resource bundle to be loaded. If no name is provided, it will load the
 * resource bundle named <code>config.i18n.j3dorgResources</code> from
 * the classpath. This class follows the normal naming conventions that are
 * defined in {@link java.util.ResourceBundle}. Any application providing
 * their own resource bundle must provide values for every chefx3d resource
 * that is specified in the base file that comes with this toolkit. Failure
 * to do so will result in blank areas on the user interface and most
 * probably an unusable application.
 * <p>
 *
 * If an application wishes to also internationalise other settings, such
 * as numerical representations, then you can query this class for the
 * Locale it actually loaded, rather than the one you requested (fairly
 * typical issue).
 * <p>
 *
 * <b>Note:</b>
 * <p>
 * Because this class just stores string values, it is entirely possible
 * to use it to store non-text information as well for a more generic
 * internationalisation system. For example, storing the names of icon
 * files could be a valid use of this class, allowing localisation for
 * more than just text, but also graphical resources.
 *
 * <p>
 * <b>Resource Bundle Property Naming Conventions</b>
 * </p>
 *
 * With a property file containing all the internationalisation of a
 * probably very large application, naming conventions are a necessity.
 * Properties that are internal to any j3d.org-specific user interface
 * element will start with the package name, class name (case included)
 * and finally the resource property name after that.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class I18nManager {

    /**
     * Error message when the language code identifier provided is not 2
     * characters in length, as required by Locales.
     */
    private static final String INVALID_LANG_LENGTH =
        "The language code is not the required 2 characters in length: ";

    /**
     * Error message when the country code identifier provided is not 2
     * characters in length, as required by Locales.
     */
    private static final String INVALID_COUNTRY_LENGTH =
        "The country code is not the required 2 characters in length: ";

    /** Name of the preference we are looking for for the country code */
    private static final String COUNTRY_PREF = "countryCode";

    /** Name of the preference we are looking for for the country code */
    private static final String LANGUAGE_PREF = "languageCode";

    /** Name of the preference we are looking for for the country code */
    private static final String VARIANT_PREF = "variantCode";

    /** The name of the default resource bundle to load */
    private static final String DEFAULT_RESOURCES_FILE =
        "config.i18n.j3dorgResources";

    /** The global manager instance shared by everyone */
    private static I18nManager instance;

    /** The current application node for fetching preferences from */
    private String applicationName;

    /** Resource bundle name to load, if overriden by the end user */
    private String resourceFileName;

    /** The currently set country code */
    private String countryCode;

    /** The currently set language code */
    private String languageCode;

    /** Variange code of the language, if needed */
    private String variantCode;

    /** The resource bundle with all our internationalised strings */
    private ResourceBundle stringResources;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private I18nManager() {
        Preferences prefs = Preferences.userNodeForPackage(I18nManager.class);

        countryCode = prefs.get(COUNTRY_PREF, null);
        languageCode = prefs.get(LANGUAGE_PREF, null);
        variantCode = prefs.get(VARIANT_PREF, null);

        Locale locale = findLocale();

        resourceFileName = DEFAULT_RESOURCES_FILE;
        stringResources = ResourceBundle.getBundle(resourceFileName,
                                                   locale);
    }

    /**
     * Get the global instance of the internationalisation manager for the
     * given root class.
     */
    public static I18nManager getManager() {

        if(instance == null)
            instance = new I18nManager();

        return instance;
    }

    /**
     * Set the manager to use the given application's set of localisation
     * settings. This method should only be called once during the entire
     * lifetime of the application - ideally at the very start before any
     * other users of this class will have had a chance to read it. Setting
     * this value ensures that the preferences are stored per end user
     * application of this class, and is not generic to all installed
     * applications on the end user's computer that makes use of this library
     * All references to get strings after this call will then use this
     * specific application's language settings without need for further
     * qualification. Setting null removes the application and sets the
     * defaults for all users of this library that don't explicitly set an
     * application name.
     *
     * @param appName A name string describing the end user application
     * @param resourceFile If not null, use this as the resource bundle
     *    base name to be fetched, rather than the default file name
     */
    public void setApplication(String appName, String resourceFile) {

        Preferences main_prefs =
            Preferences.userNodeForPackage(I18nManager.class);

        boolean node_check_ok = false;

        try {
            node_check_ok = main_prefs.nodeExists(appName);
        } catch(BackingStoreException bse) {
            // ignore this exception and treat it like we couldn't find
            // it.
        }

        if(node_check_ok) {
            Preferences prefs = main_prefs.node(appName);

            countryCode = prefs.get(COUNTRY_PREF, null);
            languageCode = prefs.get(LANGUAGE_PREF, null);
            variantCode = prefs.get(VARIANT_PREF, null);
        } else {
            // It doesn't exist, so force the creation but use the
            // default platform locale settings
            Preferences prefs = main_prefs.node(appName);

            countryCode = null;
            languageCode = null;
            variantCode = null;
        }

        applicationName = appName;

        resourceFileName = resourceFile == null ?
                           DEFAULT_RESOURCES_FILE :
                           resourceFile;

        Locale locale = findLocale();
        stringResources = ResourceBundle.getBundle(resourceFileName,
                                                   locale);
    }

    /**
     * Get the currently set application string that langauge settings are
     * being used for. If none is currently set, return null.
     *
     * @return The current application name or null if not set
     */
    public String getApplication() {
        return applicationName;
    }

    /**
     * Get the name of the resource file that is being loaded. If none is
     * set, it will return the default file name used by this library.
     *
     * @return The name of the base resource file used for text strings
     */
    public String getResourceName() {
        return resourceFileName;
    }

    /**
     * Manually change the locale to the given country and language settings
     * to overrided the current settings. This will be stored in user
     * preferences and used for any subsequent accesses to this manager, and
     * any time after this that the application is started, if and only if
     * an application name has been set. If no application name is set, this
     * will persist for the lifetime of this application instance, but be lost
     * on restart.
     * <p>
     * Setting the language value to null will reset the system back to the
     * default platform setting. The other arguments may be null or specified.
     * If null, the defaults for those are used from the local platform
     * settings.
     * <p>
     * Language and country strings are required to be the 2 letter identifiers
     * used in the ISO specifications. See the documentation of
     * {@link java.util.Locale} for more information about valid codes. The
     * method will check for appropriate length codes and issue an exception
     * if they are not two characters.
     *
     * @param language The language identifier to load
     * @param country The optional country code to load
     * @param variant The optional language variant to load
     * @throws IllegalArgument exception if the country or language codes are
     *   not correctly formated (length and case)
     * @see java.util.Locale
     * @see http://www.loc.gov/standards/iso639-2/englangn.html
     * @see http://www.davros.org/misc/iso3166.txt
     */
    public void changeLocale(String language, String country, String variant)
        throws IllegalArgumentException {

        if(language != null && language.length() != 2)
            throw new IllegalArgumentException(INVALID_LANG_LENGTH + language);

        if(country != null && country.length() != 2)
            throw new IllegalArgumentException(INVALID_COUNTRY_LENGTH + country);

        // Probably also want to check for correct case here.

        // If we have an application name set, store these preferences
        // for that application. Otherwise, ignore the requests
        if(applicationName != null) {
            Preferences main_prefs =
                Preferences.userNodeForPackage(I18nManager.class);

            Preferences prefs = main_prefs.node(applicationName);

            if(country == null)
                prefs.remove(COUNTRY_PREF);
            else
                prefs.put(COUNTRY_PREF, country);

            if(language == null)
                prefs.remove(LANGUAGE_PREF);
            else
                prefs.put(LANGUAGE_PREF, language);

            if(variant == null)
                prefs.remove(VARIANT_PREF);
            else
                prefs.put(VARIANT_PREF, variant);
        }

        languageCode = language;
        countryCode = country;
        variantCode = variant;

        Locale locale = findLocale();

        stringResources = ResourceBundle.getBundle(resourceFileName,
                                                   locale);
    }

    /**
     * Get the loaded locale that was used for the resources file
     * This can be used to create matching other internationalisation
     * implementation details such as currency and number formatters.
     *
     * @return The locale corresponding to the loaded resource files
     */
    public Locale getFoundLocale() {
        return stringResources.getLocale();
    }

    /**
     * Get the language code that is currently set.
     *
     * @return The current language code or null if not set
     */
    public String getRequestedLanguage() {
        return languageCode;
    }

    /**
     * Get the country code that is currently set.
     *
     * @return The current country code or null if not set
     */
    public String getRequestedCountry() {
        return countryCode;
    }

    /**
     * Get the language variant setting that is currently set.
     *
     * @return The current language variant code or null if not set
     */
    public String getRequestedLanguageVariant() {
        return variantCode;
    }

    /**
     * Get the localised string for the given property name.
     */
    public String getString(String property) {
        return stringResources.getString(property);
    }

    /**
     * Internal convenience method to load a locale based on the currently
     * set country and language settings.
     *
     * @return The new locale for the current settings
     */
    private Locale findLocale() {

        Locale locale;

        if(languageCode == null) {
            locale = Locale.getDefault();
        } else if(countryCode == null) {
            locale = new Locale(languageCode);
        } else if(variantCode == null) {
            locale = new Locale(languageCode, countryCode);
        } else {
            locale = new Locale(languageCode, countryCode, variantCode);
        }

        return locale;
    }
}
