package net.bither.qrcode;

import net.bither.fonts.AwesomeIcon;
import net.bither.utils.LocaliserUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GenerateUnsignedTxPanel extends DisplayBitherQRCodePanle {
    private IScanQRCode scanQRCode;

    public GenerateUnsignedTxPanel(IScanQRCode scanQRCode, String codeString) {
        super(codeString, true);
        this.scanQRCode = scanQRCode;
        updateTitle(LocaliserUtils.getString("unsigned_transaction_qr_code_title"));
        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        modifOkButton(AwesomeIcon.CAMERA, LocaliserUtils.getString("unsigned_transaction_qr_code_complete"));

    }

    private void onOK() {
        onCancel();
        SelectTransportQRCodePanel selectTransportQRCodePanel = new SelectTransportQRCodePanel(scanQRCode, true);
        selectTransportQRCodePanel.showPanel();
    }
}
