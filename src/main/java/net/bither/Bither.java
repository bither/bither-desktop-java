/*
 *
 *  * Copyright 2014 http://Bither.net
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package net.bither;


import net.bither.bitherj.BitherjSettings;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.crypto.mnemonic.MnemonicCode;
import net.bither.db.AddressDBHelper;
import net.bither.db.DesktopDbImpl;
import net.bither.db.TxDBHelper;
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
import net.bither.runnable.RunnableListener;
import net.bither.utils.*;
import net.bither.viewsystem.CoreController;
import net.bither.viewsystem.MainFrame;
import net.bither.viewsystem.action.ExitAction;
import net.bither.viewsystem.base.ColorAndFontConstants;
import net.bither.viewsystem.base.FontSizer;
import net.bither.viewsystem.dialogs.DialogConfirmTask;
import net.bither.viewsystem.dialogs.DialogProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
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
        // LoggingFactory.bootstrap();
        try {
            initialiseJVM();
        } catch (Exception e) {
            e.printStackTrace();
        }
        applicationDataDirectoryLocator = new ApplicationDataDirectoryLocator();
        initBitherApplication();
        initApp(args);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    StringUtil.maxUsedSize();
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//        }).start();
//        System.out.println("addresses:" + AbstractDb.addressProvider.getAddresses().size());
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    AbstractDb.addressProvider.getAddresses();
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
        //  StringUtil.callSystemGC();
//                }
//            }
//        }).start();

    }

    private static void initBitherApplication() {
        ApplicationInstanceManager.txDBHelper = new TxDBHelper(applicationDataDirectoryLocator.getApplicationDataDirectory());
        ApplicationInstanceManager.txDBHelper.initDb();
        ApplicationInstanceManager.addressDBHelper = new AddressDBHelper(applicationDataDirectoryLocator.getApplicationDataDirectory());
        ApplicationInstanceManager.addressDBHelper.initDb();
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
        }
        //todo A single program
        if (!ApplicationInstanceManager.registerInstance(rawURI)) {
            // Instance already running.
            System.out.println("Another instance of MultiBit is already running.  Exiting.");
            System.exit(0);
        }
        ApplicationInstanceManager.setApplicationInstanceListener(new ApplicationDataDirectoryLocator.ApplicationInstanceListener() {
            @Override
            public void newInstanceCreated(String rawURI) {
                final String finalRawUri = rawURI;
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
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            defaults.put("nimbusOrange", defaults.get("nimbusBase"));
        } catch (UnsupportedLookAndFeelException e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e1) {
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
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Bither");
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
            e.printStackTrace();
            // Try saving any dirty wallets.
            if (coreController != null) {
                ExitAction exitAction = new ExitAction();
                exitAction.actionPerformed(null);
            }
        }
        setVersionCode();

    }

    private static void setVersionCode() {
        if (UpgradeUtil.needUpgrade()) {
            final DialogProgress dialogProgress = new DialogProgress();
            UpgradeUtil.upgradeNewVerion(new RunnableListener() {
                @Override
                public void prepare() {
                    PeerUtil.stopPeer();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dialogProgress.pack();
                            dialogProgress.setVisible(true);
                        }
                    });

                }

                @Override
                public void success(Object obj) {
                    PeerUtil.startPeer();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dialogProgress.dispose();
                            Bither.refreshFrame();
                            UserPreference.getInstance().setVerionCode(BitherSetting.VERSION_CODE);
                            PeerUtil.startPeer();
                        }
                    });
                }

                @Override
                public void error(int errorCode, String errorMsg) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dialogProgress.dispose();
                            DialogConfirmTask dialogConfirmTask =
                                    new DialogConfirmTask(LocaliserUtils.getString("upgrade_error_db_is_lock"), null);
                            dialogConfirmTask.pack();
                            dialogConfirmTask.setVisible(true);

                            ExitAction exitAction = new ExitAction();
                            exitAction.actionPerformed(null);
                        }
                    });

                }
            });
        } else {
            if (UserPreference.getInstance().getVerionCode() < BitherSetting.VERSION_CODE) {
                UserPreference.getInstance().setVerionCode(BitherSetting.VERSION_CODE);
            }
            PeerUtil.startPeer();
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
        return activeWalletModelData;
    }

    public static void setActivePerWalletModelData(Address address) {
        activeWalletModelData = address;
    }

    public static void refreshFrame() {
        if (SwingUtilities.isEventDispatchThread()) {
            refreshFrameInUi();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    refreshFrameInUi();
                }
            });
        }

    }

    private static void refreshFrameInUi() {
        Bither.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Bither.getCoreController().fireRecreateAllViews(true);
        Bither.getCoreController().fireDataChangedUpdateNow();
        Bither.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Bither.getMainFrame().getMainFrameUi().clearScroll();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

}
