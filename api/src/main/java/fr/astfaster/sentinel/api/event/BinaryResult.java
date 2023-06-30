package fr.astfaster.sentinel.api.event;

public class BinaryResult {

    private final boolean allowed;

    private BinaryResult(boolean allowed) {
        this.allowed = allowed;
    }

    public boolean get() {
        return this.allowed;
    }

    public static BinaryResult allowed() {
        return new BinaryResult(true);
    }

    public static BinaryResult disallowed() {
        return new BinaryResult(false);
    }

}
