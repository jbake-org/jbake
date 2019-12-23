package org.jbake.app;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.jbake.util.DebugUtil;
import org.junit.Assert;
import org.junit.Test;

public class DebugUtilTest
{

    @Test
    public void printMap() throws UnsupportedEncodingException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("stringKey", "stringVal");
            map.put("forNullVal", null);
            map.put(null, "forNullKey");
            map.put("forObject", new Object());
            map.put("forCharset", StandardCharsets.UTF_8);
            map.put("forNonSerializableVal", new Exception("nonSerializableVal"));
            DebugUtil.printMap(map, ps);
        }
        String printed = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        System.out.println(printed);

        Assert.assertTrue(printed.contains("stringKey :: stringVal"));
        Assert.assertTrue(printed.contains("null :: forNullKey"));
        Assert.assertTrue(printed.contains("forNullVal :: null"));
        Assert.assertTrue(printed.contains("forCharset :: UTF-8"));
        Assert.assertTrue(printed.contains("forNonSerializableVal :: java.lang.Exception: nonSerializableVal"));
    }
}
