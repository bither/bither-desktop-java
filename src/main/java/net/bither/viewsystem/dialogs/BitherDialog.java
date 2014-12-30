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
package net.bither.viewsystem.dialogs;

import net.bither.Bither;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BitherDialog extends JDialog {
    private static final int AnimDuration = 150;
    private static final int FrameInterval = 30;
    private Timer timer;
    private int preferredHeight;

    public BitherDialog() {
        super(Bither.getMainFrame(), "");
    }

    private int headerHeight = 0;


    protected void initDialog() {
        setUndecorated(true);
        setMinimumSize(new Dimension(600, 50));
        final Dimension d = getSize();
        final Dimension p = Bither.getMainFrame().getSize();
        JPanel header = Bither.getMainFrame().getMainFrameUi().getDevidePanel();
        int y = (int) header.getLocationOnScreen().getY() + header.getHeight();
        headerHeight = y - d.height;

        int x = Bither.getMainFrame().getX() + (p.width - d.width) / 2;
        setBounds(x, y, d.width, d.height);
        //paintComponent(Bither.getMainFrame().getGraphics());
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            preferredHeight = Math.max(getSize().height, getMinimumSize().height);
            Dimension min = getMinimumSize();
            Dimension max = getMaximumSize();
            Dimension pre = getPreferredSize();
            min.height = 1;
            max.height = 1;
            pre.height = 1;
            setMinimumSize(min);
            setMaximumSize(max);
            setPreferredSize(pre);
            pack();
            if (timer != null && timer.isRunning()) {
                timer.stop();
            }
            timer = new Timer(FrameInterval, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    animateShow();
                }
            });
            timer.setRepeats(true);
            timer.start();
            super.setVisible(b);

        } else {
            super.setVisible(b);
        }
    }

    @Override
    public void dispose() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        timer = new Timer(FrameInterval, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animateHide();
            }
        });
        timer.setRepeats(true);
        timer.start();
        Bither.getMainFrame().validate();
        Bither.getMainFrame().repaint();
    }

    private void animateShow() {
        double step = (double) preferredHeight / (double) AnimDuration * (double) FrameInterval;
        Dimension pre = getPreferredSize();
        if (pre.height < preferredHeight) {
            pre.height += step;
        } else {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
            return;
        }
        pre.height = Math.min(pre.height, preferredHeight);
        Dimension min = getMinimumSize();
        Dimension max = getMaximumSize();
        min.height = pre.height;
        max.height = pre.height;
        setMinimumSize(min);
        setPreferredSize(pre);
        setMaximumSize(max);
        pack();
    }

    private void animateHide() {
        double step = (double) preferredHeight / (double) AnimDuration * (double) FrameInterval;
        Dimension pre = getPreferredSize();
        if (pre.height > 0) {
            pre.height -= step;
        } else {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
            super.dispose();
            return;
        }
        pre.height = Math.min(pre.height, preferredHeight);
        Dimension min = getMinimumSize();
        Dimension max = getMaximumSize();
        min.height = pre.height;
        max.height = pre.height;
        setMinimumSize(min);
        setPreferredSize(pre);
        setMaximumSize(max);
        pack();
    }


    protected void paintComponent(Graphics graphics) {

        // Reposition the center panel on the fly
        //calculatePosition();


        Graphics2D g = (Graphics2D) graphics;

        // Always use black even for light themes
        g.setPaint(Color.BLACK);

        // Set the opacity
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

        // Create the darkened border rectangle (will appear beneath the panel layer)
        g.fillRect(0, headerHeight, Bither.getMainFrame().getWidth(), Bither.getMainFrame().getHeight() - headerHeight);

    }
}
