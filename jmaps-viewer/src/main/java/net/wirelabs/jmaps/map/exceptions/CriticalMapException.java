package net.wirelabs.jmaps.map.exceptions;

/**
 * Exception for all situations where map cannot be parsed or displayed
 */
public class CriticalMapException extends RuntimeException {

    public CriticalMapException(String s, Exception e) {
        super(s,e);
    }
    public CriticalMapException(String s) {
        super(s);
    }
}
