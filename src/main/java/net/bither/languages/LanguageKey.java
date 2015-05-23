/*
 *
 *  Copyright 2014 http://Bither.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package net.bither.languages;


import net.bither.fonts.TitleFontDecorator;
import net.bither.viewsystem.components.Images;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public enum LanguageKey {

    /**
     * Afrikaans in Africa
     */
    AF_AF("af_AF", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * Arabic
     */
    AR_AR("ar_AR", TitleFontDecorator.IMPACT_REGULAR),
    /**
     * Czech
     */
    CS_CZ("cs_CZ", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * Danish
     */
    DA_DK("da_DK", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * German
     */
    DE_DE("de_DE", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * Greek
     */
    EL_GR("el_GR", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * English (United Kingdom)
     */
    EN_GB("en_GB", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * English (United States)
     */
    EN_US("en_US", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * Esperanto
     */
    EO("eo", TitleFontDecorator.CORBEN_REGULAR), // Esperanto has no country
    /**
     * Spanish
     */
    ES_ES("es_ES", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * Farsi
     */
    FA_IR("fa_IR", TitleFontDecorator.IMPACT_REGULAR),
    /**
     * Finnish
     */
    FI_FI("fi_FI", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * French
     */
    FR_FR("fr_FR", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * Hindi (should use NotoSans-Bold when proven)
     */
    HI_IN("hi_IN", TitleFontDecorator.IMPACT_REGULAR),
    /**
     * Croatian
     */
    HR_HR("hr_HR", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * Hungarian
     */
    HU_HU("hu_HU", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * Indonesian
     */
    IN_ID("in_ID", TitleFontDecorator.IMPACT_REGULAR), // Legacy form of "id_ID" for Indonesian in Indonesia
    /**
     * Hebrew (Israel)
     */
    IW_IL("iw_IL", TitleFontDecorator.IMPACT_REGULAR), // Legacy form of "he_IL" for Hebrew in Israel
    /**
     * Italian
     */
    IT_IT("it_IT", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * Japanese
     */
    JA_JP("ja_JP", TitleFontDecorator.IMPACT_REGULAR),
    /**
     * Korean
     */
    KO_KR("ko_KR", TitleFontDecorator.IMPACT_REGULAR),
    /**
     * Latvian
     */
    LV_LV("lv_LV", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * Lithuanian
     */
    LT_LT("lt_LT", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * Dutch
     */
    NL_NL("nl_NL", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * Norwegian
     */
    NO_NO("no_NO", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * Polish
     */
    PL_PL("pl_PL", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * Portuguese (Brazil)
     */
    PT_BR("pt_BR", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * Portuguese (Portugal)
     */
    PT_PT("pt_PT", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * Romanian
     */
    RO_RO("ro_RO", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * Russian
     */
    RU_RU("ru_RU", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * Slovak
     */
    SK_SK("sk_SK", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * Slovene
     */
    SL_SI("sl_SI", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * Swedish
     */
    SV_SV("sv_SV", TitleFontDecorator.CORBEN_REGULAR),
    /**
     * Swahili
     */
    SW_KE("sw_KE", TitleFontDecorator.IMPACT_REGULAR),
    /**
     * Tamil
     */
    TA_LK("ta_LK", TitleFontDecorator.IMPACT_REGULAR),
    /**
     * Thai
     */
    TH_TH("th_TH", TitleFontDecorator.IMPACT_REGULAR),
    /**
     * Tagalog
     */
    TL_PH("tl_PH", TitleFontDecorator.IMPACT_REGULAR),
    /**
     * Turkish
     */
    TR_TR("tr_TR", TitleFontDecorator.IMPACT_REGULAR),
    /**
     * Vietnamese
     */
    VI_VN("vi_VN", TitleFontDecorator.OPENSANS_SEMIBOLD),
    /**
     * Chinese (Simplified)
     */
    ZH_CN("zh_CN", TitleFontDecorator.IMPACT_REGULAR),
    /**
     * Chinese (Traditional)
     */
    ZH_TW("zh_TW", TitleFontDecorator.IMPACT_REGULAR);

    private final String key;
    private final String countryCode;
    private final String languageCode;
    private final String languageName;
    private final Font titleFont;
    private ImageIcon icon;

    private LanguageKey(String key, Font titleFont) {

        this.titleFont = titleFont;

        // The bundle is cached by the JVM
        ResourceBundle rb = ResourceBundle.getBundle(Languages.BASE_NAME);

        this.key = key;
        this.languageCode = key.substring(0, 2);

        if (key.contains("_")) {
            this.countryCode = key.substring(3, 5);
        } else {
            this.countryCode = languageCode;
        }

        this.icon = Images.newLanguageCodeIcon(languageCode);
        this.languageName = rb.getString(key);
    }

    /**
     * <p>Reset the icons after a theme switch</p>
     */
    public static void resetIcons() {
        for (LanguageKey languageKey : values()) {
            languageKey.icon = Images.newLanguageCodeIcon(languageKey.languageCode);
        }
    }

    /**
     * @return The appropriate title font
     */
    public Font getTitleFont() {
        return titleFont;
    }

    /**
     * @return The key for use with the resource bundles
     */
    public String getKey() {
        return key;
    }

    /**
     * @return The icon for use with a list renderer
     */
    public ImageIcon getIcon() {
        return icon;
    }

    /**
     * @return The 2-letter country code (e.g. "UK")
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * @return The 2-letter language code (e.g. "en")
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * @return The language name (e.g. "English (United Kingdom)")
     */
    public String getLanguageName() {
        return languageName;
    }

    /**
     * @param locale The locale providing at least language and region
     * @return The matching language key, or the default EN_US since it is dominant on the internet
     */
    public static LanguageKey fromLocale(Locale locale) {

        // Ensure we use English rules for uppercase to identify enum keys
        // We use the legacy names for countries for consistency
        String language = locale.getLanguage().toUpperCase(Locale.ENGLISH);
        String country = locale.getCountry().toUpperCase(Locale.ENGLISH);
        String variant = locale.getVariant().toUpperCase(Locale.ENGLISH);

        String matcher1 = language + "_" + country + "_" + variant;
        String matcher2 = language + "_" + country;

        for (LanguageKey languageKey : values()) {

            // Language, country and variant
            if (languageKey.name().equals(matcher1)) {
                return languageKey;
            }

            // Language and country
            if (languageKey.name().equals(matcher2)) {
                return languageKey;
            }

            // At this point we match only on language (e.g. "EO" for Esperanto)
            // so that we don't introduce a country or region bias

            // Language only
            if (languageKey.name().equals(matcher2)) {
                return languageKey;
            }

        }

        // We have an unsupported locale so we use the first entry that matches
        // the supported language

        // Find the first entry with the supported language
        for (LanguageKey languageKey : values()) {

            if (languageKey.name().substring(0, 2).equals(language)) {
                return languageKey;
            }

        }

        // Unsupported language so default to EN_US since it is the dominant locale on the internet
        return LanguageKey.EN_US;

    }

    /**
     * @param languageName The language name (e.g. "English (United Kingdom)") as specified in the primary resource bundle
     * @return The language key matching the language name
     */
    public static LanguageKey fromLanguageName(String languageName) {

        // Use the resource bundle translations
        for (LanguageKey languageKey : values()) {
            if (languageKey.getLanguageName().equalsIgnoreCase(languageName)) {
                return languageKey;
            }
        }

        // Unknown language name so fall back to Java locale lookup for current locale
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getDisplayName().equalsIgnoreCase(languageName)) {
                return fromLocale(locale);
            }
        }

        throw new IllegalArgumentException("'languageName' was not matched for '" + languageName + "'");
    }

    /**
     * @return The localised names of the languages in the order they are declared
     */
    public static String[] localisedNames() {

        String[] languageNames = new String[values().length];

        int i = 0;
        for (LanguageKey languageKey : values()) {
            languageNames[i] = languageKey.getLanguageName();
            i++;
        }

        return languageNames;
    }
}
