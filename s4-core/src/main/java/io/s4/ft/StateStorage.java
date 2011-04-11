package io.s4.ft;

public interface StateStorage {

    public void saveState(String key, byte[] state, StorageCallback callback);

    public byte[] fetchState(String key);
}
