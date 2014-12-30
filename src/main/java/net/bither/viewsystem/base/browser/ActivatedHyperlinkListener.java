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

import net.bither.viewsystem.MainFrame;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.net.URL;

public class ActivatedHyperlinkListener implements HyperlinkListener {
    MainFrame mainFrame;
    Browser browser;

    public ActivatedHyperlinkListener(MainFrame frame, Browser browser) {
        this.mainFrame = frame;
        this.browser = browser;
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
        HyperlinkEvent.EventType type = hyperlinkEvent.getEventType();
        final URL url = hyperlinkEvent.getURL();
        if (type == HyperlinkEvent.EventType.ENTERED) {
//            Message message = new Message(url.toString(), true);
//            message.setShowInMessagesTab(false);
//            MessageManager.INSTANCE.addMessage(message);
            if (browser.isLoading()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        browser.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    }
                });
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        browser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                });
            }
        } else if (type == HyperlinkEvent.EventType.EXITED) {
//            Message message = new Message(SPACER, true);
//            message.setShowInMessagesTab(false);
//            MessageManager.INSTANCE.addMessage(message);
            if (browser.isLoading()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        browser.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    }
                });
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        browser.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                });
            }
        } else if (type == HyperlinkEvent.EventType.ACTIVATED) {

        }
    }
}
