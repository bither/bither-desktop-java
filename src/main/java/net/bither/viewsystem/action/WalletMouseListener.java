package net.bither.viewsystem.action;

import net.bither.Bither;
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
            if (!this.singleWalletForm.getPerWalletModelData().getAddress()
                    .equals(Bither.getActionAddress().getAddress())) {
                walletListPanel.selectWalletPanelByFilename(this.singleWalletForm.getPerWalletModelData().getAddress());
                Bither.getCoreController().fireDataChangedUpdateNow();
            }
            this.singleWalletForm.getPanel().requestFocusInWindow();

        }

    }

}