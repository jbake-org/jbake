package org.jbake

import org.apache.commons.lang3.SystemUtils
import org.jbake.util.Logging
import org.jbake.util.Logging.logger
import java.io.File
import java.io.IOException

class BinaryRunner(private val workingDir: File) {

    var processOutput: String = ""
        private set

    @Throws(IOException::class, InterruptedException::class)
    fun runWithArguments(vararg arguments: String?): Process {
        val processBuilder = ProcessBuilder(*arguments)
        processBuilder.directory(workingDir)
        processBuilder.redirectErrorStream(true)

        val process = processBuilder.start()

        // Capture output in a thread to avoid blocking
        val outputBuilder = StringBuilder()
        val outputThread = Thread {
            try {
                process.inputStream.bufferedReader().forEachLine { line ->
                    println(line)
                    outputBuilder.appendLine(line)
                }
            } catch (e: IOException) {
                // Stream may be closed if process terminates - this is expected
            }
        }
        outputThread.start()

        // Wait for process to complete
        process.waitFor()

        // Wait for output thread to finish reading
        outputThread.join(5000) // Wait up to 5 seconds for output to be consumed

        // Store the captured output
        processOutput = outputBuilder.toString()

        return process
    }


    companion object {
        private val isWindows = SystemUtils.IS_OS_WINDOWS
        private val gradlePath = if (isWindows) "build\\install\\jbake\\bin\\jbake.bat" else "build/install/jbake/bin/jbake"
        private val mavenPath = if (isWindows) "target\\appassembler\\bin\\jbake.bat" else "target/appassembler/bin/jbake"

        val jbakeExecutableRelative: File
            get() {
                // Check Gradle path first (relative to jbake-dist working directory)
                val gradleFile: File = File(gradlePath).let { if(it.exists()) return it else it }

                // Fall back to Maven path
                val mavenFile: File = File(mavenPath).let { if(it.exists()) return it else it }

                "No jbake launcher found! CWD: ${File(".")}  Gradle: ${gradleFile.path} Maven: ${mavenFile.path}"
                    .let { throw Exception(it) }

                // Default to Gradle path if neither exists (will fail with clear error)
                //return File("No/built/binary/found")
            }

        private val log by Logging.logger()
    }
}
