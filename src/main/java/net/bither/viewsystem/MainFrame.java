/**
 * Copyright 2012 multibit.org
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
package net.bither.viewsystem;


import com.google.common.base.Preconditions;
import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.bitherj.core.Address;
import net.bither.platform.listener.GenericQuitEventListener;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.DisplayHint;
import net.bither.viewsystem.base.ViewEnum;
import net.bither.viewsystem.base.ViewSystem;
import net.bither.viewsystem.base.Viewable;
import net.bither.viewsystem.dialogs.PanelDialog;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class MainFrame extends JFrame implements ViewSystem, ApplicationListener {

    private static final Logger log = LoggerFactory.getLogger(MainFrame.class);

    private final CoreController coreController;
    private String helpContext;


    final private GenericQuitEventListener quitEventListener;
    private MainFrameUI mainFrameUi;


    @SuppressWarnings("deprecation")
    public MainFrame(CoreController coreController, ViewEnum initialView) {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread(), "MainFrame isEventDispatchThread");

        this.coreController = coreController;

        this.quitEventListener = this.coreController;

        this.mainFrameUi = new MainFrameUI(MainFrame.this, this.quitEventListener);

        remapCommandOnMac();

        setCursor(Cursor.WAIT_CURSOR);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String titleText = LocaliserUtils.getString("bitherframe_title");
        setTitle(titleText);

        ToolTipManager.sharedInstance().setDismissDelay(BitherSetting.TOOLTIP_DISMISSAL_DELAY);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                quitEventListener.onQuitEvent(null, MainFrameUI.bitherFrameQuitResponse);
            }
        });

        applyComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));

        sizeAndCenter();


        this.mainFrameUi.initUI(initialView);

        this.mainFrameUi.focusableUI();

        displayView(null != initialView ? initialView : ViewEnum.DEFAULT_VIEW());

        pack();

        setVisible(true);


    }


    private void remapCommandOnMac() {
        // Remap to command v and C on a Mac
        if (Bither.getGenericApplication() != null && Bither.getGenericApplication().isMac()) {
            InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
        }

    }

    public MainFrameUI getMainFrameUi() {
        return mainFrameUi;
    }

    private void sizeAndCenter() {
        // Get the screen size as a java dimension.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int height = (int) (screenSize.height * BitherSetting.PROPORTION_OF_VERTICAL_SCREEN_TO_FILL);
        int width = (int) (screenSize.width * BitherSetting.PROPORTION_OF_HORIZONTAL_SCREEN_TO_FILL);

        // Set the jframe height and width.
        setPreferredSize(new Dimension(width, height));
        double startVerticalPositionRatio = (1 - BitherSetting.PROPORTION_OF_VERTICAL_SCREEN_TO_FILL) / 2;
        double startHorizontalPositionRatio = (1 - BitherSetting.PROPORTION_OF_HORIZONTAL_SCREEN_TO_FILL) / 2;
        setLocation((int) (width * startHorizontalPositionRatio), (int) (height * startVerticalPositionRatio));
    }


    @Override
    public void setHelpContext(String helpContext) {
        this.helpContext = helpContext;
    }


    /**
     * Recreate all views.
     */
    @Override
    public void recreateAllViews(final boolean initUI, final ViewEnum initialView) {
        // if initUI set, do an invokeLater or else it can sometimes leave the menu items in the Mac header row.
        if (EventQueue.isDispatchThread() && !initUI) {
            mainFrameUi.recreateAllViewsOnSwingThread(initUI, initialView);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mainFrameUi.recreateAllViewsOnSwingThread(initUI, initialView);
                }
            });
        }
    }


    /**
     * Display next view.
     */
    @Override
    public void displayView(ViewEnum viewToDisplay) {
        log.debug("Displaying view '" + viewToDisplay + "'");
        // Open wallet view obselete - show transactions
        if (ViewEnum.OPEN_WALLET_VIEW == viewToDisplay) {
            viewToDisplay = ViewEnum.TRANSACTIONS_VIEW;
        }
        // Create Bulk addreses obselete - show transactions
        if (ViewEnum.CREATE_BULK_ADDRESSES_VIEW == viewToDisplay) {
            viewToDisplay = ViewEnum.TRANSACTIONS_VIEW;
        }

        // Show wallets view always on display.
        if (ViewEnum.YOUR_WALLETS_VIEW == viewToDisplay) {
            mainFrameUi.getWalletsView().displayView(DisplayHint.COMPLETE_REDRAW);
            return;
        }

        coreController.setCurrentView(viewToDisplay);

        final Viewable nextViewFinal = mainFrameUi.getViewFactory().getView(viewToDisplay);

        if (nextViewFinal == null) {
            log.debug("Cannot display view " + viewToDisplay);
            return;
        }

        if (EventQueue.isDispatchThread()) {
            displayViewOnSwingThread(nextViewFinal);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    displayViewOnSwingThread(nextViewFinal);
                }
            });
        }
    }

    private void displayViewOnSwingThread(final Viewable nextViewFinal) {
        if (nextViewFinal.getViewId() != ViewEnum.TRANSACTIONS_VIEW && nextViewFinal.getViewId() != ViewEnum.COLD_WALLET_VIEW) {
            PanelDialog panelDialog = new PanelDialog(nextViewFinal.getPanel());
            panelDialog.pack();
            panelDialog.setVisible(true);
            return;
        }

        nextViewFinal.displayView(DisplayHint.COMPLETE_REDRAW);

        //log.debug("viewTabbedPane " + System.identityHashCode(viewTabbedPane) + " finally has " + viewTabbedPane.getTabCount() + " tabs.");
        this.setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        getMainFrameUi().clearScroll();

    }


    /**
     * Navigate away from view - this may be on another thread hence the
     * SwingUtilities.invokeLater.
     */
    @Override
    public void navigateAwayFromView(ViewEnum viewToNavigateAwayFrom) {
        if (ViewEnum.YOUR_WALLETS_VIEW == viewToNavigateAwayFrom) {
            // Do nothing
            return;
        }

        final Viewable viewToNavigateAwayFromFinal = mainFrameUi.getViewFactory().getView(viewToNavigateAwayFrom);

//        if (viewToNavigateAwayFromFinal != null) {
//            if (EventQueue.isDispatchThread()) {
//                viewToNavigateAwayFromFinal.navigateAwayFromView();
//            } else {
//                SwingUtilities.invokeLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        viewToNavigateAwayFromFinal.navigateAwayFromView();
//                    }
//                });
//            }
//        }
    }


    @Override
    public void fireFilesHaveBeenChangedByAnotherProcess(Address perWalletModelData) {
        if (Bither.getActionAddress() != null
                && Bither.getActionAddress().getAddress().equals(perWalletModelData.getAddress())) {
//            Message message = new Message(BitherSetting.createTooltipText(LocaliserUtils.getString("singleWalletPanel.dataHasChanged.tooltip.1") + " "
//                    + LocaliserUtils.getString("singleWalletPanel.dataHasChanged.tooltip.2")), true);
//            MessageManager.INSTANCE.addMessage(message);
        }
        fireDataChangedUpdateNow(DisplayHint.COMPLETE_REDRAW);
    }

    /**
     * Mark that the UI needs to be updated as soon as possible.
     */
    @Override
    public void fireDataChangedUpdateNow(final DisplayHint displayHint) {
        if (EventQueue.isDispatchThread()) {
            mainFrameUi.fireDataChangedOnSwingThread(displayHint);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mainFrameUi.fireDataChangedOnSwingThread(displayHint);
                }
            });
        }
    }

    @Override
    @Deprecated
    public void handleAbout(ApplicationEvent event) {
        coreController.displayView(ViewEnum.TRANSACTIONS_VIEW);
        event.setHandled(true);
    }

    @Override
    @Deprecated
    public void handleOpenApplication(ApplicationEvent event) {
        // Ok, we know our application started.
        // Not much to do about that..
    }

    @Override
    @Deprecated
    public void handleOpenFile(ApplicationEvent event) {
        // TODO i18n required.
        JOptionPane.showMessageDialog(this, "Sorry, opening of files with double click is not yet implemented.  Wallet was "
                + event.getFilename());
    }

    @Override
    @Deprecated
    public void handlePreferences(ApplicationEvent event) {

    }

    @Override
    @Deprecated
    public void handlePrintFile(ApplicationEvent event) {
        // TODO i18n required.
        JOptionPane.showMessageDialog(this, "Sorry, printing not implemented");
    }

    @Override
    @Deprecated
    public void handleQuit(ApplicationEvent event) {
        throw new UnsupportedOperationException("Deprecated.");
    }

    @Override
    public void handleReOpenApplication(ApplicationEvent event) {
        setVisible(true);
    }


    public void bringToFront() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                toFront();
                repaint();
            }
        });
    }

    //todo exchange mainFrameUi.updateHeader();


}

