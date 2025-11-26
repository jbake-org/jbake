package org.jbake.launcher

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ZipUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.app.configuration.JBakeConfiguration
import java.io.File
import java.io.FileInputStream

/**
 * Initialises sample folder structure with pre-defined template
 */
class Init(private val config: JBakeConfiguration) {

    @Deprecated("use {@link Init#Init(JBakeConfiguration)} instead")
    constructor(config: CompositeConfiguration) : this(DefaultJBakeConfiguration(config))

    /**
     * Performs checks on output folder before extracting template file.
     *
     * @throws Exception if required folder structure can't be achieved without content overwriting.
     */
    fun run(outputDir: File, templateDir: File, templateType: String) {
        if (!outputDir.canWrite()) throw Exception("Output folder is not writeable!")

        val contents = outputDir.listFiles()
        var safe = true
        if (contents != null) {
            for (content in contents) {
                if (!content.isDirectory()) continue
                if (content.getName().equals(config.templateFolderName, ignoreCase = true)) safe = false
                if (content.getName().equals(config.contentFolderName, ignoreCase = true)) safe = false
                if (content.getName().equals(config.assetFolderName, ignoreCase = true)) safe = false
            }
        }

        if (!safe) throw Exception(String.format("Output folder '%s' already contains structure!", outputDir.absolutePath))

        if (config.getExampleProjectByType(templateType) == null)
            throw Exception("Cannot locate example project type: $templateType")

        val templateFile = File(templateDir, config.getExampleProjectByType(templateType))
        if (!templateFile.exists())
            throw Exception("Cannot find example project file: " + templateFile.path)

        ZipUtil.extract(FileInputStream(templateFile), outputDir)
    }
}
