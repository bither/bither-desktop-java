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

package net.bither.languages;

/**
 * <p>Enum to provide the following to application:</p>
 * <ul>
 * <li>Message keys to use for internationalisation</li>
 * </ul>
 * <p/>
 * <h3>Naming conventions</h3>
 * <p>Message keys are placed in an enum for type safety.</p>
 * <p>Message keys have their resource key so that IDEs can maintain a "where used" reference lookup.</p>
 * <p>Message keys are never concatenated to form larger sentences. Instead use token replacements
 * within a larger sentence structure.</p>
 * <p>Resource keys are simply the message key enum name in lower case.</p>
 *
 * @since 0.0.1
 */
public enum MessageKey {

    // Verbs
    APPLY("apply"),
    APPLY_TOOLTIP("apply_tooltip"),

    UNDO("undo"),
    UNDO_TOOLTIP("undo_tooltip"),

    CANCEL("close"),
    CANCEL_TOOLTIP("close_tooltip"),

    EXIT("exit"),
    EXIT_TOOLTIP("exit_tooltip"),

    SWITCH("switch"),
    SWITCH_TOOLTIP("switch_tooltip"),

    SEND("send"),
    SEND_TOOLTIP("send_tooltip"),

    REQUEST("request"),
    REQUEST_TOOLTIP("request_tooltip"),

    REFRESH("refresh"),
    REFRESH_TOOLTIP("refresh_tooltip"),

    HOME("home"),
    HOME_TOOLTIP("home_tooltip"),

    FINISH("finish"),
    FINISH_TOOLTIP("finish_tooltip"),

    CLOSE("close"),
    CLOSE_TOOLTIP("close_tooltip"),

    SHOW("show"),
    HIDE("hide"),
    HDM("HDM"),
    hdm_keychain_add_hot("hdm_keychain_add_hot"),
    hdm_keychain_add_cold("hdm_keychain_add_cold"),
    hdm_keychain_add_server("hdm_keychain_add_server"),
    ADD("create_new_wallet_action_text"),
    activity_name_add_hdm_address("activity_name_add_hdm_address"),
    ADD_TOOLTIP("create_new_wallet_action_text"),

    RCHECK("rcheck_button"),
    RCHECK_TOOLTIP("rcheck_button"),

    MORE("more_title"),
    UNSIGNED("unsigned"),
    MORE_TOOLTIP("more_title"),
    ABOUT("about_title"),
    ABOUT_TOOLTIP("about.tooltip"),
    EDIT("edit"),
    EDIT_TOOLTIP("edit_tooltip"),

    DELETE("delete"),
    DELETE_TOOLTIP("delete_tooltip"),

    RESTORE("restore"),
    RESTORE_TOOLTIP("restore_tooltip"),

    SEARCH("search"),
    SEARCH_TOOLTIP("search_tooltip"),

    BACK("back"),
    BACK_TOOLTIP("back_tooltip"),

    FORWARD("forward"),
    FORWARD_TOOLTIP("forward_tooltip"),

    BROWSE("browse"),
    BROWSE_TOOLTIP("browse_tooltip"),

    PASTE("paste"),
    PASTE_TOOLTIP("paste_tooltip"),

    PASTE_ALL("paste_all"),
    PASTE_ALL_TOOLTIP("paste_all_tooltip"),

    COPY("copy"),
    COPY_TOOLTIP("copy_tooltip"),

    REPAIR("repair"),
    REPAIR_TOOLTIP("repair_tooltip"),

    CLEAR_ALL("clear_all"),
    CLEAR_ALL_TOOLTIP("clear_all_tooltip"),

    COPY_ALL("copy_all"),
    COPY_ALL_TOOLTIP("copy_all_tooltip"),

    SIGN_MESSAGE("sign_message_button"),


    VERIFY_MESSAGE("verify_message_signature_verify_button"),


    // Nouns

    YES("ok"),
    YES_TOOLTIP("yes_tooltip"),

    NO("no"),
    NO_TOOLTIP("no_tooltip"),

    NEXT("next_page"),
    NEXT_TOOLTIP("next_tooltip"),

    PREVIOUS("previous_page"),
    PREVIOUS_TOOLTIP("previous_tooltip"),

    ALL("all"),
    NONE("none"),
    ERROR("error"),
    @Deprecated
    DEFAULT("default"),

    QR_CODE("qr_code"),
    QR_CODE_TOOLTIP("qr_code_tooltip"),

    // Adjectives
    LEADING("leading"),
    TRAILING("trailing"),

    // Field labels

    RECIPIENT("recipient"),
    RECIPIENT_TOOLTIP("recipient_tooltip"),

    TRANSACTION_LABEL("transaction_label"),
    TRANSACTION_LABEL_TOOLTIP("transaction_label_tooltip"),
    QR_CODE_LABEL("qr_code_label"),
    QR_CODE_LABEL_TOOLTIP("qr_code_label_tooltip"),
    QR_CODE_NOTE_1("qr_code_note_1"),
    QR_CODE_NOTE_2("qr_code_note_2"),

    PRIVATE_NOTES("private_notes"),
    PRIVATE_NOTES_TOOLTIP("private_notes_tooltip"),

    NOTES("notes"),
    NOTES_TOOLTIP("notes_tooltip"),

    // Sign/verify message

    MESSAGE("verify_message_signature_message_hint"),


    SIGNATURE("verify_message_signature_signature_hint"),


    CONTACT_IMAGE("contact_image"),

    ONE_OF_YOUR_ADDRESSES("one_of_your_addresses"),
    THIS_BITCOIN_WAS_SENT_TO_YOU("this_bitcoin_was_sent_to_you"),

    SELECT_FOLDER("select_folder"),
    SELECT_FOLDER_TOOLTIP("select_folder_tooltip"),

    SELECT_FILE("select_file"),
    SELECT_FILE_TOOLTIP("select_file_tooltip"),

    SELECT_WALLET("select_wallet"),
    SELECT_WALLET_TOOLTIP("select_wallet_tooltip"),

    ENTER_PASSWORD("password_prompt"),
    ENTER_PASSWORD_TOOLTIP("enter_password_tooltip"),

    ENTER_NEW_PASSWORD("edit_password_new_password"),

    RETYPE_NEW_PASSWORD("edit_password_confirm_new_password"),

    PASSWORD_VERIFIED("password_verified"),
    PASSWORD_FAILED("password_failed"),

    BITCOIN_AMOUNT("bitcoin_amount"),
    BITCOIN_AMOUNT_TOOLTIP("bitcoin_amount_tooltip"),
    LOCAL_AMOUNT("tx_amount_label"),
    LOCAL_AMOUNT_TOOLTIP("local_amount_tooltip"),
    LOCAL_AMOUNT_INCLUDING_FEES("local_amount_including_fees"),
    AMOUNT_PAID("amount_paid"),
    TRANSACTION_FEE("transaction_fee"),

    CLIENT_FEE("client_fee"),
    CLIENT_FEE_NOW("client_fee_now"),
    CLIENT_FEE_LATER_PLURAL("client_fee_later_plural"),
    CLIENT_FEE_LATER_SINGULAR("client_fee_later_singular"),
    CLIENT_FEE_OVERPAID("client_fee_overpaid"),

    COINBASE("coinbase"),

    NAME("name"),
    NAME_TOOLTIP("name_tooltip"),
    NAMES("names"),
    EMAIL_ADDRESS("email_address"),
    EMAIL_ADDRESS_TOOLTIP("email_address_tooltip"),
    BITCOIN_ADDRESS("address"),
    BITCOIN_ADDRESS_TOOLTIP("bitcoin_address_tooltip"),
    EXTENDED_PUBLIC_KEY("extended_public_key"),
    EXTENDED_PUBLIC_KEY_TOOLTIP("extended_public_key_tooltip"),
    TAGS("tags"),
    TAGS_TOOLTIP("tags_tooltip"),
    EXAMPLE("example"),
    VERSION("version"),

    VISIT_WEBSITE("visit_website"),
    VISIT_WEBSITE_TOOLTIP("visit_website_tooltip"),

    DISPLAY_LANGUAGE("display_language"),

    SHOW_BALANCE("show_balance"),
    SHOW_BALANCE_TOOLTIP("show_balance_tooltip"),

    SELECT_THEME("select_theme"),
    SELECT_THEME_TOOLTIP("select_theme_tooltip"),

    SELECT_DECIMAL_SEPARATOR("select_decimal_separator"),
    SELECT_DECIMAL_SEPARATOR_TOOLTIP("select_decimal_separator_tooltip"),

    SELECT_GROUPING_SEPARATOR("select_grouping_separator"),
    SELECT_GROUPING_SEPARATOR_TOOLTIP("select_grouping_separator_tooltip"),

    SELECT_LOCAL_SYMBOL("select_local_symbol"),
    SELECT_LOCAL_SYMBOL_TOOLTIP("select_local_symbol_tooltip"),

    SELECT_BITCOIN_SYMBOL("select_bitcoin_symbol"),
    SELECT_BITCOIN_SYMBOL_TOOLTIP("select_bitcoin_symbol_tooltip"),

    SELECT_PLACEMENT("select_placement"),
    SELECT_PLACEMENT_TOOLTIP("select_placement_tooltip"),

    SELECT_EXCHANGE_RATE_PROVIDER("select_exchange_rate_provider"),
    SELECT_EXCHANGE_RATE_PROVIDER_TOOLTIP("select_exchange_rate_provider_tooltip"),

    SELECT_LOCAL_CURRENCY("select_local_currency"),
    SELECT_LOCAL_CURRENCY_TOOLTIP("select_local_currency_tooltip"),

    ENTER_ACCESS_CODE("enter_access_code"),
    ENTER_ACCESS_CODE_TOOLTIP("enter_access_code_tooltip"),
    EXCHANGE_RATE_LABEL("exchange_rate_label"),

    NOT_AVAILABLE("not_available"),

    NO_CLIENT_FEE_WAS_ADDED("no_client_fee_was_added"),

    ALERT_SOUND("alert_sound"),
    ALERT_SOUND_TOOLTIP("alert_sound_tooltip"),

    RECEIVE_SOUND("receive_sound"),
    RECEIVE_SOUND_TOOLTIP("receive_sound_tooltip"),

    PLAY_SOUND("play_sound"),
    PLAY_SOUND_TOOLTIP("play_sound_tooltip"),

    SELECT_TOR("select_tor"),
    SELECT_TOR_TOOLTIP("select_tor_tooltip"),


    // Sidebar

    SIDEBAR_TREE("sidebar_tree"),
    WALLET("wallet"),
    SEND_OR_REQUEST("send_or_request"),

    CONTACTS("contacts"),
    CONTACTS_TOOLTIP("contacts_tooltip"),

    PAYMENTS("payments"),

    PREFERENCES("preferences"),
    MANAGE_WALLET("manage_wallet"),
    TOOLS("tools"),

    EXIT_OR_SWITCH("exit_or_switch"),
    EXIT_WALLET("exit_wallet"),
    SWITCH_WALLET("switch_wallet"),

    // Tools
    HISTORY("history"),
    HISTORY_TOOLTIP("history_tooltip"),

    // Placeholders
    APPROXIMATELY("approximately"),
    PARENTHESES("parentheses"),
    LIST_COMMA("list_comma"),
    LIST_ELLIPSIS("list_ellipsis"),
    DECIMAL_COMMA("decimal_comma"),
    DECIMAL_POINT("decimal_point"),
    DECIMAL_SPACE("decimal_space"),

    BITCOIN_URI_ALERT("bitcoin_uri_label"),
    PAYMENT_RECEIVED_ALERT("payment_received_label"),
    PAYMENT_SENT_ALERT("payment_sent_label"),

    PEER_COUNT("peer_count"),
    BLOCKS_LEFT("blocks_left"),

    @Deprecated
    LAST_TRANSACTION_ID("last_transaction_id"),

    @Deprecated
    TRANSACTION_COUNT("transaction_count"),

    // Panels

    NETWORK_CONFIGURATION_ERROR("network_configuration_error"),
    HELP("help"),

    SETTINGS("settings"),
    SETTINGS_TOOLTIP("settings_tooltip"),

    // Titles

    MULTIBIT_HD_TITLE("multibit_hd_title"),

    // Send/request
    REQUEST_BITCOIN_TITLE("request_bitcoin_title"),

    SEND_BITCOIN_TITLE("send_bitcoin_title"),
    CONFIRM_SEND_TITLE("confirm_send_title"),
    SEND_PROGRESS_TITLE("send_progress_title"),

    EXIT_OR_SWITCH_TITLE("exit_or_switch_title"),

    ABOUT_TITLE("about_title"),

    SECURITY_TITLE("security_title"),

    WELCOME_TITLE("welcome_title"),
    SELECT_LANGUAGE_TITLE("select_language_title"),
    SELECT_WALLET_TITLE("select_wallet_title"),

    CREATE_WALLET_PREPARATION_TITLE("create_wallet_preparation_title"),
    SELECT_BACKUP_LOCATION_TITLE("select_backup_location_title"),
    CREATE_WALLET_SEED_PHRASE_TITLE("create_wallet_seed_phrase_title"),
    CONFIRM_WALLET_SEED_PHRASE_TITLE("confirm_wallet_seed_phrase_title"),
    CREATE_WALLET_PASSWORD_TITLE("create_wallet_password_title"),
    CREATE_WALLET_REPORT_TITLE("create_wallet_report_title"),

    RESTORE_PASSWORD_SEED_PHRASE_TITLE("restore_password_seed_phrase_title"),
    RESTORE_PASSWORD_BACKUP_LOCATION_TITLE("restore_password_backup_location_title"),
    RESTORE_PASSWORD_SELECT_BACKUP_TITLE("restore_password_select_backup_title"),
    RESTORE_PASSWORD_TIMESTAMP_TITLE("restore_password_timestamp_title"),
    RESTORE_PASSWORD_REPORT_TITLE("restore_password_report_title"),
    RESTORE_PASSWORD_REPORT_MESSAGE_FAIL("restore_password_report_message_fail"),
    RESTORE_PASSWORD_REPORT_MESSAGE_SUCCESS("restore_password_report_message_success"),

    RESTORE_WALLET_SEED_PHRASE_TITLE("import_hdm_cold_seed_phrase"),
    RESTORE_WALLET_SEED_PHRASE_HD_TITLE("import_hd_account_seed_phrase"),
    RESTORE_WALLET_BACKUP_LOCATION_TITLE("restore_wallet_backup_location_title"),
    RESTORE_WALLET_SELECT_BACKUP_TITLE("restore_wallet_select_backup_title"),
    RESTORE_WALLET_TIMESTAMP_TITLE("restore_wallet_timestamp_title"),
    RESTORE_WALLET_REPORT_TITLE("restore_wallet_report_title"),

    NEW_CONTACT_TITLE("new_contact_title"),
    EDIT_CONTACT_TITLE("edit_contact_title"),
    EDIT_CONTACTS_TITLE("edit_contacts_title"),

    NEW_HISTORY_ENTRY_TITLE("new_history_entry_title"),
    EDIT_HISTORY_ENTRY_TITLE("edit_history_entry_title"),
    EDIT_HISTORY_ENTRIES_TITLE("edit_history_entries_title"),

    PASSWORD_TITLE("password_title"),

    PASSWORD_UNLOCK("password_unlock"),
    PASSWORD_UNLOCK_TOOLTIP("password_unlock_tooltip"),

    CHECK_PRIVATE_KEY("check_private_key_button"),
    CHECK_PRIVATE_KEY_TOOLTIP("check_private_key_button"),
    // Credentials


    PIN_FAILURE("pin_failure"),
    PIN_SUCCESS("pin_success"),


    // Settings titles

    ADD_ADDRESS_SETTING("add_address_setting"),
    EXCHANGE_SETTINGS_TITLE("exchange_settings_title"),
    UNITS_SETTINGS_TITLE("units_settings_title"),
    APPEARANCE_SETTINGS_TITLE("appearance_settings_title"),
    SOUNDS_SETTINGS_TITLE("sounds_settings_title"),
    FEES_SETTINGS_TITLE("fees_settings_title"),
    LABS_SETTINGS_TITLE("labs_settings_title"),

    // Manage wallet titles

    EDIT_WALLET_TITLE("edit_wallet_title"),
    WALLET_DETAILS_TITLE("wallet_details_title"),

    CHANGE_PASSWORD_TITLE("change_password_title"),

    CHANGE_PIN_SELECT_OPTION_TITLE("change_pin_select_option_title"),
    CHANGE_PIN_OPTION("change_pin_option"),
    REMOVE_PIN_OPTION("remove_pin_option"),
    CHANGE_PIN_ENTER_CURRENT_PIN_TITLE("change_pin_enter_current_pin_title"),
    CHANGE_PIN_ENTER_NEW_PIN_TITLE("change_pin_enter_new_pin_title"),
    CHANGE_PIN_CONFIRM_NEW_PIN_TITLE("change_pin_confirm_new_pin_title"),
    CHANGE_PIN_CONFIRM_CHANGE_PIN_TITLE("change_pin_confirm_change_pin_title"),

    REPAIR_WALLET_TITLE("repair_wallet_title"),
    REPAIR_WALLET_PROGRESS_TITLE("repair_wallet_progress_title"),

    EMPTY_WALLET_TITLE("empty_wallet_title"),
    EMPTY_WALLET_CONFIRM_TITLE("empty_wallet_confirm_title"),
    EMPTY_WALLET_PROGRESS_TITLE("empty_wallet_progress_title"),

    // Tools titles
    SELECT_SIGN_ADDRESS("sign_message_select_address"),
    SIGN_MESSAGE_TITLE("sign_message_activity_name"),
    VERIFY_MESSAGE_TITLE("verify_message_signature_activity_name"),

    VERIFY_NETWORK_TITLE("verify_network_title"),


    // Labels
    CONFIRM_SEND_MESSAGE("confirm_send_message"),

    EXCHANGE_RATE_PROVIDER("exchange_rate_provider"),
    EXCHANGE_RATE_PROVIDER_TOOLTIP("exchange_rate_provider_tooltip"),

    BROADCAST_STATUS("broadcast_status"),
    RELAY_STATUS("relay_status"),
    CONFIRMATION_STATUS("confirmation_status"),
    VERIFICATION_STATUS("verification_status"),
    EXCHANGE_RATE_STATUS_OK("exchange_rate_status_ok"),
    EXCHANGE_RATE_STATUS_WARN("exchange_rate_status_warn"),
    SEED_PHRASE_CREATED_STATUS("seed_phrase_created_status"),
    WALLET_PASSWORD_CREATED_STATUS("wallet_password_created_status"),
    BACKUP_LOCATION_STATUS("backup_location_status"),
    SELECT_BACKUP("select_backup"),
    WALLET_CREATED_STATUS("wallet_created_status"),
    CACERTS_INSTALLED_STATUS("cacerts_installed_status"),
    WALLET_REPAIRED_STATUS("wallet_repaired_status"),
    SYNCHRONIZING_STATUS("synchronizing_status"),

    ADDRESS_IS_MINE_STATUS("address_is_mine_status"),

    PASSWORD_CHANGED_STATUS("password_changed_status"),

    HISTORY_WALLET_CREATED("history_wallet_created"),
    HISTORY_WALLET_OPENED("history_wallet_opened"),

    ACCEPT_LICENCE("accept_licence"),
    REJECT_LICENCE("reject_licence"),

    ALERT_REMAINING("alert_remaining"),
    EXCHANGE_FIAT_RATE("exchange_fiat_rate"),
    EXCHANGE_FIAT_RATE_WITH_PROVIDER("exchange_fiat_rate_with_provider"),

    CLICK_NEXT_TO_CONTINUE("click_next_to_continue"),
    CLICK_FINISH_TO_CONTINUE("click_finish_to_continue"),

    SELECT_LANGUAGE("select_language"),
    SELECT_LANGUAGE_TOOLTIP("select_language_tooltip"),

    SEED_SIZE("seed_size"),
    SEED_SIZE_TOOLTIP("seed_size_tooltip"),

    SEED_PHRASE("seed_phrase"),
    SEED_PHRASE_TOOLTIP("seed_phrase_tooltip"),
    TIMESTAMP("timestamp"),
    TIMESTAMP_TOOLTIP("timestamp_tooltip"),
    MULTI_EDIT_NOTE("multi_edit_note"),

    TRANSACTION_CONSTRUCTION_STATUS_SUMMARY("transaction_construction_status_summary"),
    TRANSACTION_CONSTRUCTION_STATUS_DETAIL("transaction_construction_status_detail"),
    TRANSACTION_BROADCAST_STATUS_SUMMARY("transaction_broadcast_status_summary"),
    TRANSACTION_BROADCAST_STATUS_DETAIL("transaction_broadcast_status_detail"),
    TRANSACTION_CONFIRMATION_STATUS("transaction_confirmation_status"),

    // Tool buttons
    SHOW_WELCOME_WIZARD("show_welcome_wizard"),
    SHOW_WELCOME_WIZARD_TOOLTIP("show_welcome_wizard_tooltip"),

    SHOW_SEND_WIZARD("show_send_wizard"),
    SHOW_SEND_WIZARD_TOOLTIP("show_send_wizard_tooltip"),

    SHOW_REQUEST_WIZARD("show_request_wizard"),
    SHOW_REQUEST_WIZARD_TOOLTIP("show_request_wizard_tooltip"),

    SHOW_SOUNDS_WIZARD("show_sounds_wizard"),
    SHOW_SOUNDS_WIZARD_TOOLTIP("show_sounds_wizard_tooltip"),

    SHOW_LABS_WIZARD("show_labs_wizard"),
    SHOW_LABS_WIZARD_TOOLTIP("show_labs_wizard_tooltip"),

    SHOW_UNITS_WIZARD("show_units_wizard"),
    SHOW_UNITS_WIZARD_TOOLTIP("show_units_wizard_tooltip"),

    SHOW_EXCHANGE_WIZARD("show_exchange_wizard"),
    SHOW_EXCHANGE_WIZARD_TOOLTIP("show_exchange_wizard_tooltip"),

    SHOW_APPEARANCE_WIZARD("show_appearance_wizard"),
    SHOW_APPEARANCE_WIZARD_TOOLTIP("show_appearance_wizard_tooltip"),

    SHOW_LANGUAGE_WIZARD("show_language_wizard"),
    SHOW_LANGUAGE_WIZARD_TOOLTIP("show_language_wizard_tooltip"),

    SHOW_EDIT_WALLET_WIZARD("show_edit_wallet_wizard"),
    SHOW_EDIT_WALLET_WIZARD_TOOLTIP("show_edit_wallet_wizard_tooltip"),

    SHOW_CHANGE_PASSWORD_WIZARD("edit_password_title"),
    SHOW_CHANGE_PASSWORD_WIZARD_TOOLTIP("changePasswordAction.tooltip"),

    SHOW_CHANGE_PIN_WIZARD("show_change_pin_wizard"),
    SHOW_CHANGE_PIN_WIZARD_TOOLTIP("show_change_pin_wizard_tooltip"),

    SHOW_VERIFY_NETWORK_WIZARD("show_verify_network_wizard"),
    SHOW_VERIFY_NETWORK_WIZARD_TOOLTIP("show_verify_network_wizard_tooltip"),

    SHOW_REPAIR_WALLET_WIZARD("show_repair_wallet_wizard"),
    SHOW_REPAIR_WALLET_WIZARD_TOOLTIP("show_repair_wallet_wizard_tooltip"),

    SHOW_WALLET_DETAILS_WIZARD("show_wallet_details_wizard"),
    SHOW_WALLET_DETAILS_WIZARD_TOOLTIP("show_wallet_details_wizard_tooltip"),

    SHOW_EMPTY_WALLET_WIZARD("show_empty_wallet_wizard"),
    SHOW_EMPTY_WALLET_WIZARD_TOOLTIP("show_empty_wallet_wizard_tooltip"),

    SHOW_SIGN_WIZARD("show_sign_wizard"),
    SHOW_SIGN_WIZARD_TOOLTIP("show_sign_wizard_tooltip"),

    SHOW_VERIFY_WIZARD("show_verify_wizard"),
    SHOW_VERIFY_WIZARD_TOOLTIP("show_verify_wizard_tooltip"),

    SHOW_ABOUT_WIZARD("show_about_wizard"),
    SHOW_ABOUT_WIZARD_TOOLTIP("show_about_wizard_tooltip"),

    SHOW_ALERT("show_alert"),
    HIDE_ALERT("hide_alert"),

    // Radio buttons
    CREATE_WALLET("create_wallet"),
    RESTORE_PASSWORD("restore_password"),
    RESTORE_WALLET("restore_wallet"),
    USE_EXISTING_WALLET("use_existing_wallet"),

    RESTORE_FROM_SEED_PHRASE("restore_from_seed_phrase"),
    RESTORE_FROM_BACKUP("restore_from_backup"),

    CLOUD_BACKUP_LOCATION("cloud_backup_location"),

    WALLET_DEFAULT_NOTES("wallet_default_notes"),


    // Notes (can contain HTML),

    WELCOME_NOTE_1("welcome_note_1"),
    WELCOME_NOTE_2("welcome_note_2"),
    WELCOME_NOTE_3("welcome_note_3"),

    ABOUT_NOTE_1("about_note_1"),
    ABOUT_NOTE_2("about_note_2"),
    ABOUT_NOTE_3("about_note_3"),

    SELECT_BACKUP_NOTE_1("select_backup_note_1"),
    SELECT_BACKUP_NOTE_1_TOOLTIP("select_backup_note_1_tooltip"),
    SELECT_BACKUP_NOTE_2("select_backup_note_2"),

    PREPARATION_NOTE_1("preparation_note_1"),
    PREPARATION_NOTE_2("preparation_note_2"),
    PREPARATION_NOTE_3("preparation_note_3"),
    PREPARATION_NOTE_4("preparation_note_4"),
    PREPARATION_NOTE_5("preparation_note_5"),
    PREPARATION_NOTE_6("preparation_note_6"),

    SELECT_BACKUP_LOCATION_NOTE_1("select_backup_location_note_1"),
    SELECT_BACKUP_LOCATION_NOTE_2("select_backup_location_note_2"),
    SELECT_BACKUP_LOCATION_NOTE_3("select_backup_location_note_3"),
    SELECT_BACKUP_LOCATION_NOTE_4("select_backup_location_note_4"),

    SEED_WARNING_NOTE_1("seed_warning_note_1"),
    SEED_WARNING_NOTE_2("seed_warning_note_2"),
    SEED_WARNING_NOTE_3("seed_warning_note_3"),
    SEED_WARNING_NOTE_4("seed_warning_note_4"),
    SEED_WARNING_NOTE_5("seed_warning_note_5"),

    CONFIRM_SEED_PHRASE_NOTE_1("confirm_seed_phrase_note_1"),
    CONFIRM_SEED_PHRASE_NOTE_2("confirm_seed_phrase_note_2"),
    CONFIRM_SEED_PHRASE_NOTE_3("confirm_seed_phrase_note_3"),

    WALLET_PASSWORD_NOTE_1("wallet_password_note_1"),
    WALLET_PASSWORD_NOTE_2("wallet_password_note_2"),
    WALLET_PASSWORD_NOTE_3("wallet_password_note_3"),

    RESTORE_METHOD_NOTE_1("restore_method_note_1"),
    RESTORE_METHOD_NOTE_2("restore_method_note_2"),
    RESTORE_METHOD_NOTE_3("restore_method_note_3"),
    RESTORE_METHOD_NOTE_4("restore_method_note_4"),

    RESTORE_BACKUP_NOTE_1("restore_backup_note_1"),
    RESTORE_BACKUP_NOTE_2("restore_backup_note_2"),
    RESTORE_BACKUP_NOTE_3("restore_backup_note_3"),

    RESTORE_FROM_SEED_PHRASE_NOTE_1("restore_from_seed_phrase_note_1"),
    RESTORE_FROM_SEED_PHRASE_NOTE_2("restore_from_seed_phrase_note_2"),
    RESTORE_FROM_SEED_PHRASE_NOTE_3("restore_from_seed_phrase_note_3"),

    RESTORE_TIMESTAMP_NOTE_1("restore_timestamp_note_1"),
    RESTORE_TIMESTAMP_NOTE_2("restore_timestamp_note_2"),
    RESTORE_TIMESTAMP_NOTE_3("restore_timestamp_note_3"),
    RESTORE_TIMESTAMP_NOTE_4("restore_timestamp_note_4"),

    PASSWORD_NOTE_1("password_note_1"),

    SELECT_WALLET_NOTE_1("select_wallet_note_1"),

    RESTORE_WALLET_NOTE_1("restore_wallet_note_1"),

    CHANGE_PASSWORD_NOTE_1("change_password_note_1"),
    CHANGE_PASSWORD_NOTE_2("change_password_note_2"),

    ENTER_CURRENT_PIN("enter_current_pin"),
    ENTER_NEW_PIN("enter_new_pin"),
    CONFIRM_NEW_PIN("confirm_new_pin"),
    SEND_CONFIRM("send_confirm"),
    ENTER_PIN_LOOK_AT_DEVICE("enter_pin_look_at_device"),

    VERIFY_NETWORK_NOTE_1("verify_network_note_1"),
    VERIFY_NETWORK_NOTE_2("verify_network_note_2"),

    UNITS_SETTINGS_NOTE_1("units_settings_note_1"),

    EXCHANGE_SETTINGS_NOTE_1("exchange_settings_note_1"),

    LANGUAGE_CHANGE_NOTE_1("language_change_note_1"),

    THEME_CHANGE_NOTE_1("theme_change_note_1"),

    SOUND_CHANGE_NOTE_1("sound_change_note_1"),

    LAB_CHANGE_NOTE_1("lab_change_note_1"),

    DATA_ENTERED_NOTE_1("data_entered_note_1"),
    DATA_ENTERED_NOTE_2("data_entered_note_2"),

    SIGN_MESSAGE_NOTE_1("sign_message_note_1"),
    SIGN_MESSAGE_NOTE_2("sign_message_note_2"),
    SIGN_MESSAGE_NOTE_3("sign_message_note_3"),

    VERIFY_MESSAGE_NOTE_1("verify_message_note_1"),
    VERIFY_MESSAGE_NOTE_2("verify_message_signature_activity_name"),

    RECIPIENT_SUMMARY("recipient_summary"),

    AMOUNT_SUMMARY_WITH_RATE("amount_summary_with_rate"),
    AMOUNT_SUMMARY_NO_RATE("amount_summary_no_rate"),

    // Wallet detail panel
    APPLICATION_DIRECTORY("application_directory"),
    WALLET_DIRECTORY("wallet_directory"),

    // Payments panel and wizard
    DETAILS("details"),
    DETAILS_TOOLTIP("details_tooltip"),

    EXPORT("export_text"),
    EXPORT_TOOLTIP("exportTransactionsSubmitAction.text"),
    IMPORT("import_title"),
    IMPORT_TOOLTIP("import_title"),

    DELETE_PAYMENT_REQUEST("delete_payment_request"),
    DELETE_PAYMENT_REQUEST_TOOLTIP("delete_payment_request_tooltip"),

    TRANSACTION_OVERVIEW("transaction_overview"),
    TRANSACTION_AMOUNT("transaction_amount"),
    TRANSACTION_DETAIL("transaction_detail"),

    CHOOSE_PAYMENT_REQUEST("choose_payment_request"),
    CHOOSE_PAYMENT_REQUEST_TOOLTIP("choose_payment_request_tooltip"),

    CHOOSE_PAYMENT_REQUEST_LABEL("choose_payment_request_label"),
    PAYMENT_REQUEST_INFO_SINGULAR("payment_request_info_singular"),
    PAYMENT_REQUEST_INFO_PLURAL("payment_request_info_plural"),
    MATCHING_PAYMENT_REQUEST("matching_payment_request"),
    PAYMENT_REQUEST("payment_request"),
    DATE("date"),
    STATUS("status"),
    TYPE("type"),
    FIAT_AMOUNT("fiat_amount"),
    FIAT_CURRENCY("fiat_currency"),

    DESCRIPTION("description"),
    DESCRIPTION_READ_ONLY("description_read_only"),

    TRANSACTION_HASH("transaction_hash"),
    RAW_TRANSACTION("raw_transaction"),

    VIEW_IN_BLOCKCHAIN_INFO("view_in_blockchain_info"),
    VIEW_IN_BLOCKCHAIN_INFO_TOOLTIP("view_in_blockchain_info_tooltip"),

    VIEW_IN_EXTERNAL_BROWSER("view_in_external_browser"),
    VIEW_IN_EXTERNAL_BROWSER_TOOLTIP("view_in_external_browser_tooltip"),

    SIZE("size"),
    SIZE_VALUE("size_value"),


    // Themes
    LIGHT_THEME("light_theme"),
    DARK_THEME("dark_theme"),
    BOOTSTRAP_THEME("bootstrap_theme"),

    // Date rendering
    TODAY("today"),
    YESTERDAY("yesterday"),

    // Export payments
    SELECT_EXPORT_PAYMENTS_LOCATION("select_export_payments_location"),
    EXPORT_PAYMENTS_REPORT("export_payments_report"),
    SELECT_EXPORT_PAYMENTS_LOCATION_NOTE_1("select_export_payments_location_note_1"),
    SELECT_EXPORT_PAYMENTS_LOCATION_NOTE_2("select_export_payments_location_note_2"),
    SELECT_EXPORT_PAYMENTS_LOCATION_NOTE_3("select_export_payments_location_note_3"),
    SELECT_EXPORT_PAYMENTS_LOCATION_NOTE_4("select_export_payments_location_note_4"),
    EXPORT_TRANSACTIONS_STEM("export_transactions_stem"),
    EXPORT_PAYMENT_REQUESTS_STEM("export_payment_requests_stem"),
    EXPORT_WAS_SUCCESSFUL("export_was_successful"),
    TRANSACTIONS_WERE_EXPORTED_TO_THE_FILE("transactions_were_exported_to_the_file"),
    PAYMENT_REQUESTS_WERE_EXPORTED_TO_THE_FILE("payment_requests_were_exported_to_the_file"),
    COULD_NOT_WRITE_TO_THE_DIRECTORY("could_not_write_to_the_directory"),

    // Repair wallet
    REPAIR_WALLET_NOTE_1("repair_wallet_note_1"),
    REPAIR_WALLET_NOTE_2("repair_wallet_note_2"),
    REPAIR_WALLET_NOTE_3("repair_wallet_note_3"),

    SPENDABLE_BALANCE_IS_LOWER("spendable_balance_is_lower"),
    FA_EYE("add_address_tab_watchonly"),
    FROM_CAMERA("qr_code_form_camera"),
    FROM_FILE("qr_code_from_file"),
    PRIVATE_KEY_QRCODE_ENCRYPTED("address_detail_private_key_qr_code_encrypted"),
    PRIVATE_KEY_QRCODE_DECRYPTED("address_detail_private_key_qr_code_decrypted"),
    PRIVATE_KEY_TEXT("private_key_text"),
    IMPORT_PRIVATE_KEY_TEXT("import_private_key_text"),
    IMPORT_PRIVATE_KEY_QRCODE("import_private_key_qr_code"),
    IMPORT_BIP38_PRIVATE_KEY_TEXT("import_bip38_key_qr_code"),
    IMPORT_BIP38_PRIVATE_KEY_QRCODE("import_bip38_key_qr_code"),
    ADVANCE("advance"),
    PEERS("peers"),
    BLOCKS("blocks"),
    RELOAD_TX("reload_tx"),
    SWITCH_COLD("launch_sequence_switch_to_cold"),
    ADDRESS("address"),
    ADDRESS_DETAIL("address_detail"),
    TRANSCATION("transcation"),
    SIGN_TX("sign_transaction"),
    WATCH_ONLY_QRCODE("qr_code_for_all_addresses"),
    CLONE_QRCODE("clone_from_button"),
    HDM_COLD_PUB_KEY_QR_CODE("hdm_cold_pub_key_qr_code_name"),
    HDM_SERVER_QR_CODE("hdm_server_qr_code_name"),
    HDM_COLD_SEED_QR_CODE("hdm_cold_seed_qr_code"),
    HDM_COLD_SEED_WORD_LIST("hdm_cold_seed_word_list"),
    HDM_KEYCHAIN_ADD_COLD("hdm_keychain_add_cold"),
    import_hdm_cold_seed_qr_code("import_hdm_cold_seed_qr_code"),
    import_hdm_cold_seed_phrase("import_hdm_cold_seed_phrase"),
    xrandom("xrandom"),
    hdm_singular_check_title("hdm_singular_check_title"),
    address_group_hdm_recovery("address_group_hdm_recovery"),
    hdm_send_with_cold("hdm_send_with_cold"),
    hdm_reset_server_password_setting_name("hdm_reset_server_password_setting_name"),
    hdm_hot_seed_qr_code("hdm_hot_seed_qr_code"),
    hdm_hot_seed_word_list("hdm_hot_seed_word_list"),
    donate_button("donate_button"),
    add_address_generate_address_with_private_key("add_address_generate_address_with_private_key"),
    vanity_address("vanity_address"),
    add_hd_account_tab_hd("add_hd_account_tab_hd"),
    add_hd_account_seed_qr_code("add_hd_account_seed_qr_code"),
    add_hd_account_seed_qr_phrase("add_hd_account_seed_qr_phrase"),
    import_hd_account_seed_qr_code("import_hd_account_seed_qr_code"),
    import_hd_account_seed_phrase("import_hd_account_seed_phrase"),
    vanity_address_option("vanity_address_option")


    // End of enum
    ;

    private final String key;

    private MessageKey(String key) {
        this.key = key;
    }

    /**
     * @return The key for use with the resource bundles
     */

    public String getKey() {
        return key;
    }

}
