package net.bither.qrcode;

import net.bither.fonts.AwesomeIcon;
import net.bither.utils.LocaliserUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HDMServerUnsignedQRCodePanel extends DisplayBitherQRCodePanel {


    private IScanQRCode scanQRCode;

    public HDMServerUnsignedQRCodePanel(IScanQRCode scanQRCode, String codeString) {
        super(codeString, true);
        this.scanQRCode = scanQRCode;
        updateTitle(LocaliserUtils.getString("hdm_keychain_add_unsigned_server_qr_code_title"));
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        modifOkButton(AwesomeIcon.CAMERA, LocaliserUtils.getString("unsigned_transaction_qr_code_complete"));

    }

    private void onOK() {
        closePanel();
        SelectTransportQRCodePanel selectTransportQRCodePanel = new SelectTransportQRCodePanel(scanQRCode, true);
        selectTransportQRCodePanel.showPanel();
    }
}
