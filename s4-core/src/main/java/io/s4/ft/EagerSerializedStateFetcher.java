package io.s4.ft;

import java.util.Set;

import org.apache.log4j.Logger;

public class EagerSerializedStateFetcher implements Runnable {

    private static final int TOKEN_COUNT = Integer.valueOf(System.getProperty(
            "s4.ft.fetcher.token.count", "5"));
    private static final int TOKEN_TIME_MS = Integer.valueOf(System
            .getProperty("s4.ft.fetcher.token.time", "50"));
    SafeKeeper sk;

    private static Logger LOG = Logger
            .getLogger(EagerSerializedStateFetcher.class);

    public EagerSerializedStateFetcher(SafeKeeper sk) {
        this.sk = sk;
    }

    @Override
    public void run() {
        // FIXME log
        System.out.println("STARTING EAGER FETCHING THREAD");
        Set<String> storedKeyStrings = sk.getKeyStorage().fetchStoredKeys();
        NamingSchema ns = sk.getNamingSchema();
        for (String keyString : storedKeyStrings) {
            // TODO validate ids through hash function?
            String streamName = ns.getStreamName(keyString);
            String prototypeID = ns.getPrototypeId(keyString);
            String className = ns.getClassName(keyString);
            String key = ns.getKey(keyString);
            sk.getKeysToRecover().add(new SafeKeeperId(streamName, prototypeID, className, key));
        }

        long startTime = System.currentTimeMillis();
        int tokenCount = TOKEN_COUNT;
        
        for (SafeKeeperId safeKeeperId : sk.getKeysToRecover()) {

            if (tokenCount == 0) {
                if ((System.currentTimeMillis() - startTime) < (TOKEN_COUNT * TOKEN_TIME_MS)) {
                    try {
                        Thread.sleep(TOKEN_COUNT * TOKEN_TIME_MS
                                - (System.currentTimeMillis() - startTime));
                    } catch (InterruptedException e) {
                        LOG.error(e);
                    }
                }
                tokenCount = TOKEN_COUNT;
                startTime = System.currentTimeMillis();
            }

            if (sk.getKeysToRecover().contains(safeKeeperId)) {
                if (!sk.isCached(safeKeeperId)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Fetching state for id: " + safeKeeperId);
                    }

                    byte[] state = sk.fetchSerializedState(safeKeeperId);
                    if (state != null) {
                        sk.cacheSerializedState(safeKeeperId, state);
                        // send an event to recover
                        sk.initiateRecovery(safeKeeperId);
                    }
                    tokenCount--;
                }
            }

        }

    }

}
