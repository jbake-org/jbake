package org.jbake.render

import org.junit.Assert
import org.junit.Test
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

class ServiceLoaderTest {
    @Test
    fun testLoadRenderer() {
        val serviceDescription =
            ClassLoader.getSystemClassLoader().getResource("META-INF/services/org.jbake.render.RenderingTool")
        val services = File(serviceDescription!!.toURI())
        Assert.assertTrue("Service definitions File exists", services.exists())

        val fileReader = FileReader(services)
        val reader = BufferedReader(fileReader)

        var serviceProvider: String?
        val renderingToolClasses: MutableList<String> = ArrayList<String>()

        for (tool in ServiceLoader.load(RenderingTool::class.java)) {
            renderingToolClasses.add(tool.javaClass.getName())
        }

        while ((reader.readLine().also { serviceProvider = it }) != null) {
            Assert.assertTrue(
                "Rendering tool $serviceProvider loaded",
                renderingToolClasses.contains(serviceProvider)
            )
        }
    }
}
