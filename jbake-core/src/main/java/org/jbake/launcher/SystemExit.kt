package org.jbake.launcher

enum class SystemExit {
    SUCCESS,
    ERROR,
    CONFIGURATION_ERROR,
    INIT_ERROR,
    SERVER_ERROR;

    val status: Int
        get() = this.ordinal
}
