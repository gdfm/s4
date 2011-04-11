package io.s4.ft;

import java.io.File;

import org.apache.commons.codec.binary.Base64;

public class FileSystemNamingSchema implements NamingSchema {
    private static final String SEPARATOR = "\t";

    @Override
    public String getKey(String key) {
        return this.extractionHelper(key, 3);
    }

    @Override
    public String getClassName(String key) {
        return this.extractionHelper(key, 2);
    }

    @Override
    public String getPrototypeId(String key) {
        return this.extractionHelper(key, 1);
    }

    @Override
    public String getStreamName(String key) {
        return this.extractionHelper(key, 0);
    }

    @Override
    public String getStringRepresentation(SafeKeeperId PEid) {
        StringBuilder builder = new StringBuilder();
        builder.append(PEid.getStreamName() == null ? "" : PEid.getStreamName());
        builder.append(SEPARATOR);
        builder.append(PEid.getPrototypeId() == null ? "" : PEid.getPrototypeId());
        builder.append(SEPARATOR);
        builder.append(PEid.getClassName() == null ? "" : PEid.getClassName());
        builder.append(SEPARATOR);
        builder.append(PEid.getKey() == null ? "" : PEid.getKey());
        String result = PEid.getPrototypeId() + File.separator
                + Base64.encodeBase64URLSafeString(builder.toString().getBytes());
        return result;
    }

    private String extractionHelper(String key, int pos) {
        String encoded;
        try {
            encoded = key.split(File.separator)[1];
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Unexpected key format: " + key);
        }
        String decoded = new String(Base64.decodeBase64(encoded));
        String[] elements = decoded.split(SEPARATOR);
        if (elements.length != 4)
            throw new IllegalArgumentException("Unexpected decoded key format: " + decoded);
        return elements[pos];
    }
}
