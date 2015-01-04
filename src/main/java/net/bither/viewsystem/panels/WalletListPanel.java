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
package net.bither.viewsystem.panels;

import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.BitherjSettings;
import net.bither.bitherj.core.BlockChain;
import net.bither.implbitherj.BlockNotificationCenter;
import net.bither.preference.UserPreference;
import net.bither.utils.ImageLoader;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.*;
import net.bither.viewsystem.components.ScrollBarUIDecorator;
import net.bither.viewsystem.dialogs.MessageDialog;
import net.bither.viewsystem.froms.IAddressForm;
import net.bither.viewsystem.froms.SingleColdWalletFrom;
import net.bither.viewsystem.froms.SingleWalletForm;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.metal.MetalButtonUI;
import javax.swing.plaf.metal.MetalScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The wallet list view.
 */
public class WalletListPanel extends JPanel implements Viewable, ComponentListener, BlockNotificationCenter.IBlockListener {

    private static final long serialVersionUID = 191352298245057705L;


    private JPanel walletListPanel;
    private ArrayList<IAddressForm> walletPanels;

  //  private JScrollPane scrollPane;
    private static final int TOP_BORDER = 0;
    public static final int LEFT_BORDER = 0;
    public static final int RIGHT_BORDER = 0;



    /**
     * Creates a new {@link WalletListPanel}.
     */
    public WalletListPanel() {
        walletPanels = new ArrayList<IAddressForm>();
        setOpaque(false);
        setFocusable(true);
        applyComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));
        initUI();
    }

    @Override
    public void displayView(DisplayHint displayHint) {
        displayView(displayHint, true);
    }

    private void displayView(DisplayHint displayHint, boolean blinkEnabled) {
        if (walletPanels != null) {
            synchronized (walletPanels) {

                for (IAddressForm loopSingleWalletPanel : walletPanels) {

                    // Make sure the totals displayed and encryption status are correct.

                    loopSingleWalletPanel.updateFromModel();

                    //amountFiatLabelSize = Math.max(amountFiatLabelSize, loopSingleWalletPanel.getFiatLabelWidth());
                }


            }
        }
        invalidate();
        revalidate();
        repaint();
    }

    public void selectWalletPanelByFilename(String address) {
        if (walletPanels != null) {
            synchronized (walletPanels) {
                for (IAddressForm loopSingleWalletPanel : walletPanels) {
                    loopSingleWalletPanel.updateFromModel();
                    if (loopSingleWalletPanel.getPerWalletModelData().getAddress() != null) {
                        if (loopSingleWalletPanel.getPerWalletModelData().getAddress().equals(address)) {
                            Bither.setActivePerWalletModelData(loopSingleWalletPanel.getPerWalletModelData());
                            loopSingleWalletPanel.setSelected(true);
                            Rectangle bounds = loopSingleWalletPanel.getPanel().getParent().getBounds();
                            walletListPanel.scrollRectToVisible(bounds);

                        } else {
                            loopSingleWalletPanel.setSelected(false);
                        }
                    }
                }
            }
        }
    }


    public void initUI() {
        setLayout(new BorderLayout());

        createWalletListPanel();

        removeAll();
        add(walletListPanel, BorderLayout.CENTER);
    }

    private JPanel createWalletListPanel() {
        walletListPanel = new JPanel();
        walletListPanel.setLayout(new GridBagLayout());
        walletListPanel.setOpaque(false);
        walletListPanel.setFocusable(true);
        walletListPanel.setBackground(Themes.currentTheme.detailPanelBackground());
        walletListPanel.setBorder(BorderFactory.createEmptyBorder());
        walletListPanel.setComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 10.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        // Get the wallets from the model.
        List<Address> perWalletModelDataList = AddressManager.getInstance().getAllAddresses();
        if (perWalletModelDataList.size() > 0 && UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {

            addPanel(constraints, LocaliserUtils.getString("address.group.private"));
        }

        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.COLD) {
            addColdAddressList(constraints, AddressManager.getInstance().getPrivKeyAddresses());

        } else {
            if (perWalletModelDataList != null) {
                addHotAddressList(constraints, AddressManager.getInstance().getPrivKeyAddresses());
            }
            if (AddressManager.getInstance().getWatchOnlyAddresses().size() > 0) {
                addPanel(constraints, LocaliserUtils.getString("address.group.watch.only"));
                if (perWalletModelDataList != null) {
                    addHotAddressList(constraints, AddressManager.getInstance().getWatchOnlyAddresses());
                }
            }
            Address activeAddress = null;
            if (AddressManager.getInstance().getPrivKeyAddresses().size() > 0) {
                activeAddress = AddressManager.getInstance().getPrivKeyAddresses().get(0);
            } else {
                if (AddressManager.getInstance().getWatchOnlyAddresses().size() > 0) {
                    activeAddress = AddressManager.getInstance().getWatchOnlyAddresses().get(0);
                }
            }

            Bither.setActivePerWalletModelData(activeAddress);
            if (activeAddress != null) {
                selectWalletPanelByFilename(activeAddress.getAddress());
            }

        }
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 10000.0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        JPanel fill1 = new JPanel();
        fill1.setOpaque(false);
        walletListPanel.add(fill1, constraints);

        return walletListPanel;
    }

    private void addColdAddressList(GridBagConstraints constraints, List<Address> addresses) {
        synchronized (walletPanels) {
            for (Address loopPerWalletModelData : addresses) {
                if (loopPerWalletModelData != null) {
                    JPanel outerPanel = new JPanel();
                    outerPanel.setOpaque(false);
                    outerPanel.setBorder(BorderFactory.createEmptyBorder(TOP_BORDER, LEFT_BORDER, 0, RIGHT_BORDER));
                    outerPanel.setLayout(new GridBagLayout());

                    GridBagConstraints constraints2 = new GridBagConstraints();
                    constraints2.fill = GridBagConstraints.BOTH;
                    constraints2.gridx = 0;
                    constraints2.gridy = 0;
                    constraints2.weightx = 1.0;
                    constraints2.weighty = 1.0;
                    constraints2.gridwidth = 1;
                    constraints2.gridheight = 1;
                    constraints2.anchor = GridBagConstraints.CENTER;

                    SingleColdWalletFrom coldWalletFrom = new SingleColdWalletFrom(loopPerWalletModelData, this);
                    coldWalletFrom.getPanel().setComponentOrientation(ComponentOrientation
                            .getOrientation(LocaliserUtils.getLocale()));


                    outerPanel.add(coldWalletFrom.getPanel(), constraints2);


                    walletListPanel.add(outerPanel, constraints);
                    walletPanels.add(coldWalletFrom);
                    constraints.gridy = constraints.gridy + 1;
                    Address activeAddress = null;
                    if (AddressManager.getInstance().getPrivKeyAddresses().size() > 0) {
                        activeAddress = AddressManager.getInstance().getPrivKeyAddresses().get(0);
                    }
                    Bither.setActivePerWalletModelData(activeAddress);
                    if (activeAddress != null) {
                        selectWalletPanelByFilename(activeAddress.getAddress());
                    }
                }
            }
        }


    }

    private void addHotAddressList(GridBagConstraints constraints, List<Address> addresses) {
        synchronized (walletPanels) {
            for (Address loopPerWalletModelData : addresses) {
                if (loopPerWalletModelData != null) {
                    JPanel outerPanel = new JPanel();
                    outerPanel.setOpaque(false);
                    outerPanel.setBorder(BorderFactory.createEmptyBorder(TOP_BORDER, LEFT_BORDER, 0, RIGHT_BORDER));
                    outerPanel.setLayout(new GridBagLayout());

                    GridBagConstraints constraints2 = new GridBagConstraints();
                    constraints2.fill = GridBagConstraints.BOTH;
                    constraints2.gridx = 0;
                    constraints2.gridy = 0;
                    constraints2.weightx = 1.0;
                    constraints2.weighty = 1.0;
                    constraints2.gridwidth = 1;
                    constraints2.gridheight = 1;
                    constraints2.anchor = GridBagConstraints.CENTER;

                    SingleWalletForm singleWalletForm = new SingleWalletForm(loopPerWalletModelData, this);
                    singleWalletForm.getPanel().setComponentOrientation(ComponentOrientation
                            .getOrientation(LocaliserUtils.getLocale()));


                    outerPanel.add(singleWalletForm.getPanel(), constraints2);


                    walletListPanel.add(outerPanel, constraints);
                    walletPanels.add(singleWalletForm);
                    constraints.gridy = constraints.gridy + 1;
                }
            }
        }


    }

    private void addPanel(GridBagConstraints constraints, String titleName) {

        JPanel hotTilePanel = new JPanel();
        hotTilePanel.setOpaque(false);
        hotTilePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xc5c5c5)));
        hotTilePanel.setLayout(new GridBagLayout());
        BitherLabel label = new BitherLabel("  "+titleName);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setBackground(new Color(0xf3f3f3));
        label.setForeground(Color.BLACK);
        label.setOpaque(true);

        GridBagConstraints labConstraints2 = new GridBagConstraints();
        labConstraints2.fill = GridBagConstraints.BOTH;
        labConstraints2.gridx = 0;
        labConstraints2.gridy = 0;
        labConstraints2.weightx = 1.0;
        labConstraints2.weighty = 1.0;
        labConstraints2.gridwidth = GridBagConstraints.REMAINDER;
        labConstraints2.gridheight = 1;
        labConstraints2.anchor = GridBagConstraints.WEST;
        label.setFont(FontSizer.INSTANCE.getAdjustedDefaultFontWithDelta(-1));
        label.setPreferredSize(new Dimension(-1, 30));
        hotTilePanel.add(label, labConstraints2);

        walletListPanel.add(hotTilePanel, constraints);
        constraints.gridy = constraints.gridy + 1;

    }


    public final void selectAdjacentWallet(KeyEvent e, String keyStatus) {
        int keyCode = e.getKeyCode();
        int modifiersEx = e.getModifiersEx();

        boolean moveToNextWallet = ((keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_KP_DOWN) && modifiersEx == KeyEvent.SHIFT_DOWN_MASK);
        boolean moveToPreviousWallet = ((keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_KP_UP) && modifiersEx == KeyEvent.SHIFT_DOWN_MASK);

        if (walletPanels != null) {
            synchronized (walletPanels) {
                int currentlySelectedWalletIndex = 0;
                int nextSelectedWalletIndex = -1;
                for (IAddressForm loopSingleWalletPanel : walletPanels) {

                    // Found the currently selected panel.
                    if (moveToNextWallet && currentlySelectedWalletIndex < walletPanels.size() - 1) {
                        nextSelectedWalletIndex = currentlySelectedWalletIndex + 1;
                        break;

                    } else {
                        if (moveToPreviousWallet && currentlySelectedWalletIndex > 0) {
                            nextSelectedWalletIndex = currentlySelectedWalletIndex - 1;
                            break;
                        }
                    }


                    currentlySelectedWalletIndex++;
                }
                if (nextSelectedWalletIndex > -1) {
                    Bither.getCoreController().fireDataChangedUpdateNow();
                }
            }
        }
    }


    @Override
    public ViewEnum getViewId() {
        return ViewEnum.YOUR_WALLETS_VIEW;
    }

    @Override
    public void componentHidden(ComponentEvent arg0) {
    }

    @Override
    public void componentMoved(ComponentEvent arg0) {
    }

    @Override
    public void componentResized(ComponentEvent arg0) {
        int preferredWalletWidth = SingleWalletForm.calculateNormalWidth(this) + LEFT_BORDER + RIGHT_BORDER + 4;
//        if (scrollPane.getVerticalScrollBar().isVisible()) {
//            preferredWalletWidth -= BitherSetting.SCROLL_BAR_DELTA;
//        }

        walletListPanel.setPreferredSize(new Dimension(preferredWalletWidth, walletListPanel.getPreferredSize().height));
        Bither.getMainFrame().getMainFrameUi().calculateDividerPosition();
    }

    @Override
    public void componentShown(ComponentEvent arg0) {
    }


    @Override
    public void blockChange() {
        Bither.getCoreController().fireDataChangedUpdateNow();

    }

    @Override
    public JPanel getPanel() {
        return this;
    }

}