package net.bither.viewsystem.froms;

import com.google.common.base.Preconditions;
import net.bither.Bither;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.model.Market;
import net.bither.preference.UserPreference;
import net.bither.utils.ExchangeUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.MarketUtil;
import net.bither.utils.ViewUtil;
import net.bither.viewsystem.base.Buttons;
import net.bither.viewsystem.base.Labels;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.components.ComboBoxes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Locale;

public class ExchangePreferencePanel extends WizardPanel {
    private JComboBox<String> exchangeProviderComboBox;
    private JButton marketRateProviderBrowserButton;

    private JLabel currencyCodeLabel;
    private JComboBox<String> currencyCodeComboBox;

    public ExchangePreferencePanel(boolean isPopover) {
        super(MessageKey.EXCHANGE_SETTINGS_TITLE, AwesomeIcon.DOLLAR, isPopover);

    }

    public ExchangePreferencePanel() {
        this(false);

    }

    @Override
    public void initialiseContent(JPanel panel) {


        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[][][]", // Column constraints
                "[][][][]80[]" // Row constraints
        ));

        Locale locale = LocaliserUtils.getLocale();

        Preconditions.checkNotNull(locale, "'locale' cannot be empty");

        marketRateProviderBrowserButton = Buttons.newLaunchBrowserButton(getExchangeRateProviderBrowserAction());


        exchangeProviderComboBox = ComboBoxes.newExchangeRateProviderComboBox(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                int marketIndex = exchangeProviderComboBox.getSelectedIndex();
                MarketUtil.MarketType selectMarketType = MarketUtil.getMarketType(marketIndex);
                if (UserPreference.getInstance().getDefaultMarket() != selectMarketType) {
                    UserPreference.getInstance().setMarketType(selectMarketType);
                    Bither.getMainFrame().getMainFrameUi().getTickerTablePanel().updateTicker();

                }


            }
        });
        currencyCodeComboBox = ComboBoxes.newCurrencyCodeComboBox(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int currencyIndex = currencyCodeComboBox.getSelectedIndex();

                ExchangeUtil.Currency selectCurrency = ExchangeUtil.getCurrency(currencyIndex);


                if (UserPreference.getInstance().getDefaultCurrency() != selectCurrency) {
                    UserPreference.getInstance().setExchangeCurrency(selectCurrency);
                    Bither.getMainFrame().getMainFrameUi().getTickerTablePanel().updateTicker();

                }

            }
        });


        // Local currency
        currencyCodeLabel = Labels.newLocalCurrency();


        //panel.add(Labels.newExchangeSettingsNote(), "growx,push,span 3,wrap");

        panel.add(Labels.newSelectExchangeRateProvider(), "shrink");
        panel.add(exchangeProviderComboBox, "growx,push");
        panel.add(marketRateProviderBrowserButton, "shrink,wrap");


        panel.add(currencyCodeLabel, "shrink");
        panel.add(currencyCodeComboBox, "growx,push");

    }

    private Action getExchangeRateProviderBrowserAction() {

        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Market market = MarketUtil.getDefaultMarket();
                    ViewUtil.openURI(new URI(market.getUrl()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        };
    }
}
