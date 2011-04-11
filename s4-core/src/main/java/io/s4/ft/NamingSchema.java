package io.s4.ft;

public interface NamingSchema {

    public String getKey(String key);

    public String getClassName(String key);

    public String getStreamName(String key);

    public String getPrototypeId(String key);
    
    public String getStringRepresentation(SafeKeeperId PEid);
}
