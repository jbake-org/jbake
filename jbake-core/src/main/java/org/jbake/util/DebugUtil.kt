package org.jbake.util

import java.io.PrintStream

object DebugUtil {
    fun <T : Any?> printMap(map: MutableMap<String?, T?>, printStream: PrintStream) {
        printStream.println()
        for (entry in map.entries) {
            printStream.println(entry.key + " :: " + entry.value)
        }
        printStream.println()
    }
}
