package org.jbake.app

import org.jbake.util.DebugUtil.printMap
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

class DebugUtilTest {
    @Test
    @Throws(UnsupportedEncodingException::class)
    fun printMap() {
        val map = HashMap<String, Any?>()
        map["stringKey"] = "stringVal"
        map["forNullVal"] = null
        map["forObject"] = Any()
        map["forCharset"] = StandardCharsets.UTF_8
        map["forNonSerializableVal"] = Exception("nonSerializableVal")

        val baos = ByteArrayOutputStream()
        PrintStream(baos, true, "UTF-8").use { ps ->
            printMap<Any?>(map, ps)
        }
        val printed = String(baos.toByteArray(), StandardCharsets.UTF_8)
        println(printed)

        assertTrue(printed.contains("stringKey :: stringVal"))
        /// Removed, as I see no reason to support null keys.
        //Assert.assertTrue(printed.contains("null :: forNullKey"))
        assertTrue(printed.contains("forNullVal :: null"))
        assertTrue(printed.contains("forCharset :: UTF-8"))
        assertTrue(printed.contains("forNonSerializableVal :: java.lang.Exception: nonSerializableVal"))
    }
}
