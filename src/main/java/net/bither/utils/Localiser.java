/**
 * Copyright 2011 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bither.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class Localiser {

    private static final Logger log = LoggerFactory.getLogger(Localiser.class);


    public static final String BITHER_RESOURCE_BUNDLE_NAME = "viewer";

    public static final String VERSION_PROPERTY_KEY_NAME = "version";
    public static final String VERSION_PROPERTIES_FILENAME = "/version.properties";

    private ResourceBundle resourceBundle;
    private MessageFormat formatter;
    private Properties versionProperties;

    private final static String MISSING_RESOURCE_TEXT = "Missing resource : ";

    private NumberFormat numberFormat;
    private NumberFormat numberFormatNotLocalised;
    private Locale locale;

    public static final int NUMBER_OF_FRACTION_DIGITS_FOR_BITCOIN = 8;

    /**
     * Localiser hardwired to English - mainly for testing
     */
    public Localiser() {
        this(new Locale("en"));
    }

    /**
     * Create a Localiser using a ResourceBundle based on the specified
     * 'bundleName' with Locale 'locale'.
     *
     * @param locale
     */
    public Localiser(Locale locale) {
        formatter = new MessageFormat("");


        setLocale(locale);

        numberFormat = NumberFormat.getInstance(locale);
        numberFormat.setMaximumFractionDigits(NUMBER_OF_FRACTION_DIGITS_FOR_BITCOIN);
        numberFormatNotLocalised = NumberFormat.getInstance(Locale.ENGLISH);
        numberFormatNotLocalised.setMaximumFractionDigits(NUMBER_OF_FRACTION_DIGITS_FOR_BITCOIN);
        numberFormatNotLocalised.setGroupingUsed(false);


    }

    synchronized public String getString(String key) {
        if (key == null) {
            return "";
        }


        if (resourceBundle != null) {
            try {
                return resourceBundle.getString(key);
            } catch (NullPointerException npe) {
                return MISSING_RESOURCE_TEXT + key + " (npe)";
            } catch (ClassCastException cce) {
                return MISSING_RESOURCE_TEXT + key + " (cce)";
            } catch (MissingResourceException mre) {
                return MISSING_RESOURCE_TEXT + key + " (mre)";
            }
        } else {
            return MISSING_RESOURCE_TEXT + key;
        }
    }

    synchronized public String getString(String key, Object[] parameters) {
        if (key == null) {
            return "";
        }

        if (resourceBundle != null) {
            try {
                String pattern = resourceBundle.getString(key);
                // Change any apostrophes to  \u2032 as MessageFormatter swallows them
                pattern = pattern.replaceAll("\u0027", "\u2032");
                formatter.applyPattern(pattern);
                return formatter.format(parameters);
            } catch (NullPointerException npe) {
                return MISSING_RESOURCE_TEXT + key + " (npe)";
            } catch (IllegalArgumentException iae) {
                return MISSING_RESOURCE_TEXT + key + " (iae)";
            } catch (ClassCastException cce) {
                return MISSING_RESOURCE_TEXT + key + " (cce)";
            } catch (MissingResourceException mre) {
                return MISSING_RESOURCE_TEXT + key + " (mre)";
            } catch (StringIndexOutOfBoundsException sioobe) {
                return MISSING_RESOURCE_TEXT + key + " (sioobe)";
            }
        } else {
            return MISSING_RESOURCE_TEXT + key;
        }
    }

    public Locale getLocale() {
        return locale;
    }

    private void setLocale(Locale locale) {


//        if ("he".equals(locale.getLanguage()) || "iw".equals(locale.getLanguage())) {
//            // Hebrew can be he or iw
//            this.locale = new Locale("iw");
//
//        }
//        if ("id".equals(locale.getLanguage()) || "in".equals(locale.getLanguage())) {
//            // Indonesian can be id or in
//            this.locale = new Locale("in");
//
//        } else {
//            this.locale = locale;
//        }

        this.locale = locale;

        formatter.setLocale(locale);

        numberFormat = NumberFormat.getInstance(locale);
        numberFormat.setMaximumFractionDigits(NUMBER_OF_FRACTION_DIGITS_FOR_BITCOIN);

        resourceBundle = ResourceBundle.getBundle(BITHER_RESOURCE_BUNDLE_NAME);

    }

    /**
     * Get the version number specified in the version.properties file.
     *
     * @return
     */
    public String getVersionNumber() {
        String version = "";
        if (versionProperties == null) {
            versionProperties = new Properties();
            try {
                java.net.URL versionPropertiesURL = Localiser.class.getResource(VERSION_PROPERTIES_FILENAME);
                if (versionPropertiesURL != null) {
                    versionProperties.load(versionPropertiesURL.openStream());
                }
            } catch (IOException ioe) {
                log.error(ioe.getMessage(), ioe);
            }
        }

        if (versionProperties != null) {
            version = versionProperties.getProperty(VERSION_PROPERTY_KEY_NAME);
            if (version == null) {
                version = "";
            }
        }
        return version;
    }


}