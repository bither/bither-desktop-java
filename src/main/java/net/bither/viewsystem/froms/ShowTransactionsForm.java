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

package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.BitherSetting;
import net.bither.bitherj.api.http.BitherUrl;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDAccount;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.utils.Utils;
import net.bither.implbitherj.BlockNotificationCenter;
import net.bither.implbitherj.TxNotificationCenter;
import net.bither.languages.MessageKey;
import net.bither.model.TxTableModel;
import net.bither.utils.DateUtils;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.ViewUtil;
import net.bither.viewsystem.action.ShowTransactionDetailsAction;
import net.bither.viewsystem.base.*;
import net.bither.viewsystem.base.renderer.DecimalAlignRenderer;
import net.bither.viewsystem.base.renderer.ImageRenderer;
import net.bither.viewsystem.base.renderer.TrailingJustifiedDateRenderer;
import net.bither.viewsystem.components.ScrollBarUIDecorator;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ShowTransactionsForm implements Viewable, TxNotificationCenter.ITxListener, BlockNotificationCenter.IBlockListener {

    private JTable table;
    private TxTableModel txTableModel;
    private TableRowSorter<TableModel> rowSorter;

    private ListSelectionModel listSelectionModel;
    private int selectedRow = -1;

    private Action showTransactionDetailsAction;
    private JButton showTransactionsButton;


    private JScrollPane scrollPane;
    private ShowTransactionHeaderForm showTransactionHeaderForm;
    private JPanel panelMain;
    private List<Tx> txList = new ArrayList<Tx>();
    private JButton btnAddress;

    public ShowTransactionsForm() {
        TxNotificationCenter.addTxListener(ShowTransactionsForm.this);
        BlockNotificationCenter.addBlockChange(ShowTransactionsForm.this);
        initUI();
        panelMain.applyComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));
        refreshTx();

    }

    private void refreshTx() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Bither.getActionAddress() != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (Bither.getActionAddress() instanceof HDAccount) {
                                btnAddress.setVisible(false);
                            } else {
                                btnAddress.setVisible(true);
                            }
                        }
                    });

                    final List<Tx> actionTxList = Bither.getActionAddress().getTxs();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {

                            txList.clear();
                            txList.addAll(actionTxList);
                            txTableModel.fireTableDataChanged();
                        }
                    });

                }

            }
        }).start();
    }

    private void initUI() {

        showTransactionHeaderForm = new ShowTransactionHeaderForm();
        JPanel panel = showTransactionHeaderForm.getPanel();
        JPanel btnTxPanel = createTxDetailButtonPanel();
        panelMain.add(panel, BorderLayout.NORTH);

        JPanel transactionsPanel = createTransactionsPanel();

        panelMain.add(transactionsPanel, BorderLayout.CENTER);

        panelMain.add(btnTxPanel, BorderLayout.SOUTH);

        if (AddressManager.getInstance().getAllAddresses().size() == 0 &&
                AddressManager.getInstance().getHdAccount() == null) {
            showTransactionHeaderForm.setVisible(false);
        } else {
            showTransactionHeaderForm.setVisible(true);
        }

    }


    private JPanel createTransactionsPanel() {
        JPanel transactionsPanel = new JPanel();
        transactionsPanel.setMinimumSize(new Dimension(550, 160));
        transactionsPanel.setBackground(Themes.currentTheme.detailPanelBackground());
        transactionsPanel.setLayout(new GridBagLayout());
        transactionsPanel.setOpaque(true);
        GridBagConstraints constraints = new GridBagConstraints();

        txTableModel = new TxTableModel(txList);
        table = new JTable(txTableModel);
        table.setOpaque(false);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));
        table.setRowHeight(Math.max(BitherSetting.MINIMUM_ICON_HEIGHT, panelMain.getFontMetrics(FontSizer.INSTANCE.getAdjustedDefaultFont()).getHeight()) + BitherSetting.HEIGHT_DELTA);

        // Use status icons.
        table.getColumnModel().getColumn(0).setCellRenderer(new ImageRenderer(ShowTransactionsForm.this));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setBackground(Themes.currentTheme.detailPanelBackground());

        // No row is currently selected.
        selectedRow = -1;

        // Listener for row selections.
        listSelectionModel = table.getSelectionModel();
        listSelectionModel.addListSelectionListener(new SharedListSelectionHandler(showTransactionDetailsAction));

        // Date right justified.
        table.getColumnModel().getColumn(1).setCellRenderer(new TrailingJustifiedDateRenderer(ShowTransactionsForm.this));

        // Justify column headers.
        justifyColumnHeaders();

//        // Description leading justified (set explicitly as it does not seem to work otherwise).
//        if (ComponentOrientation.getOrientation(LocaliserUtils.getLocale()).isLeftToRight()) {
//            table.getColumnModel().getColumn(2).setCellRenderer(new LeadingJustifiedRenderer(ShowTransactionsForm.this));
//        } else {
//            table.getColumnModel().getColumn(2).setCellRenderer(new TrailingJustifiedStringRenderer(ShowTransactionsForm.this));
//        }

        // Amount decimal aligned
        DecimalAlignRenderer decimalAlignRenderer = new DecimalAlignRenderer(ShowTransactionsForm.this);
        table.getColumnModel().getColumn(2).setCellRenderer(decimalAlignRenderer);


        FontMetrics fontMetrics = panelMain.getFontMetrics(FontSizer.INSTANCE.getAdjustedDefaultFont());
        TableColumn tableColumn = table.getColumnModel().getColumn(0); // status
        int statusWidth = fontMetrics.stringWidth(LocaliserUtils.getString("walletData.statusText"));
        tableColumn.setPreferredWidth(statusWidth + BitherSetting.STATUS_WIDTH_DELTA);

        tableColumn = table.getColumnModel().getColumn(1); // Date.


        int dateWidth = Math.max(fontMetrics.stringWidth(LocaliserUtils.getString("walletData.dateText")),
                fontMetrics.stringWidth(DateUtils.getDateTimeString(new Date(DateUtils.nowUtc().getMillis()))));
        tableColumn.setPreferredWidth(dateWidth);

        tableColumn = table.getColumnModel().getColumn(2); // Amount (BTC).
        int amountBTCWidth = Math.max(fontMetrics.stringWidth(LocaliserUtils.getString("sendBitcoinPanel.amountLabel") + " (BTC)"),
                fontMetrics.stringWidth("00000.000000000"));
        tableColumn.setPreferredWidth(amountBTCWidth);
        tableColumn.setMinWidth(amountBTCWidth);

        // Row sorter.
        rowSorter = new TableRowSorter<TableModel>(table.getModel());
        table.setRowSorter(rowSorter);

        // Sort by date descending.
        List<TableRowSorter.SortKey> sortKeys = new ArrayList<TableRowSorter.SortKey>();
        sortKeys.add(new TableRowSorter.SortKey(1, SortOrder.DESCENDING));
        rowSorter.setSortKeys(sortKeys);


        rowSorter.setComparator(1, dateComparator);
        rowSorter.setComparator(2, comparatorNumber);


        scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPaneSetup();

        showTransactionDetailsAction.setEnabled(table.getSelectedRow() > -1);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;

        transactionsPanel.add(scrollPane, constraints);

        return transactionsPanel;
    }

    private void justifyColumnHeaders() {
        TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
        JLabel label = (JLabel) renderer;
        label.setHorizontalAlignment(JLabel.CENTER);

    }

    private JPanel createTxDetailButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SystemColor.windowBorder));
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(Themes.currentTheme.detailPanelBackground());
        buttonPanel.setComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));


        showTransactionDetailsAction = new ShowTransactionDetailsAction(this);
        showTransactionsButton = Buttons.newLaunchBrowserButton(showTransactionDetailsAction
                , MessageKey.TRANSCATION);
        showTransactionsButton.setEnabled(false);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 0.1;
        constraints.weighty = 1.0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        buttonPanel.add(showTransactionsButton, constraints);


        btnAddress = Buttons.newLaunchBrowserButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String url = BitherUrl.BLOCKCHAIN_INFO_ADDRESS_URL + Bither.getActionAddress().getAddress();
                    ViewUtil.openURI(new URI(url));
                } catch (URISyntaxException ie) {
                    ie.printStackTrace();
                }

            }
        }, MessageKey.ADDRESS);
        if (Bither.getActionAddress() == null) {
            btnAddress.setEnabled(false);
        }


        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 0.1;
        constraints.weighty = 1.0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.anchor = GridBagConstraints.LINE_END;
        buttonPanel.add(btnAddress, constraints);


//        JPanel fill1 = new JPanel();
//        fill1.setOpaque(false);
//        constraints.fill = GridBagConstraints.HORIZONTAL;
//        constraints.gridx = 3;
//        constraints.gridy = 0;
//        constraints.weightx = 200;
//        constraints.weighty = 1;
//        constraints.gridwidth = 1;
//        constraints.gridheight = 1;
//        constraints.anchor = GridBagConstraints.CENTER;
//        buttonPanel.add(fill1, constraints);

        return buttonPanel;
    }


    private void scrollPaneSetup() {
        scrollPane.setBackground(Themes.currentTheme.detailPanelBackground());
        scrollPane.getViewport().setBackground(Themes.currentTheme.detailPanelBackground());
        scrollPane.setComponentOrientation(ComponentOrientation.getOrientation(LocaliserUtils.getLocale()));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(BitherSetting.SCROLL_INCREMENT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(BitherSetting.SCROLL_INCREMENT);
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, SystemColor.windowBorder));
        ScrollBarUIDecorator.apply(scrollPane, false);
    }

    @Override
    public void displayView(DisplayHint displayHint) {
        if (AddressManager.getInstance().getAllAddresses().size() == 0 &&
                AddressManager.getInstance().getHdAccount() == null) {
            showTransactionHeaderForm.setVisible(false);
        } else {
            showTransactionHeaderForm.setVisible(true);
        }
        justifyColumnHeaders();
        scrollPaneSetup();

        // Amount decimal aligned
        DecimalAlignRenderer decimalAlignRenderer = new DecimalAlignRenderer(ShowTransactionsForm.this);
        table.getColumnModel().getColumn(2).setCellRenderer(decimalAlignRenderer);
        showTransactionHeaderForm.updateUI();
        refreshTx();

        if (selectedRow > -1 && selectedRow < table.getRowCount()) {
            table.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }


    public void setSelectedRow(int row) {
        selectedRow = row;
    }

    public Tx getSelectedRowData() {
        int row = table.getSelectedRow();
        return txTableModel.getRow(rowSorter.convertRowIndexToModel(row));
    }

    public JTable getTable() {
        return table;
    }

    @Override
    public ViewEnum getViewId() {
        return ViewEnum.TRANSACTIONS_VIEW;
    }

    @Override
    public void notificatTx(String address, Tx tx, Tx.TxNotificationType txNotificationType, long deltaBalance) {
        String actionAddress = "";
        if (Bither.getActionAddress() != null) {
            actionAddress = Bither.getActionAddress().getAddress();
        }
        if (Utils.compareString(address, actionAddress)) {
            displayView(DisplayHint.WALLET_TRANSACTIONS_HAVE_CHANGED);
        }

    }

    @Override
    public void blockChange() {
        displayView(DisplayHint.WALLET_TRANSACTIONS_HAVE_CHANGED);
    }

    @Override
    public JPanel getPanel() {
        return panelMain;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelMain = new JPanel();
        panelMain.setLayout(new BorderLayout(0, 0));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }

    class SharedListSelectionHandler implements ListSelectionListener {
        private Action showTransactionDetailsAction;

        SharedListSelectionHandler(Action action) {
            this.showTransactionDetailsAction = action;
        }

        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
                showTransactionDetailsAction.setEnabled(false);
                showTransactionsButton.invalidate();
                showTransactionsButton.validate();
                showTransactionsButton.repaint();
            } else {
                // Find out which indexes are selected.
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        showTransactionDetailsAction.setEnabled(true);
                        showTransactionsButton.invalidate();
                        showTransactionsButton.validate();
                        showTransactionsButton.repaint();
                        break;
                    }
                }
            }
        }
    }

    Comparator<Date> dateComparator = new Comparator<Date>() {
        @Override
        public int compare(Date o1, Date o2) {
            if (o1 == null) {
                if (o2 == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                if (o2 == null) {
                    return -1;
                }
            }
            long n1 = o1.getTime();
            long n2 = o2.getTime();
            if (n1 == 0) {
                // Object 1 has missing date.
                return 1;
            }
            if (n2 == 0) {
                // Object 2 has missing date.
                return -1;
            }
            if (n1 < n2) {
                return -1;
            } else if (n1 > n2) {
                return 1;
            } else {
                return 0;
            }
        }
    };


    Comparator<String> comparatorNumber = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            try {
                if (o1 == null) {
                    if (o2 == null) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if (o2 == null) {
                        return -1;
                    }
                }
                DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(LocaliserUtils.getLocale());
                formatter.setParseBigDecimal(true);

                // Convert spaces to non breakable space.
                o1 = o1.replaceAll(" ", "\u00A0");
                o2 = o2.replaceAll(" ", "\u00A0");

                BigDecimal parsedO1 = (BigDecimal) formatter.parse(o1);
                BigDecimal parsedO2 = (BigDecimal) formatter.parse(o2);
                return parsedO1.compareTo(parsedO2);
            } catch (NumberFormatException nfe) {
                return o1.compareTo(o2);
            } catch (ParseException e) {
                return o1.compareTo(o2);
            }
        }
    };


}
