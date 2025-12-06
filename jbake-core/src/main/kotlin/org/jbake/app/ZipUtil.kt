package org.jbake.app

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Provides Zip file related functions
 */
object ZipUtil {
    /**
     * Extracts content of Zip file to specified output path.
     */
    @Throws(IOException::class)
    fun extract(inputStream: InputStream, extractToDir: File) {
        val zis = ZipInputStream(inputStream)
        var entry: ZipEntry?
        val buffer = ByteArray(1024)

        while ((zis.getNextEntry().also { entry = it }) != null) {
            val outputFile = extractToDir.toPath().resolve(entry!!.getName()).toFile()
            val outputParent = File(outputFile.getParent())
            outputParent.mkdirs()

            if (entry.isDirectory) {
                if (!outputFile.exists()) outputFile.mkdir()
            } else {
                FileOutputStream(outputFile).use { fos ->
                    var len: Int
                    while ((zis.read(buffer).also { len = it }) > 0) {
                        fos.write(buffer, 0, len)
                    }
                }
            }
        }
    }
}
