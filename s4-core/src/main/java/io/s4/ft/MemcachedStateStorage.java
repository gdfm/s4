/*
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License. See accompanying LICENSE file. 
 */

package io.s4.ft;

import java.util.Set;

public class MemcachedStateStorage implements StateStorage {

    @Override
    public void saveState(SafeKeeperId key, byte[] state,
            StorageCallback callback) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte[] fetchState(SafeKeeperId key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<SafeKeeperId> fetchStoredKeys() {
        // TODO Auto-generated method stub
        return null;
    }

}