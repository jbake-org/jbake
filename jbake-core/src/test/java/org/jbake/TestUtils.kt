package org.jbake

import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.io.IOException
import java.nio.file.Path

object TestUtils {

    /**
     * Hides the assets on Windows that start with a dot (e.g. .test.txt but not test.txt) so File.isHidden() returns true for those files.
     */
    @Throws(IOException::class, InterruptedException::class)
    fun hideAssets(assets: File) {
        if (!isWindows) return
        val hiddenFiles = assets.listFiles { dir, name -> name.startsWith(".") }
        for (file in hiddenFiles) {
            Runtime.getRuntime().exec(arrayOf("attrib", "+h", file.absolutePath)).waitFor()
        }
    }

    val isWindows: Boolean
        get() = SystemUtils.IS_OS_WINDOWS

    val testResourcesAsSourceDir: File
        get() = getTestResourcesAsSourceDir("/fixture")

    fun getTestResourcesAsSourceDir(name: String): File {
        val resource = TestUtils::class.java.getResource(name) ?: throw IllegalArgumentException("Resource not found: $name")
        return File(resource.file)
    }

    fun newDir(base: File?, folderName: String): File {
        val templateDir = File(base, folderName)
        templateDir.mkdir()
        return templateDir
    }

    fun escapeBackSlashes(path: Path)
        = path.toString().let {
            if (!isWindows) it else it.replace("""\""", """\\""")
        }
}
