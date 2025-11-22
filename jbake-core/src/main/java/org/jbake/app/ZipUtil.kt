package org.jbake.app

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Provides Zip file related functions
 *
 * @author Jonathan Bullock [jonbullock@gmail.com](mailto:jonbullock@gmail.com)
 */
object ZipUtil {
    /**
     * Extracts content of Zip file to specified output path.
     *
     * @param is             [InputStream] InputStream of Zip file
     * @param outputFolder    folder where Zip file should be extracted to
     * @throws IOException    if IOException occurs
     */
    @Throws(IOException::class)
    fun extract(`is`: InputStream, outputFolder: File) {
        val zis = ZipInputStream(`is`)
        var entry: ZipEntry?
        val buffer = ByteArray(1024)

        while ((zis.getNextEntry().also { entry = it }) != null) {
            val outputFile = File(outputFolder.getCanonicalPath() + File.separatorChar + entry!!.getName())
            val outputParent = File(outputFile.getParent())
            outputParent.mkdirs()

            if (entry.isDirectory) {
                if (!outputFile.exists()) {
                    outputFile.mkdir()
                }
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
