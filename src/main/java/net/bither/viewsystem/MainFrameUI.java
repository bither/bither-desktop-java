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

package net.bither.viewsystem;

import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.implbitherj.BlockNotificationCenter;
import net.bither.platform.listener.GenericQuitEventListener;
import net.bither.platform.listener.GenericQuitResponse;
import net.bither.preference.UserPreference;
import net.bither.utils.ImageLoader;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.DisplayHint;
import net.bither.viewsystem.base.ViewEnum;
import net.bither.viewsystem.base.Viewable;
import net.bither.viewsystem.components.ScrollBarUIDecorator;
import net.bither.viewsystem.froms.MenuBar;
import net.bither.viewsystem.froms.SingleWalletForm;
import net.bither.viewsystem.panels.WalletListPanel;
import net.bither.viewsystem.themes.Themes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;


public class MainFrameUI {
    private MainFrame frame;
    private WalletListPanel walletsView;
    private ViewFactory viewFactory;

    private JSplitPane splitPane;

    // private BitherTabbedPane viewTabbedPane;
    private JPanel headerPanel;
    private JPanel devidePanel;
    private net.bither.viewsystem.froms.MenuBar menuBarFrom;
    private JScrollPane scrollPane;

    private static final Logger log = LoggerFactory.getLogger(MainFrameUI.class);


    final private GenericQuitEventListener quitEventListener;

    public static final GenericQuitResponse bitherFrameQuitResponse = new GenericQuitResponse() {
        @Override
        public void cancelQuit() {
            log.debug("Quit Canceled");
        }

        @Override
        public void performQuit() {
            log.debug("Performed Quit");
        }
    };


    public MainFrameUI(MainFrame frame, GenericQuitEventListener quitEventListener) {
        this.frame = frame;

        this.quitEventListener = quitEventListener;
        viewFactory = new ViewFactory();

    }


    public JPanel getDevidePanel() {
        return devidePanel;
    }


    public WalletListPanel getWalletsView() {
        return walletsView;
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public void initUI(ViewEnum initialView) {
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBackground(Themes.currentTheme.detailPanelBackground());
        GridBagConstraints constraints = new GridBagConstraints();
        GridBagConstraints constraints2 = new GridBagConstraints();

        headerPanel = new JPanel();
        headerPanel.setOpaque(true);
        headerPanel.setBackground(Themes.currentTheme.detailPanelBackground());
        headerPanel.setLayout(new GridBagLayout());
        headerPanel.applyComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));
        menuBarFrom = new MenuBar();

        // Set the application icon.
        ImageIcon imageIcon = ImageLoader.createImageIcon(ImageLoader.BITHER_ICON_FILE);
        if (imageIcon != null) {
            frame.setIconImage(imageIcon.getImage());
        }


        constraints2.fill = GridBagConstraints.BOTH;
        constraints2.gridx = 0;
        constraints2.gridy = 0;
        constraints2.gridwidth = 1;
        constraints2.gridheight = 1;
        constraints2.weightx = 1000.0;
        constraints2.weighty = 1.0;
        constraints2.anchor = GridBagConstraints.LINE_START;

        headerPanel.add(menuBarFrom.getPanelMain(), constraints2);


        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.LINE_START;
        contentPane.add(headerPanel, constraints);


        devidePanel = new JPanel();
        // fill1.setOpaque(false);
        Dimension dimension = new Dimension(1000, 1);
        devidePanel.setPreferredSize(dimension);
        devidePanel.setMinimumSize(dimension);
        devidePanel.setMaximumSize(dimension);
        devidePanel.setBackground(new Color(0xd1d1d1));
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.CENTER;
        contentPane.add(devidePanel, constraints);

        // Create the wallet list panel.
        walletsView = new WalletListPanel();
        BlockNotificationCenter.addBlockChange(walletsView);
        JPanel viewTabbedPane;//= new JPanel(new BorderLayout());


        // Add the transactions tab.
        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.COLD) {
            // JPanel transactionsOutlinePanel = new JPanel(new BorderLayout());
            Viewable coldWalletView = viewFactory.getView(ViewEnum.COLD_WALLET_VIEW);
            viewTabbedPane = coldWalletView.getPanel();


        } else {

            Viewable transactionsView = viewFactory.getView(ViewEnum.TRANSACTIONS_VIEW);
            viewTabbedPane = transactionsView.getPanel();
        }
        // viewTabbedPane.setBackground(ColorAndFontConstants.BACKGROUND_COLOR);
        GridBagLayout gridBagLayout = new GridBagLayout();
        JPanel rightPanel = new JPanel(gridBagLayout);
        rightPanel.setOpaque(true);
        rightPanel.setBackground(Themes.currentTheme.detailPanelBackground());
        GridBagConstraints rightContraints = new GridBagConstraints();
        rightContraints.fill = GridBagConstraints.BOTH;
        rightContraints.gridx = 0;
        rightContraints.gridy = 0;
        rightContraints.gridwidth = 1;
        rightContraints.gridheight = 1;
        rightContraints.weightx = 1.0;
        rightContraints.weighty = 1.0;
        rightContraints.anchor = GridBagConstraints.LINE_START;
        rightContraints.insets = new Insets(0, 5, 0, 0);
        rightPanel.add(viewTabbedPane, rightContraints);

        // Create a split pane with the two scroll panes in it.
        scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(walletsView);
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(BitherSetting.SCROLL_INCREMENT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(BitherSetting.SCROLL_INCREMENT);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.setComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));
        ScrollBarUIDecorator.apply(scrollPane, false);


        if (ComponentOrientation.LEFT_TO_RIGHT == ComponentOrientation.getOrientation(LocaliserUtils.getLocale())) {
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, rightPanel);
        } else {
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, rightPanel, scrollPane);
            splitPane.setResizeWeight(1.0);
        }
        splitPane.setDividerSize(3);
        splitPane.setBackground(Themes.currentTheme.text());
        splitPane.setBorder(
                BorderFactory.createMatteBorder(
                        1, 0, 1, 0,
                        Themes.currentTheme.text()
                ));

        splitPane.setOneTouchExpandable(false);
        splitPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SystemColor.windowBorder));
        // splitPane.setBackground(ColorAndFontConstants.BACKGROUND_COLOR);
        splitPane.setOpaque(true);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1000.0;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        contentPane.add(splitPane, constraints);

        calculateDividerPosition();

        // Cannot get the RTL wallets drawing nicely so switch off adjustment.
        splitPane.setEnabled(ComponentOrientation.LEFT_TO_RIGHT.equals(ComponentOrientation.getOrientation(LocaliserUtils.getLocale())));

    }


    public void updateHeaderOnSwingThread(final long estimatedBalance) {


        String titleText = LocaliserUtils.getString("bitherframe_title");
        frame.setTitle(titleText);

    }

    public void calculateDividerPosition() {
        int dividerPosition = SingleWalletForm.calculateNormalWidth(walletsView) + BitherSetting.WALLET_WIDTH_DELTA;
//        if (walletsView.getScrollPane().getVerticalScrollBar().isVisible()) {
//            dividerPosition += BitherSetting.SCROLL_BAR_DELTA;
//        }
        if (walletsView != null && walletsView.getPreferredSize() != null && walletsView.getPreferredSize().width > dividerPosition) {
            dividerPosition = walletsView.getPreferredSize().width;
        }

        if (ComponentOrientation.RIGHT_TO_LEFT == ComponentOrientation.getOrientation(LocaliserUtils.getLocale())) {
            int width = frame.getWidth();
            if (width == 0) {
                width = (int) frame.getPreferredSize().getWidth();
            }
            dividerPosition = width - dividerPosition; // - WalletListPanel.LEFT_BORDER - WalletListPanel.RIGHT_BORDER - 2;
        }
        splitPane.setEnabled(true);
        splitPane.setDividerLocation(dividerPosition);
        splitPane.setEnabled(ComponentOrientation.LEFT_TO_RIGHT.equals(ComponentOrientation.getOrientation(LocaliserUtils.getLocale())));
    }

    public void recreateAllViewsOnSwingThread(final boolean initUI, ViewEnum initialView) {

        // Close down current view.
        if (Bither.getCoreController().getCurrentView() != ViewEnum.UNKNOWN_VIEW) {
            frame.navigateAwayFromView(Bither.getCoreController().getCurrentView());
        }

        if (initUI) {
            Container contentPane = frame.getContentPane();
            viewFactory.initialise();
            contentPane.removeAll();

            initUI(null);
            try {
                frame.applyComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));
            } catch (ClassCastException cce) {
                cce.printStackTrace();
            }
        }

        //statusBar.refreshOnlineStatusText();

        updateHeader();

        // Tell the wallets list to display.
        if (walletsView != null) {
            walletsView.displayView(DisplayHint.COMPLETE_REDRAW);
        }


    }

    public MenuBar getTickerTablePanel() {
        return menuBarFrom;
    }

    /**
     * Actually update the UI.
     * (Called back from the FireDataChangedTimerTask).
     */
    public void fireDataChangedOnSwingThread(DisplayHint displayHint) {
        updateHeader();

        // Update the password related menu items.
        updateMenuItemsOnWalletChange();

        // Tell the wallets list to display.
        if (walletsView != null) {
            walletsView.displayView(displayHint);
        }

        // Tell the current view to update itself.
        Viewable currentViewView = viewFactory.getView(Bither.getCoreController().getCurrentView());
        if (currentViewView != null) {
            currentViewView.displayView(displayHint);
        }
    }

    private void updateMenuItemsOnWalletChange() {

    }


    public void updateHeader() {
        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.COLD) {
            return;
        }
        long finalEstimatedBalance = 0;
        for (Address address : AddressManager.getInstance().getAllAddresses()) {
            finalEstimatedBalance = finalEstimatedBalance + address.getBalance();
        }
        if (AddressManager.getInstance().getHdAccount() != null) {
            finalEstimatedBalance = finalEstimatedBalance + AddressManager.getInstance().getHdAccount().getBalance();
        }
        final long total = finalEstimatedBalance;

        if (EventQueue.isDispatchThread()) {
            updateHeaderOnSwingThread(total);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateHeaderOnSwingThread(total);
                }
            });
        }
    }

    public void focusableUI() {

        if (UserPreference.getInstance().getAppMode() == BitherjSettings.AppMode.HOT) {


            updateHeader();
        }

        calculateDividerPosition();
    }

    //todo Entering for the first time vericalScrollbar errors
    public void clearScroll() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });

    }


}
