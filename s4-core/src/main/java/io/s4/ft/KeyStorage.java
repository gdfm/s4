package io.s4.ft;

import java.util.Set;

public interface KeyStorage {

    // adds a key to the storage
    public void addKey(String key);
    
    // returns empty if no stored key
    public Set<String> fetchStoredKeys();
}
