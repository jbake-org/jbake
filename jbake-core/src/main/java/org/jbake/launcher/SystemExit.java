package org.jbake.launcher;

public enum SystemExit {
    SUCCESS,
    ERROR,
    CONFIGURATION_ERROR,
    INIT_ERROR,
    SERVER_ERROR;

    public int getStatus() {
        return this.ordinal();
    }
}
