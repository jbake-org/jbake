package org.jbake

import java.io.*

class BinaryRunner(private val folder: File?) {
    @Throws(IOException::class, InterruptedException::class)
    fun runWithArguments(vararg arguments: String?): Process {
        val processBuilder = ProcessBuilder(*arguments)
        processBuilder.directory(folder)
        processBuilder.redirectErrorStream(true)

        val process = processBuilder.start()
        printOutput(process.inputStream)
        process.waitFor()

        return process
    }

    @Throws(IOException::class)
    private fun printOutput(inputStream: InputStream) {
        var line: String?
        val reader = BufferedReader(InputStreamReader(inputStream))

        while ((reader.readLine().also { line = it }) != null) {
            println(line)
        }
        reader.close()
    }
}
