package org.jbake.app;

import java.io.PrintStream;
import java.sql.SQLOutput;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class DebugUtil
{
    public static <T extends Object> void printMap(String label, Map<String, T> map, PrintStream printStream){
        if (null == map) {
            printStream.println("The Map is null.");
            return;
        }

        if (null == printStream) {
            printStream = System.out;
            return;
        }
        if (!StringUtils.isBlank(label)) {
            printStream.println(label + ":");
        }

        printStream.println();
        for (Map.Entry<String, T> entry: map.entrySet()) {
            printStream.println(entry.getKey() + " :: " + entry.getValue());
        }
        printStream.println();
    }
}
