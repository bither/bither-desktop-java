package net.bither;

import net.bither.preference.UserPreference;
import net.bither.utils.DateUtils;
import net.bither.viewsystem.base.ColorAndFontConstants;
import net.bither.viewsystem.base.FontSizer;

import java.awt.*;
import java.text.ParseException;
import java.util.Date;

public class BitherSetting {

    public static final int NOT_RELEVANT_PERCENTAGE_COMPLETE = -1;
    public static final int WATCH_ONLY_ADDRESS_COUNT_LIMIT = 150;
    public static final int PRIVATE_KEY_OF_HOT_COUNT_LIMIT = 50;
    public static final String VERSION = "0.0.2";

    public static final String USER_PROPERTIES_HEADER_TEXT = "bither";
    public static final String MAIN_DIR = "Bither";

    public static final String BLOCKCHAIN_INFO_PREFIX = "http://blockchain.info/tx-index/";
    public static final String BLOCKMETA_TRANSACTION_PREFIX = "http://www.blockmeta.com/tx/";

    public static Date genesisBlockCreationDate;

    public static final int TOOLTIP_DISMISSAL_DELAY = 12000; // millisecs
    public static final int TABLE_BORDER = 3;
    public static final int STATUS_WIDTH_DELTA = 8;
    public static final int MINIMUM_ICON_HEIGHT = 16;
    public static final int HEIGHT_DELTA = 3;
    public static final String THREE_SPACER = "   "; // 3 spaces

    public static final double PROPORTION_OF_VERTICAL_SCREEN_TO_FILL = 0.75D;
    public static final double PROPORTION_OF_HORIZONTAL_SCREEN_TO_FILL = 0.82D;


    public static final String EXAMPLE_MEDIUM_FIELD_TEXT = "Typical text 00.12345678 BTC (000.01 XYZ)";


    public static final int WALLET_WIDTH_DELTA = 25;

    public static final int SCROLL_BAR_DELTA = 20;

    public static final int WIDTH_OF_SPLIT_PANE_DIVIDER = 9;

    public static final int MENU_HORIZONTAL_INSET = 8;
    public static final int MENU_VERTICAL_INSET = 1;

    public static final int BALANCE_SPACER = 7;


    public static final int HELP_BUTTON_INDENT = 6;


    public static final String SPENDABLE_TEXT_IN_ENGLISH = "Spendable";

    public static final String USER_LANGUAGE_IS_DEFAULT = "isDefault";


    public static final int SCROLL_INCREMENT = 12;

    // User preference font.
    public static final String FONT = "font";

    public static final int PASSWORD_LENGTH_MAX = 43;
    public static final int PASSWORD_LENGTH_MIN = 6;


    // User preferences.


    public static final String OPEN_EXCHANGE_RATES_EXCHANGE_NAME = "OpenExchangeRates";


    static {
        try {
            genesisBlockCreationDate = DateUtils.getDateTimeForTimeZone("2009-01-03 18:15:05");
        } catch (ParseException e) {
            // Will never happen.
            e.printStackTrace();
        }
    }

    public enum AddressType {
        Normal, TxTooMuch, SpecialAddress
    }


    public static String createTooltipTextForMenuItem(String tooltip) {
        String useScreenMenuBar = System.getProperty("apple.laf.useScreenMenuBar");
        String lookAndFeel = "" + UserPreference.getInstance().getLookAndFeel();

        if (Boolean.TRUE.toString().equalsIgnoreCase(useScreenMenuBar) && ("system".equalsIgnoreCase(lookAndFeel)
                || "null".equalsIgnoreCase(lookAndFeel)
                || lookAndFeel.toLowerCase().startsWith("mac"))) {
            // No HTML wrapping of tooltip.
            return tooltip;
        } else {
            return createTooltipText(tooltip);
        }
    }

    public static String createTooltipText(String toolTip) {
        return createMultilineTooltipText(new String[]{toolTip});
    }

    public static String createMultilineTooltipText(String[] toolTips) {
        // Multiline tool tip text.
        int fontSize = ColorAndFontConstants.BITHER_DEFAULT_FONT_SIZE;
        boolean isItalic = false;
        boolean isBold = false;
        FontSizer.INSTANCE.initialise();
        Font adjustedFont = FontSizer.INSTANCE.getAdjustedDefaultFont();
        if (adjustedFont != null) {
            fontSize = adjustedFont.getSize();
            isItalic = adjustedFont.isItalic();
            isBold = adjustedFont.isBold();
        }

        String fontCSS = "font-size:" + fontSize + "pt; font-family:" + adjustedFont.getFamily() + ";";
        if (isItalic) {
            fontCSS = fontCSS + "font-style:italic;";
        } else {
            fontCSS = fontCSS + "font-style:normal;";
        }
        if (isBold) {
            fontCSS = fontCSS + "font-weight:bold;";
        } else {
            fontCSS = fontCSS + "font-weight:normal;";
        }
        StringBuilder toolTipText = new StringBuilder("<html><font face=\"sansserif\" style= \"").append(fontCSS).append("\">");

        if (toolTips != null) {
            for (int i = 0; i < toolTips.length - 1; i++) {
                if (toolTips[i] != null && !"".equals(toolTips[i])) {
                    toolTipText.append(toolTips[i]).append("<br>");
                }
            }
            toolTipText.append(toolTips[toolTips.length - 1]);
        }
        toolTipText.append("</font></html>");

        return toolTipText.toString();
    }


}
