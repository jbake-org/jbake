package org.jbake.launcher;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.jbake.app.ZipUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;

import java.io.File;
import java.io.FileInputStream;

/**
 * Initialises sample folder structure with pre-defined template
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 *
 */
public class Init {

    private final JBakeConfiguration config;

    /**
     * @param config The project configuration
     * @deprecated use {@link Init#Init(JBakeConfiguration)} instead
     */
    @Deprecated
    public Init(CompositeConfiguration config) {
        this(new DefaultJBakeConfiguration(config));
    }

    public Init(JBakeConfiguration config) {
        this.config = config;
    }

    /**
     * Performs checks on output folder before extracting template file
     *
     * @param outputFolder                        Target directory for extracting template file
     * @param templateLocationFolder  Source location for template file
     * @param templateType                        Type of the template to be used
     * @throws Exception                            if required folder structure can't be achieved without content overwriting
     */
    public void run(File outputFolder, File templateLocationFolder, String templateType) throws Exception {
        if (!outputFolder.canWrite()) {
            throw new Exception("Output folder is not writeable!");
        }
        if (hasExistingStructure(outputFolder)) {
            throw new Exception(String.format("Output folder '%s' already contains structure!", outputFolder.getAbsolutePath()));
        }
        String exampleProject = config.getExampleProjectByType(templateType);
        if (exampleProject == null) {
            throw new Exception("Cannot locate example project type: " + templateType);
        }
        File templateFile = new File(templateLocationFolder, exampleProject);
        if (!templateFile.exists()) {
            throw new Exception("Cannot find example project file: " + templateFile.getPath());
        }
        ZipUtil.extract(new FileInputStream(templateFile), outputFolder);
    }


    private boolean hasExistingStructure(File outputFolder) {
        File[] contents = outputFolder.listFiles();
        if (contents == null) {
            return false;
        }

        for (File content : contents) {
            if (content.isDirectory() && isJBakeFolder(content.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isJBakeFolder(String name) {
        return name.equalsIgnoreCase(config.getTemplateFolderName())
            || name.equalsIgnoreCase(config.getContentFolderName())
            || name.equalsIgnoreCase(config.getAssetFolderName());
    }
}
