package net.bither.viewsystem.froms;

import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Panels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MorePanel extends WizardPanel {

    private JButton btnAdvance;
    private JButton btnPeer;
    private JButton btnBlcok;
    private JButton btnExchange;
    private JButton btnVerfyMessage;

    public MorePanel() {
        super(MessageKey.MORE, AwesomeIcon.ELLIPSIS_H, false);
    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][][][][][]", // Column constraints
                "[]10[][][][][][]100[][][]" // Row constraints
        ));
        btnAdvance = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                AdvancePanel advancePanel = new AdvancePanel();
                advancePanel.showPanel();

            }
        }, MessageKey.ADVANCE, AwesomeIcon.FA_BOOK);

        btnPeer = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                PeersPanel peersPanel = new PeersPanel();
                peersPanel.showPanel();
            }
        }, MessageKey.PEERS, AwesomeIcon.FA_USERS);
        btnBlcok = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                BlockPanel blockPanel = new BlockPanel();
                blockPanel.showPanel();


            }
        }, MessageKey.BLOCKS, AwesomeIcon.FA_SHARE_ALT);
        btnExchange = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ExchangePreferencePanel exchangePreferencePanel = new ExchangePreferencePanel(true);
                exchangePreferencePanel.showPanel();

            }
        }, MessageKey.EXCHANGE_SETTINGS_TITLE, AwesomeIcon.DOLLAR);
        btnVerfyMessage = Buttons.newNormalButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
                VerifyMessagePanel verifyMessagePanel = new VerifyMessagePanel();
                verifyMessagePanel.showPanel();

            }
        }, MessageKey.VERIFY_MESSAGE_TITLE, AwesomeIcon.CHECK);
        panel.add(btnAdvance, "align center,cell 3 2 ,grow,wrap");
        panel.add(btnPeer, "align center,cell 3 3,grow,wrap");
        panel.add(btnBlcok, "align center,cell 3 4,grow,wrap");
        panel.add(btnExchange, "align center,cell 3 5,grow,wrap");
        panel.add(btnVerfyMessage, "align center,cell 3 6,grow,wrap");


    }
}
