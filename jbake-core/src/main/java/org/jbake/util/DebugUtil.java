package org.jbake.util;

import java.io.PrintStream;
import java.util.Map;

public class DebugUtil {
    public static <T extends Object> void printMap(Map<String, T> map, PrintStream printStream) {
        printStream.println();
        for (Map.Entry<String, T> entry: map.entrySet()) {
            printStream.println(entry.getKey() + " :: " + entry.getValue());
        }
        printStream.println();
    }
}
