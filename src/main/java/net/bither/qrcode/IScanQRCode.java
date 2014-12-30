package net.bither.qrcode;

public interface IScanQRCode {
    public void handleResult(String result, IReadQRCode readQRCode);
}
