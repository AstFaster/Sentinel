package fr.astfaster.sentinel.proxy.network.protocol;

public class Location {

    private final String dimension;
    private final long position;

    public Location(String dimension, long position) {
        this.dimension = dimension;
        this.position = position;
    }

    public String dimension() {
        return this.dimension;
    }

    public long position() {
        return this.position;
    }

}
