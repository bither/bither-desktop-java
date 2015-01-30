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
package net.bither;


import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.crypto.mnemonic.MnemonicCode;
import net.bither.db.AddressDatabaseHelper;
import net.bither.db.BitherDBHelper;
import net.bither.db.DesktopDbImpl;
import net.bither.implbitherj.DesktopImplAbstractApp;
import net.bither.logging.LoggingConfiguration;
import net.bither.logging.LoggingFactory;
import net.bither.mnemonic.MnemonicCodeDesktop;
import net.bither.network.ReplayManager;
import net.bither.platform.GenericApplication;
import net.bither.platform.GenericApplicationFactory;
import net.bither.platform.GenericApplicationSpecification;
import net.bither.platform.builder.OSUtils;
import net.bither.platform.listener.GenericOpenURIEvent;
import net.bither.preference.UserPreference;
import net.bither.utils.Localiser;
import net.bither.utils.LocaliserUtils;
import net.bither.utils.PeerUtil;
import net.bither.viewsystem.CoreController;
import net.bither.viewsystem.MainFrame;
import net.bither.viewsystem.action.ExitAction;
import net.bither.viewsystem.base.ColorAndFontConstants;
import net.bither.viewsystem.base.FontSizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.text.DefaultEditorKit;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Locale;

public final class Bither {

    private static final Logger log = LoggerFactory.getLogger(Bither.class);

    public static long reloadTxTime = -1;
    private static CoreController coreController = null;

    private static MainFrame mainFrame = null;

    private static GenericApplication genericApplication = null;
    private static ApplicationDataDirectoryLocator applicationDataDirectoryLocator = null;


    private static Address activeWalletModelData;

    /**
     * Utility class should not have a public constructor
     */
    private Bither() {
    }


    @SuppressWarnings("deprecation")
    public static void main(String args[]) {
        new LoggingFactory(new LoggingConfiguration(), "bither").configure();
        LoggingFactory.bootstrap();
        try {
            initialiseJVM();
        } catch (Exception e) {
            e.printStackTrace();
        }
        applicationDataDirectoryLocator = new ApplicationDataDirectoryLocator();
        initBitherApplication();
        initApp(args);

    }

    private static void initBitherApplication() {
        ApplicationInstanceManager.txDBHelper = new BitherDBHelper(applicationDataDirectoryLocator.getApplicationDataDirectory());
        ApplicationInstanceManager.txDBHelper.initDb();
        ApplicationInstanceManager.addressDatabaseHelper = new AddressDatabaseHelper(applicationDataDirectoryLocator.getApplicationDataDirectory());
        ApplicationInstanceManager.addressDatabaseHelper.initDb();
        if (UserPreference.getInstance().getAppMode() == null) {
            UserPreference.getInstance().setAppMode(BitherjSettings.AppMode.HOT);
        }

        DesktopImplAbstractApp desktopImplAbstractApp = new DesktopImplAbstractApp();
        desktopImplAbstractApp.construct();
        DesktopDbImpl desktopDb = new DesktopDbImpl();
        desktopDb.construct();
        AddressManager.getInstance();
        try {
            MnemonicCode.setInstance(new MnemonicCodeDesktop());
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                PeerUtil.startPeer();
            }
        }).start();

        //  UserPreference.getInstance().setTransactionFeeMode(BitherjSettings.TransactionFeeMode.Low);


    }

    public static boolean canReloadTx() {
        if (reloadTxTime == -1) {
            return true;
        } else {
            return reloadTxTime + 60 * 60 * 1000 < System.currentTimeMillis();
        }
    }

    private static void runRawURI(String args[]) {
        for (String str : args) {
            System.out.println("args:" + str);
        }
        String rawURI = null;
        if (args != null && args.length > 0) {
            rawURI = args[0];
            log.debug("The args[0] passed into MultiBit = '" + args[0] + "'");
        }
        //todo A single program
//        if (!ApplicationInstanceManager.registerInstance(rawURI)) {
//            // Instance already running.
//            System.out.println("Another instance of MultiBit is already running.  Exiting.");
//            System.exit(0);
//        }
        ApplicationInstanceManager.setApplicationInstanceListener(new ApplicationDataDirectoryLocator.ApplicationInstanceListener() {
            @Override
            public void newInstanceCreated(String rawURI) {
                final String finalRawUri = rawURI;
                log.debug("New instance of MultiBit detected, rawURI = " + rawURI + " ...");
                Runnable doProcessCommandLine = new Runnable() {
                    @Override
                    public void run() {
                        processCommandLineURI(finalRawUri);
                    }
                };

                SwingUtilities.invokeLater(doProcessCommandLine);
            }
        });

    }

    private static void runProcessCommandLineURIWithArgs(String args[]) {
        log.debug("Checking for Bitcoin URI on command line");
        // Check for a valid entry on the command line (protocol handler).
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                log.debug("Started with args[{}]: '{}'", i, args[i]);
            }
            processCommandLineURI(args[0]);
        } else {
            log.debug("No Bitcoin URI provided as an argument");
        }
    }

    private static void initialiseJVM() throws Exception {

        log.debug("Initialising JVM...");

        // Although we guarantee the JVM through the packager it is possible that
        // a power user will use their own
        try {
            // We guarantee the JVM through the packager so we should try it first
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e1) {
                log.error("No look and feel available. MultiBit HD requires Java 7 or higher.", e1);
                System.exit(-1);
            }
        }

        // Set any bespoke system properties
        try {
            // Fix for Windows / Java 7 / VPN bug
            System.setProperty("java.net.preferIPv4Stack", "true");

            // Fix for version.txt not visible for Java 7
            System.setProperty("jsse.enableSNIExtension", "false");

            if (OSUtils.isMac()) {

                // Ensure the correct name is displayed in the application menu
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "multiBit HD");

                // Ensure OSX key bindings are used for copy, paste etc
                // Use the Nimbus keys and ensure this occurs before any component creation
                addOSXKeyStrokes((InputMap) UIManager.get("TextField.focusInputMap"));
                addOSXKeyStrokes((InputMap) UIManager.get("FormattedTextField.focusInputMap"));
                addOSXKeyStrokes((InputMap) UIManager.get("TextArea.focusInputMap"));
                addOSXKeyStrokes((InputMap) UIManager.get("PasswordField.focusInputMap"));
                addOSXKeyStrokes((InputMap) UIManager.get("EditorPane.focusInputMap"));

            }


        } catch (SecurityException se) {
            log.error(se.getClass().getName() + " " + se.getMessage());
        }

    }

    private static void addOSXKeyStrokes(InputMap inputMap) {

        // Undo and redo require more complex handling
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK), DefaultEditorKit.selectAllAction);

    }

    private static void fixJavaBug() {
        // Set any bespoke system properties.
        try {
            // Fix for Windows / Java 7 / VPN bug.
            System.setProperty("java.net.preferIPv4Stack", "true");

            // Fix for version.txt not visible for Java 7
            System.setProperty("jsse.enableSNIExtension", "false");
        } catch (SecurityException se) {
            log.error(se.getClass().getName() + " " + se.getMessage());
        }


    }

    private static void initController(String[] args) {
        try {
            coreController = new CoreController();
            GenericApplicationSpecification specification = new GenericApplicationSpecification();
            specification.getOpenURIEventListeners().add(coreController);
            specification.getPreferencesEventListeners().add(coreController);
            specification.getAboutEventListeners().add(coreController);
            specification.getQuitEventListeners().add(coreController);
            genericApplication = GenericApplicationFactory.INSTANCE.buildGenericApplication(specification);
            runRawURI(args);
            Localiser localiser;
            String userLanguageCode = UserPreference.getInstance().getUserLanguageCode();
            if (userLanguageCode == null) {
                // Initial install - no language info supplied - see if we can
                // use the user default, else Localiser will set it to English.
                localiser = new Localiser(Locale.getDefault());
                UserPreference.getInstance().setUserLanguageCode(localiser.getLocale().getLanguage());

            } else {
                if (BitherSetting.USER_LANGUAGE_IS_DEFAULT.equals(userLanguageCode)) {
                    localiser = new Localiser(Locale.getDefault());
                } else {
                    localiser = new Localiser(new Locale(userLanguageCode));
                }
            }

            LocaliserUtils.setLocaliser(localiser);


            // Initialise replay manager.
            ReplayManager.INSTANCE.initialise(false);


            // Initialise singletons.
            ColorAndFontConstants.init();
            FontSizer.INSTANCE.initialise();

            mainFrame = new MainFrame(coreController, coreController.getCurrentView());
            coreController.registerViewSystem(mainFrame);
            runProcessCommandLineURIWithArgs(args);
            // Indicate to the application that startup has completed.
            coreController.setApplicationStarting(false);
            // Check for any pending URI operations.
            //  bitcoinController.handleOpenURI(rememberedRawBitcoinURI);

            // Check to see if there is a new version.
        } catch (Exception e) {
            // An odd unrecoverable error occurred.
            e.printStackTrace();

            log.error("An unexpected error caused MultiBit to quit.");
            log.error("The error was '" + e.getClass().getCanonicalName() + " " + e.getMessage() + "'");
            e.printStackTrace();
            log.error("Please read http://multibit.org/help_troubleshooting.html for help on troubleshooting.");

            // Try saving any dirty wallets.
            if (coreController != null) {
                ExitAction exitAction = new ExitAction();
                exitAction.actionPerformed(null);
            }
        }

    }

    private static void initApp(final String args[]) {

        // Enclosing try to enable graceful closure for unexpected errors.
        fixJavaBug();

        if (SwingUtilities.isEventDispatchThread()) {

            initController(args);

        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {

                    initController(args);
                    // Create the controllers.


                }
            });
        }


    }

    private static void processCommandLineURI(String rawURI) {
        try {
            // Attempt to detect if the command line URI is valid.
            // Note that this is largely because IE6-8 strip URL encoding
            // when passing in URIs to a protocol handler.
            // However, there is also the chance that anyone could
            // hand-craft a URI and pass
            // it in with non-ASCII character encoding present in the label
            // This a really limited approach (no consideration of
            // "amount=10.0&label=Black & White")
            // but should be OK for early use cases.
            int queryParamIndex = rawURI.indexOf('?');
            if (queryParamIndex > 0 && !rawURI.contains("%")) {
                // Possibly encoded but more likely not
                String encodedQueryParams = URLEncoder.encode(rawURI.substring(queryParamIndex + 1), "UTF-8");
                rawURI = rawURI.substring(0, queryParamIndex) + "?" + encodedQueryParams;
                rawURI = rawURI.replaceAll("%3D", "=");
                rawURI = rawURI.replaceAll("%26", "&");
            }
            final URI uri;
            log.debug("Working with '{}' as a Bitcoin URI", rawURI);
            // Construct an OpenURIEvent to simulate receiving this from a
            // listener
            uri = new URI(rawURI);
            GenericOpenURIEvent event = new GenericOpenURIEvent() {
                @Override
                public URI getURI() {
                    return uri;
                }
            };
            Bither.getCoreController().displayView(Bither.getCoreController().getCurrentView());
            // Call the event which will attempt validation against the
            // Bitcoin URI specification.
            coreController.onOpenURIEvent(event);
        } catch (URISyntaxException e) {
            log.error("URI is malformed. Received: '{}'", rawURI);
        } catch (UnsupportedEncodingException e) {
            log.error("UTF=8 is not supported on this platform");
        }
    }

    public static MainFrame getMainFrame() {
        return mainFrame;
    }

    public static CoreController getCoreController() {
        return coreController;
    }

    public static GenericApplication getGenericApplication() {
        return genericApplication;
    }

    public static ApplicationDataDirectoryLocator getApplicationDataDirectoryLocator() {
        return applicationDataDirectoryLocator;
    }

    public static Address getActionAddress() {
        if (activeWalletModelData == null) {
            if (AddressManager.getInstance().getAllAddresses().size() > 0) {
                activeWalletModelData = AddressManager.getInstance().getAllAddresses().get(0);
            }
        }
        return activeWalletModelData;
    }

    public static void setActivePerWalletModelData(Address address) {
        activeWalletModelData = address;
    }


}
