package org.jbake.app

import org.jbake.util.DebugUtil.printMap
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

class DebugUtilTest {
    @Test
    @Throws(UnsupportedEncodingException::class)
    fun printMap() {
        val baos = ByteArrayOutputStream()
        PrintStream(baos, true, "UTF-8").use { ps ->
            val map = HashMap<String?, Any?>()
            map.put("stringKey", "stringVal")
            map.put("forNullVal", null)
            map.put(null, "forNullKey")
            map.put("forObject", Any())
            map.put("forCharset", StandardCharsets.UTF_8)
            map.put("forNonSerializableVal", Exception("nonSerializableVal"))
            printMap<Any?>(map, ps)
        }
        val printed = String(baos.toByteArray(), StandardCharsets.UTF_8)
        println(printed)

        Assert.assertTrue(printed.contains("stringKey :: stringVal"))
        Assert.assertTrue(printed.contains("null :: forNullKey"))
        Assert.assertTrue(printed.contains("forNullVal :: null"))
        Assert.assertTrue(printed.contains("forCharset :: UTF-8"))
        Assert.assertTrue(printed.contains("forNonSerializableVal :: java.lang.Exception: nonSerializableVal"))
    }
}
