/**
 * Copyright 2013 multibit.org
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

package net.bither.network;


import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.PeerManager;
import net.bither.bitherj.exception.BlockStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


public enum ReplayManager {
    INSTANCE;

    private static final Logger log = LoggerFactory.getLogger(ReplayManager.class);
    private ReplayManagerTimerTask replayManagerTimerTask;
    private Timer replayManagerTimer;

    private static final int REPLAY_MANAGER_DELAY_TIME = 0; // ms
    private static final int REPLAY_MANAGER_REPEAT_TIME = 333; // ms

    private final Queue<ReplayTask> replayTaskQueue = new LinkedList<ReplayTask>();


    public void initialise(boolean clearQueue) {


        if (clearQueue) {
            replayTaskQueue.clear();
        }
        replayManagerTimerTask = new ReplayManagerTimerTask(replayTaskQueue);
        replayManagerTimer = new Timer();
        replayManagerTimer.scheduleAtFixedRate(replayManagerTimerTask, REPLAY_MANAGER_DELAY_TIME, REPLAY_MANAGER_REPEAT_TIME);
    }

    /**
     * Synchronise one or more wallets with the blockchain.
     */
    public void syncWallet(final ReplayTask replayTask) throws IOException,
            BlockStoreException {
        log.info("Starting replay task : " + replayTask.toString());


        // Mark the wallets as busy and set the replay task uuid into the model
        List<Address> perWalletModelDataList = replayTask.getPerWalletModelDataToReplay();
        if (perWalletModelDataList != null) {
            for (Address perWalletModelData : perWalletModelDataList) {
//                perWalletModelData.setBusy(true);
//                perWalletModelData.setBusyTaskKey("multiBitDownloadListener.downloadingText");
//                perWalletModelData.setBusyTaskVerbKey("multiBitDownloadListener.downloadingTextShort");
//                perWalletModelData.setReplayTaskUUID(replayTask.getUuid());
            }

        }

        Date dateToReplayFrom = replayTask.getStartDate();


        log.debug("Starting replay of blockchain from date = '" + dateToReplayFrom);

        // Reset UI to zero peers.
        // controller.getPeerEventListener().onPeerDisconnected(null, 0);

        // Restart peerGroup and download rest of blockchain.

        // controller.getMultiBitService().getPeerGroup().stopAndWait();
        log.debug("PeerGroup is now stopped.");

        // Reset UI to zero peers.
        //  controller.getPeerEventListener().onPeerDisconnected(null, 0);

        // Close the blockstore and recreate a new one.
//        int newChainHeightAfterTruncate = controller.getMultiBitService().createNewBlockStoreForReplay(dateToReplayFrom);
//        log.debug("dateToReplayFrom = " + dateToReplayFrom + ", newChainHeightAfterTruncate = " + newChainHeightAfterTruncate);
//        replayTask.setStartHeight(newChainHeightAfterTruncate);

        // Create a new PeerGroup.
        //  controller.getMultiBitService().createNewPeerGroup();
        log.debug("Recreated PeerGroup.");

        // Hook up the download listeners.


        // Start up the PeerGroup.
        if (!PeerManager.instance().isConnected())
            PeerManager.instance().start();

        log.debug("About to start  blockchain download.");
        //  controller.getMultiBitService().getPeerGroup().downloadBlockChain();
        log.debug("Blockchain download started.");
    }


    /**
     * See if there is a waiting replay task for a perWalletModelData
     *
     * @param perWalletModelData
     * @return the waiting ReplayTask or null if there is not one.
     */
    @SuppressWarnings("unchecked")
    public ReplayTask getWaitingReplayTask(Address perWalletModelData) {
        synchronized (replayTaskQueue) {
            if (replayTaskQueue.isEmpty()) {
                return null;
            } else {
                for (ReplayTask replayTask : (List<ReplayTask>) replayTaskQueue) {
                    List<Address> list = replayTask.getPerWalletModelDataToReplay();
                    if (list != null) {
                        for (Address item : list) {
//                            if (perWalletModelData.getWalletFilename().equals(item.getWalletFilename())) {
//                                return replayTask;
//                            }
                        }
                    }

                }
                return null;
            }
        }
    }


}