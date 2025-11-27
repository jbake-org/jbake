package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import org.jbake.util.DebugUtil.printMap
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets

class DebugUtilTest : StringSpec({
    "printMap" {
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

        printed shouldContain "stringKey :: stringVal"
        printed shouldContain "forNullVal :: null"
        printed shouldContain "forCharset :: UTF-8"
        printed shouldContain "forNonSerializableVal :: java.lang.Exception: nonSerializableVal"
    }
})
