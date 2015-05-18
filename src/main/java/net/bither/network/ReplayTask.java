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

package net.bither.network;

import net.bither.bitherj.core.Address;
import net.bither.message.Message;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A class encapsulating a request on one or more wallets to perform a
 * blockchain replay
 */
public class ReplayTask {

    public static final int UNKNOWN_START_HEIGHT = -1;

    private final List<Address> perWalletModelDataToReplay;

    /**
     * The start date of the replay task.
     */
    private final Date startDate;

    /**
     * The height the blockchain needs to be truncated to.
     */
    private int startHeight;

    /**
     * A UUID identifying this replay task.
     */
    private final UUID uuid;

    /**
     * The percent complete as reported by the downloadlistener.
     */
    private long percentComplete;

    public ReplayTask(List<Address> perWalletModelDataToReplay, Date startDate, int startHeight) {
        this.perWalletModelDataToReplay = perWalletModelDataToReplay;
        this.startDate = startDate;
        this.startHeight = startHeight;
        this.percentComplete = Message.NOT_RELEVANT_PERCENTAGE_COMPLETE;
        this.uuid = UUID.randomUUID();
    }

    public List<Address> getPerWalletModelDataToReplay() {
        return perWalletModelDataToReplay;
    }

    public Date getStartDate() {
        return startDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((perWalletModelDataToReplay == null) ? 0 : perWalletModelDataToReplay.hashCode());
        result = prime * result + (int) (percentComplete ^ (percentComplete >>> 32));
        result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
        result = prime * result + startHeight;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ReplayTask))
            return false;
        ReplayTask other = (ReplayTask) obj;
        if (perWalletModelDataToReplay == null) {
            if (other.perWalletModelDataToReplay != null)
                return false;
        } else if (!perWalletModelDataToReplay.equals(other.perWalletModelDataToReplay))
            return false;
        if (percentComplete != other.percentComplete)
            return false;
        if (startDate == null) {
            if (other.startDate != null)
                return false;
        } else if (!startDate.equals(other.startDate))
            return false;
        if (startHeight != other.startHeight)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ReplayTask [perWalletModelDataToReplay=" + perWalletModelDataToReplay + ", startDate=" + startDate
                + ", startHeight=" + startHeight + ", uuid=" + uuid
                + ", percentComplete=" + percentComplete + "]";
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(long percentComplete) {
        this.percentComplete = percentComplete;
    }

    public int getStartHeight() {
        return startHeight;
    }

    public void setStartHeight(int startHeight) {
        this.startHeight = startHeight;
    }
}
