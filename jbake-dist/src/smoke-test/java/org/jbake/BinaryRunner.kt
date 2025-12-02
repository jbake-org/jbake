package org.jbake

import org.apache.commons.lang3.SystemUtils
import java.io.*

class BinaryRunner(private val workingDir: File) {

    @Throws(IOException::class, InterruptedException::class)
    fun runWithArguments(vararg arguments: String?): Process {
        val processBuilder = ProcessBuilder(*arguments)
        processBuilder.directory(workingDir)
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

    companion object {
        private val isWindows = SystemUtils.IS_OS_WINDOWS
        private val gradlePath = if (isWindows) "build\\install\\jbake\\bin\\jbake.bat" else "build/install/jbake/bin/jbake"
        private val mavenPath = if (isWindows) "target\\appassembler\\bin\\jbake.bat" else "target/appassembler/bin/jbake"

        val jbakeExecutableRelative: File
            get() {
                // Check Gradle path first (relative to jbake-dist working directory)
                val gradleFile = File(gradlePath)
                if (gradleFile.exists()) return gradleFile

                // Fall back to Maven path
                val mavenFile = File(mavenPath)
                if (mavenFile.exists()) return mavenFile

                // Default to Gradle path if neither exists (will fail with clear error)
                return gradleFile
            }
    }
}
