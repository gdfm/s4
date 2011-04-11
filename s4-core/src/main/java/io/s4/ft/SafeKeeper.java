package io.s4.ft;

import io.s4.dispatcher.Dispatcher;
import io.s4.processor.AbstractPE;
import io.s4.serialize.SerializerDeserializer;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

public class SafeKeeper {

    public enum StorageResultCode {
        SUCCESS, FAILURE
    }

    private static Logger LOG = Logger.getLogger(SafeKeeper.class);
    private ConcurrentMap<SafeKeeperId, byte[]> serializedStateCache = new ConcurrentHashMap<SafeKeeperId, byte[]>(
            16, 0.75f, 2);
    private Set<SafeKeeperId> keysToRecover = Collections
            .newSetFromMap(new ConcurrentHashMap<SafeKeeperId, Boolean>(16,
                    0.75f, 2));
    private Thread eagerFetchingThread;
    private StateStorage stateStorage;
    private KeyStorage keyStorage;
    private NamingSchema namingSchema;
    private Dispatcher loopbackDispatcher;
    private SerializerDeserializer serializer;

    public SafeKeeper() {
    }

    public void init() {
        // start background eager fetching thread (TODO: iff eager fetching)
        eagerFetchingThread = new Thread(new EagerSerializedStateFetcher(this),
                "EagerSerializedStateLoader");
        eagerFetchingThread.start();
    }

    public void saveState(SafeKeeperId key, byte[] state) {
        String keyString = namingSchema.getStringRepresentation(key);
        keyStorage.addKey(keyString);
        stateStorage.saveState(keyString, state, new StorageCallBackLogger());
    }

    public byte[] fetchSerializedState(SafeKeeperId key) {
        byte[] result = null;
        String keyString = namingSchema.getStringRepresentation(key);
        if (serializedStateCache.containsKey(key)) {
            result = serializedStateCache.remove(key);
        } else {
            result = stateStorage.fetchState(keyString);
        }
        keysToRecover.remove(key);
        return result;
    }

    // TODO externalize
    private static class StorageCallBackLogger implements StorageCallback {
        @Override
        public void storageOperationResult(StorageResultCode code,
                Object message) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Callback from storage: " + message);
            }
        }
    }

    public void invalidateStateCacheEntry(SafeKeeperId safeKeeperId) {
        serializedStateCache.remove(safeKeeperId);
    }

    public void generateCheckpoint(AbstractPE pe) {
        // generate event
        InitiateCheckpointingEvent initiateCheckpointingEvent = new InitiateCheckpointingEvent(
                pe.getSafeKeeperId());

        // inject event
        loopbackDispatcher.dispatchEvent(pe.getStreamName(),
                initiateCheckpointingEvent);
    }

    public void initiateRecovery(SafeKeeperId safeKeeperId) {
        RecoveryEvent recoveryEvent = new RecoveryEvent(safeKeeperId);
        loopbackDispatcher.dispatchEvent(safeKeeperId.getStreamName(),
                recoveryEvent);
    }

    public void setStateStorage(StateStorage stateStorage) {
        this.stateStorage = stateStorage;
    }

    public StateStorage getStateStorage() {
        return stateStorage;
    }

    public void setKeyStorage(KeyStorage keyStorage) {
        this.keyStorage = keyStorage;
    }

    public KeyStorage getKeyStorage() {
        return keyStorage;
    }

    public void setNamingSchema(NamingSchema namingSchema) {
        this.namingSchema = namingSchema;
    }

    public NamingSchema getNamingSchema() {
        return namingSchema;
    }

    public void setLoopbackDispatcher(Dispatcher dispatcher) {
        this.loopbackDispatcher = dispatcher;
    }

    public void cacheSerializedState(SafeKeeperId safeKeeperId, byte[] state) {
        serializedStateCache.putIfAbsent(safeKeeperId, state);
    }

    public boolean isCached(SafeKeeperId safeKeeperId) {
        return serializedStateCache.containsKey(safeKeeperId);
    }

    public Set<SafeKeeperId> getKeysToRecover() {
        return keysToRecover;
    }

    public void setSerializer(SerializerDeserializer serializer) {
        this.serializer = serializer;
    }

    public SerializerDeserializer getSerializer() {
        return serializer;
    }

}
