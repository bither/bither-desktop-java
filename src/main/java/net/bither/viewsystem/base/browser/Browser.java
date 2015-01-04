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
package net.bither.viewsystem.base.browser;

import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.ColorAndFontConstants;
import net.bither.viewsystem.base.FontSizer;
import net.bither.viewsystem.themes.Themes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class Browser extends javax.swing.JEditorPane {
    private static final long serialVersionUID = 1L;

    private Logger log = LoggerFactory.getLogger(Browser.class);

    private String loadingMessage;

    private String currentHref;

    private boolean loading = false;

    public Browser(String currentHref) {
        super();

        this.currentHref = currentHref;


        try {
            if (Bither.getMainFrame() != null) {
                Bither.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
            addHyperlinkListener(new ActivatedHyperlinkListener(Bither.getMainFrame(), this));

            loadingMessage = LocaliserUtils.getString("browser.loadingMessage");

            setEditable(false);
            setBackground(Themes.currentTheme.detailPanelBackground());

            String fontName = null;
            if (fontName == null || "".equals(fontName)) {
                fontName = ColorAndFontConstants.BITHER_DEFAULT_FONT_NAME;
            }
            // Add in san-serif as a fallback.
            fontName = fontName + ", san-serif";

            int fontSize = ColorAndFontConstants.BITHER_DEFAULT_FONT_SIZE;
            boolean isItalic = false;
            boolean isBold = false;
            Font adjustedFont = FontSizer.INSTANCE.getAdjustedDefaultFont();
            if (adjustedFont != null) {
                setFont(adjustedFont);
                fontSize = adjustedFont.getSize();
                isItalic = adjustedFont.isItalic();
                isBold = adjustedFont.isBold();
            }

            String fontCSS = "font-size:" + fontSize + "pt; font-family:" + fontName + ";";
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

            HTMLEditorKit kit = new HTMLEditorKit();
            setEditorKit(kit);
            javax.swing.text.html.StyleSheet styleSheet = kit.getStyleSheet();
            styleSheet.addRule("body {" + fontCSS + "}");
            Document doc = kit.createDefaultDocument();
            setDocument(doc);

            log.debug("Trying to load '" + currentHref + "'...");
        } catch (Exception ex) {
            showUnableToLoadMessage(ex.getClass().getCanonicalName() + " " + ex.getMessage());
        }
    }

    public static String getLoadingMessage(String href, String loadingMessage) {
        return href + BitherSetting.THREE_SPACER + loadingMessage + "...";
    }

    /**
     * This internal method attempts to load and display the specified URL.
     */
    public boolean visit(String newHref, boolean forceLoad) {
        try {
            if (forceLoad || !newHref.equals(currentHref)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Load and display the URL.
                getUrlContentInBackground(this, new URL(newHref));
                currentHref = newHref;


            }
            return true; // Return success.
        } catch (IOException ex) {
            showUnableToLoadMessage(ex.getClass().getCanonicalName() + " " + ex.getMessage());
            return false; // Return failure.
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Get the URL contents in a background thread.
     */
    private void getUrlContentInBackground(final Browser browser, final URL url) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {

            private String message = null;

            private StringBuffer stringBuffer = new StringBuffer();

            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    browser.setLoading(true);

                    InputStream in = url.openStream();

                    byte[] buffer = new byte[256];

                    while (true) {
                        int byteRead = in.read(buffer);
                        if (byteRead == -1)
                            break;
                        for (int i = 0; i < byteRead; i++) {
                            stringBuffer.append((char) buffer[i]);
                        }
                    }
                    return true;
                } catch (IOException ioe) {
                    message = ioe.getClass().getCanonicalName() + " " + ioe.getMessage();
                    return false;
                } finally {
                    browser.setLoading(false);
                }
            }

            @Override
            protected void done() {
                Boolean wasSuccessful = false;
                try {
                    wasSuccessful = get();

                    if (wasSuccessful) {
                        String result = stringBuffer.toString();
                        if (result != null && result.length() > 0) {
                            browser.setText(result);
                            // Scroll to top.
                            browser.setCaretPosition(0);

                        } else {
                            if (message != null) {
                                showUnableToLoadMessage(message);
                            }
                        }
                    } else {
                        if (message != null) {
                            showUnableToLoadMessage(message);
                        }
                    }
                } catch (InterruptedException e) {
                    showUnableToLoadMessage(message);
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    showUnableToLoadMessage(message);
                    e.printStackTrace();
                } finally {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        };
        worker.execute();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isLoading() {
        return loading;
    }

    private void showUnableToLoadMessage(String message) {


    }
}
