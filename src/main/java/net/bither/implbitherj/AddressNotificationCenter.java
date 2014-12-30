package net.bither.implbitherj;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class AddressNotificationCenter {
    public interface IAddressListener {
        public void addressLoadComplete();
    }

    private static List<IAddressListener> addressListenerList = new ArrayList<IAddressListener>();

    public static void notificationAddressLoadComplete() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (IAddressListener addressListener : addressListenerList) {
                    addressListener.addressLoadComplete();
                }
            }
        });

    }

    public static void addAddressListener(IAddressListener addressListener) {
        if (!addressListenerList.contains(addressListener)) {
            addressListenerList.add(addressListener);
        }
    }

    public static void removeAddressListener(IAddressListener addressListener) {
        addressListenerList.remove(addressListener);
    }
}
