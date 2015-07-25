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
import net.bither.bitherj.db.imp.AbstractBlockProvider;
import net.bither.bitherj.db.imp.base.IDb;
import net.bither.db.base.JavaDb;

public class Block2Provider extends AbstractBlockProvider {

    private static Block2Provider blockProvider = new Block2Provider(ApplicationInstanceManager.txDBHelper);

    public static Block2Provider getInstance() {
        return blockProvider;
    }

    private TxDBHelper helper;

    public Block2Provider(TxDBHelper helper) {
        this.helper = helper;
    }

    @Override
    public IDb getReadDb() {
        return new JavaDb(this.helper.getConn());
    }

    @Override
    public IDb getWriteDb() {
        return new JavaDb(this.helper.getConn());
    }
}
