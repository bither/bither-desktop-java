package net.bither.viewsystem.action;

import net.bither.Bither;
import net.bither.bitherj.utils.Utils;
import net.bither.viewsystem.froms.IAddressForm;
import net.bither.viewsystem.panels.WalletListPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class WalletMouseListener extends MouseAdapter implements MouseListener {
    private WalletListPanel walletListPanel;
    private IAddressForm singleWalletForm;

    public WalletMouseListener(WalletListPanel walletListPanel, IAddressForm singleWalletForm) {
        super();
        this.walletListPanel = walletListPanel;
        this.singleWalletForm = singleWalletForm;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (this.singleWalletForm != null) {
            String activeAddress = null;
            if (Bither.getActionAddress() != null) {
                activeAddress = Bither.getActionAddress().getAddress();
            }
            if (!Utils.compareString(this.singleWalletForm.getOnlyName()
                    , activeAddress)) {
                walletListPanel.selectWalletPanelByFilename(this.singleWalletForm.getOnlyName());
                Bither.getCoreController().fireDataChangedUpdateNow();
            }
            this.singleWalletForm.getPanel().requestFocusInWindow();

        }

    }

}