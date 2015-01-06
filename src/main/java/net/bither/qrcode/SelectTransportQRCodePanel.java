package net.bither.qrcode;

import net.bither.bitherj.qrcode.QRCodeTransportPage;
import net.bither.bitherj.utils.Utils;
import net.bither.utils.LocaliserUtils;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;


public class SelectTransportQRCodePanel extends SelectQRCodePanel {

    private int totalPage = 1;
    private ArrayList<QRCodeTransportPage> pages;
    private String lastResult;

    public SelectTransportQRCodePanel(IScanQRCode scanQRCode, boolean isPopover) {
        super(scanQRCode, isPopover);
        pages = new ArrayList<QRCodeTransportPage>();


    }

    public SelectTransportQRCodePanel(IScanQRCode scanQRCode) {
        this(scanQRCode, false);

    }

    @Override
    public void initialiseContent(JPanel panel) {
        super.initialiseContent(panel);
        setMsg(LocaliserUtils.getString("scan.qr.transport.init.label"));
    }

    @Override
    protected void fromFile() {

        startFileChooser(new IFileChooser() {
            @Override
            public void selectFile(File file) {
                if (file != null) {
                    String str = QRCodeEncoderDecoder.decode(file);
                    System.out.println("qrcode:" + str);
                    handleResult(str);

                }

            }
        });
    }

    @Override
    protected void fromScan() {
        close();
        ScanTransportQRCodeDialog scanQRCodeDialog = new ScanTransportQRCodeDialog(this.scanQRCode);
        scanQRCodeDialog.pack();
        scanQRCodeDialog.setVisible(true);
    }

    public void handleResult(String result) {
        if (Utils.isEmpty(result)) {
            setMsg(LocaliserUtils.getString("no_format_qr_code"));
        } else {
            if (resultValid(result)) {
                QRCodeTransportPage page = QRCodeTransportPage
                        .formatQrCodeTransport(result);
                pages.add(page);
                totalPage = page.getSumPage();
                if (page.getCurrentPage() < totalPage - 1) {
                    String str = String.format(LocaliserUtils.getString("scan.qr.transport.page.label"),
                            pages.size() + 1, totalPage);
                    setMsg(str);

                } else {
                    complete();
                }
            } else {
                shake();
            }

        }

    }

    public boolean resultValid(String result) {
        if (!Utils.compareString(result, lastResult)) {
            shake();
        }
        lastResult = result;
        QRCodeTransportPage page = QRCodeTransportPage
                .formatQrCodeTransport(result);
        if (page == null) {
            return false;
        }
        if (page.getCurrentPage() == pages.size()) {
            return true;
        }
        return false;
    }

    private void shake() {
        //todo shake
        System.out.println("shake");
    }

    private void complete() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    String string = QRCodeTransportPage.qrCodeTransportToString(pages);
                    scanQRCode.handleResult(string, SelectTransportQRCodePanel.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }


}
