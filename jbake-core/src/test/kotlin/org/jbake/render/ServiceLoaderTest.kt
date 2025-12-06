package org.jbake.render

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

class ServiceLoaderTest : StringSpec({

    "testLoadRenderer" {
        val serviceDescription =
            ClassLoader.getSystemClassLoader().getResource("META-INF/services/org.jbake.render.RenderingTool")
        val services = File(serviceDescription!!.toURI())
        services.exists() shouldBe true

        val fileReader = FileReader(services)
        val reader = BufferedReader(fileReader)

        var serviceProvider: String?
        val renderingToolClasses: MutableList<String> = ArrayList<String>()

        for (tool in ServiceLoader.load(RenderingTool::class.java)) {
            renderingToolClasses.add(tool.javaClass.getName())
        }

        while ((reader.readLine().also { serviceProvider = it }) != null) {
            renderingToolClasses.contains(serviceProvider) shouldBe true
        }
    }
})
