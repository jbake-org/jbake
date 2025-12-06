package org.jbake.util

import java.io.File

object PathUtils {

    val SYSPROP_USER_DIR: String
        get() = System.getProperty("user.dir")

    /**
     * Return the directory path string ensuring it ends with a platform separator.
     * Template engines expect a directory-like prefix; centralizing this avoids scattered concatenations.
     */
    fun ensureTrailingSeparatorForDirectory(directory: File)
        = directory.path.trimEnd(File.separatorChar) + File.separator
}

inline fun <reified T : kotlin.Enum<T>> enumValueOf(type: String): T? {
    return runCatching { java.lang.Enum.valueOf(T::class.java, type)  }.getOrNull()
}
