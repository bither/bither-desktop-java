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

package net.bither.viewsystem.dialogs;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import net.bither.BitherUI;
import net.bither.qrcode.DesktopQRCodReceive;
import net.bither.qrcode.DesktopQRCodSend;
import net.bither.qrcode.QRCodeGenerator;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.concurrent.ThreadFactory;

public abstract class AbstractDesktopHDMMsgDialog extends BitherDialog implements Runnable, ThreadFactory {
    private JPanel contentPane;
    private JButton buttonCancel;
    private JPanel mainPanel;


    private JLabel imageLabel;

    private Webcam webcam = null;
    private WebcamPanel panel = null;

    protected boolean isRunning = true;

    protected DesktopQRCodSend desktopQRCodSend;
    protected DesktopQRCodReceive desktopQRCodReceive;


    protected boolean isSendMode;

    public AbstractDesktopHDMMsgDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonCancel);


        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });


        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        initialiseContent();

        Dimension dimension = new Dimension(BitherUI.UI_MIN_WIDTH, BitherUI.WIZARD_MIN_HEIGHT);
        setMinimumSize(dimension);
        setPreferredSize(dimension);
        setMaximumSize(dimension);
        initDialog();
        inited();
    }

    public void onCancel() {
        if (webcam.isOpen()) {
            webcam.close();
        }
        isRunning = false;

        dispose();
    }

    public void initialiseContent() {
        mainPanel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][]", // Column constraints
                "[][]" // Row constraints
        ));
        imageLabel = Labels.newValueLabel("");
        mainPanel.add(imageLabel, "align center,cell 0 0,grow");

        Dimension size = WebcamResolution.QVGA.getSize();
        java.util.List<Webcam> webcams = Webcam.getWebcams();
        if (webcams.size() > 0) {
            webcam = webcams.get(0);
            webcam.setViewSize(size);
            panel = new WebcamPanel(webcam);
            panel.setPreferredSize(size);
            mainPanel.add(panel, "align center,cell 1 0,grow");

        } else {
            dispose();
            new MessageDialog(LocaliserUtils.getString("camer_is_not_available")).showMsg();
        }


    }


    protected void showQRCode(String qrCodeString) {
        int scaleWidth = BitherUI.POPOVER_MIN_WIDTH;
        int scaleHeight = BitherUI.POPOVER_MIN_WIDTH;
        Image image = QRCodeGenerator.generateQRcode(qrCodeString, null, null, 1);
        if (image != null) {
            int scaleFactor = (int) (Math.floor(Math.min(scaleHeight / image.getHeight(null),
                    scaleWidth / image.getWidth(null))));
            BufferedImage qrCodeImage = QRCodeGenerator.generateQRcode(qrCodeString, null, null, scaleFactor);
            imageLabel.setIcon(new ImageIcon(qrCodeImage));
        }

    }

    @Override
    public void run() {
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Result result = null;
            BufferedImage image = null;
            if (webcam.isOpen()) {
                if ((image = webcam.getImage()) == null) {
                    continue;
                }
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                try {
                    result = new MultiFormatReader().decode(bitmap);

                } catch (NotFoundException e) {

                }
            }
            if (result != null && result.getText() != null) {
                handleScanResult(result.getText());

            }


        } while (isRunning);
    }

    protected abstract void handleScanResult(final String result);

    protected abstract void inited();

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "example-runner");
        t.setDaemon(true);
        return t;
    }

}
