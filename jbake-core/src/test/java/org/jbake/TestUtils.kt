package org.jbake

import org.apache.commons.vfs2.util.Os
import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.nio.file.Path

object TestUtils {
    /**
     * Hides the assets on Windows that start with a dot (e.g. .test.txt but not test.txt) so File.isHidden() returns true for those files.
     */
    @Throws(IOException::class, InterruptedException::class)
    fun hideAssets(assets: File) {
        if (isWindows) {
            val hiddenFiles = assets.listFiles(object : FilenameFilter {
                override fun accept(dir: File?, name: String): Boolean {
                    return name.startsWith(".")
                }
            })
            for (file in hiddenFiles!!) {
                val process = Runtime.getRuntime().exec(arrayOf<String>("attrib", "+h", file.getAbsolutePath()))
                process.waitFor()
            }
        }
    }

    val isWindows: Boolean
        get() = Os.isFamily(Os.OS_FAMILY_WINDOWS)

    val testResourcesAsSourceFolder: File
        get() = getTestResourcesAsSourceFolder("/fixture")

    fun getTestResourcesAsSourceFolder(name: String): File {
        return File(TestUtils::class.java.getResource(name).getFile())
    }

    fun newFolder(base: File?, folderName: String): File {
        val templateFolder = File(base, folderName)
        templateFolder.mkdir()
        return templateFolder
    }

    fun getOsPath(path: Path): String {
        if (isWindows) {
            return path.toString().replace("\\", "\\\\")
        }
        return path.toString()
    }
}
