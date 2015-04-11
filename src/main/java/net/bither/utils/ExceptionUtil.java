package net.bither.utils;

import net.bither.bitherj.api.http.HttpSetting;

public class ExceptionUtil {
    public static final String getHDMHttpExceptionMessage(int code) {
        switch (code) {
            case HttpSetting.HDMBIdIsAlready:
                return LocaliserUtils.getString("hdm_exception_bid_already_exists");
            case HttpSetting.MessageSignatureIsWrong:
                return LocaliserUtils.getString("hdm_keychain_add_sign_server_qr_code_error");
            default:
                return LocaliserUtils.getString("network_or_connection_error");
        }
    }
}
