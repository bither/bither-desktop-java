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

package net.bither.db;

import net.bither.ApplicationInstanceManager;
import net.bither.bitherj.core.*;
import net.bither.bitherj.db.IColdHDAccountProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ColdHDAccountProvider implements IColdHDAccountProvider {

    private static ColdHDAccountProvider coldHDAccountProvider =
            new ColdHDAccountProvider(ApplicationInstanceManager.txDBHelper);

    public static ColdHDAccountProvider getInstance() {
        return coldHDAccountProvider;
    }

    private TxDBHelper mDb;


    private ColdHDAccountProvider(TxDBHelper db) {
        this.mDb = db;
    }

    @Override
    public void addAddress(List<HDAccount.HDAccountAddress> hdAccountAddresses) {

    }

    @Override
    public int issuedIndex(AbstractHD.PathType pathType) {
        return 0;
    }

    @Override
    public int allGeneratedAddressCount(AbstractHD.PathType pathType) {
        return 0;
    }

    @Override
    public void updateIssuedIndex(AbstractHD.PathType pathType, int index) {

    }

    @Override
    public String externalAddress() {
        return null;
    }

    @Override
    public HashSet<String> getBelongAccountAddresses(List<String> addressList) {
        return new HashSet<String>();
    }

    @Override
    public HDAccount.HDAccountAddress addressForPath(AbstractHD.PathType type, int index) {
        return null;
    }

    @Override
    public List<byte[]> getPubs(AbstractHD.PathType pathType) {
        return new ArrayList<byte[]>();
    }

    @Override
    public List<HDAccount.HDAccountAddress> belongAccount(List<String> addresses) {
        return new ArrayList<HDAccount.HDAccountAddress>();
    }

    @Override
    public void updateSyncdComplete(HDAccount.HDAccountAddress address) {

    }

    @Override
    public void setSyncdNotComplete() {

    }

    @Override
    public int unSyncedAddressCount() {
        return 0;
    }

    @Override
    public void updateSyncdForIndex(AbstractHD.PathType pathType, int index) {

    }

    @Override
    public List<HDAccount.HDAccountAddress> getSigningAddressesForInputs(List<In> inList) {
        return new ArrayList<HDAccount.HDAccountAddress>();
    }

    @Override
    public int hdAccountTxCount() {
        return 0;
    }

    @Override
    public long getHDAccountConfirmedBanlance(int hdAccountId) {
        return 0;
    }

    @Override
    public List<Tx> getHDAccountUnconfirmedTx() {
        return new ArrayList<Tx>();
    }

    @Override
    public long sentFromAccount(int hdAccountId, byte[] txHash) {
        return 0;
    }

    @Override
    public List<Tx> getTxAndDetailByHDAccount(int page) {
        return new ArrayList<Tx>();
    }

    @Override
    public List<Tx> getTxAndDetailByHDAccount() {
        return new ArrayList<Tx>();
    }

    @Override
    public List<Out> getUnspendOutByHDAccount(int hdAccountId) {
        return new ArrayList<Out>();
    }

    @Override
    public List<Tx> getRecentlyTxsByAccount(int greateThanBlockNo, int limit) {
        return new ArrayList<Tx>();
    }

    @Override
    public int getUnspendOutCountByHDAccountWithPath(int hdAccountId, AbstractHD.PathType pathType) {
        return 0;
    }

    @Override
    public List<Out> getUnspendOutByHDAccountWithPath(int hdAccountId, AbstractHD.PathType pathType) {
        return new ArrayList<Out>();
    }
}
