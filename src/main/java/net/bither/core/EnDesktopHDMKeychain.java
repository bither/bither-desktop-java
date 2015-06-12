package net.bither.core;


import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import net.bither.bitherj.AbstractApp;
import net.bither.bitherj.api.CreateHDMAddressApi;
import net.bither.bitherj.core.AbstractHD;
import net.bither.bitherj.core.HDMAddress;
import net.bither.bitherj.core.HDMBId;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.EncryptedData;
import net.bither.bitherj.crypto.PasswordSeed;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.hd.HDKeyDerivation;
import net.bither.bitherj.crypto.mnemonic.MnemonicCode;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;
import net.bither.bitherj.db.AbstractDb;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Base58;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.db.EnDesktopProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EnDesktopHDMKeychain extends AbstractHD {

    public static interface HDMFetchRemotePublicKeys {
        void completeRemotePublicKeys(CharSequence password, List<HDMAddress.Pubs> partialPubs)
                throws Exception;
    }

    public static interface HDMFetchRemoteAddresses {
        List<HDMAddress.Pubs> getRemoteExistsPublicKeys(CharSequence password);
    }

    public static interface HDMAddressChangeDelegate {
        public void hdmAddressAdded(EnDesktopHDMAddress address);
    }

    private static final Logger log = LoggerFactory.getLogger(EnDesktopHDMKeychain.class);


    protected ArrayList<EnDesktopHDMAddress> allCompletedAddresses;
    private Collection<EnDesktopHDMAddress> addressesInUse;
    private Collection<EnDesktopHDMAddress> addressesTrashed;


    private HDMAddressChangeDelegate addressChangeDelegate;

    public EnDesktopHDMKeychain(byte[] mnemonicSeed, CharSequence password) throws MnemonicException
            .MnemonicLengthException {
        this.mnemonicSeed = mnemonicSeed;
        String firstAddress = null;
        EncryptedData encryptedMnemonicSeed = null;
        EncryptedData encryptedHDSeed = null;
        ECKey k = new ECKey(mnemonicSeed, null);
        String address = k.toAddress();
        k.clearPrivateKey();

        hdSeed = seedFromMnemonic(mnemonicSeed);
        encryptedHDSeed = new EncryptedData(hdSeed, password, isFromXRandom);
        encryptedMnemonicSeed = new EncryptedData(mnemonicSeed, password, isFromXRandom);
        firstAddress = getFirstAddressFromSeed(password);
        wipeHDSeed();
        wipeMnemonicSeed();
        hdSeedId = EnDesktopProvider.getInstance().addHDKey(encryptedMnemonicSeed.toEncryptedString(),
                encryptedHDSeed.toEncryptedString(), firstAddress, isFromXRandom, address, null, null);
        allCompletedAddresses = new ArrayList<EnDesktopHDMAddress>();

    }

    // Create With Random
    public EnDesktopHDMKeychain(SecureRandom random, CharSequence password) {
        isFromXRandom = random.getClass().getCanonicalName().indexOf("XRandom") >= 0;
        mnemonicSeed = new byte[32];
        String firstAddress = null;
        EncryptedData encryptedMnemonicSeed = null;
        EncryptedData encryptedHDSeed = null;
        while (firstAddress == null) {
            try {
                random.nextBytes(mnemonicSeed);
                hdSeed = seedFromMnemonic(mnemonicSeed);
                encryptedHDSeed = new EncryptedData(hdSeed, password, isFromXRandom);
                encryptedMnemonicSeed = new EncryptedData(mnemonicSeed, password, isFromXRandom);
                firstAddress = getFirstAddressFromSeed(password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ECKey k = new ECKey(mnemonicSeed, null);
        String address = k.toAddress();
        k.clearPrivateKey();
        wipeHDSeed();
        wipeMnemonicSeed();
        hdSeedId = EnDesktopProvider.getInstance().addHDKey(encryptedMnemonicSeed.toEncryptedString(),
                encryptedHDSeed.toEncryptedString(), firstAddress, isFromXRandom, address, null, null);
        allCompletedAddresses = new ArrayList<EnDesktopHDMAddress>();
    }

    // From DB
    public EnDesktopHDMKeychain(int seedId) {
        this.hdSeedId = seedId;
        allCompletedAddresses = new ArrayList<EnDesktopHDMAddress>();
        initFromDb();
    }

    // Import
    public EnDesktopHDMKeychain(EncryptedData encryptedMnemonicSeed, CharSequence password,
                                HDMFetchRemoteAddresses fetchDelegate) throws
            HDMBitherIdNotMatchException, MnemonicException.MnemonicLengthException {
        mnemonicSeed = encryptedMnemonicSeed.decrypt(password);
        hdSeed = seedFromMnemonic(mnemonicSeed);
        isFromXRandom = encryptedMnemonicSeed.isXRandom();
        EncryptedData encryptedHDSeed = new EncryptedData(hdSeed, password, isFromXRandom);
        allCompletedAddresses = new ArrayList<EnDesktopHDMAddress>();
        ArrayList<EnDesktopHDMAddress> as = new ArrayList<EnDesktopHDMAddress>();
        ArrayList<HDMAddress.Pubs> uncompPubs = new ArrayList<HDMAddress.Pubs>();
        if (fetchDelegate != null) {
            List<HDMAddress.Pubs> pubs = fetchDelegate.getRemoteExistsPublicKeys(password);
            if (pubs.size() > 0) {
                try {
                    DeterministicKey root = externalChainRoot(password);
                    byte[] pubDerived = root.deriveSoftened(0).getPubKey();
                    byte[] pubFetched = pubs.get(0).hot;
                    root.wipe();
                    if (!Arrays.equals(pubDerived, pubFetched)) {
                        wipeMnemonicSeed();
                        wipeHDSeed();
                        throw new HDMBitherIdNotMatchException();
                    }
                } catch (MnemonicException.MnemonicLengthException e) {
                    wipeMnemonicSeed();
                    wipeHDSeed();
                    throw e;
                }
            }
            for (HDMAddress.Pubs p : pubs) {
                if (p.isCompleted()) {
                    as.add(new EnDesktopHDMAddress(p, this, false));
                } else {
                    uncompPubs.add(p);
                }
            }
        }
        ECKey k = new ECKey(mnemonicSeed, null);
        String address = k.toAddress();
        k.clearPrivateKey();
        String firstAddress = getFirstAddressFromSeed(password);
        wipeMnemonicSeed();
        wipeHDSeed();

        this.hdSeedId = EnDesktopProvider.getInstance().addHDKey(encryptedMnemonicSeed
                        .toEncryptedString(), encryptedHDSeed.toEncryptedString(), firstAddress,
                isFromXRandom, address, null, null);
        if (as.size() > 0) {
            EnDesktopProvider.getInstance().completeHDMAddresses(getHdSeedId(), as);
            allCompletedAddresses.addAll(as);
            if (uncompPubs.size() > 0) {
                EnDesktopProvider.getInstance().prepareHDMAddresses(getHdSeedId(), uncompPubs);
                for (HDMAddress.Pubs p : uncompPubs) {
                    AbstractDb.addressProvider.setHDMPubsRemote(getHdSeedId(), p.index, p.remote);
                }
            }
        }
    }


    public int prepareAddresses(int count, CharSequence password, byte[] coldExternalRootPub) {
        DeterministicKey externalRootHot;
        DeterministicKey externalRootCold = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                (coldExternalRootPub);

        try {
            externalRootHot = externalChainRoot(password);
            externalRootHot.clearPrivateKey();
        } catch (MnemonicException.MnemonicLengthException e) {
            return 0;
        }
        ArrayList<HDMAddress.Pubs> pubs = new ArrayList<HDMAddress.Pubs>();
        int startIndex = 0;
        int maxIndex = EnDesktopProvider.getInstance().maxHDMAddressPubIndex(getHdSeedId());
        if (maxIndex >= 0) {
            startIndex = maxIndex + 1;
        }

        if (startIndex > 0) {
            HDMBId id = HDMBId.getHDMBidFromDb();
            if (id != null) {
                String hdmIdAddress = id.getAddress();
                if (!Utils.compareString(hdmIdAddress, Utils.toAddress(externalRootCold
                        .deriveSoftened(0).getPubKeyHash()))) {
                    throw new HDMColdPubNotSameException();
                }
            }
        }

        for (int i = startIndex;
             pubs.size() < count;
             i++) {
            HDMAddress.Pubs p = new HDMAddress.Pubs();
            try {
                p.hot = externalRootHot.deriveSoftened(i).getPubKey();
            } catch (Exception e) {
                e.printStackTrace();
                p.hot = HDMAddress.Pubs.EmptyBytes;
            }
            try {
                p.cold = externalRootCold.deriveSoftened(i).getPubKey();
            } catch (Exception e) {
                e.printStackTrace();
                p.cold = HDMAddress.Pubs.EmptyBytes;
            }
            p.index = i;
            pubs.add(p);
        }
        EnDesktopProvider.getInstance().prepareHDMAddresses(getHdSeedId(), pubs);
        if (externalRootHot != null) {
            externalRootHot.wipe();
        }
        if (externalRootCold != null) {
            externalRootCold.wipe();
        }
        return pubs.size();
    }


    public List<EnDesktopHDMAddress> getAddresses() {
        synchronized (allCompletedAddresses) {
            if (addressesInUse == null) {
                addressesInUse = Collections2.filter(allCompletedAddresses,
                        new Predicate<EnDesktopHDMAddress>() {
                            @Override
                            public boolean apply(@Nullable EnDesktopHDMAddress input) {
                                return !input.isTrashed();
                            }
                        });
            }
            return new ArrayList<EnDesktopHDMAddress>(addressesInUse);
        }
    }

    public List<EnDesktopHDMAddress> getTrashedAddresses() {
        synchronized (allCompletedAddresses) {
            if (addressesTrashed == null) {
                addressesTrashed = Collections2.filter(allCompletedAddresses,
                        new Predicate<EnDesktopHDMAddress>() {
                            @Override
                            public boolean apply(@Nullable EnDesktopHDMAddress input) {
                                return input.isTrashed();
                            }
                        });
            }
            return new ArrayList<EnDesktopHDMAddress>(addressesTrashed);
        }
    }

    private DeterministicKey externalChainRoot(CharSequence password) throws MnemonicException.MnemonicLengthException {
        DeterministicKey master = masterKey(password);
        DeterministicKey accountKey = getAccount(master);
        DeterministicKey externalKey = getChainRootKey(accountKey, PathType.EXTERNAL_ROOT_PATH);
        master.wipe();
        accountKey.wipe();
        return externalKey;
    }

    public byte[] getExternalChainRootPubExtended(CharSequence password) throws MnemonicException
            .MnemonicLengthException {
        DeterministicKey ex = externalChainRoot(password);
        byte[] pub = ex.getPubKeyExtended();
        ex.wipe();
        return pub;
    }

    public String getExternalChainRootPubExtendedAsHex(CharSequence password) throws
            MnemonicException.MnemonicLengthException {
        return Utils.bytesToHexString(getExternalChainRootPubExtended(password)).toUpperCase();
    }


    public int getCurrentMaxAddressIndex() {
        synchronized (allCompletedAddresses) {
            int max = Integer.MIN_VALUE;
            for (EnDesktopHDMAddress address : allCompletedAddresses) {
                if (address.getIndex() > max) {
                    max = address.getIndex();
                }
            }
            return max;
        }
    }

    public List<EnDesktopHDMAddress> getAllCompletedAddresses() {
        synchronized (allCompletedAddresses) {
            return allCompletedAddresses;
        }
    }

    private void initFromDb() {
        isFromXRandom = EnDesktopProvider.getInstance().isHDSeedFromXRandom(getHdSeedId());
        initAddressesFromDb();
    }

    private void initAddressesFromDb() {
        synchronized (allCompletedAddresses) {
            List<EnDesktopHDMAddress> addrs = EnDesktopProvider.getInstance().getHDMAddressInUse(this);
            if (addrs != null) {
                allCompletedAddresses.addAll(addrs);
            }
        }
    }


    public HDMAddressChangeDelegate getAddressChangeDelegate() {
        return addressChangeDelegate;
    }

    public void setAddressChangeDelegate(HDMAddressChangeDelegate addressChangeDelegate) {
        this.addressChangeDelegate = addressChangeDelegate;
    }

    public boolean isFromXRandom() {
        return isFromXRandom;
    }


    public String getFullEncryptPrivKey() {
        String encryptPrivKey = getEncryptedMnemonicSeed();
        return PrivateKeyUtil.getFullencryptHDMKeyChain(isFromXRandom, encryptPrivKey);
    }

    public String getQRCodeFullEncryptPrivKey() {
        return QRCodeUtil.HDM_QR_CODE_FLAG
                + getFullEncryptPrivKey();
    }

    @Override
    protected String getEncryptedHDSeed() {

        String encrypted = EnDesktopProvider.getInstance().getEncryptHDSeed(hdSeedId);
        if (encrypted == null) {
            return null;
        }
        return encrypted.toUpperCase();
    }

    @Override
    public String getEncryptedMnemonicSeed() {

        return EnDesktopProvider.getInstance().getEncryptMnemonicSeed(hdSeedId).toUpperCase();
    }

    public String getFirstAddressFromDb() {
        return EnDesktopProvider.getInstance().getHDMFristAddress(hdSeedId);
    }

    public boolean checkWithPassword(CharSequence password) {

        try {
            decryptHDSeed(password);
            decryptMnemonicSeed(password);
            byte[] hdCopy = Arrays.copyOf(hdSeed, hdSeed.length);
            boolean hdSeedSafe = Utils.compareString(getFirstAddressFromDb(),
                    getFirstAddressFromSeed(null));
            boolean mnemonicSeedSafe = Arrays.equals(seedFromMnemonic(mnemonicSeed), hdCopy);
            Utils.wipeBytes(hdCopy);
            wipeHDSeed();
            wipeMnemonicSeed();
            return hdSeedSafe && mnemonicSeedSafe;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void getRemotePublicKeys(HDMBId hdmBId, CharSequence password,
                                           List<HDMAddress.Pubs> partialPubs) throws Exception {
        byte[] decryptedPassword = hdmBId.decryptHDMBIdPassword(password);
        CreateHDMAddressApi createHDMAddressApi = new CreateHDMAddressApi(hdmBId.getAddress(),
                partialPubs, decryptedPassword);
        createHDMAddressApi.handleHttpPost();
        List<byte[]> remotePubs = createHDMAddressApi.getResult();
        for (int i = 0;
             i < partialPubs.size();
             i++) {
            HDMAddress.Pubs pubs = partialPubs.get(i);
            pubs.remote = remotePubs.get(i);
        }
    }

    public static final class HDMColdPubNotSameException extends RuntimeException {

    }

    public static final class HDMBitherIdNotMatchException extends RuntimeException {
        public static final String msg = "HDM Bid Not Match";

        public HDMBitherIdNotMatchException() {
            super(msg);
        }
    }

    public static boolean checkPassword(String keysString, CharSequence password) throws
            MnemonicException.MnemonicLengthException {
        String[] passwordSeeds = QRCodeUtil.splitOfPasswordSeed(keysString);
        String address = Base58.hexToBase58WithAddress(passwordSeeds[0]);
        String encreyptString = Utils.joinString(new String[]{passwordSeeds[1], passwordSeeds[2],
                passwordSeeds[3]}, QRCodeUtil.QR_CODE_SPLIT);
        byte[] seed = new EncryptedData(encreyptString).decrypt(password);
        MnemonicCode mnemonic = MnemonicCode.instance();

        byte[] s = mnemonic.toSeed(mnemonic.toMnemonic(seed), "");

        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(s);

        DeterministicKey purpose = master.deriveHardened(44);

        DeterministicKey coinType = purpose.deriveHardened(0);

        DeterministicKey account = coinType.deriveHardened(0);

        DeterministicKey external = account.deriveSoftened(0);

        external.clearPrivateKey();

        DeterministicKey key = external.deriveSoftened(0);
        boolean result = Utils.compareString(address, Utils.toAddress(key.getPubKeyHash()));
        key.wipe();

        return result;
    }


}
