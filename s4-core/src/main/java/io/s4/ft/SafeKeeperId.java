package io.s4.ft;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SafeKeeperId {

    // TODO add field for taskId
    private String streamName;
    private String prototypeId;
    private String className;
    private String key;

    private static final Pattern STRING_REPRESENTATION_PATTERN = Pattern
            .compile("\\[(\\S*)\\];\\[(\\S*)\\];\\[(\\S*)\\];\\[(\\S*)\\]");

    public SafeKeeperId() {
    }

    public SafeKeeperId(String streamName, String prototypeID,
            String className, String key) {
        super();
        this.streamName = streamName;
        this.prototypeId = prototypeID;
        this.className = className;
        this.key = key;
    }

    public SafeKeeperId(String keyAsString) {
        Matcher matcher = STRING_REPRESENTATION_PATTERN.matcher(keyAsString);

        try {
            matcher.find();
            streamName = "".equals(matcher.group(1)) ? null : matcher.group(1);
            prototypeId = "".equals(matcher.group(2)) ? null : matcher.group(2);
            className = "".equals(matcher.group(3)) ? null : matcher.group(3);
            key = "".equals(matcher.group(4)) ? null : matcher.group(4);
        } catch (IndexOutOfBoundsException e) {
            // FIXME logger
            System.err.println(e);
        }

    }

    public String getKey() {
        return key;
    }

    public String getClassName() {
        return className;
    }

    public String getStreamName() {
        return streamName;
    }

    public String getPrototypeId() {
        return prototypeId;
    }

    public String toString() {
        return "[STREAM];[PROTO_ID];[CLASS];[KEY] --> "
                + getStringRepresentation();
    }

    private String getStringRepresentation() {
        return "[" + (streamName == null ? "" : streamName) + "];["
                + (prototypeId == null ? "" : prototypeId) + "];["
                + (className == null ? "" : className) + "];["
                + (key == null ? "" : key) + "]";
    }

    @Override
    public int hashCode() {
        return getStringRepresentation().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // FIXME arrange this stuff
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        SafeKeeperId other = (SafeKeeperId) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (prototypeId == null) {
            if (other.prototypeId != null)
                return false;
        } else if (!prototypeId.equals(other.prototypeId))
            return false;
        if (streamName == null) {
            if (other.streamName != null)
                return false;
        } else if (!streamName.equals(other.streamName))
            return false;
        return true;
    }

}
